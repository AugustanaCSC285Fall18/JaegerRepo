package datamodel;

import java.awt.Rectangle;
import java.io.FileNotFoundException;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Video {
	
	private String filePath;
	private transient VideoCapture vidCap;
	private int emptyFrameNum;
	private int startFrameNum;
	private int endFrameNum;
	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private Rectangle arenaBounds; 
	private double timeStep;
	private double vidSize;

	
	/**
	 * Constructor
	 * @param filePath
	 * @throws FileNotFoundException
	 */
	public Video(String filePath) throws FileNotFoundException {
		this.filePath = filePath;
		this.vidCap = new VideoCapture(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
		//fill in some reasonable default/starting values for several fields
		this.emptyFrameNum = 0;
		this.startFrameNum = 0;
		this.endFrameNum = this.getTotalNumFrames()-1;
		
		int frameWidth = (int)vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
		int frameHeight = (int)vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
		this.arenaBounds = new Rectangle(0,0,frameWidth,frameHeight);
	}
	
	/**
	 * Opens the video
	 * @throws FileNotFoundException
	 */
	public synchronized void connectVideoCapture() throws FileNotFoundException {
		this.vidCap = new VideoCapture(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
	}
	
	/**
	 * Changes the current frame number
	 * @param seekFrame
	 */
	public synchronized void setCurrentFrameNum(int seekFrame) {
		vidCap.set(Videoio.CV_CAP_PROP_POS_FRAMES, (double) seekFrame);
	}
	
	/**
	 * 
	 * @return the current frame number
	 */
	public synchronized int getCurrentFrameNum() {
		return (int) vidCap.get(Videoio.CV_CAP_PROP_POS_FRAMES);
	}
	
	/**
	 * 
	 * @return
	 */
	public synchronized Mat readFrame() {
		Mat frame = new Mat();
		vidCap.read(frame);
		return frame;
	}
	
	/**
	 * 
	 * @return file path
	 */
	public String getFilePath() {
		return this.filePath;
	}
	
	/** 
	 * @return frames per second
	 */
	public double getFrameRate() {
		return vidCap.get(Videoio.CAP_PROP_FPS);
	}
	
	/**
	 * 
	 * @return the total number of frames
	 */
	public int getTotalNumFrames() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_COUNT);
	}
	
	/**
	 * 
	 * @return the number of the empty frame
	 */
	public int getEmptyFrameNum() {
		return emptyFrameNum;
	}
	/**
	 * Changes the number of the empty frame
	 * @param emptyFrameNum
	 */
	public void setEmptyFrameNum(int emptyFrameNum) {
		this.emptyFrameNum = emptyFrameNum;
	}
	
	/**
	 * 	
	 * @return the amount of time for each timestep
	 */
	public double getTimeStep () {
		return timeStep;
	}
	
	/**
	 * Changes the timestep amount 
	 * @param timeStep
	 */
	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
	}
	
	/**
	 * 
	 * @return the starting frame number
	 */
	public int getStartFrameNum() {
		return startFrameNum;
	}
	
	/**
	 * Changes the starting frame number
	 * @param startFrameNum
	 */
	public void setStartFrameNum(int startFrameNum) {
		this.startFrameNum = startFrameNum;
	}
	
	/**
	 * 
	 * @return the number of the last frame
	 */
	public int getEndFrameNum() {
		return endFrameNum;
	}
	
	/**
	 * 
	 * @return the width of the frame
	 */
	public synchronized int getFrameWidth() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
	}
	
	/**
	 * 
	 * @return the height of the frame
	 */
	public synchronized int getFrameHeight() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
	}
	
	/**
	 * Changes the number of the last frame
	 * @param endFrameNum
	 */
	public void setEndFrameNum(int endFrameNum) {
		this.endFrameNum = endFrameNum;
	}
	
	/**
	 * 
	 * @return number of pixels/cm in the X direction
	 */
	public double getXPixelsPerCm() {
		return xPixelsPerCm;
	}
	
	/**
	 * Changes X pixels/cm
	 * @param xPixelsPerCm
	 */
	public void setXPixelsPerCm(double xPixelsPerCm) {
		this.xPixelsPerCm = xPixelsPerCm;
	}
	
	/**
	 * 
	 * @return number of pixels per cm in the Y direction
	 */
	public double getYPixelsPerCm() {
		return yPixelsPerCm;
	}
	
	/**
	 * changes Y pixels/cm
	 * @param yPixelsPerCm
	 */
	public void setYPixelsPerCm(double yPixelsPerCm) {
		this.yPixelsPerCm = yPixelsPerCm;
	}
	
	/**
	 * 
	 * @return the average pixels/cm from Y and X pixels/cm
	 */
	public double getAvgPixelsPerCm() {
		return (xPixelsPerCm + yPixelsPerCm)/2;
	}
	
	/**
	 * 
	 * @return a rectangle that is area in which the tracking takes place
	 */
	public Rectangle getArenaBounds() {
		return arenaBounds;
	}
	
	/**
	 * changes the area in which the tracking takes place
	 * @param arenaBounds
	 */
	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}
	
	/**
	 * 
	 * @param numFrames
	 * @return converts frames into seconds
	 */
	public double convertFrameNumsToSeconds(int numFrames) {
		return numFrames / getFrameRate();
	}
	
	/**
	 * 
	 * @param numSecs
	 * @return Converts seconds into frames
	 */
	public int convertSecondsToFrameNums(double numSecs) {
		return (int) Math.round(numSecs * getFrameRate());
	}
	
	/**
	 * 
	 * @return the length of the video
	 */
	public double getVidSize() {
		return vidSize;
	}
	
	/**
	 * Changes the length of the video
	 * @param vidSize
	 */
	public void setVidSize(double vidSize) {
		this.vidSize = vidSize;
	}
	
	/**
	 * 
	 * @param currentFrameNum
	 * @return A string showing the current time
	 */
	public String getCurrentTime(int currentFrameNum) {
		int frameRate = (int) getFrameRate();
		int framePerMin = (int) frameRate * 60;
		int framePerHour = (int) framePerMin * 60;

		String hours = String.format("%02d", currentFrameNum / framePerHour);
		String minutes = String.format("%02d", currentFrameNum / framePerMin - Integer.parseInt(hours) * 60);
		String seconds = String.format("%02d",
				currentFrameNum / frameRate - Integer.parseInt(hours) * 3600 - Integer.parseInt(minutes) * 60);

		String time = hours + ":" + minutes + ":" + seconds;
		return time;
	}

}
