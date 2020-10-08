package com.mz.libot.core.commands.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nonnull;

import com.mz.libot.core.commands.exceptions.runtime.NumberOverflowException;
import com.mz.libot.core.commands.exceptions.startup.MissingParametersException;
import com.mz.libot.utils.Parser;

public class Parameters implements Iterable<String> {

	@Nonnull
	private final String[] parsedParameters;
	private final int parametersDesiredQuantity;

	/**
	 * Formats a string with a command call into array of parameters.
	 *
	 * @param input
	 *            command call
	 * @param limit
	 *            splitter limit, 0 for no limit
	 * @param trim
	 *            whether to trim the results
	 * @param omitName
	 *            whether to omit command's name
	 * @return an array of input parameters
	 */
	@SuppressWarnings("null")
	@Nonnull
	public static String[] parseParameters(String input, int limit, boolean trim, boolean omitName) {
		String[] splitted = input.split("\n| ", limit);

		if (splitted[0].startsWith("<@") && splitted[0].endsWith(">"))
			splitted = input.split("\n| ", limit > 0 ? limit + 1 : 0);

		List<String> result = new ArrayList<>(Arrays.asList(splitted));

		if (omitName) {

			if (result.get(0).startsWith("<@") && result.get(0).endsWith(">"))
				result.remove(1);

			result.remove(0);
		}

		result.removeIf(t -> t.trim().equals(""));

		if (trim)
			result.replaceAll(String::trim);

		return result.toArray(new String[result.size()]);
	}

	/**
	 * Creates parameter system.
	 *
	 * @param quantity
	 *            desired quantity of parameters. If there are more, they will be merged
	 *            with the last parameter
	 * @param command
	 *            full command
	 */
	public Parameters(int quantity, String command) {
		// TODO manual trim control
		this.parsedParameters = parseParameters(command, quantity + 1, true, true);
		this.parametersDesiredQuantity = quantity;
	}

	/**
	 * Gets parameter at index as a string
	 *
	 * @param index
	 *            index of parameter to get
	 * @return parameter at index as an string
	 * @throws MissingParametersException
	 *             If no parameter is at that index.
	 */
	public String get(int index) {
		try {
			return this.parsedParameters[index];
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new MissingParametersException();
		}
	}

	/**
	 * Gets parameter at index as an integer
	 *
	 * @param index
	 *            index of parameter to get
	 * @return parameter at index as an integer
	 * @throws NumberFormatException
	 *             if index is not an integer
	 * @throws MissingParametersException
	 *             if no parameter is at that index
	 * @throws NumberOverflowException
	 *             if the number is too big
	 */
	public int getAsInteger(int index) {
		return Parser.parseInt(this.get(index));
		// Retrieves the parameter and turns it into an integer, if possible
	}

	/**
	 * Checks if parameters are null, if at least one parameter is an empty string or if
	 * there are not enough of them (less than provided parametersDesiredQuantity)
	 *
	 * @return true if everything is OK, false if not
	 */
	public boolean check() {
		return this.check(this.parametersDesiredQuantity);
	}

	/**
	 * Check if at least one parameter is blank {@link String#isBlank()} or if there are
	 * too little parameters (less than {@code min}.
	 *
	 * @param min
	 *            Minimum number of parameters.
	 * @return {@code true} if everything is OK, {@code false} if not.
	 */
	public boolean check(int min) {
		for (String parameter : this.parsedParameters) {
			if (parameter.isBlank())
				return false;
		}

		return min <= this.parsedParameters.length;
	}

	/**
	 * Returns length of current parameters object
	 *
	 * @return quantity of parameters
	 */
	public int size() {
		return this.parsedParameters.length;
	}

	/**
	 * @return parameters as an array
	 */
	public String[] asArray() {
		return this.parsedParameters.clone();
	}

	@Override
	public Iterator<String> iterator() {
		return Arrays.asList(this.parsedParameters).iterator();
	}
}
