package de.george.g3dit.config;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;

import de.george.g3dit.EditorContext;
import de.george.g3dit.settings.SettingsUpdatedEvent;
import de.george.g3dit.util.event.HolderEventList;

public abstract class ReloadableConfigFile<T> {
	private static final Logger logger = LoggerFactory.getLogger(ReloadableConfigFile.class);

	private EditorContext ctx;
	private String path;
	private T content;

	private boolean editLocked;

	private HolderEventList<T> contentChangedListeners = new HolderEventList<>();

	public ReloadableConfigFile(EditorContext ctx, String path) {
		this.ctx = ctx;
		this.path = path;
		ctx.eventBus().register(this);
		reloadContent();
	}

	public T getContent() {
		return content;
	}

	public synchronized void updateContent(T newContent) {
		Optional<File> configFile = ctx.getFileManager().getLocalConfigFile(path);
		if (configFile.isPresent()) {
			try {
				write(newContent, configFile.get());
			} catch (IOException e) {
				logger.warn("Failed to write config file.", e);

			}
		} else {
			logger.warn("Failed to get config file.");
		}
		content = newContent;
		reloadContent();
	}

	@Subscribe
	public void onSettingsUpdated(SettingsUpdatedEvent event) {
		reloadContent();
	}

	private synchronized void reloadContent() {
		try {
			Optional<File> localConfigFile = ctx.getFileManager().getLocalConfigFile(path);
			// No primary data folder set
			if (!localConfigFile.isPresent()) {
				content = read(ctx.getFileManager().getDefaultConfigFile(path));
			} else {
				if (!localConfigFile.get().exists()) {
					File defaultConfigFile = ctx.getFileManager().getDefaultConfigFile(path);
					try {
						localConfigFile.get().getParentFile().mkdirs();
						Files.copy(defaultConfigFile.toPath(), localConfigFile.get().toPath());
					} catch (IOException e) {
						logger.warn("Failed to create local config file.", e);
						content = read(defaultConfigFile);
					}
				}
				content = read(localConfigFile.get());
			}
		} catch (IOException e) {
			logger.warn("Failed to read config file.", e);
			content = getDefaultValue();
		}
		contentChangedListeners.notify(content);
	}

	protected abstract T read(File configFile) throws IOException;

	protected void write(T content, File configFile) throws IOException {
		throw new UnsupportedOperationException();
	}

	protected abstract T getDefaultValue();

	public void addContentChangedListener(Object holder, Consumer<T> callback) {
		contentChangedListeners.addListener(holder, callback);
		callback.accept(content);
	}

	public synchronized boolean isEditLocked() {
		return editLocked;
	}

	public synchronized boolean acquireEditLock() {
		if (this.editLocked) {
			return false;
		}
		this.editLocked = true;
		return true;
	}

	public synchronized void releaseEditLock() {
		this.editLocked = false;
	}
}
