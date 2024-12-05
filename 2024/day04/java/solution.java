void main() throws IOException {

	var puzzle = new ArrayList<String>();
	try (
		var stdin = new BufferedReader(new InputStreamReader(System.in));
		var lines = stdin.lines()
	) {
		lines.forEach(puzzle::add);
	}

	final int[] DX = {1, -1, 1, -1, 0, 1, 0, -1};
	final int[] DY = {1, -1, -1, 1, 1, 0, -1, 0};
	final char[] XMAS = {'X', 'M', 'A', 'S'};

	IntBinaryOperator star1 = (x, y) -> {
		int result = 0;
		outer:
		for (int s = 0; s < DX.length; ++s) {
			var lastX = x + 3 * DX[s];
			var lastY = y + 3 * DY[s];
			if (lastX < 0 || lastX >= puzzle.size() || lastY < 0 || lastY >= puzzle.get(x).length()) {
				continue;
			}
			for (int i = 0; i < XMAS.length; i++) {
				var v = puzzle.get(x + i * DX[s]).charAt(y + i * DY[s]);
				if (v != XMAS[i]) {
					continue outer;
				}
			}
			++result;
		}
		return result;
	};

	IntBinaryOperator star2 = (x, y) -> {
		if (x - 1 < 0 || x + 1 >= puzzle.size() || y - 1 < 0 || y + 1 >= puzzle.get(x).length()) {
			return 0;
		}
		var line = puzzle.get(x);
		var v = line.charAt(y);
		if (v != 'A') {
			return 0;
		}

		for (int s = 0; s < DX.length / 2; s += 2) {
			var c1 = puzzle.get(x + DX[s]).charAt(y + DY[s]);
			var c2 = puzzle.get(x + DX[s + 1]).charAt(y + DY[s + 1]);
			if (!(c1 == 'M' && c2 == 'S' || c1 == 'S' && c2 == 'M')) {
				return 0;
			}
		}
		return 1;
	};

	int cnt1 = 0;
	int cnt2 = 0;
	for (int i = 0; i < puzzle.size(); ++i) {
		var line = puzzle.get(i);
		for (int j = 0; j < line.length(); ++j) {
			cnt1 += star1.applyAsInt(i, j);
			cnt2 += star2.applyAsInt(i, j);
		}
	}
	println("Star 1 " + cnt1);
	println("Star 2 " + cnt2);
}
