package de.george.lrentnode.classes;

import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.util.ClassUtil;

public class eCCollisionShape_PS extends G3Class {

	private List<eCCollisionShape> shapes;

	public eCCollisionShape_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	public List<eCCollisionShape> getShapes() {
		return shapes;
	}

	public void addShape(eCCollisionShape shape) {
		shapes.add(shape);
	}

	public eCCollisionShape removeShape(int shapeIndex) {
		return shapes.remove(shapeIndex);
	}

	public boolean removeShape(eCCollisionShape shape) {
		return shapes.remove(shape);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		shapes = reader.readList(ClassUtil::readSubClass, eCCollisionShape.class);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.writeList(shapes, ClassUtil::writeSubClass);
	}
}
