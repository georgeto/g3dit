package de.george.g3dit.tab.shared;

import java.awt.Container;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.netbeans.validation.api.builtin.stringvalidation.StringValidators;
import org.netbeans.validation.api.ui.ValidationGroup;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache;
import de.george.g3dit.gui.components.JEnumComboBox;
import de.george.g3dit.gui.components.JTemplateGuidField;
import de.george.g3dit.gui.dialogs.TemplateIntelliHints;
import de.george.g3dit.gui.dialogs.TemplateNameSearchDialog;
import de.george.g3dit.gui.dialogs.TemplateNameSearchDialog.TemplateSearchListener;
import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.g3dit.gui.edit.handler.LambdaPropertyHandler;
import de.george.g3dit.gui.theme.LayoutUtils;
import de.george.g3dit.gui.validation.TemplateExistenceValidator;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.validation.GuidValidator;
import de.george.g3utils.validation.ValidationGroupWrapper;
import de.george.lrentnode.archive.G3ClassContainer;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.gCInventory_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.enums.G3Enums.gEStackType;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.ClassUtil;
import de.george.lrentnode.util.PropertyUtil;
import net.miginfocom.swing.MigLayout;

public class SharedInventarTab extends AbstractPropertySharedTab {
	private InventarStacksPanel stacksPanel;

	public SharedInventarTab(EditorContext ctx, Container container) {
		super(ctx, container);
	}

	@Override
	protected void initPropertyPanel(PropertyPanel propertyPanel, ValidationGroup validation, JScrollPane scrollPane) {
		propertyPanel.addHeadline(I.tr("TreasureSets"));
		TemplateCache tpleCache = Caches.template(ctx);
		for (int i = 1; i <= 5; i++) {
			propertyPanel.add(PropertyUtil.GetTreasureSetProperty(i))
					.<JTextField>customize(tfTreasureSet -> new TemplateIntelliHints(tfTreasureSet, tpleCache,
							e -> e.getClasses().contains("gCTreasureSet_PS"), false))
					.fullWidth().done();
		}

		propertyPanel.addHeadline(I.tr("InventoryStacks"));
		stacksPanel = new InventarStacksPanel(scrollPane);
		stacksPanel.initValidation(validation);
		propertyPanel.add(new LambdaPropertyHandler(stacksPanel, stacksPanel::loadValues, stacksPanel::saveValues)).grow()
				.constraints("width 100:300:450, wrap").done();
	}

	@Override
	public void cleanUp() {}

	@Override
	public String getTabTitle() {
		return I.tr("Inventory");
	}

	@Override
	public boolean isActive(G3ClassContainer entity) {
		return entity.hasClass(CD.gCInventory_PS.class);
	}

	class InventarStacksPanel extends AbstractElementsPanel<G3ClassContainer> {
		public InventarStacksPanel(JScrollPane navScroll) {
			super(I.tr("Stack"), navScroll, true);

			setLayout(new MigLayout("fillx, insets 0 5 0 0", "[]"));
		}

		/**
		 * Nach dem Umschalten auf eine Entity aufrufen, um deren Werte ins GUI zu laden
		 */
		@Override
		public void loadValuesInternal(G3ClassContainer entity) {
			gCInventory_PS inv = entity.getClass(CD.gCInventory_PS.class);
			for (G3Class stack : inv.stacks) {
				int amount = stack.property(CD.gCInventoryStack.Amount).getInt();
				int stackType = stack.property(CD.gCInventoryStack.Generated).getEnumValue();
				String template = stack.property(CD.gCInventorySlot.Template).getGuid();
				int quality = stack.property(CD.gCInventoryStack.Quality).getInt();
				InventarStackPanel stackPanel = new InventarStackPanel(amount, stackType, template, quality);
				insertElementRelative(stackPanel, null, InsertPosition.After);
			}
		}

		/**
		 * Vor dem Umschalten auf eine andere Entity aufrufen, um Änderungen zu speichern
		 */
		@Override
		public void saveValuesInternal(G3ClassContainer entity) {
			gCInventory_PS inv = entity.getClass(CD.gCInventory_PS.class);
			inv.stacks.clear();
			for (int i = 0; i < getComponentCount(); i++) {
				InventarStackPanel stack = (InventarStackPanel) getComponent(i);
				inv.stacks.add(ClassUtil.createInventoryStack(Integer.valueOf(stack.getAmount()), stack.getStackType(),
						GuidUtil.parseGuid(stack.getTemplate()), stack.getQuality()));
			}
		}

