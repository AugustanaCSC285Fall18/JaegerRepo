package application;

import java.io.File;
import java.text.DecimalFormat;

import datamodel.AnimalTrack;
import datamodel.ProjectData;
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
	@FXML private TextField chickNum;
	

	private DecimalFormat df = new DecimalFormat("#.##");
	protected ProjectData currentProject;
	

	private Stage stage;
	
	@FXML
	public void initialize() {
		currentProject = ProjectData.getCurrentProject();
		if (currentProject != null) {
			browseTextField.setText(currentProject.getVideo().getFilePath());
			startTime.setText(df.format(currentProject.getVideo().convertFrameNumsToSeconds(currentProject.getVideo().getStartFrameNum())));
			endTime.setText(df.format(currentProject.getVideo().convertFrameNumsToSeconds(currentProject.getVideo().getEndFrameNum())));
			chickNum.setText("" + currentProject.getChickNum());
			vidLengthTxt.setText(df.format(currentProject.getVideo().getTotalNumFrames() / currentProject.getVideo().getFrameRate()) + " seconds");
		}
		
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
			double videoLength = currentProject.getVideo().getTotalNumFrames() / currentProject.getVideo().getFrameRate();
			// TODO: change this to hours : minutes : seconds
			vidLengthTxt.setText(df.format(videoLength) + " seconds");
			
			startTime.setText("" + 0);
			double defaultEndTime = currentProject.getVideo().convertFrameNumsToSeconds((currentProject.getVideo().getTotalNumFrames() - 1));
			endTime.setText("" + df.format(defaultEndTime) );

			
		}	
	}

	@FXML
	public void handleStartTrackingBtn() {
	    try{
	    	FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SettingWindow.fxml"));
	    	Accordion root = (Accordion) fxmlLoader.load();
	    	SettingWindowController controller = fxmlLoader.getController();
	    	Scene scene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
	    	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());

//	    	currentProject.getVideo().setStartFrameNum(currentProject.getVideo().convertSecondsToFrameNums(Double.parseDouble(startTime.getText())));
//	    	currentProject.getVideo().setCurrentFrameNum(currentProject.getVideo().convertSecondsToFrameNums(Double.parseDouble(startTime.getText()) + 1));
//	    	currentProject.getVideo().setEndFrameNum(currentProject.getVideo().convertSecondsToFrameNums(Double.parseDouble(endTime.getText())));
	    	
	    	currentProject.setChickNum(Integer.parseInt(chickNum.getText()));
	    	System.err.println(Integer.parseInt(chickNum.getText()));
	    	
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
