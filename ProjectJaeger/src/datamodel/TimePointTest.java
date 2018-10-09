package datamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import static datamodel.TimePoint.*;
import org.junit.jupiter.api.Test;

class TimePointTest {
	private double x=100;     // location
	private double y=200;  
	
	@Test
	void testGetDistanceTo() {
		TimePoint Other = new TimePoint(100,200,456);
	//	assertEquals(3, TimePoint.getDistanceTo(Other));
	}

}
