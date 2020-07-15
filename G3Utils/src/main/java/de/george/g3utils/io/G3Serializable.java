package de.george.g3utils.io;

public interface G3Serializable {
	public void read(G3FileReader reader);

	public default void read(G3FileReader reader, int size) {
		read(reader);
	}

	public void write(G3FileWriter writer);
}
