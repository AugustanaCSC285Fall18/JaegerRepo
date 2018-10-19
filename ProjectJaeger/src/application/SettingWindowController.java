package application;

import datamodel.ProjectData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;



public class SettingWindowController {
	
	private Stage stage;
	
	@FXML
	public void initialize() {
		
	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;	
	
	
	}
	
	@FXML
	public void handleStartTrackingButton() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("SettingWindow.fxml"));
	    	BorderPane root = (BorderPane) fxmlLoader.load();
	    	MainWindowController controller = fxmlLoader.getController();
	    	Scene scene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
	    	scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
	
	//    	currentProject.getVideo().setStartFrameNum(currentProject.getVideo().convertSecondsToFrameNums(Double.parseDouble(startTime.getText())));
	//    	currentProject.getVideo().setCurrentFrameNum(currentProject.getVideo().convertSecondsToFrameNums(Double.parseDouble(startTime.getText()) + 1));
	//    	currentProject.getVideo().setEndFrameNum(currentProject.getVideo().convertSecondsToFrameNums(Double.parseDouble(endTime.getText())));
	    	
	//    	currentProject.setChickNum(Integer.parseInt(chickNum.getText()));
	//    	System.err.println(Integer.parseInt(chickNum.getText()));
	//    	
	//    	stage.setTitle("Chick Tracker");
	    	stage.setScene(scene);
	    	controller.initializeWithStage(stage);
	    	stage.show();
    	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
