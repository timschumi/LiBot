package com.mz.libot.utils;

import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

/**
 * Globally available utility classes, mostly for string manipulation.
 *
 * @author Jim q
 */
public class StringUtils {

	/**
	 * Returns an array of strings, one for each line in the string after it has been
	 * wrapped to fit lines of {@code maxWidth}. Lines end with any of CR, LF, or CR LF.
	 * A line ending at the end of the string will not output a further, empty string.
	 * <p>
	 * This code assumes {@code string} is not {@code null}.
	 *
	 * @param string
	 *            the string to split
	 * @param fm
	 *            needed for string width calculations
	 * @param maxWidth
	 *            the max line width, in points
	 * @return a non-empty list of strings
	 */
	public static List<String> wrap(String string, FontMetrics fm, int maxWidth) {
		List<String> lines = splitIntoLines(string);
		if (lines.isEmpty())
			return lines;

		ArrayList<String> strings = new ArrayList<>();
		for (String string2 : lines)
			wrapLineInto(string2, strings, fm, maxWidth);
		return strings;
	}

	/**
	 * Given a line of text and font metrics information, wrap the line and add the new
	 * line(s) to {@code list}.
	 *
	 * @param line
	 *            a line of text
	 * @param list
	 *            an output list of strings
	 * @param fm
	 *            font metrics
	 * @param maxWidth
	 *            maximum width of the line(s)
	 */
	public static void wrapLineInto(String line, List<String> list, FontMetrics fm, int maxWidth) {
		String newLine = line;

		int len = newLine.length();
		int width;
		while (len > 0 && (width = fm.stringWidth(newLine)) > maxWidth) {
			// Guess where to split the line. Look for the next space before
			// or after the guess.
			int guess = len * maxWidth / width;
			String before = newLine.substring(0, guess).trim();

			width = fm.stringWidth(before);
			int pos;
			if (width > maxWidth) // Too long
				pos = findBreakBefore(newLine, guess);
			else { // Too short or possibly just right
				pos = findBreakAfter(newLine, guess);
				if (pos != -1) { // Make sure this doesn't make us too long
					before = newLine.substring(0, pos).trim();
					if (fm.stringWidth(before) > maxWidth)
						pos = findBreakBefore(newLine, guess);
				}
			}
			if (pos == -1)
				pos = guess; // Split in the middle of the word

			list.add(newLine.substring(0, pos).trim());
			newLine = newLine.substring(pos).trim();
			len = newLine.length();
		}
		if (len > 0)
			list.add(newLine);
	}

	/**
	 * Returns the index of the first whitespace character or '-' in {@code line} that is
	 * at or before {@code start}. Returns -1 if no such character is found.
	 *
	 * @param line
	 *            a string
	 * @param start
	 *            where to star looking
	 * @return int
	 */
	public static int findBreakBefore(String line, int start) {
		for (int i = start; i >= 0; --i) {
			char c = line.charAt(i);
			if (Character.isWhitespace(c) || c == '-')
				return i;
		}
		return -1;
	}

	/**
	 * Returns the index of the first whitespace character or '-' in {@code line} that is
	 * at or after {@code start}. Returns -1 if no such character is found.
	 *
	 * @param line
	 *            a string
	 * @param start
	 *            where to star looking
	 * @return int
	 */
	public static int findBreakAfter(String line, int start) {
		int len = line.length();
		for (int i = start; i < len; ++i) {
			char c = line.charAt(i);
			if (Character.isWhitespace(c) || c == '-')
				return i;
		}
		return -1;
	}

	/**
	 * Returns an array of strings, one for each line in the string. Lines end with any
	 * of CR, LF, or CR LF. A line ending at the end of the string will not output a
	 * further, empty string.
	 * <p>
	 * This code assumes {@code string} is not {@code null}.
	 *
	 * @param string
	 *            the string to split
	 * @return a non-empty list of strings
	 */
	public static List<String> splitIntoLines(String string) {
		ArrayList<String> strings = new ArrayList<>();

		int len = string.length();
		if (len == 0) {
			strings.add("");
			return strings;
		}

		int lineStart = 0;

		for (int i = 0; i < len; ++i) {
			char c = string.charAt(i);
			if (c == '\r') {
				int newlineLength = 1;
				if (i + 1 < len && string.charAt(i + 1) == '\n')
					newlineLength = 2;
				strings.add(string.substring(lineStart, i));
				lineStart = i + newlineLength;
				if (newlineLength == 2) // skip \n next time through loop
					++i;
			} else if (c == '\n') {
				strings.add(string.substring(lineStart, i));
				lineStart = i + 1;
			}
		}
		if (lineStart < len)
			strings.add(string.substring(lineStart));

		return strings;
	}

	private StringUtils() {}

}
