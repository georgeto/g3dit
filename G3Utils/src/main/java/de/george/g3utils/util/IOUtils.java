package de.george.g3utils.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
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

	public static final FileFilter archiveFileFilter = (file) -> (file.getName().endsWith(".node") || file.getName().endsWith(".lrentdat"))
			&& !file.getName().startsWith("{");
	public static final FileFilter lrentdatFileFilter = (file) -> file.getName().endsWith(".lrentdat") && !file.getName().startsWith("{");
	public static final FileFilter nodeFileFilter = (file) -> file.getName().endsWith(".node") && !file.getName().startsWith("{");
	public static final FileFilter tpleFileFilter = (
			file) -> (file.getName().endsWith(".tple") && !file.getName().startsWith("_deleted_"));
	public static final FileFilter secdatFileFilter = (file) -> file.getName().endsWith(".secdat");
	public static final FileFilter meshFilter = (file) -> file.getName().endsWith(".xcmsh") || file.getName().endsWith(".xact")
			|| file.getName().endsWith(".xlmsh");

	/**
	 * Recursively lists all files under the given {@code rootDirectory} that match the
	 * {@code fileFilter}.
	 *
	 * @param rootDirectory
	 * @param fileFilter
	 * @return
	 */
	public static List<File> listFiles(String rootDirectory, FileFilter fileFilter) {
		ArrayList<File> files = new ArrayList<>();

		try (FastFileTreeWalker walker = new FastFileTreeWalker()) {
			FastFileTreeWalker.PathWithAttrs entry = walker.walk(Paths.get(rootDirectory));
			while (entry != null) {
				// Only return files, not directories.
				if (entry.attributes().isRegularFile()) {
					File file = entry.file().toFile();
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
	public static List<File> listFilesPrioritized(Iterable<String> rootDirectories, FileFilter fileFilter) {
		TreeMap<String, File> files = new TreeMap<>();

		for (String rootDirectory : rootDirectories) {
			try (FastFileTreeWalker walker = new FastFileTreeWalker()) {
				Path rootDir = Paths.get(rootDirectory);
				FastFileTreeWalker.PathWithAttrs entry = walker.walk(rootDir);
				while (entry != null) {
					// Only return files, not directories.
					if (entry.attributes().isRegularFile()) {
						File file = entry.file().toFile();
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

	public static Optional<File> findFirstFile(String rootDirectory, FileFilter fileFilter) {
		try (FastFileTreeWalker walker = new FastFileTreeWalker()) {
			FastFileTreeWalker.PathWithAttrs entry = walker.walk(Paths.get(rootDirectory));
			while (entry != null) {
				// Only return files, not directories.
				if (entry.attributes().isRegularFile()) {
					File file = entry.file().toFile();
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

	public static Optional<File> findFirstFile(Iterable<String> rootDirectories, FileFilter fileFilter) {
		for (String rootDirectory : rootDirectories) {
			Optional<File> file = findFirstFile(rootDirectory, fileFilter);
			if (file.isPresent()) {
				return file;
			}
		}
		return Optional.empty();
	}

	public static Callable<List<File>> joinFileCallables(List<Callable<List<File>>> callables) {
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
				logger.warn("Fehler beim Auflisten von Dateien.", e);
				return Stream.empty();
			}
		}).collect(Collectors.toList());
	}

	public static byte[] readFileBytes(File file) throws IOException {
		byte[] fileBytes;
		try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
			fileBytes = new byte[(int) file.length()];
			int readBytes = 0;
			while (readBytes < file.length()) {
				int temp = in.read(fileBytes, readBytes, (int) file.length() - readBytes);
				if (temp == -1) {
					break;
				}
				readBytes += temp;
			}
		}
		return fileBytes;
	}

	public static void writeFileBytes(File file, byte[] bytes) throws IOException {
		try (BufferedOutputStream outf = new BufferedOutputStream(new FileOutputStream(file))) {
			outf.write(bytes);
			outf.flush();
			outf.close();
		}

	}

	public static List<String> readTextFile(File file, Charset charset) throws IOException {
		try (FileInputStream in = new FileInputStream(file)) {
			return IOUtils.readTextFile(in, charset);
		}
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

	public static void writeTextFile(Collection<String> lines, File file, Charset charset) throws IOException {
		try (FileOutputStream out = new FileOutputStream(file)) {
			writeTextFile(lines, out, charset);
		}
	}

	public static void writeTextFile(StringBuilder builder, File file, Charset charset) throws IOException {
		try (FileOutputStream out = new FileOutputStream(file)) {
			writeTextFile(builder, out, charset);
		}
	}

	public static void writeTextFile(Collection<String> lines, OutputStream out, Charset charset) throws IOException {
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
		kryo.register(File.class, new Serializer<File>() {
			@Override
			public void write(Kryo kryo, Output output, File object) {
				output.writeString(object.getPath());
			}

			@Override
			public File read(Kryo kryo, Input input, Class<File> type) {
				return new File(input.readString());
			}
		});
		ImmutableSetSerializer.registerSerializers(kryo);
		return kryo;
	}

	public static Object loadObjectFromFile(File inFile) throws IOException, KryoException {
		try (Input output = new Input(new FileInputStream(inFile))) {
			return getKryo().readClassAndObject(output);
		}

	}

	public static void saveObjectsToFile(File outFile, Object... obj) throws IOException {
		saveObjectToFile(obj, outFile);
	}

	public static void saveObjectToFile(Object obj, File outFile) throws IOException {
		try (Output output = new Output(new FileOutputStream(outFile))) {
			getKryo().writeClassAndObject(output, obj);
		}
	}

	public static String stripExtension(String str) {

		// Handle null case specially.
		if (str == null) {
			return null;
		}

		// Get position of last '.'.
		int pos = str.lastIndexOf(".");

		// If there wasn't any '.' just return the string as is.
		if (pos == -1) {
			return str;
		}

		// Otherwise return the string, up to the dot.
		return str.substring(0, pos);
	}

	public static String changeExtension(String str, String newExt) {
		return stripExtension(str) + "." + newExt;
	}

	public static File changeExtension(File file, String extension) {
		return new File(file.getParentFile(), IOUtils.changeExtension(file.getName(), extension));
	}

	public static String ensureTrailingSlash(String path) {
		return path + (path.endsWith(File.separator) ? "" : File.separator);
	}

	public static String getManifestAttribute(Class<?> clazz, String name) {
		try {
			Attributes.Name attributeName = new Attributes.Name(name);
			Enumeration<URL> resources = clazz.getClassLoader().getResources(JarFile.MANIFEST_NAME);
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				try {
					Manifest manifest = new Manifest(url.openStream());
					if (manifest.getMainAttributes().containsKey(attributeName)) {
						return (String) manifest.getMainAttributes().get(attributeName);
					}
				} catch (Exception e) {
					logger.warn("{}", e);
				}
			}
		} catch (IOException e) {
			logger.warn("{}", e);
		}
		return null;
	}
}
