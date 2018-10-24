package application;

import java.text.DecimalFormat;

import datamodel.ProjectData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
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
	@FXML private Button setStartTimeBtn;
	@FXML private Button setEndTimeBtn;
	@FXML private Label statusTxt;
	@FXML private TextField timeStepTxt;
	private GraphicsContext gc;
	
	@FXML private Button calibrateXbtn;
	@FXML private Button calibrateYbtn;
	@FXML private TextField measuredX;
	@FXML private TextField measuredY;
	
	private double firstCalibrationClickX = -1;
	private double firstCalibrationClickY = -1;
	private double firstXValue;
	private double firstYValue;
	private double pixelDistanceX;
	private double pixelDistanceY;
	private double measuredDistanceX;
	private double measuredDistanceY;
	private double pixelPerUnitX;
	private double pixelPerUnitY;
	
	
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
	public void handleStartTimeBtn() {
		int curFrame = (int) sliderVideoTime.getValue();
		currentProject.getVideo().setStartFrameNum((curFrame));
		statusTxt.setText("Start time is set at " + df.format(currentProject.getVideo().convertFrameNumsToSeconds(curFrame)) + " secs");
	}
	
	@FXML
	public void handleEndTimeBtn() {
		int curFrame = (int) sliderVideoTime.getValue();
		currentProject.getVideo().setEndFrameNum((int) sliderVideoTime.getValue());
		statusTxt.setText("End time is set at " + df.format(currentProject.getVideo().convertFrameNumsToSeconds(curFrame)) + " secs");
	}
	
	// fix this to secs later
	@FXML
	public void handleSetTimeStep() {
		currentProject.getVideo().setTimeStep(Integer.parseInt(timeStepTxt.getText()));
		statusTxt.setText("Time step is set to " + currentProject.getVideo().getTimeStep()); 
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
	
	
	@FXML
	public void handleCalibrationX() {
		calibrateXbtn.setText("First Click");
			calCanvas.setOnMousePressed(event -> {
				CalibrateX(event);
			});
	}
	
	@FXML
	public void handleCalibrationY() {
		calibrateYbtn.setText("First Click");
			calCanvas.setOnMousePressed(event -> {
				CalibrateY(event);
			});
			
	}
	
	@FXML
	private void CalibrateX (MouseEvent event) {
		// test point
		gc = calCanvas.getGraphicsContext2D();		
		gc.fillOval(event.getX()-5, event.getY() -5, 10, 10);
		
		if (firstCalibrationClickX < 0) { // first click during calibration
			firstXValue = event.getX();
			firstCalibrationClickX++;
			
			System.out.println("horizontal1: " + firstXValue);
			calibrateXbtn.setText("Second Click");
		} else { // second click during calibration
			pixelDistanceX = Math.abs(event.getX() - firstXValue);
			System.out.println("horizontal2: " + event.getX());
			System.out.println("distanceX: " + pixelDistanceX);

			measuredDistanceX=Integer.parseInt(measuredX.getText()); //need measured distance
			pixelPerUnitX = pixelDistanceX / measuredDistanceX;
			
			System.out.println(pixelDistanceX);
			System.out.println(measuredDistanceX);
			System.out.println(pixelPerUnitX);
			calibrateXbtn.setText("Complete");
		}
		

	}
	
	@FXML
	private void CalibrateY(MouseEvent event) {
		
		if (firstCalibrationClickY < 0) { // first click during calibration
			firstYValue = event.getY();
			firstCalibrationClickY++;
			
			System.out.println("Vertical1: " + firstYValue);
			calibrateYbtn.setText("Second Click");
		} else { // second click during calibration
			pixelDistanceY = Math.abs(event.getY() - firstYValue);
			System.out.println("Vertical2: " + event.getY());
			System.out.println("distanceY: " + pixelDistanceY);

			measuredDistanceY=Integer.parseInt(measuredY.getText());
			pixelPerUnitY = pixelDistanceY / measuredDistanceY;
			
			System.out.println(pixelDistanceY);
			System.out.println(measuredDistanceY);
			System.out.println(pixelPerUnitY);
			calibrateYbtn.setText("Complete");
		}
	}

}
