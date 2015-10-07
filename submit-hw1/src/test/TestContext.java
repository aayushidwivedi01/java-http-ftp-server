package test;
/**
 * Testcases for Context class
 * Tests setInitParameters() against known values
 */
import static org.junit.Assert.*;

import org.junit.Test;

import edu.upenn.cis.cis455.servlet.Context;

public class TestContext {

	
	
	@Test
	public void testSetInitParameters(){
		Context context = new Context();
		context.setInitParam("test", "value");
		String val = context.getInitParameter("test");
		assertEquals("Not set properly", "value", val);
	}
	
}
