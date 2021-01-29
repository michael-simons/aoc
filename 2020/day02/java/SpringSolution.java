///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 15
//JAVAC_OPTIONS --enable-preview -source 15
//JAVA_OPTIONS --enable-preview
//DEPS org.springframework:spring-context:5.3.3

package com.example.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.support.GenericApplicationContext;

public final class SpringSolution {

	record PolicyAndPassword(int min, int max, char c, String value) {
	}

	static class PasswordReader {
		private static final Pattern PATTERN = Pattern.compile("(\\d+)-(\\d+) ([a-zA-Z]): ([a-zA-Z]+)");

		Collection<PolicyAndPassword> read(Stream<String> input) {
			return input
				.map(PATTERN::matcher)
				.filter(Matcher::matches)
				.map(m -> new PolicyAndPassword(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)),
					m.group(3).charAt(0), m.group(4)))
				.collect(Collectors.toList());
		}
	}

	interface ValidationService {
		long countValidPasswords(Collection<PolicyAndPassword> passwords);
	}

	public static void main(String[] args) throws IOException {
		var exampleInput = """
			1-3 a: abcde
			1-3 b: cdefg
			2-9 c: ccccccccc
			""";

		var input = args.length == 0 ? exampleInput : Files.readString(Path.of(args[0]));

		try (var context = new GenericApplicationContext()) {

			context.registerBean(PasswordReader.class);
			context.registerBean("policy1", ValidationService.class, () -> passwords -> passwords
				.stream()
				.filter(password -> {
					var cnt = password.value.chars().filter(c -> c == password.c).count();
					return cnt >= password.min && cnt <= password.max;
				})
				.count());
			context.registerBean("policy2", ValidationService.class, () -> passwords -> passwords
				.stream()
				.filter(password ->
					password.value.charAt(password.min - 1) == password.c ^
					password.value.charAt(password.max - 1) == password.c
				)
				.count());
			context.refresh();

			var possiblePasswords = context.getBean(PasswordReader.class).read(input.lines());

			context.getBeansOfType(ValidationService.class)
				.forEach((name, instance) -> System.out.println("%s valid passwords according to %s".formatted(
					instance.countValidPasswords(possiblePasswords), name)));
		}
	}
}
