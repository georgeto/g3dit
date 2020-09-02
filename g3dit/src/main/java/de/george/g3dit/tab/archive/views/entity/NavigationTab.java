package de.george.g3dit.tab.archive.views.entity;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.netbeans.validation.api.ui.ValidationGroup;

import com.teamunify.i18n.I;

import de.george.g3dit.entitytree.filter.GuidEntityFilter.MatchMode;
import de.george.g3dit.gui.components.JEntityGuidField;
import de.george.g3dit.gui.components.JSearchGuidField;
import de.george.g3dit.gui.components.JSearchGuidField.GuildFieldMenuItem;
import de.george.g3dit.gui.dialogs.EntitySearchDialog;
import de.george.g3dit.gui.validation.EntityExistenceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.tab.shared.AbstractElementPanel;
import de.george.g3dit.tab.shared.AbstractElementsPanel;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.validation.GuidValidator;
import de.george.g3utils.validation.ValidationGroupWrapper;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.properties.bCPropertyID;
import de.george.lrentnode.properties.bCString;
import net.miginfocom.swing.MigLayout;

public class NavigationTab extends AbstractEntityTab {
	private NavigationRoutinesPanel routinesPanel;

	public NavigationTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	@Override
	public String getTabTitle() {
		return "Navigation";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return entity.hasClass(CD.gCNavigation_PS.class);
	}

	@Override
	protected void initComponents() {
		setLayout(new MigLayout("fillx", "[]"));
		routinesPanel = new NavigationRoutinesPanel(getScrollpane());
		routinesPanel.initValidation(validation());
		add(routinesPanel, "width 100:300:450, spanx 4, grow, wrap");
	}

	@Override
	public void loadValues(eCEntity entity) {
		routinesPanel.loadValues(entity);
	}

	@Override
	public void saveValues(eCEntity entity) {
		routinesPanel.saveValues(entity);
	}

	private class NavigationRoutinesPanel extends AbstractElementsPanel<eCEntity> {
		public NavigationRoutinesPanel(JScrollPane navScroll) {
			super(I.tr("Routine"), navScroll, false);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void loadValuesInternal(eCEntity entity) {
			// Routinen laden
			G3Class navigation = entity.getClass(CD.gCNavigation_PS.class);
			List<String> routineNames = new ArrayList<>(
					navigation.property(CD.gCNavigation_PS.RoutineNames).getEntries(bCString::getString));
			List<String> workingPoints = new ArrayList<>(
					navigation.property(CD.gCNavigation_PS.WorkingPoints).getEntries(bCPropertyID::getGuid));
			List<String> relaxingPoints = new ArrayList<>(
					navigation.property(CD.gCNavigation_PS.RelaxingPoints).getEntries(bCPropertyID::getGuid));
			List<String> sleepingPoints = new ArrayList<>(
					navigation.property(CD.gCNavigation_PS.SleepingPoints).getEntries(bCPropertyID::getGuid));

			// Startroutine laden
			String routineName = navigation.property(CD.gCNavigation_PS.Routine).getString();
			int index = routineNames.indexOf(routineName);
			if (index != -1) {
				routineNames.remove(index);
				workingPoints.remove(index);
				relaxingPoints.remove(index);
				sleepingPoints.remove(index);
			} else {
				index = 0;
			}

			routineNames.add(index, routineName);
			workingPoints.add(index, navigation.property(CD.gCNavigation_PS.WorkingPoint).getGuid());
			relaxingPoints.add(index, navigation.property(CD.gCNavigation_PS.RelaxingPoint).getGuid());
			sleepingPoints.add(index, navigation.property(CD.gCNavigation_PS.SleepingPoint).getGuid());

			// RoutinePanels erstellen
			if (routineNames.size() == workingPoints.size() && routineNames.size() == relaxingPoints.size()
					&& routineNames.size() == sleepingPoints.size()) {
				for (int i = 0; i < workingPoints.size(); i++) {
					NavigationRoutinePanel routinePanel = new NavigationRoutinePanel(routineNames.get(i), workingPoints.get(i),
							relaxingPoints.get(i), sleepingPoints.get(i));
					insertElementRelative(routinePanel, null, InsertPosition.After);
				}
			} else {
				throw new IllegalArgumentException(entity.getName() + ": gCNavigation_PS is invalid.");
			}
		}

		@Override
		public void saveValuesInternal(eCEntity entity) {
			int routineCount = getComponentCount();

			// Routinen Speichern
			List<String> routineNames = new ArrayList<>(routineCount);
			List<String> workingPoints = new ArrayList<>(routineCount);
			List<String> relaxingPoints = new ArrayList<>(routineCount);
			List<String> sleepingPoints = new ArrayList<>(routineCount);

			for (int i = 0; i < routineCount; i++) {
				NavigationRoutinePanel routine = (NavigationRoutinePanel) getComponent(i);
				routineNames.add(routine.getRoutineName());
				workingPoints.add(GuidUtil.parseGuid(routine.getWorking()));
				relaxingPoints.add(GuidUtil.parseGuid(routine.getRelaxing()));
				sleepingPoints.add(GuidUtil.parseGuid(routine.getSleeping()));
			}

			G3Class navigation = entity.getClass(CD.gCNavigation_PS.class);
			navigation.property(CD.gCNavigation_PS.RoutineNames).setEntries(routineNames, bCString::new);
			navigation.property(CD.gCNavigation_PS.WorkingPoints).setEntries(workingPoints, bCPropertyID::new);
			navigation.property(CD.gCNavigation_PS.RelaxingPoints).setEntries(relaxingPoints, bCPropertyID::new);
			navigation.property(CD.gCNavigation_PS.SleepingPoints).setEntries(sleepingPoints, bCPropertyID::new);

			// Startroutine speichern
			String routineName = navigation.property(CD.gCNavigation_PS.Routine).getString();
			int index = routineNames.indexOf(routineName);
			if (index == -1) {
				index = 0;
			}
			if (routineNames.size() > 0) {
				navigation.property(CD.gCNavigation_PS.Routine).setString(routineNames.get(index));
				navigation.property(CD.gCNavigation_PS.WorkingPoint).setGuid(workingPoints.get(index));
				navigation.property(CD.gCNavigation_PS.RelaxingPoint).setGuid(relaxingPoints.get(index));
				navigation.property(CD.gCNavigation_PS.SleepingPoint).setGuid(sleepingPoints.get(index));
			}
		}

		@Override
		protected AbstractElementPanel getNewElement() {
			return new NavigationRoutinePanel("", "", "", "");
		}

		@Override
		protected void removeValuesInternal(eCEntity entity) {
			// Nicht nötig, da allowEmpty = false
		}
	}

