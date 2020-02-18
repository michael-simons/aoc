import static java.lang.Byte.toUnsignedInt;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Solution {

	public static void main(String... a) throws NoSuchAlgorithmException {

		var input = "ugkcyxxp";

		var solution = new Solution(MessageDigest.getInstance("MD5"));

		System.out.println("Pt1 " + solution.computePasswordV1(input));
		System.out.println("Pt2 " + solution.computePasswordV2(input));
	}

	private final MessageDigest digest;
	private final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();

	Solution(MessageDigest digest) {
		this.digest = digest;
	}

	String computePasswordV1(String input) {

		var password = new StringBuilder();
		var index = 0L;

		while (password.length() < 8) {
			var hash = digest.digest((input + index++).getBytes());
			var firstFive = toUnsignedInt(hash[0]) +
				toUnsignedInt(hash[1]) +
				toUnsignedInt((byte) (hash[2] >> 4));

			if (firstFive == 0) {
				password.append(HEX_DIGITS[hash[2] & 0xf]);
			}
		}
		return password.toString();
	}

	String computePasswordV2(String input) {

		var empty = '_';
		var password = new char[8];
		Arrays.fill(password, empty);

		var found = 0;
		var index = 0L;

		while (found < 8) {
			var hash = digest.digest((input + index++).getBytes());
			var firstFive = toUnsignedInt(hash[0]) +
				toUnsignedInt(hash[1]) +
				toUnsignedInt((byte) (hash[2] >> 4));

			if (firstFive == 0) {
				var pos = Character.digit(HEX_DIGITS[hash[2] & 0xf], 10);

				if (pos >= 0 && pos < 8 && password[pos] == empty) {
					password[pos] = HEX_DIGITS[(hash[3] >> 4) & 0xf];
					++found;
				}
			}
		}

		return new String(password);
	}
}
