package application;

import java.io.File;
import java.text.DecimalFormat;

import datamodel.AnimalTrack;
import datamodel.ProjectData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class StartWindowController {
	
	@FXML private Button browseBtn;
	@FXML private TextField browseTextField;
	@FXML private TextField vidLengthTxt;
	@FXML private TextField dateTxt;
	@FXML private TextField vidSizeTxt;
	@FXML private TextField editorTxt;
	@FXML private TextField verticalPxCm;
	@FXML private Button startTrackingBtn;
	@FXML private TextField startTime;	
	@FXML private TextField endTime;
	
	protected ProjectData currentProject;
	

	private Stage stage;
	
	@FXML
	public void initialize() {
				
		
		
	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;	
	
	}
	
	@FXML
	public void handleBrowseBtn() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		File chosenFile = fileChooser.showOpenDialog(stage);
		if (chosenFile != null) {
			browseTextField.setText(chosenFile.getAbsolutePath());
			loadVideo(chosenFile.getAbsolutePath());
			DecimalFormat df = new DecimalFormat("#.##");
			double videoLength = currentProject.getVideo().getTotalNumFrames() / currentProject.getVideo().getFrameRate();
			// TODO: change this to hours : minutes : seconds
			vidLengthTxt.setText(df.format(videoLength) + " seconds");
		}	
	}
	
	@FXML
	public void handleStartTime() {
		currentProject.getVideo().setStartFrameNum(currentProject.getVideo().convertSecondsToFrameNums(Double.parseDouble(startTime.getText())));
	}
	
	@FXML
	public void handleEndTime() {
		currentProject.getVideo().setEndFrameNum(currentProject.getVideo().convertSecondsToFrameNums(Double.parseDouble(endTime.getText())));
	}
	
	@FXML
	public void handleStartTrackingBtn() {
	    try{
	    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
	    	BorderPane root = (BorderPane) fxmlLoader.load();
	    	MainWindowController controller = fxmlLoader.getController();
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

	public void loadVideo(String filePath) {
		try {
			ProjectData.loadCurrentProject(filePath);
			currentProject = ProjectData.getCurrentProject();
			ProjectData.getCurrentProject().getTracks().add(new AnimalTrack("Chick 1"));
			
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	

	
}
