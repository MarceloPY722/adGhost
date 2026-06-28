use adblock::engine::Engine;
use adblock::lists::{FilterSet, ParseOptions};
use std::fs;
use std::fs::File;
use std::io::Write;
use std::path::Path;

fn main() {
    let list_files = vec![
        "easylist.txt",
        "easyprivacy.txt",
        "ublock-unbreak.txt",
    ];

    let data_dir = Path::new(env!("CARGO_MANIFEST_DIR")).parent().unwrap().join("app/src/main/assets");

    let mut combined = String::new();

    for file_name in &list_files {
        let path = data_dir.join(file_name);
        println!("Loading: {}", path.display());
        match fs::read_to_string(&path) {
            Ok(content) => {
                let lines = content.lines().count();
                combined.push_str(&content);
                combined.push('\n');
                println!("  -> {} lines loaded", lines);
            }
            Err(e) => {
                eprintln!("Failed to load {}: {}", file_name, e);
            }
        }
    }

    println!("Total: {} lines, {} bytes", combined.lines().count(), combined.len());

    println!("Creating FilterSet...");
    let mut filter_set = FilterSet::new(false);
    filter_set.add_filter_list(&combined, ParseOptions::default());
    drop(combined);

    println!("Creating Engine...");
    let engine = Engine::from_filter_set(filter_set, true);

    println!("Serializing...");
    let serialized = engine.serialize();
    println!("Serialized size: {} bytes ({} MB)", serialized.len(), serialized.len() as f64 / 1024.0 / 1024.0);

    let out_path = data_dir.join("adblock_engine.dat");
    let mut file = File::create(&out_path).expect("Failed to create output file");
    file.write_all(&serialized).expect("Failed to write");
    println!("Saved to: {}", out_path.display());
}
