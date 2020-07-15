package de.george.g3utils.validation;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;

import javax.swing.JLabel;
import javax.swing.JRootPane;
import javax.swing.plaf.basic.BasicLabelUI;

import org.netbeans.validation.api.ui.swing.ValidationPanel;

import de.george.g3utils.util.ReflectionUtils;

public class LayeredValidationPanel {
	private ValidationPanel validationPanel;
	private JRootPane rootPane;

	public LayeredValidationPanel() {
		validationPanel = new ValidationPanel();

		/**
		 * Workaround for X11GraphicsEnvironment
		 * <p>
		 * The native method X11GraphicsDevice.getCurrentDisplayMode() blocks sometimes. Used by
		 * netbeans.MultilineLabelUI.
		 */
		String geName = GraphicsEnvironment.getLocalGraphicsEnvironment().getClass().getName();
		if (geName.equals("sun.awt.X11GraphicsEnvironment")) {
			Field problemLabelField = ReflectionUtils.getField(ValidationPanel.class, "problemLabel");
			JLabel problemLabel = (JLabel) ReflectionUtils.getFieldValue(problemLabelField, validationPanel);
			problemLabel.setUI(new BasicLabelUI());
		}

		rootPane = new JRootPane();
		rootPane.getContentPane().add(validationPanel);
	}

	public ValidationPanel validationPanel() {
		return validationPanel;
	}

	public JRootPane layeredPane() {
		return rootPane;
	}
}
