package de.george.g3dit.gui.components;

import static j2html.TagCreator.body;
import static j2html.TagCreator.head;
import static j2html.TagCreator.html;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.style;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.Viewport;
import org.fit.cssbox.swingbox.SwingBoxEditorKit;
import org.fit.cssbox.swingbox.util.DefaultAnalyzer;

import com.formdev.flatlaf.ui.FlatUIUtils;

import de.george.g3dit.util.HtmlCreator;
import j2html.tags.DomContent;
import j2html.tags.InlineStaticResource;

public class HtmlEditorPane {
	public final JEditorPane editorPane;
	public final JScrollPane scrollPane;

	private final String cssTemplate;
	private Integer prevCssInputHash = null;
	private String css;

	private HtmlEditorPane(String cssTemplate) {
		this.cssTemplate = cssTemplate;
		editorPane = new JEditorPane();
		scrollPane = new JScrollPane(editorPane);
		editorPane.setEditorKit(new SwingBoxEditorKit(new DefaultAnalyzer() {
			// The size of a JEditorPane with a SwingBoxEditorKit depends on the available
			// space. As a result the pane is always a bit to large for its JScrollPane.
			// Therefore scrollbars are added although they are not really necessary.
			@Override
			public Viewport analyze(DocumentSource docSource, Dimension dim) throws Exception {
				return super.analyze(docSource, new Dimension(scrollPane.getWidth() - 30, scrollPane.getHeight() - 30));
			}

			@Override
			public Viewport update(Dimension dim) throws Exception {
				return super.update(new Dimension(scrollPane.getWidth() - 30, scrollPane.getHeight() - 30));
			}
		}));
		editorPane.setEditable(false);
	}

	public static HtmlEditorPane withCssTemplate(String cssTemplate) {
		return new HtmlEditorPane(cssTemplate);
	}

	public static HtmlEditorPane withCssTemplateFile(String cssTemplateFile) {
		return new HtmlEditorPane(InlineStaticResource.getFileAsString(cssTemplateFile));
	}

	public void setHtml(DomContent... content) {
		String html;
		if (content.length > 0) {
			html = html(head(meta().attr("charset", "UTF-8"), style(getCss())), body(content)).render();
			editorPane.setText(html);
		} else {
			editorPane.setText(null);
		}
		SwingUtilities.invokeLater(() -> {
			editorPane.setCaretPosition(0);
			scrollPane.getVerticalScrollBar().setValue(0);
			scrollPane.getHorizontalScrollBar().setValue(0);
		});
	}

	private String getCss() {
		Color foregroundColor = editorPane.getForeground();
		Color backgroundColor = UIManager.getBoolean("Component.isIntelliJTheme") ? FlatUIUtils.getParentBackground(editorPane)
				: editorPane.getBackground();
		Color linkColor = Optional.ofNullable(UIManager.getColor("Component.linkColor"))
				.orElseGet(() -> UIManager.getColor("EditorPane.selectionBackground"));

		int cssInputHash = Objects.hash(foregroundColor, backgroundColor, linkColor);
		if (prevCssInputHash == null || prevCssInputHash != cssInputHash) {
			prevCssInputHash = cssInputHash;
			css = cssTemplate.replace("$BODY_BACKGROUND_COLOR", HtmlCreator.formatColorAsHex(backgroundColor))
					.replace("$BODY_FOREGROUND_COLOR", HtmlCreator.formatColorAsHex(foregroundColor))
					.replace("$HREF_COLOR", HtmlCreator.formatColorAsHex(linkColor));
		}
		return css;
	}
}
