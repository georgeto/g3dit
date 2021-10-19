package de.george.g3utils.structure;

public class Guid {
	private String guid;

	public Guid(String guid) {
		this.guid = GuidUtil.parseGuid(guid);
	}

	public boolean isValid() {
		return guid != null;
	}

	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = GuidUtil.parseGuid(guid);
	}

	public String toGroup() {
		return GuidUtil.hexToGroup(guid);
	}

	public String toPlain() {
		return GuidUtil.hexToPlain(guid);
	}

	public static Guid randomGuid() {
		return new Guid(GuidUtil.randomGUID());
	}

	@Override
	public int hashCode() {
		return guid == null ? 0 : guid.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!isValid()) {
			return false;
		}

		if (obj instanceof String) {
			return getGuid().equals(GuidUtil.parseGuid((String) obj));
		}

		if (obj instanceof Guid other) {
			return other.isValid() && getGuid().equals(other.getGuid());
		}

		return false;
	}

}
