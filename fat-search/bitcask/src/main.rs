use clap::{Parser, Subcommand};

use crate::bitcask::Bitcask;

mod bitcask;

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
    let mut bitcask = Bitcask::new(std::path::PathBuf::from("db/cask.1"));
    
    match args.action {
        Action::Get { key } => {
            bitcask.get(key);
        }
        Action::Set { key, value } => {
            bitcask.set(key.clone(), value);

            bitcask.get(key.clone());
        }
        Action::Delete { key } => println!("DELETE {} from {:?}", key, args.db),
    }
}
