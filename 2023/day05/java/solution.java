import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Stream;

record Range(long start, long len) {
}

static long searchMin(ToLongFunction<Long> function, long start, long len) {
	// Reached a single value range, compute the min there
	if (len == 1) {
		return Math.min(function.applyAsLong(start), function.applyAsLong(start + 1));
	}

	var l2 = len / 2;
	long middle = start + l2;

	var locationAtStart = function.applyAsLong(start);
	var locationAtMiddle = function.applyAsLong(middle);
	var locationAtEnd = function.applyAsLong(start + len);

	var result = Long.MAX_VALUE;
	// Was the first half monotonous increasing?
	if (locationAtStart + l2 != locationAtMiddle) {
		// Recompute the min in that half
		result = searchMin(function, start, l2);
	}
	// Was the second half monotonous increasing?
	if (locationAtMiddle + (len - l2) != locationAtEnd) {
		// Recompute the min in that half
		result = Math.min(result, searchMin(function, middle, (len - l2)));
	}
	return result;
}

void main() throws Exception {

	var seeds = new ArrayList<Range>();
	var maps = new ArrayList<NavigableMap<Long, Range>>();

	try (var stdin = new BufferedReader(new InputStreamReader(System.in))) {
		String line;
		NavigableMap<Long, Range> currentMap = null;
		while ((line = stdin.readLine()) != null) {
			if (line.isEmpty()) {
				if (currentMap != null) {
					maps.add(currentMap);
				}
				currentMap = null;
			} else if (line.startsWith("seeds:")) {
				var values = line.substring(line.indexOf(":") + 1).trim().split(" ");
				for (int i = 0; i < values.length; i += 2) {
					var start = Long.parseLong(values[i]);
					var len = Long.parseLong(values[i + 1]);
					seeds.add(new Range(start, len));
				}
			} else if (line.endsWith("map:")) {
				currentMap = new TreeMap<>();
			} else if (currentMap != null) {
				var values = line.split(" ");
				currentMap.put(Long.parseLong(values[1]), new Range(Long.parseLong(values[0]), Long.parseLong(values[2])));
			}
		}
		if (currentMap != null) {
			maps.add(currentMap);
		}
	}

	ToLongFunction<Long> findLocation = seed -> {
		for (var map : maps) {
			var entry = map.floorEntry(seed);
			if (entry != null && seed < entry.getKey() + entry.getValue().len) {
				seed = ((seed - entry.getKey()) + entry.getValue().start);
			}
		}
		return seed;
	};

	try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
		var tasks = new ArrayList<Subtask<Long>>();
		tasks.add(scope.fork(() -> seeds.stream().flatMap(r -> Stream.of(r.start, r.len))
			.mapToLong(findLocation)
			.min().orElseThrow()));

		Function<Range, Callable<Long>> newComputeLocationTask = range ->
			() -> searchMin(findLocation, range.start, range.len);

		seeds.stream()
			.map(newComputeLocationTask)
			.map(scope::fork)
			.forEach(tasks::add);

		scope.join().throwIfFailed();

		var part1 = tasks.get(0).get();
		var part2 = tasks.stream().skip(1)
			.map(Subtask::get)
			.min(Long::compareTo)
			.orElseThrow();

		System.out.println("Star 1: " + part1);
		System.out.println("Star 2: " + part2);
	}
}
