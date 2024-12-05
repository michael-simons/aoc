void main() throws IOException {

	record Result(long star1, long star2) {
	}

	var rules = new HashMap<String, Set<String>>();
	var updates = new ArrayList<String[]>();

	try (
		var stdin = new BufferedReader(new InputStreamReader(System.in));
	) {
		String line;
		boolean inRules = true;
		while ((line = stdin.readLine()) != null) {
			if (inRules) {
				if (line.isBlank()) {
					inRules = false;
				} else {
					var p = line.indexOf("|");
					rules.computeIfAbsent(line.substring(0, p), _ -> new HashSet<>()).add(line.substring(p + 1));
				}
				continue;
			}
			updates.add(line.split(","));
		}
	}

	Predicate<String[]> isOrdered = numbers -> {
		for (int i = numbers.length - 1; i >= 0; --i) {
			var rule = rules.get(numbers[i]);
			if (rule != null && Arrays.stream(numbers, 0, i).anyMatch(rule::contains)) {
				return false;
			}
		}
		return true;
	};
	ToLongFunction<String[]> pickMiddle = numbers -> Long.parseLong(numbers[numbers.length / 2]);
	UnaryOperator<String[]> sort = numbers -> Arrays.stream(numbers).sorted((o1, o2) -> {
		if (Objects.requireNonNullElseGet(rules.get(o1), Set::of).contains(o2)) {
			return -1;
		} else if (Objects.requireNonNullElseGet(rules.get(o2), Set::of).contains(o1)) {
			return 1;
		}
		return 0;
	}).toArray(String[]::new);

	var solution = updates.stream()
		.collect(Collectors.teeing(
			Collectors.filtering(isOrdered, Collectors.summingLong(pickMiddle)),
			Collectors.filtering(Predicate.not(isOrdered), Collectors.mapping(sort, Collectors.summingLong(pickMiddle))),
			Result::new
		));
	System.out.println(solution);
}