		@Override
		protected void removeValuesInternal(G3ClassContainer entity) {
			entity.<gCInventory_PS>getClass(CD.gCInventory_PS.class).stacks.clear();
		}

		@Override
		protected AbstractElementPanel getNewElement() {
			return new InventarStackPanel(1, gEStackType.gEStackType_Normal, "", 0);
		}

	}

	class InventarStackPanel extends AbstractElementPanel implements TemplateSearchListener {
		private JTextField tfAmount;
		private JTemplateGuidField tfTemplate;
		private JEnumComboBox<gEStackType> cbStackType;

		private JButton btnTple;

		private String title;

		private QualityPanel qualityPanel;

		public InventarStackPanel(int inAmount, int inStackType, String inGuid, int inQuality) {
			super(I.tr("Stack"), stacksPanel);
			setLayout(new MigLayout("", "[]10px[]push[]"));

			add(new JLabel(I.tr("Amount")), "cell 0 0");

			tfAmount = SwingUtils.createUndoTF(String.valueOf(inAmount));
			add(tfAmount, "cell 1 0, width 50:100:100");

			add(new JLabel(I.tr("Type")), "cell 0 1");

			cbStackType = new JEnumComboBox<>(gEStackType.class);
			cbStackType.setSelectedValue(inStackType);
			add(cbStackType, "cell 1 1, width 50:100:100");

			add(new JLabel(I.tr("Template")), "cell 0 2");

			tfTemplate = new JTemplateGuidField(ctx);
			add(tfTemplate, "cell 1 2, width 100:300:300");
			tfTemplate.addGuidFiedListener(newGuid -> {
				TemplateCache tpleCache = Caches.template(ctx);
				if (tpleCache.isValid()) {
					title = tpleCache.getEntryByGuid(newGuid).map(TemplateCache.TemplateCacheEntry::getName)
							.orElse(I.tr("<no template found>"));
					updateBorderTitle();
				}
			});
			tfTemplate.setText(inGuid);

			JPanel operationPanel = getOperationPanel();
			add(operationPanel, "cell 2 0, spanx 2, spany 3");

			btnTple = new JButton(Icons.getImageIcon(Icons.Action.BOOK));
			btnTple.setToolTipText(I.tr("Load template"));

			operationPanel.add(btnTple, LayoutUtils.sqrBtn("cell 1 1"));
			btnTple.addActionListener(e -> new TemplateNameSearchDialog(InventarStackPanel.this, ctx).open());

			add(new JLabel(I.tr("Quality")), "cell 0 3");
			qualityPanel = new QualityPanel();
			qualityPanel.setQuality(inQuality);
			add(qualityPanel, "cell 1 3, spanx 3");
		}

		@Override
		@SuppressWarnings("unchecked")
		public void initValidation(ValidationGroup group) {
			tfAmount.setName(I.tr("Amount"));
			group.add(tfAmount, StringValidators.REQUIRE_VALID_INTEGER, StringValidators.REQUIRE_NON_NEGATIVE_NUMBER);
			tfTemplate.initValidation(group, I.tr("Template guid"), GuidValidator.INSTANCE, new TemplateExistenceValidator(group, ctx));
		}

		@Override
		public void removeValidation(ValidationGroupWrapper group) {
			group.remove(tfAmount);
			tfTemplate.removeValidation(group);
		}

		public String getAmount() {
			return tfAmount.getText();
		}

		public int getStackType() {
			return cbStackType.getSelectedValue();
		}

		public String getTemplate() {
			return tfTemplate.getText();
		}

		public int getQuality() {
			return qualityPanel.getQuality();
		}

		@Override
		public boolean templateSearchCallback(TemplateFile tple) {
			if (tple.getHeaderCount() == 2) {
				tfTemplate.setText(tple.getReferenceHeader().getGuid());
				return true;
			}
			return false;
		}

		@Override
		protected String getBorderTitle() {
			return title;
		}
	}
}
