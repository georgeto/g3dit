package de.george.g3dit.gui.theme;

import java.io.FileInputStream;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatPropertiesLaf;
import com.formdev.flatlaf.IntelliJTheme;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.util.SystemInfo;

import de.george.g3utils.util.FilesEx;

public class ThemeManager {
	private static final Logger logger = LoggerFactory.getLogger(ThemeManager.class);

	public static ThemeInfo getNativeTheme() {
		LookAndFeel nativeLookAndFeel = null;
		try {
			nativeLookAndFeel = (LookAndFeel) Class.forName(UIManager.getSystemLookAndFeelClassName()).newInstance();
		} catch (ReflectiveOperationException e) {
			logger.warn("Failed to create system look and feel.", e);
		}

		if (SystemInfo.isWindows) {
			try {
				nativeLookAndFeel = (LookAndFeel) Class.forName("com.jgoodies.looks.windows.WindowsLookAndFeel").getConstructor()
						.newInstance();
			} catch (ReflectiveOperationException | LinkageError e) {
				logger.warn("Failed to create windows look and feel.", e);
			}
		}

		if (nativeLookAndFeel != null)
			return new ThemeInfo(nativeLookAndFeel.getName(), nativeLookAndFeel.getDescription(), null, false,
					nativeLookAndFeel.getClass().getName(), null);

		return null;
	}

	public static boolean setTheme(ThemeInfo theme, boolean early) {
		if (theme == null)
			return false;

		if (theme.lafClassName() != null) {
			if (theme.lafClassName().equals(UIManager.getLookAndFeel().getClass().getName()))
				return true;

			if (!early)
				FlatAnimatedLafChange.showSnapshot();

			try {
				UIManager.setLookAndFeel(theme.lafClassName());
			} catch (Exception ex) {
				logger.error("Failed to create '{}'.", theme.lafClassName(), ex);
				TaskDialogs.showException(ex);
			}
		} else if (theme.themeFile() != null) {
			if (!early)
				FlatAnimatedLafChange.showSnapshot();

			try {
				if (FilesEx.hasFileExtension(theme.themeFile(), "properties")) {
					FlatLaf.setup(new FlatPropertiesLaf(theme.name(), theme.themeFile().toFile()));
				} else
					FlatLaf.setup(IntelliJTheme.createLaf(new FileInputStream(theme.themeFile().toFile())));
			} catch (Exception ex) {
				logger.error("Failed to load '{}'.", theme.lafClassName(), ex);
				TaskDialogs.showException(ex);
			}
		}

		// update all components
		if (!early) {
			FlatLaf.updateUI();
			FlatAnimatedLafChange.hideSnapshotWithAnimation();
		}

		return true;
	}

	public static boolean setThemeOrDefault(ThemeInfo theme, boolean early) {
		return setTheme(theme, early) || setTheme(getNativeTheme(), early);
	}
}
