package de.george.g3dit.tab.shared;

import java.awt.Container;

import javax.swing.JScrollPane;

import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.edit.PropertyPanel;
import de.george.lrentnode.archive.G3ClassContainer;
import net.miginfocom.swing.MigLayout;

public abstract class AbstractPropertySharedTab extends AbstractSharedTab {
	private PropertyPanel propertyPanel;

	public AbstractPropertySharedTab(EditorContext ctx, Container container) {
		super(ctx, container);
	}

	@Override
	public final void initComponents(ValidationGroup validation, JScrollPane scrollPane) {
		container.setLayout(getLayout());
		propertyPanel = new PropertyPanel(ctx);
		initPropertyPanel(propertyPanel, validation, scrollPane);
		container.add(propertyPanel.getContent(), "spanx" + (isGrow() ? ", grow" : ""));
	}

	protected MigLayout getLayout() {
		return new MigLayout("fillx");
	}

	protected boolean isGrow() {
		return true;
	}

	protected abstract void initPropertyPanel(PropertyPanel propertyPanel, ValidationGroup validation, JScrollPane scrollPane);

	@Override
	public void loadValues(G3ClassContainer container) {
		propertyPanel.load(container);
	}

	@Override
	public void saveValues(G3ClassContainer container) {
		propertyPanel.save(container);
	}
}
