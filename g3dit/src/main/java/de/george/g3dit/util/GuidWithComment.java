package de.george.g3dit.util;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GuidWithComment implements Cloneable {
	private String guid;
	private String comment;

	public GuidWithComment(@JsonProperty("guid") String guid, @JsonProperty("comment") String comment) {
		this.guid = guid;
		this.comment = comment;
	}

	@JsonProperty
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	@JsonProperty
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof GuidWithComment castOther)) {
			return false;
		}
		return Objects.equals(guid, castOther.guid);
	}

	@Override
	public int hashCode() {
		return Objects.hash(guid);
	}

	@Override
	public GuidWithComment clone() {
		return new GuidWithComment(guid, comment);
	}

}
