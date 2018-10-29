package datamodel;


public class TimePoint implements Comparable<TimePoint> {
	private double x;     // location
	private double y;      
	private int frameNum; // time (measured in frames)
	
	/**
	 * Constructor
	 * @param x
	 * @param y
	 * @param frameNum
	 */
	public TimePoint(double x, double y, int frameNum) {
		this.x = x;
		this.y = y;
		this.frameNum = frameNum;
	}
	
	/**
	 * 
	 * @return an x value
	 */
	public double getX() {
		return x;
	}
	/**
	 *  changes x value
	 * @param x
	 */
	public void setX(double x) {
		this.x = x;
	}
	
	/**
	 * 
	 * @return a y value
	 */
	public double getY() {
		return y;
	}
	
	/**
	 * changes y value
	 * @param y
	 */
	public void setY(double y) {
		this.y = y;
	}
	
	/**
	 * 
	 * @return
	 */
	public org.opencv.core.Point getPointOpenCV() {
		return new org.opencv.core.Point(x,y);
	}
	/**
	 * 
	 * @return
	 */
	public java.awt.Point getPointAWT() {
		return new java.awt.Point((int)x,(int)y);
	}
	/**
	 * 
	 * @return a frame number
	 */
	public int getFrameNum() {
		return frameNum;
	}
	
	/**
	 * @return String
	 */
	@Override
	public String toString() {
		return String.format("(%.1f,%.1f@T=%d)",x,y,frameNum);
	}
	
	/**
	 * 
	 * @param other
	 * @return distance between two timepoints
	 */
	public double getDistanceTo(TimePoint other) {
		double dx = other.x-x;
		double dy = other.y-y;
		return Math.sqrt(dx*dx+dy*dy);
	}

	/**
	 * How many frames have passed since another TimePoint
	 * @param other - the otherTimePoint to compare with
	 * @return the difference (negative if the other TimePoint is later)
	 */
	public int getTimeDiffAfter(TimePoint other) {
		return this.frameNum - other.frameNum;
	}

	/**
	 * Comparison based on the time (frame number).
	 */
	@Override
	public int compareTo(TimePoint other) {		
		return this.getTimeDiffAfter(other);
	}
	
	/**
	 * 
	 * @param main
	 * @param other
	 * @return whether two timepoints are equal
	 */
	public boolean equals(TimePoint main, TimePoint other) {
		if(main.x==other.x && main.y==other.y && main.frameNum==other.frameNum) {
			return true;
		}else {
			return false;
		}
			
	}
}

