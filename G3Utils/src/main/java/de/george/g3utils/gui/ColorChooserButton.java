package de.george.g3utils.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;

import com.bric.swing.ColorPicker;
import com.teamunify.i18n.I;

public class ColorChooserButton extends JButton {

	private Color current;

	public ColorChooserButton(Window owner, boolean showAlpha) {
		this(Color.WHITE, owner, showAlpha);
	}

	public ColorChooserButton(Color color, Window owner, boolean showAlpha) {
		setText(null);
		setIcon(null);
		setOpaque(true);
		setContentAreaFilled(false);
		setFocusPainted(false);
		setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		setSelectedColor(color);
		addActionListener(arg0 -> {
			Color newColor = ColorPicker.showDialog(owner, I.tr("Select color"), current, showAlpha);
			if (newColor != null) {
				setSelectedColor(newColor);
			}
		});
	}

	public Color getSelectedColor() {
		return current;
	}

	public void setSelectedColor(Color newColor) {
		setSelectedColor(newColor, true);
	}

	public void setSelectedColor(Color newColor, boolean notify) {

		if (newColor == null) {
			return;
		}

		current = newColor;
		setBackground(new Color(current.getRGB(), false));

		if (notify) {
			// Notify everybody that may be interested.
			for (ColorChangedListener l : listeners) {
				l.colorChanged(newColor);
			}
		}
	}

	public static interface ColorChangedListener {
		public void colorChanged(Color newColor);
	}

	private List<ColorChangedListener> listeners = new ArrayList<>();

	public void addColorChangedListener(ColorChangedListener toAdd) {
		listeners.add(toAdd);
	}

	@Override
	protected void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setColor(current);
		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.dispose();
	}
}
