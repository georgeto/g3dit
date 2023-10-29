package de.george.g3dit.cache;

import java.io.IOException;
import java.nio.file.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.util.concurrent.FutureCallback;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.ConcurrencyUtil;
import de.george.g3dit.util.FileManager;
import de.george.g3utils.util.FilesEx;
import de.george.g3utils.util.Pair;
import de.george.lrentnode.util.EntityUtil;

public class LowPolyMeshCache extends AbstractCache<LowPolyMeshCache> {
	private static final Logger logger = LoggerFactory.getLogger(LowPolyMeshCache.class);

	private ImmutableSet<String> lowPolyMeshes;
	private ImmutableSet<String> lowPolyMeshesLowerCase;

	private EditorContext ctx;

	public LowPolyMeshCache(EditorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean isValid() {
		return lowPolyMeshes != null;
	}

	public ImmutableSet<String> getLowPolyMeshes() {
		return lowPolyMeshes;
	}

	public boolean hasLowPolyMesh(String meshName) {
		return EntityUtil.toLowPolyMesh(meshName).map(String::toLowerCase).map(lowPolyMeshesLowerCase::contains).orElse(false);
	}

	@Override
	public void create() throws Exception {
		ConcurrencyUtil.executeAndInvokeLater(() -> {
			ImmutableSet<String> lowPolyMeshes = ctx.getFileManager()
					.listFiles(FileManager.RP_COMPILED_MESH, f -> FilesEx.getFileNameLowerCase(f).endsWith("_lowpoly.xcmsh")).stream()
					.map(FilesEx::getFileName).collect(ImmutableSet.toImmutableSet());

			ImmutableSet<String> lowPolyMeshesLowerCase = lowPolyMeshes.stream().map(String::toLowerCase)
					.collect(ImmutableSet.toImmutableSet());

			return Pair.of(lowPolyMeshes, lowPolyMeshesLowerCase);
		}, new FutureCallback<>() {

			@Override
			public void onSuccess(Pair<ImmutableSet<String>, ImmutableSet<String>> result) {
				lowPolyMeshes = result != null ? result.el0() : null;
				lowPolyMeshesLowerCase = result != null ? result.el1() : null;
				notifyCacheUpdated();
			}

			@Override
			public void onFailure(Throwable t) {
				logger.warn("Failed to create low poly mesh cache.", t);
			}
		}, ctx.getExecutorService());
	}

	@Override
	public void load(Path file) {
		try {
			create();
		} catch (Exception e) {
			logger.warn("Failed to create low poly mesh cache.", e);
		}
	}

	@Override
	public void save(Path file) throws IOException {}
}
