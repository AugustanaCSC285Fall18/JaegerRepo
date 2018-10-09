package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Mat;

import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;
import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Path;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;
import utils.UtilsForOpenCV;


public class MainWindowController {
	
	@FXML	private Button btnBrowse;
	@FXML	private Button playBtn;
	@FXML	private Button pauseBtn;
	@FXML	private Button startManualBtn;
	@FXML	private Button undoBtn;
	@FXML	private ImageView videoView;
	@FXML 	private Canvas vidCanvas;
	@FXML 	private Canvas pathCanvas;
	@FXML 	private TextField curFrameNumTextField;
	@FXML 	private TextField totalDistanceTextField;
	@FXML 	private TextField totalDistanceToFrameTextField;
	@FXML 	private TextField pxPerSqrInchTextField;
	@FXML	private Slider sliderVideoTime;

	private ProjectData project;
	private Stage stage;
	private AnimationTimer timer;
	private GraphicsContext vidGc;
	private GraphicsContext pathGc;
	
	private boolean undoModeToggled;
	private boolean manualTrackToggled;
	

	
	@FXML
	public void initialize() {
//		loadVideo("assets/sample1.mp4");	
		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue())); 
		
		pathGc = pathCanvas.getGraphicsContext2D();
		vidGc = vidCanvas.getGraphicsContext2D();
		
		
	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;
		
		// bind it so whenever the Scene changes width, the videoView matches it
		// (not perfect though... visual problems if the height gets too large.)
		//videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());
	
	}

	@FXML
	public void handleBrowse()  {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		File chosenFile = fileChooser.showOpenDialog(stage);
		if (chosenFile != null) {
			loadVideo(chosenFile.getPath());
		}		
		


	}
	
	@FXML
	public void handlePlay()  {
		if (project.getVideo() != null) {

			playVideo();
		}

	}
	
	@FXML
	public void handlePause()  {
		if (project.getVideo() != null) {
			timer.stop();
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
			pathCanvas.setOnMousePressed(new EventHandler<MouseEvent>() {
			    public void handle(MouseEvent me) {
//			        pathGc.fillOval(me.getX(), me.getY(), ovalDiameter, ovalDiameter);
//			        if (project.getTracks().get(currentTrack).getTimePoints().size() != 0) {
//			        	double prevX = project.getTracks().get(currentTrack).getFinalTimePoint().getX() + ovalDiameter / 2;
//			        	double prevY = project.getTracks().get(currentTrack).getFinalTimePoint().getY() + ovalDiameter / 2;
//			        	pathGc.setLineWidth(3);
//			        	pathGc.strokeLine(prevX, prevY, me.getX() + ovalDiameter / 2, me.getY() + ovalDiameter / 2);
//			        }
			        project.getTracks().get(currentTrack).add(new TimePoint(me.getX(), me.getY(), project.getVideo().getCurrentFrameNum() - 1));
			        showFrameAt(Integer.parseInt(curFrameNumTextField.getText()) + frameAdd);
			        System.out.println("Mouse pressed: " + me.getX() + " , " + me.getY() + " at frame:" + (project.getVideo().getCurrentFrameNum() - 1));
					sliderVideoTime.setValue(project.getVideo().getCurrentFrameNum());
			    }
			});
		} else {
			startManualBtn.setText("Start Manual Tracking");
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
	
	public void loadVideo(String filePath) {
		try {
			project = new ProjectData(filePath);
			project.getTracks().add(new AnimalTrack("Chick 1"));
			Video video = project.getVideo();
			sliderVideoTime.setMax(video.getTotalNumFrames()-1);

			// set current videocanvas & overlay to the size of the video
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			vidCanvas.setHeight(vidCanvas.getWidth()*vidCanvas.getHeight()/curFrame.getWidth());
			pathCanvas.setWidth(vidCanvas.getWidth());
			pathCanvas.setHeight(vidCanvas.getHeight());
			showFrameAt(0);
			
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void showFrameAt(int frameNum) {
		if (frameNum <= project.getVideo().getEndFrameNum()) {
			project.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
			vidGc.drawImage(curFrame, 0, 0, vidCanvas.getWidth(), vidCanvas.getHeight());
			drawPath(0);

			curFrameNumTextField.setText(String.format("%05d",frameNum));

		} else {
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
				double distance = prevTp.getDistanceTo(tp) * percTimeElapsed;
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
		timer = new AnimationTimer() {
			
			private long lastUpdate = 0;
			@Override
			public void handle(long now) {
				if (now - lastUpdate >= 33_000_000) {
					Platform.runLater(() -> {
						showFrameAt(project.getVideo().getCurrentFrameNum());
						sliderVideoTime.setValue(project.getVideo().getCurrentFrameNum());
					});
				}
			}
		};
		
		timer.start();
		

//		Timeline timeline = new Timeline(new KeyFrame(Duration.millis(33), ev ->  {
//			showFrameAt(project.getVideo().getCurrentFrameNum());
//		}));
//		timeline.setCycleCount(project.getVideo().getTotalNumFrames()/30);
//		timeline.play();
	}
		
		
}
