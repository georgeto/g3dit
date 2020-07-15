package de.george.g3dit.tab.template.views;

import java.awt.Component;

import de.george.g3dit.EditorContext;
import de.george.g3dit.tab.shared.SharedPropertyView;
import de.george.lrentnode.template.TemplateFile;

public class PropertyView extends SharedPropertyView implements TemplateView {
	public PropertyView(EditorContext ctx) {
		super(ctx);
	}

	@Override
	public void load(TemplateFile entity) {
		super.load(entity.getReferenceHeader());
	}

	@Override
	public void save(TemplateFile entity) {
		super.save(entity.getReferenceHeader());
	}

	@Override
	public Component getContent() {
		return this;
	}

	@Override
	public void cleanUp() {
		super.cleanUp();
	}

}
