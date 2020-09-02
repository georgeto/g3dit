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
import com.teamunify.i18n.I;

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
		super(owner, I.tr("Einstellungen"));
		this.optionStore = optionStore;
		this.settingsUpdatedCallback = settingsUpdatedCallback;

		setType(Type.UTILITY);
		setResizable(true);
		setSize(650, 650);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fill"));

		tbTabs = new JTypedTabbedPane<>();
		// tbTabs.setTabPlacement(JTabbedPane.LEFT);
		mainPanel.add(tbTabs.getComponent(), "push, grow");

		//@foff
		tbTabs.addTab(new OptionsTab(I.tr("Pfade"))
				.addHeadline(I.tr("Gothic 3 Daten"))
				.addOption(EditorOptions.Path.PRIMARY_DATA_FOLDER)
				.addOption(EditorOptions.Path.SECONDARY_DATA_FOLDER)
				.addOption(EditorOptions.Path.HIDE_PROJECTS_COMPILED)
				.addHeadline(I.tr("Externe Programme"))
				.addOption(EditorOptions.Path.FILE_MANAGER)
				.addOption(EditorOptions.Path.TINY_HEXER)
				.addOption(EditorOptions.Path.TINY_HEXER_SCRIPT)
				.addOption(EditorOptions.Path.TEXT_COMPARE)
				.addOption(EditorOptions.Path.BINARY_COMPARE));

		tbTabs.addTab(new OptionsTab(I.tr("Sprache"))
				.addHeadline(I.tr("Sprache"))
				.addOption(EditorOptions.Language.STRINGTABLE_LANGUAGE));

		tbTabs.addTab(new OptionsTab(I.tr("Diverses"))
				.addHeadline(I.tr("Speichern"))
				.addOption(EditorOptions.Misc.MAKE_BACKUP)
				.addOption(EditorOptions.Misc.CLEAN_STRINGTABLE)
				.addHeadline(I.tr("Sonstiges"))
				.addOption(EditorOptions.Misc.IMPROVE_CHANGE_DETECTION)
				.addOption(EditorOptions.Misc.OPTIMIZE_MEMORY_USAGE)
				.addOption(EditorOptions.Misc.NAVPATH_DEBUG));

		tbTabs.addTab(new OptionsTab(I.tr("3D-Ansicht"))
				.addHeadline(I.tr("Farbe und Beleuchtung"))
				.addOption(EditorOptions.D3View.BACKGROUND_COLOR, "sgx color, height 40!")
				.addOptionHorizontalStart(EditorOptions.D3View.AMBIENT_LIGHT_INTENSITY, "sgx color", 2)
				.addOptionHorizontal(EditorOptions.D3View.DIRECTIONAL_LIGHT_INTENSITY, "sgx color")
				.addOptionHorizontalStart(EditorOptions.D3View.DIRECTIONAL_LIGHT_AZIMUTH, "sgx color", 2)
				.addOptionHorizontal(EditorOptions.D3View.DIRECTIONAL_LIGHT_INCLINATION, "sgx color")
				.addHeadline(I.tr("Kamera"))
				.addOptionHorizontalStart(EditorOptions.D3View.HORIZONTAL_ROTATION, "sgx color", 2)
				.addOptionHorizontal(EditorOptions.D3View.VERTICAL_ROTATION, "sgx color")
				.addOption(EditorOptions.D3View.DISTANCE, "sgx color")
				.addHeadline(I.tr("Screenshots"))
				.addOption(EditorOptions.D3View.SCREENSHOT_FOLDER));

		tbTabs.addTab(new OptionsTab(I.tr("Theme"))
				.addHeadline(I.tr("Themes"))
				.addOption(EditorOptions.TheVoid.THEME, "push, grow"));

		tbTabs.addTab(new OptionsTab(I.tr("Dateiendungen"), "fill")
				.addHeadline(I.tr("Mit g3dit verknüpfte Dateiendungen"))
				.addOption(EditorOptions.TheVoid.FILE_EXTENSIONS, "push, grow"));

		tbTabs.getTabs().forEach(t -> t.load(optionStore));
		//@fon

		return mainPanel;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		ButtonPanel buttonPanel = newButtonPanel();

		Action okAction = SwingUtils.createAction(I.tr("Ok"), () -> {
			tbTabs.getTabs().forEach(t -> t.save(optionStore));

			affirm();
			settingsUpdatedCallback.run();
		});

		Action applyAction = SwingUtils.createAction(I.tr("Übernehmen"), () -> {
			tbTabs.getTabs().forEach(t -> t.save(optionStore));

			setDialogResult(RESULT_AFFIRMED);
			settingsUpdatedCallback.run();
		});

		Action cancelAction = SwingUtils.createAction(I.tr("Abbrechen"), () -> {
			tbTabs.getTabs().forEach(t -> t.cancel(optionStore));
			cancel();
		});

		addButton(buttonPanel, okAction, ButtonPanel.AFFIRMATIVE_BUTTON);
		addButton(buttonPanel, applyAction, ButtonPanel.OTHER_BUTTON);
		addButton(buttonPanel, cancelAction, ButtonPanel.CANCEL_BUTTON);

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
