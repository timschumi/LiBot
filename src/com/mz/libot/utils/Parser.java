package com.mz.libot.utils;

import java.math.BigInteger;

import com.mz.libot.core.commands.exceptions.runtime.NumberOverflowException;

public class Parser {

	@SuppressWarnings("unused")
	public static int parseInt(String s) {
		try {
			return Integer.parseInt(s);

		} catch (NumberFormatException e) {
			new BigInteger(s);

			throw new NumberOverflowException();
		}

	}

	@SuppressWarnings("unused")
	public static long parseLong(String s) {
		try {
			return Long.parseLong(s);

		} catch (NumberFormatException e) {
			new BigInteger(s);

			throw new NumberOverflowException();
		}

	}

}
