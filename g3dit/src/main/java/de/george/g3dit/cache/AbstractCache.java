package de.george.g3dit.cache;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.function.Consumer;

import de.george.g3dit.util.event.HolderEventList;
import de.george.g3utils.util.IOUtils;

public abstract class AbstractCache<T extends AbstractCache<T>> implements Serializable {

	private HolderEventList<T> listeners = new HolderEventList<>();

	private boolean changed = false;

	protected long creationTimestamp = -1;

	public abstract boolean isValid();

	public abstract void create() throws Exception;

	public void load() {
		load(new File(getSavePath()));
	}

	public abstract void load(File file);

	public abstract void save(File file) throws IOException;

	public final void addUpdateListener(Object holder, Consumer<T> listener) {
		listeners.addListener(holder, listener);
	}

	public final void removeUpdateListener(Object holder, Consumer<T> listener) {
		listeners.removeListener(holder, listener);
	}

	public final void removeUpdateListeners(Object holder) {
		listeners.removeListeners(holder);
	}

	@SuppressWarnings("unchecked")
	protected void notifyCacheUpdated() {
		listeners.notify((T) this);
	}

	protected Object[] loadIntern(Object object) throws Exception {
		if (object instanceof Object[]) {
			Object[] data = (Object[]) object;
			creationTimestamp = (long) data[0];
			return data;
		} else {
			throw new IOException("Cache-Datei ist ungülitg! Bitte den Cache neu erstellen.");
		}
	}

	protected void saveIntern(File file, Object... objects) throws IOException {
		Object[] objectArray = new Object[1 + objects.length];
		objectArray[0] = getCreationTimestamp();
		for (int i = 1; i <= objects.length; i++) {
			objectArray[i] = objects[i - 1];
		}
		IOUtils.saveObjectsToFile(file, objectArray);
	}

	protected void generateCreationTimestamp() {
		creationTimestamp = System.currentTimeMillis();
	}

	public long getCreationTimestamp() {
		return creationTimestamp;
	}

	public String printCreationTimestamp() {
		Calendar mydate = Calendar.getInstance();
		mydate.setTimeInMillis(getCreationTimestamp());
		return new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date(getCreationTimestamp()));
		// return mydate.get(Calendar.DAY_OF_MONTH) + "." + mydate.get(Calendar.MONTH) + "." +
		// mydate.get(Calendar.YEAR)
		// + " " + mydate.get(Calendar.HOUR_OF_DAY) + ":" + mydate.get(Calendar.MINUTE);
	}

	public String getSavePath() {
		return CacheManager.CACHE_FOLDER + File.separator + this.getClass().getSimpleName() + ".cache";
	}

	protected void markChanged(boolean changed) {
		this.changed = changed;
	}

	protected boolean isChanged() {
		return changed;
	}
}
