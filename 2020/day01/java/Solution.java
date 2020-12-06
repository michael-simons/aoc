import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Solution {

	record PolicyAndPassword(int min, int max, char c, String value) {}

	public static void main(String... a) throws IOException {

		var pattern = Pattern.compile("(\\d+)-(\\d+) ([a-zA-Z]): ([a-zA-Z]+)");
		var passwords = Files.readAllLines(Path.of("input.txt"))
			.stream()
			.map(pattern::matcher)
			.filter(Matcher::matches)
			.map(m -> new PolicyAndPassword(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), m.group(3).charAt(0), m.group(4)))
			.collect(toList());


		var star1 = passwords
			.stream()
			.filter(password -> {
				var cnt = password.value.chars().filter(c -> c == password.c).count();
				return cnt >= password.min && cnt <= password.max;
			})
			.count();
		System.out.println(star1);
		
		var star2 = passwords
			.stream()
			.filter(password -> 
				password.value.charAt(password.min-1) == password.c ^ 
				password.value.charAt(password.max-1) == password.c
			)
			.count();
		System.out.println(star2);	
	}
}
