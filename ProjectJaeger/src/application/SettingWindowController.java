package application;

import java.text.DecimalFormat;
import java.util.Optional;

import datamodel.ProjectData;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
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
	
	@FXML private Label timeLabel;
	private GraphicsContext gc;
	
	@FXML private Button calibrateXbtn;
	@FXML private Button calibrateYbtn;
	@FXML private TextField pxPerCmX;
	@FXML private TextField pxPerCmY;
	
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
//		vidCanvas.setHeight(vidCanvas.getWidth() * curFrame.getHeight() / curFrame.getWidth());
		
		showFrameAt((int) (currentProject.getVideo().getStartFrameNum()));
		sliderVideoTime.setMax(currentProject.getVideo().getEndFrameNum());
		sliderVideoTime.valueProperty().addListener((obs, oldV, newV) -> showFrameAt(newV.intValue()));
	}

	public void initializeWithStage(Stage stage) {
		this.stage = stage;	
	}
	
	/**
	 * Lets users select the beginning time for video tracking
	 */
	@FXML
	public void handleStartTimeBtn() {
		currentProject.getVideo().setStartFrameNum(currentProject.getVideo().getCurrentFrameNum());
		statusTxt.setText("Start time is set at " + currentProject.getVideo().getCurrentTime(currentProject.getVideo().getCurrentFrameNum()));
	}
	
	/**
	 * Lets users select the ending time for video tracking
	 */
	@FXML
	public void handleEndTimeBtn() {
		currentProject.getVideo().setEndFrameNum(currentProject.getVideo().getCurrentFrameNum() - 1);
		statusTxt.setText("End time is set at " + currentProject.getVideo().getCurrentTime(currentProject.getVideo().getCurrentFrameNum()));
	}
	
	/**
	 * Lets users choose the time step for video tracking
	 */
	@FXML
	public void handleSetTimeStep() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Set Time Step");
		dialog.setHeaderText(null);
		dialog.setContentText("Enter time step (in seconds): ");
		Optional<String> result = dialog.showAndWait();
			
		if (result.isPresent()) {
			currentProject.getVideo().setTimeStep(currentProject.getVideo().convertSecondsToFrameNums(Double.parseDouble(result.get())));
			statusTxt.setText("Time step is set to " + currentProject.getVideo().getTimeStep() + " frame(s)/click"); 
		}
	}
	
	/**
	 * Moves to the Main Window to start tracking
	 */
	@FXML
	public void handleStartTrackingButton() {
		if (currentProject.getVideo().getTimeStep() == 0 || pxPerCmX.getText().equals("") || pxPerCmY.getText().equals("")) {
			Alert alert = new Alert(AlertType.ERROR, "Make sure the calibration and time step are set");		
			alert.setTitle("Settings incomplete");
			alert.showAndWait();

		} else {
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
	

	/**
	 * Shows the video frame
	 * @param frameNum - the number of frames
	 */
	public void showFrameAt(int frameNum) {
		if (frameNum < currentProject.getVideo().getTotalNumFrames()) {
			currentProject.getVideo().setCurrentFrameNum(frameNum);
			Image curFrame = UtilsForOpenCV.matToJavaFXImage(currentProject.getVideo().readFrame());
			vidGc.drawImage(curFrame, 0, 0, vidCanvas.getWidth(), vidCanvas.getHeight());
			 timeLabel.setText(currentProject.getVideo().getCurrentTime(currentProject.getVideo().getCurrentFrameNum()));

		} 
	}
	
	/**
	 * Calibrates the horizontal interval
	 */
	@FXML
	public void handleCalibrationX() {
		
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Horizontal Calibration");
		dialog.setHeaderText(null);
		dialog.setContentText("Enter Measured X Value:");
		Optional<String> result = dialog.showAndWait();
		
		
	if (result.isPresent()) {
			calibrateXbtn.setDisable(true);
			measuredDistanceX=Integer.parseInt(result.get()); //need measured distance
			calibrateXbtn.setText("First Click");
			calCanvas.setOnMousePressed(event -> {
				CalibrateX(event);
				});
	}
	}
	
	/**
	 * Calibrates the vertical interval
	 */
	@FXML
	public void handleCalibrationY() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Vertical Calibration");
		dialog.setHeaderText(null);
		dialog.setContentText("Enter Measured Y Value:");
		Optional<String> result = dialog.showAndWait();
		calCanvas.setDisable(false);
		
		if (result.isPresent()) {
			calibrateYbtn.setDisable(true);
			measuredDistanceY=Integer.parseInt(result.get());
			calibrateYbtn.setText("First Click");
			calCanvas.setOnMousePressed(event -> {
				CalibrateY(event);
			});
		}
	}
	
	private void CalibrateX (MouseEvent event) {
		// test point
		gc = calCanvas.getGraphicsContext2D();		
		gc.fillOval(event.getX()-5, event.getY() -5, 10, 10);

			pixelPerUnitX = pixelDistanceX / measuredDistanceX;
			if (firstCalibrationClickX < 0) { // first click during calibration
					firstXValue = event.getX();
					firstCalibrationClickX++;
			
					System.out.println("horizontal1: " + firstXValue);
					calibrateXbtn.setText("Second Click");
			} else { // second click during calibration
				pixelDistanceX = Math.abs(event.getX() - firstXValue);
				System.out.println("horizontal2: " + event.getX());
				System.out.println("distanceX: " + pixelDistanceX);
				calCanvas.setDisable(true);
				pixelPerUnitX = pixelDistanceX / measuredDistanceX;

				System.out.println(pixelDistanceX);
				System.out.println(measuredDistanceX);
				System.out.println(pixelPerUnitX);
				calibrateXbtn.setText("Complete");
				pxPerCmX.setText(df.format(pixelPerUnitX));
			}
		
	}
	
	private void CalibrateY(MouseEvent event) {
		gc = calCanvas.getGraphicsContext2D();		
		gc.fillOval(event.getX()-5, event.getY() -5, 10, 10);
		
				if (firstCalibrationClickY < 0) { // first click during calibration
					firstYValue = event.getY();
					firstCalibrationClickY++;
		
					System.out.println("Vertical1: " + firstYValue);
					calibrateYbtn.setText("Second Click");
				} else { // second click during calibration
					pixelDistanceY = Math.abs(event.getY() - firstYValue);
					System.out.println("Vertical2: " + event.getY());
					System.out.println("distanceY: " + pixelDistanceY);
					calCanvas.setDisable(true);
			
			
				pixelPerUnitY = pixelDistanceY / measuredDistanceY;
			
				System.out.println(pixelDistanceY);
				System.out.println(measuredDistanceY);
				System.out.println(pixelPerUnitY);
				calibrateYbtn.setText("Complete");
				pxPerCmY.setText(df.format(pixelPerUnitY));
			}
		}
	

}
