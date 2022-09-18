package de.george.lrentnode.classes;

import java.util.List;

import de.george.g3utils.io.G3FileReader;
import de.george.g3utils.io.G3FileWriter;
import de.george.g3utils.io.G3Serializable;
import de.george.lrentnode.properties.bTValArray_long;

public final class gCQuestManager_PS extends G3Class {
	public static class QuestRuntimeData implements G3Serializable {
		public static class LogEntry implements G3Serializable {
			public String heading;
			public String text;

			@Override
			public void read(G3FileReader reader) {
				reader.skip(2);
				heading = reader.readEntry();
				text = reader.readEntry();
			}

			@Override
			public void write(G3FileWriter writer) {
				writer.writeUnsignedShort(1);
				writer.writeEntry(heading);
				writer.writeEntry(text);
			}
		}

		public String name;
		public int status;
		public int activationTimeYear;
		public int activationTimeDay;
		public int activationTimeHour;
		public bTValArray_long deliveryCounter;
		public List<LogEntry> logText;

		@Override
		public void read(G3FileReader reader) {
			name = reader.readEntry();
			if (reader.readUnsignedShort() != 3)
				throw new UnsupportedOperationException("gCQuestManager_PS.Quest: Version != 3 not supported.");

			status = reader.readInt();
			activationTimeYear = reader.readInt();
			activationTimeDay = reader.readInt();
			activationTimeHour = reader.readInt();
			deliveryCounter = reader.read(bTValArray_long.class);
			logText = reader.readPrefixedList(LogEntry.class);
		}

		@Override
		public void write(G3FileWriter writer) {
			writer.writeEntry(name);
			writer.writeUnsignedShort(3);
			writer.writeInt(status);
			writer.writeInt(activationTimeYear);
			writer.writeInt(activationTimeDay);
			writer.writeInt(activationTimeHour);
			writer.write(deliveryCounter);
			writer.writePrefixedList(logText);
		}
	}

	private List<QuestRuntimeData> quests;

	public gCQuestManager_PS(String className, G3FileReader reader) {
		super(className, reader);
	}

	@Override
	protected void readPostClassVersion(G3FileReader reader) {
		if (classVersion >= 2)
			quests = reader.readList(QuestRuntimeData.class);
	}

	@Override
	protected void writePostClassVersion(G3FileWriter writer) {
		if (classVersion >= 2)
			writer.writeList(quests);

	}
}
