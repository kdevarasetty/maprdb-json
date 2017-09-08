package samples.ojai.maprdb_json.util;

import static org.apache.commons.lang.RandomStringUtils.*;
import static java.lang.System.*;

import java.util.Random;

import org.ojai.types.OTimestamp;

/**
 * Useful for data generation
 * @author kirand
 *
 */
public class Randoms {
	
	private static Random r = new Random();

	public static int randomInt() {
		return r.nextInt();
	}

	public static int randomInt(int bound) {
		return r.nextInt(bound);
	}
	
	public static long randomLong() {
		return r.nextLong();
	}

	public static float randomFloat() {
		return r.nextFloat();
	}

	public static double randomFloat(long min, long max) {
		return (double)min + randomFloat() * max;
	}

	public static double randomDouble() {
		return r.nextDouble();
	}
	
	public static double randomDouble(long min, long max) {
		return (double)min + randomDouble() * max;
	}

	public static boolean randomBoolean() {
		return r.nextBoolean();
	}

	public static byte[] randomBytes(int length) {
		byte[] bytes = new byte[length];
		r.nextBytes(bytes);
		return bytes;
	}
	
	public static OTimestamp randomTimestamp() {
		long currTime = System.currentTimeMillis();
		boolean bool = randomBoolean();
		out.println(bool);
		if (bool)
			return new OTimestamp(currTime + (long) (randomDouble() * currTime));
		else
			return new OTimestamp(currTime - (long) (randomDouble() * currTime));
	}
	
	public static String randomString(int length) {
		return random(length);
	}
	
	public static String randomString(int length, boolean letters, boolean numbers) {
		return random(length, letters, numbers);
	}
	
	public static String randomAlphabeticString(int length) {
		return randomAlphabetic(length);
	}

	public static String randomAlphanumericString(int length) {
		return randomAlphanumeric(length);
	}

	public static String randomAsciiString(int length) {
		return randomAscii(length);
	}

	public static String randomNumericString(int length) {
		return randomNumeric(length);
	}
	
	public static void main(String[] args) {
		out.println(randomDouble());
		out.println(randomTimestamp());
	}
}
