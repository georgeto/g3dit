package de.george.g3dit.tab.template.views.header;

import static de.george.lrentnode.enums.G3Enums.gETreasureDistribution.gETreasureDistribution_Ammunition;
import static de.george.lrentnode.enums.G3Enums.gETreasureDistribution.gETreasureDistribution_Plunder;
import static de.george.lrentnode.enums.G3Enums.gETreasureDistribution.gETreasureDistribution_Trade_Generate;
import static de.george.lrentnode.enums.G3Enums.gETreasureDistribution.gETreasureDistribution_Trade_Refresh;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;

import com.ezware.dialog.task.TaskDialogs;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableMap;
import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.config.ReloadableConfigFile;
import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.dialogs.ItemPanel;
import de.george.g3dit.gui.dialogs.ItemPanel.InventoryStack;
import de.george.g3dit.tab.shared.QualityPanel;
import de.george.g3dit.tab.template.EditorTemplateTab;
import de.george.g3dit.util.Icons;
import de.george.g3dit.util.PropertySync;
import de.george.g3dit.util.json.JsonUtil;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.gCInventory_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.gETreasureDistribution;
import de.george.lrentnode.template.TemplateEntity;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.ClassUtil;
import net.miginfocom.swing.MigLayout;

public class TreasureSetTab extends AbstractTemplateTab {
	private final CategoriesConfigFile categories;

	private JComboBox<String> cbCategory;
	private JEnumComboBox<gETreasureDistribution> cbTreasureDistribution;
	private JTextField tfName, tfMinTStacks, tfMaxTStacks;
	private ItemPanel itemPanel;

	public TreasureSetTab(EditorTemplateTab ctx) {
		super(ctx);
		categories = ctx.getFileManager().getConfigFile("Categories.json", CategoriesConfigFile.class);
	}

	@Override
	public void initComponents() {
		setLayout(new MigLayout("", "[]20[]20[]"));

		add(SwingUtils.createBoldLabel(I.tr("Eigenschaften")), "spanx, wrap");
		add(new JLabel("Name"), "gapleft 7, gaptop 5, wrap");
		tfName = SwingUtils.createUndoTF();
		add(tfName, "gapleft 7, spanx 2, growx, width 175:225:250");

		JButton btnSaveName = new JButton(I.tr("Übernehmen"), Icons.getImageIcon(Icons.IO.SAVE));
		btnSaveName.setToolTipText(I.tr("Template Name setzt sich zusammen aus: <Category>_TS_<TreasureDistribution>_<Name>"));
		btnSaveName.addActionListener(new ActionSaveName());
		add(btnSaveName, "wrap");

		add(new JLabel("TreasureDistribution"), "gapleft 7, gaptop 5, spanx, wrap");
		cbTreasureDistribution = new JEnumComboBox<>(gETreasureDistribution.class);
		add(cbTreasureDistribution, "gapleft 7, spanx 2, growx, width 175:225:250, wrap");

		add(new JLabel("Category"), "gapleft 7, gaptop 5, wrap");
		cbCategory = new JComboBox<>();
		cbCategory.setEditable(true);
		add(cbCategory, "gapleft 7, spanx 2, growx, width 175:225:250, wrap");

		add(new JLabel("MinTransferStacks"), "gapleft 7, gaptop 5");
		add(new JLabel("MaxTransferStacks"), "wrap");

		tfMinTStacks = SwingUtils.createUndoTF();
		tfMinTStacks.setName("MinTransferStacks");
		addValidators(tfMinTStacks, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER, StringValidators.REQUIRE_VALID_INTEGER);
		add(tfMinTStacks, "gapleft 7, growx");

		tfMaxTStacks = SwingUtils.createUndoTF();
		tfMaxTStacks.setName("MaxTransferStacks");
		addValidators(tfMaxTStacks, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER, StringValidators.REQUIRE_VALID_INTEGER);
		add(tfMaxTStacks, "growx");

		JButton btnCopyToClipboard = new JButton(I.tr("Zwischenablage"), Icons.getImageIcon(Icons.Action.COPY));
		btnCopyToClipboard.setToolTipText(I.tr("Zusammenfassung des TreasureSets in Zwischenablage kopieren"));
		btnCopyToClipboard.addActionListener(a -> copyToClipboard());
		add(btnCopyToClipboard, "wrap");

		add(SwingUtils.createBoldLabel("Items"), "gaptop 10, wrap");
		itemPanel = new ItemPanel(ctx);
		add(itemPanel, "gapleft 7, grow, spanx 4, width 100:600:800, wrap");

		cbTreasureDistribution.addActionListener(new ActionTreasureDistributionChanged());
	}

