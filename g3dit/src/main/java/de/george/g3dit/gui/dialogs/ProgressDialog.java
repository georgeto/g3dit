package de.george.g3dit.gui.dialogs;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import com.teamunify.i18n.I;

import de.george.g3dit.util.Icons;
import net.miginfocom.swing.MigLayout;

public class ProgressDialog extends JDialog {

	private JProgressBar progressBar;

	private CancelListener cancelListener;

	private JLabel lblStatus;

	public ProgressDialog(Window owner, String title, String status, boolean cancelable) {
		super(owner);
		setType(Type.UTILITY);
		setResizable(false);
		setSize(300, 120);
		setTitle(title);
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new MigLayout("fill"));

		lblStatus = new JLabel(status);
		getContentPane().add(lblStatus, "wmax 270, wrap");

		progressBar = new JProgressBar();
		getContentPane().add(progressBar, "height 20!, alignx center, growx, wrap");

		JButton btnCancel = new JButton(I.tr("Cancel"), Icons.getImageIcon(Icons.Action.DELETE));
		btnCancel.setEnabled(cancelable);
		btnCancel.setFocusable(false);
		getContentPane().add(btnCancel, "alignx center, wmin 100");

		btnCancel.addActionListener(e -> {
			if (cancelListener != null) {
				cancelListener.onCancel();
			}
		});

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (cancelListener != null) {
					cancelListener.onCancel();
				}
			}
		});
	}

	public void setStatusMessage(String status) {
		lblStatus.setText(status);
	}

	public JProgressBar getProgressBar() {
		return progressBar;
	}

	public void setCancelListener(CancelListener cancelListener) {
		this.cancelListener = cancelListener;
	}

	@FunctionalInterface
	public static interface CancelListener {
		public void onCancel();
	}
}
