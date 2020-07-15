package de.george.g3dit.gui.components;

import java.util.function.Supplier;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenuItem;

public class JDynamicMenuItem extends JMenuItem {
	private final Supplier<Boolean> enabledPredicate;
	private boolean enablePending = false;

	public JDynamicMenuItem(Supplier<Boolean> enabledPredicate) {
		this.enabledPredicate = enabledPredicate;
	}

	public JDynamicMenuItem(Action a, Supplier<Boolean> enabledPredicate) {
		super(a);
		this.enabledPredicate = enabledPredicate;
	}

	public JDynamicMenuItem(Icon icon, Supplier<Boolean> enabledPredicate) {
		super(icon);
		this.enabledPredicate = enabledPredicate;
	}

	public JDynamicMenuItem(String text, Icon icon, Supplier<Boolean> enabledPredicate) {
		super(text, icon);
		this.enabledPredicate = enabledPredicate;
	}

	public JDynamicMenuItem(String text, int mnemonic, Supplier<Boolean> enabledPredicate) {
		super(text, mnemonic);
		this.enabledPredicate = enabledPredicate;
	}

	public JDynamicMenuItem(String text, Supplier<Boolean> enabledPredicate) {
		super(text);
		this.enabledPredicate = enabledPredicate;
	}

	@Override
	public boolean isEnabled() {
		if (!enablePending) {
			enablePending = true;
			setEnabled(enabledPredicate == null || enabledPredicate.get());
			enablePending = false;
		}

		return super.isEnabled();
	}
}
