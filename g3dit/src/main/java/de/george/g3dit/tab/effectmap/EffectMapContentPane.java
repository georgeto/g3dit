package de.george.g3dit.tab.effectmap;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.ImmutableBiMap;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertySheetPanel;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import de.george.g3dit.gui.components.HidingGroup;
import de.george.g3dit.gui.components.ListManageAndEdit;
import de.george.g3dit.gui.components.TableModificationControl;
import de.george.g3dit.gui.dialogs.EnterEnumDialog;
import de.george.g3dit.gui.renderer.FunctionalListCellRenderer;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.tab.archive.views.property.G3Property;
import de.george.g3dit.util.PropertySheetUtil;
import de.george.g3utils.gui.ListTableModel;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.lrentnode.effect.gCEffectCommand;
import de.george.lrentnode.effect.gCEffectCommandPlaySound;
import de.george.lrentnode.effect.gCEffectCommandPlaySound.Sample;
import de.george.lrentnode.effect.gCEffectCommandSequence;
import de.george.lrentnode.effect.gCEffectMap;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.gEEffectCommand;
import de.george.lrentnode.properties.ClassProperty;
import de.george.lrentnode.util.ClassUtil;
import net.miginfocom.swing.MigLayout;

public class EffectMapContentPane extends JPanel {
	private EditorEffectMapTab ctx;

	private EventList<gCEffectCommandSequence> effects = new BasicEventList<>();
	private EffectMapEditingPanel editingPanel;

	public EffectMapContentPane(EditorEffectMapTab ctx) {
		this.ctx = ctx;
	}

	public void initGUI() {
		editingPanel = new EffectMapEditingPanel();

		ListManageAndEdit<gCEffectCommandSequence> edit = ListManageAndEdit.create(effects, this::inputEffect, editingPanel)
				.title(I.tr("Effects")).changeMonitor(ctx).onSelect(editingPanel::loadEffect)
				.matcherEditor(tfFilter -> new TextComponentMatcherEditor<>(tfFilter, GlazedLists.textFilterator("Name")))
				.cellRenderer(new FunctionalListCellRenderer<>(gCEffectCommandSequence::getName)).build();

		setLayout(new BorderLayout());
		add(edit.getContent(), BorderLayout.CENTER);
	}

	private class EffectMapEditingPanel extends JPanel {
		private gCEffectCommandSequence effect;
		private EventList<gCEffectCommand> commands = new BasicEventList<>();
		private EffectCommandEditingPanel commandEditingPanel;
		private UndoableTextField tfName;

		public EffectMapEditingPanel() {
			setupComponents();
			loadEffect(null);
		}

		private void setupComponents() {
			setLayout(new MigLayout("fill", "[fill, grow]", "[][]20px push[fill, grow]"));

			add(new JLabel(I.tr("Name")), "wrap");

			tfName = SwingUtils.createUndoTF();
			add(tfName, "width 100:300:300, wrap");

			commandEditingPanel = new EffectCommandEditingPanel();
			ListManageAndEdit<gCEffectCommand> edit = ListManageAndEdit.create(commands, this::inputCommand, commandEditingPanel)
					.title(I.tr("Commands")).orientation(JSplitPane.VERTICAL_SPLIT).changeMonitor(ctx)
					.onSelect(commandEditingPanel::loadCommand)
					.cellRenderer(
							new FunctionalListCellRenderer<gCEffectCommand>(e -> e.getClassName().replaceFirst("gCEffectCommand", "")))
					.build();
			add(edit.getContent());
		}

		public void loadEffect(gCEffectCommandSequence effect) {
			save();

			this.effect = effect;

			commands.clear();
			tfName.setText("");
			if (effect != null) {
				tfName.setText(effect.getName());
				commands.addAll(effect.getCommands());
			}
		}

		public void save() {
			if (effect != null) {
				commandEditingPanel.save();
				effect.setName(tfName.getText());
				effect.setCommands(new ArrayList<>(commands));
			}
		}

		private Optional<gCEffectCommand> inputCommand() {
			EnterEnumDialog dialog = new EnterEnumDialog(ctx.getParentWindow(), I.tr("Neues EffectCommand erstellen"), I.tr("Erstellen"),
					Arrays.stream(G3Enums.asArray(gEEffectCommand.class)).filter(c -> !c.equals("CreateDecal")).toArray(String[]::new),
					G3Enums.asString(gEEffectCommand.class, gEEffectCommand.gEEffectCommand_PlaySound));

			if (dialog.openAndWasSuccessful()) {
				String commandType = dialog.getSelectedValue();

				gCEffectCommand command = null;
				int version;
				switch (G3Enums.asInt(gEEffectCommand.class, commandType)) {
					case gEEffectCommand.gEEffectCommand_PlaySound:
						command = new gCEffectCommandPlaySound("gCEffectCommandPlaySound", 2);
					case gEEffectCommand.gEEffectCommand_Earthquake:
						version = 2;
						break;
					default:
						version = 1;
						break;
				}

				if (command == null) {
					command = new gCEffectCommand("gCEffectCommand" + commandType, version);
				}
				ClassUtil.setDefaultProperties(command);

				return Optional.of(command);
			} else {
				return Optional.empty();
			}
		}
	}

