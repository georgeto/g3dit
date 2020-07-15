package de.george.g3dit.util.event;

import java.awt.Container;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import javax.swing.KeyStroke;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tulskiy.keymaster.common.Provider;

public class VisibilityManager implements WindowStateListener {
	private static final Logger logger = LoggerFactory.getLogger(VisibilityManager.class);

	private Provider hotKeyProvider;
	private WeakReference<Frame> lastActiveFrame;
	private WeakReference<Window> lastActiveWindow;
	private boolean iconifying;

	public VisibilityManager(Frame mainFrame, Provider hotKeyProvider) {
		this.hotKeyProvider = hotKeyProvider;
		setLastActiveFrame(mainFrame);
		lastActiveWindow = new WeakReference<>(mainFrame);
		installHotkey();
		installActiveWindowListener();
	}

	private void installHotkey() {
		hotKeyProvider.register(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK), h -> {

			if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() != null) {
				iconifying = true;
				Frame activeFrame = lastActiveFrame.get();
				if (activeFrame != null) {
					activeFrame.setState(Frame.ICONIFIED);
				} else {
					logger.warn("Konnte g3dit nicht in den Vordergrund bringen, da kein letztes aktives Fenster gefunden werden konnte.");
				}

			} else {
				Frame activeFrame = lastActiveFrame.get();
				if (activeFrame != null) {
					Window activeWindow = lastActiveWindow.get();
					if (activeWindow == null) {
						activeWindow = activeFrame;
					}
					activeFrame.setState(Frame.NORMAL);
					activeWindow.toFront();
					activeWindow.requestFocus();
					activeWindow.repaint();
				} else {
					logger.warn("Konnte g3dit nicht in den Hinterund bringen, da kein letztes aktives Fenster gefunden werden konnte.");
				}
			}
		});
	}

	private void installActiveWindowListener() {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("activeWindow", e -> {
			Window newActiveWindow = (Window) e.getNewValue();

			if (newActiveWindow != null && !iconifying) {
				Container newActiveFrame = newActiveWindow;
				while (true) {
					if (newActiveFrame == null) {
						break;
					}

					if (newActiveFrame instanceof Frame) {
						setLastActiveFrame((Frame) newActiveFrame);
						break;
					}

					newActiveFrame = newActiveFrame.getParent();
				}

				lastActiveWindow = new WeakReference<>(newActiveWindow);
			}
		});
	}

	private void setLastActiveFrame(Frame frame) {
		lastActiveFrame = new WeakReference<>(frame);
		if (Arrays.stream(frame.getWindowStateListeners()).noneMatch(l -> l == VisibilityManager.this)) {
			frame.addWindowStateListener(this);
		}
	}

	@Override
	public void windowStateChanged(WindowEvent e) {
		if (e.getNewState() == Frame.ICONIFIED && e.getWindow() == lastActiveFrame.get()) {
			iconifying = false;
		}
	}
}
