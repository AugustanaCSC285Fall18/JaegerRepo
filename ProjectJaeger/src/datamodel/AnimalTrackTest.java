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
		assertEquals(4, testingAnimalTrack.getTimePoints());
		
	}

}
