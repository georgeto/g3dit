package de.george.g3dit.config;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import com.google.common.collect.ImmutableSet;

import de.george.g3dit.EditorContext;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.util.IOUtils;

public class GuidSetConfigFile extends ReloadableConfigFile<ImmutableSet<String>> {
	public GuidSetConfigFile(EditorContext ctx, String path) {
		super(ctx, path);
	}

	@Override
	protected ImmutableSet<String> read(File configFile) throws IOException {
		return IOUtils.readTextFile(configFile, StandardCharsets.UTF_8).stream().map(GuidUtil::parseGuid).filter(Objects::nonNull)
				.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	protected ImmutableSet<String> getDefaultValue() {
		return ImmutableSet.of();
	}
}
