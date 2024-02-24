package de.george.g3utils.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.objenesis.strategy.StdInstantiatorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import de.javakaffee.kryoserializers.guava.ImmutableSetSerializer;

public class IOUtils {
	private static final Logger logger = LoggerFactory.getLogger(IOUtils.class);

	public static final PathFilter archiveFileFilter = (
			file) -> (FilesEx.hasFileExtension(file, "node") || FilesEx.hasFileExtension(file, "lrentdat"))
					&& !FilesEx.getFileName(file).startsWith("{");
	public static final PathFilter lrentdatFileFilter = (file) -> FilesEx.hasFileExtension(file, "lrentdat")
			&& !FilesEx.getFileName(file).startsWith("{");
	public static final PathFilter nodeFileFilter = (file) -> FilesEx.hasFileExtension(file, "node")
			&& !FilesEx.getFileName(file).startsWith("{");
	public static final PathFilter tpleFileFilter = (file) -> isValidTemplateFile(FilesEx.getFileName(file));
	public static final PathFilter secdatFileFilter = (file) -> FilesEx.hasFileExtension(file, "secdat");
	public static final PathFilter meshFilter = (file) -> FilesEx.hasFileExtension(file, "xcmsh") || FilesEx.hasFileExtension(file, "xact")
			|| FilesEx.hasFileExtension(file, "xlmsh");

	public static boolean isValidTemplateFile(String fileName) {
		return fileName.endsWith(".tple") && !fileName.startsWith("_deleted_") && !fileName.startsWith("Testzeug_");
	}

	/**
	 * Recursively lists all files under the given {@code rootDirectory} that match the
	 * {@code PathFilter}.
	 *
	 * @param rootDirectory
	 * @param fileFilter
	 * @return
	 */
	public static List<Path> listFiles(Path rootDirectory, PathFilter fileFilter) {
		ArrayList<Path> files = new ArrayList<>();

		try (FastFileTreeWalker walker = new FastFileTreeWalker()) {
			FastFileTreeWalker.PathWithAttrs entry = walker.walk(rootDirectory);
			while (entry != null) {
				// Only return files, not directories.
				if (entry.attributes().isRegularFile()) {
					Path file = entry.file();
					// Only return the files that match the fileFilter.
					if (fileFilter.accept(file)) {
						files.add(file);
					}
				}

				entry = walker.next();
			}
		}

		return files;
	}

	/**
	 * Recursively lists all files under the given {@code rootDirectories} that match the
	 * {@code fileFilter}. Files are deduplicated by their relative path with decreasing priority
	 * further down the {@code rootDirectories} list.
	 *
	 * @param rootDirectories
	 * @param fileFilter
	 * @return Matching files, sorted by their relative path.
	 */
	public static List<Path> listFilesPrioritized(Iterable<Path> rootDirectories, PathFilter fileFilter) {
		TreeMap<String, Path> files = new TreeMap<>();

		for (Path rootDir : rootDirectories) {
			try (FastFileTreeWalker walker = new FastFileTreeWalker()) {
				FastFileTreeWalker.PathWithAttrs entry = walker.walk(rootDir);
				while (entry != null) {
					// Only return files, not directories.
					if (entry.attributes().isRegularFile()) {
						Path file = entry.file();
						// Only return the files that match the fileFilter.
						if (fileFilter.accept(file)) {
							String relativePath = rootDir.relativize(entry.file()).toString();
							files.putIfAbsent(relativePath, file);
						}
					}

					entry = walker.next();
				}
			}
		}

		return new ArrayList<>(files.values());
	}

	public static Optional<Path> findFirstFile(Path rootDirectory, PathFilter fileFilter) {
		try (FastFileTreeWalker walker = new FastFileTreeWalker()) {
			FastFileTreeWalker.PathWithAttrs entry = walker.walk(rootDirectory);
			while (entry != null) {
				// Only return files, not directories.
				if (entry.attributes().isRegularFile()) {
					Path file = entry.file();
					// Only return the files that match the fileFilter.
					if (fileFilter.accept(file)) {
						return Optional.of(file);
					}
				}

				entry = walker.next();
			}
		}
		return Optional.empty();
	}

