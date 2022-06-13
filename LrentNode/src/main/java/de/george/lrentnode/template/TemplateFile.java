package de.george.lrentnode.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import de.george.g3utils.io.G3FileReaderEx;
import de.george.g3utils.io.G3FileWriterEx;
import de.george.g3utils.structure.Stringtable;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.AbstractEntityFile;
import de.george.lrentnode.archive.EntityTreeTraverser;
import de.george.lrentnode.archive.eCEntity;

public class TemplateFile extends AbstractEntityFile<TemplateEntity> {
	private static final byte[] IDENTIFIER = Misc.asByte("47454E4F4D455450");

	public TemplateFile(G3FileReaderEx reader) throws IOException {
		if (!isGenomeFile(reader)) {
			readInternalExt(reader, true);
		} else {
			read(reader);
		}
	}

	@Override
	protected void readInternal(G3FileReaderEx reader) throws IOException {
		readInternalExt(reader, false);
	}

	private void readInternalExt(G3FileReaderEx reader, boolean stringtableInPlace) throws IOException {
		if (reader.getRemainingSize() < IDENTIFIER.length || !Arrays.equals(reader.readByteArray(IDENTIFIER.length), IDENTIFIER)) {
			throw new IOException("'" + reader.getFileName() + "' is not a valid .tple file.");
		}

		// Skip version
		reader.skip(4);

		if (stringtableInPlace) {
			reader.readStringtable(reader.getPos(), false);
			stringtable = reader.getStringtable();
		} else {
			boolean indexedStringtableEnabled = reader.readBool();
			if (indexedStringtableEnabled) {
				int indexCount = reader.readInt();

				// Der Abschnitt 'String Index Table Entries' ist gemein, er kann die Stringtable
				// verkomplizieren,
				// indem mehrere Indexe auf einen Stringtableeintrag zeigen
				Stringtable indexedStringtable = new Stringtable();
				for (int i = 0; i < indexCount; i++) {
					indexedStringtable.addEntry(stringtable.getEntry(reader.readShort()));
				}
				reader.setStringtable(indexedStringtable);
			}
		}

		reader.skip(4); // Size Of Code Section

		List<TemplateEntity> headers = new ArrayList<>();
		int headerCount = reader.readInt();
		for (int i = 0; i < headerCount; i++) {
			TemplateEntity header = new TemplateEntity(false);
			header.read(reader, false);
			headers.add(header);
		}

		int parentIndex = 0;
		while ((parentIndex = reader.readInt()) != -1) {
			int childIndex = reader.readInt();
			if (childIndex < 0 || childIndex >= headers.size()) {
				throw new IOException(reader.getFileName() + ": SubEntityDefinition contains invalid entity index " + childIndex
						+ " at address " + (reader.getPos() - 4) + ".");
			}
			// Attach entity
			headers.get(parentIndex).attachChild(headers.get(childIndex));
		}

		setGraph(headers.get(0));
	}

	@Override
	protected void writeInternal(G3FileWriterEx writer) {
		writer.write(IDENTIFIER).writeUnsignedShort(0x3e).writeUnsignedShort(1);
		writer.writeBool(true);
		// TODO: Müsste man eigentlich am Ende schreiben, da erst dann die wahre Größe der
		// Stringtable
		// bekannt ist
		writer.writeInt(-1);

		ImmutableList<TemplateEntity> headers = getHeaders().toList();
		writer.writeInt(headers.size());
		for (TemplateEntity header : headers) {
			header.write(writer);
		}

		// SubEntityDefinition schreiben
		for (TemplateEntity entity : headers) {
			for (eCEntity child : entity.getChilds()) {
				writer.writeInt(headers.indexOf(entity)).writeInt(headers.indexOf(child));
			}
		}

		writer.writeInt(-1).writeInt(-1);

		// IndexedStringtable
		writer.startInsert(27);
		writer.writeInt(stringtable.getEntryCount());
		for (int i = 0; i < stringtable.getEntryCount(); i++) {
			writer.writeUnsignedShort(i);
		}
		writer.finishInsert();
	}

	@Override
	protected void writeInternalAfterDeadbeef(G3FileWriterEx writer, int deadbeef) {
		writer.replaceInt(deadbeef - 35 - stringtable.getEntryCount() * 2, 31 + stringtable.getEntryCount() * 2);
	}

	public TemplateEntity getHeaderByPosition(int position) {
		return getHeaders().get(position);
	}

	public int getHeaderCount() {
		return getHeaders().size();
	}

	public FluentIterable<TemplateEntity> getHeaders() {
		return EntityTreeTraverser.traversePreOrder(graph);
	}

	/**
	 * Ermittelt den Item-Header der Template, dieser hat {@link TemplateEntity#helperParent}
	 * aktiviert, und wird ausschließlich zur Verwaltung der Template über {@code *.lrtpldatasc}
	 * Dateien verwendet.
	 *
	 * @return
	 */
	public TemplateEntity getItemHeader() {
		return graph;
		// return headers.stream().filter(h -> h.helperParent).findFirst().orElse(null);
	}

	/**
	 * Ermittelt den Reference-Header der Template, dieser hat {@link TemplateEntity#helperParent}
	 * deaktiviert, und beschreibt für normale Templates, d.h. Templates die keine ObjectGroups
	 * sind, den zweiten Header der Template.
	 *
	 * @return
	 */
	public TemplateEntity getReferenceHeader() {
		return getHeaders().firstMatch(h -> !h.helperParent).orNull();
	}

	public String getFileName() {
		return getReferenceHeader().getFileName();
	}

	public void setFileName(String fileName) {
		getItemHeader().setFileName(fileName);
		getReferenceHeader().setFileName(fileName);
	}

	public String getEntityName() {
		return getReferenceHeader().getName();
	}

	public void setEntityName(String entityName) {
		getItemHeader().setName(entityName);
		getReferenceHeader().setName(entityName);
	}

	public String getTemplateContext() {
		return getFileName().replaceFirst("_" + Pattern.quote(getEntityName()) + "\\.tple$", "");
	}
}
