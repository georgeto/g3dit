package de.george.g3dit.cache;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.util.AbstractDialogFileWorker;
import de.george.g3utils.structure.bCVector;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.classes.eCIlluminated_PS;
import de.george.lrentnode.classes.eCIlluminated_PS.StaticLight;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.structures.bCFloatColor;
import de.george.lrentnode.util.EntityUtil;

public class LightCache extends AbstractCache<LightCache> {
	private static final Logger logger = LoggerFactory.getLogger(LightCache.class);

	private Set<LightSource> lights;
	private EditorContext ctx;

	public LightCache(EditorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public boolean isValid() {
		return lights != null;
	}

	public Set<LightSource> getEntries() {
		return lights;
	}

	@Override
	public void create() throws Exception {
		CreateLightCacheWorker worker = new CreateLightCacheWorker(ctx.getFileManager().worldFilesCallable());
		worker.executeAndShowDialog();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void load(final File inFile) {
		LoadObjectWorker worker = new LoadObjectWorker(inFile) {
			@Override
			protected void done() {
				try {
					Object[] data = loadIntern(get());
					lights = (Set<LightSource>) data[1];
				} catch (Exception e) {
					logger.info("Failed to load LightCache: {}", e.getMessage());
				} finally {
					notifyCacheUpdated();
				}
			}
		};
		worker.execute();
	}

	@Override
	public void save(File file) throws IOException {
		saveIntern(file, lights);
	}

	private class CreateLightCacheWorker extends AbstractDialogFileWorker<Set<LightSource>> {

		public CreateLightCacheWorker(Callable<List<File>> fileProvider) {
			super(fileProvider, null, I.tr("Erstelle LightCache"), ctx.getParentWindow());
			statusFormat = I.tr("{0, number} Lichtquellen gefunden");
		}

		@Override
		protected Set<LightSource> doInBackground() throws Exception {
			List<File> files = getFiles();

			Map<bCVector, LightSource> lightsMap = new HashMap<>();
			// Load LightSources
			ArchiveFileIterator iter = new ArchiveFileIterator(files);
			while (iter.hasNext() && !isCancelled()) {
				ArchiveFile aFile = iter.next();
				for (eCEntity entity : aFile.getEntities()) {
					G3Class clazz = entity.getClass(CD.eCStaticPointLight_PS.class);
					if (clazz != null) {
						bCVector offset = clazz.property(CD.eCStaticPointLight_PS.Offset);
						bCVector pos = offset.getTransformed(entity.getWorldMatrix());
						LightSource light = new LightSource();
						light.position = pos;
						light.range = clazz.property(CD.eCStaticPointLight_PS.Range).getFloat();
						light.castShadows = clazz.property(CD.eCStaticPointLight_PS.CastShadows).isBool();
						light.color = clazz.property(CD.eCStaticPointLight_PS.Color);
						light.name = entity.getName();
						if (Strings.isNullOrEmpty(light.name)) {
							light.name = EntityUtil.getMesh(entity).orElse(null);
						}
						lightsMap.put(light.position, light);
						publish(lightsMap.size());
					}
				}
				filesDone.incrementAndGet();
				publish(lightsMap.size());
			}

			statusFormat = I.trf("{0, number} Lichtquellen gefunden. Sammle weitere Daten...", lightsMap.size());

			// Load Intensities
			filesDone.set(0);
			iter = new ArchiveFileIterator(files);
			while (iter.hasNext() && !isCancelled()) {
				ArchiveFile aFile = iter.next();
				for (eCEntity entity : aFile.getEntities()) {
					if (entity.hasClass(CD.eCIlluminated_PS.class)) {
						eCIlluminated_PS illum = entity.getClass(CD.eCIlluminated_PS.class);
						for (StaticLight light : illum.lights.getLights()) {
							for (LightSource lightSource : lightsMap.values()) {
								if (lightSource.position.simliar(light.position, 1f)) {
									if (lightSource.intensity == null) {
										lightSource.intensity = light.intensity;
									}
									break;
								}
							}
						}
					}
				}
				filesDone.incrementAndGet();
				publish(lightsMap.size());
			}
			return new HashSet<>(lightsMap.values());
		}

		@Override
		protected void done() {
			try {
				if (!isCancelled()) {
					lights = get();
					generateCreationTimestamp();
				}

				progDlg.dispose();
			} catch (Exception ex) {
				progDlg.dispose();
				TaskDialogs.showException(ex);
			} finally {
				notifyCacheUpdated();
			}
		}
	}

	public static class LightSource implements Serializable {
		public bCFloatColor color;
		public float range;
		public boolean castShadows;
		public String name;
		public String intensity;
		public bCVector position;
	}
}
