package com.mz.utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

/**
 * CTE - Custom Text Encryption is an algorithm that can process any string with any
 * key to encrypt it and use the encrypted string with the same key to decrypt it
 * again.
 *
 * @author Marko Zajc
 */
public class CTE {

	/**
	 * A key used in encryption/decryption. It can originate from a file, a string or can
	 * be randomly generated
	 *
	 * @author Marko Zajc
	 */
	public static class Key {

		private final byte[] bytes;

		/**
		 * Generates a new key
		 *
		 * @param size
		 *            new key's size
		 * @param seedSize
		 *            new key's seed size used in random algorithm
		 */
		public Key(int size, int seedSize) {
			Random secRandom = DoSecureRandom.createRandom(seedSize);

			byte[] key = new byte[size];
			secRandom.nextBytes(key);
			this.bytes = key;
		}

		/**
		 * Creates a new key instance using already existing key. seedSize will be set to -1
		 *
		 * @param key
		 */
		public Key(byte[] key) {
			this.bytes = key.clone();
		}

		/**
		 * Reads key from a file
		 *
		 * @param path
		 *            path to the file
		 * @throws IOException
		 *             if an I/O error occurs reading from the stream
		 */
		public Key(Path path) throws IOException {
			this.bytes = Files.readAllBytes(path);
		}

		/**
		 * @return key as a byte array
		 */
		public byte[] getBytes() {
			return this.bytes.clone();
		}

		/**
		 * @return key as a string
		 */
		@Override
		public String toString() {
			return new String(this.bytes, StandardCharsets.UTF_8);
		}
	}

	/**
	 * Mode used in the processing
	 *
	 * @author Marko Zajc
	 */
	public enum Mode {
		ENCRYPT,
		DECRYPT;
	}

	final Key key;

	/**
	 * Creates a new CTE instance using a key
	 *
	 * @param key
	 *            a key that can not be changed later on
	 */
	public CTE(Key key) {
		this.key = key;
	}

	/**
	 * Processes a string.
	 *
	 * @param string
	 *            string to process
	 * @param mode
	 *            encrypt/decrypt modes
	 * @return processed string
	 */
	public String process(String string, Mode mode) {
		char[] keyChars = this.key.toString().toCharArray();
		char[] stringChars = string.toCharArray();
		// Gets bytes from message & key encoded as UTF-8

		int keyPosition = 0; // Current key position
		int i = 0; // Counter
		char[] result = new char[stringChars.length];
		for (char c : stringChars) {
			int processedChar = mode.equals(Mode.ENCRYPT) ? c + keyChars[keyPosition] : c - keyChars[keyPosition];

			result[i] = (char) processedChar;

			keyPosition++;
			if (keyPosition == keyChars.length) {
				keyPosition = 0;
			}

			i++;
		}

		return String.valueOf(result);
	}

}
