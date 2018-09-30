package application;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.List;

import org.opencv.core.Mat;

import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.TimePoint;
import datamodel.Video;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Path;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import utils.UtilsForOpenCV;


public class MainWindowController {
	
	@FXML	private Button btnBrowse;
	@FXML	private ImageView videoView;
	@FXML 	private Canvas vidCanvas;
	@FXML 	private Canvas pathCanvas;

	@FXML 	private TextField textFieldCurFrameNum;
	@FXML private Slider sliderVideoTime;

	private ProjectData project;
	private Stage stage;
	private boolean videoPlaying;
	

	
	@FXML
	public void initialize() {
	
		//TODO: delete this after done debugging.
//		loadVideo("assets/sample1.mp4");	
		videoPlaying = false;
		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue())); 
		
		GraphicsContext pathGc = pathCanvas.getGraphicsContext2D();
		
		int currentTrack = 0;
		int ovalDiameter = 6;
		int frameAdd = 5;
		pathCanvas.setOnMousePressed(new EventHandler<MouseEvent>() {
		    public void handle(MouseEvent me) {
		        pathGc.fillOval(me.getX(), me.getY(), ovalDiameter, ovalDiameter);
		        if (project.getTracks().get(currentTrack).getTimePoints().size() != 0) {
		        	double lastX = project.getTracks().get(currentTrack).getFinalTimePoint().getX() + ovalDiameter / 2;
		        	double lastY = project.getTracks().get(currentTrack).getFinalTimePoint().getY() + ovalDiameter / 2;
		        	pathGc.setLineWidth(2);
		        	pathGc.strokeLine(lastX, lastY, me.getX() + ovalDiameter / 2, me.getY() + ovalDiameter / 2);
		        }
		        project.getTracks().get(currentTrack).add(new TimePoint(me.getX(), me.getY(), (int) sliderVideoTime.getValue()));
		        showFrameAt(Integer.parseInt(textFieldCurFrameNum.getText()) + frameAdd);
		        System.out.println("Mouse pressed: " + me.getX() + " , " + me.getY() + " at frame:" + (int) sliderVideoTime.getValue());
		    }
		});
		
		if (pathCanvas.isPressed()){
			
		}
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
			System.err.println(chosenFile.getPath());
		}		
		
		//vidCanvas.widthProperty().bind(videoView.getScene().widthProperty());

	}
	
	@FXML
	public void handlePlay()  {
		if (project.getVideo() != null) {
			videoPlaying = true;
			playVideo();
		}
		//vidCanvas.widthProperty().bind(videoView.getScene().widthProperty());

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
		project.getVideo().setCurrentFrameNum(frameNum);
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
		GraphicsContext vidGc = vidCanvas.getGraphicsContext2D();
		vidGc.drawImage(curFrame, 0, 0, vidCanvas.getWidth(), vidCanvas.getHeight());
		drawPath(0, frameNum);

		textFieldCurFrameNum.setText(String.format("%05d",frameNum));	
		
	}
	
	private void drawPath(int trackNum, int frameNum) {
		GraphicsContext vidGc = pathCanvas.getGraphicsContext2D();
		vidGc.clearRect(0, 0, pathCanvas.getWidth(), pathCanvas.getHeight());
		for(TimePoint tp : project.getTracks().get(trackNum).getTimePoints()) {
	        vidGc.fillOval(tp.getX(), tp.getY(), 3, 3);
		}
	}
	
	private void playVideo() {
		while (project.getVideo().getCurrentFrameNum() <= project.getVideo().getEndFrameNum() && videoPlaying) {
			project.getVideo().setCurrentFrameNum(project.getVideo().getCurrentFrameNum() + 1);
			// TODO: complete this
		}
	}
	
}
