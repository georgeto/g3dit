package de.george.g3utils.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.Iterator;

import sun.nio.fs.BasicFileAttributesHolder;

/**
 * Stripped down reimplementation of {@link FileTreeWalker} to get maximum performance.
 *
 * <pre>
 * {@code
 *     try (FastFileTreeWalker walker = new FastFileTreeWalker(options, maxDepth)) {
 *         FastFileTreeWalker.PathWithAttrs ev = walker.walk(top);
 *         while(ev != null) {
 *             process(ev);
 *             ev = walker.next();
 *         };
 *     }
 * }
 * </pre>
 */
public class FastFileTreeWalker implements Closeable {
	private final ArrayDeque<DirectoryNode> stack = new ArrayDeque<>();
	private boolean closed;

	/**
	 * The element on the walking stack corresponding to a directory node.
	 */
	private static class DirectoryNode {
		private final Path dir;
		private final DirectoryStream<Path> stream;
		private final Iterator<Path> iterator;

		private DirectoryNode(Path dir, DirectoryStream<Path> stream) {
			this.dir = dir;
			this.stream = stream;
			iterator = stream.iterator();
		}

		Path directory() {
			return dir;
		}

		DirectoryStream<Path> stream() {
			return stream;
		}

		Iterator<Path> iterator() {
			return iterator;
		}
	}

	public static class PathWithAttrs {
		private final Path file;
		private final BasicFileAttributes attrs;

		private PathWithAttrs(Path file, BasicFileAttributes attrs) {
			this.file = file;
			this.attrs = attrs;
		}

		Path file() {
			return file;
		}

		BasicFileAttributes attributes() {
			return attrs;
		}
	}

	/**
	 * Returns the attributes of the given file, taking into account whether the walk is following
	 * sym links is not. The {@code canUseCached} argument determines whether this method can use
	 * cached attributes.
	 */
	private BasicFileAttributes getAttributes(Path file, boolean canUseCached) throws IOException {
		// if attributes are cached then use them if possible
		if (canUseCached && file instanceof BasicFileAttributesHolder) {
			BasicFileAttributes cached = ((BasicFileAttributesHolder) file).get();
			if (cached != null) {
				return cached;
			}
		}

		// attempt to get attributes of file. If fails and we are following
		// links then a link target might not exist so get attributes of link
		return Files.readAttributes(file, BasicFileAttributes.class);
	}

	/**
	 * Visits the given file, returning the {@code PathWithAttrs} corresponding to that visit. The
	 * {@code ignoreSecurityException} parameter determines whether any SecurityException should be
	 * ignored or not. If a SecurityException is thrown, and is ignored, then this method returns
	 * {@code null} to mean that there is no entry corresponding to a visit to the file. The
	 * {@code canUseCached} parameter determines whether cached attributes for the file can be used
	 * or not.
	 */
	private PathWithAttrs visit(Path entry, boolean canUseCached) {
		try {
			// need the file attributes
			BasicFileAttributes attrs = getAttributes(entry, canUseCached);

			// if file is not a directory
			if (!attrs.isDirectory()) {
				return new PathWithAttrs(entry, attrs);
			}

			// file is a directory, attempt to open it
			DirectoryStream<Path> stream = Files.newDirectoryStream(entry);

			// push a directory node to the stack and return an entry
			stack.push(new DirectoryNode(entry, stream));
			return new PathWithAttrs(entry, attrs);
		} catch (IOException ignore) {
			return null;
		}
	}

	/**
	 * Start walking from the given file.
	 */
	public PathWithAttrs walk(Path file) {
		if (closed) {
			throw new IllegalStateException("Closed");
		}

		return visit(file, false);
	}

	/**
	 * Returns the next PathWithAttrs or {@code null} if there are no more entries or the walker is
	 * closed.
	 */
	public PathWithAttrs next() {
		PathWithAttrs nextEntry = null;
		do {
			// continue iteration of the directory at the top of the stack
			DirectoryNode top = stack.peek();
			if (top == null) {
				return null; // stack is empty, we are done
			}

			Path entry = null;

			// get next entry in the directory
			Iterator<Path> iterator = top.iterator();
			try {
				if (iterator.hasNext()) {
					entry = iterator.next();
				}
			} catch (DirectoryIteratorException ignore) {
			}

			// no next entry so close and pop directory, creating corresponding event
			if (entry == null) {
				try {
					top.stream().close();
				} catch (IOException ignore) {
				}
				stack.pop();
			} else {
				// visit the entry
				nextEntry = visit(entry, true);
			}

		} while (nextEntry == null);

		return nextEntry;
	}

	/**
	 * Pops the directory node that is the current top of the stack. This method is a no-op if the
	 * stack is empty or the walker is closed.
	 */
	private void pop() {
		if (!stack.isEmpty()) {
			DirectoryNode node = stack.pop();
			try {
				node.stream().close();
			} catch (IOException ignore) {
			}
		}
	}

	/**
	 * Returns {@code true} if the walker is open.
	 */
	public boolean isOpen() {
		return !closed;
	}

	/**
	 * Closes/pops all directories on the stack.
	 */
	@Override
	public void close() {
		if (!closed) {
			while (!stack.isEmpty()) {
				pop();
			}
			closed = true;
		}
	}
}
