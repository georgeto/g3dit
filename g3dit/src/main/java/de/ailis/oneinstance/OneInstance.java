/*
 * Copyright (C) 2012 Klaus Reimer <k@ailis.de> See LICENSE.txt for licensing information.
 */

package de.ailis.oneinstance;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ailis.oneinstance.OneInstanceListener.InstanceAction;
import de.ailis.oneinstance.OneInstanceProtocol.Client;
import de.ailis.oneinstance.OneInstanceProtocol.DataFieldAccessor;
import de.ailis.oneinstance.OneInstanceProtocol.Server;

/**
 * The One-Instance manager. This is a singleton so you must use the
 * {@link OneInstance#getInstance()} method to obtain it. The application must call the
 * {@link OneInstance#register(Class, String[])} method at the beginning of its main method. If this
 * method returns false then the application must exit immediately. The application must also
 * provide an implementation of the {@link OneInstanceListener} interface which must be registered
 * with the {@link OneInstance#addListener(OneInstanceListener)} method. THis listener is called
 * when additional instances of the application are started. The listener can then decide what to do
 * with the new instance and with its command-line arguments. When the application exits then it
 * should call {@link OneInstance#unregister(Class)} but this is not a requirement. This method just
 * closes the server socket (Which will be closed anyway when the application exits) and it removes
 * the port number from the preferences so the next started application does not need to check if
 * this port is still valid. When the application instance was not the first instance then this
 * method does nothing at all.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
public final class OneInstance {
	/** The logger. */
	private static final Logger LOG = LoggerFactory.getLogger(OneInstance.class);

	/** The singleton instance. */
	private static final OneInstance instance = new OneInstance();

	/** The registered listeners. */
	private OneInstanceListener listener = null;

	/** The key name used in the preferences to remember the server port. */
	public static final String PORT_KEY = "oneInstanceServerPort";

	/** The minimum port address. */
	public static final int MIN_PORT = 49152;

	/** The maximum port address. */
	public static final int MAX_PORT = 65535;

	/** The random number generator used to find a free port number. */
	private final Random random = new Random();

	/** The server socket. */
	private OneInstanceServer server;

	/**
	 * Private constructor to prevent instantiation of singleton from outside.
	 */
	private OneInstance() {
		// Empty
	}

	/**
	 * Returns the singleton instance.
	 *
	 * @return The singleton instance.
	 */
	public static OneInstance getInstance() {
		return instance;
	}

	/**
	 * Adds a new listener. This listener is informed about a new application instance which is
	 * about to be started. The listener gets the command-line arguments and can decide what to do
	 * with the new instance by returning true or false.
	 *
	 * @param listener The listener to add. Must not be null.
	 */
	public void setListener(OneInstanceListener listener) {
		this.listener = listener;
	}

	/**
	 * Creates and returns the lock file for the specified class name.
	 *
	 * @param className The name of the main class.
	 * @return The lock file.
	 */
	private Path getLockFile(final String className) {
		return Paths.get(System.getProperty("java.io.tmpdir"), "oneinstance-" + className + ".lock");
	}

	/**
	 * Locks the specified lock file.
	 *
	 * @param lockFile The lock file.
	 * @return The lock or null if no locking could be performed.
	 */
	private FileLock lock(final Path lockFile) {
		try {
			@SuppressWarnings("resource")
			FileChannel channel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
			return channel.lock();
		} catch (IOException e) {
			LOG.warn("Unable to lock the lock file.", e);
			LOG.warn("Trying to run without a lock.");
			return null;
		}
	}

	/**
	 * Releases the specified lock.
	 *
	 * @param fileLock The file lock to release. If null then nothing is done.
	 */
	private void release(final FileLock fileLock) {
		if (fileLock == null) {
			return;
		}
		try {
			fileLock.release();
		} catch (IOException e) {
			LOG.warn("Unable to release lock file: ", e);
		}
	}

	/**
	 * Registers this instance of the application. Returns true if the application is allowed to run
	 * or false when the application must exit immediately.
	 *
	 * @param mainClass The main class of the application. Must not be null. This is used for
	 *            determining the application ID and as the user node key for the preferences.
	 * @param args The command line arguments. They are passed to an already running instance if
	 *            found. Must not be null.
	 * @return True if instance is allowed to start, false if not.
	 */
	public boolean register(Class<?> mainClass, final String[] args, PrintStream outputStream, Consumer<Integer> exitCode) {
		if (mainClass == null) {
			throw new IllegalArgumentException("mainClass must be set");
		}
		if (args == null) {
			throw new IllegalArgumentException("args must be set");
		}

		// Determine application ID from class name.
		String appId = mainClass.getName();

		try {
			// Acquire a lock
			Path lockFile = getLockFile(appId);
			FileLock lock = lock(lockFile);

			try {
				// Get the port which is currently recorded as active.
				Integer port = getActivePort(mainClass);

				// If port is found then we have to validate it
				if (port != null) {
					// Try to connect to the first instance.
					Socket socket = openClientSocket(appId, port);

					// If connection is successful then run as a client
					// (non-first instance)
					if (socket != null) {
						try {
							// Run the client and return the result from the
							// server
							return runClient(socket, args, outputStream, exitCode);
						} finally {
							socket.close();
						}
					}
				}

				// Run the server
				runServer(mainClass);

				// Mark the lock file to be deleted when this instance exits.
				lockFile.toFile().deleteOnExit();

				// Allow this first instance to run.
				return true;
			} finally {
				release(lock);
			}
		} catch (IOException e) {
			// When something went wrong log the error as a warning and then
			// let instance start.
			LOG.warn(e.toString(), e);
			return true;
		}
	}

	/**
	 * Unregisters this instance of the application. If this is the first instance then the server
	 * is closed and the port is removed from the preferences. If this is not the first instance
	 * then this method does nothing. This method should be called when the application exits. But
	 * it is not a requirement. When you don't do this then the port number will stay in the
	 * preferences so on next start of the application this port number must be validated. So by
	 * calling this method on application exit you just save the time for this port validation.
	 *
	 * @param mainClass The main class of the application. Must not be null. This is used as the
	 *            user node key for the preferences.
	 */
	public void unregister(Class<?> mainClass) {
		if (mainClass == null) {
			throw new IllegalArgumentException("mainClass must be set");
		}

		// Nothing to do when no server socket is present
		if (server == null) {
			return;
		}

		// Close the server socket
		server.stop();
		server = null;

		// Remove the port from the preferences
		Preferences prefs = Preferences.userNodeForPackage(mainClass);
		prefs.remove(PORT_KEY);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			LOG.error(e.toString(), e);
		}
	}

	/**
	 * Returns the port which was last recorded as active.
	 *
	 * @param mainClass The main class of the application.
	 * @return The active port address or null if not found.
	 */
	private Integer getActivePort(Class<?> mainClass) {
		Preferences prefs = Preferences.userNodeForPackage(mainClass);
		int port = prefs.getInt(PORT_KEY, -1);
		return port >= MIN_PORT && port <= MAX_PORT ? port : null;
	}

	/**
	 * Remembers an active port number in the preferences.
	 *
	 * @param mainClass The main class of the application.
	 * @param port The port number.
	 */
	private void setActivePort(Class<?> mainClass, int port) {
		Preferences prefs = Preferences.userNodeForPackage(mainClass);
		prefs.putInt(PORT_KEY, port);
		try {
			prefs.flush();
		} catch (BackingStoreException e) {
			LOG.error(e.toString(), e);
		}

	}

	/**
	 * Returns a random port number.
	 *
	 * @return A random port number.
	 */
	private int getRandomPort() {
		return random.nextInt(MAX_PORT - MIN_PORT) + MIN_PORT;
	}

	/**
	 * Opens a client socket to the specified port. If this is successful then the port is returned,
	 * otherwise it is closed and null is returned.
	 *
	 * @param appId The application ID.
	 * @param port The port number to connect to.
	 * @return The client socket or null if no connection to the server was possible or the server
	 *         is not the same application.
	 */
	private Socket openClientSocket(String appId, int port) {
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(InetAddress.getByName(null), port), 50);
			try {
				// Open communication channels
				OneInstanceProtocol io = new OneInstanceProtocol(socket);

				// Read the appId from the server. Use a one second timeout for
				// this just in case some unresponsive application listens on
				// this port.
				socket.setSoTimeout(1000);
				String serverAppId = io.read(OneInstanceProtocol.Server.APP_ID);
				socket.setSoTimeout(0);

				// Abort if server app ID doesn't match (Or there was none at
				// all)
				if (serverAppId == null || !serverAppId.equals(appId)) {
					socket.close();
					socket = null;
				}

				return socket;
			} catch (IOException e) {
				socket.close();
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Runs the client.
	 *
	 * @param socket The client socket.
	 * @param args The command-line arguments.
	 * @param outputStream
	 * @param exitCode
	 * @return True if server accepted the new instance, false if not.
	 * @throws IOException When communication with the server fails.
	 */
	private boolean runClient(Socket socket, String[] args, PrintStream outputStream, Consumer<Integer> exitCode) throws IOException {
		// Send serialized command-line argument list to the server.
		OneInstanceProtocol io = new OneInstanceProtocol(socket);
		io.write(Client.WORKING_DIR, Paths.get(".").toRealPath().toString());
		io.write(Client.ARGS, args);

		// Read response from server
		String response = io.read(Server.RESULT);
		if (response != null) {
			if (response.equals(InstanceAction.Wait.name())) {
				// Receive output
				try {
					while (true) {
						DataFieldAccessor status = io.read(Server.STDOUT, Server.EXIT_CODE);
						if (status.isField(Server.STDOUT)) {
							outputStream.print(status.getValue(Server.STDOUT));
						} else {
							exitCode.accept(status.getValue(Server.EXIT_CODE));
							break;
						}
					}
				} catch (IOException e) {
					LOG.error("Lost connection to the main app instance.", e);
				}

				return false;
			} else {
				// If response is "exit" then don't start new instance. Any other
				// reply will allow the new instance.
				return !response.equals(InstanceAction.Exit.name());
			}
		} else {
			// No response, allow new instance.
			return true;
		}
	}

	/**
	 * Runs the server in a new thread and then returns.
	 *
	 * @param mainClass The main class.
	 */
	private void runServer(Class<?> mainClass) {
		while (true) {
			String appId = mainClass.getName();
			int port = getRandomPort();
			try {
				server = new OneInstanceServer(appId, port);
				setActivePort(mainClass, port);
				server.start();
			} catch (PortAlreadyInUseException e) {
				// Ignored, trying next port.
				continue;
			}
			return;
		}
	}

	/**
	 * Fires the newInstance event. When no listeners are registered then this method always returns
	 * false. If at least one listener accepts the new instance then true is returned.
	 *
	 * @param workingDir The current working directory of the client. Needed if relative pathnames
	 *            are specified on the command line because the server may currently be in a
	 *            different directory than the client.
	 * @param args The command line arguments of the new instance.
	 * @return True if the new instance is allowed to start, false if it must exit.
	 */
	InstanceAction fireNewInstance(final Path workingDir, final String[] args, PrintWriter writer, Consumer<Integer> exitCode) {
		if (listener == null) {
			return InstanceAction.Exit;
		}

		return listener.newInstanceCreated(workingDir, args, writer, exitCode);
	}
}
