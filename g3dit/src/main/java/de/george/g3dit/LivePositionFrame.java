package de.george.g3dit;

import java.awt.Container;
import java.awt.Window;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ezware.dialog.task.TaskDialogs;
import com.google.common.base.Throwables;
import com.tulskiy.keymaster.common.Provider;

import de.george.g3dit.cache.TemplateCache.TemplateCacheEntry;
import de.george.g3dit.gui.components.EnableGroup;
import de.george.g3dit.gui.components.FloatSpinner;
import de.george.g3dit.gui.dialogs.DisplayTextDialog;
import de.george.g3dit.gui.dialogs.NavigateTemplateDialog;
import de.george.g3dit.gui.theme.LayoutUtils;
import de.george.g3dit.rpc.IpcHelper;
import de.george.g3dit.rpc.IpcUtil;
import de.george.g3dit.rpc.MonotonicallyOrderedIpc;
import de.george.g3dit.rpc.proto.DTC;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.EntityRequest;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.EntityRequest.IdentifierCase;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.Position;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.ResponseContainer;
import de.george.g3dit.rpc.proto.G3RemoteControlProtos.SpawnRequest;
import de.george.g3dit.rpc.proto.RemoteProperty;
import de.george.g3dit.rpc.zmq.ResponseCallback;
import de.george.g3dit.tab.EditorTab.EditorTabType;
import de.george.g3dit.tab.archive.EditorArchiveTab;
import de.george.g3dit.util.AbstractDialogFileWorker;
import de.george.g3dit.util.Icons;
import de.george.g3utils.gui.SwingUtils;
import de.george.g3utils.gui.UndoableTextField;
import de.george.g3utils.structure.GuidUtil;
import de.george.g3utils.structure.bCEulerAngles;
import de.george.g3utils.structure.bCMatrix;
import de.george.g3utils.structure.bCVector;
import de.george.g3utils.util.IOUtils;
import de.george.g3utils.util.Misc;
import de.george.lrentnode.archive.ArchiveFile;
import de.george.lrentnode.archive.eCEntity;
import de.george.lrentnode.archive.lrentdat.LrentdatEntity;
import de.george.lrentnode.archive.node.NodeEntity;
import de.george.lrentnode.iterator.ArchiveFileIterator;
import de.george.lrentnode.util.FileUtil;
import net.miginfocom.swing.MigLayout;

public class LivePositionFrame extends JFrame {
	private static final String PC_HERO = "PC_Hero";
	private static final String PC_CAMERA = "PC_Camera";

	private static final KeyStroke KEYSTROKE_CAPTURE_1 = KeyStroke.getKeyStroke(KeyEvent.VK_PERIOD,
			InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);
	private static final KeyStroke KEYSTROKE_CAPTURE_2 = KeyStroke.getKeyStroke(KeyEvent.VK_X,
			InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK);

	private static final Logger logger = LoggerFactory.getLogger(LivePositionFrame.class);

	private static final float DEFAULT_STEP_ROTATION = 1.0f;
	private static final float DEFAULT_STEP_POSITION = 2.0f;
	private static final float DEFAULT_STEP_SCALE = 0.01f;

	private EditorContext ctx;

	private MonotonicallyOrderedIpc ipcUpdatePosition = new MonotonicallyOrderedIpc();
	private ScheduledExecutorService ex = Executors.newSingleThreadScheduledExecutor();

	private UndoableTextField tfSearchField;

	private JLabel lblPosition;

	private FloatSpinner spX, spY, spZ, spPitch, spYaw, spRoll, spScaleX, spScaleY, spScaleZ;

	private JRadioButton rbName;
	private JRadioButton rbGuid;

	private JRadioButtonMenuItem rbSpawnAtHero;
	private JRadioButtonMenuItem rbSpawnAtCamera;
	private JRadioButtonMenuItem rbSpawnAtCurrent;

	private volatile boolean isValidSearchActive;
	private volatile EnableGroup ifValidSearchResult;

	private bCVector translation;
	private bCEulerAngles rotation;
	private bCVector scale;
	private String name;
	private String guid;

