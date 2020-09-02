package de.george.g3dit.tab.archive.views;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.swing.JPanel;

import com.teamunify.i18n.I;

import de.george.g3dit.EditorContext;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.TemplateCache;
import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.entitytree.TreeRenderer;
import de.george.g3dit.entitytree.filter.ITreeExtension;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.GuidUtil;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.EntityUtil;
import de.george.navmap.data.NavZone;

public class EntityTreeExtension implements ITreeExtension {
	private EditorContext ctx;

	public EntityTreeExtension(EditorContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void guiInit(JPanel extensionPanel, ActionListener rbActionListener) {}

	@Override
	public boolean filterLeave(eCEntity entity) {
		return true;
	}

	@Override
	public boolean isFilterActive() {
		return false;
	}

	@Override
	public void renderElement(TreeRenderer element, eCEntity entity, ArchiveFile file) {
		List<String> warnings = new ArrayList<>();
		List<String> errors = new ArrayList<>();

		// Überprüfen
		if (EntityUtil.checkOwnerMapping(entity) == 1) {
			warnings.add(I.tr("Owner-Entity ungleich Parent-Entity"));
			// TODO: Add even more checks (CheckManager...)
		}

		TemplateCache cache = Caches.template(ctx);
		if (cache.isValid() && GuidUtil.isValid(entity.getCreator())) {
			Optional<TemplateCacheEntry> entry = cache.getEntryByGuid(entity.getCreator());
			long entityChangeTime = entity.getDataChangedTimeStamp();
			if (entry.isPresent()) {
				long creatorChangeTime = entry.get().getChangeTime();
				if (entityChangeTime != creatorChangeTime) {
					warnings.add(entityChangeTime < creatorChangeTime
							? I.trf("ChangeTime-Wert ({0, number}) kleiner als in referenzierter Template ({1, number})", entityChangeTime,
									creatorChangeTime)
							: I.trf("ChangeTime-Wert ({0, number}) größer als in referenzierter Template ({1, number})", entityChangeTime,
									creatorChangeTime));
				}
			}
		}

		if (entity.hasClass(CD.gCNavZone_PS.class)) {
			NavZone navZone = NavZone.fromArchiveEntity(entity);
			errors.addAll(navZone.generateErrors());
		}

		SwingUtils.generateTooltipErrorList(element, errors, warnings);
	}
}
