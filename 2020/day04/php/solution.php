<?php
    define("REQUIRED", array_fill_keys(array('byr', 'iyr', 'eyr' , 'hgt', 'hcl', 'ecl', 'pid', 'cid'), ''));
    define("VALID_EYE_COLORS", array('amb', 'blu', 'brn', 'gry', 'grn', 'hzl', 'oth'));

    function is_valid($passport) {
        $diff = array_keys(array_diff_key(REQUIRED, $passport));
        if(empty($diff) || (sizeof($diff) === 1 && $diff[0] === 'cid')) {
            return 1;
        } else {
            return 0;
        }
    }

    function is_valid2($passport) {

        if(is_valid($passport) !== 1) {
            return 0;
        }
        $valid = true;
        $valid = $valid &&
            array_key_exists('byr', $passport) &&
            strlen($passport['byr']) === 4 &&
            $passport['byr'] >= 1920 && $passport['byr'] <= 2002;

        $valid = $valid &&
            array_key_exists('iyr', $passport) &&
            strlen($passport['iyr']) === 4 &&
            $passport['iyr'] >= 2010 && $passport['iyr'] <= 2020;

        $valid = $valid &&
            array_key_exists('eyr', $passport) &&
            strlen($passport['eyr']) === 4 &&
            $passport['eyr'] >= 2020 && $passport['eyr'] <= 2030;

        $valid = $valid &&
            array_key_exists('hgt', $passport) &&
            preg_match("/((1[5-8][0-9]|19[0-3])cm|(59|6[0-9]|7[0-6])in)/", $passport['hgt']);

        $valid = $valid &&
            array_key_exists('hcl', $passport) &&
            preg_match("/#([0-9]|[a-f]){6}/", $passport['hcl']);

        $valid = $valid &&
            array_key_exists('ecl', $passport) &&
            in_array($passport['ecl'], VALID_EYE_COLORS);

        $valid = $valid &&
            array_key_exists('pid', $passport) &&
            preg_match('/^\d{9}$/', $passport['pid']);

        return $valid ? 1 : 0;
    }

    $handle = @fopen(sizeof($argv) <= 1 ? 'input.txt' : $argv[1], 'r');
    if ($handle) {
        $passport = array();
        $starOne = 0;
        $starTwo = 0;
        while (($line = fgets($handle)) !== false) {
            $line = trim($line);
            if(empty($line)) {
                $starOne += is_valid($passport);
                $starTwo += is_valid2($passport);
                $passport = array();
            } else {
                $items = explode(' ', $line);
                foreach ($items as &$value) {
                    $part = explode(':', $value);
                    $passport[$part[0]] = $part[1];
                }
                unset($value);
            }
        }
        fclose($handle);
        print "Star one: $starOne\n";
        print "Star two: $starTwo\n";
    }
?>