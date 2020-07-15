/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

/* Adapted from org.apache.commons.text.WordUtils 1.6 (newline detection added) */

package de.george.g3utils.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;

/**
 * <p>
 * Operations on Strings that contain words.
 * </p>
 * <p>
 * This class tries to handle <code>null</code> input gracefully. An exception will not be thrown
 * for a <code>null</code> input. Each method documents its behavior in more detail.
 * </p>
 *
 * @since 1.1
 */
public class WordWrapUtils {
	// Wrapping
	// --------------------------------------------------------------------------
	/**
	 * <p>
	 * Wraps a single line of text, identifying words by <code>' '</code>.
	 * </p>
	 * <p>
	 * New lines will be separated by the system property line separator. Very long words, such as
	 * URLs will <i>not</i> be wrapped.
	 * </p>
	 * <p>
	 * Leading spaces on a new line are stripped. Trailing spaces are not stripped.
	 * </p>
	 * <table border="1">
	 * <caption>Examples</caption>
	 * <tr>
	 * <th>input</th>
	 * <th>wrapLength</th>
	 * <th>result</th>
	 * </tr>
	 * <tr>
	 * <td>null</td>
	 * <td>*</td>
	 * <td>null</td>
	 * </tr>
	 * <tr>
	 * <td>""</td>
	 * <td>*</td>
	 * <td>""</td>
	 * </tr>
	 * <tr>
	 * <td>"Here is one line of text that is going to be wrapped after 20 columns."</td>
	 * <td>20</td>
	 * <td>"Here is one line of\ntext that is going\nto be wrapped after\n20 columns."</td>
	 * </tr>
	 * <tr>
	 * <td>"Click here to jump to the commons website - http://commons.apache.org"</td>
	 * <td>20</td>
	 * <td>"Click here to jump\nto the commons\nwebsite -\nhttp://commons.apache.org"</td>
	 * </tr>
	 * <tr>
	 * <td>"Click here, http://commons.apache.org, to jump to the commons website"</td>
	 * <td>20</td>
	 * <td>"Click here,\nhttp://commons.apache.org,\nto jump to the\ncommons website"</td>
	 * </tr>
	 * </table>
	 * (assuming that '\n' is the systems line separator)
	 *
	 * @param str the String to be word wrapped, may be null
	 * @param wrapLength the column to wrap the words at, less than 1 is treated as 1
	 * @return a line with newlines inserted, <code>null</code> if null input
	 */
	public static String wrap(final String str, final int wrapLength) {
		return wrap(str, wrapLength, null, false);
	}

