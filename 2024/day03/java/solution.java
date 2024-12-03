void main() throws IOException {

	record IntermediateResult(boolean skip, long value) {
		static IntermediateResult of(boolean skip, MatchResult mr) {
			return new IntermediateResult(skip, Long.parseLong(mr.group("multiplicand")) * Long.parseLong(mr.group("multiplier")));
		}
	}

	record Result(long star1, long star2) {
	}

	var pattern = Pattern.compile("(?<ctrl>don't\\(\\)|do\\(\\))|(?<op>mul\\((?<multiplicand>-?\\d++),(?<multiplier>-?\\d++)\\))");
	try (
		var stdin = new BufferedReader(new InputStreamReader(System.in));
		var lines = stdin.lines()
	) {
		var solution = lines.map(pattern::matcher).flatMap(Matcher::results)
			.<IntermediateResult>gather(Gatherer.ofSequential(AtomicBoolean::new, (skip, match, downstream) -> {
				var ctrl = match.group("ctrl");
				var op = match.group("op");
				if (op != null) {
					return downstream.push(IntermediateResult.of(skip.get(), match));
				}
				skip.set("don't()".equals(ctrl));
				return true;
			})).collect(Collectors.teeing(
				Collectors.summingLong(IntermediateResult::value),
				Collectors.summingLong(t -> t.skip() ? 0 : t.value()),
				Result::new)
			);
		println(solution);
	}
}