	private final Object searchLock = new Object();
	private volatile String searchString;
	private volatile EntityRequest.IdentifierCase searchMode;
	private volatile boolean updateSpinners;

	private DisplayTextDialog logDialog;

	public LivePositionFrame(EditorContext ctx) {
		this.ctx = ctx;
		setTitle("Live Entity Position");
		setIconImage(SwingUtils.getG3Icon());
		this.setSize(450, 450);
		setResizable(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		logDialog = new DisplayTextDialog("LivePosition-Log", "", this, false);
		logDialog.setDefaultCloseOperation(HIDE_ON_CLOSE);
		logDialog.initialize();
		createContentPanel();
	}

	public void createContentPanel() {
		setLayout(new MigLayout("fillx", "[fill, grow][fill, grow][fill, grow]10[]", "[][][][][][][][][][][][]push"));

		Container mainPanel = getContentPane();

		ifValidSearchResult = new EnableGroup();

		createSearchSection(mainPanel);
		createCurrentPositionSection(mainPanel);
		createChangePositionSection(mainPanel);

		ifValidSearchResult.setEnabled(false);

		ex.scheduleAtFixedRate(() -> {
			try {
				updatePosition();
			} catch (Exception e) {
				logger.warn("Fehler in updatePosition().", e);
			}
		}, 0, 250, TimeUnit.MILLISECONDS);

		setDefaultCancelAction(SwingUtils.createAction(this::dispose));
	}

	private void createSearchSection(Container mainPanel) {
		tfSearchField = SwingUtils.createUndoTF();

		JButton btnErase = new JButton(Icons.getImageIcon(Icons.Action.ERASE));
		btnErase.setFocusable(false);
		btnErase.setToolTipText("Suche leeren");
		btnErase.addActionListener(e -> tfSearchField.setText(null));

		Action searchAction = SwingUtils.createAction("Suchen", Icons.getImageIcon(Icons.Action.FIND), () -> searchEntity(null));
		setDefaultAction(searchAction);
		JButton btnSearch = new JButton(searchAction);
		btnSearch.setFocusable(false);

		mainPanel.add(tfSearchField, "split 3, width 100%, spanx 4");
		mainPanel.add(btnSearch, "height 23!");
		mainPanel.add(btnErase, "width 23!, height 23!, wrap");

		rbName = new JRadioButton("Name", true);
		rbName.setFocusable(false);
		rbGuid = new JRadioButton("Guid");
		rbGuid.setFocusable(false);
		SwingUtils.createButtonGroup(rbName, rbGuid);

		JButton btnSearchHero = new JButton("Held");
		btnSearchHero.addActionListener(e -> searchForName(PC_HERO));
		JButton btnSearchCamera = new JButton("Kamera");
		btnSearchCamera.addActionListener(e -> searchForName(PC_CAMERA));

		JCheckBox cbFocus = new JCheckBox("Fokussierte Entity");
		JCheckBox cbEditor = new JCheckBox("Editor-Entity");
		cbEditor.setToolTipText(SwingUtils.getMultilineText("Zeigt im Ingame-Editor ausgewählte Entity an.",
				"Besonders nützlich, wenn man ingame den 'Pick Mode' zur Entity-Auswahl verwendet."));

		EnableGroup groupFocus = EnableGroup.create(tfSearchField, btnSearch, btnErase, rbName, rbGuid).add(searchAction);
		EnableGroup groupEditor = EnableGroup.create(groupFocus);
		groupFocus.add(cbEditor);
		groupEditor.add(cbFocus);
		cbFocus.addActionListener(e -> triggerSearchModeCheckbox(cbFocus, groupFocus, IdentifierCase.FOCUS));
		cbEditor.addActionListener(e -> triggerSearchModeCheckbox(cbEditor, groupEditor, IdentifierCase.EDITOR));

		mainPanel.add(rbName, "split 6, spanx 4");
		mainPanel.add(rbGuid, "gapleft 7");
		mainPanel.add(btnSearchHero, "gapleft push");
		mainPanel.add(btnSearchCamera, "gapleft 7");
		mainPanel.add(cbFocus, "id cbFocus, gapleft 15, wrap");
		mainPanel.add(cbEditor, "pos cbFocus.x cbFocus.y2");
	}

	private void triggerSearchModeCheckbox(JCheckBox cb, EnableGroup group, IdentifierCase searchModeNoParam) {
		if (cb.isSelected()) {
			searchEntity(searchModeNoParam);
		} else if (ifValidSearchResult.isEnabled()) {
			synchronized (searchLock) {
				searchString = GuidUtil.parseGuid(guid);
				searchMode = IdentifierCase.GUID;
			}
		} else {
			isValidSearchActive = false;
		}
		group.setEnabled(!cb.isSelected());
	}

	private void createCurrentPositionSection(Container mainPanel) {
		mainPanel.add(SwingUtils.createBoldLabel("Aktuelle Position"), "spanx 3, wrap");
		lblPosition = new JLabel("-- Bitte Suche starten --");
		int height = SwingUtils.getMultilabelHeight(5);
		mainPanel.add(lblPosition, "gapleft 7, spanx 3, height " + height + "!");

		JPanel btnPanel = new JPanel(new MigLayout("ins 0"));

		JButton btnCopyInfo = new JButton(Icons.getImageIcon(Icons.Misc.COOKIE_CHOCO_SPRINKLES));
		btnCopyInfo.setToolTipText("Name, Guid und Position in Zwischenablage kopieren");
		btnPanel.add(btnCopyInfo, LayoutUtils.sqrBtn("sg btn"));
		btnCopyInfo.addActionListener(e -> IOUtils.copyToClipboard(
				"Name: " + name + "\nGuid: " + GuidUtil.parseGuid(guid) + "\n" + Misc.positionToString(translation, rotation, scale)));

		JButton btnCopyPosition = new JButton(Icons.getImageIcon(Icons.Misc.COOKIE));
		btnCopyPosition.setToolTipText("Position in Zwischenablage kopieren");
		btnPanel.add(btnCopyPosition, "sg btn, wrap");
		btnCopyPosition.addActionListener(e -> IOUtils.copyToClipboard(Misc.positionToString(translation, rotation, scale)));

		JButton btnCopyGuid = new JButton(Icons.getImageIcon(Icons.Misc.COOKIE_CHOCO));
		btnCopyGuid.setToolTipText("Guid in Zwischenablage kopieren");
		btnPanel.add(btnCopyGuid, "sg btn");
		btnCopyGuid.addActionListener(e -> IOUtils.copyToClipboard(GuidUtil.parseGuid(guid)));

		JButton btnCopyMarvinPosition = new JButton(Icons.getImageIcon(Icons.Misc.COOKIE_BITE));
		btnCopyMarvinPosition.setToolTipText("Position in Zwischenablage kopieren (gerundet für Konsole)");
		btnPanel.add(btnCopyMarvinPosition, "sg btn, wrap");
		btnCopyMarvinPosition.addActionListener(e -> IOUtils.copyToClipboard(translation.toMarvinString()));

		JButton btnShowEntity = new JButton(Icons.getImageIcon(Icons.Action.FIND));
		btnShowEntity.setToolTipText("Entity öffnen");
		btnPanel.add(btnShowEntity, "sg btn");
		btnShowEntity.addActionListener(e -> showEntity(false));

		JButton btnApplyToEntity = new JButton(Icons.getImageIcon(Icons.Document.EXPORT));
		btnApplyToEntity.setToolTipText("Position für Entity in Weltdaten übernehmen");
		btnPanel.add(btnApplyToEntity, "sg btn, wrap");
		btnApplyToEntity.addActionListener(e -> showEntity(true));

		JButton btnGotoEntity = new JButton(Icons.getImageIcon(Icons.Misc.GEOLOCATION));
		btnGotoEntity.setToolTipText("Teleportiert den Spieler zur Entity.");
		btnPanel.add(btnGotoEntity, "sg btn");
		btnGotoEntity.addActionListener(e -> IpcUtil.gotoGuid(guid));

		JButton btnDumpEntity = new JButton(Icons.getImageIcon(Icons.IO.IMPORT));
		btnDumpEntity.setToolTipText("Entity dumpen");
		btnPanel.add(btnDumpEntity, "sg btn");
		btnDumpEntity.addActionListener(e -> {
			RemoteProperty.getEntity(guid).whenComplete((entity, exception) -> SwingUtilities.invokeLater(() -> {
				if (exception != null) {
					log(Throwables.getStackTraceAsString(exception));
					return;
				}

				// Entity
				ArchiveFile file = null;
				if (entity instanceof LrentdatEntity) {
					file = FileUtil.createEmptyLrentdat();
				} else if (entity instanceof NodeEntity) {
					file = FileUtil.createEmptyNode();
				}

				if (file != null) {
					file.getGraph().attachChild(entity);
					ctx.getEditor().openArchive(file, true);
				}
			}));
		});

		mainPanel.add(btnPanel, "align center, wrap");

		ifValidSearchResult.add(btnCopyInfo, btnCopyGuid, btnShowEntity, btnCopyPosition, btnCopyMarvinPosition, btnApplyToEntity,
				btnGotoEntity, btnDumpEntity);
	}

	private void createChangePositionSection(Container mainPanel) {
		mainPanel.add(SwingUtils.createBoldLabel("Position bearbeiten"), "gaptop 10, spanx 3");
		mainPanel.add(SwingUtils.createBoldLabel("+/-"), "wrap");

		mainPanel.add(new JLabel("x-pos"), "gapleft 7");
		mainPanel.add(new JLabel("y-pos"));
		mainPanel.add(new JLabel("z-pos"));
		mainPanel.add(new JLabel("Position"), "gapleft 7, wrap");

		spX = new FloatSpinner();
		spX.addSpinActionListener(e -> applyPosition());
		mainPanel.add(spX, "wmin 50, gapleft 7, sg posin");
		spY = new FloatSpinner();
		spY.addSpinActionListener(e -> applyPosition());
		mainPanel.add(spY, "wmin 50, sg posin");
		spZ = new FloatSpinner();
		spZ.addSpinActionListener(e -> applyPosition());
		mainPanel.add(spZ, "wmin 50, sg posin");
		JTextField tfStepPos = SwingUtils.createUndoTF(Misc.formatFloat(DEFAULT_STEP_POSITION));
		mainPanel.add(tfStepPos, "gapleft 7, width 50!, sg step, wrap");
		FloatSpinner.bindStepSizeEditor(tfStepPos, spX, spY, spZ);

		mainPanel.add(new JLabel("pitch"), "gapleft 7");
		mainPanel.add(new JLabel("yaw"));
		mainPanel.add(new JLabel("roll"));
		mainPanel.add(new JLabel("Rotation"), "gapleft 7, wrap");

		spPitch = new FloatSpinner();
		spPitch.addSpinActionListener(e -> applyPosition());
		mainPanel.add(spPitch, "wmin 50, gapleft 7, sg posin");
		spYaw = new FloatSpinner();
		spYaw.addSpinActionListener(e -> applyPosition());
		mainPanel.add(spYaw, "wmin 50, sg posin");
		spRoll = new FloatSpinner();
		spRoll.addSpinActionListener(e -> applyPosition());
		mainPanel.add(spRoll, "wmin 50, sg posin");
		JTextField tfStepRot = SwingUtils.createUndoTF(Misc.formatFloat(DEFAULT_STEP_ROTATION));
		mainPanel.add(tfStepRot, "gapleft 7, sg step, wrap");
		FloatSpinner.bindStepSizeEditor(tfStepRot, spPitch, spYaw, spRoll);

		mainPanel.add(new JLabel("x-scale"), "gapleft 7");
		mainPanel.add(new JLabel("y-scale"));
		mainPanel.add(new JLabel("z-scale"));
		mainPanel.add(new JLabel("Scale"), "gapleft 7, wrap");

		spScaleX = new FloatSpinner();
		spScaleX.addSpinActionListener(e -> applyPosition());
		mainPanel.add(spScaleX, "wmin 50, gapleft 7, sg posin");
		spScaleY = new FloatSpinner();
		spScaleY.addSpinActionListener(e -> applyPosition());
		mainPanel.add(spScaleY, "wmin 50, sg posin");
		spScaleZ = new FloatSpinner();
		spScaleZ.addSpinActionListener(e -> applyPosition());
		mainPanel.add(spScaleZ, "wmin 50, sg posin");
		JTextField tfStepScale = SwingUtils.createUndoTF(Misc.formatFloat(DEFAULT_STEP_SCALE));
		mainPanel.add(tfStepScale, "gapleft 7, sg step, wrap");
		FloatSpinner.bindStepSizeEditor(tfStepScale, spScaleX, spScaleY, spScaleZ);

		JButton btnApply = new JButton("Ändern");
		btnApply.addActionListener(e -> applyPosition());
		mainPanel.add(btnApply, "gapleft 7");
		ifValidSearchResult.add(btnApply);

		JButton btnLoad = new JButton("Aktuelle laden");
		btnLoad.addActionListener(e -> updateSpinners());
		mainPanel.add(btnLoad, "");
		ifValidSearchResult.add(btnLoad);

		JButton btnPastePosition = new JButton(Icons.getImageIcon(Icons.IO.IMPORT));
		btnPastePosition.setToolTipText("Position aus Zwischenablage verwenden");
		mainPanel.add(btnPastePosition, LayoutUtils.sqrBtn("split 3"));
		btnPastePosition.addActionListener(e -> handlePastePosition());

		JButton btnPutToGround = new JButton(Icons.getImageIcon(Icons.Arrow.DOWN));
		btnPutToGround.setToolTipText("Auf Bodenhöhe verschieben");
		mainPanel.add(btnPutToGround, LayoutUtils.sqrBtn());
		btnPutToGround.addActionListener(e -> handlePutToGround());
		ifValidSearchResult.add(btnPutToGround);

		JCheckBox cbCapture = new JCheckBox("Capture");
		cbCapture.setToolTipText(
				String.format("Aktiviere globalen Hotkey (%s oder %s) zur Erfassung der aktuellen Position im LivePosition-Log.",
						SwingUtils.getKeyStrokeText(KEYSTROKE_CAPTURE_1), SwingUtils.getKeyStrokeText(KEYSTROKE_CAPTURE_2)));
		cbCapture.addActionListener(e -> {
			Provider hotKeyProvider = ctx.getEditor().getHotKeyProvider();
			if (cbCapture.isSelected()) {
				hotKeyProvider.register(KEYSTROKE_CAPTURE_1, h -> capturePosition());
				hotKeyProvider.register(KEYSTROKE_CAPTURE_2, h -> capturePosition());
			} else {
				hotKeyProvider.unregister(KEYSTROKE_CAPTURE_1);
				hotKeyProvider.unregister(KEYSTROKE_CAPTURE_2);
			}
		});
		mainPanel.add(cbCapture, "gapafter push");

		JButton btnSpawn = new JButton(Icons.getImageIcon(Icons.Misc.WAND_MAGIC));
		btnSpawn.setToolTipText("Template spawnen (Position über Kontextmenü einstellbar)");
		mainPanel.add(btnSpawn, LayoutUtils.sqrBtn("split 2, alignx center"));
		btnSpawn.addActionListener(e -> spawnTemplate());

		JPopupMenu pmSpawn = new JPopupMenu();
		rbSpawnAtHero = (JRadioButtonMenuItem) pmSpawn.add(new JRadioButtonMenuItem("Held", true));
		rbSpawnAtCamera = (JRadioButtonMenuItem) pmSpawn.add(new JRadioButtonMenuItem("Kamera"));
		rbSpawnAtCurrent = (JRadioButtonMenuItem) pmSpawn.add(new JRadioButtonMenuItem("Aktuelle Position"));
		SwingUtils.createButtonGroup(rbSpawnAtHero, rbSpawnAtCamera, rbSpawnAtCurrent);
		btnSpawn.setComponentPopupMenu(pmSpawn);

		JButton btnShowLog = new JButton(Icons.getImageIcon(Icons.Data.LOG));
		btnShowLog.setToolTipText("Log anzeigen");
		mainPanel.add(btnShowLog, LayoutUtils.sqrBtn());
		btnShowLog.addActionListener(e -> showLog());

		SwingUtils.addKeyStroke(btnLoad, JComponent.WHEN_IN_FOCUSED_WINDOW, "Refresh",
				KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK), this::updateSpinners, true);

		SwingUtils.addKeyStroke(btnPutToGround, JComponent.WHEN_IN_FOCUSED_WINDOW, "PutToGround",
				KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_DOWN_MASK), this::handlePutToGround, true);