	public static Optional<Path> findFirstFile(Iterable<Path> rootDirectories, PathFilter fileFilter) {
		for (Path rootDirectory : rootDirectories) {
			Optional<Path> file = findFirstFile(rootDirectory, fileFilter);
			if (file.isPresent()) {
				return file;
			}
		}
		return Optional.empty();
	}

	public static Callable<List<Path>> joinFileCallables(List<Callable<List<Path>>> callables) {
		if (callables.isEmpty()) {
			return Collections::emptyList;
		}

		if (callables.size() == 1) {
			return callables.get(0);
		}

		return () -> callables.stream().flatMap(c -> {
			try {
				return c.call().stream();
			} catch (Exception e) {
				logger.warn("Error while listing files.", e);
				return Stream.empty();
			}
		}).collect(Collectors.toList());
	}

	public static List<String> readTextFile(Path file, Charset charset) throws IOException {
		return Files.readAllLines(file, charset);
	}

	public static List<String> readTextFile(InputStream in, Charset charset) throws IOException {
		List<String> lines = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, charset))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
		}
		return lines;
	}

	public static void writeTextFile(Iterable<String> lines, Path file, Charset charset) throws IOException {
		Files.write(file, lines, charset);
	}

	public static void writeTextFile(StringBuilder builder, Path file, Charset charset) throws IOException {
		try (OutputStream out = Files.newOutputStream(file)) {
			writeTextFile(builder, out, charset);
		}
	}

	public static void writeTextFile(Iterable<String> lines, OutputStream out, Charset charset) throws IOException {
		try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, charset))) {
			for (String line : lines) {
				writer.println(line);
			}
			writer.flush();
		}
	}

	public static void writeTextFile(StringBuilder builder, OutputStream out, Charset charset) throws IOException {
		try (OutputStreamWriter writer = new OutputStreamWriter(out, charset)) {
			writer.append(builder);
			writer.flush();
		}
	}

	public static void copyToClipboard(String content) {
		Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
		c.setContents(new StringSelection(content), null);
	}

	public static String getClipboardContent() {
		try {
			return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (Exception e) {
			return null;
		}
	}

	public static Kryo getKryo() {
		Kryo kryo = new Kryo();
		// Beschreibung siehe: https://github.com/EsotericSoftware/kryo#object-creation
		kryo.setInstantiatorStrategy(new Kryo.DefaultInstantiatorStrategy(new StdInstantiatorStrategy()));
		kryo.addDefaultSerializer(Path.class, new Serializer<Path>() {
			@Override
			public void write(Kryo kryo, Output output, Path object) {
				output.writeString(object.toString());
			}

			@Override
			public Path read(Kryo kryo, Input input, Class<Path> type) {
				return Paths.get(input.readString());
			}
		});
		ImmutableSetSerializer.registerSerializers(kryo);
		return kryo;
	}

	public static Object loadObjectFromFile(Path inFile) throws IOException, KryoException {
		try (Input output = new Input(Files.newInputStream(inFile))) {
			return getKryo().readClassAndObject(output);
		}

	}

	public static void saveObjectsToFile(Path outFile, Object... obj) throws IOException {
		saveObjectToFile(obj, outFile);
	}

	public static void saveObjectToFile(Object obj, Path outFile) throws IOException {
		try (Output output = new Output(Files.newOutputStream(outFile))) {
			getKryo().writeClassAndObject(output, obj);
		}
	}

	public static Optional<String> getManifestAttribute(Class<?> clazz, String name) {
		try {
			Attributes.Name attributeName = new Attributes.Name(name);
			Enumeration<URL> resources = clazz.getClassLoader().getResources(JarFile.MANIFEST_NAME);
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				try {
					Manifest manifest = new Manifest(url.openStream());
					if (manifest.getMainAttributes().containsKey(attributeName)) {
						return Optional.ofNullable((String) manifest.getMainAttributes().get(attributeName));
					}
				} catch (Exception e) {
					logger.warn("Failed to get manifest attribute.", e);
				}
			}
		} catch (IOException e) {
			logger.warn("Failed to enumerate manifest resources.", e);
		}
		return Optional.empty();
	}
}
