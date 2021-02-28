package de.george.g3dit.tab.template;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import com.ezware.dialog.task.TaskDialogs;
import com.jidesoft.plaf.UIDefaultsLookup;
import com.jidesoft.swing.PartialGradientLineBorder;
import com.jidesoft.swing.PartialSide;

import de.danielbechler.diff.node.DiffNode;
import de.danielbechler.diff.node.ToMapPrintingVisitor;
import de.george.g3dit.gui.dialogs.DisplayTextDialog;
import de.george.g3dit.gui.dialogs.SelectClassDialog;
import de.george.g3dit.jme.EntityViewer;
import de.george.g3dit.tab.template.views.PropertyView;
import de.george.g3dit.tab.template.views.TemplateHeaderView;
import de.george.g3dit.tab.template.views.TemplateView;
import de.george.g3dit.util.AssetResolver;
import de.george.g3dit.util.Icons;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.classes.G3Class;
import de.george.lrentnode.diff.EntityDiffer;
import de.george.lrentnode.template.TemplateFile;
import de.george.lrentnode.util.ClassUtil;
import de.george.lrentnode.util.FileUtil;

public class TemplateContentPane extends JPanel {
	private EditorTemplateTab ctx;

	public static enum TemplateViewType {
		ENTITY,
		PROPERTY;
	}

	private Map<TemplateViewType, TemplateView> views = new HashMap<>();
	private TemplateView curView;

	private JToolBar toolBar;

	public TemplateContentPane(EditorTemplateTab ctx) {
		this.ctx = ctx;
	}

	public void initGUI() {
		setLayout(new BorderLayout());

		createToolbar();

		views.put(TemplateViewType.ENTITY, new TemplateHeaderView(ctx));
		views.put(TemplateViewType.PROPERTY, new PropertyView(ctx));

		showView(TemplateViewType.ENTITY);

		setVisible(true);
	}

