package edu.upenn.cis.cis455.servlet;

import javax.servlet.*;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.webserver.HttpServer;
import edu.upenn.cis.cis455.webserver.ThreadPool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * @author Nick Taylor
 */
public class Context implements ServletContext {
	static final Logger logger = Logger.getLogger(ThreadPool.class); 

	private HashMap<String,Object> attributes;
	private HashMap<String,String> initParams;
	
	public Context() {
		attributes = new HashMap<String,Object>();
		initParams = new HashMap<String,String>();
	}
	
	public Object getAttribute(String name) {
		return attributes.get(name);
	}
	
	public Enumeration getAttributeNames() {
		Set<String> keys = attributes.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public ServletContext getContext(String name) {
		return this;
	}
	
	public String getInitParameter(String name) {
		return initParams.get(name);
	}
	
	public Enumeration getInitParameterNames() {
		Set<String> keys = initParams.keySet();
		Vector<String> atts = new Vector<String>(keys);
		return atts.elements();
	}
	
	public int getMajorVersion() {
		return 2;
	}
	
	public String getMimeType(String file) {
		return null;
	}
	
	public int getMinorVersion() {
		return 4;
	}
	
	//not required
	public RequestDispatcher getNamedDispatcher(String name) {
		return null;
	}
	
	public String getRealPath(String path) {
		int port = HttpServer.getPortNumber();
		String realPath = "http://localhost:" + String.valueOf(port) + "" + path;
 		return realPath;
	}
	
	//not required
	public RequestDispatcher getRequestDispatcher(String name) {
		return null;
	}
	
	//not required
	public java.net.URL getResource(String path) {
		return null;
	}
	
	//not required
	public java.io.InputStream getResourceAsStream(String path) {
		return null;
	}
	
	//not required
	public java.util.Set getResourcePaths(String path) {
		return null;
	}
	
	public String getServerInfo() {
		return "localhost";
	}
	
	public Servlet getServlet(String name) {
		return null;
	}
	
	public String getServletContextName() {
		return HttpServer.getWebAppName();
	}
	
	//Deprecated
	public Enumeration getServletNames() {
		return null;
	}
	
	//Deprecated
	public Enumeration getServlets() {
		return null;
	}
	
	//not required
	public void log(Exception exception, String msg) {
		log(msg, (Throwable) exception);
	}
	//not required
	public void log(String msg) {
	
	}
	
	//not required
	public void log(String message, Throwable throwable) {
		return;
	}
	
	public void removeAttribute(String name) {
		attributes.remove(name);
	}
	
	public void setAttribute(String name, Object object) {
		attributes.put(name, object);
	}
	
	public void setInitParam(String name, String value) {
		initParams.put(name, value);
	}
}
