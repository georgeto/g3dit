package de.george.g3dit;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jidesoft.swing.MultilineLabel;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import de.george.g3dit.cache.CacheManager;
import de.george.g3dit.gui.components.JEventList;
import de.george.g3dit.gui.components.JTextAreaExt;
import de.george.g3dit.gui.components.TextLineNumber;
import de.george.g3dit.gui.dialogs.ExtStandardDialog;
import de.george.g3dit.gui.renderer.BeanListCellRenderer;
import de.george.g3dit.scripts.IScript;
import de.george.g3dit.scripts.IScriptEnvironment;
import de.george.g3dit.settings.Option;
import de.george.g3dit.settings.OptionPanel;
import de.george.g3dit.util.ClasspathScanUtil;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.FileManager;
import de.george.g3dit.util.ListUtil;
import net.miginfocom.swing.MigLayout;

public class ScriptManager implements IScriptEnvironment {
	private static final Logger logger = LoggerFactory.getLogger(ScriptManager.class);

	private Set<IScript> scripts;
	private EditorContext ctx;
	private ScriptDialog scriptDialog;

	public ScriptManager(EditorContext ctx) {
		scripts = new TreeSet<>(Comparator.comparing(IScript::getTitle));
		this.ctx = ctx;
	}

	public void addDefaultScripts() {
		ConcurrencyUtil.executeAndInvokeLaterOnSuccess(() -> {
			List<IScript> scripts = new ArrayList<>();
			// Fehlermeldungen (verursacht durch Packaging mit gradle-one-jar) unterdrücken
			for (Class<? extends IScript> script : ClasspathScanUtil.findSubtypesOf(IScript.class, "de.george.g3dit.scripts")) {
				try {
					if (!Modifier.isAbstract(script.getModifiers())) {
						scripts.add(script.newInstance());
					}
				} catch (InstantiationException | IllegalAccessException e) {
					logger.warn("Unable to instantiate '{}': {})", script.getSimpleName(), e);
				}
			}
			return scripts;
		}, scripts -> scripts.forEach(this::addScript), ctx.getExecutorService());
	}

	public void addScript(IScript script) {
		scripts.add(script);
	}

	public void removeScript(IScript script) {
		scripts.remove(script);
	}

	@Override
	public EditorContext getEditorContext() {
		return ctx;
	}

	@Override
	public FileManager getFileManager() {
		return ctx.getFileManager();
	}

	@Override
	public CacheManager getCacheManager() {
		return ctx.getCacheManager();
	}

	@Override
	public Window getParentWindow() {
		return ctx.getParentWindow();
	}

	@Override
	public void log(String message) {
		SwingUtilities.invokeLater(() -> scriptDialog.area.append(message + "\n"));
	}

	@Override
	public void log(String message, Object... arguments) {
		SwingUtilities.invokeLater(() -> scriptDialog.area.append(String.format(message + "\n", arguments)));
	}

	@Override
	public <T> T getOption(Option<T> option) {
		return ctx.getOptionStore().get(option);
	}

	public void showScriptDialog() {
		if (scriptDialog == null) {
			scriptDialog = new ScriptDialog();
			scriptDialog.setLocationRelativeTo(ctx.getParentWindow());
		}
		scriptDialog.setVisible(true);
	}

	private class ScriptDialog extends ExtStandardDialog {
		private JEventList<IScript> scriptList;
		private MultilineLabel lblDescription;
		private JButton btnExecute;
		private JTextAreaExt area;
		private OptionPanel optionPanel;

		public ScriptDialog() {
			super(ctx.getParentWindow(), "Scripts ausführen", true);
			setSize(1000, 700);
			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent windowEvent) {
					// Save script settings when ScriptDialog is closed.
					optionPanel.save(ctx.getOptionStore());
				}
			});
		}

		private void scriptSelected() {
			// Save script settings.
			optionPanel.save(ctx.getOptionStore());
			optionPanel.removeAllOptions();

			List<IScript> selectedScripts = scriptList.getSelectedValuesList();
			if (!selectedScripts.isEmpty()) {
				IScript selectedScript = selectedScripts.get(0);
				optionPanel.addHeadline(selectedScript.getTitle());
				btnExecute.setEnabled(true);

				optionPanel.addComponent(lblDescription, "grow, gapbottom 10");
				lblDescription.setText(selectedScript.getDescription());
				selectedScript.installOptions(optionPanel);

				optionPanel.addComponent(btnExecute, "alignx right");

				// Load script settings from option store.
				optionPanel.load(ctx.getOptionStore());
			} else {
				btnExecute.setEnabled(false);
			}
		}

		private void executeScript() {
			// Save script settings to option store.
			optionPanel.save(ctx.getOptionStore());

			area.setText(null);
			IScript selectedValue = scriptList.getSelectedValue();
			log("'" + selectedValue.getTitle() + "' wird ausgeführt.");
			boolean result;
			try {
				result = selectedValue.execute(ScriptManager.this);
			} catch (Exception e) {
				result = false;
				log("Exception: %s", e.getMessage());
				logger.warn("Error while executing script '{}'.", selectedValue.getTitle(), e);
			}
			log("Ausführung " + (result ? "erfolgreich." : "fehlgeschlagen."));
			ctx.runGC();
		}

		@Override
		public JComponent createContentPanel() {
			JPanel mainPanel = new JPanel(new MigLayout("", "[]10px[fill, grow 100]", "[fill][fill, grow 100]"));

			SortedList<IScript> sortedScripts = new SortedList<>(GlazedLists.eventList(scripts), Comparator.comparing(IScript::getTitle));
			scriptList = new JEventList<>(sortedScripts);
			scriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			scriptList.addListSelectionListener(e -> scriptSelected());
			scriptList.setCellRenderer(new BeanListCellRenderer("Title"));
			mainPanel.add(new JScrollPane(scriptList), "spany 2, cell 0 0");

			lblDescription = new MultilineLabel("");
			btnExecute = new JButton("Script ausführen");
			btnExecute.addActionListener(a -> executeScript());
			ListUtil.enableOnEqual(scriptList, btnExecute, 1);

			optionPanel = new OptionPanel(this);
			optionPanel.getContent().setBorder(null);
			mainPanel.add(optionPanel.getContent(), "cell 1 0");

			area = new JTextAreaExt();
			area.getScrollPane().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
			TextLineNumber tln = new TextLineNumber(area);
			area.getScrollPane().setRowHeaderView(tln);
			area.setEditable(false);
			mainPanel.add(area.getScrollPane(), "cell 1 1");

			return mainPanel;
		}
	}
}
