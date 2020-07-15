package de.george.g3dit.gui.dialogs;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.WindowConstants;

import de.george.g3dit.util.Icons;

public class ProgressDialog extends JDialog {

	private JProgressBar progressBar;

	private CancelListener cancelListener;

	private JLabel lblStatus;

	public ProgressDialog(Window owner, String title, String status, boolean cancelable) {
		super(owner);
		setType(Type.UTILITY);
		setResizable(false);
		setSize(300, 115);
		setTitle(title);
		setModal(true);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);

		progressBar = new JProgressBar();
		progressBar.setBounds(10, 30, 274, 20);
		getContentPane().add(progressBar);

		lblStatus = new JLabel(status);
		lblStatus.setBounds(10, 11, 274, 14);
		getContentPane().add(lblStatus);

		JButton btnCancel = new JButton("Abbrechen", Icons.getImageIcon(Icons.Action.DELETE));
		btnCancel.setBounds(94, 58, 105, 23);
		btnCancel.setEnabled(cancelable);
		btnCancel.setFocusable(false);
		getContentPane().add(btnCancel);

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
