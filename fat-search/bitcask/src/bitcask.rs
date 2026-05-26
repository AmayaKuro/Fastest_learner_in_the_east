use std::{
    collections::HashMap,
    fs::OpenOptions,
    io::{Read, Seek, Write},
    path::PathBuf,
    time::{SystemTime, UNIX_EPOCH},
};

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
        Self {
            path: path,
            log_map: HashMap::new(),
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

    pub fn set(&mut self, key: String, value: String) {
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

                let err = f.write(buffer.as_bytes());
                if let Err(e) = err {
                    eprintln!(
                        "Error: Failed to write to database at {:?}: {}",
                        self.path, e
                    );
                    return;
                }

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
                return;
            }
        }

        println!("SET {}={} in {:?} at {}", key, value, self.path, ts);
    }
}
