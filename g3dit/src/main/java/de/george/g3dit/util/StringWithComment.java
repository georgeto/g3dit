package de.george.g3dit.util;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

public class StringWithComment implements Cloneable {
	private String value;
	private String comment;

	public StringWithComment(@JsonProperty("value") String value, @JsonProperty("comment") String comment) {
		this.value = value;
		this.comment = comment;
	}

	@JsonProperty
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
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
		if (!(other instanceof StringWithComment castOther)) {
			return false;
		}
		return Objects.equals(value, castOther.value);
	}

	@Override
	public int hashCode() {
		return Objects.hash(value);
	}

	@Override
	public StringWithComment clone() {
		return new StringWithComment(value, comment);
	}
}
