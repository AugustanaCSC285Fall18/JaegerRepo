package application;

import java.util.ArrayList;
import java.util.List;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class MainWindowController {

	@FXML private Button calibrationBtn;
	@FXML private Button playBtn;
	@FXML private Button startManualBtn;
	@FXML private Button undoBtn;
	@FXML private Canvas vidCanvas;
	@FXML private Canvas pathCanvas;
	@FXML private Label timeElapsed;
	@FXML private TextField totalDistanceTextField;
	@FXML private TextField totalDistanceToFrameTextField;
	@FXML private TextField pxPerSqrInchTextField;
	@FXML private Slider sliderVideoTime;
	@FXML private MenuButton chickMenu;
	@FXML private Button backBtn;

	private Stage stage;
	private AnimationTimer timer;
	private GraphicsContext vidGc;
	private GraphicsContext pathGc;

	private boolean undoModeToggled;
	private boolean manualTrackToggled;
	private boolean videoPlayed;

	private double firstCalibrationClickX = -1;
	private double firstCalibrationClickY = -1;
	private boolean isHorizontal = true;
	private double pixelDistanceX;
	private double pixelDistanceY;
	private double measuredDistanceX;
	private double measuredDistanceY;
	private double pixelPerUnitX;
	private double pixelPerUnitY;

	private final String[] colour = { "OrangeRed", "Gold", "LawnGreen", "DarkTurquoise", "Violet", "MediumSlateBlue" };
	private ToggleGroup menuToggleGroup;
	protected ProjectData currentProject;
	private int currentTrack = 0;

	@FXML
	public void initialize() {
		currentProject = ProjectData.getCurrentProject();

		initializeMenu();

		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));
		pathGc = pathCanvas.getGraphicsContext2D();
		vidGc = vidCanvas.getGraphicsContext2D();

		sliderVideoTime.setMin(currentProject.getVideo().getStartFrameNum());
		sliderVideoTime.setMax(currentProject.getVideo().getEndFrameNum());

		// set current video canvas & overlay to the size of the video
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(currentProject.getVideo().readFrame());

		vidCanvas.setHeight(vidCanvas.getWidth() * curFrame.getHeight() / curFrame.getWidth());

//		pathCanvas.setCache(true);
//		pathCanvas.setCacheHint(CacheHint.SPEED);

		pathCanvas.setWidth(vidCanvas.getWidth());
		pathCanvas.setHeight(vidCanvas.getHeight());

