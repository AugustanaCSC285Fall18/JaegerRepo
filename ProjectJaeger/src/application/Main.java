package application;
	
import org.opencv.core.Core;


import datamodel.ProjectData;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.fxml.FXMLLoader;


public class Main extends Application {

	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("Chick Tracker");
			FXMLLoader loader = new FXMLLoader(getClass().getResource("StartWindow.fxml"));
			BorderPane root = (BorderPane)loader.load();
			StartWindowController controller = loader.getController();
			
			Scene scene = new Scene(root,root.getPrefWidth(),root.getPrefHeight());
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			primaryStage.setScene(scene);
			controller.initializeWithStage(primaryStage);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		launch(args);
	}
}
