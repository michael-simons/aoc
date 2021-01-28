///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 15
//JAVAC_OPTIONS --enable-preview -source 15
//JAVA_OPTIONS --enable-preview

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class Solution {

	record Range(int start, int end) {

		boolean includes(int n) {
			return n >= start && n <= end;
		}
	}

	record Rule(Range r1, Range r2) {

		boolean isValid(int n) {

			return r1.includes(n) || r2.includes(n);
		}
	}

	record Ticket(int[] numbers) {
	}

	record Field(int index, String name) {
	}

	record Input(Map<String, Rule> validRanges, Ticket theTicket, List<Ticket> nearbyTickets) {

		private static final Pattern RANGE_PATTERN = Pattern.compile("([\\w\\s]+): (\\d+)-(\\d+) or (\\d+)-(\\d+)");

		static Input of(List<String> lines) {

			var ranges = new HashMap<String, Rule>();
			var tickets = new ArrayList<Ticket>();

			lines.forEach(line -> {
				var matcher = RANGE_PATTERN.matcher(line);
				if (matcher.matches()) {
					ranges.put(matcher.group(1), new Rule(
						new Range(Integer.parseInt(matcher.group(2)), Integer.parseInt(matcher.group(3))),
						new Range(Integer.parseInt(matcher.group(4)), Integer.parseInt(matcher.group(5)))));
				} else {
					int[] numbers = Arrays.stream(line.split(","))
						.filter(((Predicate<? super String>) String::isBlank).negate())
						.filter(s -> !s.endsWith(":"))
						.mapToInt(Integer::parseInt).toArray();
					if (numbers.length != 0) {
						tickets.add(new Ticket(numbers));
					}
				}
			});

			return new Input(Collections.unmodifiableMap(ranges), tickets.get(0),
				Collections.unmodifiableList(tickets.subList(1, tickets.size())));
		}

		public int getTicketScanningErrorRate() {

			return nearbyTickets.stream()
				.flatMapToInt(t -> Arrays.stream(t.numbers))
				.filter(i -> !validRanges.values().stream().anyMatch(rule -> rule.isValid(i)))
				.sum();
		}

		public Set<Field> computeFields() {

			var numFields = getNumFields();
			var validTicketsNearBy = getValidTicketsNearBy();
			var knownFields = new HashSet<Field>();
			var unknownFields = new HashSet<>(validRanges().keySet());
			while (!unknownFields.isEmpty()) {
				for (int i = 0; i < numFields; i++) {
					var eligible = new HashSet<>(unknownFields);
					for (Ticket t : validTicketsNearBy) {
						eligible.retainAll(findMatchingRulesFor(t.numbers()[i]));
					}
					if (eligible.size() == 1) {
						knownFields.add(new Field(i, eligible.iterator().next()));
						unknownFields.removeAll(eligible);
					}
				}
			}

			return Collections.unmodifiableSet(knownFields);
		}

		private int getNumFields() {

			return this.theTicket.numbers().length;
		}

		private List<Ticket> getValidTicketsNearBy() {

			return nearbyTickets
				.stream().filter(t -> Arrays.stream(t.numbers)
					.allMatch(i -> validRanges.values().stream().anyMatch(rule -> rule.isValid(i))))
				.collect(Collectors.toUnmodifiableList());
		}

		public Set<String> findMatchingRulesFor(int i) {

			return this.validRanges.entrySet().stream()
				.filter(e -> e.getValue().isValid(i))
				.map(Map.Entry::getKey)
				.collect(Collectors.toSet());
		}
	}

	public static void main(String... args) throws IOException {

		var input = Input.of(Files.readAllLines(Path.of(args.length == 0 ? "input.txt" : args[0])));

		var starOne = input.getTicketScanningErrorRate();
		var starTwo = input.computeFields().stream().filter(f -> f.name().startsWith("departure"))
			.mapToLong(e -> input.theTicket().numbers()[e.index()]).reduce(1L, (a, b) -> a * b);

		System.out.println("Star one " + starOne);
		System.out.println("Star two " + starTwo);
	}
}
