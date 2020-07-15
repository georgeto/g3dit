package de.george.g3dit.scripts;

import java.awt.Window;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.CacheManager;
import de.george.g3dit.settings.Option;
import de.george.g3dit.util.FileManager;

public interface IScriptEnvironment {
	public EditorContext getEditorContext();

	public CacheManager getCacheManager();

	public FileManager getFileManager();

	public Window getParentWindow();

	public void log(String message);

	public void log(String message, Object... arguments);

	public <T> T getOption(Option<T> option);
}
