package datamodel;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TimePointTest {

	TimePoint tp1 = new TimePoint(40, 120, 3244);
	TimePoint tp2 = new TimePoint(100, 200, 456);
	TimePoint tp3 = new TimePoint(325, 435, 245);

	@Test
	void testGetDistanceTo() {

		assertEquals(100, tp1.getDistanceTo(tp2));
		assertEquals(0, tp1.getDistanceTo(tp1));
		assertEquals(Math.sqrt(180450), tp1.getDistanceTo(tp3));
		assertEquals(Math.sqrt(105850), tp3.getDistanceTo(tp2));
		
	}

	@Test
	void testGetTimeDiffAfter() {

		assertEquals(2788, tp1.getTimeDiffAfter(tp2));
		assertEquals(0, tp1.getTimeDiffAfter(tp1));
		assertEquals(-211, tp3.getTimeDiffAfter(tp2));
		assertEquals(-2999, tp3.getTimeDiffAfter(tp1));
	}
}
