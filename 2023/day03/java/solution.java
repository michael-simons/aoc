import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

record Symbol(String name, int pos, List<Integer> values) {

	Symbol(String name, int pos) {
		this(name, pos, new ArrayList<>());
	}
}

record Candidate(int value, int start, int end) {
}

record Line(String value, List<Symbol> symbols, List<Candidate> candidates) {
	private static final Pattern candiatePattern = Pattern.compile("\\d+");
	private static final Pattern symbolPattern = Pattern.compile("[^.\\d]");

	static Line of(String src) {
		if (src == null) {
			return null;
		}

		var line = new Line(src, new ArrayList<>(), new ArrayList<>());
		var symbols = symbolPattern.matcher(src);
		while (symbols.find()) {
			line.symbols.add(new Symbol(symbols.group(), symbols.start()));
		}

		var matcher = candiatePattern.matcher(line.value);
		while (matcher.find()) {
			var value = Integer.parseInt(matcher.group());
			var candidate = new Candidate(value, matcher.start() - 1, matcher.end());
			line.candidates.add(candidate);
		}

		return line;
	}

	List<Integer> findAdjacent(Line prior) {
		var result = new ArrayList<Integer>();
		for (var candidate : prior.candidates) {
			for (var symbol : symbols) {
				if (candidate.start <= symbol.pos && symbol.pos <= candidate.end) {
					result.add(candidate.value);
					symbol.values.add(candidate.value);
				}
			}
		}
		return result;
	}
}

void main() {

	var partNumbers = new ArrayList<Integer>();
	var gears = new ArrayList<Symbol>();
	var window = new ArrayDeque<Line>();

	try (var stdin = new BufferedReader(new InputStreamReader(System.in))) {
		Line line;
		while ((line = Line.of(stdin.readLine())) != null) {
			window.add(line);
			if (window.size() >= 2) {
				var prior = window.poll();
				var current = window.element();

				partNumbers.addAll(current.findAdjacent(prior));
				partNumbers.addAll(current.findAdjacent(current));
				partNumbers.addAll(prior.findAdjacent(current));

				prior.symbols.stream().filter(s -> "*".equals(s.name)).forEach(gears::add);
			}
		}
		System.out.println("Star 1: " + partNumbers.stream().reduce(0, Integer::sum));
		var star2 = gears.stream()
			.filter(s -> s.values.size() == 2)
			.map(s -> s.values().stream().reduce(1, (v1, v2) -> v1 * v2))
			.reduce(0, Integer::sum);
		System.out.println("Star 2: " + star2);
	} catch (IOException e) {
		throw new UncheckedIOException(e);
	}
}
