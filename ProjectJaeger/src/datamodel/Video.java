package datamodel;

import java.awt.Rectangle;
import java.io.FileNotFoundException;

import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

public class Video {
	
	private String filePath;
	private VideoCapture vidCap;
	private int emptyFrameNum;
	private int startFrameNum;
	private int endFrameNum;
	private double xPixelsPerCm;
	private double yPixelsPerCm;
	private Rectangle arenaBounds; 
	private double timeStep;
	private double vidSize;

	
		
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
	public synchronized void connectVideoCapture() throws FileNotFoundException {
		this.vidCap = new VideoCapture(filePath);
		if (!vidCap.isOpened()) {
			throw new FileNotFoundException("Unable to open video file: " + filePath);
		}
	}
	public synchronized void setCurrentFrameNum(int seekFrame) {
		vidCap.set(Videoio.CV_CAP_PROP_POS_FRAMES, (double) seekFrame);
	}
	public synchronized int getCurrentFrameNum() {
		return (int) vidCap.get(Videoio.CV_CAP_PROP_POS_FRAMES);
	}
	
	public synchronized Mat readFrame() {
		Mat frame = new Mat();
		vidCap.read(frame);
		return frame;
	}
	public String getFilePath() {
		return this.filePath;
	}
	/** 
	 * @return frames per second
	 */
	public double getFrameRate() {
		return vidCap.get(Videoio.CAP_PROP_FPS);
	}
	public int getTotalNumFrames() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_COUNT);
	}

	public int getEmptyFrameNum() {
		return emptyFrameNum;
	}

	public void setEmptyFrameNum(int emptyFrameNum) {
		this.emptyFrameNum = emptyFrameNum;
	}
		
	public double getTimeStep () {
		return timeStep;
	}
	
	public void setTimeStep(double timeStep) {
		this.timeStep = timeStep;
	}
	
	public int getStartFrameNum() {
		return startFrameNum;
	}
	
	public void setStartFrameNum(int startFrameNum) {
		this.startFrameNum = startFrameNum;
	}

	public int getEndFrameNum() {
		return endFrameNum;
	}
	
	public synchronized int getFrameWidth() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_WIDTH);
	}

	public synchronized int getFrameHeight() {
		return (int) vidCap.get(Videoio.CAP_PROP_FRAME_HEIGHT);
	}

	public void setEndFrameNum(int endFrameNum) {
		this.endFrameNum = endFrameNum;
	}

	public double getXPixelsPerCm() {
		return xPixelsPerCm;
	}
	
	public void setXPixelsPerCm(double xPixelsPerCm) {
		this.xPixelsPerCm = xPixelsPerCm;
	}

	public double getYPixelsPerCm() {
		return yPixelsPerCm;
	}

	public void setYPixelsPerCm(double yPixelsPerCm) {
		this.yPixelsPerCm = yPixelsPerCm;
	}

	public double getAvgPixelsPerCm() {
		return (xPixelsPerCm + yPixelsPerCm)/2;
	}

	public Rectangle getArenaBounds() {
		return arenaBounds;
	}

	public void setArenaBounds(Rectangle arenaBounds) {
		this.arenaBounds = arenaBounds;
	}
	
	public double convertFrameNumsToSeconds(int numFrames) {
		return numFrames / getFrameRate();
	}

	public int convertSecondsToFrameNums(double numSecs) {
		return (int) Math.round(numSecs * getFrameRate());
	}
	
	public double getVidSize() {
		return vidSize;
	}
	public void setVidSize(double vidSize) {
		this.vidSize = vidSize;
	}
	
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
