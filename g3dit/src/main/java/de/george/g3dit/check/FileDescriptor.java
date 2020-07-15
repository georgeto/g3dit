package de.george.g3dit.check;

import java.io.File;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.george.lrentnode.archive.ArchiveFile.ArchiveType;

public class FileDescriptor implements Comparable<FileDescriptor> {
	public enum FileType {
		Node,
		Lrentdat,
		Template,
		Other
	}

	private final File path;
	private final FileType type;

	@JsonCreator
	public FileDescriptor(@JsonProperty("path") File path, @JsonProperty("type") FileType type) {
		this.path = Objects.requireNonNull(path);
		this.type = Objects.requireNonNull(type);
	}

	public FileDescriptor(File path, ArchiveType type) {
		this(path, type == ArchiveType.Node ? FileType.Node : FileType.Lrentdat);
	}

	@JsonProperty
	public File getPath() {
		return path;
	}

	@JsonProperty
	public FileType getType() {
		return type;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof FileDescriptor)) {
			return false;
		}

		FileDescriptor castOther = (FileDescriptor) other;
		return Objects.equals(path, castOther.path) && Objects.equals(type, castOther.type);
	}

	@Override
	public int hashCode() {
		return Objects.hash(path, type);
	}

	@Override
	public int compareTo(FileDescriptor o) {
		return path.compareTo(o.path);
	}

	public static FileDescriptor none(FileType type) {
		return new FileDescriptor(new File("<None>"), type);
	}

	public static FileDescriptor none(ArchiveType type) {
		return new FileDescriptor(new File("<None>"), type);
	}
}
