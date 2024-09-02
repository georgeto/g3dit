package de.george.g3dit.util.event;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import com.google.common.eventbus.EventBus;

import de.george.g3utils.util.PathFilter;
import one.util.streamex.StreamEx;

public class FileDropListener extends EventBusProvider implements DropTargetListener {
	private PathFilter[] filters;

	public FileDropListener(PathFilter... filters) {
		this.filters = filters;
	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		acceptDrag(dtde);
	}

	@Override
	public void dragExit(DropTargetEvent dte) {}

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		acceptDrag(dtde);
	}

	private void acceptDrag(DropTargetDragEvent dtde) {
		if (iterTransferredFiles(dtde.getTransferable()).findAny().isPresent())
			dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
		else
			dtde.rejectDrag();
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {}

	@Override
	@SuppressWarnings("unchecked")
	public void drop(DropTargetDropEvent dtde) {
		dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

		boolean dropped = false;
		for (Path file : iterTransferredFiles(dtde.getTransferable())) {
			eventBus().post(new FileDropEvent(file));
			dropped = true;
		}

		if (dropped)
			dtde.dropComplete(true);
		else
			dtde.rejectDrop();
	}

	/**
	 * Feuert folgende Events:
	 * <li>{@link FileDropListener.FileDropEvent}<EditorTab>
	 */
	@Override
	public EventBus eventBus() {
		return super.eventBus();
	}

	private StreamEx<Path> iterTransferredFiles(Transferable transferable) {
		try {
			List<File> files = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
			Predicate<Path> matchesAnyFilter = f -> Arrays.stream(filters).anyMatch(filter -> filter.accept(f));
			return StreamEx.of(files).map(File::toPath).filter(matchesAnyFilter);
		} catch (UnsupportedFlavorException | IOException e) {
			return StreamEx.empty();
		}
	}

	public static class FileDropEvent {
		private Path file;

		public FileDropEvent(Path file) {
			this.file = file;
		}

		public Path getFile() {
			return file;
		}
	}
}
