import java.io.*;
import java.math.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;

record Neighbour(String left, String right) {}

static long numSteps(List<String> instructions, Map<String, Neighbour> graph, String start, Predicate<String> end) {
	if (!graph.containsKey(start)) {
		return -1;
	}

	var cnt = 0;
	while (!end.test(start)) {
		for (var instruction : instructions) {
			var node = graph.get(start);
			start = switch (instruction) {
				case "L" -> node.left;
				case "R" -> node.right;
				default -> throw new RuntimeException();
			};
			++cnt;
		}
	}
	return cnt;
}

static Map<Long, Long> primeFactorization(long number) {
	var result = new HashMap<Long, Long>();
	var z = BigInteger.valueOf(number);
	while (z.compareTo(BigInteger.ONE) >= 1) {
		var prime = BigInteger.TWO;
		BigInteger factor = null;
		while (prime.pow(2).compareTo(z) <= 0 && factor == null) {
			if (z.remainder(prime).equals(BigInteger.ZERO)) {
				factor = prime;
			} else {
				prime = prime.nextProbablePrime();
			}
		}
		if (factor == null) {
			factor = z;
		}
		result.merge(factor.longValue(), 1L, Long::sum);
		z = z.divide(factor);
	}
	return result;
}

void main() throws Exception {
	var graph = new HashMap<String, Neighbour>();
	var instructions = new ArrayList<String>();

	try (var stdin = new BufferedReader(new InputStreamReader(System.in))) {
		var nodePattern = Pattern.compile("(\\w{3}) = \\((\\w{3}), (\\w{3})\\)");
		String line;
		while ((line = stdin.readLine()) != null) {
			if (line.isBlank()) {
				continue;
			}
			var m = nodePattern.matcher(line);
			if (!m.matches() && instructions.isEmpty()) {
				instructions.addAll(Arrays.asList(line.split("")));
			} else {
				graph.put(m.group(1), new Neighbour(m.group(2), m.group(3)));
			}
		}
	}

	try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {

		var part1 = scope.fork(() -> numSteps(instructions, graph, "AAA", "ZZZ"::equals));

		// Each xxA node ends up in the same xxZ node, so applying all instructions at once goes in circles. I tried to
		// wait this out, but my patience is limited. The total number of steps is the least common multiple of the
		// number of steps from each start to each goal then.
		var tasks = graph.keySet().stream()
			.filter(n -> n.endsWith("A"))
			.map(start -> scope.fork(() -> primeFactorization(numSteps(instructions, graph, start, ut -> ut.endsWith("Z")))))
			.toList();

		scope.join().throwIfFailed();
		var part2 = tasks.stream()
			.map(StructuredTaskScope.Subtask::get)
			// Computest the lowest common multiple from the prime factors of the highest order
			.reduce(new HashMap<>(), (m1, m2) -> {
				m2.forEach((k, v) -> m1.merge(k, v, Math::max));
				return m1;
			})
			.entrySet().stream()
			.mapToLong(e -> (long) Math.pow(e.getKey(), e.getValue()))
			.reduce(1L, (l1, l2) -> l1 * l2);

		System.out.println("Star 1: " + part1.get());
		System.out.println("Star 2: " + part2);
	}
}
