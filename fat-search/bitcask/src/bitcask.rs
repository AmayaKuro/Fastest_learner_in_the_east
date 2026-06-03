use std::{
    collections::HashMap,
    fs::OpenOptions,
    io::{Read, Seek, Write},
    path::PathBuf,
    time::{SystemTime, UNIX_EPOCH},
};

use anyhow::bail;
use crc::Crc;

struct LogMap {
    file_id: String,
    value_size: usize,
    value_position: u64,
    timestamp: u64,
}

pub struct Bitcask {
    path: PathBuf,
    log_map: HashMap<String, LogMap>,
}

impl Bitcask {
    pub fn new(path: PathBuf) -> Self {
        let mut log_map = HashMap::new();
        if let Ok(f) = std::fs::File::open(&path) {
            use std::io::{BufRead, BufReader};
            let mut reader = BufReader::new(f);
            let mut current_position = 0;
            let mut line = String::new();
            while let Ok(bytes_read) = reader.read_line(&mut line) {
                if bytes_read == 0 {
                    break;
                }
                
                let line_content = line.trim_end_matches('\n');
                let parts: Vec<&str> = line_content.splitn(6, ',').collect();
                if parts.len() == 6 {
                    let ts_str = parts[1];
                    let key = parts[4].to_string();
                    
                    if let Ok(timestamp) = ts_str.parse::<u64>() {
                        log_map.insert(key, LogMap {
                            file_id: path.to_string_lossy().to_string(),
                            value_size: bytes_read,
                            value_position: current_position as u64,
                            timestamp,
                        });
                    }
                }
                current_position += bytes_read;
                line.clear();
            }
        }

        Self {
            path,
            log_map,
        }
    }

    pub fn get(&mut self, key: String) {
        if let Some(log_entry) = self
            .log_map
            .get(&key)
        {
            match OpenOptions::new().read(true).open(log_entry.file_id.clone()) {
                Ok(mut f) => {
                    if let Err(e) = f.seek(std::io::SeekFrom::Start(log_entry.value_position)) {
                        eprintln!(
                            "Error: Failed to seek in database at {:?}: {}",
                            self.path, e
                        );
                        return;
                    }

                    let mut data = vec![0; log_entry.value_size];
                    if let Err(e) = f.read_exact(&mut data) {
                        eprintln!(
                            "Error: Failed to read from database at {:?}: {}",
                            self.path, e
                        );
                        return;
                    }
                    let data = String::from_utf8_lossy(&data);
                    let parts: Vec<&str> = data.trim_end_matches('\n').split(',').collect();
                    if parts.len() > 5 {
                        println!("{}", parts[5]); // crc, timestamp, key_size, value_size, key, value
                    }
                }
                Err(e) => {
                    eprintln!("Error: Cannot access database at {:?}: {}", self.path, e);
                    return;
                }
            }
        }
    }

    pub fn set(&mut self, key: String, value: String) -> anyhow::Result<bool> {
        let now = SystemTime::now();
        let ts = now
            .duration_since(UNIX_EPOCH)
            .map(|d| d.as_secs())
            .unwrap_or(0);
        let key_size = key.len();
        let value_size = value.len();

        let data = ts.to_string()
            + ","
            + key_size.to_string().as_str()
            + ","
            + value_size.to_string().as_str()
            + ","
            + key.as_str()
            + ","
            + value.as_str();

        let mut buf = Vec::new();
        buf.extend_from_slice(data.as_bytes());

        let cs = Crc::<u32>::new(&crc::CRC_32_CKSUM).checksum(&buf);
        let buffer = format!("{},{}\n", cs, data); // data already a comma separated list

        match OpenOptions::new()
            .read(true)
            .write(true)
            .append(true)
            .create(true)
            .open(&self.path)
        {
            Ok(mut f) => {
                let current_position = f.seek(std::io::SeekFrom::End(0));

                f.write(buffer.as_bytes())?;

                self.log_map.insert(
                    key.clone(),
                    LogMap {
                        file_id: self.path.to_string_lossy().to_string(),
                        value_size: buffer.len(),
                        value_position: current_position.unwrap_or(0),
                        timestamp: ts,
                    },
                );
                println!("Write at {:?}", self.path);
            }
            Err(e) => {
                eprintln!("Error: Cannot access database at {:?}: {}", self.path, e);
                bail!(e);
            }
        }

        println!("SET {}={} in {:?} at {}", key, value, self.path, ts);
        return Ok(true);
    }
}
