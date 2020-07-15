package de.george.g3dit.tab.secdat;

import java.util.Optional;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.ezware.dialog.task.TaskDialogs;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import de.george.g3dit.gui.components.JEventList;
import de.george.g3dit.gui.components.ListModificationControl;
import de.george.g3utils.gui.SwingUtils;
import de.george.lrentnode.archive.SecDat;
import net.miginfocom.swing.MigLayout;

public class SecdatContentPane extends JPanel {
	private EditorSecdatTab ctx;

	private EventList<String> lrentdats = new BasicEventList<>();
	private EventList<String> nodes = new BasicEventList<>();

	public SecdatContentPane(EditorSecdatTab ctx) {
		this.ctx = ctx;
	}

	public void initGUI() {
		setLayout(new MigLayout("", "[]"));

		add(SwingUtils.createBoldLabel("Lrentdats"), "wrap");
		JEventList<String> lrentdatList = new JEventList<>(lrentdats);
		add(new JScrollPane(lrentdatList), "gapleft 7, width 100:400:500, sgx lrentdats, wrap");
		add(new ListModificationControl<>(ctx, lrentdatList, lrentdats, () -> inputEntry("Lrentdat")), "gapleft 7, sgx lrentdats, wrap");

		add(SwingUtils.createBoldLabel("Nodes"), "gaptop 5, wrap");
		JEventList<String> nodeList = new JEventList<>(nodes);
		add(new JScrollPane(nodeList), "gapleft 7, width 100:400:500, sgx nodes, wrap");
		add(new ListModificationControl<>(ctx, nodeList, nodes, () -> inputEntry("Node")), "gapleft 7, sgx nodes, wrap");
	}

	public void loadValues() {
		SecDat secdat = ctx.getCurrentSecdat();
		lrentdats.clear();
		lrentdats.addAll(secdat.getLrentdatFiles());
		nodes.clear();
		nodes.addAll(secdat.getNodeFiles());
	}

	public void saveValues() {
		SecDat secdat = ctx.getCurrentSecdat();
		secdat.getLrentdatFiles().clear();
		secdat.getLrentdatFiles().addAll(lrentdats);
		secdat.getNodeFiles().clear();
		secdat.getNodeFiles().addAll(nodes);
	}

	public void onClose() {
		// TODO Auto-generated method stub
	}

	private Optional<String> inputEntry(String name) {
		String input = TaskDialogs.input(ctx.getParentWindow(), "Neuen Eintrag erstellen", "Bitte Namen der " + name + " eingeben",
				name + " einfügen");

		return Optional.ofNullable(input).filter(i -> !i.isEmpty());
	}
}
