package datamodel;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.paint.Color;

public class AnimalTrack {
	private String animalID;
	private List<TimePoint> positions;
	private Color color;
	
	
	public AnimalTrack(String id) {
		this(id, Color.GRAY);
	}
	public AnimalTrack(String id, Color color) {
		this.animalID = id;
		positions = new ArrayList<TimePoint>();
		this.color = color;
	}
	
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
	
	public void addAll(AnimalTrack segment) {
		for(TimePoint pt : segment.positions) {
			add(pt);
		}
	}
	
	public void add(double x, double y, int frameNum) {
		add(new TimePoint(x, y, frameNum));
	}

	
	public String getAnimalID() {
		return animalID;
	}
	
	public TimePoint getTimePointAtIndex(int index) {
		return positions.get(index);
	}
	
	public int getNumPoints() {
		return positions.size();
	}
	
	public void setColor(Color color) {
		this.color = color;
	}
	
	public Color getColor() {
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
	
	public TimePoint getFinalTimePoint() {
		return positions.get(positions.size()-1);
	}
	
	public String toString() {
		int startFrame = positions.get(0).getFrameNum();
		int endFrame = getFinalTimePoint().getFrameNum();
		return "AnimalTrack[id="+ animalID + ",numPts=" + positions.size()+" start=" + startFrame + " end=" + endFrame +"]"; 
	}
		
}

