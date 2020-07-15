package de.george.g3dit.gui.components;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;

import org.netbeans.validation.api.ui.swing.ValidationPanel;

import de.george.g3utils.validation.LayeredValidationPanel;

public abstract class ValidationPanelContainer<T extends ValidationPanelContainer<T>> extends JPanel {
	private JScrollPane scrollPane;
	private LayeredValidationPanel validationPanel;

	/**
	 * Einmalig nach erstellen des Panels aufrufen, um die Fehleranzeige zu initialisieren
	 */
	public abstract void initValidation();

	/**
	 * ValidationPanel entählt den Tab
	 *
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public T createValiditionPanel() {
		validationPanel = new LayeredValidationPanel();
		validationPanel.validationPanel().setInnerComponent(this);
		this.afterScrollPaneCreation();
		this.initValidation();
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
		this.afterScrollPaneCreation();
		this.initValidation();
		return (T) this;
	}

	/**
	 * Wird aufgerufen, nachdem scrollPane instanziiert wurde
	 */
	protected void afterScrollPaneCreation() {}

	public ValidationPanel getValidationPanel() {
		return validationPanel.validationPanel();
	}

	public JRootPane getLayeredPane() {
		return validationPanel.layeredPane();
	}

	protected JScrollPane getScrollpane() {
		return scrollPane;
	}
}
