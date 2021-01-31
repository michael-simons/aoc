///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 15
//DEPS io.projectreactor:reactor-core:3.3.9.RELEASE

import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class ReactiveSolution {

	static <T> Flux<List<T>> kCombinations(int k, List<T> values) {

		return Flux.just(List.<T>of())
				.expandDeep(current -> {
					if (current.size() == k) {
						return Flux.empty();
					}
					var newCombinations = new ArrayList<List<T>>();
					var startIndex = current.isEmpty() ? 0 : values.indexOf(current.get(current.size() - 1)) + 1;
					for (var element : values.subList(startIndex, values.size())) {
						var next = new ArrayList<T>(current);
						next.add(element);
						newCombinations.add(next);
					}
					return Flux.fromIterable(newCombinations);
				})
				.filter(combination -> combination.size() == k);
	}

	public static void main(String... args) throws IOException {

		var input = args.length == 0 ? EXAMPLE_INPUT : Files.readString(Path.of(args[0]));

		var numbers = input.lines()
				.map(Long::parseLong)
				.collect(Collectors.toList());
		var lengthOfPreamble = numbers.size() > 25 ? 25 : 5;

		var starOne = Flux.range(lengthOfPreamble, numbers.size())
				.map(i -> Tuples.of(numbers.get(i), numbers.subList(i - lengthOfPreamble, i)))
				.filterWhen(t -> kCombinations(2, t.getT2())
						.all(c -> {
							var current = t.getT1();
							var v1 = c.get(0);
							var v2 = c.get(1);
							return v1.equals(v2) || !current.equals(v1 + v2);
						})
				)
				.map(Tuple2::getT1)
				.blockFirst();

		var starTwo = Flux.fromIterable(numbers).index()
				.flatMap(t -> {
					var index = t.getT1();
					var value = t.getT2();
					return Flux.just(value).concat(Flux.fromIterable(numbers.subList((int) (index + 1), numbers.size())))
							.bufferWhile(new Predicate<>() {
								long sum = 0;

								@Override
								public boolean test(Long aLong) {
									return (sum += aLong) <= starOne;
								}
							});
				})
				.filter(w -> w.stream().reduce(0L, (v1, v2) -> v1 + v2).equals(starOne))
				.map(l -> {
					var sortedRange = l.stream().sorted().collect(Collectors.toList());
					return sortedRange.get(0) + sortedRange.get(sortedRange.size() - 1);
				})
				.blockFirst();

		System.out.println("Star one " + starOne);
		System.out.println("Star two " + starTwo);
	}

	private static final String EXAMPLE_INPUT = """
			35
			20
			15
			25
			47
			40
			62
			55
			65
			95
			102
			117
			150
			182
			127
			219
			299
			277
			309
			576
			""";
}
