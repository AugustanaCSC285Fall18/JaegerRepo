package application;

import java.util.List;
import java.util.Optional;

import org.opencv.core.Mat;

import autotracking.AutoTrackListener;
import autotracking.AutoTracker;
import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;

public class MainWindowController implements AutoTrackListener {
	
	@FXML   private Button saveBtn;
	@FXML   private Button exportBtn;
	@FXML   private Button calibrationBtn;
	@FXML	private Button playBtn;
	@FXML	private Button startManualBtn;
	@FXML	private Button undoBtn;
	@FXML 	private Canvas vidCanvas;
	@FXML 	private Canvas pathCanvas;
	@FXML 	private TextField curFrameNumTextField;
	@FXML 	private TextField totalDistanceTextField;
	@FXML 	private TextField totalDistanceToFrameTextField;
	@FXML 	private TextField pxPerSqrInchTextField;
	@FXML	private Slider sliderVideoTime;
	@FXML	private MenuButton chickMenu;
	@FXML private Button startAutoBtn;
	@FXML private Label timeElapsed;
	@FXML private Button backBtn;

	private Stage stage;
	private AnimationTimer timer;
	private GraphicsContext vidGc;
	private GraphicsContext pathGc;
	private AutoTracker autotracker;

	private boolean undoModeToggled;
	private boolean manualTrackToggled;
	private boolean videoPlayed;

	private final String[] color = { "OrangeRed", "Gold", "LawnGreen", "DarkTurquoise", "Violet", "MediumSlateBlue" };
	private ToggleGroup menuToggleGroup;
	protected ProjectData currentProject;
	private AnimalTrack selectedChick;

