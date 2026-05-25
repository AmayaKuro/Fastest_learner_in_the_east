use clap::{Parser, Subcommand};
use crc::Crc;
use std::fs::OpenOptions;
use std::io::Write;
use std::time::{SystemTime, UNIX_EPOCH};

#[derive(Parser)]
#[command(name = "bitcask")]
struct Args {
    /// The path to the database file
    #[arg(long, default_value = "db/cask.1")]
    db: std::path::PathBuf,

    #[command(subcommand)]
    action: Action,
}

#[derive(Subcommand)]
enum Action {
    /// Get a value by key
    #[command(alias = "Get", alias = "GET")]
    Get { key: String },
    /// Set a value for a key
    #[command(alias = "Set", alias = "SET")]
    Set { key: String, value: String },
    /// Delete a key
    #[command(alias = "Delete", alias = "DELETE")]
    Delete { key: String },
}

fn main() {
    let args = Args::parse();

    match args.action {
        Action::Get { key } => println!("GET {} from {:?}", key, args.db),
        Action::Set { key, value } => {
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

            match OpenOptions::new()
                .read(true)
                .write(true)
                .append(true)
                .create(true)
                .open(&args.db)
            {
                Ok(mut f) => {
                    let cs = Crc::<u32>::new(&crc::CRC_32_CKSUM).checksum(&buf);
                    let err = f.write_fmt(format_args!("{},", cs));
                    if let Err(e) = err {
                        eprintln!("Error: Failed to write to database at {:?}: {}", args.db, e);
                        return;
                    }
                    let err = f.write_fmt(format_args!("{}", data)); // data already a comma separated list
                    if let Err(e) = err {
                        eprintln!("Error: Failed to write to database at {:?}: {}", args.db, e);
                        return;
                    }
                    println!("Database available at {:?}", args.db);

                }
                Err(e) => {
                    eprintln!("Error: Cannot access database at {:?}: {}", args.db, e);
                    return;
                }
            }

            println!("SET {}={} in {:?} at {}", key, value, args.db, ts);
        }
        Action::Delete { key } => println!("DELETE {} from {:?}", key, args.db),
    }
}
