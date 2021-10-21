package de.george.g3dit.gui.theme;

import java.io.InputStream;
import java.lang.reflect.Field;

import javax.swing.LookAndFeel;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

import com.ezware.dialog.task.IContentDesign;
import com.ezware.dialog.task.TaskDialog;
import com.ezware.dialog.task.design.DefaultContentDesign;
import com.formdev.flatlaf.FlatDefaultsAddon;

import de.george.g3utils.util.ReflectionUtils;

public class FlatTaskDialogDefaultsAddon extends FlatDefaultsAddon {
	@Override
	public InputStream getDefaults(Class<?> lafClass) {
		// LookAndFeelFactory.registerDefaultInitializer(FlatLaf.class.getName(),
		// FlatJideOssDefaultsAddon.FlatJideUIDefaultsCustomizer.class.getName());
		// LookAndFeelFactory.registerDefaultCustomizer(FlatLaf.class.getName(),
		// FlatJideOssDefaultsAddon.FlatJideUIDefaultsCustomizer.class.getName());

		return super.getDefaults(lafClass);
	}

	private static final Field FIELD_DESIGN = ReflectionUtils.getField(TaskDialog.class, "design");
	private static final Field FIELD_COMMAND_LINK_PAINTER = ReflectionUtils.getField(DefaultContentDesign.class, "commandButtonPainter");

	@Override
	public void afterDefaultsLoading(LookAndFeel laf, UIDefaults defaults) {
		// Force initialization of ContentDesign
		TaskDialog.makeKey("");

		UIManager.put(IContentDesign.COLOR_MESSAGE_BACKGROUND, defaults.getColor("Button.background"));
		UIManager.put(IContentDesign.COLOR_INSTRUCTION_FOREGROUND, defaults.getColor("Button.foreground"));

		DefaultContentDesign design = ReflectionUtils.getFieldValue(FIELD_DESIGN, null);
		ReflectionUtils.setFieldValue(FIELD_COMMAND_LINK_PAINTER, design, new FlatCommandLinkPainter());
	}

	@Override
	public int getPriority() {
		return 11;
	}
}
