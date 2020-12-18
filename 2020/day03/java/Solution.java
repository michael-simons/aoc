///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVAC_OPTIONS --enable-preview -source 15
//JAVA_OPTIONS --enable-preview

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.UnaryOperator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Solution {

    record Slope(int right, int down) {}

    record ADarkAndEndlessForest(List<char[]> patterns) {

        long countTrees(Slope slope) {

            var cnt = 0L;
            var x = 0;
            var y = 0;
            while (x < patterns.size()) {

                var pattern = patterns.get(x);
                if (pattern[y] == '#') {
                    cnt += 1;
                }

                x += slope.down();
                y = (y + slope.right()) % pattern.length;
            }
            return cnt;
        }
    }

    public static void main(String... args) throws IOException {

        var forest = new ADarkAndEndlessForest(
            Files
                .readAllLines(Path.of(args.length == 0 ? "input.txt" : args[0]))
                .stream().map(String::toCharArray)
                .collect(Collectors.toList())
        );

        var starOne = forest.countTrees(new Slope(3, 1));
        System.out.println("Star one: " + starOne);

        var starTwo = Stream.of(
            new Slope(1, 1), 
            new Slope(3, 1), 
            new Slope(5, 1), 
            new Slope(7, 1), 
            new Slope(1, 2)
            ).map(forest::countTrees).reduce(1L, (a, b) -> a * b);
        System.out.println("Star two: " + starTwo);
    }
}
