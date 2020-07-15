package de.george.g3dit.gui.dialogs;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Window;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;

import com.jidesoft.dialog.ButtonPanel;
import com.jidesoft.dialog.StandardDialog;

import de.george.g3utils.gui.SwingUtils;

public abstract class ExtStandardDialog extends StandardDialog {
	public ExtStandardDialog() throws HeadlessException {
		setModal(false);
		installDefaultCancelAction();
	}

	public ExtStandardDialog(Dialog owner, boolean modal) throws HeadlessException {
		super(owner, modal);
		installDefaultCancelAction();
	}

	public ExtStandardDialog(Dialog owner, String title, boolean modal, GraphicsConfiguration gc) throws HeadlessException {
		super(owner, title, modal, gc);
		installDefaultCancelAction();
	}

	public ExtStandardDialog(Dialog owner, String title, boolean modal) throws HeadlessException {
		super(owner, title, modal);
		installDefaultCancelAction();
	}

	public ExtStandardDialog(Dialog owner, String title) throws HeadlessException {
		super(owner, title);
		setModal(false);
		installDefaultCancelAction();
	}

	public ExtStandardDialog(Frame owner, boolean modal) throws HeadlessException {
		super(owner, modal);
		installDefaultCancelAction();
	}

	public ExtStandardDialog(Frame owner, String title, boolean modal) throws HeadlessException {
		super(owner, title, modal);
		installDefaultCancelAction();
	}

	public ExtStandardDialog(Frame owner, String title) throws HeadlessException {
		super(owner, title, false);
		installDefaultCancelAction();
	}

	public ExtStandardDialog(Frame owner) throws HeadlessException {
		super(owner, false);
		installDefaultCancelAction();
	}

	public ExtStandardDialog(Window owner, String title, boolean modal) throws HeadlessException {
		super(owner, title);
		setModal(modal);
		installDefaultCancelAction();
	}

	public ExtStandardDialog(Window owner, String title) throws HeadlessException {
		super(owner, title);
		setModal(false);
		installDefaultCancelAction();
	}

	public ExtStandardDialog(Window owner) throws HeadlessException {
		super(owner);
		setModal(false);
		installDefaultCancelAction();
	}

	private void installDefaultCancelAction() {
		setDefaultCancelAction(SwingUtils.createAction(this::dispose));
	}

	public boolean isCancelled() {
		return getDialogResult() == RESULT_CANCELLED;
	}

	@Override
	public JComponent createBannerPanel() {
		return null;
	}

	@Override
	public ButtonPanel createButtonPanel() {
		return null;
	}

	protected ButtonPanel newButtonPanel() {
		ButtonPanel buttonPanel = new ButtonPanel();
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
		return buttonPanel;
	}

	protected void addButton(ButtonPanel buttonPanel, Action action, String type) {
		addButton(buttonPanel, new JButton(), action, type);
	}

	protected void addButton(ButtonPanel buttonPanel, JButton btn, Action action, String type) {
		buttonPanel.addButton(btn, type);
		btn.setAction(action);
		switch (type) {
			case ButtonPanel.AFFIRMATIVE_BUTTON:
				setDefaultAction(action);
				getRootPane().setDefaultButton(btn);
				break;
			case ButtonPanel.CANCEL_BUTTON:
				setDefaultCancelAction(action);
				break;
			default:
				break;
		}
	}

	protected void addDefaultButton(ButtonPanel buttonPanel, String title) {
		Action action = SwingUtils.createAction(title, () -> affirm());
		addButton(buttonPanel, action, ButtonPanel.AFFIRMATIVE_BUTTON);
	}

	protected void addDefaultCancelButton(ButtonPanel buttonPanel) {
		Action action = SwingUtils.createAction("Abbrechen", () -> cancel());
		addButton(buttonPanel, action, ButtonPanel.CANCEL_BUTTON);
	}

	public void open() {
		open(getParent());
	}

	public void open(Component positionRelativeTo) {
		setLocationRelativeTo(positionRelativeTo);
		setVisible(true);
	}

	public boolean openAndWasSuccessful() {
		open();
		return wasSuccessful();
	}

	public boolean openAndWasSuccessful(Component positionRelativeTo) {
		open(positionRelativeTo);
		return wasSuccessful();
	}

	public boolean wasSuccessful() {
		return getDialogResult() == StandardDialog.RESULT_AFFIRMED;
	}

	protected void affirm() {
		setDialogResult(RESULT_AFFIRMED);
		dispose();
	}

	protected void cancel() {
		setDialogResult(RESULT_CANCELLED);
		dispose();
	}
}
