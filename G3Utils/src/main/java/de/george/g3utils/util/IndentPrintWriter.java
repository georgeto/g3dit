package de.george.g3utils.util;

import java.io.PrintWriter;
import java.io.Writer;

public class IndentPrintWriter extends PrintWriter {
	private boolean newLine;
	private String singleIndent = "    ";
	private String currentIndent = "";

	public IndentPrintWriter(Writer pOut, String indent) {
		super(pOut);
		singleIndent = indent;
	}

	public void indent() {
		currentIndent += singleIndent;
	}

	public void unindent() {
		if (currentIndent.isEmpty()) {
			return;
		}
		currentIndent = currentIndent.substring(0, currentIndent.length() - singleIndent.length());
	}

	@Override
	public void print(String pString) {
		// indent when printing at the start of a new line
		if (newLine) {
			super.print(currentIndent);
			newLine = false;
		}

		// strip the last new line symbol (if there is one)
		boolean endsWithNewLine = pString.endsWith("\n");
		if (endsWithNewLine) {
			pString = pString.substring(0, pString.length() - 1);
		}

		// print the text (add indent after new-lines)
		pString = pString.replaceAll("\n", "\n" + currentIndent);
		super.print(pString);

		// finally add the stripped new-line symbol.
		if (endsWithNewLine) {
			println();
		}
	}

	@Override
	public void println() {
		super.println();
		newLine = true;
	}
}
