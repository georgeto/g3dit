package de.george.g3dit.gui.components;

import java.util.function.Function;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;

public class SeverityMatcherEditor<T> extends AbstractMatcherEditor<T> {
	private JSeverityComboBox cbSeverity;
	private Function<T, Severity> severityExtractor;

	public SeverityMatcherEditor(JSeverityComboBox cbSeverity, Function<T, Severity> severityExtractor) {
		this.cbSeverity = cbSeverity;
		this.severityExtractor = severityExtractor;
		cbSeverity.addItemListener(e -> policyChanged());
		policyChanged();
	}

	public void policyChanged() {
		Severity severity = cbSeverity.getSelectedItem();
		if (severity != Severity.Info) {
			fireChanged(value -> severityExtractor.apply(value).compareTo(severity) >= 0);
		} else {
			fireMatchAll();
		}
	}
}
