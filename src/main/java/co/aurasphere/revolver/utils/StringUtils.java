/*
 * MIT License
 *
 * Copyright (c) 2016-2017 Donato Rimenti.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package co.aurasphere.revolver.utils;

import java.util.List;

/**
 * Utility class for String operations.
 * 
 * @author Donato Rimenti
 */
public class StringUtils {

	/**
	 * Instantiates a new StringUtils.
	 */
	private StringUtils() {
	}

	/**
	 * Returns the string passed as argument with the first character uppercase.
	 * 
	 * @param string
	 *            the string to capitalize.
	 * @return the string passed as argument with the first character uppercase.
	 */
	public static String capitalize(String string) {
		return string.substring(0, 1).toUpperCase()
				+ string.substring(1, string.length());
	}

	/**
	 * Returns a string representing the content of a list of strings, separed
	 * by commas.
	 * 
	 * @param stringList
	 *            the list to stringify.
	 * @return a string representing the list passed as argument.
	 */
	public static String stringListToString(List<String> stringList) {
		if (stringList == null) {
			return "";
		}
		StringBuilder builder = new StringBuilder();
		for (String s : stringList) {
			builder.append(s).append(", ");
		}
		// Removes the last comma.
		int len = builder.length();
		if (len > 0) {
			builder.delete(len - 2, len);
		}
		return builder.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "StringUtils []";
	}

}
