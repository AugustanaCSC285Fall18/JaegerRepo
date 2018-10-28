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
	private static final String COMMA = ",";
	
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
//		FileWriter fileWriter = null;
		try {
			FileWriter fw = new FileWriter(outFile);
			
			for (int numChicks = 0; numChicks < tracks.size(); numChicks++) {
				AnimalTrack chickTrack = tracks.get(numChicks);
				for (int numPoints = 0; numPoints < chickTrack.getNumPoints(); numPoints++) {
					fw.append(String.valueOf(chickTrack.getAnimalID()));
					fw.append(COMMA);
					fw.append(String.valueOf(video.getCurrentTime(chickTrack.getTimePointAtIndex(numPoints).getFrameNum())));
					fw.append(COMMA);
					fw.append(String.valueOf(chickTrack.getTimePointAtIndex(numPoints).getX()));
					fw.append(COMMA);
					fw.append(String.valueOf(chickTrack.getTimePointAtIndex(numPoints).getX()));
					fw.append(COMMA + "\n");
				}
			}
			
			System.err.println("CSV file was created successfully.");
			fw.flush();
			fw.close();
		} catch(Exception e) {
			System.err.println("Error in CsvFileWriter");
			e.printStackTrace();}
//		} finally {
//			
//			try {
//				fw.flush();
//				fw.close();
//			} catch(IOException e) {
//				System.out.println("Error while flushing/closing fileWriter.");
//				e.printStackTrace();
//			}
//		}
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
