package de.george.g3dit.config;

import com.google.common.collect.ImmutableSet;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.StringWithComment;

public class StringWithCommentConfigFile extends JsonSetConfigFile<StringWithComment> {
	public StringWithCommentConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	public Class<StringWithComment> type() {
		return StringWithComment.class;
	}

	public ImmutableSet<StringWithComment> getClonedContent() {
		return getContent().stream().map(StringWithComment::clone).collect(ImmutableSet.toImmutableSet());
	}

	public ImmutableSet<String> getValues() {
		return getContent().stream().map(StringWithComment::getValue).collect(ImmutableSet.toImmutableSet());
	}
}
