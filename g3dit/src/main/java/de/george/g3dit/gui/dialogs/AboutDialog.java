package de.george.g3dit.gui.dialogs;

import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.jidesoft.dialog.BannerPanel;

import de.george.g3dit.Editor;
import de.george.g3dit.gui.components.JLinkLabel;
import de.george.g3utils.gui.SwingUtils;
import net.miginfocom.swing.MigLayout;

public class AboutDialog extends ExtStandardDialog {
	public AboutDialog(Window owner) {
		super(owner, "Über g3dit", true);
		setType(Type.UTILITY);
		setResizable(false);
		setSize(500, 420);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
	}

	@Override
	public JComponent createBannerPanel() {
		BannerPanel bannerPanel = new BannerPanel("g3dit " + Editor.EDITOR_VERSION,
				"(c) 2014-2020 George\n\nCommunity Story Project - g3csp.de", SwingUtils.loadIcon("/res/g3gold.png"));
		return bannerPanel;
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fillx", "[][]"));

		JPanel libPanel = new JPanel(new MigLayout("fill, insets 0"));
		JPanel iconPanel = new JPanel(new MigLayout("fill, insets 0", "", "[][][][]push"));
		mainPanel.add(libPanel, "width 50%, growy");
		mainPanel.add(iconPanel, "width 50%, growy, wrap");

		libPanel.add(SwingUtils.createBoldLabel("Bibliotheken"), "wrap");
		libPanel.add(new JLinkLabel("MigLayout", "http://www.miglayout.com/"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("Simple Validation 1.5", "https://kenai.com/projects/simplevalidation"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("XFileDialog", "http://code.google.com/p/xfiledialog/"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("JIDE Common Layer", "http://www.jidesoft.com/products/oss.htm"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("SwingX", "https://swingx.java.net/"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("JGoodies Looks", "http://www.jgoodies.com/freeware/libraries/looks/"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("JTS Topology Suite", "http://www.vividsolutions.com/jts/JTSHome.htm"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("Guava", "https://github.com/google/guava"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("OneInstance", "https://github.com/kayahr/oneinstance"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("WinRun4J", "https://github.com/poidasmith/winrun4j"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("JBreadcrumb", "https://github.com/ghedlund/jbreadcrumb"), "gapleft 7, wrap");

		iconPanel.add(SwingUtils.createBoldLabel("Icons"), "wrap");
		iconPanel.add(new JLinkLabel("Fugue Icons", "http://p.yusukekamiyamane.com"), "gapleft 7, wrap");
		iconPanel.add(new JLinkLabel("Crystal Clear", "http://everaldo.com"), "gapleft 7, wrap");
		iconPanel.add(new JLinkLabel("Silk Icons", "http://www.famfamfam.com/lab/icons/silk/"), "gapleft 7, wrap");

		return mainPanel;
	}
}
