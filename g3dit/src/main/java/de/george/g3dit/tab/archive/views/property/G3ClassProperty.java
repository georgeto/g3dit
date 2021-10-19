package de.george.g3dit.tab.archive.views.property;

import com.l2fprod.common.propertysheet.AbstractProperty;
import com.l2fprod.common.propertysheet.Property;

import de.george.lrentnode.classes.G3Class;

public class G3ClassProperty extends AbstractProperty {
	private G3Class clazz;

	private String category;
	private Property parent;

	private String name;

	public G3ClassProperty(String name, G3Class clazz) {
		this.name = name;
		this.clazz = clazz;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return getName();
	}

	@Override
	public String getShortDescription() {
		return clazz.getClassName();
	}

	@Override
	public Class<?> getType() {
		return null;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public void setValue(Object value) {}

	@Override
	public Property getParentProperty() {
		return parent;
	}

	@Override
	public Property[] getSubProperties() {
		return clazz.properties().stream().map(p -> new G3Property(p, clazz.getClassName())).toArray(G3Property[]::new);
	}

	@Override
	public void readFromObject(Object object) {
		// We don't need this
	}

	@Override
	public void writeToObject(Object object) {
		// We don't need this
	}
}
