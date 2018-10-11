package application;

import java.io.File;

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
	@FXML private TextField horizontalPxCm;
	@FXML private TextField verticalPxCm;
	@FXML private Button startTrackingBtn;
	@FXML private TextField startTime;	
	@FXML private TextField endTime;
	

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
		}	
	}
	
	@FXML
	public void handleStartTime() {
		Main.startTime = startTime.getText();
		System.err.println(Main.startTime);
	}
	
	@FXML
	public void handleEndTime() {
		Main.endTime = endTime.getText();
		System.err.println(Main.endTime);
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
			Main.project = new ProjectData(filePath);
			Main.project.getTracks().add(new AnimalTrack("Chick 1"));
			
		
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
