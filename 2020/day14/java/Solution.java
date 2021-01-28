///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 15
//JAVAC_OPTIONS --enable-preview -source 15
//JAVA_OPTIONS --enable-preview

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public final class Solution {

	private static final Pattern MASK_PATTERN = Pattern.compile("mask\\s=\\s([01X]+)");
	private static final Pattern MEM_PATTERN = Pattern.compile("mem\\[(\\d+)\\]\\s=\\s(\\d+)");

	record Mask(char[] bits) {

		static Mask of(String mask) {
			return new Mask(mask.toCharArray());
		}

		static Mask empty() {
			return new Mask(new char[0]);
		}

		static Mask of(char[] unresolved, List<Integer> active) {
			var newBits = Arrays.copyOf(unresolved, unresolved.length);
			for (int i = 0; i < newBits.length; ++i) {
				if (newBits[i] == 'X') {
					newBits[i] = active.contains(i) ? '1' : '0';
				}
			}
			return new Mask(newBits);
		}

		Long applyTo(Long value) {

			var result = value;
			for (int i = 0; i < bits.length; ++i) {
				var pos = bits.length - i - 1;
				result = switch (bits[i]) {
					case '0' -> result & ~(1L << pos);
					case '1' -> result | (1L << pos);
					default -> result;
				};
			}
			return result;
		}

		List<Long> resolveFloatingBitsAndApply(Long value) {

			var floatingBits = new ArrayList<Integer>();
			var newMask = new char[bits.length];
			for (int i = 0; i < bits.length; ++i) {
				var pos = bits.length - i - 1;
				newMask[i] = switch (bits[i]) {
					case '0' -> ((value >>> pos) & 1) == 1 ? '1' : '0';
					case '1' -> '1';
					default -> 'X';
				};

				if (newMask[i] == 'X') {
					floatingBits.add(i);
				}
			}

			return combinationsOf(floatingBits)
				.map(combination -> Mask.of(newMask, combination))
				.map(mask -> mask.applyTo(value))
				.collect(Collectors.toUnmodifiableList());
		}
	}

	static long starOne(List<String> input) {

		var memory = new HashMap<Long, Long>();
		var currentMask = Mask.empty();
		for (var instruction : input) {
			var matcher = MASK_PATTERN.matcher(instruction);
			if (matcher.matches()) {
				currentMask = Mask.of(matcher.group(1));
			} else if ((matcher = MEM_PATTERN.matcher(instruction)).matches()) {
				memory.put(Long.valueOf(matcher.group(1)), currentMask.applyTo(Long.valueOf(matcher.group(2))));
			}
		}

		return memory.values().stream().mapToLong(Long::longValue).sum();
	}

	static long starTwo(List<String> input) {

		var memory = new HashMap<Long, Long>();
		var currentMask = Mask.empty();
		for (var instruction : input) {
			var matcher = MASK_PATTERN.matcher(instruction);
			if (matcher.matches()) {
				currentMask = Mask.of(matcher.group(1));
			} else if ((matcher = MEM_PATTERN.matcher(instruction)).matches()) {

				var targetAddresses = currentMask
					.resolveFloatingBitsAndApply(Long.valueOf(matcher.group(1)));
				var newValue = Long.valueOf(matcher.group(2));
				targetAddresses
					.forEach(address -> memory.put(address, newValue));
			}
		}

		return memory.values().stream().mapToLong(Long::longValue).sum();
	}

	public static void main(String... args) throws IOException {

		var input = Files.readAllLines(Path.of(args.length == 0 ? "input.txt" : args[0]));

		System.out.println("Star one " + starOne(input));
		System.out.println("Star two " + starTwo(input));
	}

	private static <T> List<List<T>> kCombinations(int k, List<T> values) {

		if (k == 0 || values.isEmpty()) {
			return List.of(List.of());
		}

		if (k == values.size()) {
			return List.of(values);
		}

		if (k == 1) {
			return values.stream().map(List::of).collect(Collectors.toUnmodifiableList());
		}

		return IntStream.range(0, values.size() - k + 1).boxed()
			.flatMap(i -> {
				var head = values.subList(i, i + 1);
				return kCombinations(k - 1, values.subList(i + 1, values.size()))
					.stream()
					.map(tail -> Stream.concat(head.stream(), tail.stream()).collect(Collectors.toUnmodifiableList()));
			})
			.collect(Collectors.toUnmodifiableList());
	}

	private static <T> Stream<List<T>> combinationsOf(List<T> values) {

		return IntStream.rangeClosed(0, values.size())
			.mapToObj(k -> kCombinations(k, values))
			.flatMap(List::stream);
	}
}