	@Override
	public String getTabTitle() {
		return "TreasureSet";
	}

	@Override
	public boolean isActive(TemplateFile tple) {
		return tple.getReferenceHeader().hasClass(CD.gCInventory_PS.class)
				&& tple.getReferenceHeader().hasClass(CD.gCTreasureSet_PS.class);
	}

	@Override
	public void loadValues(TemplateFile tple) {
		TemplateEntity header = tple.getReferenceHeader();

		PropertySync syncTreasure = PropertySync.wrap(header.getClass(CD.gCTreasureSet_PS.class));
		syncTreasure.readEnum(cbTreasureDistribution, CD.gCTreasureSet_PS.TreasureDistribution);
		syncTreasure.readLong(tfMinTStacks, CD.gCTreasureSet_PS.MinTransferStacks);
		syncTreasure.readLong(tfMaxTStacks, CD.gCTreasureSet_PS.MaxTransferStacks);

		gCInventory_PS inventory = header.getClass(CD.gCInventory_PS.class);
		itemPanel.clearItems();
		for (G3Class item : inventory.stacks) {
			String guid = item.property(CD.gCInventorySlot.Template).getGuid();
			int amount = item.property(CD.gCInventoryStack.Amount).getInt();
			int quality = item.property(CD.gCInventoryStack.Quality).getInt();
			itemPanel.addItem(new InventoryStack(guid, "", amount, quality));
		}

		String fullName = tple.getFileName().replace(".tple", "");
		String[] pres = categories.getContent().get(getTreasureDistribution());
		if (pres != null) {
			for (String pre : pres) {
				if (fullName.startsWith(pre)) {
					cbCategory.setSelectedItem(pre);
					break;
				}
			}
		}
		tfName.setText(tple.getEntityName().replaceFirst(".*TS_[^_]*_", ""));
	}

	@Override
	public void saveValues(TemplateFile tple) {
		saveValuesToEntity(tple.getReferenceHeader());
	}

	private void saveValuesToEntity(TemplateEntity header) {
		PropertySync syncTreasure = PropertySync.wrap(header.getClass(CD.gCTreasureSet_PS.class));
		syncTreasure.writeEnum(cbTreasureDistribution, CD.gCTreasureSet_PS.TreasureDistribution);
		syncTreasure.writeLong(tfMinTStacks, CD.gCTreasureSet_PS.MinTransferStacks);
		syncTreasure.writeLong(tfMaxTStacks, CD.gCTreasureSet_PS.MaxTransferStacks);

		int itemPos = 0;
		List<InventoryStack> items = itemPanel.getItems();
		for (InventoryStack item : items) {
			String guid = GuidUtil.parseGuid(item.getRefId());
			itemPos++;
			if (guid == null) {
				TaskDialogs.error(ctx.getParentWindow(), "", I.trf("Die Guid von Item #{0, number} ist ungültig.", itemPos));
				return;
			}
		}

		gCInventory_PS inventory = header.getClass(CD.gCInventory_PS.class);
		inventory.stacks.clear();
		for (InventoryStack item : items) {
			inventory.stacks.add(ClassUtil.createInventoryStack(item.getAmount(), item.getRefId(), item.getQuality()));
		}
	}

	private String getTreasureDistribution() {
		return cbTreasureDistribution.getSelectedItem();
	}

	private void copyToClipboard() {
		TemplateEntity currentTreasureSet = (TemplateEntity) ctx.getCurrentTemplate().getReferenceHeader().clone();
		saveValuesToEntity(currentTreasureSet);
		String result = treasureSetToString(currentTreasureSet);
		IOUtils.copyToClipboard(result);
	}