	private void createToolbar() {
		toolBar = new JToolBar(SwingConstants.HORIZONTAL);
		toolBar.setFloatable(false);
		toolBar.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 3, 0), new PartialGradientLineBorder(
						new Color[] {new Color(0, 0, 128), UIDefaultsLookup.getColor("control")}, 2, PartialSide.SOUTH)),
				BorderFactory.createEmptyBorder(0, 0, 2, 0)));

		JButton btnAddClasses = new JButton(Icons.getImageIcon(Icons.Data.CLASS_PLUS));
		btnAddClasses.setToolTipText("Klassen hinzufügen");
		toolBar.add(btnAddClasses);
		btnAddClasses.addActionListener(e -> {
			Optional<TemplateFile> selectedTple = ctx.getFileManager().selectAndOpenTemplate();
			if (selectedTple.isPresent()) {
				eCEntity classContainer = selectedTple.get().getReferenceHeader();
				SelectClassDialog classSelect = new SelectClassDialog(ctx.getParentWindow(), "Klassen hinzufügen", "Hinzufügen",
						classContainer);
				if (classSelect.openAndWasSuccessful()) {
					TemplateFile tple = ctx.getCurrentTemplate();
					for (G3Class clazz : classSelect.getResultClasses()) {
						tple.getReferenceHeader().addClass(ClassUtil.clone(clazz), classContainer.getClassVersion(clazz));
					}
					ctx.refreshView();
				}
			}
		});

		JButton btnRemClasses = new JButton(Icons.getImageIcon(Icons.Data.CLASS_MINUS));
		btnRemClasses.setToolTipText("Klassen entfernen");
		toolBar.add(btnRemClasses);
		btnRemClasses.addActionListener(e -> {
			TemplateFile tple = ctx.getCurrentTemplate();
			SelectClassDialog dialog = new SelectClassDialog(ctx.getParentWindow(), "Klassen entfernen", "Entfernen",
					tple.getReferenceHeader());

			if (dialog.openAndWasSuccessful()) {
				for (G3Class clazz : dialog.getResultClasses()) {
					tple.getReferenceHeader().removeClass(clazz.getClassName());
				}
				ctx.refreshView();
			}
		});
		toolBar.addSeparator();
		JButton btn3dView = new JButton("3D-Ansicht", Icons.getImageIcon(Icons.Data.THREED));
		btn3dView.setFocusable(false);
		toolBar.add(btn3dView);
		btn3dView.addActionListener(e -> {
			EntityViewer.getInstance(ctx).showContainer(ctx.getCurrentTemplate().getReferenceHeader());
		});
		JButton btnAssetInfo = new JButton("Asset-Info", Icons.getImageIcon(Icons.Data.INFORMATION));
		btnAssetInfo.setToolTipText("Ermittelt die von der Template verwendeten Assets und kopiert sie in die Zwischenablage.");
		btnAssetInfo.setFocusable(false);
		toolBar.add(btnAssetInfo);
		btnAssetInfo.addActionListener(e -> {
			eCEntity template = ctx.getCurrentTemplate().getReferenceHeader();
			AssetResolver resolver = AssetResolver.with(ctx).build();
			new DisplayTextDialog("Asset-Info", resolver.resolveContainer(template).print(), ctx.getParentWindow(), false).open();
		});
		toolBar.addSeparator();

		JButton btnProcessTemplateContext = new JButton("TemplateContext prüfen", Icons.getImageIcon(Icons.Select.TICK));
		btnProcessTemplateContext
				.setToolTipText("Überprüft ob die Template in einem TemplateContext eingetragen ist und bietet Korrekturmaßnahmen an.");
		btnProcessTemplateContext.setFocusable(false);
		toolBar.add(btnProcessTemplateContext);
		btnProcessTemplateContext.addActionListener(a -> ctx.checkTemplateContext());

		toolBar.addSeparator();
		JButton btnDiff = new JButton("Mit Originaldaten vergleichen", Icons.getImageIcon(Icons.Action.DIFF));
		btnDiff.setToolTipText("Vergleicht Template mit Version aus den Originaldaten.");
		btnDiff.setFocusable(false);
		toolBar.add(btnDiff);
		btnDiff.addActionListener(a -> {
			try {
				Optional<File> originalFile = ctx.getFileManager().moveFromPrimaryToSecondary(ctx.getDataFile().get());
				if (!originalFile.isPresent() || !originalFile.get().isFile()) {
					TaskDialogs.error(ctx.getParentWindow(), "",
							"Es gibt keine Version des Templates in den Originaldaten bzw. das Template befindet sich selbst in den Originaldaten.");
					return;
				}

				TemplateFile tple = FileUtil.openTemplate(originalFile.get());
				DiffNode diff = new EntityDiffer(true).diff(ctx.getCurrentTemplate().getGraph(), tple.getGraph());
				ToMapPrintingVisitor mapPrintingVisitor = new ToMapPrintingVisitor(ctx.getCurrentTemplate().getGraph(), tple.getGraph());
				diff.visit(mapPrintingVisitor);
				DisplayTextDialog dialog = new DisplayTextDialog("Vergleich: Template - Original-Template",
						mapPrintingVisitor.getMessagesAsString(), ctx.getParentWindow(), true);
				dialog.setVisible(true);
			} catch (IOException e) {
				TaskDialogs.showException(e);
			}
		});
	}

	/**
	 * Schaltet auf das angegebene View um.
	 *
	 * @param viewType Typ des Views
	 */
	public void showView(TemplateViewType viewType) {
		// Änderungen vor dem Wechseln auf ein anderes View speichern
		if (curView != null) {
			saveView();
		}

		// View wechseln
		curView = views.get(viewType);
		loadView();

		// GUI aktualisieren
		removeAll();
		// JToolBar toolBar = curView.getToolBar();
		if (toolBar == null) {
			add(curView.getContent(), BorderLayout.CENTER);
		} else {
			JPanel container = new JPanel(new BorderLayout());
			container.add(toolBar, BorderLayout.NORTH);
			container.add(curView.getContent(), BorderLayout.CENTER);
			add(container, BorderLayout.CENTER);
		}
		validate();
		repaint(50L);
	}

	public void loadView() {
		curView.load(ctx.getCurrentTemplate());
	}

	public void saveView() {
		curView.save(ctx.getCurrentTemplate());
	}

	public void refreshView() {
		saveView();
		loadView();
	}

	public void onClose() {
		for (TemplateView view : views.values()) {
			view.cleanUp();
		}
	}
}
