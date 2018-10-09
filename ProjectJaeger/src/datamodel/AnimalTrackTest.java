package datamodel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AnimalTrackTest {

	@Test
	void testAdding() {
		AnimalTrack testingAnimalTrack = new AnimalTrack("Chickens");
		assertEquals("Chickens",testingAnimalTrack.getAnimalID());
		
		testingAnimalTrack.add(new TimePoint(100,200,0));		
		testingAnimalTrack.add(new TimePoint(210,110,1));		
		testingAnimalTrack.add(new TimePoint(300,250,4));	
		testingAnimalTrack.add(new TimePoint(300,250,5));
		assertEquals(4, testingAnimalTrack.getNumPoints());
		
		TimePoint point0 = testingAnimalTrack.getTimePointAtTime(0);
		//need equals method for TimePoints
		assertEquals(new TimePoint(100,200,0), point0);
		TimePoint point3 = testingAnimalTrack.getTimePointAtTime(3);
		assertNull(point3);
		TimePoint lastPt = testingAnimalTrack.getFinalTimePoint();
		assertEquals(5,lastPt.getFrameNum());
	}

}
