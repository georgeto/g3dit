package de.george.g3dit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;

import org.jdesktop.swingx.table.ColumnFactory;
import org.jdesktop.swingx.table.TableColumnExt;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.io.Resources;
import com.teamunify.i18n.I;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.impl.beans.BeanTableFormat;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import de.george.g3dit.cache.Caches;
import de.george.g3dit.cache.NavCache;
import de.george.g3dit.check.EntityDescriptor;
import de.george.g3dit.check.FileDescriptor;
import de.george.g3dit.check.FileDescriptor.FileType;
import de.george.g3dit.gui.dialogs.DisplayHtmlDialog;
import de.george.g3dit.gui.dialogs.EntitySearchDialog;
import de.george.g3dit.gui.map.MapAndTableComponent;
import de.george.g3dit.gui.map.MapItem;
import de.george.g3dit.gui.map.NavObjectOverlay;
import de.george.g3dit.gui.map.PlayerPositionOverlay;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.structure.bCVector2;
import de.george.lrentnode.archive.eCEntity;
import de.george.navmap.data.NavPath;
import de.george.navmap.data.NavZone;
import de.george.navmap.data.NegCircle;
import de.george.navmap.data.NegZone;
import de.george.navmap.data.PrefPath;
import de.george.navmap.sections.NavMap;
import de.george.navmap.util.NavCalc;
import hu.kazocsaba.imageviewer.ImageMouseClickAdapter;
import hu.kazocsaba.imageviewer.ImageMouseEvent;
import net.miginfocom.swing.MigLayout;

public class EntityMap extends JFrame {
	private EditorContext ctx;
	private MapAndTableComponent<EntityMapItem> map;
	private JCheckBox cbShowPlayerPosition;

	private static EntityMap instance;

	public static EntityMap getInstance(EditorContext editorContext) {
		if (instance == null) {
			instance = new EntityMap(editorContext);
		}

		return instance;
	}

