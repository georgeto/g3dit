package de.george.g3dit.gui.renderer;

import java.awt.Component;
import java.lang.reflect.Method;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import com.l2fprod.common.beans.BeanUtils;

import de.george.g3utils.gui.SwingUtils;

public class BeanListCellRenderer extends DefaultListCellRenderer {
	private String propertyName;
	private String toolTipPropertyName;
	private int wrapDescription;

	public BeanListCellRenderer(String propertyName) {
		this(propertyName, null);
	}

	public BeanListCellRenderer(String propertyName, String toolTipPropertyName) {
		this(propertyName, toolTipPropertyName, -1);
	}

	public BeanListCellRenderer(String propertyName, String toolTipPropertyName, int wrapDescription) {
		this.propertyName = propertyName;
		this.toolTipPropertyName = toolTipPropertyName;
		this.wrapDescription = wrapDescription;
	}

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

		try {
			Method method = BeanUtils.getReadMethod(value.getClass(), propertyName);
			if (method != null) {
				setText(method.invoke(value).toString());
			}

			if (toolTipPropertyName != null) {
				Method tooltipMethod = BeanUtils.getReadMethod(value.getClass(), toolTipPropertyName);
				if (tooltipMethod != null) {
					String tooltipText = tooltipMethod.invoke(value).toString();
					if (wrapDescription != -1) {
						tooltipText = SwingUtils.wrapTooltipText(tooltipText, wrapDescription);
					}
					setToolTipText(tooltipText);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return this;
	}

}
