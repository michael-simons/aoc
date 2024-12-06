record Position(int x, int y, int dx, int dy) {

	Position backAndRight() {
		return new Position(x - dx, y - dy, -1 * dy, dx);
	}

	Position move() {
		return new Position(x + dx, y + dy, dx, dy);
	}
}

void walk(List<char[]> map, Position position, Predicate<Position> continueOnPosition) {

	while (position.x >= 0 && position.x < map.getFirst().length && position.y >= 0 && position.y < map.size()) {
		if (map.get(position.y)[position.x] == '#') {
			position = position.backAndRight();
		} else if (continueOnPosition.test(position)) {
			position = position.move();
		} else {
			break;
		}
	}
}

void main() throws IOException {

	var map = new ArrayList<char[]>();
	var startPosition = new Position(0, 0, 0, 0);
	try (var stdin = new BufferedReader(new InputStreamReader(System.in))) {
		var y = 0;
		String line;
		while ((line = stdin.readLine()) != null) {
			var cols = line.toCharArray();
			int x;
			if ((x = line.indexOf('^')) >= 0) {
				startPosition = new Position(x, y, 0, -1);
			}
			++y;
			map.add(cols);
		}
	}

	var uniquePositions = new HashSet<Position>();
	walk(map, startPosition, position -> {
		uniquePositions.add(new Position(position.x, position.y, 0, 0));
		return true;
	});

	var star1 = new AtomicInteger(uniquePositions.size());
	var star2 = new AtomicInteger();
	for (Position p : uniquePositions) {
		map.get(p.y)[p.x] = '#';
		var crossedPosition = new HashSet<Position>();
		walk(map, startPosition, position -> {
			var newPosition = crossedPosition.add(position);
			if (!newPosition) {
				star2.incrementAndGet();
			}
			return newPosition;
		});
		map.get(p.y)[p.x] = 'X';
	}
	assert star1.get() == 41 && star2.get() == 6 : "Sample does not work";
	println("Result[star1=%d, star2=%d]".formatted(star1.get(), star2.get()));
}