	/**
	 * <p>
	 * Wraps a single line of text, identifying words by <code>' '</code>.
	 * </p>
	 * <p>
	 * Leading spaces on a new line are stripped. Trailing spaces are not stripped.
	 * </p>
	 * <table border="1">
	 * <caption>Examples</caption>
	 * <tr>
	 * <th>input</th>
	 * <th>wrapLength</th>
	 * <th>newLineString</th>
	 * <th>wrapLongWords</th>
	 * <th>result</th>
	 * </tr>
	 * <tr>
	 * <td>null</td>
	 * <td>*</td>
	 * <td>*</td>
	 * <td>true/false</td>
	 * <td>null</td>
	 * </tr>
	 * <tr>
	 * <td>""</td>
	 * <td>*</td>
	 * <td>*</td>
	 * <td>true/false</td>
	 * <td>""</td>
	 * </tr>
	 * <tr>
	 * <td>"Here is one line of text that is going to be wrapped after 20 columns."</td>
	 * <td>20</td>
	 * <td>"\n"</td>
	 * <td>true/false</td>
	 * <td>"Here is one line of\ntext that is going\nto be wrapped after\n20 columns."</td>
	 * </tr>
	 * <tr>
	 * <td>"Here is one line of text that is going to be wrapped after 20 columns."</td>
	 * <td>20</td>
	 * <td>"&lt;br /&gt;"</td>
	 * <td>true/false</td>
	 * <td>"Here is one line of&lt;br /&gt;text that is going&lt; br /&gt;to be wrapped after&lt;br
	 * /&gt;20 columns."</td>
	 * </tr>
	 * <tr>
	 * <td>"Here is one line of text that is going to be wrapped after 20 columns."</td>
	 * <td>20</td>
	 * <td>null</td>
	 * <td>true/false</td>
	 * <td>"Here is one line of" + systemNewLine + "text that is going" + systemNewLine + "to be
	 * wrapped after" + systemNewLine + "20 columns."</td>
	 * </tr>
	 * <tr>
	 * <td>"Click here to jump to the commons website - http://commons.apache.org"</td>
	 * <td>20</td>
	 * <td>"\n"</td>
	 * <td>false</td>
	 * <td>"Click here to jump\nto the commons\nwebsite -\nhttp://commons.apache.org"</td>
	 * </tr>
	 * <tr>
	 * <td>"Click here to jump to the commons website - http://commons.apache.org"</td>
	 * <td>20</td>
	 * <td>"\n"</td>
	 * <td>true</td>
	 * <td>"Click here to jump\nto the commons\nwebsite -\nhttp://commons.apach\ne.org"</td>
	 * </tr>
	 * </table>
	 *
	 * @param str the String to be word wrapped, may be null
	 * @param wrapLength the column to wrap the words at, less than 1 is treated as 1
	 * @param newLineStr the string to insert for a new line, <code>null</code> uses the system
	 *            property line separator
	 * @param wrapLongWords true if long words (such as URLs) should be wrapped
	 * @return a line with newlines inserted, <code>null</code> if null input
	 */
	public static String wrap(final String str, final int wrapLength, final String newLineStr, final boolean wrapLongWords) {
		return wrap(str, wrapLength, newLineStr, wrapLongWords, " ", null);
	}