	@FXML
	public void initialize() {
		currentProject = ProjectData.getCurrentProject();

		currentProject.getVideo().setXPixelsPerCm(6.5); //  these are just rough estimates!
		currentProject.getVideo().setYPixelsPerCm(6.7);
		initializeMenu();

		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));
		pathGc = pathCanvas.getGraphicsContext2D();
		vidGc = vidCanvas.getGraphicsContext2D();

		sliderVideoTime.setMin(currentProject.getVideo().getStartFrameNum());
		sliderVideoTime.setMax(currentProject.getVideo().getEndFrameNum());

		// set current video canvas & overlay to the size of the video
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(currentProject.getVideo().readFrame());

		vidCanvas.setHeight(vidCanvas.getWidth() * curFrame.getHeight() / curFrame.getWidth());

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
		
		// bind it so whenever the Scene changes width, the videoView matches it
		// (not perfect though... visual problems if the height gets too large.)
		// videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());

	}

	public void initializeMenu() {
		
//		for (int num = 0; num < currentProject.getChickNum(); num++) {
//			RadioMenuItem item = new RadioMenuItem("Chick " + (num + 1));
//			item.setToggleGroup(menuToggleGroup);
//			
//			item.setOnAction(a -> {
//				currentTrack = Integer.parseInt(item.getId());
//				System.err.println(Integer.parseInt(item.getId()) + 1 + " selected");
//				System.err.println("currentTrack: " + currentTrack);
//				showFrameAt(currentProject.getVideo().getCurrentFrameNum());
//			});
//			currentProject.getTracks().add(new AnimalTrack(""+ num));
//			chickMenu.getItems().add(item);
//			
//
//		}
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
			AnimalTrack newChick = new AnimalTrack(chickName, Color.web((color[chickMenu.getItems().size() % color.length])));
			currentProject.getTracks().add(newChick);
			RadioMenuItem newChickItem = new RadioMenuItem(chickName);
			chickMenu.getItems().add(newChickItem);
			newChickItem.setOnAction(a -> {
				selectedChick = newChick;
				System.err.println(Integer.parseInt(newChickItem.getId()) + 1 + " selected");
				System.err.println("ChickIndx AddChickBtn: " + selectedChick.getAnimalID());
				showFrameAt(currentProject.getVideo().getCurrentFrameNum());
			});
			newChickItem.setToggleGroup(menuToggleGroup);
			if(chickMenu.getItems().size() == 1) {
				newChickItem.setSelected(true);
				selectedChick = newChick;
			}
			newChickItem.setId("" + (chickMenu.getItems().size() - 1));
			newChickItem.setStyle("-fx-background-color: " + color[(chickMenu.getItems().size() - 1) % color.length] + ";");
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
	int frameAdd = 20;

	@FXML
	public void handleManualTrack() {
		manualTrackToggled = !manualTrackToggled;
		if (manualTrackToggled) {
			startManualBtn.setText("Stop Manual Tracking");
			pathCanvas.setOnMousePressed((me) -> {
				selectedChick.add(new TimePoint(me.getX(), me.getY(), currentProject.getVideo().getCurrentFrameNum() - 1));
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
	

	
	@FXML
	public void handleSave() {
		//currentProject.saveToFile();
	}
	
	
	@FXML
	public void handleExport() {
		//I have code in the DataProject class to export a csv file, not sure if this 
		//method is needed or if i should just do it in the DataProject class
	}

	public void showFrameAt(int frameNum) {
		if (frameNum <= currentProject.getVideo().getEndFrameNum() && (autotracker == null || !autotracker.isRunning())) {
			currentProject.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(currentProject.getVideo().readFrame());
			vidGc.drawImage(curFrame, 0, 0, vidCanvas.getWidth(), vidCanvas.getHeight());
			if (selectedChick != null) {
				drawPath(selectedChick);
				System.err.println("chickInd at ShowFrame :" + selectedChick.getAnimalID());
			}


			// curFrameNumTextField.setText(String.format("%05d",frameNum));
			timeElapsed.setText(getCurrentTime(frameNum));
		} else {
			videoPlayed = false;
			timer.stop();
		}
	}
	
	private void drawPath(AnimalTrack curChick) {

		pathGc.clearRect(0, 0, pathCanvas.getWidth(), pathCanvas.getHeight());
		if (curChick.getTimePoints().size() != 0) {
			pathGc.setFill(Color.WHITE);
			pathGc.setStroke(curChick.getColor());
			TimePoint prevTp = curChick.getTimePoints().get(0);

			for (TimePoint tp : curChick.getTimePoints()) {

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
		if (autotracker == null || !autotracker.isRunning()) {
			Video video = currentProject.getVideo();
//			video.setStartFrameNum(Integer.parseInt(textfieldStartFrame.getText()));
//			video.setEndFrameNum(Integer.parseInt(textfieldEndFrame.getText()));
			video.setStartFrameNum(100);
			video.setEndFrameNum(3000);
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
		}
		 
	}
	@Override
	public void handleTrackedFrame(Mat frame, int frameNumber, double fractionComplete) {
		Image imgFrame = UtilsForOpenCV.matToJavaFXImage(frame);
		// this method is being run by the AutoTracker's thread, so we must
		// ask the JavaFX UI thread to update some visual properties
		Platform.runLater(() -> { 
			videoPlayed = false;
			currentProject.getVideo().setCurrentFrameNum(frameNumber);
			vidGc.drawImage(imgFrame, 0, 0, vidCanvas.getWidth(), vidCanvas.getHeight());
//			drawPath(currentTrack);
//			System.err.println("currentTrack :" + currentTrack);
//			progressAutoTrack.setProgress(fractionComplete);
			sliderVideoTime.setValue(frameNumber);
			timeElapsed.setText(getCurrentTime(frameNumber));
		});		
	}

	@Override
	public void trackingComplete(List<AnimalTrack> trackedSegments) {
		currentProject.getUnassignedSegments().clear();
		currentProject.getUnassignedSegments().addAll(trackedSegments);

		for (AnimalTrack track: trackedSegments) {
			System.out.println(track);
//			System.out.println("  " + track.getPositions());
		}
		Platform.runLater(() -> { 
//			progressAutoTrack.setProgress(1.0);
			startAutoBtn.setText("Start auto-tracking");
		});	
	}

}
