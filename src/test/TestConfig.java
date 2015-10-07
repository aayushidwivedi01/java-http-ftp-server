package test;

/**
 * Testcases for Config class
 */
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import edu.upenn.cis.cis455.servlet.Config;
import edu.upenn.cis.cis455.servlet.Context;

public class TestConfig {
	
	//Tests whether getInitParameters() returns the correct values
	@Test
	public void testGetInitParametes(){
		Context context = new Context();
		
		Config config = new Config("test", context);
		config.setInitParam("test", "value");
		String val = config.getInitParameter("test");
		assertEquals("Incorrect parameter value", "value",val);
	}
	
	//tests whether getServletName returns the correct servlet name
	@Test
	public void testGetServletName(){
		Context context = new Context();
		Config config = new Config("test", context);
		
		String val = config.getServletName();
		assertEquals("Incorrect servlet name", "test",val);
	}
}
