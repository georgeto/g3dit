package de.george.g3dit.config;

import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableSet;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.GuidWithComment;

public class GuidWithCommentConfigFile extends JsonSetConfigFile<GuidWithComment> {
	public GuidWithCommentConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	protected Class<GuidWithComment> type() {
		return GuidWithComment.class;
	}

	public ImmutableSet<GuidWithComment> getClonedContent() {
		return getContent().stream().map(GuidWithComment::clone).collect(ImmutableSet.toImmutableSet());
	}

	public Set<String> getGuids() {
		return getContent().stream().map(GuidWithComment::getGuid).collect(Collectors.toSet());
	}
}
