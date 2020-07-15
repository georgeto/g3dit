package de.george.g3dit.tab.archive.views;

import java.util.List;

import javax.swing.event.TreeSelectionEvent;

import de.george.g3dit.entitytree.filter.ITreeExtension;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.lrentnode.archive.eCEntity;

public abstract class SingleEntityArchiveView implements ArchiveView {
	protected EditorArchiveTab ctx;

	/**
	 * Wenn sich beim Wechsel auf diesen View die ausgeählte Entity ändert, etwa aufgrund eines
	 * speziellen {@link ITreeExtension}-Filters, hat diese zur Folge, dass die
	 * {@link PropertyView#save(eCEntity)}-Methode dieses Views für die zuvor ausgewählte Entity
	 * ausgeführt wird. Potenziell veraltete, noch im State dieses Views gespeicherte Daten, werden
	 * dann auf die Entity angewendet.
	 */
	private boolean responsibleForCurrentEntity = false;

	public SingleEntityArchiveView(EditorArchiveTab ctx) {
		this.ctx = ctx;
	}

	@Override
	public final void load(eCEntity entity) {
		responsibleForCurrentEntity = true;
		if (entity != null) {
			loadInternal(entity);
		}
	}

	protected abstract void loadInternal(eCEntity entity);

	@Override
	public void entitySelectionChanged(TreeSelectionEvent e) {
		List<eCEntity> selectedEntities = ctx.getEntityTree().getSelectedEntities();

		if (responsibleForCurrentEntity) {
			save(ctx.getCurrentEntity());
		}

		if (selectedEntities.size() == 1) {
			ctx.changeEntity(selectedEntities.get(0));
			load(ctx.getCurrentEntity());
		}
	}

	@Override
	public void onLeave() {
		responsibleForCurrentEntity = false;
	}
}
