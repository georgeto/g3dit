package de.george.g3dit.entitytree.filter;

import com.google.common.base.Strings;

import de.george.g3utils.io.G3Serializable;
import de.george.g3utils.util.Holder;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.properties.bCString;
import de.george.lrentnode.properties.bTArray;
import de.george.lrentnode.util.PropertyUtil;

public class StringPropertyFilter extends AbstractEntityFilter {
	private String text;

	public StringPropertyFilter(String text) {
		this.text = Strings.nullToEmpty(text).toLowerCase();
	}

	@Override
	public boolean matches(eCEntity entity) {
		return matchesStringProperty(entity);
	}

	private boolean matchesStringProperty(eCEntity entity) {
		Holder<Boolean> matches = new Holder<>(false);
		PropertyUtil.visitProperties(entity, (property, propertySet) -> {
			G3Serializable value = property.getValue();
			if (value instanceof bCString) {
				if (matchesText((bCString) value)) {
					matches.hold(true);
					return false;
				}
			} else if (value instanceof bTArray && ((bTArray<?>) value).getEntryType() == bCString.class) {
				@SuppressWarnings("unchecked")
				bTArray<bCString> array = (bTArray<bCString>) value;
				for (bCString string : array.getEntries()) {
					if (matchesText(string)) {
						matches.hold(true);
						return false;
					}
				}
			}
			return true;
		});
		return matches.held();
	}

	private boolean matchesText(bCString value) {
		return value.getString().toLowerCase().contains(text);
	}

	@Override
	public boolean isValid() {
		return !text.isEmpty();
	}
}
