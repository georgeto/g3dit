package de.george.g3dit.tab.shared;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.ezware.dialog.task.TaskDialogs;
import com.teamunify.i18n.I;

import de.george.g3dit.gui.dialogs.TemplateNameSearchDialog;
import de.george.g3dit.gui.theme.LayoutUtils;
import de.george.g3dit.tab.EditorTab;
import de.george.g3dit.tab.archive.views.entity.dialogs.BoundingBoxDialog;
import de.george.g3dit.util.FileManager;
import de.george.g3dit.util.Icons;
import de.george.g3utils.structure.bCBox;
import de.george.g3utils.util.IOUtils;
import de.george.lrentnode.classes.eCResourceMeshComplex_PS;
import de.george.lrentnode.classes.desc.CD;
import de.george.lrentnode.util.FileUtil;
import net.miginfocom.swing.MigLayout;

public class BoundingBoxPanel extends JPanel {
	private bCBox boundingBox;

	private JLabel lblMin;
	private JLabel lblMax;

	private EditorTab ctx;
	private Consumer<bCBox> changeBoundingBoxConsumer;
	private Supplier<String> meshNameSupplier;
	private Supplier<String> defaultTemplateNameSupplier;

	public BoundingBoxPanel(String title, EditorTab ctx, Consumer<bCBox> changeBoundingBoxConsumer, Supplier<String> meshNameSupplier,
			Supplier<String> defaultTemplateNameSupplier) {
		this.ctx = ctx;
		this.changeBoundingBoxConsumer = changeBoundingBoxConsumer;
		this.meshNameSupplier = meshNameSupplier;
		this.defaultTemplateNameSupplier = defaultTemplateNameSupplier;

		setLayout(new MigLayout("", "[grow]push[]5[]"));
		setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY, 1, true), title));

		lblMin = new JLabel();
		add(lblMin, "split 2, spany 2, flowy");

		lblMax = new JLabel();
		add(lblMax, "");

		JButton btnEditBoundingBox = new JButton(Icons.getImageIcon(Icons.Action.EDIT));
		btnEditBoundingBox.setToolTipText(I.tr("BoundingBox ändern"));
		add(btnEditBoundingBox, LayoutUtils.sqrBtn());
		btnEditBoundingBox.addActionListener(e -> handleChangeBoundingBox());

		JButton btnCopyBoundingBox = new JButton(Icons.getImageIcon(Icons.Action.COPY));
		btnCopyBoundingBox.setToolTipText(I.tr("BoundingBox in Zwischenablage kopieren"));
		add(btnCopyBoundingBox, LayoutUtils.sqrBtn("wrap"));
		btnCopyBoundingBox.addActionListener(e -> handleCopyBoundingBox());

		JButton btnLoadBoundingBoxTple = new JButton(Icons.getImageIcon(Icons.IO.UPLOAD));
		btnLoadBoundingBoxTple.setToolTipText(I.tr("BoundingBox aus Template laden"));
		add(btnLoadBoundingBoxTple, LayoutUtils.sqrBtn());
		btnLoadBoundingBoxTple.addActionListener(e -> handleLoadBoundingBoxTple());

		JButton btnLoadBoundingBoxMesh = new JButton(Icons.getImageIcon(Icons.IO.IMPORT));
		btnLoadBoundingBoxMesh.setToolTipText(I.tr("BoundingBox aus Mesh laden"));
		add(btnLoadBoundingBoxMesh, LayoutUtils.sqrBtn());
		btnLoadBoundingBoxMesh.addActionListener(e -> handleLoadBoundingBoxMesh());
	}

	public void setBoundingBox(bCBox boundingBox) {
		this.boundingBox = boundingBox.clone();

		lblMin.setText(I.trf("Min: {0}", this.boundingBox.getMin()));
		lblMax.setText(I.trf("Max: {0}", this.boundingBox.getMax()));
	}

	private void handleCopyBoundingBox() {
		IOUtils.copyToClipboard("Min: " + boundingBox.getMin() + "\nMax: " + boundingBox.getMax());
	}

	private void handleChangeBoundingBox() {
		BoundingBoxDialog dialog = new BoundingBoxDialog(ctx.getParentWindow(), boundingBox);

		if (dialog.openAndWasSuccessful(this)) {
			changeBoundingBoxConsumer.accept(dialog.getBox());
		}
	}

	private void handleLoadBoundingBoxMesh() {
		String meshName = meshNameSupplier.get();
		if (meshName == null || !meshName.endsWith("xcmsh")) {
			TaskDialogs.error(ctx.getParentWindow(), "", I.tr("Entity hat kein statisches Mesh."));
			return;
		}

		Optional<File> meshFile = ctx.getFileManager().searchFile(FileManager.RP_COMPILED_MESH, meshName);
		if (!meshFile.isPresent()) {
			TaskDialogs.error(ctx.getParentWindow(), "", I.tr("Mesh konnte nicht gefunden werden") + ": " + meshName);
			return;
		}

		try {
			eCResourceMeshComplex_PS mesh = FileUtil.openMesh(meshFile.get());
			changeBoundingBoxConsumer.accept(mesh.property(CD.eCResourceMeshComplex_PS.BoundingBox));
		} catch (IOException e) {
			TaskDialogs.showException(e);
		}

	}

	private void handleLoadBoundingBoxTple() {
		new TemplateNameSearchDialog(tpleFile -> {
			if (tpleFile.getHeaderCount() == 2) {
				changeBoundingBoxConsumer.accept(tpleFile.getReferenceHeader().getLocalNodeBoundary());
				return true;
			} else {
				return false;
			}
		}, ctx, defaultTemplateNameSupplier.get()).open(this);
	}
}
