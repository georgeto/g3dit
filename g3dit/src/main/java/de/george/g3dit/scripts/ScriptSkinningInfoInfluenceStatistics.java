package de.george.g3dit.scripts;

import java.io.File;
import java.util.List;

import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;
import com.teamunify.i18n.I;

import de.george.lrentnode.archive.animation.Chunks.LMA_CHUNK;
import de.george.lrentnode.archive.animation.Chunks.SkinningInfoChunk;
import de.george.lrentnode.archive.animation.eCResourceAnimationActor_PS;
import de.george.lrentnode.util.FileUtil;
import one.util.streamex.StreamEx;

public class ScriptSkinningInfoInfluenceStatistics implements IScript {

	@Override
	public String getTitle() {
		return I.tr("Determine distribution of SkinningInfo influence frequency");
	}

	@Override
	public String getDescription() {
		return I.tr("Determine distribution of SkinningInfo influence frequency.");
	}

	@Override
	public boolean execute(IScriptEnvironment env) {
		Multiset<Integer> result = TreeMultiset.create();
		for (File file : env.getFileManager().listAnimatedMeshes()) {
			try {
				eCResourceAnimationActor_PS resAnimActor = FileUtil.openAnimationActor(file);
				StreamEx.of(resAnimActor.actor).append(resAnimActor.lods.stream())
						.flatMap(a -> a.<SkinningInfoChunk>getChunksByType(LMA_CHUNK.LMA_CHUNK_SKINNINGINFO).stream())
						.flatMap(c -> c.influences.stream()).map(List::size).forEach(result::add);
			} catch (Exception e) {
				env.log(I.trf("Error while loading {0}.", file.getName()));
			}
		}
		result.elementSet().forEach(k -> env.log(k + ": " + result.count(k)));
		return true;
	}
}
