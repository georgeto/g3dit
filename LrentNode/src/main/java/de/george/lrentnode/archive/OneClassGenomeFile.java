package de.george.lrentnode.archive;

import java.io.IOException;

import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.io.GenomeFile;
import de.george.g3utils.structure.Stringtable;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.util.ClassUtil;

public class OneClassGenomeFile extends GenomeFile {
	private G3Class containedClass;

	public OneClassGenomeFile(G3FileReaderEx reader) throws IOException {
		read(reader);
	}

	public OneClassGenomeFile(G3Class containedClass) {
		this.containedClass = containedClass;
		stringtable = new Stringtable();
	}

	@SuppressWarnings("unchecked")
	public <T extends G3Class> T getContainedClass() {
		return (T) containedClass;
	}

	public void setContainedClass(G3Class containedClass) {
		this.containedClass = containedClass;
	}

	@Override
	protected void readInternal(G3FileReaderEx reader) {
		containedClass = ClassUtil.readSubClass(reader);
	}

	@Override
	protected void writeInternal(G3FileWriterEx writer) {
		ClassUtil.writeSubClass(writer, containedClass);
	}
}
