package de.george.g3dit.tab.archive.views.entity;

import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.border.EtchedBorder;

import com.teamunify.i18n.I;
import de.george.g3dit.gui.components.JTextAreaExt;
import de.george.g3dit.gui.components.TextLineNumber;
import de.george.g3dit.gui.validation.PointDistanceValidator;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.eCEntity;
import one.util.streamex.StreamEx;

public abstract class AbstractNavTab extends AbstractEntityTab {

	protected JTextAreaExt taSticks;
	protected JRadioButton rbAbsolut, rbRelative;
	protected JCheckBox cbManualCoords;

	public AbstractNavTab(EditorArchiveTab ctx) {
		super(ctx);
	}

	protected void setupComponents(String strManualCoord) {
		rbRelative = new JRadioButton(I.tr("Relative Koordinaten"), true);
		rbAbsolut = new JRadioButton(I.tr("Absolute Koordinaten"));
		SwingUtils.createButtonGroup(rbRelative, rbAbsolut);

		taSticks = new JTextAreaExt(true);
		taSticks.getScrollPane().setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
		TextLineNumber tln = new TextLineNumber(taSticks);
		taSticks.getScrollPane().setRowHeaderView(tln);
		taSticks.setName("Sticks");
		addValidators(taSticks, new PointDistanceValidator());

		cbManualCoords = new JCheckBox(strManualCoord, false);
		cbManualCoords.setEnabled(false);

		JLabel lblStickList = new JLabel(I.tr("Sticks (x-Pos/y-Pos/z-Pos//)"));
		add(lblStickList, "gaptop 7, wrap");

		add(rbRelative, "");
		add(rbAbsolut, "wrap");

		add(taSticks.getScrollPane(), "spanx 2, grow, push, wrap");

		ActionListener coordListener = e -> {
			cbManualCoords.setEnabled(!rbRelative.isSelected());
			loadValues(ctx.getCurrentEntity());
		};

		rbAbsolut.addActionListener(coordListener);
		rbRelative.addActionListener(coordListener);
	}

	protected void loadSticks(eCEntity entity, List<bCVector> sticks) {
		StreamEx<bCVector> stream = StreamEx.of(sticks);
		if (rbAbsolut.isSelected()) {
			stream = stream.map(p -> p.getTransformed(entity.getWorldMatrix()));
		}
		taSticks.setText(Misc.formatVectorList(stream));
	}
}
