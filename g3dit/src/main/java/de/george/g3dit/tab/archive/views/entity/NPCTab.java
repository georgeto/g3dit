package de.george.g3dit.tab.archive.views.entity;

import javax.swing.JComboBox;
import javax.swing.JTabbedPane;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

import com.google.common.collect.Iterables;

import de.george.g3dit.config.StringListConfigFile;
import de.george.g3dit.entitytree.filter.GuidEntityFilter.MatchMode;
import de.george.g3dit.gui.components.JEntityGuidField;
import de.george.g3dit.gui.dialogs.EntitySearchDialog;
import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.edit.handler.LambdaPropertyHandler;
import de.george.g3dit.gui.validation.EntityExistenceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.util.Icons;
import de.george.g3utils.validation.EmtpyWarnValidator;
import de.george.g3utils.validation.GuidValidator;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gESlot;
import de.george.lrentnode.util.NPCUtil;

public class NPCTab extends AbstractPropertyEntityTab {
	private final StringListConfigFile voices;

	private JTabbedPane slotTabs;

	public NPCTab(EditorArchiveTab ctx) {
		super(ctx);
		voices = ctx.getFileManager().getConfigFile("Voices.list", StringListConfigFile.class);
	}

	@Override
	public void initPropertyPanel(PropertyPanel propertyPanel) {
		slotTabs = new JTabbedPane();
		slotTabs.addTab("Kopf", new NPCSlotPanel(gESlot.gESlot_Head, ctx));
		slotTabs.addTab("Körper", new NPCSlotPanel(gESlot.gESlot_Body, ctx));
		slotTabs.addTab("Haare", new NPCSlotPanel(gESlot.gESlot_Hair, ctx));
		slotTabs.addTab("Bart", new NPCSlotPanel(gESlot.gESlot_Beard, ctx));
		slotTabs.addTab("Helm", new NPCSlotPanel(gESlot.gESlot_Helmet, ctx));
		for (int i = 0; i < slotTabs.getTabCount(); i++) {
			((NPCSlotPanel) slotTabs.getComponentAt(i)).initValidation(validation());
		}

		//@foff
		propertyPanel
			.addHeadline("Eigenschaften")
			.add(CD.gCNPC_PS.Voice).name("Stimme").horizontalStart(2).constraints("x level.x, x2 levelMax.x2")
				.valueList(Iterables.toArray(voices.getContent(), String.class))
				.<JComboBox<?>>customize(c -> c.setEditable(true))
				.validate(validation(), EmtpyWarnValidator.INSTANCE)
			.add(CD.gCNPC_PS.Level).name("Level").horizontalStart().sizegroup("level").constraints("id level")
				.validate(validation(), StringValidators.REQUIRE_NON_NEGATIVE_NUMBER, StringValidators.REQUIRE_VALID_INTEGER)
			.add(CD.gCNPC_PS.LevelMax).name("Maximales Level").horizontal().sizegroup("level").constraints("id levelMax")
				.validate(validation(), StringValidators.REQUIRE_NON_NEGATIVE_NUMBER, StringValidators.REQUIRE_VALID_INTEGER)
			.add(CD.gCNPC_PS.Gender).name("Geschlecht").horizontalStart().sizegroup("level")
			.add(CD.gCNPC_PS.Species).name("Rasse").horizontal()
			.add(CD.gCNPC_PS.PoliticalAlignment).name("Politische Ausrichtung").horizontal()
			.add(CD.gCNPC_PS.Class).name("Klasse").horizontalStart()
			.add(CD.gCNPC_PS.Type).name("Typ").horizontal()
			.add(CD.gCDialog_PS.TradeEnabled).name("Händler").horizontal()
				.hideIf(entity -> !entity.hasClass(CD.gCDialog_PS.class))
			.addHeadline("Wache")
			.add(CD.gCNPC_PS.GuardPoint).horizontalStart(2)
				.validate(validation(), GuidValidator.INSTANCE_ALLOW_EMPTY, new EntityExistenceValidator(validation(), ctx))
			.add(CD.gCNPC_PS.GuardStatus).horizontal()
			.addHeadline("Sonstiges")
			.add(CD.gCNPC_PS.Enclave)
				.validate(validation(), GuidValidator.INSTANCE_ALLOW_EMPTY, new EntityExistenceValidator(validation(), ctx))
				.<JEntityGuidField>customize(tfEnclave ->
					tfEnclave.addMenuItem("Alle Mitglieder der Enclave auflisten", Icons.getImageIcon(Icons.Misc.GLOBE),
						(c, text) -> EntitySearchDialog.openEntitySearchGuid(c, MatchMode.Enclave, text)))
			.addHeadline("Körperteile")
			.add(new LambdaPropertyHandler(slotTabs, entity -> {
				for (int i = 0; i < slotTabs.getTabCount(); i++) {
					((NPCSlotPanel) slotTabs.getComponentAt(i)).loadSlot(entity);
				}
			}, entity -> {})).grow().constraints("width 100:300:400, wrap")
			.done();
		//@fon
	}

	@Override
	public String getTabTitle() {
		return "NPC";
	}

	@Override
	public boolean isActive(eCEntity entity) {
		return NPCUtil.isNPC(entity);
	}
}
