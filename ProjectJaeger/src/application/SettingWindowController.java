package application;

import java.text.DecimalFormat;

import datamodel.ProjectData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Accordion;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import utils.UtilsForOpenCV;



public class SettingWindowController {
	
	private Stage stage;
	private GraphicsContext vidGc;
	private GraphicsContext calGc;

	private DecimalFormat df = new DecimalFormat("#.##");
	protected ProjectData currentProject;
	
	@FXML private Slider sliderVideoTime;
	@FXML private Canvas vidCanvas;
	@FXML private Canvas calCanvas;
	
	
	@FXML
	public void initialize() {
		currentProject = ProjectData.getCurrentProject();
		
		Image curFrame = UtilsForOpenCV.matToJavaFXImage(currentProject.getVideo().readFrame());
		vidGc = vidCanvas.getGraphicsContext2D();
		calGc = calCanvas.getGraphicsContext2D();
		calGc.drawImage(curFrame, 0, 0, vidCanvas.getWidth(), vidCanvas.getHeight());
		vidCanvas.setHeight(vidCanvas.getWidth() * curFrame.getHeight() / curFrame.getWidth());
		
		showFrameAt((int) (currentProject.getVideo().getStartFrameNum()));
		sliderVideoTime.setMax(currentProject.getVideo().getEndFrameNum());
		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));
	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;	
	
	
	}
	
	@FXML
	public void handleStartTrackingButton() {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MainWindow.fxml"));
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
	
	public void showFrameAt(int frameNum) {
		if (frameNum <= currentProject.getVideo().getEndFrameNum()) {
			currentProject.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(currentProject.getVideo().readFrame());
			vidGc.drawImage(curFrame, 0, 0, vidCanvas.getWidth(), vidCanvas.getHeight());


			// curFrameNumTextField.setText(String.format("%05d",frameNum));

		} 
	}

}
