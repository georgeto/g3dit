package de.george.g3dit.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractButton;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;

import de.george.g3utils.util.Misc;

public class FloatSpinner extends JSpinner {
	private boolean processingKeyBinding = false;
	private float lastValue = 0;

	private List<ActionListener> listeners = new ArrayList<>();

	public FloatSpinner() {
		this(1.0f);
	}

	public FloatSpinner(float min, float max) {
		this(min, max, 1.0f);
	}

	public FloatSpinner(float stepSize) {
		this(-Float.MAX_VALUE, Float.MAX_VALUE, stepSize);
	}

	public FloatSpinner(float min, float max, float stepSize) {
		super(new SpinnerNumberModel(Float.valueOf(0.0f), Float.valueOf(min), Float.valueOf(max), Float.valueOf(stepSize)));
		NumberEditor editor = new JSpinner.NumberEditor(this, "0.####");
		DecimalFormat format = editor.getFormat();
		format.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.US));
		setEditor(editor);

		final AbstractButton btn1 = (AbstractButton) getComponent(0);
		final AbstractButton btn2 = (AbstractButton) getComponent(1);

		addChangeListener(e -> {
			float value = getVal();
			// Up or down button pressed, or up or down arrow key pressed
			if (Misc.compareFloat(Math.abs(value - lastValue), getStepSize(), 0.00001f)
					&& (btn1.getModel().isArmed() || btn2.getModel().isArmed() || processingKeyBinding)) {
				for (ActionListener listener : listeners) {
					listener.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, null));
				}
			}
			lastValue = value;
		});
	}

	@Override
	protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
		processingKeyBinding = true;
		boolean result = super.processKeyBinding(ks, e, condition, pressed);
		processingKeyBinding = false;
		return result;
	}

	public float getVal() {
		return (Float) getValue();
	}

	public void setVal(float value) {
		setValue(value);
		// Update displayed value
		((DefaultEditor) getEditor()).stateChanged(new ChangeEvent(this));
	}

	public float getLastVal() {
		return lastValue;
	}

	public float getStepSize() {
		return ((SpinnerNumberModel) getModel()).getStepSize().floatValue();
	}

	public void setStepSize(float step) {
		((SpinnerNumberModel) getModel()).setStepSize(step);
	}

	public void addSpinActionListener(ActionListener listener) {
		listeners.add(listener);
	}

	public static void bindStepSizeEditor(JTextField tfEditor, FloatSpinner... spinners) {
		float stepping = Float.valueOf(tfEditor.getText());
		if (stepping > 0) {
			for (FloatSpinner spinner : spinners) {
				spinner.setStepSize(stepping);
			}
		}

		tfEditor.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				try {
					float stepping = Float.valueOf(tfEditor.getText());
					if (stepping > 0) {
						for (FloatSpinner spinner : spinners) {
							spinner.setStepSize(stepping);
						}
					}
				} catch (NumberFormatException e1) {
					// Nothing
				}
				tfEditor.setText(Misc.formatFloat(spinners[0].getStepSize()));
			}

			@Override
			public void focusGained(FocusEvent e) {}
		});
	}
}
