package test;

/**
 * Testcases for Session class
 */
import static org.junit.Assert.*;

import org.junit.Test;

import edu.upenn.cis.cis455.servlet.Session;

public class TestSession {
	
	//tests whether maxInterval is correctly set by setMaxInterval()
	@Test
	public void testSetMaxIterval(){
		Session session = new Session();
		session.setMaxInactiveInterval(4);
		int val = session.getMaxInactiveInterval();
		assertEquals("Not set properly", 4, val);
	}
	
	//tests whether getLastAccessesTime() returns the correct value;
	public void testGetLastAccessedTime(){
		Session session = new Session();
		long expected = session.getCreationTime();
		long actual = session.getLastAccessedTime();
		assertEquals("Incorrect last accessed time", expected, actual);
	}

}
