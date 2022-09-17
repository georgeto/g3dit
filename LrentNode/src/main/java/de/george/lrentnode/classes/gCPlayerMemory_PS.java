package de.george.lrentnode.classes;

import java.util.Map;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.util.ClassUtil;

public final class gCPlayerMemory_PS extends G3Class {
	public Map<String, G3Class> attributes;

	public gCPlayerMemory_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (classVersion <= 4) {
			throw new UnsupportedOperationException("gCPlayerMemory_PS: Version <= 4 not supported.");
		}

		attributes = reader.readMap(G3FileReader::readEntry, ClassUtil::readSubClass);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.writeMap(attributes, G3FileWriter::writeEntry, ClassUtil::writeSubClass);
	}

}
