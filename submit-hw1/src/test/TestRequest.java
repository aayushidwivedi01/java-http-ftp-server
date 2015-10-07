package test;
/**
 * Testcases for Request class
 */

import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.junit.Test;

import edu.upenn.cis.cis455.servlet.Request;
import edu.upenn.cis.cis455.servlet.Session;


public class TestRequest {
	
	
	//Tests whether authorization type is BASIC
	@Test
	public void testGetAuthType(){
		Request request = new Request();
		String response = request.getAuthType();
		assertEquals("Authorization type not BASIC", "BASIC", response);
	}
	
	//Tests whether a null Session is detected by hasSession()
	@Test
	public void testHasSessionIsFalse(){
		Session session = null;
		Request request = new Request(session);
		boolean response = request.hasSession();
		assertEquals("Session not null", false, response );
	}
	
	//Tests whether a non-null session is correctly detected by hasSession
	@Test
	public void testHasSessionIsTrue(){
		Session session = new Session();
		Request request = new Request(session);
		boolean response = request.hasSession();
		assertEquals("Session not null", true, response );
	}
	
	//Tests getSession(false) does not create a new session
	@Test
	public void testGetSessionForNullSession(){
		Session session = null;
		Request request = new Request(session);
		HttpSession response = request.getSession(false);
		assertEquals("Session not null", null, response);
	}
	
	//Tests whether correct header is returned by getHeader()
	@Test
	public void testGetHeader(){
		Request request = new Request();
		HashMap<String, ArrayList<String>>otherHeaders = request.getHttpOtherHeaders();
		ArrayList<String> contentType = new ArrayList<String>();
		contentType.add("text/html");
		otherHeaders.put("Content-Type".toLowerCase(), contentType);
		String response = request.getHeader("Content-Type");
		assertEquals("Wrong content type", "text/html" , response);
	}
	
	//Tests whether correct query string is returned by getQueryString()
	@Test
	public void testGetQueryString(){
		Request request = new Request();
		HashMap<String, String>mainHeaders = request.getHttpMainHeaders();
		String	path = "/query/string?param=val&param2=val2";
		mainHeaders.put("path", path);
		String response = request.getQueryString();
		
		assertEquals("param=val&param2=val2", response);
		}
	
	
	
	
	
}
