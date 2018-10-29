package application;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import datamodel.AnimalTrack;
import datamodel.ProjectData;
import datamodel.Video;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class StartWindowController {

	@FXML private Button loadBtn;
	@FXML private Button browseBtn;
	@FXML private TextField browseTextField;
	@FXML private TextField vidLengthTxt;
	@FXML private TextField dateTxt;
	@FXML private TextField sizeTxt;
	@FXML private TextField editorTxt;
	@FXML private TextField verticalPxCm;
	@FXML private Button startTrackingBtn;

	

	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd"); 
	private DecimalFormat df = new DecimalFormat("#.##");
	protected ProjectData currentProject;

	private Stage stage;

	@FXML
	public void initialize() {
		currentProject = ProjectData.getCurrentProject();
 
		if (currentProject != null) {
			browseTextField.setText(currentProject.getVideo().getFilePath());
			vidLengthTxt.setText(currentProject.getVideo().getCurrentTime(currentProject.getVideo().getTotalNumFrames()));
			dateTxt.setText(dtf.format(currentProject.getModifiedDate()));
			sizeTxt.setText(df.format(currentProject.getVideo().getVidSize()) + " MB");
		}
	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;
	}

	/**
	 * Let users browse the video in their PCs
	 */
	@FXML
	public void handleBrowseBtn() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Video File");
		File chosenFile = fileChooser.showOpenDialog(stage);
		

		if (chosenFile != null) {
			loadVideo(chosenFile.getAbsolutePath());
			currentProject.getVideo().setVidSize(chosenFile.length()/(1024*1024.0));
			sizeTxt.setText(df.format(currentProject.getVideo().getVidSize()) + " MB");
			browseTextField.setText(chosenFile.getAbsolutePath());
			vidLengthTxt.setText(currentProject.getVideo().getCurrentTime(currentProject.getVideo().getTotalNumFrames()));
			dateTxt.setText(dtf.format(currentProject.getModifiedDate()));
		}
	}

	/**
	 * Moves to the Setting Window
	 */
	@FXML
	public void handleStartTrackingBtn() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SettingWindow.fxml"));
			Accordion root = (Accordion) fxmlLoader.load();
			SettingWindowController controller = fxmlLoader.getController();
			Scene scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

//	    	currentProject.getVideo().setStartFrameNum(currentProject.getVideo().convertSecondsToFrameNums(Double.parseDouble(startTime.getText())));
//	    	currentProject.getVideo().setCurrentFrameNum(currentProject.getVideo().convertSecondsToFrameNums(Double.parseDouble(startTime.getText()) + 1));
//	    	currentProject.getVideo().setEndFrameNum(currentProject.getVideo().convertSecondsToFrameNums(Double.parseDouble(endTime.getText())));

//			currentProject.setChickNum(5);

			stage.setTitle("Chick Tracker");
			stage.setScene(scene);
			controller.initializeWithStage(stage);
			stage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the chosen video
	 * @param filePath - the chosen video's file path
	 */
	public void loadVideo(String filePath) {
		try {
			ProjectData.loadCurrentProject(filePath);
			currentProject = ProjectData.getCurrentProject();
			ProjectData.getCurrentProject().getTracks().add(new AnimalTrack("Anonymous Chick"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the saved progress
	 * @throws FileNotFoundException 
	 */
	@FXML
	public void handleLoadProgress() throws FileNotFoundException {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Load Progress");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("JSON", "*.json"));

		File file = fileChooser.showOpenDialog(stage);
		if (file != null) {
			ProjectData.loadFromFile(file);
			currentProject = ProjectData.getCurrentProject();
			try {
				FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
		    	BorderPane root = (BorderPane) fxmlLoader.load();
		    	MainWindowController controller = fxmlLoader.getController();
		    	Scene scene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
		    	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
		
		    	stage.setScene(scene);
		    	controller.initializeWithStage(stage);
		    	stage.show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
