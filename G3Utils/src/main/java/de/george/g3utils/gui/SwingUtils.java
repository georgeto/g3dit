package de.george.g3utils.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import de.george.g3utils.util.WordWrapUtils;
import one.util.streamex.StreamEx;

public class SwingUtils {
	private static final Logger logger = LoggerFactory.getLogger(SwingUtils.class);

	public static UndoableTextField createUndoTF() {
		return new UndoableTextField();
	}

	public static UndoableTextField createUndoTF(String text) {
		return new UndoableTextField(text);
	}

	public static final int getComponentIndex(Component component) {
		if (component != null && component.getParent() != null) {
			Container c = component.getParent();
			for (int i = 0; i < c.getComponentCount(); i++) {
				if (c.getComponent(i) == component) {
					return i;
				}
			}
		}

		return -1;
	}

	public static ButtonGroup createButtonGroup(AbstractButton... buttons) {
		ButtonGroup group = new ButtonGroup();
		for (AbstractButton button : buttons) {
			group.add(button);
		}
		return group;
	}

	public static void setSystemLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			logger.warn("Error while changing Look and Feel.");
		}
	}

	public static void setLookAndFeel(LookAndFeel lookAndFeel) {
		try {
			UIManager.setLookAndFeel(lookAndFeel);
		} catch (Exception e) {
			logger.warn("Error while changing Look and Feel.");
		}
	}

	public static ImageIcon loadIcon(String path) {
		return new ImageIcon(loadImage(path));
	}

	public static Image loadImage(String path) {
		try {
			return ImageIO.read(SwingUtils.class.getResource(path));
		} catch (IOException e) {
			return null;
		}
	}

	public static Image getG3Icon() {
		return loadImage("/res/G3.png");
	}

	public static TitledBorder createTitledBorder(Border innerBorder, String title, Color color, boolean bold) {
		TitledBorder titledBorder = BorderFactory.createTitledBorder(innerBorder, title, TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, null, color);
		if (bold) {
			titledBorder.setTitleFont(UIManager.getFont("TitledBorder.font").deriveFont(Font.BOLD));
		}
		return titledBorder;
	}

	public static JLabel createBoldLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(label.getFont().deriveFont(Font.BOLD, 13));
		return label;
	}

	public static DocumentListener createDocumentListener(Runnable function) {
		return new DocumentListener() {
			@Override
			public void removeUpdate(DocumentEvent e) {
				function.run();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				function.run();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				function.run();
			}
		};
	}

	public static void generateTooltipErrorList(JComponent comp, List<String> errors, List<String> warnings) {
		int totalCount = errors.size() + warnings.size();
		if (totalCount == 0) {
			return;
		}

		Color severity = errors.isEmpty() ? new Color(255, 100, 0) : Color.RED;
		comp.setForeground(severity);

		ArrayList<String> entries = Lists.newArrayList(
				Iterables.concat(StreamEx.of(errors).map(e -> "[Fehler] " + e), StreamEx.of(warnings).map(e -> "[Warnung] " + e)));
		if (totalCount == 1) {
			comp.setToolTipText(entries.get(0));
		} else {
			comp.setForeground(Color.RED);
			comp.setToolTipText(entries.stream().collect(Collectors.joining("<p>• ", "<html>• ", "</html>")));
		}
	}

	public static Action createAction(Runnable listener) {
		return createAction(null, null, listener);

	}

	public static Action createAction(String name, Runnable listener) {
		return createAction(name, null, listener);

	}

	public static Action createAction(String name, Icon icon, Runnable listener) {
		return new AbstractAction(name, icon) {
			@Override
			public void actionPerformed(ActionEvent e) {
				listener.run();
			}
		};
	}

	public static int getMultilabelHeight(int lines) {
		return (int) new JLabel("<html>" + Strings.repeat("<br>", lines) + "</html>").getPreferredSize().getHeight();
	}

	public static String getMultilineText(String... lines) {
		return Arrays.stream(lines).collect(Collectors.joining("<br>", "<html>", "</html>"));
	}

	public static String wrapTooltipText(String tooltipText, int wrapLength) {
		return "<html>" + WordWrapUtils.wrap(tooltipText, wrapLength, "<br>", false, " ", "\r?\n") + "</html>";
	}

	public static void bringToFront(Window window) {
		if (window instanceof Frame) {
			Frame frame = (Frame) window;
			if (frame.getState() == Frame.ICONIFIED) {
				frame.setState(Frame.NORMAL);
			}
		}
		window.toFront();
		window.requestFocus();
		window.repaint();
	}

	public static JEditorPane createSelectableLabel() {
		return createSelectableLabel(null);
	}

	public static JEditorPane createSelectableLabel(String text) {
		// create a JEditorPane that renders HTML and defaults to the system font.
		JEditorPane editorPane = new JEditorPane(new HTMLEditorKit().getContentType(), text);
		// set the text of the JEditorPane to the given text.
		editorPane.setText(text);

		// add a CSS rule to force body tags to use the default label font
		// instead of the value in javax.swing.text.html.default.csss
		Font font = UIManager.getFont("Label.font");
		String bodyRule = "body { font-family: " + font.getFamily() + "; " + "font-size: " + font.getSize() + "pt; }";
		((HTMLDocument) editorPane.getDocument()).getStyleSheet().addRule(bodyRule);

		editorPane.setOpaque(false);
		editorPane.setBorder(null);
		editorPane.setEditable(false);

		return editorPane;
	}

	/**
	 * A convenience method to ensure a method is being accessed from the Event Dispatch Thread.
	 */
	public static void checkAccessThread() {
		if (!SwingUtilities.isEventDispatchThread()) {
			throw new IllegalStateException(
					"This method must be accessed from the Swing Event Dispatch Thread, but was called on Thread \""
							+ Thread.currentThread().getName() + "\"");
		}
	}

	public static void addKeyStroke(JComponent comp, String actionMapKey, int keyCode, Runnable callback) {
		addKeyStroke(comp, actionMapKey, keyCode, 0, callback);
	}

	public static void addKeyStroke(JComponent comp, String actionMapKey, int keyCode, int modifiers, Runnable callback) {
		addKeyStroke(comp, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, actionMapKey, KeyStroke.getKeyStroke(keyCode, modifiers),
				callback, false);
	}

	public static void addWindowKeyStroke(JComponent comp, String actionMapKey, int keyCode, Runnable callback) {
		addWindowKeyStroke(comp, actionMapKey, keyCode, 0, callback);
	}

	public static void addWindowKeyStroke(JComponent comp, String actionMapKey, int keyCode, int modifiers, Runnable callback) {
		addKeyStroke(comp, JComponent.WHEN_IN_FOCUSED_WINDOW, actionMapKey, KeyStroke.getKeyStroke(keyCode, modifiers), callback, true);
	}

	public static void addWindowKeyStroke(JComponent comp, String actionMapKey, int keyCode, int modifiers, Action action) {
		addKeyStroke(comp, JComponent.WHEN_IN_FOCUSED_WINDOW, actionMapKey, KeyStroke.getKeyStroke(keyCode, modifiers), action, true);
	}

	public static void addKeyStroke(JComponent comp, int condition, String actionMapKey, KeyStroke keyStroke, Runnable callback) {
		addKeyStroke(comp, condition, actionMapKey, keyStroke, callback, true);
	}

	/**
	 * @param comp
	 * @param condition one of {@link JComponent#WHEN_IN_FOCUSED_WINDOW},
	 *            {@link JComponent#WHEN_FOCUSED},
	 *            {@link JComponent#WHEN_ANCESTOR_OF_FOCUSED_COMPONENT}
	 * @param actionMapKey
	 * @param keyStroke
	 * @param callback
	 */
	public static void addKeyStroke(JComponent comp, int condition, String actionMapKey, KeyStroke keyStroke, Runnable callback,
			boolean addTooltip) {
		addKeyStroke(comp, condition, actionMapKey, keyStroke, SwingUtils.createAction(callback), addTooltip);
	}

	public static void addKeyStroke(JComponent comp, int condition, String actionMapKey, KeyStroke keyStroke, Action action,
			boolean addTooltip) {
		comp.getInputMap(condition).put(keyStroke, actionMapKey);
		comp.getActionMap().put(actionMapKey, action);

		if (addTooltip) {
			String tooltip = Strings.nullToEmpty(comp.getToolTipText());
			if (!tooltip.isEmpty()) {
				tooltip += " ";
			}

			tooltip += "[" + getKeyStrokeText(keyStroke) + "]";
			comp.setToolTipText(tooltip);
		}
	}

	public static String getKeyStrokeText(KeyStroke keyStroke) {
		String accText = "";
		if (keyStroke != null) {
			int modifiers = keyStroke.getModifiers();
			if (modifiers > 0) {
				accText = KeyEvent.getKeyModifiersText(modifiers);
				accText += "+";
			}
			int keyCode = keyStroke.getKeyCode();
			if (keyCode != 0) {
				accText += KeyEvent.getKeyText(keyCode);
			} else {
				accText += keyStroke.getKeyChar();
			}
		}
		return accText;
	}

	public static JButton keyStrokeButton(String text, int keyCode, int modifiers, Runnable callback) {
		return keyStrokeButton(text, null, null, keyCode, modifiers, callback);
	}

	public static JButton keyStrokeButton(String text, String tooltip, int keyCode, int modifiers, Runnable callback) {
		return keyStrokeButton(text, tooltip, null, keyCode, modifiers, callback);
	}

	public static JButton keyStrokeButton(String text, Icon icon, int keyCode, int modifiers, Runnable callback) {
		return keyStrokeButton(text, null, icon, keyCode, modifiers, callback);
	}

	public static JButton keyStrokeButton(String text, String tooltip, Icon icon, int keyCode, int modifiers, Runnable callback) {
		Action action = SwingUtils.createAction(text, icon, callback);
		JButton btn = new JButton(action);
		btn.setToolTipText(tooltip);
		btn.setFocusable(false);
		SwingUtils.addWindowKeyStroke(btn, text, keyCode, modifiers, action);
		return btn;
	}

	public static int getScreenWorkingWidth() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().width;
	}

	public static int getScreenWorkingHeight() {
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height;
	}

	public static Color getAlphaColor(Color baseColor, float alphaValue) {
		if (alphaValue < 0.0 || alphaValue > 1.0) {
			throw new IllegalArgumentException("Alpha has to be between 0.0 and 1.0.");
		}
		return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int) (alphaValue * 255));
	}

	public static Component monospaceFont(Component component) {
		component.setFont(new Font("Lucida Console", Font.PLAIN, component.getFont().getSize()));
		return component;
	}

	public static Component smallFont(Component component) {
		component.setFont(component.getFont().deriveFont(component.getFont().getSize2D() * 0.75f));
		return component;
	}
}
