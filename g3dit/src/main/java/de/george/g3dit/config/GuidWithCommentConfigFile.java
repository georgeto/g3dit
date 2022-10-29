package de.george.g3dit.config;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.GuidWithComment;

import java.util.Set;
import java.util.stream.Collectors;

public class GuidWithCommentConfigFile extends JsonSetConfigFile<GuidWithComment> {
	public GuidWithCommentConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	public Class<GuidWithComment> type() {
		return GuidWithComment.class;
	}

	public Set<String> getGuids() {
		return getContent().stream().map(GuidWithComment::getGuid).collect(Collectors.toSet());
	}
}
