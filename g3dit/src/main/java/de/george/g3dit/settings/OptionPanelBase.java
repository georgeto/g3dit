package de.george.g3dit.settings;

import java.awt.Component;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;

import de.george.g3dit.gui.dialogs.CheckBoxListSelectDialog;
import de.george.g3dit.gui.renderer.BeanListCellRenderer;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import net.miginfocom.swing.MigLayout;

@SuppressWarnings("rawtypes")
public class OptionPanelBase<T extends OptionPanelBase<T>> {
	private Window parent;
	private JPanel content;
	private JButton btnReset;
	private Map<OptionHandler, Option> handlers = new LinkedHashMap<>();

	public OptionPanelBase(Window parent) {
		this(parent, "fillx", "[grow]", null);
	}

	public OptionPanelBase(Window parent, String layoutConstraints) {
		this(parent, layoutConstraints, null, null);
	}

	public OptionPanelBase(Window parent, String layoutConstraints, String colConstraints) {
		this(parent, layoutConstraints, colConstraints, null);
	}

	public OptionPanelBase(Window parent, String layoutConstraints, String colConstraints, String rowConstraints) {
		this.parent = parent;

		content = new JPanel(new MigLayout(layoutConstraints, colConstraints, rowConstraints));

		btnReset = new JButton(Icons.getImageIcon(Icons.Document.TEMPLATE));
		btnReset.setToolTipText("Einstellungen zurücksetzen");
		btnReset.setFocusable(false);
		btnReset.addActionListener(a -> resetToDefaults());
	}

	@SuppressWarnings("unchecked")
	public T addHeadline(String text) {
		content.add(SwingUtils.createBoldLabel(text), handlers.isEmpty() ? "" : "newline, gaptop u");
		return (T) this;
	}

	public T addComponent(Component comp) {
		return addComponent(comp, "grow");
	}

	@SuppressWarnings("unchecked")
	public T addComponent(Component comp, String constraints) {
		content.add(comp, Joiner.on(", ").skipNulls().join("newline, gapleft i", Strings.emptyToNull(constraints)));
		return (T) this;
	}

	public T addOption(Option<?> option) {
		return addOption(option, "grow");
	}

	public T addOptionHorizontalStart(Option<?> option, String constraints, int count) {
		return addOptionInternal(option,
				Joiner.on(", ").skipNulls().join("newline", "gapleft i", "split " + count, Strings.emptyToNull(constraints)));
	}

	public T addOptionHorizontal(Option<?> option, String constraints) {
		return addOptionInternal(option, Joiner.on(", ").skipNulls().join("gapleft i", Strings.emptyToNull(constraints)));
	}

	public T addOption(Option<?> option, String constraints) {
		return addOptionInternal(option, Joiner.on(", ").skipNulls().join("newline, gapleft i", Strings.emptyToNull(constraints)));
	}

	@SuppressWarnings("unchecked")
	private T addOptionInternal(Option<?> option, String constraints) {
		if (handlers.isEmpty()) {
			addResetButton();
		}

		OptionHandler<?> handler = option.createOptionHandler(parent);
		content.add(handler.getContent(), constraints);
		handlers.put(handler, option);
		return (T) this;
	}

	public void removeAllOptions() {
		content.removeAll();
		content.revalidate();
		handlers.clear();
	}

	public JComponent getContent() {
		return content;
	}

	@SuppressWarnings("unchecked")
	public void load(OptionStore optionStore) {
		handlers.forEach((key, value) -> key.load(optionStore, value));
	}

	@SuppressWarnings("unchecked")
	public void save(OptionStore optionStore) {
		handlers.forEach((key, value) -> key.save(optionStore, value));
	}

	@SuppressWarnings("unchecked")
	public void cancel(OptionStore optionStore) {
		handlers.forEach((key, value) -> key.cancel(optionStore, value));
	}

	@SuppressWarnings("unchecked")
	public void resetToDefaults() {
		CheckBoxListSelectDialog<Option> dialog = new CheckBoxListSelectDialog<>(parent, "Einstellungen zurücksetzen", handlers.values(),
				new BeanListCellRenderer("displayName"));
		if (dialog.openAndWasSuccessful()) {
			List<Option> entries = dialog.getSelectedEntries();
			handlers.entrySet().stream().filter(k -> entries.contains(k.getValue()))
					.forEach(k -> k.getKey().load(DefaultValueOptionStore.INSTANCE, k.getValue()));
		}
	}

	private void addResetButton() {
		content.add(btnReset, "id reset, pos (container.x2 - reset.w) (container.y)");
	}
}
