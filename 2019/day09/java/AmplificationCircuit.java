import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * This is day 2019/7 solution, using day 9s computer.
 */
final class AmplificationCircuit {

	private final List<Long> instructions;

	AmplificationCircuit(List<Long> instructions) {
		this.instructions = instructions;
	}

	long amplify() {

		var initialPhasesAndComputers = LongStream.rangeClosed(0, 4).mapToObj(i -> Map.entry(Solution.Computer.loadProgram(instructions), Optional.of(i))).collect(toList());
		return permutationsOf(initialPhasesAndComputers)
			.map(s -> runSequence(true, 0L, s))
			.max(Long::compareTo)
			.get();
	}

	long amplifyWithFeedback() {

		return permutationsOf(LongStream.rangeClosed(5, 9).mapToObj(Optional::of).collect(toList()))
			.parallel()
			.map(phases -> {
				var computers = IntStream.rangeClosed(0, phases.size() - 1)
					.mapToObj(i -> Solution.Computer.loadProgram(instructions)).collect(toList());

				var phasesIterator = phases.iterator();
				Supplier<Optional<Long>> phaseSupplier = () -> phasesIterator.hasNext() ?
					phasesIterator.next() :
					Optional.empty();
				Supplier<Boolean> nextRun = () -> computers.get(computers.size() - 1).expectsInput();

				var lastOutput = new AtomicLong(0);
				while (nextRun.get()) {
					var sequence = computers.stream().map(c -> Map.entry(c, phaseSupplier.get())).collect(toList());
					lastOutput.set(runSequence(false, lastOutput.get(), sequence));
				}
				return lastOutput.get();
			})
			.max(Long::compareTo)
			.get();
	}

	private static <T> void swap(T[] v, int i, int j) {
		T t = v[i];
		v[i] = v[j];
		v[j] = t;
	}

	@SuppressWarnings("unchecked")
	private static <T> Stream<List<T>> permutationsOf(List<T> v) {
		return permuteImpl((T[]) v.toArray(), v.size());
	}

	/**
	 * See: https://en.wikipedia.org/wiki/Heap%27s_algorithm
	 *
	 * @param v
	 * @param n
	 * @return
	 */
	private static <T> Stream<List<T>> permuteImpl(T[] v, int n) {

		var builder = Stream.<List<T>>builder();
		int c[] = new int[n];

		builder.accept(List.of(v));

		int i = 0;
		while (i < n) {
			if (c[i] < i) {
				if (i % 2 == 0) {
					swap(v, 0, i);
				} else {
					swap(v, c[i], i);
				}
				builder.accept(List.of(v));
				c[i] += 1;
				i = 0;
			} else {
				c[i] = 0;
				i += 1;
			}
		}

		return builder.build();
	}

	private static Long runSequence(boolean b, Long initialInput,
		List<Map.Entry<Solution.Computer, Optional<Long>>> sequence) {
		AtomicLong input = new AtomicLong(initialInput);
		for (var computerAndPhase : sequence) {
			var c = computerAndPhase.getKey();
			c.pipe(input.get());
			computerAndPhase.getValue().ifPresent(c::pipe);
			input.set(c.run(b).drain().stream().findFirst().get());
		}
		return input.get();
	}
}
