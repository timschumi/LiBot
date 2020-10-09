package com.mz.utils;

import java.util.Random;

public class KeyGenerator {

	private KeyGenerator() {}

	/**
	 * Generates a key using a secure random algorithm
	 *
	 * @param length
	 *            - length of the generated key
	 * @param seedSize
	 *            - size of seed used in random generator
	 * @return generated key
	 */
	public static String generateKey(int length, int seedSize) {
		StringBuilder result = new StringBuilder();
		Random secRandom = DoSecureRandom.createRandom(seedSize);
		for (long i = 0; i <= length - 1; i++) {
			float random = secRandom.nextFloat();
			char character = (char) (Math.round(random * 255) + 33);
			result.append(Character.toString(character));
		}
		return result.toString();
	}
}