	private String treasureSetToString(TemplateEntity treasureSet) {
		StringBuffer buffer = new StringBuffer();

		int treasureDistribution = treasureSet.getProperty(CD.gCTreasureSet_PS.TreasureDistribution).getEnumValue();
		int minTransferStacks = treasureSet.getProperty(CD.gCTreasureSet_PS.MinTransferStacks).getLong();
		int maxTransferStacks = treasureSet.getProperty(CD.gCTreasureSet_PS.MaxTransferStacks).getLong();

		boolean useTransferStacks = switch (treasureDistribution) {
			case gETreasureDistribution_Plunder, gETreasureDistribution_Trade_Generate, gETreasureDistribution_Ammunition -> true;
			default -> false;
		};

		boolean random = switch (treasureDistribution) {
			case gETreasureDistribution_Plunder, gETreasureDistribution_Trade_Generate, gETreasureDistribution_Trade_Refresh -> true;
			default -> false;
		};

		buffer.append(treasureSet.getName()).append(": ");
		buffer.append(G3Enums.asString(gETreasureDistribution.class, treasureDistribution));
		if (useTransferStacks) {
			buffer.append(", ").append(String.format("%d-%dx", minTransferStacks, maxTransferStacks));
		}
		buffer.append("\n");

		gCInventory_PS inventory = treasureSet.getClass(CD.gCInventory_PS.class);
		for (G3Class stack : inventory.stacks) {
			int amount = stack.property(CD.gCInventoryStack.Amount).getInt();
			if (!random || amount <= 1) {
				buffer.append(String.format("%dx", amount));
			} else {
				buffer.append(String.format("%d-%dx", amount / 2, amount));
			}

			String guid = stack.property(CD.gCInventorySlot.Template).getGuid();
			String name = guid;
			// Lookup item name
			Optional<TemplateCacheEntry> item = Caches.template(ctx).getEntryByGuid(guid);
			if (item.isPresent()) {
				name = item.get().getName();
				// Lookup stringtable name
				String focusName = Caches.stringtable(ctx).getFocusNamesOrEmpty().get(name);
				name = String.format("%s (%s)", focusName, name);
			}
			buffer.append(" ").append(name);

			int quality = stack.property(CD.gCInventoryStack.Quality).getInt();
			if (quality != 0) {
				buffer.append(" [").append(QualityPanel.getQualityAsString(quality)).append("]");
			}
			buffer.append("\n");
		}

		return buffer.toString();
	}

	private class ActionTreasureDistributionChanged extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			cbCategory.removeAllItems();
			String[] types = categories.getContent().get(getTreasureDistribution());
			if (types == null) {
				types = new String[] {getTreasureDistribution()};
			}
			cbCategory.setModel(new DefaultComboBoxModel<>(types));
			cbCategory.setSelectedIndex(0);
		}
	}

	private class ActionSaveName extends AbstractAction {
		@Override
		public void actionPerformed(ActionEvent e) {
			ctx.saveView();

			TemplateFile tple = ctx.getCurrentTemplate();
			String entityName = "TS_" + cbTreasureDistribution.getSelectedItem() + "_" + tfName.getText();
			tple.setEntityName(entityName);
			tple.setFileName(cbCategory.getSelectedItem() + "_" + entityName + ".tple");

			ctx.loadView();
		}
	}

	public static class CategoriesConfigFile extends ReloadableConfigFile<ImmutableMap<String, String[]>> {
		public CategoriesConfigFile(EditorContext ctx, String path) {
			super(ctx, path);
		}

		@Override
		protected ImmutableMap<String, String[]> read(File configFile) throws IOException {
			return ImmutableMap.copyOf(JsonUtil.noAutodetectMapper().<Map<String, String[]>>readValue(configFile,
					TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String[].class)));
		}

		@Override
		protected ImmutableMap<String, String[]> getDefaultValue() {
			return ImmutableMap.of();
		}
	}

}
