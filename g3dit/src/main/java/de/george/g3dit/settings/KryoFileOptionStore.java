package de.george.g3dit.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.george.g3utils.util.IOUtils;

public class KryoFileOptionStore extends AbstractConcurrentOptionStore {
	private Logger logger = LoggerFactory.getLogger(KryoFileOptionStore.class);

	private String fileName;

	@SuppressWarnings("unchecked")
	public KryoFileOptionStore(File file) {
		fileName = file.getAbsolutePath();
		if (file.exists()) {
			try {
				Kryo kryo = IOUtils.getKryo();
				try (Input input = new Input(new FileInputStream(file))) {
					while (!input.eof()) {
						Object size = kryo.readClassAndObject(input);
						// Support reading the legacy format (serialize the ConcurrentHashMap)
						if (size instanceof ConcurrentHashMap) {
							options.putAll((Map<String, Object>) size);
							break;
						} else if (size instanceof Integer) {
							Input optionInput = new Input(input.readBytes((int) size));
							try {
								String key = (String) kryo.readClassAndObject(optionInput);
								Object value = kryo.readClassAndObject(optionInput);
								options.put(key, value);
							} catch (Exception e) {
								logger.warn("Encountered malformed option while loading the config file.", e);
							}
						} else {
							logger.warn("Fehler beim Laden der Konfigurationsdatei.");
							break;
						}
					}
				}
			} catch (IOException e) {
				// Nichts, vielleicht existiert noch keine Config datei
				logger.info("Fehler beim Öffnen der Konfigurationsdatei: {}", e.getMessage());
			} catch (KryoException e) {
				logger.warn("Fehler beim Laden der Konfigurationsdatei.", e);
			}
		}
	}

	@Override
	public boolean save() {
		synchronized (fileName) {
			try {
				Kryo kryo = IOUtils.getKryo();
				try (Output output = new Output(new FileOutputStream(new File(fileName)))) {
					for (Map.Entry<String, Object> option : options.entrySet()) {
						Output optionOutput = new Output(256, -1);
						kryo.writeClassAndObject(optionOutput, option.getKey());
						kryo.writeClassAndObject(optionOutput, option.getValue());
						int optionSize = optionOutput.position();
						kryo.writeClassAndObject(output, Integer.valueOf(optionSize));
						output.writeBytes(optionOutput.getBuffer(), 0, optionSize);
					}
				}
				return true;
			} catch (IOException e) {
				logger.info("Fehler beim Speichern der Konfigurationsdatei: {}", e.getMessage());
				return false;
			}
		}
	}
}
