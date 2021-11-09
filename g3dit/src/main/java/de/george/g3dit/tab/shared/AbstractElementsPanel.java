package de.george.g3dit.tab.shared;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.netbeans.validation.api.ui.ValidationGroup;

import com.teamunify.i18n.I;

import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.validation.ValidationGroupWrapper;
import net.miginfocom.swing.MigLayout;

public abstract class AbstractElementsPanel<T> extends JPanel {
	private static final String ELEMENT_PANEL_CONSTRAINTS = "wrap, width 100:300:450, grow";

	protected enum InsertPosition {
		Before,
		After
	}

	private boolean allowEmpty;

	private ValidationGroupWrapper validationGroup;
	private JScrollPane navScroll;
	private JButton addElementBtn;

	public AbstractElementsPanel(String elementName, JScrollPane navScroll, boolean allowEmpty) {
		this.navScroll = navScroll;
		this.allowEmpty = allowEmpty;

		setLayout(new MigLayout("fillx, insets 0 5 0 0", "[]"));

		addElementBtn = new JButton(I.trf("Add {0}", elementName), Icons.getImageIcon(Icons.Action.ADD));
		addElementBtn.addActionListener(e -> insertNewElement(null, InsertPosition.After));
	}

	/**
	 * Einmalig nach erstellen des Panels aufrufen, um die Fehleranzeige zu initialisieren
	 */
	public void initValidation(ValidationGroup validation) {
		validationGroup = new ValidationGroupWrapper(validation);
	}

	public boolean isEmpty() {
		return getComponentCount() == 0 || getComponent(0).equals(addElementBtn);
	}

	/**
	 * Nach dem Umschalten auf eine Entity aufrufen, um deren Werte ins GUI zu laden
	 */
	public void loadValues(T entity) {
		// Alle alten Panels löschen
		removeAll();
		if (validationGroup != null) {
			validationGroup.removeAll();
		}

		loadValuesInternal(entity);

		updateStatus();
		repaintPanel();
	}

	public void saveValues(T entity) {
		if (!isEmpty()) {
			saveValuesInternal(entity);
		} else {
			removeValuesInternal(entity);
		}
	}

	/**
	 * {@link stack} um eine Position nach oben verschieben
	 *
	 * @param element
	 */
	public void moveUp(AbstractElementPanel element) {
		int index = SwingUtils.getComponentIndex(element);
		if (index > 0) {
			remove(element);
			add(element, ELEMENT_PANEL_CONSTRAINTS, index - 1);
			updateStatus();
			repaintPanel();
		}
	}

	/**
	 * {@link stack} um eine Position nach unten verschieben
	 *
	 * @param element
	 */
	public void moveDown(AbstractElementPanel element) {
		int index = SwingUtils.getComponentIndex(element);
		if (index < getComponentCount() - 1) {
			remove(element);
			add(element, ELEMENT_PANEL_CONSTRAINTS, index + 1);
			updateStatus();
			repaintPanel();
		}
	}

	/**
	 * {@link stack} entfernen
	 *
	 * @param element
	 */
	public void removeElement(AbstractElementPanel element) {
		remove(element);
		if (validationGroup != null) {
			element.removeValidation(validationGroup);
		}
		updateStatus();
		repaintPanel();
	}

	/**
	 * @param element
	 * @param mode
	 */
	public void insertNewElement(AbstractElementPanel element, InsertPosition mode) {
		insertElementRelative(getNewElement(), element, mode);
	}

	/**
	 * Fügt ein Element relativ zu einem vorhanden Element ein.
	 *
	 * @param insertElement Element, dass eingefügt werden soll.
	 * @param relElement null oder bereits vorhandenes Element.
	 * @param mode
	 */
	public void insertElementRelative(AbstractElementPanel insertElement, AbstractElementPanel relElement, InsertPosition mode) {
		if (validationGroup != null) {
			insertElement.initValidation(validationGroup.getValidationGroup());
		}
		if (relElement != null) {
			add(insertElement, ELEMENT_PANEL_CONSTRAINTS,
					SwingUtils.getComponentIndex(relElement) + (mode == InsertPosition.Before ? 0 : 1));
		} else {
			// Keine Stacks, nur der Button zum Hinzufügen
			if (isEmpty()) {
				removeAll();
			}
			add(insertElement, ELEMENT_PANEL_CONSTRAINTS);
		}
		updateStatus();
		repaintPanel();
	}

	public void clear() {
		removeAll();
		updateStatus();
		repaintPanel();
	}

	/**
	 * Aktualisiert die Rauf/Runter/Löschen Buttons der Stacks
	 */
	private void updateStatus() {
		int compCount = getComponentCount();
		if (compCount == 0) {
			add(addElementBtn, "align center");
		}
		if (compCount == 1) {
			((AbstractElementPanel) getComponent(0)).setStatus(false, false, allowEmpty, 1);
		} else if (compCount >= 2) {
			((AbstractElementPanel) getComponent(0)).setStatus(false, true, true, 1);
			((AbstractElementPanel) getComponent(compCount - 1)).setStatus(true, false, true, compCount);
			for (int i = 1; i < compCount - 1; i++) {
				((AbstractElementPanel) getComponent(i)).setStatus(true, true, true, i + 1);
			}
		}

	}

	/**
	 * Nach Löschen, Hinzufügen oder Verschieben von Stacks ausführen
	 */
	private void repaintPanel() {
		validate();
		repaint(50L);
		navScroll.validate();
	}

	protected abstract void loadValuesInternal(T entity);

	/**
	 * Vor dem Umschalten auf eine andere Entity aufrufen, um Änderungen zu speichern
	 */
	public abstract void saveValuesInternal(T entity);

	/**
	 * Wird anstatt saveValuesInternal aufgerufen, wenn das Panel beim Speichern keine Elemente
	 * enthält.
	 */
	protected abstract void removeValuesInternal(T entity);

	protected abstract AbstractElementPanel getNewElement();
}
