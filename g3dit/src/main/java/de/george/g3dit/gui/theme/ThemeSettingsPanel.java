package de.george.g3dit.gui.theme;

import java.awt.Component;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Predicate;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.event.ListSelectionEvent;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import de.george.g3dit.gui.components.JEventList;
import de.george.g3dit.gui.components.ListCellTitledBorder;
import de.george.g3utils.gui.JComboBoxExt;
import de.george.g3utils.util.ReflectionUtils;
import net.miginfocom.swing.MigLayout;
import one.util.streamex.StreamEx;

// Adapted from IJThemesPanel
public class ThemeSettingsPanel extends JPanel {
	private final EventList<ThemeInfo> themes = new BasicEventList<>();
	private final HashMap<Integer, String> categories = new HashMap<>();
	private JComboBox<String> cbFilter;
	private JEventList<ThemeInfo> themesList;

	public ThemeSettingsPanel() {
		setLayout(new MigLayout("insets 0"));

		cbFilter = new JComboBoxExt<>("all", "dark", "light");
		cbFilter.addItemListener(e -> updateThemesList());
		add(cbFilter, "alignx right, wrap");

		themesList = new JEventList<>(themes);
		themesList.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		themesList.setCellRenderer(new DefaultListCellRenderer() {
			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				String title = categories.get(index);
				String name = ((ThemeInfo) value).name();
				int sep = name.indexOf('/');
				if (sep >= 0)
					name = name.substring(sep + 1).trim();

				JComponent c = (JComponent) super.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
				c.setToolTipText(buildToolTip((ThemeInfo) value));
				if (title != null)
					c.setBorder(new CompoundBorder(new ListCellTitledBorder(themesList, title), c.getBorder()));
				return c;
			}

			private String buildToolTip(ThemeInfo ti) {
				return "";
			}
		});
		themesList.addListSelectionListener(this::themesListValueChanged);
		add(new JScrollPane(themesList), "grow, push");
		updateThemesList();
	}

	private void updateThemesList() {
		// TODO: Custom themes in config directory?

		ThemeInfo oldSel = themesList.getSelectedValue();

		themes.clear();
		categories.clear();

		boolean filterDark = cbFilter.getSelectedItem().equals("light");
		boolean filterLight = cbFilter.getSelectedItem().equals("dark");
		Predicate<ThemeInfo> filter = i -> !(i.dark() && filterDark) && !(!i.dark() && filterLight);

		ThemeInfo nativeTheme = ThemeManager.getNativeTheme();
		if (nativeTheme != null) {
			categories.put(themes.size(), I.tr("Native"));
			themes.add(nativeTheme);
		}

		categories.put(themes.size(), I.tr("Core Themes"));
		StreamEx.of(FlatDarkLaf.class, FlatLightLaf.class, FlatIntelliJLaf.class, FlatDarculaLaf.class)
				.map(ReflectionUtils::getConstructor).map(ReflectionUtils::newInstance)
				.map(l -> new ThemeInfo(l.getName(), l.getDescription(), null, l.isDark(), l.getClass().getName(), null)).filter(filter)
				.forEach(themes::add);

		categories.put(themes.size(), I.tr("IntelliJ Themes"));
		StreamEx.of(FlatAllIJThemes.INFOS).map(i -> new ThemeInfo(i.getName(), "", null, i.isDark(), i.getClassName(), null))
				.filter(filter).forEach(themes::add);

		themesList.setSelectedValue(oldSel, true);
	}

	private void themesListValueChanged(ListSelectionEvent e) {
		ThemeInfo themeInfo = themesList.getSelectedValue();

		if (e.getValueIsAdjusting())
			return;

		SwingUtilities.invokeLater(() -> ThemeManager.setTheme(themeInfo, false));
	}

	public void setSelectedTheme(ThemeInfo theme) {
		themesList.setSelectedValue(theme, true);
	}

	public Optional<ThemeInfo> getSelectedTheme() {
		return Optional.ofNullable(themesList.getSelectedValue());
	}
}