	private class EffectCommandEditingPanel extends JPanel {
		private gCEffectCommand command;

		private PropertySheetPanel commandSheet;
		private JXTable sampleTable;
		private SampleTableModel sampleModel;
		private HidingGroup sampleHidingGroup;

		public EffectCommandEditingPanel() {
			setupComponents();
			loadCommand(null);
		}

		private void setupComponents() {
			setLayout(new MigLayout("fill", "[grow, fill]", "[grow 100, fill]12px push[grow 50, fill]"));

			commandSheet = PropertySheetUtil.createPropertySheetPanel();
			add(commandSheet, "wrap");

			sampleTable = new JXTable();
			TableUtil.disableSearch(sampleTable);
			sampleTable.setColumnFactory(new SampleTableColumFactory());
			sampleTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
			sampleModel = new SampleTableModel();
			sampleTable.setModel(sampleModel);

			JLabel lblSamples = SwingUtils.createBoldLabel(I.tr("Samples"));
			add(lblSamples, "gaptop u, hidemode 2, wrap");
			JScrollPane scrollSampleTable = new JScrollPane(sampleTable);
			add(scrollSampleTable, "hidemode 2, wrap");
			TableModificationControl<Sample> modControl = new TableModificationControl<>(ctx, sampleTable, sampleModel,
					() -> new Sample("", 1));
			add(modControl, "hidemode 2");

			sampleHidingGroup = HidingGroup.create(lblSamples, scrollSampleTable, modControl);
		}

		private void loadCommand(gCEffectCommand command) {
			this.command = command;

			commandSheet.setProperties(new Property[0]);
			if (command != null) {
				for (ClassProperty<?> property : command.properties()) {
					commandSheet.addProperty(new G3Property(property, command.getClassName()));
				}
			}

			if (command instanceof gCEffectCommandPlaySound soundCommand) {
				sampleModel.setEntries(soundCommand.samples);
				sampleHidingGroup.setVisible(true);
			} else {
				sampleModel.clearEntries();
				sampleHidingGroup.setVisible(false);
			}
		}

		public void save() {
			TableUtil.stopEditing(commandSheet.getTable());
			TableUtil.stopEditing(sampleTable);

			if (command != null && command instanceof gCEffectCommandPlaySound soundCommand) {
				soundCommand.samples = new ArrayList<>(sampleModel.getEntries());
			}
		}

		private static final ImmutableBiMap<String, String> SAMPLE_COLUMN_MAPPING = ImmutableBiMap.of("Sample", I.tr("Sample"),
				"Probability", I.tr("Probability"));

		private class SampleTableModel extends ListTableModel<Sample> {
			public SampleTableModel() {
				super(SAMPLE_COLUMN_MAPPING.values().toArray(new String[0]));
			}

			@Override
			public Object getValueAt(Sample entry, int col) {
				return switch (col) {
					case 0 -> entry.name;
					case 1 -> entry.probability;
					default -> null;
				};
			}

			@Override
			public void setValueAt(Object value, Sample entry, int col) {
				boolean changed = false;
				switch (col) {
					case 0:
						changed = !entry.name.equals(value);
						entry.name = (String) value;
					case 1:
						changed = entry.probability != (float) value;
						entry.probability = (Float) value;
				}
				if (changed) {
					ctx.fileChanged();
				}
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return true;
			}
		}

		private class SampleTableColumFactory extends ColumnFactory {
			@Override
			public void configureColumnWidths(JXTable table, TableColumnExt columnExt) {
				columnExt.setEditable(true);
				switch (SAMPLE_COLUMN_MAPPING.inverse().get(columnExt.getTitle())) {
					case "Sample" -> columnExt.setPreferredWidth(300);
					case "Probability" -> columnExt.setPreferredWidth(100);
					default -> super.configureColumnWidths(table, columnExt);
				}
			}
		}

	}

	public void loadValues() {
		gCEffectMap effectMap = ctx.getCurrentEffectMap();
		effects.clear();
		effects.addAll(effectMap.effects);
	}

	public void saveValues() {
		editingPanel.save();

		gCEffectMap effectMap = ctx.getCurrentEffectMap();
		effectMap.effects.clear();
		effectMap.effects.addAll(effects);
	}

	public void onClose() {
		// TODO Auto-generated method stub
	}

	private Optional<gCEffectCommandSequence> inputEffect() {
		String input = TaskDialogs.input(ctx.getParentWindow(), I.tr("Neuen Effekt erstellen"), I.tr("Bitte Namen des Effekts eingeben"),
				I.tr("Effekt einfügen"));

		return Optional.ofNullable(input).filter(i -> !i.isEmpty()).map(gCEffectCommandSequence::new);
	}
}
