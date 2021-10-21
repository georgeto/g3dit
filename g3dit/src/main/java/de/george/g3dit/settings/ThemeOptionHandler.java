package de.george.g3dit.settings;

import java.awt.Window;

import javax.swing.JLabel;
import javax.swing.JPanel;

import de.george.g3dit.gui.theme.ThemeInfo;
import de.george.g3dit.gui.theme.ThemeManager;
import de.george.g3dit.gui.theme.ThemeSettingsPanel;

public class ThemeOptionHandler extends AbstractOptionHandler<ThemeInfo> {
	private ThemeSettingsPanel themeSettingsPanel;

	public ThemeOptionHandler(Window parent) {
		super(parent);
	}

	@Override
	protected JPanel initContent() {
		JPanel content = super.initContent();

		themeSettingsPanel = new ThemeSettingsPanel();
		content.add(themeSettingsPanel, "width 300!, grow, push, wrap");
		content.add(new JLabel("After changing the theme it may be necessary to restart g3dit."), "pushy, alignx right, aligny bottom");
		return content;
	}

	@Override
	public void load(OptionStore optionStore, Option<ThemeInfo> option) {
		themeSettingsPanel.setSelectedTheme(optionStore.get(option));
	}

	@Override
	public void save(OptionStore optionStore, Option<ThemeInfo> option) {
		themeSettingsPanel.getSelectedTheme().ifPresent(theme -> optionStore.put(option, theme));
	}

	@Override
	public void cancel(OptionStore optionStore, Option<ThemeInfo> option) {
		// Revert back to old theme
		ThemeManager.setTheme(optionStore.get(option), false);
	}
}
