use clap::{Parser, Subcommand};

use crate::bitcask::Bitcask;

use crate::reverted_index::RevertedIndex;

mod bitcask;
mod reverted_index;

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
    /// Search for keys by words
    #[command(alias = "Search", alias = "SEARCH")]
    Search { words: String },
}

fn main() {
    let args = Args::parse();
    let mut bitcask = Bitcask::new(args.db.clone());
    let mut reverted_index = RevertedIndex::new(&args.db);

    match args.action {
        Action::Get { key } => {
            bitcask.get(key);
        }
        Action::Set { key, value } => {
            let result = bitcask.set(key.clone(), value.clone());
            if let Err(e) = result {
                eprintln!(
                    "Error: Failed to write to database at {:?}: {}",
                    args.db, e
                );
                return;
            }

            reverted_index.set(key.clone(), value.clone());

            bitcask.get(key.clone());
        }
        Action::Delete { key } => println!("DELETE {} from {:?}", key, args.db),
        Action::Search { words } => {
            let result = reverted_index.search(&words);
            match result {
                Some(keys) => {
                    // println!("Found keys for '{}':", words);
                    for key in keys {
                        // println!("- {}", key);
                        bitcask.get(key);
                    }
                }
                None => println!("No keys found for '{}'", words),
            }
        }
    }
}
