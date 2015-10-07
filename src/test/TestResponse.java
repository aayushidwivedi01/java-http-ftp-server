package test;
/**
 * Testcases for Responce Class
 */
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;

import org.junit.Test;

import edu.upenn.cis.cis455.servlet.Request;
import edu.upenn.cis.cis455.servlet.Response;

public class TestResponse {

	//Tests whether addInitHeader returns the correct value for a know header
	@Test
	public void testaddIntHeader(){
		Response response = new Response();
		
		response.addIntHeader("IntHeader", 2);
		HashMap<String, ArrayList<String>>headers = response.getHeader();
		String val = headers.get("IntHeader").get(0);
		assertEquals("Wrong value", "2" , val);
		
	}
	
	//Tests whether containsHeader() returns the correct value
	@Test
	public void testContainsHeader(){
		Response response = new Response();
		response.addIntHeader("IntHeader", 2);
		boolean val = response.containsHeader("IntHeader");
		assertEquals("Does not contain the header", true, val );
	}
}
