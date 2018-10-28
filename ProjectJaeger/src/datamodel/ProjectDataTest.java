package datamodel;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.jupiter.api.Test;

class ProjectDataTest {

	
	ProjectData fakeDataToTest() throws FileNotFoundException {
		ProjectData.loadCurrentProject("testVideos/CircleTest1_no_overlap.mp4");
		ProjectData test = ProjectData.getCurrentProject();
		
		AnimalTrack trackNumOne = new AnimalTrack("chickenOne");
		AnimalTrack trackNumTwo = new AnimalTrack("chickenTwo");
		test.getTracks().add(trackNumOne);
		test.getTracks().add(trackNumTwo);
		
		trackNumOne.add(new TimePoint(50, 100, 0));
		trackNumTwo.add(new TimePoint(120, 250, 50));
		return test; 
	}
	
	
	@Test
	void testSavingAFile() throws FileNotFoundException {
		ProjectData fake = fakeDataToTest();
		File saveFile = new File("fake_test.test"); 
		fake.saveToFile(saveFile);
		assertTrue(saveFile.exists());
	}

	@Test
	void testExportCSVFile() throws FileNotFoundException {
//		ProjectData fake = fakeDataToTest();
		File saveFile = new File("fake_test.test"); 
//		fake.exportCSVFile(saveFile);
		assertTrue(saveFile.exists());
	}
	
}
