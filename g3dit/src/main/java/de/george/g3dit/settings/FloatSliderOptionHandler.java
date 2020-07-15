package de.george.g3dit.settings;

import java.awt.Window;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.util.Misc;

public class FloatSliderOptionHandler extends TitledOptionHandler<Float> {
	private JSlider sSlider;
	private int min, max;
	private float divider;
	private boolean textInput;
	private boolean editingLock = false;
	private JTextField tfSliderValue;

	public FloatSliderOptionHandler(Window parent, String title, int min, int max, float divider, boolean textInput) {
		super(parent, title);
		this.min = min;
		this.max = max;
		this.divider = divider;
		this.textInput = textInput;
	}

	@Override
	protected void load(Float value) {
		editingLock = true;
		sSlider.setValue(Math.round(value * divider));
		if (textInput) {
			tfSliderValue.setText(Misc.formatFloat(value));
		}
		editingLock = false;
	}

	@Override
	protected Optional<Float> save() {
		return Optional.of(sSlider.getValue() / divider);
	}

	@Override
	protected void addValueComponent(JPanel content) {
		sSlider = createSliderWithLabels();
		content.add(sSlider, "grow");
		if (textInput) {
			tfSliderValue = new JTextField();
			content.add(tfSliderValue, "width 50!");
			tfSliderValue.getDocument().addDocumentListener(SwingUtils.createDocumentListener(() -> {
				if (!editingLock) {
					editingLock = true;
					try {
						sSlider.setValue(Math.round(Misc.parseFloat(tfSliderValue.getText()) * divider));
					} catch (NumberFormatException e) {
					}
					editingLock = false;
				}
			}));

			sSlider.addChangeListener(e -> {
				if (!editingLock) {
					editingLock = true;
					tfSliderValue.setText(Misc.formatFloat(sSlider.getValue() / divider));
					editingLock = false;
				}
			});
		}
	}

	private JSlider createSliderWithLabels() {
		JSlider slider = new JSlider(SwingConstants.HORIZONTAL, min, max, min);
		Dictionary<Integer, JComponent> labels = new Hashtable<>();
		int inc = (max - min) / 5;
		slider.setMajorTickSpacing(inc);
		for (int labelIndex = min; labelIndex <= max; labelIndex += inc) {
			labels.put(Integer.valueOf(labelIndex), new JLabel(Misc.formatFloat(labelIndex / divider), SwingConstants.CENTER));
		}
		slider.setLabelTable(labels);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		return slider;
	}
}