	/**
	 * <p>
	 * Wraps a single line of text, identifying words by <code>wrapOn</code>.
	 * </p>
	 * <p>
	 * Leading spaces on a new line are stripped. Trailing spaces are not stripped.
	 * </p>
	 * <table border="1">
	 * <caption>Examples</caption>
	 * <tr>
	 * <th>input</th>
	 * <th>wrapLength</th>
	 * <th>newLineString</th>
	 * <th>wrapLongWords</th>
	 * <th>wrapOn</th>
	 * <th>result</th>
	 * </tr>
	 * <tr>
	 * <td>null</td>
	 * <td>*</td>
	 * <td>*</td>
	 * <td>true/false</td>
	 * <td>*</td>
	 * <td>null</td>
	 * </tr>
	 * <tr>
	 * <td>""</td>
	 * <td>*</td>
	 * <td>*</td>
	 * <td>true/false</td>
	 * <td>*</td>
	 * <td>""</td>
	 * </tr>
	 * <tr>
	 * <td>"Here is one line of text that is going to be wrapped after 20 columns."</td>
	 * <td>20</td>
	 * <td>"\n"</td>
	 * <td>true/false</td>
	 * <td>" "</td>
	 * <td>"Here is one line of\ntext that is going\nto be wrapped after\n20 columns."</td>
	 * </tr>
	 * <tr>
	 * <td>"Here is one line of text that is going to be wrapped after 20 columns."</td>
	 * <td>20</td>
	 * <td>"&lt;br /&gt;"</td>
	 * <td>true/false</td>
	 * <td>" "</td>
	 * <td>"Here is one line of&lt;br /&gt;text that is going&lt;br /&gt; to be wrapped after&lt;br
	 * /&gt;20 columns."</td>
	 * </tr>
	 * <tr>
	 * <td>"Here is one line of text that is going to be wrapped after 20 columns."</td>
	 * <td>20</td>
	 * <td>null</td>
	 * <td>true/false</td>
	 * <td>" "</td>
	 * <td>"Here is one line of" + systemNewLine + "text that is going" + systemNewLine + "to be
	 * wrapped after" + systemNewLine + "20 columns."</td>
	 * </tr>
	 * <tr>
	 * <td>"Click here to jump to the commons website - http://commons.apache.org"</td>
	 * <td>20</td>
	 * <td>"\n"</td>
	 * <td>false</td>
	 * <td>" "</td>
	 * <td>"Click here to jump\nto the commons\nwebsite -\nhttp://commons.apache.org"</td>
	 * </tr>
	 * <tr>
	 * <td>"Click here to jump to the commons website - http://commons.apache.org"</td>
	 * <td>20</td>
	 * <td>"\n"</td>
	 * <td>true</td>
	 * <td>" "</td>
	 * <td>"Click here to jump\nto the commons\nwebsite -\nhttp://commons.apach\ne.org"</td>
	 * </tr>
	 * <tr>
	 * <td>"flammable/inflammable"</td>
	 * <td>20</td>
	 * <td>"\n"</td>
	 * <td>true</td>
	 * <td>"/"</td>
	 * <td>"flammable\ninflammable"</td>
	 * </tr>
	 * </table>
	 *
	 * @param str the String to be word wrapped, may be null
	 * @param wrapLength the column to wrap the words at, less than 1 is treated as 1
	 * @param newLineStr the string to insert for a new line, <code>null</code> uses the system
	 *            property line separator
	 * @param wrapLongWords true if long words (such as URLs) should be wrapped
	 * @param wrapOn regex expression to be used as a breakable characters, if blank string is
	 *            provided a space character will be used
	 * @param detectLine regex expression to be detected as newlines
	 * @return a line with newlines inserted, <code>null</code> if null input
	 */
	public static String wrap(final String str, int wrapLength, String newLineStr, final boolean wrapLongWords, String wrapOn,
			String detectLine) {
		if (str == null) {
			return null;
		}
		if (newLineStr == null) {
			newLineStr = System.lineSeparator();
		}
		if (wrapLength < 1) {
			wrapLength = 1;
		}
		if (Strings.isNullOrEmpty(wrapOn)) {
			wrapOn = " ";
		}
		final Pattern patternToWrapOn = Pattern.compile(wrapOn);

		String[] inputLines;
		if (detectLine != null) {
			inputLines = str.split(detectLine, -1);
		} else {
			inputLines = new String[] {str};
		}

		final StringBuilder wrappedLine = new StringBuilder(str.length() + 32);

		boolean firstLine = true;
		for (String inputLine : inputLines) {
			if (!firstLine) {
				wrappedLine.append(newLineStr);
			} else {
				firstLine = false;
			}

			final int inputLineLength = inputLine.length();
			int offset = 0;
			while (offset < inputLineLength) {
				int spaceToWrapAt = -1;
				Matcher matcher = patternToWrapOn.matcher(inputLine.substring(offset,
						Math.min((int) Math.min(Integer.MAX_VALUE, offset + wrapLength + 1L), inputLineLength)));
				if (matcher.find()) {
					if (matcher.start() == 0) {
						offset += matcher.end();
						continue;
					}
					spaceToWrapAt = matcher.start() + offset;
				}

				// only last line without leading spaces is left
				if (inputLineLength - offset <= wrapLength) {
					break;
				}

				while (matcher.find()) {
					spaceToWrapAt = matcher.start() + offset;
				}

				if (spaceToWrapAt >= offset) {
					// normal case
					wrappedLine.append(inputLine, offset, spaceToWrapAt);
					wrappedLine.append(newLineStr);
					offset = spaceToWrapAt + 1;

				} else {
					// really long word or URL
					if (wrapLongWords) {
						// wrap really long word one line at a time
						wrappedLine.append(inputLine, offset, wrapLength + offset);
						wrappedLine.append(newLineStr);
						offset += wrapLength;
					} else {
						// do not wrap really long word, just extend beyond limit
						matcher = patternToWrapOn.matcher(inputLine.substring(offset + wrapLength));
						if (matcher.find()) {
							spaceToWrapAt = matcher.start() + offset + wrapLength;
						}

						if (spaceToWrapAt >= 0) {
							wrappedLine.append(inputLine, offset, spaceToWrapAt);
							wrappedLine.append(newLineStr);
							offset = spaceToWrapAt + 1;
						} else {
							wrappedLine.append(inputLine, offset, inputLine.length());
							offset = inputLineLength;
						}
					}
				}
			}

			// Whatever is left in line is short enough to just pass through
			wrappedLine.append(inputLine, offset, inputLine.length());
		}

		return wrappedLine.toString();
	}
}
