package de.george.g3dit.gui.dialogs;

import java.awt.Window;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

import com.google.common.base.StandardSystemProperty;
import com.jidesoft.dialog.BannerPanel;
import com.teamunify.i18n.I;

import de.george.g3dit.Editor;
import de.george.g3dit.gui.components.JLinkLabel;
import de.george.g3utils.gui.SwingUtils;
import net.miginfocom.swing.MigLayout;

public class AboutDialog extends ExtStandardDialog {
	public AboutDialog(Window owner) {
		super(owner, I.tr("About g3dit"), true);
		setType(Type.UTILITY);
		setResizable(false);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		autosize(500, 0);
	}

	@Override
	public JComponent createBannerPanel() {
		BannerPanel bannerPanel = new BannerPanel("g3dit " + Editor.EDITOR_VERSION,
				"(c) 2014-2021 George\n\nCommunity Story Project - g3csp.de", SwingUtils.loadIcon("/res/g3gold.png"));
		return bannerPanel;
	}

	@Override
	public JComponent createContentPanel() {
		JPanel mainPanel = new JPanel(new MigLayout("fillx"));

		JPanel libPanel = new JPanel(new MigLayout("fill, insets 0"));
		JPanel iconPanel = new JPanel(new MigLayout("fill, insets 0"));
		mainPanel.add(libPanel, "width 50%, aligny top");
		mainPanel.add(iconPanel, "width 50%, aligny top, wrap");

		libPanel.add(SwingUtils.createBoldLabel(I.tr("Libraries")), "wrap");
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
		libPanel.add(new JLinkLabel("JavaInfo", "https://github.com/Bill-Stewart/JavaInfo"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("JBreadcrumb", "https://github.com/ghedlund/jbreadcrumb"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("FlatLaf", "https://github.com/JFormDesigner/FlatLaf"), "gapleft 7, wrap");
		libPanel.add(new JLinkLabel("easy-i18n", "https://github.com/awkay/easy-i18n"), "gapleft 7, wrap");

		iconPanel.add(SwingUtils.createBoldLabel(I.tr("Icons")), "wrap");
		iconPanel.add(new JLinkLabel("Fugue Icons", "http://p.yusukekamiyamane.com"), "gapleft 7, wrap");
		iconPanel.add(new JLinkLabel("Crystal Clear", "http://everaldo.com"), "gapleft 7, wrap");
		iconPanel.add(new JLinkLabel("Silk Icons", "http://www.famfamfam.com/lab/icons/silk/"), "gapleft 7, wrap");
		iconPanel.add(new JLinkLabel("Silk Flags", "http://www.famfamfam.com/lab/icons/flags/"), "gapleft 7, wrap");
		iconPanel.add(new JLinkLabel("Country Flags in SVG", "https://github.com/lipis/flag-icons"), "gapleft 7, wrap");

		String javaInfo = SwingUtils.getMultilineText(String.format("Java: %s", StandardSystemProperty.JAVA_VERSION.value()), String
				.format("JVM: %s (%s)", StandardSystemProperty.JAVA_VM_NAME.value(), StandardSystemProperty.JAVA_VM_VERSION.value()));
		mainPanel.add(new JLabel(javaInfo), "spanx, alignx right, aligny bottom");
		return mainPanel;
	}
}
