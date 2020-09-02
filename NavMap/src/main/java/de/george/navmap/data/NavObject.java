package de.george.navmap.data;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.properties.eCPropertySetProxy;

public class NavObject implements G3Serializable {
	private static Logger logger = LoggerFactory.getLogger(NavObject.class);

	public String guid;
	public boolean isNavPath;
	public List<Integer> indexes;

	public NavObject(String guid, boolean isNavPath, List<Integer> indexes) {
		this.guid = guid;
		this.isNavPath = isNavPath;
		this.indexes = indexes;
	}

	public void addIndex(int index) {
		for (int i = 0; i < indexes.size(); i++) {
			Integer curIndex = indexes.get(i);

			if (index == curIndex) {
				logger.warn("{} {}: Insertion of index {} failed, since it is already registered.", !isNavPath ? "NavZone" : "NavPath",
						guid, index);
			}

			if (curIndex > index) {
				indexes.add(i, index);
				return;
			}
		}
		indexes.add(index);
	}

	public void removeIndex(Integer index) {
		if (!indexes.remove(index)) {
			logger.warn("{} {}: Removal of index {} failed, since it is not registered.", !isNavPath ? "NavZone" : "NavPath", guid, index);
		}
	}

	@Override
	public void read(G3FileReader reader) {
		guid = reader.read(eCPropertySetProxy.class).getGuid();
		isNavPath = reader.readBool();
		reader.skipListPrefix();
		indexes = reader.readList(G3FileReader::readInt, reader.readUnsignedShort());
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.write(new eCPropertySetProxy(guid, isNavPath ? "gCNavPath_PS" : "gCNavZone_PS"));
		writer.writeBool(isNavPath);
		writer.writeListPrefix();
		writer.writeUnsignedShort(indexes.size());
		writer.write(indexes, G3FileWriter::writeInt);
	}
}
