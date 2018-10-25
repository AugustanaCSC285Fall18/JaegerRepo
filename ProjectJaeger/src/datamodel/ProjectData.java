package datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ProjectData {
	private Video video;
	private List<AnimalTrack> tracks;
	private List<AnimalTrack> unassignedSegments;
	private int chickNum;
	private int activeTrack;

	private static ProjectData currentProject;
	
	public static void loadCurrentProject(String videoFilePath) throws FileNotFoundException {
		currentProject = new ProjectData(videoFilePath);		
	}
	
	public static ProjectData getCurrentProject() {
		return currentProject;
	}
	

	private static final String FILE_HEADER = "animalID, positions";
	private static final String COMMA = ",";
	private static final String NEW_LINE_SEPARATOR = "\n";
	

	private ProjectData(String videoFilePath) throws FileNotFoundException {
		video = new Video(videoFilePath);
		tracks = new ArrayList<>();
		unassignedSegments = new ArrayList<>();
	}

	public Video getVideo() {
		return video;
	}
	
	public List<AnimalTrack> getTracks() {
		return tracks;
	}

	public List<AnimalTrack> getUnassignedSegments() {
		return unassignedSegments;
	}
	
	public int getChickNum() {
		return chickNum;
	}
	
	public void setChickNum(int chickNum) {
		this.chickNum = chickNum;
	}
	
	public void setActiveTrack(int trackNum) {
		this.activeTrack = trackNum;
	}


	public void exportCSVFile(File outFile) throws FileNotFoundException{
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(outFile);
			fileWriter.append(FILE_HEADER.toString());
			fileWriter.append(NEW_LINE_SEPARATOR);
			
//			for(AnimalTrack animalTrack : positions) {
//				fileWriter.append(String.valueOf(animalTrack.getAnimalID()));
//				fileWriter.append(COMMA);
//				fileWriter.append(String.valueOf(animalTrack.getTimePoint());
//			}
			
			System.out.println("CSV file was created successfully.");
			
		} catch(Exception e) {
			System.out.println("Error in CsvFileWriter");
			e.printStackTrace();
		} finally {
			
			try {
				fileWriter.flush();
				fileWriter.close();
			} catch(IOException e) {
				System.out.println("Error while flushing/closing fileWriter.");
				e.printStackTrace();
			}
		}
	}
	
	public void saveToFile(File saveFile) throws FileNotFoundException {
		String json = toJSON();
		PrintWriter out = new PrintWriter(saveFile);
		out.print(json);
		out.close();
	}
	
	public String toJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();		
		return gson.toJson(this);
	}
	
	public static ProjectData loadFromFile(File loadFile) throws FileNotFoundException {
		String json = new Scanner(loadFile).useDelimiter("\\Z").next();
		return fromJSON(json);
	}
	
	public static ProjectData fromJSON(String jsonText) throws FileNotFoundException {
		Gson gson = new Gson();
		ProjectData data = gson.fromJson(jsonText, ProjectData.class);
		data.getVideo().connectVideoCapture();
		return data;
	}

	
}
