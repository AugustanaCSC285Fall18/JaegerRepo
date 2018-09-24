package edu.augustana.csc285.jaeger;

public class Video {
		
	private double frameRate;
	private String fileName;
	private int totalNumFrames;
	
	
	
	public Video(double frameRate, String fileName, int totalNumFrames) {
		super();
		this.frameRate = frameRate;
		this.fileName = fileName;
		this.totalNumFrames = totalNumFrames;
	}
	
	public double getFrameRate() {
		return frameRate;
	}
	public void setFrameRate(double frameRate) {
		this.frameRate = frameRate;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public int getTotalNumFrames() {
		return totalNumFrames;
	}
	public void setTotalNumFrames(int totalNumFrames) {
		this.totalNumFrames = totalNumFrames;
	}
}
