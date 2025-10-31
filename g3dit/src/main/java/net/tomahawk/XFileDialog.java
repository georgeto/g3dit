package net.tomahawk;

import java.applet.Applet;
import java.awt.Component;
import java.awt.FileDialog;
import java.io.File;
import java.nio.charset.StandardCharsets;

import javax.swing.JFileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// to add patch from Jose
// 1. the new extensionsfilter has been added.
// 2. get should return files directly
//
//
public class XFileDialog {
	private static final Logger logger = LoggerFactory.getLogger(XFileDialog.class);

	// XFileDialog's parent could be a Frame or an Applet,
	// therefore, we have to use Component instead of Frame here.

	private Component parent = null;
	private static JFileChooser failsafe;

	// the five basic functions provided in XFileDialog
	enum Mode {
		LOAD_FILE("Select File", 0),
		LOAD_FOLDER("Select Folder", 0),
		LOAD_FILES("Select Files", 0),
		LOAD_FOLDERS("Select Folders", 0),
		SAVE_FILE("Save File", 1);

		String name;
		int nativeMode;

		Mode(String name, int nativeMode) {
			this.name = name;
			this.nativeMode = nativeMode;
		}
	}

	// Two flags
	private static boolean initOnce = false;
	private static boolean nativeEnabled = false;

	// Tracing flag
	// tracing is always ON by default,
	// if you do not like the trace info,
	// call XFileDialog.setTraceLevel(0);
	//
	private static int traceLevel = 1;

	public native void initWithWindowTitle(String windowtitle);

	public native void initWithJAWT(Component c, String javahome);

	private String nativefilters = "";

	public String getDirectory() {
		if (nativeEnabled) {
			byte[] result = getDirectory2();
			return byteArray2String(result);
		} else {
			return failsafe.getCurrentDirectory().getAbsolutePath();
		}
	}

	public native byte[] getDirectory2();

	public void setDirectory(String path) {
		if (path == null) {
			return;
		}

		if (nativeEnabled) {
			setDirectory2(path);
		} else {
			failsafe.setCurrentDirectory(new File(path));
		}
	}

	public native void setDirectory2(String path);

	// save file dialog
	public String getSaveFile() {

		if (nativeEnabled) {
			// selectmode is determined automatically according to
			// the caller function,
			//
			// Otherwise, it may be misleading as some features are not
			// implemented yet.
			//
			//
			setMode2(FileDialog.SAVE);
			byte[] result = getFile2();
			return byteArray2String(result);
		} else {
			setJavaSelectionMode(Mode.SAVE_FILE);
			if (!showJavaDialog(parent, Mode.SAVE_FILE))
				return null;

			File temp = failsafe.getSelectedFile();
			if (temp != null) {
				return temp.getName();
			} else {
				return null;
			}
		}

	}

	// Always return the relative filename
	public String getFile() {
		if (nativeEnabled) {
			setMode2(FileDialog.LOAD);
			byte[] result = getFile2();
			return byteArray2String(result);
		} else {
			setJavaSelectionMode(Mode.LOAD_FILE);
			if (!showJavaDialog(parent, Mode.LOAD_FILE))
				return null;

			File temp = failsafe.getSelectedFile();
			if (temp != null) {
				return temp.getName();
			} else {
				return null;
			}
		}
	}

	// Return the absolute path of a folder
	public String getFolder() {
		if (nativeEnabled) {
			setMode2(FileDialog.LOAD);
			byte[] result = getFolder2();
			return byteArray2String(result);
		} else {

			setJavaSelectionMode(Mode.LOAD_FOLDER);
			if (!showJavaDialog(parent, Mode.LOAD_FOLDER))
				return null;

			File temp = failsafe.getSelectedFile();
			if (temp != null) {
				return temp.getAbsolutePath();
			} else {
				return null;
			}

		}
	}

	// Return the absolute path of folders
	public String[] getFolders() {
		if (nativeEnabled) {
			setMode2(FileDialog.LOAD);
			byte[][] result = getFolders2();

			String[] strs = null;

			if (result != null) {
				int length = result.length;
				trace("JNI>>: " + length + " folders selected");

				strs = new String[length];
				for (int i = 0; i < length; i++) {
					strs[i] = byteArray2String(result[i]);
				}
			}
			return strs;

		} else {

			setJavaSelectionMode(Mode.LOAD_FOLDERS);
			if (!showJavaDialog(parent, Mode.LOAD_FOLDERS))
				return null;

			File[] temp1 = failsafe.getSelectedFiles();
			if (temp1 == null) {
				return null;
			} else {
				String[] temp2 = new String[temp1.length];
				for (int i = 0; i < temp1.length; i++) {
					temp2[i] = temp1[i].getAbsolutePath();
				}
				return temp2;
			}

		}
	}

