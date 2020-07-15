package de.george.g3dit.config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.type.TypeFactory;
import com.google.common.collect.ImmutableSet;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.GuidWithComment;
import de.george.g3dit.util.JsonUtil;

public class GuidWithCommentConfigFile extends ReloadableConfigFile<ImmutableSet<GuidWithComment>> {
	public GuidWithCommentConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	protected ImmutableSet<GuidWithComment> read(File configFile) throws IOException {
		return ImmutableSet.copyOf(JsonUtil.fieldAutodetectMapper().<List<GuidWithComment>>readValue(configFile,
				TypeFactory.defaultInstance().constructCollectionLikeType(ArrayList.class, GuidWithComment.class)));
	}

	@Override
	protected void write(ImmutableSet<GuidWithComment> content, File configFile) throws IOException {
		JsonUtil.fieldAutodetectMapper().writeValue(configFile, content);
	}

	@Override
	protected ImmutableSet<GuidWithComment> getDefaultValue() {
		return ImmutableSet.of();
	}

	public ImmutableSet<GuidWithComment> getClonedContent() {
		return getContent().stream().map(GuidWithComment::clone).collect(ImmutableSet.toImmutableSet());
	}

	public Set<String> getGuids() {
		return getContent().stream().map(GuidWithComment::getGuid).collect(Collectors.toSet());
	}
}
