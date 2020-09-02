/*
 * Copyright (c) 2009-2012 jMonkeyEngine All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided with
 * the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package de.george.g3dit.jme;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.WindowConstants;

import com.jme3.app.LostFocusBehavior;
import com.jme3.system.AppSettings;
import com.jme3.system.awt.PaintMode;
import com.teamunify.i18n.I;

import de.george.g3utils.gui.SwingUtils;

public class JmeAppFrame<T extends EditorAwareApplication> {
	private ExecutorService executor;
	private MyAwtPanel panel;
	private T app;
	private JFrame frame;
	private JMenuBar menuBar;

	private int width;
	private int height;

	public JmeAppFrame() {
		this(640, 480);
	}

	public JmeAppFrame(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void initApp(T applicaton) {
		AppSettings settings = new AppSettings(true);
		settings.setWidth(width);
		settings.setHeight(height);

		// Support transparent background for screenshots
		settings.setAlphaBits(8);
		settings.setBitsPerPixel(24);
		settings.setCustomRenderer(MyAwtPanelsContext.class);
		settings.setFrameRate(60);
		settings.setSamples(4);

		app = applicaton;
		app.setShowSettings(false);
		app.setLostFocusBehavior(LostFocusBehavior.ThrottleOnLostFocus);
		app.setSettings(settings);
		app.setDisplayFps(false);
		app.setDisplayStatView(false);
	}

	public void startApp() {
		executor = Executors.newSingleThreadExecutor();
		executor.submit(() -> app.start());
		executor.shutdown();
		app.awaitInited();

		MyAwtPanelsContext ctx = (MyAwtPanelsContext) app.getContext();
		// AwtPanel creates RGB8 FrameBuffer, which fucks up transparent screenshots
		panel = ctx.createPanel(PaintMode.Accelerated);
		panel.setPreferredSize(new Dimension(ctx.getSettings().getWidth(), ctx.getSettings().getHeight()));
		ctx.setInputSource(panel);

		app.enqueue(() -> panel.attachTo(true, app.getViewPort(), app.getGuiViewPort()));
	}

	public T getApp() {
		return app;
	}

	public JFrame getFrame() {
		return frame;
	}

	public void setMenuBar(JMenuBar menuBar) {
		this.menuBar = menuBar;
	}

	public void createFrame() {
		frame = new JFrame(I.tr("3D-Ansicht"));
		frame.setIconImage(SwingUtils.getG3Icon());
		frame.setSize(width, height);
		frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {

			@Override
			public void windowClosing(WindowEvent e) {
				app.setPaused(true);
				frame.setVisible(false);
			}

			@Override
			public void windowIconified(WindowEvent e) {
				app.setPaused(true);
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				app.setPaused(false);
			}

			@Override
			public void windowActivated(WindowEvent e) {
				app.gainFocus();
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				app.loseFocus();
			}
		});

		frame.setJMenuBar(menuBar);
		frame.add(panel, BorderLayout.CENTER);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	public void show() {
		app.setPaused(false);
		frame.setVisible(true);
	}
}
