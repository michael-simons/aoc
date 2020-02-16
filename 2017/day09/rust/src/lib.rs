//! A library solving Advent Of Code 2017 day 9.

use std::fmt;

pub struct Result {
    pub score: i32,
    pub num_garbage: i32,
}

/// Implementation of the Display-trait for the Result struct.
impl fmt::Display for Result {
    fn fmt(&self, f: &mut fmt::Formatter<'_>) -> fmt::Result {
        write!(f, "Score = {}, Number of Garbage = {}", self.score, self.num_garbage)
    }
}

// tag::theSolution[]
/// Analyzes a stream of characters.
pub fn analyze(stream: &str) -> Result {
    let mut result = Result { score: 0, num_garbage: 0 };

    let mut depth = 0;
    let mut garbage = false;
    let mut cancel = false;

    for piece in stream.chars() {
        if cancel {
            cancel = false;
            continue;
        }
        match piece {
            '!' => cancel = true,
            '>' => garbage = false,
            _ => {
                if garbage {
                    result.num_garbage += 1;
                    continue;
                }
                match piece {
                    '<' => garbage = true,
                    '{' => depth += 1,
                    '}' => {
                        result.score += depth;
                        depth -= 1
                    }
                    _ => continue
                }
            }
        }
    }
    return result;
}
// end::theSolution[]

#[cfg(test)]
mod tests {
    use crate::analyze;

    #[test]
    fn groups_should_be_counted_correctly() {

        let test_data = [
            ("{}", 1),
            ("{{{}}}", 6),
            ("{{},{}}", 5),
            ("{{{},{},{{}}}}", 16),
            ("{<a>,<a>,<a>,<a>}", 1),
            ("{{<ab>},{<ab>},{<ab>},{<ab>}}", 9),
            ("{{<!!>},{<!!>},{<!!>},{<!!>}}", 9),
            ("{{<a!>},{<a!>},{<a!>},{<ab>}}", 3),
            ("{<{},{},{{}}>}", 1),
            ("{{<a>},{<a>},{<a>},{<a>}}", 9),
            ("{{<!>},{<!>},{<!>},{<a>}}", 3)
        ];

        for test in test_data.iter() {
            let result = analyze(test.0);
            assert_eq!(result.score, test.1);
        }
    }

    #[test]
    fn should_count_garbage() {

        let test_data = [
            ("{<>}", 0),
            ("{<random characters>}", 17),
            ("{<<<<>}", 3),
            ("{<{!>}>}", 2),
            ("{<!!>}", 0),
            ("{<!!!>>}", 0),
            ("{<{o\"i!a,<{i<a>}", 10)
        ];

        for test in test_data.iter() {
            let result = analyze(test.0);
            assert_eq!(result.num_garbage, test.1);
        }
    }
}
