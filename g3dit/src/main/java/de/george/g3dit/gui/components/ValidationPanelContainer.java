package de.george.g3dit.gui.components;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;

import org.netbeans.validation.api.Validator;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.swing.ValidationPanel;

import de.george.g3utils.validation.LayeredValidationPanel;

public abstract class ValidationPanelContainer<T extends ValidationPanelContainer<T>> extends JPanel {
	private JScrollPane scrollPane;
	private LayeredValidationPanel validationPanel;

	/**
	 * ValidationPanel entählt den Tab
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T createValiditionPanel() {
		validationPanel = new LayeredValidationPanel();
		validationPanel.validationPanel().setInnerComponent(this);
		this.initComponents();
		return (T) this;
	}

	/**
	 * ValidtionPanel enthält den Tab nicht direkt, sondern eine ScrollPane, welche den Tab enthält
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T createScrolledValiditionPanel() {
		validationPanel = new LayeredValidationPanel();
		scrollPane = new JScrollPane();
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		scrollPane.setViewportView(this);
		validationPanel.validationPanel().setInnerComponent(scrollPane);
		this.initComponents();
		return (T) this;
	}

	/**
	 * Wird aufgerufen nachdem ValidationPanel und eventuell ScrollPanel erstellten wurden.
	 */
	protected abstract void initComponents();

	public ValidationPanel getValidationPanel() {
		return validationPanel.validationPanel();
	}

	public JRootPane getLayeredPane() {
		return validationPanel.layeredPane();
	}

	protected JScrollPane getScrollpane() {
		return scrollPane;
	}

	protected final ValidationGroup validation() {
		return getValidationPanel().getValidationGroup();
	}

	@SafeVarargs
	protected final <ComponentType, ValueType> void addValidators(ComponentType comp, Validator<ValueType>... validators) {
		validation().add(comp, validators);
	}
}
