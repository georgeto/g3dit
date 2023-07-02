/*
 * Copyright (C) 2012 Klaus Reimer <k@ailis.de> See LICENSE.txt for licensing information.
 */

package de.ailis.oneinstance;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.ailis.oneinstance.OneInstanceListener.InstanceAction;
import de.ailis.oneinstance.OneInstanceProtocol.Client;
import de.ailis.oneinstance.OneInstanceProtocol.Server;

/**
 * This thread handles the communication with a single client.
 *
 * @author Klaus Reimer (k@ailis.de)
 */
final class OneInstanceClient implements Runnable {
	/** The logger. */
	private static final Logger LOG = LoggerFactory.getLogger(OneInstanceClient.class);

	/** The application id. */
	private String appId;

	/** The client socket. */
	private Socket socket;

	/**
	 * Constructor.
	 *
	 * @param socket The client socket.
	 * @param appId The application id.
	 */
	OneInstanceClient(final Socket socket, String appId) {
		this.socket = socket;
		this.appId = appId;
	}

	/**
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try {
			try {
				OneInstanceProtocol io = new OneInstanceProtocol(socket);

				// Send the application ID.
				io.write(Server.APP_ID, appId);

				// Read the data from the client
				Path workingDir = Paths.get(io.read(Client.WORKING_DIR));
				String[] args = io.read(Client.ARGS);

				AtomicBoolean outputClosed = new AtomicBoolean(false);
				AtomicInteger exitCode = new AtomicInteger(0);
				LinkedBlockingQueue<char[]> outputQueue = new LinkedBlockingQueue<>();

				// Call event handler
				InstanceAction result = OneInstance.getInstance().fireNewInstance(workingDir, args, new PrintWriter(new Writer() {
					@Override
					public void write(char[] p0, int p1, int p2) throws IOException {
						outputQueue.add(Arrays.copyOfRange(p0, p1, p2));
					}

					@Override
					public void flush() throws IOException {

					}

					@Override
					public void close() throws IOException {
						outputClosed.set(true);
					}
				}), exitCode::set);

				// Send the result
				io.write(Server.RESULT, result.name());

				if (result == InstanceAction.Wait) {
					while (!outputQueue.isEmpty() || !outputClosed.get()) {
						char[] data = outputQueue.poll(100, TimeUnit.MILLISECONDS);
						if (data != null) {
							io.write(Server.STDOUT, new String(data));
						}
					}
				}

				// Send the exit code
				io.write(Server.EXIT_CODE, exitCode.get());

				// Wait for client disconnect.
				socket.shutdownOutput();
				socket.getInputStream().read();
			} finally {
				socket.close();
			}
		} catch (IOException | InterruptedException e) {
			LOG.error(e.toString(), e);
		}
	}
}
