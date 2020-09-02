package de.george.lrentnode.classes;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;

public class gCItem_PS extends G3Class {
	private static final Logger logger = LoggerFactory.getLogger(gCItem_PS.class);

	// Konstant, wird in gCItem_PS::Write immer auf diesen Wert gesetzt.
	private static final String ITEM_STATS = "010000000100000000010000000001000000000100000000";

	private int slot, scriptExecuted;
	// In Templates egal (?!), in den Weltdaten auf 1, wenn Ausrüstung von NPC, und ansonsten 0.
	private int visible;
	private List<ScriptLine> script;

	public gCItem_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (!reader.read(24).equals(ITEM_STATS)) {
			reader.warn(logger, "(1) gCItem_PS unexpected file structure.");
		}
		slot = reader.readInt();
		visible = reader.readUnsignedByte();
		script = reader.readPrefixedList(ScriptLine.class);
		scriptExecuted = reader.readUnsignedByte();
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		writer.write(ITEM_STATS).writeInt(slot).writeUnsignedByte(visible);
		writer.writePrefixedList(script);
		writer.writeUnsignedByte(scriptExecuted);
	}

	public int getSlot() {
		return slot;
	}

	public void setSlot(int slot) {
		this.slot = slot;
	}

	public boolean getScriptExecuted() {
		return (slot & 1) != 0;
	}

	public void setScriptExecuted(boolean scriptExecuted) {
		this.scriptExecuted = scriptExecuted ? 1 : 0;
	}

	public boolean getVisible() {
		return (visible & 1) != 0;
	}

	public void setVisible(boolean visible) {
		this.visible = visible ? 1 : 0;
	}

	public List<ScriptLine> getScript() {
		return script;
	}

	public void setScript(List<ScriptLine> script) {
		this.script = script;
	}

	public static class ScriptLine implements G3Serializable {
		public String command, entity1, entity2, id1, id2;

		public ScriptLine() {
			this("", "", "", "", "");
		}

		public ScriptLine(String command, String entity1, String entity2, String id1, String id2) {
			this.command = command;
			this.entity1 = entity1;
			this.entity2 = entity2;
			this.id1 = id1;
			this.id2 = id2;
		}

		public String getCommand() {
			return command;
		}

		public void setCommand(String command) {
			this.command = command;
		}

		public String getEntity1() {
			return entity1;
		}

		public void setEntity1(String entity1) {
			this.entity1 = entity1;
		}

		public String getEntity2() {
			return entity2;
		}

		public void setEntity2(String entity2) {
			this.entity2 = entity2;
		}

		public String getId1() {
			return id1;
		}

		public void setId1(String id1) {
			this.id1 = id1;
		}

		public String getId2() {
			return id2;
		}

		public void setId2(String id2) {
			this.id2 = id2;
		}

		@Override
		public void read(G3FileReader reader) {
			reader.skip(2); // Always '0100'
			command = reader.readEntry();
			entity1 = reader.readEntry();
			entity2 = reader.readEntry();
			id1 = reader.readEntry();
			id2 = reader.readEntry();
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeUnsignedShort(1);
			writer.writeEntry(command).writeEntry(entity1).writeEntry(entity2).writeEntry(id1).writeEntry(id2);
		}
	}
}