		SwingUtils.addKeyStroke(btnSpawn, JComponent.WHEN_IN_FOCUSED_WINDOW, "SpawnTemplate",
				KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK), this::spawnTemplate, true);

		SwingUtils.addKeyStroke(btnShowLog, JComponent.WHEN_IN_FOCUSED_WINDOW, "ShowLog",
				KeyStroke.getKeyStroke(KeyEvent.VK_L, InputEvent.CTRL_DOWN_MASK), this::showLog, true);
	}

	@Override
	public void dispose() {
		super.dispose();
		ex.shutdownNow();
	}

	private void setDefaultCancelAction(Action defaultCancleAction) {
		getRootPane().registerKeyboardAction(defaultCancleAction, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	private void setDefaultAction(Action defaultAction) {
		getRootPane().registerKeyboardAction(defaultAction, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
	}

	private void log(String message, Object... args) {
		logDialog.append(String.format(message + "\n", args));
	}

	private void searchEntity(IdentifierCase searchModeNoParam) {
		synchronized (searchLock) {
			if (searchModeNoParam != null) {
				searchMode = searchModeNoParam;
			} else if (rbName.isSelected()) {
				searchString = Optional.of(tfSearchField.getText()).filter(e -> !e.isEmpty()).orElse(null);
				searchMode = IdentifierCase.NAME;
			} else {
				searchString = GuidUtil.parseGuid(tfSearchField.getText());
				searchMode = IdentifierCase.GUID;
			}
			updateSpinners = true;
		}

		if (searchString == null && searchMode != IdentifierCase.FOCUS && searchMode != IdentifierCase.EDITOR) {
			if (searchMode == IdentifierCase.GUID) {
				lblPosition.setText("Guid ist ungültig");
			} else if (searchMode == IdentifierCase.NAME) {
				lblPosition.setText("Name darf nicht leer sein.");
			}
			isValidSearchActive = false;
			ifValidSearchResult.setEnabled(false);
		} else {
			isValidSearchActive = true;
		}
	}

	private void searchForName(String name) {
		synchronized (searchLock) {
			searchString = name;
			searchMode = IdentifierCase.NAME;
			updateSpinners = true;
			isValidSearchActive = true;
		}
	}

	private void searchForGuid(String guid) {
		synchronized (searchLock) {
			searchString = guid;
			searchMode = IdentifierCase.GUID;
			updateSpinners = true;
			isValidSearchActive = true;
		}
	}

	private void applyPosition() {
		if (!ifValidSearchResult.isEnabled()) {
			return;
		}

		bCEulerAngles rotation = bCEulerAngles.fromDegree(spYaw.getVal(), spPitch.getVal(), spRoll.getVal());
		bCVector scaling = new bCVector(spScaleX.getVal(), spScaleY.getVal(), spScaleZ.getVal());
		bCVector translation = new bCVector(spX.getVal(), spY.getVal(), spZ.getVal());

		IpcHelper.getIpc().sendRequest(
				EntityRequest.newBuilder().setGuid(guid).setMoveto(DTC.convert(rotation, scaling, translation)).build(), null, null);
	}

	private void updatePosition() {
		EntityRequest request;
		synchronized (searchLock) {
			if (!isValidSearchActive) {
				return;
			}

			request = switch (searchMode) {
				case NAME -> EntityRequest.newBuilder().setName(searchString).build();
				case GUID -> EntityRequest.newBuilder().setGuid(GuidUtil.hexToGuidText(searchString)).build();
				case FOCUS -> EntityRequest.newBuilder().setFocus(true).build();
				case EDITOR -> EntityRequest.newBuilder().setEditor(true).build();
				default -> throw new IllegalStateException();
			};
		}

		ipcUpdatePosition.sendRequest(request, (s, rc, ud) -> {
			SwingUtilities.invokeLater(() -> {
				if (!isValidSearchActive) {
					return;
				}

				if (s == ResponseCallback.Status.Timeout) {
					lblPosition.setText("-- Gothic 3 nicht erreichbar -- ");
					ifValidSearchResult.setEnabled(false);
				} else if (rc.getStatus() == ResponseContainer.Status.FAILED) {
					lblPosition.setText("-- Entity nicht gefunden --");
					ifValidSearchResult.setEnabled(false);
				} else {
					Position position = rc.getEntityResponse().getPosition();
					translation = DTC.convert(position.getTranslation());
					rotation = DTC.convert(position.getRotation());
					scale = DTC.convert(position.getScale());
					name = rc.getEntityResponse().getName();
					boolean guidChanged = guid == null || !guid.equals(rc.getEntityResponse().getGuid());
					guid = rc.getEntityResponse().getGuid();

					if (updateSpinners || guidChanged) {
						updateSpinners();
						updateSpinners = false;
					}

					lblPosition.setText("<html>Name: " + name + "<br>Guid: " + GuidUtil.parseGuid(guid) + "<br>"
							+ Misc.positionToPrettyString(translation, rotation, scale).replaceAll("\n", "<br>") + "</html>");
					ifValidSearchResult.setEnabled(true);
				}
			});
		});
	}

	private void updateSpinners() {
		updateSpinners(translation, rotation, scale);
	}

	private void updateSpinners(bCVector t, bCEulerAngles r, bCVector s) {
		spX.setVal(t.getX());
		spY.setVal(t.getY());
		spZ.setVal(t.getZ());
		spPitch.setVal(r.getPitchDeg());
		spYaw.setVal(r.getYawDeg());
		spRoll.setVal(r.getRollDeg());
		spScaleX.setVal(s.getX());
		spScaleY.setVal(s.getY());
		spScaleZ.setVal(s.getZ());
	}

	private void handlePastePosition() {
		String clipboardContent = IOUtils.getClipboardContent();
		bCVector position = Misc.stringToPosition(clipboardContent);
		if (position != null) {
			updateSpinners(position, Misc.stringToRotation(clipboardContent), Misc.stringToScaling(clipboardContent));
		} else {
			TaskDialogs.inform(this, "Zwischenablage enthält keine Positionsdaten", null);
		}
	}

	private void handlePutToGround() {
		EntityRequest request = EntityRequest.newBuilder().setGuid(guid).setPutToGround(true).build();
		IpcHelper.getIpc().sendRequest(request, null, null);
	}

	private void capturePosition() {
		if (translation != null) {
			log(translation.toString());
		}
	}

	private void spawnTemplate() {
		NavigateTemplateDialog dialog = new NavigateTemplateDialog(this, ctx, TemplateCacheEntry::isHelperParent);
		if (dialog.openAndWasSuccessful()) {
			for (TemplateCacheEntry tple : dialog.getSelectedEntries()) {
				SpawnRequest.Builder request = SpawnRequest.newBuilder().setTemplateGuid(GuidUtil.hexToGuidText(tple.getGuid()));

				if (rbSpawnAtCurrent.isSelected() && ifValidSearchResult.isEnabled()) {
					request.setPosition(DTC.convert(translation));
				} else if (rbSpawnAtCamera.isSelected()) {
					request.setEntityName(PC_CAMERA);
				} else {
					request.setEntityName(PC_HERO);
				}

				IpcHelper.getIpc().sendRequest(request.build(), (s, rc, ud) -> SwingUtilities.invokeLater(() -> {
					log("Spawne '%s'", tple.getName());
					if (s == ResponseCallback.Status.Timeout) {
						log("Gothic 3 nicht erreichbar");
					} else if (rc.getStatus() == ResponseContainer.Status.FAILED) {
						log("Aus unbekannten Grund fehlgeschlagen.");
					} else {
						Position position = rc.getEntityResponse().getPosition();
						bCVector translation = DTC.convert(position.getTranslation());
						bCEulerAngles rotation = DTC.convert(position.getRotation());
						bCVector scale = DTC.convert(position.getScale());
						String name = rc.getEntityResponse().getName();
						String guid = GuidUtil.parseGuid(rc.getEntityResponse().getGuid());

						log("Name: %s", name);
						log("Guid: %s", guid);
						log(Misc.positionToString(translation, rotation, scale));

						searchForGuid(guid);
					}
				}), null);
			}
		}
	}

	private void showLog() {
		logDialog.setVisible(true);
		SwingUtils.bringToFront(logDialog);
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void showEntity(boolean applyPosition) {
		String entityGuid = GuidUtil.parseGuid(guid);
		String entityName = name;
		bCMatrix entityPosition = new bCMatrix(rotation, scale, translation);

		Optional<EditorArchiveTab> containingTab = ctx.getEditor().<EditorArchiveTab>getTabs(EditorTabType.Archive)
				.filter(t -> t.getCurrentFile().getEntityByGuid(entityGuid).isPresent()).findFirst();

		if (!containingTab.isPresent()) {
			FindEntityWorker entityWorker = new FindEntityWorker(entityGuid, ctx.getFileManager().worldFilesCallable(), this);
			entityWorker.executeAndShowDialog();
			if (!entityWorker.isCancelled()) {
				Optional<File> file = entityWorker.getContainingFile();
				if (file.isPresent()) {
					SwingUtils.bringToFront(ctx.getParentWindow());
					if (!ctx.getEditor().openFile(file.get())) {
						return;
					}

					containingTab = (Optional) ctx.getEditor().getSelectedTab();
				}
			} else {
				return;
			}
		} else {
			ctx.getEditor().selectTab(containingTab.get());
			SwingUtils.bringToFront(ctx.getParentWindow());
		}

		if (containingTab.isPresent() && containingTab.get().type() == EditorTabType.Archive) {
			EditorArchiveTab archiveTab = containingTab.get();
			eCEntity entity = archiveTab.getCurrentFile().getEntityByGuid(entityGuid).orElse(null);

			if (applyPosition) {
				archiveTab.modifyEntityMatrix(entity, e -> e.setToWorldMatrix(entityPosition));
			}

			archiveTab.getEntityTree().selectEntity(entity);
		} else {
			TaskDialogs.error(this, "Entity nicht gefunden",
					"Die Entity '" + entityName + "' mit der Guid '" + entityGuid + "' konnte nicht gefunden werden.");
		}

	}

	private static class FindEntityWorker extends AbstractDialogFileWorker<Optional<File>> {
		private String entityGuid;
		private Optional<File> result;

		public FindEntityWorker(String entityGuid, Callable<List<File>> fileProvider, Window parent) {
			super(fileProvider, null, "Suche Entity", parent);
			this.entityGuid = entityGuid;
			statusFormat = "Entity wird gesucht...";
		}

		@Override
		protected Optional<File> doInBackground() throws Exception {
			int filesDone = 0;
			ArchiveFileIterator iter = new ArchiveFileIterator(getFiles());
			while (iter.hasNext() && !isCancelled()) {
				for (eCEntity entity : iter.next().getEntities()) {
					if (isCancelled()) {
						return Optional.empty();
					}

					if (entity.getGuid().equals(entityGuid)) {
						return Optional.of(iter.nextFile());
					}
				}

				publish(0, ++filesDone);
			}
			return Optional.empty();
		}

		@Override
		protected void done() {
			try {
				if (!isCancelled()) {
					result = get();
				}

				progDlg.dispose();
			} catch (Exception ex) {
				progDlg.dispose();
				TaskDialogs.showException(ex);
			}
		}

		public Optional<File> getContainingFile() {
			return result;
		}
	}
}
