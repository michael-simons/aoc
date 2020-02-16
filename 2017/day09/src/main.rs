use std::{env, fs};

use stream_processing::analyze;

fn main() {
    let args: Vec<String> = env::args().collect();

    if args.len() < 2 {
        panic!("Need a filename!");
    }

    let filename = &args[1];
    let input = fs::read_to_string(filename)
        .expect("Something went wrong reading the file");
    let result = analyze(&input);

    println! {"{}", result}
}
