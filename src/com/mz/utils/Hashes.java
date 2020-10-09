package com.mz.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hashes {

	private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	private Hashes() {}

	/**
	 * Generates given text's hash
	 *
	 * @param text
	 *            text to get hash from
	 * @param algorithm
	 *            algorithm to use in hash generation
	 * @return provided text's hash generated with given algorithm
	 * @throws NoSuchAlgorithmException
	 *             if given algorithm does not exist
	 */
	public static String getHash(String text, String algorithm) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance(algorithm);
		md.update(text.getBytes());
		byte[] digest = md.digest();
		char[] hexChars = new char[digest.length * 2];
		for (int j = 0; j < digest.length; j++) {
			int v = digest[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}
}
