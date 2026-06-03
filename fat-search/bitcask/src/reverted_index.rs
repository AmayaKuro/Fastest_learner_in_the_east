use std::{collections::{HashMap, HashSet}};

pub struct RevertedIndex {
    indexes: HashMap<String, Vec<String>>, // word -> list of keys in bitcask memory
}

impl RevertedIndex {
    pub fn new(path: &std::path::Path) -> Self {
        let mut indexes: HashMap<String, HashSet<String>> = HashMap::new(); // Inside db will have duplication 
        
        if let Ok(f) = std::fs::File::open(path) {
            use std::io::{BufRead, BufReader};
            let mut reader = BufReader::new(f);
            let mut line = String::new();
            while let Ok(bytes_read) = reader.read_line(&mut line) {
                if bytes_read == 0 {
                    break;
                }
                let line_content = line.trim_end_matches('\n');
                let parts: Vec<&str> = line_content.splitn(6, ',').collect();
                if parts.len() == 6 {
                    let key = parts[4].to_string();
                    let value = parts[5];
                    
                    let word = value.to_lowercase();
                    let tokens = word.split_whitespace();

                    for token in tokens {
                        indexes
                            .entry(token.to_string())
                            .or_insert_with(HashSet::<String>::new)
                            .insert(key.clone());
                    }
                }
                line.clear();
            }
        }
        Self { indexes: indexes.into_iter().map(|(k, v)| (k, v.into_iter().collect())).collect() }
    }

    pub fn set(&mut self, key: String, data: String) {
        let word = data.to_lowercase();
        let tokens = word.split_whitespace();

        for token in tokens {
            self.indexes
                .entry(token.to_string())
                .or_insert_with(Vec::new)
                .push(key.clone());
        }
    }

    pub fn search(&self, words: &str) -> Option<Vec<String>> {
        let words = words.to_lowercase();
        let mut tokens = words.split_whitespace();

        let first_token = tokens.next()?;
        let mut intersection: std::collections::HashSet<String> = self
            .indexes
            .get(first_token)?
            .iter()
            .cloned()
            .collect();

        for token in tokens {
            if intersection.is_empty() {
                break;
            }
            let current_keys = self.indexes.get(token)?;
            let current_set: std::collections::HashSet<String> = current_keys.iter().cloned().collect();
            intersection.retain(|k| current_set.contains(k));
        }

        Some(intersection.into_iter().collect())
    }
}
