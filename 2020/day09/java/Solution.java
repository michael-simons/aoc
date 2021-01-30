///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 15
//JAVAC_OPTIONS --enable-preview -source 15
//JAVA_OPTIONS --enable-preview

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class Solution {

	private static <T> List<List<T>> kCombinations(int k, List<T> values) {

		var combinations = List.of(List.<T>of());
		for (int depth = 0; depth < k; ++depth) {
			var newCombinations = new ArrayList<List<T>>();
			for (var current : combinations) {
				var startIndex = current.isEmpty() ? 0 : values.indexOf(current.get(current.size() - 1)) + 1;
				for (var element : values.subList(startIndex, values.size())) {
					var next = new ArrayList<T>();
					next.addAll(current);
					next.add(element);
					newCombinations.add(next);
				}
			}
			combinations = newCombinations;
		}
		return combinations;
	}

	record Tuple(long v1, long v2) {

		boolean differentValues() {
			return v1 != v2;
		}

		long sum() {
			return v1 + v2;
		}
	}

	public static void main(String... args) throws IOException {

		var numbers = Files
				.readAllLines(Path.of(args.length == 0 ? "input.txt" : args[0]))
				.stream().map(Long::parseLong)
				.collect(Collectors.toList());
		var lengthOfPreamble = numbers.size() > 25 ? 25 : 5;

		var starOne = -1L;
		for (int i = lengthOfPreamble; i < numbers.size(); ++i) {
			var current = numbers.get(i);
			var preamble = numbers.subList(i - lengthOfPreamble, i);
			var b = kCombinations(2, preamble)
					.stream()
					.map(p -> new Tuple(p.get(0), p.get(1)))
					.filter(Tuple::differentValues)
					.map(Tuple::sum)
					.filter(p -> p.equals(current))
					.findAny()
					.isPresent();
			if (!b) {
				starOne = current;
				break;
			}
		}

		var i = 0;
		var j = 0;
		var sum = 0L;
		while (sum != starOne && i++ < numbers.size()) {
			sum = numbers.get(i);
			j = i;
			while (sum < starOne && j++ < numbers.size()) {
				sum += numbers.get(j);
			}
		}

		var sortedRange = numbers.subList(i, j + 1).stream().sorted().collect(Collectors.toList());
		var starTwo = sortedRange.get(0) + sortedRange.get(sortedRange.size() - 1);

		System.out.println("Star one " + starOne);
		System.out.println("Star two " + starTwo);
	}
}
