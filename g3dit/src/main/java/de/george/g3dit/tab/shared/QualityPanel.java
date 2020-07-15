package de.george.g3dit.tab.shared;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JPanel;

import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.desc.PropertyDescriptor;
import de.george.lrentnode.enums.G3Enums;
import de.george.lrentnode.enums.G3Enums.gEItemQuality;
import de.george.lrentnode.properties.gLong;
import net.miginfocom.swing.MigLayout;

public class QualityPanel extends JPanel {
	private static String[] QUALITIES = G3Enums.asArray(gEItemQuality.class);

	private List<JCheckBox> cbQuality = new ArrayList<>(8);
	private PropertyDescriptor<gLong> property;

	public QualityPanel() {
		this(null);
	}

	public QualityPanel(PropertyDescriptor<gLong> property) {
		this.property = property;

		setLayout(new MigLayout("ins 0", "[]20[]20[]20[]"));
		for (int i = 0; i < 8; i++) {
			JCheckBox cbTemp = new JCheckBox(QUALITIES[i]);
			cbQuality.add(cbTemp);
			add(cbTemp, "width 20%" + ((i + 1) % 4 == 0 ? ", wrap" : ""));
		}
	}

	public void setQuality(int quality) {
		for (int i = 0; i < 8; i++) {
			int itemQuality = G3Enums.asInt(gEItemQuality.class, QUALITIES[i]);
			cbQuality.get(i).setSelected((quality & itemQuality) == itemQuality);
		}
	}

	public int getQuality() {
		int quality = 0;
		for (int i = 0; i < 8; i++) {
			if (cbQuality.get(i).isSelected()) {
				quality |= G3Enums.asInt(gEItemQuality.class, QUALITIES[i]);
			}
		}
		return quality;
	}

	public void readQuality(G3Class clazz) {
		setQuality(clazz.property(property).getLong());
	}

	public void writeQuality(G3Class clazz) {
		clazz.property(property).setLong(getQuality());
	}

	public static String getQualityAsString(int quality) {
		List<String> qualities = new ArrayList<>();
		for (int i = 0; i < 8; i++) {
			int itemQuality = G3Enums.asInt(gEItemQuality.class, QUALITIES[i]);
			if ((quality & itemQuality) == itemQuality) {
				qualities.add(QUALITIES[i]);
			}
		}
		return qualities.isEmpty() ? "-" : qualities.stream().collect(Collectors.joining(" | "));
	}
}
