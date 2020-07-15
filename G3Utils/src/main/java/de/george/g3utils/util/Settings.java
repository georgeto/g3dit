package de.george.g3utils.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Settings {

	private Properties props;
	private String fileName;

	public Settings(String fileName) {
		this(new File(fileName));
	}

	public Settings(File file) {
		fileName = file.getAbsolutePath();
		props = new Properties();
		try {
			props.load(new FileInputStream(file));
		} catch (IOException e) {
			// Nichts, vielleicht existiert noch keine Config datei
		}
	}

	public void putString(String key, String value) {
		props.put(key, value);
	}

	public void putInteger(String key, int value) {
		props.put(key, Integer.toString(value));
	}

	public void putBoolean(String key, boolean value) {
		props.put(key, Boolean.toString(value));
	}

	public String getString(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}

	public int getInteger(String key, int defaultValue) {
		String value = props.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		String value = props.getProperty(key);
		if (value == null) {
			return defaultValue;
		}
		try {
			return Boolean.parseBoolean(value);
		} catch (NumberFormatException e) {
			return defaultValue;
		}
	}

	public void remove(String key) {
		props.remove(key);
	}

	public boolean save() {
		try {
			FileWriter writer = new FileWriter(fileName);
			props.store(writer, null);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

}
