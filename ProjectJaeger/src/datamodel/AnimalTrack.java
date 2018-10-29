package datamodel;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

public class AnimalTrack {
	private String animalID;
	private List<TimePoint> positions;
	private String color;
	
	/**
	 * Constructor
	 * @param id
	 */
	public AnimalTrack(String id) {
		this(id, "gray");
	}
	
	/**
	 * Constructor
	 * @param id
	 * @param color
	 */
	public AnimalTrack(String id, String color) {
		this.animalID = id;
		positions = new ArrayList<TimePoint>();
		this.color = color;
	}
	
	/**
	 * 
	 * @param pt
	 */
	public void add(TimePoint pt) {
		if (positions.size() == 0) {
			positions.add(pt);
		} else if (positions.get(positions.size() - 1).getFrameNum() < pt.getFrameNum()) {
			positions.add(pt);
		} else {
			int index = 0;
			while (positions.get(index).getFrameNum() < pt.getFrameNum()) {
				index++;
			}
			positions.add(index, pt);
		}
	}
	
	/**
	 * Adds all the timepoints in a positions array
	 * @param segment
	 */
	public void addAll(AnimalTrack segment) {
		for(TimePoint pt : segment.positions) {
			add(pt);
		}
	}
	
	/**
	 * Adds a new timepoint
	 * @param x
	 * @param y
	 * @param frameNum
	 */
	public void add(double x, double y, int frameNum) {
		add(new TimePoint(x, y, frameNum));
	}

	/**
	 * 
	 * @return String animalID
	 */
	public String getAnimalID() {
		return animalID;
	}
	
	/**
	 * 
	 * @param index
	 * @return the timepoint at an index in the array of positions
	 */
	public TimePoint getTimePointAtIndex(int index) {
		return positions.get(index);
	}
	
	/**
	 * 
	 * @return number of timepoints in the positions array
	 */
	public int getNumPoints() {
		return positions.size();
	}
	
	/**
	 * 
	 * @param color
	 */
	public void setColor(String color) {
		this.color = color;
	}
	
	/**
	 * 
	 * @return color
	 */
	public String getColor() {
		return color;
	}
	
	/**
	 * @return a deep copy of time points
	 */
	public List<TimePoint> getTimePoints() {
		List<TimePoint> clone = new ArrayList<TimePoint>();
		clone.addAll(positions);
		return clone;
	}

	/**
	 * Returns the TimePoint at the specified time, or null
	 * @param frameNum
	 * @return
	 */
	public TimePoint getTimePointAtTime(int frameNum) {
		//TODO: This method's implementation is inefficient [linear search is O(N)]
		//      Replace this with binary search (O(log n)] or use a Map for fast access
		for (TimePoint pt : positions) {
			if (pt.getFrameNum() == frameNum) {
				return pt;
			}
		}
		return null;
	}
	
	/**
	 *
	 * @return the final timepoint of the animal track
	 */
	public TimePoint getFinalTimePoint() {
		return positions.get(positions.size()-1);
	}
	
	/**
	 * 
	 * @return total distance the track traveled.
	 */
	public double getDistanceInPixels() {
		double sum = 0;
		for (int i = 0; i < positions.size() - 2; i++) {
			sum += positions.get(i).getDistanceTo(positions.get(i + 1));
		}
		return sum;
	}
	
	/**
	 * Creates a string describing the animal track
	 */
	public String toString() {
		int startFrame = positions.get(0).getFrameNum();
		int endFrame = getFinalTimePoint().getFrameNum();
		return "AnimalTrack[id="+ animalID + ",numPts=" + positions.size()+" start=" + startFrame + " end=" + endFrame +"]"; 
	}
		
}

