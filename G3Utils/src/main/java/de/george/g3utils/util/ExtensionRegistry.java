package de.george.g3utils.util;

import java.util.Optional;

import com.github.sarxos.winreg.HKey;
import com.github.sarxos.winreg.RegistryException;
import com.github.sarxos.winreg.WindowsRegistry;

public class ExtensionRegistry {
	private static final String SOFTWARE_CLASSES = "Software\\Classes\\";
	private static final String PROG_ID = "\\shell\\open\\command";

	private static ExtensionRegistry instance = new ExtensionRegistry();

	private ExtensionRegistry() {}

	public static ExtensionRegistry getInstance() {
		return instance;
	}

	/**
	 * Erzeugt aus {@code path} eine Kommandozeile, der Form {@code "path" "%1"}. <br>
	 * Sie kann für das Erstellen einer {@code ProgID} benutzt werden.
	 *
	 * @param path Pfad zur ausführbaren Programmdatei
	 * @return Kommandozeile
	 */
	public String createDefaultCommand(String path) {
		return "\"" + path + "\" \"%1\"";
	}

	/**
	 * Gibt die Kommandozeile des, für eine Dateiendung, registrierten Programms zurück.
	 *
	 * @param extension Dateiendung
	 * @return Optional.empty(), wenn kein Programm registriert ist, ansonsten die Kommandozeile mit
	 *         der das Programm eingetragen ist.
	 */
	public Optional<String> getRegisteredProgram(String extension) {
		WindowsRegistry registry = WindowsRegistry.getInstance();
		try {
			String extKey = registry.readString(HKey.HKCU, SOFTWARE_CLASSES + "." + extension, "");

			if (extKey == null) {
				return Optional.empty();
			}

			String regProgram = registry.readString(HKey.HKCU, SOFTWARE_CLASSES + extKey + PROG_ID, "");

			return Optional.ofNullable(regProgram);
		} catch (RegistryException e) {
			return Optional.empty();
		}
	}

	/**
	 * Prüft, ob für eine Dateiendung irgendein Programm registriert ist.
	 *
	 * @param extension Dateiendung
	 * @return true, wenn ein Programm registriert ist
	 */
	public boolean isRegistered(String extension) {
		return getRegisteredProgram(extension).isPresent();
	}

	/**
	 * Prüft, ob für eine Dateiendung das Programm, dessen ausführbare Datei unter {@code path} zu
	 * finden ist, registriert ist.
	 *
	 * @param extension Dateiendung
	 * @param path Pfad zur ausführbaren Programmdatei
	 * @return true, wenn das Programm registriert ist
	 */
	public boolean isRegisteredTo(String extension, String path) {
		Optional<String> program = getRegisteredProgram(extension);
		if (program.isPresent()) {
			return program.get().toLowerCase().contains(path.toLowerCase());
		}
		return false;
	}

	/**
	 * Erstellt in {@code HKEY_CURRENT_USER\Software\Classes} eine {@code ProgID} für das Programm.
	 *
	 * @param program Name des Programms
	 * @param command Kommandozeile, um das Programm auszuführen
	 * @throws RegistryException
	 */
	public void createProgID(String program, String command) throws RegistryException {
		WindowsRegistry registry = WindowsRegistry.getInstance();
		registry.createKey(HKey.HKCU, SOFTWARE_CLASSES + program + PROG_ID);
		registry.writeStringValue(HKey.HKCU, SOFTWARE_CLASSES + program + PROG_ID, "", command);
	}

	/**
	 * Löscht den, durch die Windows 7 Funktion "Standardprogramm auswählen..." erstellten Eintrag
	 * {@code UserChoice}, bei dessen Existenz die Einträge in
	 * {@code HKEY_CURRENT_USER\Software\Classes} ignoriert werden.
	 *
	 * @param extension
	 */
	public void deleteUserChoice(String extension) {
		try {
			WindowsRegistry.getInstance().deleteKey(HKey.HKCU,
					"Software\\Microsoft\\Windows\\CurrentVersion\\Explorer\\FileExts\\." + extension + "\\UserChoice");
		} catch (RegistryException e) {
			// UserChoice existiert nicht
		}
	}

	/**
	 * Registriert ein Programm für eine Dateiendung
	 * <p>
	 * Im ersten Schritt wird {@link ExtensionRegistry#createProgID(String, String)} aufgerufen.
	 * <br>
	 * Im zweiten Schritt wird dann diese ProgID für die Dateiendung registriert. <br>
	 * Im dritten Schritt wird {@link ExtensionRegistry#deleteUserChoice(String)} aufgreufen.
	 *
	 * @param extension Dateiendung
	 * @param program Name des Programms
	 * @param command Kommandozeile, um das Programm auszuführen
	 * @throws RegistryException
	 */
	public void registerExtension(String extension, String program, String command) throws RegistryException {
		createProgID(program, command);

		WindowsRegistry registry = WindowsRegistry.getInstance();
		registry.createKey(HKey.HKCU, SOFTWARE_CLASSES + "." + extension);
		registry.writeStringValue(HKey.HKCU, SOFTWARE_CLASSES + "." + extension, "", program);

		deleteUserChoice(extension);
	}

	/**
	 * Löscht die Zuordnung der Dateiendung zum dafür registrierten Programm.
	 *
	 * @param extension Dateiendung
	 */
	public void unregisterExtension(String extension) {
		try {
			WindowsRegistry.getInstance().deleteKey(HKey.HKCU, SOFTWARE_CLASSES + "." + extension);
			deleteUserChoice(extension);
		} catch (RegistryException e) {
			// Dateiendung ist nicht der Registry eingetragen
		}
	}
}
