///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 15
//JAVAC_OPTIONS --enable-preview -source 15
//JAVA_OPTIONS --enable-preview

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Solution {

	record Present(int l, int w, int h) {

		static Present of(String values) {
			var hlp = values.split("x");
			return new Present(Integer.parseInt(hlp[0]), Integer.parseInt(hlp[1]), Integer.parseInt(hlp[2]));
		}

		int surfaceArea() {
			return 2 * (l * w + w * h + h * l);
		}

		int slack() {
			return Math.min(l * w, Math.min(w * h, h * l));
		}

		int volume() {
			return l * w * h;
		}

		int smallestPerimeter() {
			return Stream.of(l, w, h).sorted().limit(2).mapToInt(v -> 2 * v).sum();
		}
	}

	public static void main(String... args) throws IOException {
		var presents = Files
				.readAllLines(Path.of(args.length == 0 ? "input.txt" : args[0]))
				.stream().map(Present::of)
				.collect(Collectors.toList());

		var starOne = presents.stream().mapToInt(p -> p.surfaceArea() + p.slack()).sum();
		var starTwo = presents.stream().mapToInt(p -> p.volume() + p.smallestPerimeter()).sum();

		System.out.println("Star one " + starOne);
		System.out.println("Star two " + starTwo);
	}
}
