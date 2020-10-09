package com.mz.utils;

public class DisplayAs {

	private DisplayAs() {}

	public static String progressBar(int percentage, int size) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < percentage; i++) {
			for (int j = 0; j < size; j++) {
				sb.append("█");
			}
		}

		return sb.toString();
	}

}
