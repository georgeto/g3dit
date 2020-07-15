package de.george.g3dit.tab;

import java.awt.Window;
import java.util.Optional;

import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningExecutorService;

import de.george.g3dit.Editor;
import de.george.g3dit.EditorContext;
import de.george.g3dit.IpcMonitor;
import de.george.g3dit.StatusBar;
import de.george.g3dit.cache.CacheManager;
import de.george.g3dit.gui.components.tab.ITypedTab;
import de.george.g3dit.settings.OptionStore;
import de.george.g3dit.util.FileManager;
import de.george.g3dit.util.NavMapManager;

public abstract class EditorTab implements ITypedTab, EditorContext {
	public static enum EditorTabType {
		Archive,
		Template,
		Secdat,
		EffectMap,
		NegZone,
		PrefPath,
		NegCircle
	}

	private final EditorTabType tabType;
	protected final EditorContext ctx;

	protected EditorTab(EditorContext ctx, EditorTabType tabType) {
		this.ctx = ctx;
		this.tabType = tabType;
	}

	/**
	 * Wird aufgerufen, wenn versucht wird den Tab zu schließen.
	 *
	 * @param appExit Grund für den Aufruf ist, dass das ganze Programm beendet wird.
	 * @return true, wenn der Tab geschlossen werden kann
	 */
	public abstract boolean onClose(boolean appExit);

	/**
	 * Die 'Speichern' Funktion im Hauptmenü wurde betätigt.
	 */
	public abstract void onSave();

	/**
	 * Die 'Speichern unter' Funktion im Hauptmenü wurde betätigt.
	 */
	public abstract void onSaveAs();

	@Override
	public final CacheManager getCacheManager() {
		return ctx.getCacheManager();
	}

	@Override
	public final FileManager getFileManager() {
		return ctx.getFileManager();
	}

	@Override
	public NavMapManager getNavMapManager() {
		return ctx.getNavMapManager();
	}

	public final StatusBar getStatusBar() {
		return ctx.getEditor().getStatusBar();
	}

	@Override
	public final Window getParentWindow() {
		return ctx.getParentWindow();
	}

	@Override
	public EventBus eventBus() {
		return ctx.eventBus();
	}

	@Override
	public final OptionStore getOptionStore() {
		return ctx.getOptionStore();
	}

	@Override
	public Editor getEditor() {
		return ctx.getEditor();
	}

	@Override
	public ListeningExecutorService getExecutorService() {
		return ctx.getExecutorService();
	}

	@Override
	public IpcMonitor getIpcMonitor() {
		return ctx.getIpcMonitor();
	}

	@Override
	public void runGC() {
		ctx.runGC();
	}

	public abstract String getTitle();

	/**
	 * Titel der im Hauptfenster angezeigt wird, wenn dieser Tab ausgewählt ist.
	 *
	 * @return
	 */
	public String getEditorTitle() {
		return Editor.EDITOR_TITLE;
	}

	public final EditorTabType type() {
		return tabType;
	}

	public static class OpenedEvent {
		private EditorTab tab;

		public OpenedEvent(EditorTab tab) {
			this.tab = tab;
		}

		public EditorTab getTab() {
			return tab;
		}
	}

	public static class ClosedEvent {
		private EditorTab tab;

		public ClosedEvent(EditorTab tab) {
			this.tab = tab;
		}

		public EditorTab getTab() {
			return tab;
		}
	}

	public static class SelectedEvent {
		private Optional<EditorTab> tab;
		private Optional<EditorTab> previousTab;

		public SelectedEvent(Optional<EditorTab> previousTab, Optional<EditorTab> tab) {
			this.previousTab = previousTab;
			this.tab = tab;
		}

		public Optional<EditorTab> getTab() {
			return tab;
		}

		public Optional<EditorTab> getPreviousTab() {
			return previousTab;
		}
	}

	public static class StateChangedEvent {
		private EditorTab tab;

		public StateChangedEvent(EditorTab tab) {
			this.tab = tab;
		}

		public EditorTab getTab() {
			return tab;
		}
	}
}