	public EntityMap(EditorContext ctx) {
		this.ctx = ctx;
		setTitle(I.tr("Entity Map"));
		setIconImage(SwingUtils.getG3Icon());
		setSize(SwingUtils.getScreenWorkingWidth(), SwingUtils.getScreenWorkingHeight());
		setResizable(true);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
		SwingUtils.addKeyStroke(getRootPane(), JComponent.WHEN_IN_FOCUSED_WINDOW, "Hide Map",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), this::dispose);
		createContentPanel();
	}

	public void addEntity(eCEntity entity, FileDescriptor file) {
		addEntity(new EntityDescriptor(entity, file), entity.getWorldPosition());
	}

	public void addEntity(EntityDescriptor entity, bCVector worldPosition) {
		if (!containsEntity(entity, worldPosition)) {
			map.addItem(new EntityMapItem(entity, worldPosition));
		}
		setVisible(true);
	}

	private static final FileDescriptor XNAV_FILE_DESC = new FileDescriptor(new File("NavigationMap.xnav"), FileType.Other);

	private String getNameFromCache(String baseName, String guid) {
		String name = Caches.entity(ctx).getDisplayName(guid);
		if (name != null) {
			return "[" + baseName + "] " + name;
		} else {
			return baseName;
		}
	}

	private Optional<FileDescriptor> getFileFromCache(String guid) {
		return Caches.entity(ctx).getFile(guid);
	}

	public void addNegCircle(NegCircle negCircle) {
		String name = getNameFromCache("NegCircle", negCircle.getCircleGuid());
		addEntity(new EntityDescriptor(name, name, negCircle.circleGuid, -1,
				getFileFromCache(negCircle.getCircleGuid()).orElse(XNAV_FILE_DESC)), negCircle.getCenter());
	}

	public void addNegZone(NegZone negZone) {
		addEntity(new EntityDescriptor("NegZone", "NegZone", negZone.getGuid(), -1, XNAV_FILE_DESC), negZone.getWorldRadiusOffset());
	}

	public void addNavZone(NavZone navZone) {
		String name = getNameFromCache("NavZone", navZone.getGuid());
		addEntity(new EntityDescriptor(name, name, navZone.getGuid(), -1, getFileFromCache(navZone.getGuid()).orElse(XNAV_FILE_DESC)),
				navZone.getWorldRadiusOffset());
	}

	public void addNavPath(NavPath navPath) {
		String name = getNameFromCache("NavPath", navPath.getGuid());
		addEntity(new EntityDescriptor(name, name, navPath.getGuid(), -1, getFileFromCache(navPath.getGuid()).orElse(XNAV_FILE_DESC)),
				navPath.getWorldPoints().get(0));
	}

	public void addPrefPath(PrefPath prefPath) {
		addEntity(new EntityDescriptor("PrefPath", "PrefPath", prefPath.getVirtualGuid(), -1, XNAV_FILE_DESC), prefPath.getRadiusOffset());
	}

	public void showArea(bCBox area) {
		map.getMap().showArea(area);
	}

	public boolean containsEntity(EntityDescriptor entity, bCVector worldPosition) {
		return map.getItems().stream()
				.anyMatch(m -> m.getDescriptor().getGuid().equals(entity.getGuid()) && m.getPosition().equals(worldPosition));
	}

	private TextFilterator<EntityMapItem> getTextFilterator() {
		return GlazedLists.textFilterator(EntityMapItem.class, EntityMapItem.FIELDS.toArray(new String[0]));
	}

	private void createContentPanel() {
		UndoableTextField tfSearch = SwingUtils.createUndoTF();

		TextComponentMatcherEditor<EntityMapItem> filteredEntitiesMatcherEditor = new TextComponentMatcherEditor<>(tfSearch,
				getTextFilterator());

		map = new MapAndTableComponent<>(new EntityMapItemColumnFactory(), new EntityMapItemTableFormat(), filteredEntitiesMatcherEditor);
		map.setCallbackGoto(this::onGoto);
		map.setCallbackNavigate(this::onNavigate);
		map.getMap().addOverlay(
				new PlayerPositionOverlay<>(map.getMap().getModel(), () -> isShowing() && cbShowPlayerPosition.isSelected()), 1);

		map.getMap().addOverlay(new NavObjectOverlay<>(map.getMap().getModel(), ctx), 1);
		map.getMap().addMenuItem(I.tr("Search for NavObject"), point -> {
			// TODO: Search in area?
			NavCache navCache = Caches.nav(ctx);
			for (NavZone navZone : navCache.getZones()) {
				if (NavCalc.isInZoneRadius(navZone, point) && navZone.test3DPointInZone(point)) {
					addNavZone(navZone);
				}
			}

			for (NavPath navPath : navCache.getPaths()) {
				if (navPath.test3DPointOnPath(point)) {
					addNavPath(navPath);
				}
			}

			NavMap navMap = ctx.getNavMapManager().getNavMap(false);
			for (NegZone negZone : navMap.getNegZones()) {
				if (NavCalc.isInZoneRadius(negZone, point) && negZone.test3DPointInZone(point)) {
					addNegZone(negZone);
				}
			}
		});

		map.getMap().addMenuItem(I.tr("Search connected NavObjects"), point -> {
			EntityMapItem entity = map.getMap().getModel().getNearest(point, 20);
			if (entity == null) {
				return;
			}

			NavCache navCache = Caches.nav(ctx);
			NavMap navMap = ctx.getNavMapManager().getNavMap(true);

			NavZone navZone = navCache.getZoneByGuid(entity.getGuid());
			if (navZone != null) {
				navMap.getNegZonesForZone(navZone.getGuid()).forEach(this::addNegZone);
				navMap.getPrefPathsForZone(navZone.getGuid()).forEach(this::addPrefPath);
				navCache.getPaths().stream().filter(p -> navZone.getGuid().equals(p.zoneAGuid) || navZone.getGuid().equals(p.zoneBGuid))
						.forEach(this::addNavPath);
				// Dont add NegCircles, to much spam...
				return;
			}

			Optional<NegZone> negZone = navMap.getNegZone(entity.getGuid());
			if (negZone.isPresent()) {
				navCache.getZone(negZone.get().getZoneGuid()).ifPresent(this::addNavZone);
				return;
			}

			Optional<NegCircle> negCircle = navMap.getNegCircle(entity.getGuid());
			if (negCircle.isPresent()) {
				negCircle.get().zoneGuids.stream().map(navCache::getZone).forEach(o -> o.ifPresent(this::addNavZone));
				negCircle.get().zoneGuids.stream().map(navCache::getPath).forEach(o -> o.ifPresent(this::addNavPath));
				return;
			}

			NavPath path = navCache.getPathByGuid(entity.getGuid());
			if (path != null) {
				navCache.getZone(path.zoneAGuid).ifPresent(this::addNavZone);
				navCache.getZone(path.zoneBGuid).ifPresent(this::addNavZone);
				return;
			}

			Optional<PrefPath> prefPath = navMap.getPrefPath(entity.getGuid());
			if (prefPath.isPresent()) {
				navCache.getZone(prefPath.get().getZoneGuid()).ifPresent(this::addNavZone);
				return;
			}
		});

		map.getMap().addMouseListener(new ImageMouseClickAdapter() {
			@Override
			public void mouseClicked(ImageMouseEvent e) {
				if (e.getOriginalEvent().getButton() == MouseEvent.BUTTON1
						&& (e.getOriginalEvent().getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) == InputEvent.SHIFT_DOWN_MASK
						&& (e.getOriginalEvent().getModifiersEx() & InputEvent.CTRL_DOWN_MASK) == InputEvent.CTRL_DOWN_MASK) {
					bCVector position = map.getMap().getModel().pixelsToPosition(new bCVector2(e.getX(), e.getY()));
					EntitySearchDialog.openEntitySearchPosition(ctx, position, 2500, true);
				}
			}
		});

		SwingUtils.addKeyStroke(map.getTable(), JComponent.WHEN_IN_FOCUSED_WINDOW, "Remove Selected Entities",
				KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), () -> map.removeItems(map.getItemsSelectedInList()));

		JButton btnHelp = new JButton(I.tr("Help"));
		btnHelp.addActionListener(a -> {
			try {
				new DisplayHtmlDialog(I.tr("Entity Map Help"),
						Resources.toString(Resources.getResource(EntityMap.class, "/res/ChestEditorHelp.html"), StandardCharsets.UTF_8),
						EntityMap.this, 700, 700, false).setVisible(true);
			} catch (IOException e) {
				TaskDialogs.showException(e);
			}
		});

		cbShowPlayerPosition = new JCheckBox(I.tr("Show live position of the hero"), true);

		JPanel tablePanel = new JPanel(new MigLayout("fill", "[][]", "[][grow][]"));
		tablePanel.add(tfSearch, "grow, wrap");
		tablePanel.add(new JScrollPane(map.getTable()), "spanx, grow, wrap");
		tablePanel.add(map.getCbOnlySelected());
		tablePanel.add(cbShowPlayerPosition);
		tablePanel.add(btnHelp, "align right, wrap");

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(tablePanel);
		splitPane.setRightComponent(map.getMap().getViewer().getComponent());
		splitPane.getLeftComponent().setMinimumSize(new Dimension(0, 0));
		splitPane.getRightComponent().setMinimumSize(new Dimension(0, 0));
		add(splitPane, BorderLayout.CENTER);
		splitPane.setResizeWeight(0.5);
		splitPane.setDividerLocation(getWidth() / 2);
	}

	private void onGoto(EntityMapItem item) {
		IpcUtil.gotoPosition(item.getPosition());
	}

	private void onNavigate(EntityMapItem entity) {
		ctx.getEditor().openEntity(entity.descriptor);
	}

	private static class EntityMapItemTableFormat extends BeanTableFormat<EntityMapItem> {
		public EntityMapItemTableFormat() {
			super(EntityMapItem.class, Iterables.toArray(EntityMapItem.FIELDS, String.class),
					Iterables.toArray(EntityMapItem.FIELD_NAMES, String.class));
		}
	}

	private static class EntityMapItemColumnFactory extends ColumnFactory {
		@Override
		public void configureTableColumn(TableModel model, TableColumnExt columnExt) {
			super.configureTableColumn(model, columnExt);

			int fieldIndex = EntityMapItem.FIELD_NAMES.indexOf(columnExt.getTitle());
			if (fieldIndex != -1) {
				switch (EntityMapItem.FIELDS.get(fieldIndex)) {
					case "Title" -> columnExt.setPrototypeValue("G3_Object_Bookshelf_Scrolls_01");
				}
			}
		}
	}

	public static class EntityMapItem implements MapItem, Comparable<EntityMapItem> {
		public static final ImmutableMap<String, String> FIELD_MAPPING = ImmutableMap.of("Title", I.tr("Name"), "File", I.tr("File"),
				"Guid", I.tr("Guid"));
		public static final ImmutableList<String> FIELDS = FIELD_MAPPING.keySet().asList();
		public static final ImmutableList<String> FIELD_NAMES = FIELD_MAPPING.values().asList();

		private final EntityDescriptor descriptor;
		private final bCVector position;

		public EntityMapItem(EntityDescriptor descriptor, bCVector position) {
			this.descriptor = descriptor;
			this.position = position.clone();
		}

		public EntityDescriptor getDescriptor() {
			return descriptor;
		}

		@Override
		public bCVector getPosition() {
			return position;
		}

		@Override
		public String getTitle() {
			return descriptor.getDisplayName();
		}

		public String getGuid() {
			return descriptor.getGuid();
		}

		public String getFile() {
			return descriptor.getFile().getPath().getName();
		}

		@Override
		public int compareTo(EntityMapItem o) {
			return descriptor.getDisplayName().compareTo(o.descriptor.getDisplayName());
		}
	}
}
