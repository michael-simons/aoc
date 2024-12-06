record Position(int x, int y, int dx, int dy) {
	Position backAndRight() {
		return new Position(x - dx, y - dy, -1 * dy, dx);
	}

	Position move() {
		return new Position(x + dx, y + dy, dx, dy);
	}

	boolean inMap(List<char[]> map) {
		return x >= 0 && x < map.getFirst().length && y >= 0 && y < map.size();
	}

	char getValue(List<char[]> map) {
		return map.get(y)[x];
	}
}

static void walk(List<char[]> map, Position position, Predicate<Position> continueOnPosition) {
	while (position.inMap(map)) {
		var v = position.getValue(map);
		if (v == '#') {
			position = position.backAndRight();
		} else {
			if (!continueOnPosition.test(position)) {
				break;
			}
			position = position.move();
		}
	}
}

void main() throws IOException {

	var map = new ArrayList<char[]>();
	var startPosition = new Position(0, 0, 0, 0);
	try (var stdin = new BufferedReader(new InputStreamReader(System.in));) {
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
		map.get(position.y)[position.x] = 'X';
		return true;
	});

	var star1 = new AtomicInteger(uniquePositions.size());
	var star2 = new AtomicInteger();
	for (int r = 0; r < map.size(); ++r) {
		for (int c = 0; c < map.get(r).length; ++c) {
			if (map.get(r)[c] != 'X') {
				continue;
			}

			map.get(r)[c] = '#';
			uniquePositions.clear();
			walk(map, startPosition, position -> {
				var newPosition = uniquePositions.add(position);
				if (!newPosition) {
					star2.incrementAndGet();
				}
				return newPosition;
			});
			map.get(r)[c] = 'X';
		}
	}
	println("Result[star1=%d, star2=%d]".formatted(star1.get(), star2.get()));
}
