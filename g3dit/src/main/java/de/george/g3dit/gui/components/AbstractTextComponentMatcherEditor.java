/* Glazed Lists (c) 2003-2006 */
/* http://publicobject.com/glazedlists/ publicobject.com, */
/* O'Dell Engineering Ltd. */
package de.george.g3dit.gui.components;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

import ca.odell.glazedlists.matchers.AbstractMatcherEditor;
import ca.odell.glazedlists.matchers.TextMatcherEditor;

/**
 * A MatcherEditor that matches Objects that contain the filter text located within a
 * {@link Document}. This {@link TextMatcherEditor} is directly coupled with a Document and fires
 * MatcherEditor changes in response to Document changes. This matcher is fully concrete and is
 * expected to be used by Swing applications.
 * <p>
 * The {@link AbstractTextComponentMatcherEditor} constructors require that either a
 * {@link Document} or a {@link JTextComponent} (from which a {@link Document} is extracted) be
 * specified.
 * <p>
 * The MatcherEditor registers itself as a {@link DocumentListener} on the given Document, or
 * {@link ActionListener} on the {@link JTextComponent} for non-live filtering. If a
 * {@link JTextComponent} is given on construction, it is also watched for changes of its Document
 * and the Document used by this MatcherEditor is updated to reflect the latest Document behind the
 * text component. If this MatcherEditor must be garbage collected before the underlying Document,
 * or JTextComponent, the listeners can be unregistered by calling {@link #dispose()}.
 *
 * @author James Lemieux
 */
public abstract class AbstractTextComponentMatcherEditor<E> extends AbstractMatcherEditor<E> {

	/** the Document that provides the filter values */
	private Document document;

	/** the JTextComponent being observed for actions */
	private final JTextComponent textComponent;

	/** whether we're listening to each keystroke */
	private boolean live;

	/** The listener attached to the given {@link #document}. */
	private final FilterHandler filterHandler = new FilterHandler();

	/**
	 * Creates a TextMatcherEditor bound to the {@link Document} backing the given
	 * <code>textComponent</code> with the given <code>textFilterator</code>.
	 *
	 * @param textComponent the text component backed by the {@link Document} that is the source of
	 *            text filter values
	 * @param textFilterator an object capable of producing Strings from the objects being filtered.
	 *            If <code>textFilterator</code> is <code>null</code> then all filtered objects are
	 *            expected to implement {@link ca.odell.glazedlists.TextFilterable}.
	 */
	public AbstractTextComponentMatcherEditor(JTextComponent textComponent) {
		this(textComponent, true);
	}

	/**
	 * Creates a TextMatcherEditor bound to the {@link Document} backing the given
	 * <code>textComponent</code> with the given <code>textFilterator</code>.
	 *
	 * @param textComponent the text component backed by the {@link Document} that is the source of
	 *            text filter values
	 * @param textFilterator an object capable of producing Strings from the objects being filtered.
	 *            If <code>textFilterator</code> is <code>null</code> then all filtered objects are
	 *            expected to implement {@link ca.odell.glazedlists.TextFilterable}.
	 * @param live <code>true</code> to filter by the keystroke or <code>false</code> to filter only
	 *            when {@link java.awt.event.KeyEvent#VK_ENTER Enter} is pressed within the
	 *            {@link JTextComponent}. Note that non-live filtering is only supported if
	 *            <code>textComponent</code> is a {@link JTextField}.
	 * @throws IllegalArgumentException if the <code>textComponent</code> is not a
	 *             {@link JTextField} and non-live filtering is specified.
	 */
	public AbstractTextComponentMatcherEditor(JTextComponent textComponent, boolean live) {
		this(textComponent, textComponent.getDocument(), live);
	}

	/**
	 * Creates a TextMatcherEditor bound to the given <code>document</code> with the given
	 * <code>textFilterator</code>.
	 *
	 * @param document the {@link Document} that is the source of text filter values
	 * @param textFilterator an object capable of producing Strings from the objects being filtered.
	 *            If <code>textFilterator</code> is <code>null</code> then all filtered objects are
	 *            expected to implement {@link ca.odell.glazedlists.TextFilterable}.
	 */
	public AbstractTextComponentMatcherEditor(Document document) {
		this(null, document, true);
	}

	/**
	 * This private constructor implements the actual construction work and thus ensures that all
	 * public constructors agree on the construction logic.
	 */
	private AbstractTextComponentMatcherEditor(JTextComponent textComponent, Document document, boolean live) {
		this.textComponent = textComponent;
		this.document = document;
		this.live = live;
		registerListeners(live);

		// if the document is non-empty to begin with!
		refilter();
	}

	/**
	 * Whether filtering occurs by the keystroke or not.
	 */
	public boolean isLive() {
		return live;
	}

	/**
	 * Toggle between filtering by the keystroke and not.
	 *
	 * @param live <code>true</code> to filter by the keystroke or <code>false</code> to filter only
	 *            when {@link java.awt.event.KeyEvent#VK_ENTER Enter} is pressed within the
	 *            {@link JTextComponent}. Note that non-live filtering is only supported if
	 *            <code>textComponent</code> is a {@link JTextField}.
	 */
	public void setLive(boolean live) {
		if (live == this.live) {
			return;
		}
		deregisterListeners(this.live);
		this.live = live;
		registerListeners(this.live);
	}

	/**
	 * Listen live or on action performed.
	 */
	private void registerListeners(boolean live) {
		if (live) {
			document.addDocumentListener(filterHandler);
		} else {
			if (textComponent == null) {
				throw new IllegalArgumentException("Non-live filtering supported only for JTextField (document provided)");
			}
			if (!(textComponent instanceof JTextField textField)) {
				throw new IllegalArgumentException(
						"Non-live filtering supported only for JTextField (argument class " + textComponent.getClass().getName() + ")");
			}
			textField.addActionListener(filterHandler);
		}

		if (textComponent != null) {
			textComponent.addPropertyChangeListener(filterHandler);
		}
	}

	/**
	 * Stop listening.
	 */
	private void deregisterListeners(boolean live) {
		if (live) {
			document.removeDocumentListener(filterHandler);
		} else {
			JTextField textField = (JTextField) textComponent;
			textField.removeActionListener(filterHandler);
		}

		if (textComponent != null) {
			textComponent.removePropertyChangeListener(filterHandler);
		}
	}

	/**
	 * A cleanup method which stops this MatcherEditor from listening to changes on the underlying
	 * {@link Document}, thus freeing the MatcherEditor or Document to be garbage collected.
	 */
	public void dispose() {
		deregisterListeners(live);
	}

	/**
	 * Update the filter text from the contents of the Document.
	 */
	private void refilter() {
		try {
			this.updateFilter(document.getText(0, document.getLength()));
		} catch (BadLocationException ble) {
			// this shouldn't ever, ever happen
			throw new RuntimeException(ble);
		}
	}

	protected abstract void updateFilter(String text);

	/**
	 * This class responds to any change in the Document by setting the filter text of this
	 * TextMatcherEditor to the contents of the Document.
	 */
	private class FilterHandler implements DocumentListener, ActionListener, PropertyChangeListener {
		@Override
		public void insertUpdate(DocumentEvent e) {
			refilter();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			refilter();
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			refilter();
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			refilter();
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			if ("document" == evt.getPropertyName()) {
				// stop listening to the old Document
				deregisterListeners(live);

				// start listening to the new Document
				document = textComponent.getDocument();
				registerListeners(live);

				// refilter based on the new Document
				refilter();
			}
		}
	}
}
