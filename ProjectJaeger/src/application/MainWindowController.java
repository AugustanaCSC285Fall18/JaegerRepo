package application;


import java.util.List;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;


public class MainWindowController {
	
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
	

	private ProjectData project = Main.project;
	private Stage stage;
	private AnimationTimer timer;
	private GraphicsContext vidGc;
	private GraphicsContext pathGc;
	
	private boolean undoModeToggled;
	private boolean manualTrackToggled;
	private boolean videoPlayed;
	
	private double firstCalibrationClickX = -1; 
	private double firstCalibrationClickY = -1; 
	private double horizontalCalDistance;
	private double verticalCalDistance;
	private boolean isHorizontal = true;
	
	@FXML
	public void initialize() {
		double startTime = Double.parseDouble(Main.startTime);
		double frameRate = project.getVideo().getFrameRate();

		chickMenu.getItems().add(new MenuItem("Chick 4"));	
		initializeMenu();
		
		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue())); 
		pathGc = pathCanvas.getGraphicsContext2D();
		vidGc = vidCanvas.getGraphicsContext2D();

		
		Video video = project.getVideo();
		sliderVideoTime.setMax(video.getTotalNumFrames()-1);

		// set current videocanvas & overlay to the size of the video
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
		
		vidCanvas.setHeight(vidCanvas.getWidth()*curFrame.getHeight()/curFrame.getWidth());
		
		
//		pathCanvas.setCache(true);
//		pathCanvas.setCacheHint(CacheHint.SPEED);
		
		pathCanvas.setWidth(vidCanvas.getWidth());
		pathCanvas.setHeight(vidCanvas.getHeight());
//		showFrameAt(0);
		showFrameAt((int)(startTime*frameRate));
		
	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;
		
		// bind it so whenever the Scene changes width, the videoView matches it
		// (not perfect though... visual problems if the height gets too large.)
		//videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());
	
	}

	
	public void initializeMenu()  {
		List<MenuItem> menuItems = chickMenu.getItems();
		for (MenuItem item: menuItems){
			item.setOnAction(handle -> {
					System.err.println(item.getText() + "chosen.");
			});
		}
	}
	
	@FXML
	public void handlePlay()  {
		if (project.getVideo() != null) {
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
	int currentTrack = 0;
	int ovalDiameter = 6;
	int frameAdd = 20;
	
	@FXML
	public void handleManualTrack()  {
		manualTrackToggled = !manualTrackToggled;
		if(manualTrackToggled) {
			startManualBtn.setText("Stop Manual Tracking");
			pathCanvas.setOnMousePressed((me) -> {
			        project.getTracks().get(currentTrack).add(new TimePoint(me.getX(), me.getY(), project.getVideo().getCurrentFrameNum() - 1));
			        showFrameAt((int) (sliderVideoTime.getValue() + frameAdd));
			        System.out.println("Mouse pressed: " + me.getX() + " , " + me.getY() + " at frame:" + (project.getVideo().getCurrentFrameNum() - 1));
					sliderVideoTime.setValue(project.getVideo().getCurrentFrameNum());
			});
		} else {
			startManualBtn.setText("Start Manual Tracking");
			pathCanvas.setOnMousePressed(handle -> {});
		}
	}
	
	@FXML
	public void handleUndo()  {
		undoModeToggled =  !undoModeToggled;
		showFrameAt(project.getVideo().getCurrentFrameNum());
		if (undoModeToggled) {
			undoBtn.setText("Undo On");
		} else {
			undoBtn.setText("Undo Off");
		}
	}
	
	@FXML
	public void handlePxPerSqrInch()  {

	}
	
	@FXML
	public void handleCalibration() {
		if(isHorizontal) {
			pathCanvas.setOnMousePressed(event -> {handleClickDuringHorizontalCalibration(event);});
		}else {
			pathCanvas.setOnMousePressed(event -> {handleClickDuringVerticalCalibration(event);});
		}
	}
	
	private void handleClickDuringHorizontalCalibration(MouseEvent event) {
		if (firstCalibrationClickX < 0) { // first click during calibration
			firstCalibrationClickX = event.getX();
		} else { // second click during calibration
			horizontalCalDistance = Math.abs(event.getX() - firstCalibrationClickX); 
		}
		isHorizontal = false;
		
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Horizontal Calibration");
		dialog.setContentText("Enter measured length (cm): ");
	}
	
	private void handleClickDuringVerticalCalibration(MouseEvent event) {
		if (firstCalibrationClickY < 0) { // first click during calibration
			firstCalibrationClickY = event.getY();
		} else { // second click during calibration
			verticalCalDistance = Math.abs(event.getY() - firstCalibrationClickY); 
		}
		
		TextInputDialog dialog = new TextInputDialog("");
		dialog.setTitle("Vertical Calibration");
		dialog.setContentText("Enter measured length (cm): ");
	}	

	public void showFrameAt(int frameNum) {
		double endTime = Double.parseDouble(Main.endTime);
		double frameRate = project.getVideo().getFrameRate();
//		if (frameNum <= project.getVideo().getEndFrameNum()) {
		if (frameNum <= (int)(endTime*frameRate)) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			vidGc.drawImage(curFrame, 0, 0, vidCanvas.getWidth(), vidCanvas.getHeight());
			drawPath(0);

			//curFrameNumTextField.setText(String.format("%05d",frameNum));
			curFrameNumTextField.setText(String.format("%5.2f second(s)", frameNum/frameRate));

		} else {
			videoPlayed = false;
			timer.stop();
		}
	}
	
	private void drawPath(int trackNum) {

		if (project.getTracks().get(currentTrack).getTimePoints().size() != 0) {
			pathGc.clearRect(0, 0, pathCanvas.getWidth(), pathCanvas.getHeight());
			TimePoint prevTp = project.getTracks().get(trackNum).getTimePoints().get(0);
			
			for (TimePoint tp : project.getTracks().get(trackNum).getTimePoints()) {
				
				// percentage of time elapsed between 2 time points
				double percTimeElapsed;
				double curframeNum = project.getVideo().getCurrentFrameNum();
				if (prevTp.getFrameNum() <= curframeNum && curframeNum <= tp.getFrameNum() && undoModeToggled) {
					percTimeElapsed = 1.0 * (project.getVideo().getCurrentFrameNum() - prevTp.getFrameNum()) / tp.getTimeDiffAfter(prevTp);
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
	
	 
	private void playVideo() {
		// code got from StackOverflow.
		timer = new AnimationTimer() {
			
			private long lastUpdate = 0;
			@Override
			public void handle(long now) {
				if (now - lastUpdate >= 3.333e+7) {
					Platform.runLater(() -> {
						showFrameAt(project.getVideo().getCurrentFrameNum());
						sliderVideoTime.setValue(project.getVideo().getCurrentFrameNum());
					});
					lastUpdate = now;
				}
			}
		};
		timer.start();
	}
		
		
}