	public native byte[] getFile2();

	public native byte[] getFolder2();

	public native byte[][] getFolders2();

	public native void setMode2(int mode);

	public native int getMode2();

	// Always return an array of relatie filenames
	public String[] getFiles() {
		if (nativeEnabled) {
			byte[][] result = getFiles2();
			String[] strs = null;

			if (result != null) {
				String parentDir = getDirectory();
				int trimheading = parentDir.length();

				int length = result.length;
				trace("JNI>>: " + length + " files selected");

				strs = new String[length];
				for (int i = 0; i < length; i++) {
					strs[i] = byteArray2String(result[i]);
					strs[i] = strs[i].substring(trimheading);

				}
			}
			return strs;
		} else {

			setJavaSelectionMode(Mode.LOAD_FILES);
			if (!showJavaDialog(parent, Mode.LOAD_FILES))
				return null;

			File[] temp1 = failsafe.getSelectedFiles();
			if (temp1 == null) {
				return null;
			} else {
				String[] temp2 = new String[temp1.length];
				for (int i = 0; i < temp1.length; i++) {
					temp2[i] = temp1[i].getName();
				}
				return temp2;
			}

		}

	}

	public native byte[][] getFiles2();

	// this method is more flexible than my early implementation
	public void resetFilters() {
		if (nativeEnabled) {
			nativefilters = ""; // reset internal native filters
		} else {
			// reset the filechooser at first
			failsafe.resetChoosableFileFilters();
		}

	}

	public void addFilters(ExtensionsFilter... filters) {

		if (nativeEnabled) {
			String filterStr = ExtensionsFilter.getNativeString(filters);
			nativefilters += filterStr;
			trace("JNI>>: nativefilters: " + nativefilters);
			setFilters2(nativefilters + "|");
			// add "|" at the end
		} else {
			for (ExtensionsFilter extensionsFilter : filters) {
				failsafe.addChoosableFileFilter(extensionsFilter);
			}
		}
	}

	public native void setFilters2(String filterstr);

	public String getTitle() {
		if (nativeEnabled) {
			byte[] result = getTitle2();
			return byteArray2String(result);
		} else {
			return failsafe.getDialogTitle();
		}
	}

	public native byte[] getTitle2();

	public void setTitle(String title) {
		if (title == null) {
			return;
		}
		if (nativeEnabled) {
			setTitle2(title);
		} else {
			failsafe.setDialogTitle(title);
		}
	}

	public native void setTitle2(String title);

	public void setFilename(String filename) {
		if (filename == null) {
			return;
		}
		if (nativeEnabled) {
			setFilename2(filename);
			// TODO
			// else failsafe.setDialogTitle(initialFilename);
		}
	}

	public native void setFilename2(String filename);

	public void setDefaultExtension(String defaultExtension) {
		if (defaultExtension == null) {
			return;
		}
		if (nativeEnabled) {
			setDefaultExtension2(defaultExtension);
			// TODO
			// else failsafe.setDialogTitle(initialFilename);
		}
	}

	public native void setDefaultExtension2(String defaultExtension);

	public void setThumbnail(boolean val) {
		if (nativeEnabled) {
			if (val) {
				setThumbnail2(1);
			} else {
				setThumbnail2(0);
			}
		} else {
			trace("JNI>>: Thumbnail is not supported in JFileChooser");
		}
	}

	public native void setThumbnail2(int val);

	public native void addPlace2(String place);

	public void addPlace(String place) {
		if (nativeEnabled) {
			addPlace2(place);
		} else {
			trace("JNI>>: Places are not supported in JFileChooser");
		}
	}

	public native void resetPlaces2();

	public void resetPlaces() {
		if (nativeEnabled) {
			resetPlaces();
		} else {
			trace("JNI>>: Places are not supported in JFileChooser");
		}
	}

	public static void setTraceLevel(int val) {
		logger.debug("Set XFileDialog traceLevel: {}", val);
		traceLevel = val;

		initClass(); // DLL should be loaded at first

		if (nativeEnabled) {
			setTraceLevel2(val);
		} else {
			trace("JNI>>: setTraceLevel is not supported in JFileChooser");
		}
	}

