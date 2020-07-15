package de.george.lrentnode.classes;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.lrentnode.structures.bCGuid;
import de.george.lrentnode.util.ClassUtil;

public abstract class eCShaderBase extends eCShaderEllementBase {
	private static final Logger logger = LoggerFactory.getLogger(eCShaderBase.class);

	protected List<G3Class> shaderElements;

	public eCShaderBase(String className, G3FileReader reader) {
		super(className, reader);
	}

	public List<G3Class> getShaderElements() {
		return shaderElements;
	}

	public void setShaderElements(List<G3Class> shaderElements) {
		this.shaderElements = shaderElements;
	}

	public Optional<eCShaderEllementBase> getElement(bCGuid token) {
		return shaderElements.stream().filter(e -> e instanceof eCShaderEllementBase).map(e -> (eCShaderEllementBase) e)
				.filter(e -> e.getToken().equals(token)).findAny();
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (reader.readShort() != 1) {
			reader.warn(logger, "eCShaderBase unbekannte Version.");
		}

		super.readPostClassVersion(reader);

		shaderElements = reader.readList(ClassUtil::readSubClass);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.writeUnsignedShort(1);

		super.writePostClassVersion(writer);

		writer.writeList(shaderElements, ClassUtil::writeSubClass);
	}
}
