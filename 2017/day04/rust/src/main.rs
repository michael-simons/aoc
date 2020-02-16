use std::env;
use std::fs::File;
use std::io::BufReader;

use high_entropy::{count_valid_passphrases, is_valid, is_valid2};

fn main() {
    let args: Vec<String> = env::args().collect();

    let filename = if args.len() < 2 {
        "input.txt"
    } else {
        &args[1]
    };

    let file = File::open(filename).expect("Something went wrong reading the file");
    let result_of_pt1 = count_valid_passphrases(&mut BufReader::new(file), &is_valid);

    let file = File::open(filename).expect("Something went wrong reading the file");
    let result_of_pt2 = count_valid_passphrases(&mut BufReader::new(file), &is_valid2);

    println!("Valid passphrases {} (v1)", result_of_pt1);
    println!("Valid passphrases {} (v2)", result_of_pt2);
}
