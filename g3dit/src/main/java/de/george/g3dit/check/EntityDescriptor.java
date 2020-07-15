package de.george.g3dit.check;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.george.lrentnode.archive.eCEntity;

public class EntityDescriptor {
	private final String name;
	private final String displayName;
	private final String guid;
	private final int index;
	private final FileDescriptor file;

	@JsonCreator
	public EntityDescriptor(@JsonProperty("name") String name, @JsonProperty("displayName") String displayName,
			@JsonProperty("guid") String guid, @JsonProperty("index") int index, @JsonProperty("file") FileDescriptor file) {
		this.name = name;
		this.displayName = displayName;
		this.guid = guid;
		this.index = index;
		this.file = file;
	}

	public EntityDescriptor(eCEntity entity, int index, FileDescriptor file) {
		this(entity.getName(), entity.toString(), entity.getGuid(), index, file);
	}

	public EntityDescriptor(eCEntity entity, FileDescriptor file) {
		this(entity, -1, file);
	}

	@JsonProperty
	public String getName() {
		return name;
	}

	@JsonProperty
	public String getDisplayName() {
		return displayName;
	}

	@JsonProperty
	public String getGuid() {
		return guid;
	}

	public boolean hasIndex() {
		return index != -1;
	}

	@JsonProperty
	public int getIndex() {
		return index;
	}

	@JsonProperty
	public FileDescriptor getFile() {
		return file;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof EntityDescriptor)) {
			return false;
		}

		EntityDescriptor castOther = (EntityDescriptor) other;
		return Objects.equals(name, castOther.name) && Objects.equals(guid, castOther.guid) && Objects.equals(index, castOther.index)
				&& Objects.equals(file, castOther.file);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, guid, index, file);
	}

	public EntityDescriptor withFile(FileDescriptor newFile) {
		return new EntityDescriptor(name, displayName, guid, index, newFile);
	}
}
