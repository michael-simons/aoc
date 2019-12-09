import static java.util.stream.Collectors.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Tests {

	public static void main(String... a) throws IOException {

		assert Solution.Computer.loadProgram(List.of(1102L, 34915192L, 34915192L, 7L, 4L, 7L, 99L, 0L)).run().head()
			.equals(Optional.of(34915192L * 34915192L));
		assert Solution.Computer.loadProgram(List.of(104L, 1125899906842624L, 99L)).run().head()
			.equals(Optional.of(1125899906842624L));

		var quine = List.of(109L, 1L, 204L, -1L, 1001L, 100L, 1L, 100L, 1008L, 100L, 16L, 101L, 1006L, 101L, 0L, 99L);
		assert quine.equals(Solution.Computer.loadProgram(quine).run().drain());

		assert new AmplificationCircuit(
			List.of(3L, 15L, 3L, 16L, 1002L, 16L, 10L, 16L, 1L, 16L, 15L, 15L, 4L, 15L, 99L, 0L, 0L)).amplify()
			== 43210L;
		assert new AmplificationCircuit(
			List.of(3L, 23L, 3L, 24L, 1002L, 24L, 10L, 24L, 1002L, 23L, -1L, 23L, 101L, 5L, 23L, 23L, 1L, 24L, 23L, 23L,
				4L, 23L, 99L, 0L, 0L)).amplify() == 54321;
		assert new AmplificationCircuit(
			List.of(3L, 26L, 1001L, 26L, -4L, 26L, 3L, 27L, 1002L, 27L, 2L, 27L, 1L, 27L, 26L, 27L, 4L, 27L, 1001L, 28L,
				-1L, 28L, 1005L, 28L, 6L, 99L, 0L, 0L, 5L)).amplifyWithFeedback() == 139629729;
		assert new AmplificationCircuit(
			List.of(3L, 52L, 1001L, 52L, -5L, 52L, 3L, 53L, 1L, 52L, 56L, 54L, 1007L, 54L, 5L, 55L, 1005L, 55L, 26L,
				1001L, 54L, -5L, 54L, 1105L, 1L, 12L, 1L, 53L, 54L, 53L, 1008L, 54L, 0L, 55L, 1001L, 55L, 1L, 55L, 2L,
				53L, 55L, 53L, 4L, 53L, 1001L, 56L, -1L, 56L, 1005L, 56L, 6L, 99L, 0L, 0L, 0L, 0L, 10L))
			.amplifyWithFeedback() == 18216;

		var instructions = Files.readAllLines(Path.of("../../day07/java/input.txt")).stream()
			.flatMap(s -> Arrays.stream(s.split(",")))
			.map(String::trim)
			.map(Long::parseLong)
			.collect(toList());
		var amplifier = new AmplificationCircuit(instructions);
		assert amplifier.amplify() == 92663L;
		assert amplifier.amplifyWithFeedback() == 14365052L;
		
		instructions = Files.readAllLines(Path.of("input.txt")).stream()
			.flatMap(s -> Arrays.stream(s.split(",")))
			.map(String::trim)
			.map(Long::parseLong)
			.collect(toList());

		var computer = Solution.Computer.loadProgram(instructions);
		assert computer.pipe(1L).run().head().equals(Optional.of(3601950151L));
		assert computer.pipe(2L).run().head().equals(Optional.of(64236L));
	}
}
