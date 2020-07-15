package de.george.g3dit.tab.shared;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.tab.shared.AbstractElementsPanel.InsertPosition;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.validation.ValidationGroupWrapper;
import net.miginfocom.swing.MigLayout;

public abstract class AbstractElementPanel extends JPanel {
	private AbstractElementsPanel<?> callback;
	protected int position;

	private JPanel operationPanel;
	private JButton upButton, downButton, deleteButton;

	public AbstractElementPanel(String elementName, AbstractElementsPanel<?> elementsPanel) {
		callback = elementsPanel;
		setBorder(SwingUtils.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true), "", null, true));

		operationPanel = new JPanel(new MigLayout("insets 0"));

		upButton = new JButton(Icons.getImageIcon(Icons.Arrow.UP));
		upButton.setToolTipText("Nach oben verschieben");
		operationPanel.add(upButton, "cell 0 0, width 27!, height 27!");
		upButton.addActionListener(e -> callback.moveUp(AbstractElementPanel.this));

		deleteButton = new JButton(Icons.getImageIcon(Icons.Action.DELETE));
		deleteButton.setToolTipText("Löschen");
		operationPanel.add(deleteButton, "cell 0 1, width 27!, height 27!");
		deleteButton.addActionListener(e -> callback.removeElement(AbstractElementPanel.this));

		downButton = new JButton(Icons.getImageIcon(Icons.Arrow.DOWN));
		downButton.setToolTipText("Nach unten verschieben");
		operationPanel.add(downButton, "cell 0 2, width 27!, height 27!");
		downButton.addActionListener(e -> callback.moveDown(AbstractElementPanel.this));

		JButton insertBeforeButton = new JButton(Icons.getImageIcon(Icons.Arrow.RETURN_BEFORE));
		insertBeforeButton.setToolTipText(elementName.concat(" davor einfügen"));
		operationPanel.add(insertBeforeButton, "cell 1 0, width 27!, height 27!");
		insertBeforeButton.addActionListener(e -> callback.insertNewElement(AbstractElementPanel.this, InsertPosition.Before));

		JButton insertAfterButton = new JButton(Icons.getImageIcon(Icons.Arrow.RETURN_AFTER));
		insertAfterButton.setToolTipText(elementName.concat(" danach einfügen"));
		operationPanel.add(insertAfterButton, "cell 1 2, width 27!, height 27!");
		insertAfterButton.addActionListener(e -> callback.insertNewElement(AbstractElementPanel.this, InsertPosition.After));
	}

	/**
	 * Liefert ein Panel, welches die Steuerungsbuttons des Elements enthält. Das Panel hat ein 2x3
	 * MigLayout, in dem alle Zellen außer (1 1) besetzt sind.
	 *
	 * @return
	 */
	protected JPanel getOperationPanel() {
		return operationPanel;
	}

	public void initValidation(ValidationGroup group) {}

	public void removeValidation(ValidationGroupWrapper group) {}

	public void setStatus(boolean up, boolean down, boolean delete, int position) {
		upButton.setEnabled(up);
		downButton.setEnabled(down);
		deleteButton.setEnabled(delete);
		this.position = position;
		updateBorderTitle();
	}

	protected void updateBorderTitle() {
		((TitledBorder) getBorder()).setTitle(getBorderTitle());
		repaint();
	}

	protected abstract String getBorderTitle();
}
