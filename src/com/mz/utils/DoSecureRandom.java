package com.mz.utils;

import java.security.SecureRandom;
import java.util.Random;

public class DoSecureRandom {

	private DoSecureRandom() {}

	/**
	 * Creates a new Random object using securely generated seed
	 *
	 * @param seedSize
	 *            size of the generated seed
	 * @return Random object
	 */
	public static Random createRandom(int seedSize) {
		SecureRandom sRandom = new SecureRandom();
		byte[] seed1 = sRandom.generateSeed(seedSize);
		sRandom.setSeed(seed1);
		return sRandom;
	}

}