//		showFrameAt(0);
		menuToggleGroup = new ToggleGroup();
		showFrameAt((int) (currentProject.getVideo().getStartFrameNum()));

	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;

		menuToggleGroup = new ToggleGroup();
		
		System.err.println("main " + currentProject.getChickNum());
		for (int num = 0; num < currentProject.getChickNum(); num++) {
			RadioMenuItem item = new RadioMenuItem("Chick " + (num + 1));
			item.setToggleGroup(menuToggleGroup);
			item.setOnAction(a -> {
//				currentTrack = num;
			});
			chickMenu.getItems().add(item);
			chickMenu.getItems().get(num).setStyle("-fx-background-color: " + colour[num] + ";");

		}

		// bind it so whenever the Scene changes width, the videoView matches it
		// (not perfect though... visual problems if the height gets too large.)
		// videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());

	}

	public void initializeMenu() {

//		for (CheckMenuItem item : menuItems) {
//			item.setOnAction(handle -> {
//				System.err.println(item.getText() + "chosen.");
//			});
//		}
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
	int frameAdd = 20;

	@FXML
	public void handleManualTrack() {
		manualTrackToggled = !manualTrackToggled;
		if (manualTrackToggled) {
			startManualBtn.setText("Stop Manual Tracking");
			pathCanvas.setOnMousePressed((me) -> {
				currentProject.getTracks().get(currentTrack)
						.add(new TimePoint(me.getX(), me.getY(), currentProject.getVideo().getCurrentFrameNum() - 1));
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
			undoBtn.setText("Undo On");
		} else {
			undoBtn.setText("Undo Off");
		}
	}

	@FXML
	public void handlePxPerSqrInch() {

	}

	@FXML
	public void handleCalibration() {
		if (isHorizontal == true) {
			pathCanvas.setOnMousePressed(event -> {
				handleClickDuringHorizontalCalibration(event);
			});
			calibrationBtn.setText("Calibrating X");
		} else {
			pathCanvas.setOnMousePressed(event -> {
				handleClickDuringVerticalCalibration(event);
			});
			calibrationBtn.setText("Calibrating Y");
		}
	}

	@FXML
	private void handleClickDuringHorizontalCalibration(MouseEvent event) {
		if (firstCalibrationClickX < 0) { // first click during calibration
			firstCalibrationClickX = event.getX();
			firstCalibrationClickX++;
			System.out.println("horizontal1: " + firstCalibrationClickX);
		} else { // second click during calibration
			pixelDistanceX = Math.abs(event.getX() - firstCalibrationClickX);
			System.out.println("horizontal2: " + event.getX());
			System.out.println("distanceX: " + pixelDistanceX);
			isHorizontal = false;

			calibrationBtn.setText("Click to calibrate Y");

			TextInputDialog dialog = new TextInputDialog("");
			dialog.setTitle("Horizontal Calibration");
			dialog.setContentText("Enter measured length (cm): ");
			measuredDistanceX = dialog.getX();

			pixelPerUnitX = pixelDistanceX / measuredDistanceX;
			System.out.println("pixelDistanceX");
			System.out.println("measuredDistanceX");
			System.out.println("pixelPerUnitX");

		}

	}
	
	@FXML
	private void handleClickDuringVerticalCalibration(MouseEvent event) {
		if (firstCalibrationClickY < 0) { // first click during calibration
			firstCalibrationClickY = event.getY();
			firstCalibrationClickY++;
			System.out.println("vertical1: " + firstCalibrationClickY);
		} else { // second click during calibration
			pixelDistanceY = Math.abs(event.getY() - firstCalibrationClickY);
			System.out.println("vertical2: " + event.getY());
			System.out.println("distanceY: " + pixelDistanceY);

			calibrationBtn.setText("Calibrated");
			calibrationBtn.setDisable(true);

			TextInputDialog dialog = new TextInputDialog("");
			dialog.setContentText("Enter measured length (cm): ");
			measuredDistanceY = dialog.getY();

			pixelPerUnitY = pixelDistanceY / measuredDistanceY;
			System.out.println("pixelDistanceY");
			System.out.println("measuredDistanceY");
			System.out.println("pixelPerUnitY");

			pathCanvas.setOnMousePressed(null);
		}

	}

	@FXML
	public void handleBackBtn() {
		 try{
		    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("StartWindow.fxml"));
		    	BorderPane root = (BorderPane) fxmlLoader.load();
		    	StartWindowController controller = fxmlLoader.getController();
		    	Scene scene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
		    	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		    	
		    	stage.setTitle("Chick Tracker");
		    	stage.setScene(scene);
		    	controller.initializeWithStage(stage);
		    	stage.show();
		    	
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
	}
	
	
	public void showFrameAt(int frameNum) {
		double frameRate = currentProject.getVideo().getFrameRate();
//		if (frameNum <= project.getVideo().getEndFrameNum()) {
		if (frameNum <= currentProject.getVideo().getEndFrameNum()) {
			currentProject.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(currentProject.getVideo().readFrame());
			vidGc.drawImage(curFrame, 0, 0, vidCanvas.getWidth(), vidCanvas.getHeight());
			drawPath(currentTrack);


			// curFrameNumTextField.setText(String.format("%05d",frameNum));
			timeElapsed.setText(getCurrentTime(frameNum));

		} else {
			videoPlayed = false;
			timer.stop();
		}
	}

	private void drawPath(int trackNum) {

		if (currentProject.getTracks().get(currentTrack).getTimePoints().size() != 0) {
			pathGc.clearRect(0, 0, pathCanvas.getWidth(), pathCanvas.getHeight());
			pathGc.setFill(Color.WHITE);
			pathGc.setStroke(Color.web(colour[trackNum], 1));
			TimePoint prevTp = currentProject.getTracks().get(trackNum).getTimePoints().get(0);

			for (TimePoint tp : currentProject.getTracks().get(trackNum).getTimePoints()) {

				// percentage of time elapsed between 2 time points
				double percTimeElapsed;
				double curframeNum = currentProject.getVideo().getCurrentFrameNum();
				if (prevTp.getFrameNum() <= curframeNum && curframeNum <= tp.getFrameNum() && undoModeToggled) {
					percTimeElapsed = 1.0 * (currentProject.getVideo().getCurrentFrameNum() - prevTp.getFrameNum())
							/ tp.getTimeDiffAfter(prevTp);
				} else if (curframeNum < prevTp.getFrameNum() && undoModeToggled) {
					percTimeElapsed = 0;
				} else {
					percTimeElapsed = 1;
				}
				double x = prevTp.getX() + (tp.getX() - prevTp.getX()) * percTimeElapsed + ovalDiameter / 2;
				double y = prevTp.getY() + (tp.getY() - prevTp.getY()) * percTimeElapsed + ovalDiameter / 2;
				pathGc.setLineWidth(3);
				if (prevTp.getFrameNum() <= curframeNum || !undoModeToggled) {
					pathGc.strokeLine(prevTp.getX() + ovalDiameter / 2, prevTp.getY() + ovalDiameter / 2, x, y);
				}
				if (tp.getFrameNum() <= curframeNum || !undoModeToggled) {
					pathGc.fillOval(tp.getX(), tp.getY(), ovalDiameter, ovalDiameter);
				}
				prevTp = tp;
			}
		}
	}

	private String getCurrentTime(int frameNum) {
		int frameRate = (int) currentProject.getVideo().getFrameRate();
		int framePerMin = (int) frameRate * 60;
		int framePerHour = (int) framePerMin * 60;

		String hours = String.format("%02d", frameNum / framePerHour);
		String minutes = String.format("%02d", frameNum / framePerMin - Integer.parseInt(hours) * 60);
		String seconds = String.format("%02d",
				frameNum / frameRate - Integer.parseInt(hours) * 3600 - Integer.parseInt(minutes) * 60);

		String time = "Time Elapsed: " + hours + ":" + minutes + ":" + seconds;
		return time;
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
							// showFrameAt(currentProject.getVideo().getCurrentFrameNum());
							int frameToShow = currentProject.getVideo().getCurrentFrameNum() + (frameDiff - 1);
							sliderVideoTime.setValue(frameToShow);
							sliderVideoTime.setVa
						});
					}
					lastUpdate = now;
				}
			}
		};

		timer.start();
	}

}
