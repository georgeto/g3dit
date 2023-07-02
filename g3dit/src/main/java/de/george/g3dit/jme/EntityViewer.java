package de.george.g3dit.jme;

import java.nio.file.Path;

import com.jme3.system.JmeContext;

import de.george.g3dit.EditorContext;

public class EntityViewer extends BaseEntityViewer {
	public EntityViewer(EditorContext editorContext) {
		super(editorContext);
	}

	private static EntityViewer instance;
	private JmeAppFrame<EntityViewerApp> viewerFrame;

	public static EntityViewer getInstance(EditorContext editorContext) {
		if (instance == null) {
			instance = new EntityViewer(editorContext);
		}

		return instance;
	}

	@Override
	protected void assureAppRunning() {
		JmeContext context = app.getContext();
		if (context == null || !context.isCreated()) {
			if (viewerFrame == null) {
				viewerFrame = new JmeAppFrame<>();
				viewerFrame.initApp(app);
				viewerFrame.startApp();
				applySettings();
			}

			if (viewerFrame.getFrame() == null) {
				// viewerFrame.setMenuBar(new EntityViewerMainMenu(this, editorContext));
				viewerFrame.createFrame();
			}
		} else {
			viewerFrame.show();
		}
	}

	public void makeScreenshot(Path outFile) {
		appTask(() -> app.makeScreenshot(outFile));
	}
}
