package de.george.g3dit.settings;

import java.awt.Window;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.winreg.RegistryException;
import com.jidesoft.swing.CheckBoxList;

import de.george.g3dit.Editor;
import de.george.g3utils.util.ExtensionRegistry;
import net.miginfocom.swing.MigLayout;

public class FileExtensionOptionHandler extends AbstractOptionHandler<Void> {
	private static final Logger logger = LoggerFactory.getLogger(FileExtensionOptionHandler.class);
	private static final String[] EXTENSIONS = new String[] {"lrentdat", "node", "secdat", "tple", "xcmsh", "xact", "xlmsh"};

	private CheckBoxList cblExt;

	public FileExtensionOptionHandler(Window parent) {
		super(parent);
	}

	@Override
	protected JPanel initContent() {
		JPanel content = super.initContent();

		cblExt = new CheckBoxList();
		JScrollPane scroll = new JScrollPane(cblExt);
		content.add(scroll, "grow, push");

		return content;
	}

	@Override
	public void load(OptionStore optionStore, Option<Void> option) {
		ExtensionRegistry registry = ExtensionRegistry.getInstance();
		String exePath = System.getProperty("g3dit.exefile");
		if (exePath == null) {
			return;
		}

		DefaultListModel<Extension> model = new DefaultListModel<>();
		cblExt.setModel(model);
		for (String ext : EXTENSIONS) {
			Extension extension = new Extension(ext);
			model.addElement(extension);
			if (registry.isRegisteredTo(ext, exePath)) {
				extension.setWasRegistered(true);
				cblExt.addCheckBoxListSelectedValue(extension, false);
			}
		}
	}

	@Override
	public void save(OptionStore optionStore, Option<Void> option) {
		ExtensionRegistry registry = ExtensionRegistry.getInstance();
		String exePath = System.getProperty("g3dit.exefile");
		if (exePath == null) {
			return;
		}

		@SuppressWarnings("unchecked")
		ListModel<Extension> model = cblExt.getModel();
		List<Object> checkedExts = Arrays.asList(cblExt.getCheckBoxListSelectedValues());
		for (int i = 0; i < model.getSize(); i++) {
			Extension ext = model.getElementAt(i);
			try {
				if (!ext.wasRegistered() && checkedExts.contains(ext)) {
					registry.registerExtension(ext.getName(), Editor.EDITOR_TITLE, registry.createDefaultCommand(exePath));
				} else if (ext.wasRegistered() && !checkedExts.contains(ext)) {
					registry.unregisterExtension(ext.getName());
				}
				ext.setWasRegistered(checkedExts.contains(ext));
			} catch (RegistryException e) {
				logger.warn("Dateiendung konnte nicht registriert werden: ", e);
			}
		}
	}

	@Override
	protected MigLayout getLayoutManager() {
		return new MigLayout("ins 0, fill");
	}

	private static class Extension {
		private String name;
		private boolean wasRegistered = false;

		public Extension(String name) {
			this.name = name;
		}

		public boolean wasRegistered() {
			return wasRegistered;
		}

		public void setWasRegistered(boolean wasRegistered) {
			this.wasRegistered = wasRegistered;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
