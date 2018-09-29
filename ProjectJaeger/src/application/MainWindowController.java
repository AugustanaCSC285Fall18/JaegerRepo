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
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
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
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import utils.UtilsForOpenCV;


public class MainWindowController {
	
	
	@FXML	private Button btnBrowse;
	@FXML	private ImageView videoView;
	@FXML private Slider sliderVideoTime;
	@FXML private TextField textFieldCurFrameNum;
	@FXML private SplitMenuButton dropDown;
	
	private ProjectData project;
	private Stage stage;
	
	
	@FXML
	public void handleDropdown()  {
		dropDown.getItems().addAll(new MenuItem("Chick 1"), new MenuItem("Chick 2"));
	}

	
@FXML public void initialize() {
		
		//loadVideo("S:/class/cs/285/sample_videos/sample1.mp4");		

		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue())); 
	}

public void initializeWithStage(Stage stage) {
	this.stage = stage;
	
	// bind it so whenever the Scene changes width, the videoView matches it
	// (not perfect though... visual problems if the height gets too large.)
	videoView.fitWidthProperty().bind(videoView.getScene().widthProperty());  
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
	
	public void loadVideo(String filePath) {
		try {
			project = new ProjectData(filePath);
			Video video = project.getVideo();
			sliderVideoTime.setMax(video.getTotalNumFrames()-1);
			showFrameAt(0);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void showFrameAt(int frameNum) {
		project.getVideo().setCurrentFrameNum(frameNum);
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(project.getVideo().readFrame());
		videoView.setImage(curFrame);
		textFieldCurFrameNum.setText(String.format("%05d",frameNum));		
	}
	
}
