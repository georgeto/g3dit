package de.george.lrentnode.effect;

import java.util.ArrayList;
import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.util.ClassUtil;

public class gCEffectCommandSequence implements G3Serializable {
	private String name;
	private List<gCEffectCommand> commands = new ArrayList<>();

	public gCEffectCommandSequence(String name) {
		this.name = name;
	}

	public gCEffectCommandSequence(String name, List<gCEffectCommand> commands) {
		this.name = name;
		this.commands.addAll(commands);
	}

	@Override
	public void read(G3FileReader reader) {
		name = reader.readEntry();
		if (reader.readUnsignedShort() != 1) {
			throw new IllegalStateException("Unsupported EffectCommandSequence version.");
		}
		commands = reader.readList(ClassUtil::readSubClass, gCEffectCommand.class);
	}

	@Override
	public void write(G3FileWriter writer) {
		writer.writeEntry(name);
		writer.writeUnsignedShort(1);
		writer.writeList(commands, ClassUtil::writeSubClass);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<gCEffectCommand> getCommands() {
		return commands;
	}

	public void setCommands(List<gCEffectCommand> commands) {
		this.commands = commands;
	}

	@Override
	public String toString() {
		return name;
	}
}