	private class NavigationRoutinePanel extends AbstractElementPanel {
		private JTextField tfName;
		private JSearchGuidField tfWorking, tfRelaxing, tfSleeping;

		public NavigationRoutinePanel(String name, String working, String relaxing, String sleeping) {
			super(I.tr("Routine"), routinesPanel);
			setLayout(new MigLayout("", "[]10px[]push[]"));

			JLabel lblName = new JLabel(I.tr("Name"));
			add(lblName, "cell 0 0");

			tfName = SwingUtils.createUndoTF(name);
			add(tfName, "cell 1 0, width 100:300:300");

			GuildFieldMenuItem miListAllEntities = new GuildFieldMenuItem(I.tr("Alle Nutzer dieses Interaktionspunktes auflisten"),
					Icons.getImageIcon(Icons.Misc.GLOBE),
					(ctx, text) -> EntitySearchDialog.openEntitySearchGuid(ctx, MatchMode.Routine, text));

			JLabel lblWorking = new JLabel(I.tr("Working"));
			add(lblWorking, "cell 0 1");

			tfWorking = new JEntityGuidField(ctx);
			tfWorking.setText(working);
			tfWorking.addMenuItem(miListAllEntities);
			add(tfWorking, "cell 1 1, width 100:300:300");

			JLabel lblRelaxing = new JLabel(I.tr("Relaxing"));
			add(lblRelaxing, "cell 0 2");

			tfRelaxing = new JEntityGuidField(ctx);
			tfRelaxing.setText(relaxing);
			tfRelaxing.addMenuItem(miListAllEntities);
			add(tfRelaxing, "cell 1 2, width 100:300:300");

			JLabel lblSleeping = new JLabel(I.tr("Sleeping"));
			add(lblSleeping, "cell 0 3");

			tfSleeping = new JEntityGuidField(ctx);
			tfSleeping.setText(sleeping);
			tfSleeping.addMenuItem(miListAllEntities);
			add(tfSleeping, "cell 1 3, width 100:300:300");

			tfName.getDocument().addDocumentListener(SwingUtils.createDocumentListener(this::updateBorderTitle));

			JPanel operationPanel = getOperationPanel();
			add(operationPanel, "cell 2 1, spanx 2, spany 3");
		}

		@Override
		public void initValidation(ValidationGroup group) {
			tfWorking.initValidation(group, I.tr("Working"), GuidValidator.INSTANCE, new EntityExistenceValidator(group, ctx));
			tfRelaxing.initValidation(group, I.tr("Relaxing"), GuidValidator.INSTANCE, new EntityExistenceValidator(group, ctx));
			tfSleeping.initValidation(group, I.tr("Sleeping"), GuidValidator.INSTANCE, new EntityExistenceValidator(group, ctx));
		}

		@Override
		public void removeValidation(ValidationGroupWrapper group) {
			tfWorking.removeValidation(group);
			tfRelaxing.removeValidation(group);
			tfSleeping.removeValidation(group);
		}

		@Override
		protected String getBorderTitle() {
			return I.trf("{0, number}. Routine: {1}", position, tfName.getText());
		}

		public String getRoutineName() {
			return tfName.getText();
		}

		public String getWorking() {
			return tfWorking.getText();
		}

		public String getRelaxing() {
			return tfRelaxing.getText();
		}

		public String getSleeping() {
			return tfSleeping.getText();
		}
	}
}
