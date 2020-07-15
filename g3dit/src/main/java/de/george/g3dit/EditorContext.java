package de.george.g3dit;

import java.awt.Window;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningExecutorService;

import de.george.g3dit.cache.CacheManager;
import de.george.g3dit.settings.OptionStore;
import de.george.g3dit.settings.SettingsUpdatedEvent;
import de.george.g3dit.tab.EditorTab;
import de.george.g3dit.util.FileManager;
import de.george.g3dit.util.NavMapManager;
import de.george.g3dit.util.event.IEventBusProvider;

public interface EditorContext extends IEventBusProvider {

	/**
	 * Feuert folgende Events
	 * <li>{@link EditorTab.OpenedEvent}
	 * <li>{@link EditorTab.ClosedEvent}
	 * <li>{@link EditorTab.SelectedEvent}
	 * <li>{@link EditorTab.StateChangedEvent}
	 * <li>{@link SettingsUpdatedEvent}
	 *
	 * @return
	 */
	@Override
	public EventBus eventBus();

	public OptionStore getOptionStore();

	public FileManager getFileManager();

	public NavMapManager getNavMapManager();

	public CacheManager getCacheManager();

	public Editor getEditor();

	public Window getParentWindow();

	public ListeningExecutorService getExecutorService();

	public IpcMonitor getIpcMonitor();

	public void runGC();
}
