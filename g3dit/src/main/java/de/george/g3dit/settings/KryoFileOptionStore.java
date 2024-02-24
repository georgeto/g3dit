package de.george.g3dit.settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.george.g3utils.util.FilesEx;
import de.george.g3utils.util.IOUtils;

public class KryoFileOptionStore extends AbstractConcurrentOptionStore {
	private Logger logger = LoggerFactory.getLogger(KryoFileOptionStore.class);

	private final String fileName;

	@SuppressWarnings("unchecked")
	public KryoFileOptionStore(Path file) {
		fileName = FilesEx.getAbsolutePath(file);
		if (Files.exists(file)) {
			try {
				Kryo kryo = IOUtils.getKryo();
				try (Input input = new Input(Files.newInputStream(file))) {
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
							logger.warn("Failed to parse the config file {}.", file);
							break;
						}
					}
				}
			} catch (IOException e) {
				logger.warn("Failed to read the config file {}: {}", file, e.getMessage());
			} catch (KryoException e) {
				logger.warn("Failed to parse the config file {}.", file, e);
			}
		}
	}

	@Override
	public boolean save() {
		synchronized (fileName) {
			try {
				Kryo kryo = IOUtils.getKryo();
				try (Output output = new Output(Files.newOutputStream(Paths.get(fileName)))) {
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
				logger.warn("Failed to save the config file {}: {}", fileName, e.getMessage());
				return false;
			}
		}
	}
}
