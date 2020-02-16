use std::collections::HashSet;
use std::io::BufRead;
use std::iter::FromIterator;

pub fn count_valid_passphrases<R: BufRead>(reader: &mut R, validator: &dyn Fn(&str) -> bool) -> i32 {
    return reader.lines()
        .filter_map(|line| if validator(line.unwrap().as_str()) { Some(1) } else { None })
        .count() as i32;
}

pub fn is_valid(passphrase: &str) -> bool {
    let mut word_map = HashSet::new();
    for word in passphrase.split_whitespace() {
        if word_map.contains(word) {
            return false;
        }
        word_map.insert(word);
    }
    return true;
}

pub fn is_valid2(passphrase: &str) -> bool {
    let mut word_map = HashSet::new();
    for word in passphrase.split_whitespace() {
        let mut chars: Vec<char> = word.chars().collect::<Vec<char>>();
        chars.sort();

        let base_word = String::from_iter(chars.iter());
        if word_map.contains(&base_word) { // Borrow the base_word to the contains method
            return false;
        }
        word_map.insert(base_word);
    }
    return true;
}

#[cfg(test)]
mod tests {
    use crate::{count_valid_passphrases, is_valid, is_valid2};

    #[test]
    fn should_detect_valid_passphrases() {
        assert!(is_valid("aa bb cc dd ee"));
        assert!(is_valid("aa bb cc dd aaa"));
    }

    #[test]
    fn should_detect_invalid_passphrases() {
        assert!(!is_valid("aa bb cc dd aa"));
    }

    #[test]
    fn should_detect_valid_passphrases2() {
        assert!(is_valid2("abcde fghij"));
        assert!(is_valid2("a ab abc abd abf abj"));
    }

    #[test]
    fn should_detect_invalid_passphrases2() {

        assert!(!is_valid2("abcde xyz ecdab"));
        assert!(!is_valid2("oiii ioii iioi iiio"));
    }

    #[test]
    fn should_count_correct() {
        let mut test_data = "aa bb cc dd aa\naa bb cc dd ee".as_bytes();
        let cnt = count_valid_passphrases(&mut test_data, &is_valid);
        assert_eq!(cnt, 1);
    }
}
