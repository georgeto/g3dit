package de.george.g3dit.gui.components;

import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javax.swing.JPanel;

import org.netbeans.validation.api.AbstractValidator;
import org.netbeans.validation.api.ui.ValidationGroup;

import de.george.g3dit.cache.TemplateCache;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.gui.dialogs.TemplateIntelliHints;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.validation.ValidationGroupWrapper;
import net.miginfocom.swing.MigLayout;

public class JTemplateGuidField extends JPanel {
	private CopyOnWriteArrayList<GuildFieldListener> listeners;

	private UndoableTextField tfGuid;
	private UndoableTextField tfTemplateName;

	private boolean editing = false;

	private TemplateCache cache;
	private boolean all;
	private Predicate<TemplateCacheEntry> filter;

	public JTemplateGuidField(TemplateCache cache) {
		this(null, cache);
	}

	public JTemplateGuidField(String text, TemplateCache cache) {
		this(null, cache, null, false);
	}

	public JTemplateGuidField(String text, TemplateCache cache, Predicate<TemplateCacheEntry> filter, boolean all) {
		this.cache = cache;
		this.filter = filter;
		this.all = all;
		listeners = new CopyOnWriteArrayList<>();
		setLayout(new MigLayout("insets 0", "[]", "[]0[]"));

		tfGuid = SwingUtils.createUndoTF();
		add(tfGuid, "width 100%, height 19:19:19, wrap");
		tfGuid.getDocument().addDocumentListener(SwingUtils.createDocumentListener(() -> updateText()));

		tfTemplateName = SwingUtils.createUndoTF();
		add(tfTemplateName, "width 100%, height 19:19:19");
		tfTemplateName.getDocument().addDocumentListener(SwingUtils.createDocumentListener(() -> updateName()));
		new TemplateIntelliHints(tfTemplateName, cache, filter, all);

		setText(text);
		cache.addUpdateListener(this, t -> updateText());
	}

	public void setText(String guid) {
		setText(guid, false);
	}

	public void setText(String guid, boolean keepHistory) {
		editing = true;

		tfGuid.setText(guid, keepHistory);
		tfTemplateName.setText(null);
		cache.getEntryByGuid(guid, getTemplateEntities()).ifPresent(e -> tfTemplateName.setText(e.getName()));

		editing = false;
		fireGuidFieldChanged();
	}

	public String getText() {
		return tfGuid.getText();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	public void initValidation(ValidationGroup group, String fieldName, AbstractValidator... validators) {
		tfGuid.setName(fieldName);
		group.add(tfGuid, validators);
	}

	public void removeValidation(ValidationGroupWrapper group) {
		group.remove(tfGuid);
	}

	private void updateName() {
		if (editing) {
			return;
		}
		editing = true;
		cache.getEntryByName(tfTemplateName.getText(), getTemplateEntities()).ifPresent(e -> {
			tfGuid.setText(e.getGuid(), true);
			fireGuidFieldChanged();
		});
		editing = false;
	}

	private void updateText() {
		if (editing) {
			return;
		}
		editing = true;
		if (tfGuid.getText().isEmpty()) {
			tfTemplateName.setText(null, true);
			fireGuidFieldChanged();
		} else {
			String guid = GuidUtil.parseGuid(tfGuid.getText());
			if (guid != null) {
				Optional<TemplateCacheEntry> entry = cache.getEntryByGuid(guid, getTemplateEntities());
				tfTemplateName.setText(entry.isPresent() ? entry.get().getName() : null, true);
				fireGuidFieldChanged();
			}

		}
		editing = false;
	}

	public void addGuidFiedListener(GuildFieldListener listener) {
		listeners.add(listener);
	}

	protected void fireGuidFieldChanged() {
		for (GuildFieldListener listener : listeners) {
			listener.guidChanged(GuidUtil.parseGuid(tfGuid.getText()));
		}
	}

	public static interface GuildFieldListener {
		public void guidChanged(String newGuid);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		tfGuid.setEnabled(enabled);
		tfTemplateName.setEnabled(false);
	}

	private Stream<TemplateCacheEntry> getTemplateEntities() {
		Stream<TemplateCacheEntry> entities = cache.getEntities(all);
		if (filter != null) {
			entities = entities.filter(filter);
		}
		return entities;
	}
}
