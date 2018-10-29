package datamodel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
	private int activeUnassignedSegment;
	
	private LocalDateTime lastModifiedDate = LocalDateTime.now(); 


	private static ProjectData currentProject;
	
	/**
	 * Loads a project from the videoFilePath
	 * @param videoFilePath
	 * @throws FileNotFoundException
	 */
	public static void loadCurrentProject(String videoFilePath) throws FileNotFoundException {
		currentProject = new ProjectData(videoFilePath);		
	}
	
	/**
	 *  Returns projectData
	 * @return ProjectData
	 */
	public static ProjectData getCurrentProject() {
		return currentProject;
	}
	
	
	private ProjectData(String videoFilePath) throws FileNotFoundException {
		video = new Video(videoFilePath);
		tracks = new ArrayList<>();
		unassignedSegments = new ArrayList<>();
		activeUnassignedSegment = 0;
	}

	/**
	 * Returns video
	 * @return Video
	 */
	public Video getVideo() {
		return video;
	}
	/**
	 * Returns an array of animal tracks
	 * @return Array of AnimalTracks
	 */
	public List<AnimalTrack> getTracks() {
		return tracks;
	}
	
	/**
	 * 
	 * @return LocalDateTime
	 */
	public LocalDateTime getModifiedDate() {
		return lastModifiedDate;
	}
	
	/**
	 * Returns Array of unassigned tracks
	 * @return Array of AnimalTracks
	 */
	public List<AnimalTrack> getUnassignedSegments() {
		return unassignedSegments;
	}
	/**
	 *  Returns the current unassigned track segment 
	 * @return AnimalTrack
	 */
	public AnimalTrack getCurrentUnassignedSegment() {
		return unassignedSegments.get(activeUnassignedSegment);
	}	
	/**
	 * Removes the current unassigned track segment
	 */
	public void removeCurrentUnassignedSegment() {
		unassignedSegments.remove(activeUnassignedSegment);
		if (unassignedSegments.size() == activeUnassignedSegment) {
			activeUnassignedSegment--;
		}
	}

	/**
	 * Switches to the last unassigned track segment
	 */
	public void moveToPrevUnassignedSegment() {
		activeUnassignedSegment--;
		if (activeUnassignedSegment < 0) {
			activeUnassignedSegment = unassignedSegments.size() - 1;
		}
	}
	/**
	 * Switches to the next unassigned track segment 
	 */
	public void moveToNextUnassignedSegment() {
		activeUnassignedSegment++;
		if (activeUnassignedSegment == unassignedSegments.size()) {
			activeUnassignedSegment = 0;
		}
	}
	/**
	 * 
	 * @return number of chicks
	 */
	public int getChickNum() {
		return chickNum;
	}
	/**
	 * Changes number of chicks
	 * @param chickNum
	 */
	public void setChickNum(int chickNum) {
		this.chickNum = chickNum;
	}
	/**
	 * Sets which track is being shown
	 * @param track
	 */
	public void setActiveTrack(AnimalTrack track) {
		if (tracks.contains(track)) {
			this.activeTrack = tracks.indexOf(track);
		} else {
			throw new IllegalArgumentException("No track found.");
		}
	}
	/**
	 * 
	 * @return AnimalTrack that is being observed
	 */
	public AnimalTrack getActiveTrack() {
		return tracks.get(activeTrack);
	}

	public void removeActiveTrack() {
		tracks.remove(activeTrack);
		if (tracks.size() == activeTrack) {
			activeTrack--;
		}
	}
	/**
	 * takes a file and exports it to CSV
	 * @param outFile
	 * @throws FileNotFoundException
	 */
	public void exportCSVFile(File outFile) throws FileNotFoundException{
		try {
			FileWriter fw = new FileWriter(outFile);
			fw.append("Chick ID, Time, X(cm), Y(cm)" + COMMA + "\n");
			
			for (AnimalTrack chickTrack : tracks) {		
				for (TimePoint pt : chickTrack.getTimePoints()) {
							fw.append(String.valueOf(chickTrack.getAnimalID()) + COMMA);
							fw.append(String.valueOf(video.getCurrentTime(pt.getFrameNum())) + COMMA);
							fw.append(String.valueOf(pt.getX()/video.getXPixelsPerCm()) + COMMA);
							fw.append(String.valueOf(pt.getY()/video.getYPixelsPerCm()) + COMMA + "\n");
				}
			}
			fw.flush();
			fw.close();
			
			System.err.println("CSV file was created successfully.");
		} catch(Exception e) {
			System.err.println("Error in CsvFileWriter");
			e.printStackTrace();}
	}
	
	/**
	 * Sends a file to JSON
	 * @param saveFile
	 * @throws FileNotFoundException
	 */
	public void saveToFile(File saveFile) throws FileNotFoundException {
		String json = toJSON();
		PrintWriter out = new PrintWriter(saveFile);
		out.print(json);
		out.close();
	}
	/**
	 * Converts the file in JSON to a string
	 * @return String
	 */
	public String toJSON() {
		Gson gson = new GsonBuilder().setPrettyPrinting().create();		
		return gson.toJson(this);
	}
	
	/**
	 * 
	 * @param loadFile
	 * @return ProjectData
	 * @throws FileNotFoundException
	 */
	public static ProjectData loadFromFile(File loadFile) throws FileNotFoundException {
		String json = new Scanner(loadFile).useDelimiter("\\Z").next();
		return fromJSON(json);
	}
	/**
	 * Converts a String to a projectdata object 
	 * @param jsonText
	 * @return ProjectData
	 * @throws FileNotFoundException
	 */
	public static ProjectData fromJSON(String jsonText) throws FileNotFoundException {
		Gson gson = new Gson();
		ProjectData data = gson.fromJson(jsonText, ProjectData.class);
		data.getVideo().connectVideoCapture();
		return data;
	}
}
