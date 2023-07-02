package de.george.g3dit.util.event;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.FileFilter;
import java.nio.file.Path;
import java.util.List;

import com.google.common.eventbus.EventBus;

public class FileDropListener extends EventBusProvider implements DropTargetListener {
	private FileFilter[] filters;

	public FileDropListener(FileFilter... filters) {
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
		try {
			Transferable tr = dtde.getTransferable();
			for (DataFlavor flavor : tr.getTransferDataFlavors()) {
				if (flavor.isFlavorJavaFileListType()) {
					@SuppressWarnings("unchecked")
					List<Path> files = (List<Path>) tr.getTransferData(flavor);
					for (Path file : files) {
						for (FileFilter filter : filters) {
							if (filter.accept(file.toFile())) {
								dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
								return;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		dtde.rejectDrag();
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {}

	@Override
	@SuppressWarnings("unchecked")
	public void drop(DropTargetDropEvent dtde) {
		try {
			Transferable tr = dtde.getTransferable();
			dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			for (DataFlavor flavor : tr.getTransferDataFlavors()) {
				if (flavor.isFlavorJavaFileListType()) {
					List<Path> files = (List<Path>) tr.getTransferData(flavor);
					for (Path file : files) {
						for (FileFilter filter : filters) {
							if (filter.accept(file.toFile())) {
								eventBus().post(new FileDropEvent(file));
								break;
							}
						}
					}
					dtde.dropComplete(true);
					return;
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
