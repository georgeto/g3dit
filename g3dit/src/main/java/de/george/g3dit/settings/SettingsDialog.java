package de.george.g3dit.settings;

import java.awt.Component;
import java.awt.Window;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import com.jidesoft.dialog.ButtonPanel;

import de.george.g3dit.gui.components.tab.ITypedTab;
import de.george.g3dit.gui.components.tab.JTypedTabbedPane;
import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3utils.gui.SwingUtils;
import net.miginfocom.swing.MigLayout;

public class SettingsDialog extends ExtStandardDialog {
	private OptionStore optionStore;

	private JTypedTabbedPane<OptionsTab> tbTabs;
	private Runnable settingsUpdatedCallback;

	public SettingsDialog(Window owner, OptionStore optionStore, Runnable settingsUpdatedCallback) {
		super(owner, "Einstellungen");
		this.optionStore = optionStore;
		this.settingsUpdatedCallback = settingsUpdatedCallback;

		setType(Type.UTILITY);
		setResizable(true);
		setSize(650, 600);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fill"));

		tbTabs = new JTypedTabbedPane<>();
		// tbTabs.setTabPlacement(JTabbedPane.LEFT);
		mainPanel.add(tbTabs.getComponent(), "push, grow");

		//@foff
		tbTabs.addTab(new OptionsTab("Pfade")
				.addHeadline("Gothic 3 Daten")
				.addOption(EditorOptions.Path.PRIMARY_DATA_FOLDER)
				.addOption(EditorOptions.Path.SECONDARY_DATA_FOLDER)
				.addOption(EditorOptions.Path.HIDE_PROJECTS_COMPILED)
				.addHeadline("Externe Programme")
				.addOption(EditorOptions.Path.FILE_MANAGER)
				.addOption(EditorOptions.Path.TINY_HEXER)
				.addOption(EditorOptions.Path.TINY_HEXER_SCRIPT)
				.addOption(EditorOptions.Path.TEXT_COMPARE)
				.addOption(EditorOptions.Path.BINARY_COMPARE));

		tbTabs.addTab(new OptionsTab("Sprache")
				.addHeadline("Sprache")
				.addOption(EditorOptions.Language.STRINGTABLE_LANGUAGE));

		tbTabs.addTab(new OptionsTab("Diverses")
				.addHeadline("Speichern")
				.addOption(EditorOptions.Misc.MAKE_BACKUP)
				.addOption(EditorOptions.Misc.CLEAN_STRINGTABLE)
				.addHeadline("Sonstiges")
				.addOption(EditorOptions.Misc.OPTIMIZE_MEMORY_USAGE)
				.addOption(EditorOptions.Misc.NAVPATH_DEBUG));

		tbTabs.addTab(new OptionsTab("3D-Ansicht")
				.addHeadline("Farbe und Beleuchtung")
				.addOption(EditorOptions.D3View.BACKGROUND_COLOR, "sgx color, height 40!")
				.addOptionHorizontalStart(EditorOptions.D3View.AMBIENT_LIGHT_INTENSITY, "sgx color", 2)
				.addOptionHorizontal(EditorOptions.D3View.DIRECTIONAL_LIGHT_INTENSITY, "sgx color")
				.addOptionHorizontalStart(EditorOptions.D3View.DIRECTIONAL_LIGHT_AZIMUTH, "sgx color", 2)
				.addOptionHorizontal(EditorOptions.D3View.DIRECTIONAL_LIGHT_INCLINATION, "sgx color")
				.addHeadline("Kamera")
				.addOptionHorizontalStart(EditorOptions.D3View.HORIZONTAL_ROTATION, "sgx color", 2)
				.addOptionHorizontal(EditorOptions.D3View.VERTICAL_ROTATION, "sgx color")
				.addOption(EditorOptions.D3View.DISTANCE, "sgx color")
				.addHeadline("Screenshots")
				.addOption(EditorOptions.D3View.SCREENSHOT_FOLDER));

		tbTabs.addTab(new OptionsTab("Dateiendungen", "fill")
				.addHeadline("Mit g3dit verknüpfte Dateiendungen")
				.addOption(EditorOptions.TheVoid.FILE_EXTENSIONS, "push, grow"));

		tbTabs.getTabs().forEach(t -> t.load(optionStore));
		//@fon

		return mainPanel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		Action okAction = SwingUtils.createAction("OK", () -> {
			tbTabs.getTabs().forEach(t -> t.save(optionStore));

			setDialogResult(RESULT_AFFIRMED);
			dispose();
			settingsUpdatedCallback.run();
		});

		Action applyAction = SwingUtils.createAction("Übernehmen", () -> {
			tbTabs.getTabs().forEach(t -> t.save(optionStore));

			setDialogResult(RESULT_AFFIRMED);
			settingsUpdatedCallback.run();
		});

		addButton(buttonPanel, okAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addButton(buttonPanel, applyAction, ButtonPanel.OTHER_BUTTON);
		addDefaultCancelButton(buttonPanel);

		return buttonPanel;
	}

	private class OptionsTab extends OptionPanelBase<OptionsTab> implements ITypedTab {
		private JScrollPane scroll;
		private String title;

		public OptionsTab(String title) {
			this(title, "fillx", "[grow]", null);
		}

		public OptionsTab(String title, String layoutConstraints) {
			this(title, layoutConstraints, null, null);
		}

		public OptionsTab(String title, String layoutConstraints, String colConstraints) {
			this(title, layoutConstraints, colConstraints, null);
		}

		public OptionsTab(String title, String layoutConstraints, String colConstraints, String rowConstraints) {
			super(SettingsDialog.this, layoutConstraints, colConstraints, rowConstraints);
			scroll = new JScrollPane(getContent());
			scroll.setBorder(null);
			this.title = title;
		}

		@Override
		public Icon getTabIcon() {
			return null;
		}

		@Override
		public String getTabTitle() {
			return title;
		}

		@Override
		public Component getTabContent() {
			return scroll;
		}
	}
}