	public static native void setTraceLevel2(int val);

	public void dispose() {}

	public native void refreshFrame();

	private static void initClass() {

		if (initOnce) {
			// reset failsafe when not native enabled
			if (!nativeEnabled) {
				failsafe.resetChoosableFileFilters();
				failsafe.setDialogTitle(null);
				failsafe.setMultiSelectionEnabled(false);
			}
			return;
		}

		trace("JNI>>: java.library.path:" + System.getProperty("java.library.path"));
		try {
			if (System.getProperty("os.arch").indexOf("64") >= 0) {
				trace("JNI>>: Loading X64 (amd64) DLL");
				System.loadLibrary("xfiledialog64");
			} else {
				trace("JNI>>: Loading X86 32bit DLL");
				System.loadLibrary("xfiledialog");
			}

			nativeEnabled = true;
		} catch (UnsatisfiedLinkError e) {
			if (System.getProperty("os.arch").indexOf("64") >= 0) {
				trace("JNI>>: The xfiledialog64.dll (AMD64) can not be loaded.");
			} else {
				trace("JNI>>: The xfiledialog.dll can not be loaded.");
			}

			trace("JNI>>: JFileChooser will be used instead.");
			nativeEnabled = false;
		}

		if (!nativeEnabled) {
			failsafe = new JFileChooser();
		}

		initOnce = true;
	}

	public XFileDialog(Component parent) {
		this.parent = parent;

		initClass();

		if (parent != null) {
			parent.setIgnoreRepaint(false); // prevent error
		}

		// solution2: find the correct window handele with jawt/awt
		if (nativeEnabled) {
			String javahome = System.getProperty("java.home");
			initWithJAWT(parent, javahome);
			nativefilters = ""; // always reset native filter str in constructor

			trace("JNI>>: Init dialog with JAWT ");

		}

	}

	public XFileDialog(Applet parent) {

		String windowtitle = null;
		this.parent = parent;

		initClass();

		if (parent != null) {
			parent.setIgnoreRepaint(false); // prevent error
		}

		// solution2: find the correct window handele with jawt/awt
		if (nativeEnabled) {
			String javahome = System.getProperty("java.home");
			initWithJAWT(parent, javahome);
			trace("JNI>>: Init dialog with JAWT for Applet/Japplet");
		}

	}

	public XFileDialog(String windowtitle) {
		initClass();
		if (nativeEnabled) {
			initWithWindowTitle(windowtitle);
			trace("JNI>>: Init dialog with Window title: " + windowtitle);
		}
	}

	public String byteArray2String(byte[] data) {
		if (data == null) {
			return null;
		}
		if (data.length == 0) {
			return null;
		}

		String str = null;

		// Windows Unicode is actually UTF-16
		//
		// the best way is :
		// use UTF-16 to decode the byteArray and get the internal
		// String.
		//
		// At the same time, the C code must return a Unicode byteArray
		// i.e. the C code must be compiled with Unicode turned on
		//

		try {
			str = new String(data, StandardCharsets.UTF_16);
			// trace("JNI>>: " + str);
		} catch (Exception e) {
			logger.warn("Failed to convert byte array to string.", e);
		}
		return str;

	}

	public static void trace(String val) {

		if (traceLevel > 0) {
			logger.info(val);
		} else {
			// do nothing
		}

	}

	private void setJavaSelectionMode(Mode mode) {
		switch (mode) {
			case LOAD_FILE, SAVE_FILE -> {
				failsafe.setFileSelectionMode(JFileChooser.FILES_ONLY);
				failsafe.setMultiSelectionEnabled(false);
			}
			case LOAD_FOLDER -> {
				failsafe.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				failsafe.setMultiSelectionEnabled(false);
			}
			case LOAD_FILES -> {
				failsafe.setFileSelectionMode(JFileChooser.FILES_ONLY);
				failsafe.setMultiSelectionEnabled(true);
			}
			case LOAD_FOLDERS -> {
				failsafe.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				failsafe.setMultiSelectionEnabled(true);
			}
		}
	}

	private boolean showJavaDialog(Component component, Mode mode) {
		return switch (mode) {
			case LOAD_FILE, LOAD_FILES, LOAD_FOLDER, LOAD_FOLDERS -> failsafe.showOpenDialog(component);
			case SAVE_FILE -> failsafe.showSaveDialog(component);
		} == JFileChooser.APPROVE_OPTION;
	}

}
