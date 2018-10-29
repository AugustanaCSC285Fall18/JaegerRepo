package application;

import java.io.*;
import java.util.*;

import org.opencv.core.Mat;

import autotracking.AutoTrackListener;
import autotracking.AutoTracker;
import datamodel.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class MainWindowController implements AutoTrackListener {

	@FXML	private MenuItem saveBtn;
	@FXML	private MenuItem exportBtn;
	@FXML	private Button calibrationBtn;
	@FXML	private Button playBtn;
	@FXML	private Button startManualBtn;
	@FXML	private Button undoBtn;
	@FXML	private Canvas vidCanvas;
	@FXML	private Canvas pathCanvas;
	@FXML	private TextField curFrameNumTextField;
	@FXML	private TextField totalDistanceTextField;
	@FXML	private TextField totalDistanceToFrameTextField;
	@FXML	private TextField pxPerSqrInchTextField;
	@FXML	private ProgressBar autoTrackProgressBar;
	@FXML	private Slider sliderVideoTime;
	@FXML	private MenuButton chickMenu;
	@FXML	private Button startAutoBtn;
	@FXML	private Label timeElapsed;
	@FXML	private Button backBtn;
	@FXML	private CheckBox showUnassigned;
	@FXML	private FlowPane playFlowPane;
	@FXML	private FlowPane segmentAssignFlowPane;

	private Stage stage;
	private AnimationTimer timer;
	private GraphicsContext vidGc;
	private GraphicsContext pathGc;
	private AutoTracker autotracker;

	private boolean undoModeToggled = true;
	private boolean manualTrackToggled;
	private boolean videoPlayed;

	private final String[] color = { "OrangeRed", "Gold", "LawnGreen", "DarkTurquoise", "Violet", "MediumSlateBlue" };
	private ToggleGroup menuToggleGroup;
	protected ProjectData currentProject;

	// ratio of original vid over displaying canvas
	private double displayScaleW;
	private double displayScaleH;

	@FXML
	public void initialize() {
		currentProject = ProjectData.getCurrentProject();
		currentProject.getVideo().setCurrentFrameNum(currentProject.getVideo().getStartFrameNum());

		// TODO: complete this then delete
		currentProject.getVideo().setXPixelsPerCm(6.5); // these are just rough estimates!
		currentProject.getVideo().setYPixelsPerCm(6.7);

		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));
		pathGc = pathCanvas.getGraphicsContext2D();
		vidGc = vidCanvas.getGraphicsContext2D();

		sliderVideoTime.setMin(currentProject.getVideo().getStartFrameNum());
		sliderVideoTime.setMax(currentProject.getVideo().getEndFrameNum());
		sliderVideoTime.setValue(sliderVideoTime.getMin());

		// set current video canvas & overlay to the size of the video
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(currentProject.getVideo().readFrame());

		pathCanvas.setWidth(vidCanvas.getWidth());
		pathCanvas.setHeight(vidCanvas.getHeight());
		displayScaleW = vidCanvas.getWidth() / currentProject.getVideo().getFrameWidth();
		displayScaleH = vidCanvas.getHeight() / currentProject.getVideo().getFrameHeight();

		menuToggleGroup = new ToggleGroup();
		showFrameAt((int) (currentProject.getVideo().getStartFrameNum()));

	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;
		menuToggleGroup = new ToggleGroup();
		stage.setOnCloseRequest(a -> System.exit(0));

	}

	@FXML
	private void handleAddChickBtn() {
		String suggestedInput = "Chick #" + (chickMenu.getItems().size() + 1);
		TextInputDialog dialog = new TextInputDialog(suggestedInput);
		dialog.setTitle("Add Chick:");
		dialog.setHeaderText(null);
		dialog.setContentText("Enter Chick Name:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()) {
			String chickName = result.get();
			AnimalTrack newChick = new AnimalTrack(chickName,
					Color.web((color[chickMenu.getItems().size() % color.length])));
			currentProject.getTracks().add(newChick);
			RadioMenuItem newChickItem = new RadioMenuItem(chickName);
			chickMenu.getItems().add(newChickItem);
			newChickItem.setOnAction(a -> {
				currentProject.setActiveTrack(newChick);
				chickMenu.setText(currentProject.getActiveTrack().getAnimalID());
				showFrameAt(currentProject.getVideo().getCurrentFrameNum());
			});
			newChickItem.setToggleGroup(menuToggleGroup);

			newChickItem.setSelected(true);
			currentProject.setActiveTrack(newChick);
			chickMenu.setText(currentProject.getActiveTrack().getAnimalID());
			showFrameAt(currentProject.getVideo().getCurrentFrameNum());

			newChickItem.setId("" + (chickMenu.getItems().size() - 1));
			newChickItem
					.setStyle("-fx-background-color: " + color[(chickMenu.getItems().size() - 1) % color.length] + ";");
		}

	}

	@FXML
	public void handlePlay() {
		if (currentProject.getVideo() != null) {
			if (!videoPlayed) {
				playVideo();
				playBtn.setText("Pause");
			} else {
				timer.stop();
				playBtn.setText("Play");
			}
			videoPlayed = !videoPlayed;
		}
	}

	// try a few variables
	int ovalDiameter = 6;


	@FXML
	public void handleManualTrack() {
		manualTrackToggled = !manualTrackToggled;
		
		if (manualTrackToggled) {
			double frameAdd = currentProject.getVideo().getTimeStep();
			startManualBtn.setText("Stop Manual Tracking");
			pathCanvas.setOnMousePressed((me) -> {
				currentProject.getActiveTrack().add(me.getX() / displayScaleW, me.getY() / displayScaleH,
						currentProject.getVideo().getCurrentFrameNum() - 1);
				showFrameAt((int) (sliderVideoTime.getValue() + frameAdd));
				System.out.println("Mouse pressed: " + me.getX() + " , " + me.getY() + " at frame:"
						+ (currentProject.getVideo().getCurrentFrameNum() - 1));
				sliderVideoTime.setValue(currentProject.getVideo().getCurrentFrameNum());
			});
		} else {
			startManualBtn.setText("Start Manual Tracking");
			pathCanvas.setOnMousePressed(handle -> {
			});
		}
	}

	@FXML
	public void handleUndo() {
		undoModeToggled = !undoModeToggled;
		showFrameAt(currentProject.getVideo().getCurrentFrameNum());
		if (undoModeToggled) {
			undoBtn.setText("View Mode: Partial Path");
		} else {
			undoBtn.setText("View Mode: Full Path");
		}
	}

	@FXML
	public void handleBackBtn() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("StartWindow.fxml"));
			BorderPane root = (BorderPane) fxmlLoader.load();
			StartWindowController controller = fxmlLoader.getController();
			Scene scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

			stage.setTitle("Chick Tracker");
			stage.setScene(scene);
			controller.initializeWithStage(stage);
			stage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	public void handleFrameForward() {
	}

	@FXML
	public void handleFrameBackward() {
	}

	@FXML
	public void handleSave() throws FileNotFoundException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Progress");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON", "*.json"));

		File file = fileChooser.showSaveDialog(stage);
		if (file != null) {
			try {
				currentProject.saveToFile(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@FXML
	public void handleExport() throws FileNotFoundException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save CSV File");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("CSV", "*.csv"));

		File file = fileChooser.showSaveDialog(stage);
		if (file != null) {
			try {
				currentProject.exportCSVFile(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void showFrameAt(int frameNum) {
		if (frameNum <= currentProject.getVideo().getEndFrameNum()
				&& (autotracker == null || !autotracker.isRunning())) {
			currentProject.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(currentProject.getVideo().readFrame());
			vidGc.drawImage(curFrame, 0, 0, vidCanvas.getWidth(), vidCanvas.getHeight());
			if (currentProject.getActiveTrack() != null) {
				clearAndDrawChickPath();
			}
			if (showUnassigned.isSelected()) {
				drawPath(currentProject.getCurrentUnassignedSegment());
			}

			// curFrameNumTextField.setText(String.format("%05d",frameNum));
			timeElapsed
					.setText(currentProject.getVideo().getCurrentTime(currentProject.getVideo().getCurrentFrameNum()));
		} else {
			videoPlayed = false;
		}
	}

	private void clearAndDrawChickPath() {
		pathGc.clearRect(0, 0, pathCanvas.getWidth(), pathCanvas.getHeight());
		drawPath(currentProject.getActiveTrack());
	}

	private void drawPath(AnimalTrack curChick) {
		if (curChick.getTimePoints().size() != 0) {
			pathGc.setFill(curChick.getColor());
			pathGc.setStroke(curChick.getColor());
			TimePoint prevTp = curChick.getTimePoints().get(0);

			for (TimePoint tp : curChick.getTimePoints()) {

				// percentage of time elapsed between 2 time points
				double percTimeElapsed;
				double curframeNum = currentProject.getVideo().getCurrentFrameNum();
				double prevTpScaledX = prevTp.getX() * displayScaleW;
				double prevTpScaledY = prevTp.getY() * displayScaleH;
				double tpScaledX = tp.getX() * displayScaleW;
				double tpScaledY = tp.getY() * displayScaleH;
				if (prevTp.getFrameNum() <= curframeNum && curframeNum <= tp.getFrameNum() && undoModeToggled) {
					percTimeElapsed = 1.0 * (currentProject.getVideo().getCurrentFrameNum() - prevTp.getFrameNum())
							/ tp.getTimeDiffAfter(prevTp);
				} else if (curframeNum < prevTp.getFrameNum() && undoModeToggled) {
					percTimeElapsed = 0;
				} else {
					percTimeElapsed = 1;
				}
				double x = prevTpScaledX + (tpScaledX - prevTpScaledX) * percTimeElapsed + ovalDiameter / 2;
				double y = prevTpScaledY + (tpScaledY - prevTpScaledY) * percTimeElapsed + ovalDiameter / 2;
				pathGc.setLineWidth(3);
				if (prevTp.getFrameNum() <= curframeNum || !undoModeToggled) {
					pathGc.strokeLine(prevTpScaledX + ovalDiameter / 2, prevTpScaledY + ovalDiameter / 2, x, y);
				}
				if (tp.getFrameNum() <= curframeNum || !undoModeToggled) {
					pathGc.fillOval(tpScaledX, tpScaledY, ovalDiameter, ovalDiameter);
				}
				prevTp = tp;
			}
		}
	}

	private void playVideo() {
		// code got from StackOverflow.
		timer = new AnimationTimer() {

			private long lastUpdate = 0;

			@Override
			public void handle(long now) {
				long timeElapsedInMillis = (now - lastUpdate) / 1_000_000;
				if (timeElapsedInMillis >= 34) {
					if (lastUpdate != 0) {
						int frameDiff = (int) Math.round(timeElapsedInMillis / 33.0);
						// System.out.printf("%.3f ms\n", frameDiff);
						Platform.runLater(() -> {
							int frameToShow = currentProject.getVideo().getCurrentFrameNum() + (frameDiff - 1);
							sliderVideoTime.setValue(frameToShow);
						});
					}
					lastUpdate = now;
				}
			}
		};

		timer.start();
	}

	@FXML
	public void handleAutoTrack() throws InterruptedException {
		playFlowPane.setDisable(!playFlowPane.isDisabled());
		if (videoPlayed) {
			handlePlay();
		}

		if (autotracker == null || !autotracker.isRunning()) {
			pathGc.clearRect(0, 0, pathCanvas.getWidth(), pathCanvas.getHeight());
			Video video = currentProject.getVideo();
			autotracker = new AutoTracker();
			// Use Observer Pattern to give autotracker a reference to this object,
			// and call back to methods in this class to update progress.
			autotracker.addAutoTrackListener(this);

			// this method will start a new thread to run AutoTracker in the background
			// so that we don't freeze up the main JavaFX UI thread.
			autotracker.startAnalysis(video);
			startAutoBtn.setText("CANCEL Auto Tracking");

		} else {
			
			autotracker.cancelAnalysis();
			startAutoBtn.setText("Start Auto Tracking");
			Platform.runLater(() -> {
				sliderVideoTime.setValue(currentProject.getVideo().getCurrentFrameNum() - 1);
			});

		}

	}

	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		// this method is being run by the AutoTracker's thread, so we must
		// ask the JavaFX UI thread to update some visual properties
		Platform.runLater(() -> {
			vidGc.drawImage(imgFrame, 0, 0, vidCanvas.getWidth(), vidCanvas.getHeight());
//			drawPath(currentTrack);
//			System.err.println("currentTrack :" + currentTrack);
			autoTrackProgressBar.setProgress(fractionComplete);
//			timeElapsed.setText(currentProject.getVideo().getCurrentTime());
		});
	}

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		currentProject.getUnassignedSegments().clear();
		currentProject.getUnassignedSegments().addAll(trackedSegments);

		for (AnimalTrack track : trackedSegments) {

			System.out.println(track);
		}

		Platform.runLater(() -> {
			playFlowPane.setDisable(false);
			segmentAssignFlowPane.setDisable(false);

			autoTrackProgressBar.setProgress(1.0);
			startAutoBtn.setText("Start auto-tracking");
		});
	}

	@FXML
	public void handleShowUnassigned() {
		showFrameAt(currentProject.getCurrentUnassignedSegment().getFinalTimePoint().getFrameNum());
		System.out.println(currentProject.getCurrentUnassignedSegment().getFinalTimePoint().getFrameNum() + " "
				+ currentProject.getVideo().getStartFrameNum());
	}

	@FXML
	public void handlePrevSegment() {
		currentProject.moveToPrevUnassignedSegment();
		showFrameAt(currentProject.getCurrentUnassignedSegment().getFinalTimePoint().getFrameNum());
	}

	@FXML
	public void handleSetSegment() {
		currentProject.getActiveTrack().addAll(currentProject.getCurrentUnassignedSegment());
		currentProject.removeCurrentUnassignedSegment();
		if (currentProject.getUnassignedSegments().size() == 0) {
			segmentAssignFlowPane.setDisable(true);
			showUnassigned.setSelected(false);
		} else {
			showFrameAt(currentProject.getCurrentUnassignedSegment().getFinalTimePoint().getFrameNum());
			System.out.println(currentProject.getCurrentUnassignedSegment().getFinalTimePoint().getFrameNum());
		}
	}

	@FXML
	public void handleNexStegment() {
		currentProject.moveToNextUnassignedSegment();
		showFrameAt(currentProject.getCurrentUnassignedSegment().getFinalTimePoint().getFrameNum());
	}
}
