package de.george.g3dit.gui.dialogs;

import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.jdesktop.swingx.JXTable;

import de.george.g3dit.EditorContext;
import de.george.g3dit.gui.table.TableUtil;
import de.george.g3dit.util.AbstractFileWorker;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.util.Pair;

public abstract class AbstractTableProgressDialog extends ExtStandardDialog {
	protected EditorContext ctx;

	private Map<JButton, Pair<Action, Action>> workButtons = new HashMap<>();
	private JButton btnWorkCurrent;

	protected JProgressBar progressBar;
	private JXTable table;

	@SuppressWarnings("rawtypes")
	protected AbstractFileWorker worker;

	public AbstractTableProgressDialog(EditorContext ctx, String title) {
		super(ctx.getParentWindow(), title, false);
		this.ctx = ctx;

		setType(Type.NORMAL);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (worker != null) {
					worker.cancel(true);
				}
			}
		});
	}

	public JButton registerAction(String workTitle, Icon workIcon, Runnable workCallback, boolean isDefault) {
		JButton btnWork = new JButton();
		btnWork.setFocusable(false);

		Action searchAction = SwingUtils.createAction(workTitle, workIcon, () -> {
			if (worker == null) {
				btnWorkCurrent = btnWork;
				workCallback.run();
			}
		});

		Action cancelAction = SwingUtils.createAction("Abbrechen", Icons.getImageIcon(Icons.Select.CANCEL), () -> {
			if (worker != null) {
				worker.cancel(true);
			}
		});

		if (isDefault) {
			setDefaultAction(searchAction);
			setDefaultCancelAction(cancelAction);
		}

		workButtons.put(btnWork, Pair.of(searchAction, cancelAction));

		btnWork.setAction(searchAction);
		return btnWork;
	}

	protected void appendBarAndTable(JPanel mainPanel, JXTable table) {
		this.table = table;
		progressBar = SwingUtils.createProgressBar();
		mainPanel.add(progressBar, "width 100%, spanx, wrap");

		JScrollPane tableScroll = new JScrollPane(table);
		mainPanel.add(tableScroll, "gaptop 7, push, span, grow, wrap");
	}

	protected void setEntryActivationListener(Consumer<Integer> listener) {
		table.addMouseListener(TableUtil.createDoubleClickListener(listener));
		table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "Enter");
		table.getActionMap().put("Enter", SwingUtils.createAction(() -> TableUtil.withSelectedRow(table, listener)));
	}

	protected void toggleWorkButton() {
		if (worker == null) {
			// SearchAction
			btnWorkCurrent.setAction(workButtons.get(btnWorkCurrent).el0());
		} else {
			// CancelAction
			btnWorkCurrent.setAction(workButtons.get(btnWorkCurrent).el1());
		}
	}

	protected void executeWorker() {
		toggleWorkButton();
		worker.setDoneCallback(this::onWorkerDone);
		worker.execute();
	}

	protected void onWorkerDone() {
		worker = null;
		toggleWorkButton();
		ctx.runGC();
	}
}
