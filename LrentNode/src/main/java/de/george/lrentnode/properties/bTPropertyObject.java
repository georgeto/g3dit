package de.george.lrentnode.properties;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.classes.ClassTypes;
import de.george.lrentnode.classes.G3Class;

public class bTPropertyObject implements G3Serializable {
	private String className;
	private G3Class clazz;

	public bTPropertyObject(String className) {
		this.className = className;
	}

	@Override
	public void read(G3FileReader reader) {
		clazz = ClassTypes.getClassInstance(className, reader);
	}

	@Override
	public void write(G3FileWriter writer) {
		clazz.write(writer);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public G3Class getClazz() {
		return clazz;
	}

	public void setClazz(G3Class clazz) {
		this.clazz = clazz;
	}
}
