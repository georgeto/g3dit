package de.george.lrentnode.properties;

import java.util.Optional;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.classes.ClassTypes;
import de.george.lrentnode.classes.G3Class;

public abstract class bTAutoPOSmartPtr<T extends G3Class> implements G3Serializable {
	protected T object;

	public Optional<T> getObject() {
		return Optional.ofNullable(object);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final void read(G3FileReader reader) {
		// Skip version
		reader.skip(2);

		boolean hasObject = reader.readBool();
		object = hasObject ? (T) ClassTypes.getClassInstance(getClassName(), reader) : null;
	}

	@Override
	public final void write(G3FileWriter writer) {
		writer.writeUnsignedShort(1);

		writer.writeBool(object != null);
		if (object != null) {
			object.write(writer);
		}
	}

	protected abstract String getClassName();

	@Override
	public String toString() {
		return object != null ? object.toString() : "<EMPTY>";
	}
}
