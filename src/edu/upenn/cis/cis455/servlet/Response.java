package edu.upenn.cis.cis455.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import edu.upenn.cis.cis455.webserver.ThreadPool;

/**
 * @author tjgreen
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class Response implements HttpServletResponse {

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	
	
	private HashMap<String, ArrayList<String>> headers = new HashMap<String, ArrayList<String>>();
	public String version;
	private int status = 200;
	private Locale locale = null;
	private String charEncoding = "ISO-8859-1";
	private int contentLength;
	private String contentType = "text/html";
	private int bufferSize = 0;
	private StringWriter stringWriter;
	private boolean committed = false;

	private HashMap<Integer, String>statusMsg = new HashMap<>();
	
	public void addCookie(Cookie arg0) {
		String cookieName = arg0.getName();
		String cookieValue = arg0.getValue();
		String cookiePath = arg0.getPath();
		String cookieExpiry = String.valueOf(arg0.getMaxAge());
		String cookieDomain = arg0.getDomain();
		String cookieVersion = "HttpOnly";
		
		StringBuilder cookieHeader = new StringBuilder();
		cookieHeader.append(cookieName + " = " + cookieValue + "; ");
		cookieHeader.append("Max-Age"+ " = " + cookieExpiry + "; ");
		cookieHeader.append("Path"+ " = " + cookiePath + "; ");
		cookieHeader.append("Domain"+ " = " + cookieDomain + "; ");
		cookieHeader.append(cookieVersion);
		
		if (headers.containsKey("Set-Cookie")){
			headers.get("Set-Cookie").add(cookieHeader.toString());
		}
		else{
			ArrayList<String> cookie = new ArrayList<>();
			cookie.add(cookieHeader.toString());
			headers.put("Set-Cookie", cookie);
		}
		
	}

	public void setVersion(String version){
		this.version = version;
	}
	
	public void setStatusMsgMap(){
		statusMsg.put(200, "OK");
		statusMsg.put(400, "Bad Request");
		statusMsg.put(304, "Not Modified");
		statusMsg.put(412, "Pre Condition Failed");
		statusMsg.put(404, "Not Found");
		statusMsg.put(300, "Redirected");
		statusMsg.put(403, "Forbidden");
		statusMsg.put(500, "Server Error");
		statusMsg.put(405, "Method Not Allowed");
		
	}
	public boolean containsHeader(String arg0) {
		if (headers.containsKey(arg0)){
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	public String encodeURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	public String encodeRedirectURL(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 */
	public String encodeUrl(String arg0) {
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 */
	public String encodeRedirectUrl(String arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	public void sendError(int arg0, String arg1) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	public void sendError(int arg0) throws IOException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	public void sendRedirect(String arg0) throws IOException {
		System.out.println("[DEBUG] redirect to " + arg0 + " requested");
		System.out.println("[DEBUG] stack trace: ");
		Exception e = new Exception();
		StackTraceElement[] frames = e.getStackTrace();
		for (int i = 0; i < frames.length; i++) {
			System.out.print("[DEBUG]   ");
			System.out.println(frames[i].toString());
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	public void setDateHeader(String arg0, long arg1) {
		ArrayList<String> val  = new ArrayList<>();
		val.add(String.valueOf(arg1));
		headers.put(arg0,  val);
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	public void addDateHeader(String arg0, long arg1) {
		// TODO Auto-generated method stub
		if (headers.containsKey(arg0)){
			headers.get(arg0).add(String.valueOf(arg1));
		}
		else {
			setDateHeader(arg0, arg1);
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	public void setHeader(String arg0, String arg1) {
		if (containsHeader(arg0)) {
			headers.get(arg0).add(0, arg1);
		}
		else{
			ArrayList<String> val = new ArrayList<String>();
			val.add(arg1);
			headers.put(arg0, val);
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	public void addHeader(String arg0, String arg1) {
		if (containsHeader(arg0)){
			headers.get(arg0).add(arg1);
		}
		else {
			setHeader(arg0, arg1);
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	public void setIntHeader(String arg0, int arg1) {
		if (containsHeader(arg0)){
			headers.get(arg0).add(0, String.valueOf(arg1));
		}
		else {
			ArrayList<String> val = new ArrayList<>();
			val.add(String.valueOf(arg1));
			headers.put(arg0, val);
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	public void addIntHeader(String arg0, int arg1) {
		if (containsHeader(arg0)){
			headers.get(arg0).add(String.valueOf(arg1));
		}
		else {
			setIntHeader(arg0, arg1);
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	public void setStatus(int arg0) {
		status = arg0;

	}

	/* DEPRECATED
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 */
	public void setStatus(int arg0, String arg1) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		return charEncoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getContentType()
	 */
	public String getContentType() {
		return contentType;
	}

	/* NOT REQUIRED
	 * (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	public ServletOutputStream getOutputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	public PrintWriter getWriter() throws IOException {
		System.out.println("Getting writer object");
		stringWriter = new StringWriter(bufferSize);
		//Socket clientSocket = ThreadPool.getClientSocket();
		return new ServletWriter(stringWriter, true, this);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0) {
		charEncoding = arg0;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	public void setContentLength(int arg0) {
		System.out.println("Setting content length");
		contentLength = arg0;
		setIntHeader("Content-Length", contentLength);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	public void setContentType(String arg0) {
		System.out.println("Setting content type");
		contentType = arg0;
		setHeader("Content-Type", contentType);

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	public void setBufferSize(int arg0) {
		bufferSize = arg0;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	public StringBuilder getHeaderResponse(){
		ArrayList<String>vals;
		StringBuilder headerResponse= new StringBuilder();
		
		setStatusMsgMap();
		String statusStr = statusMsg.get(status);
		headerResponse.append(version + " " + status + " " + statusStr + "\r\n");
		
		for(String key : headers.keySet()){
			vals = headers.get(key);
			headerResponse.append(key + " : ");
			for(int i = 0 ; i < vals.size() ; i++){
				if (i == 0)
					headerResponse.append(vals.get(i));
				else
					headerResponse.append(", " + vals.get(i));
			}
				headerResponse.append("\r\n");
		}
		return headerResponse;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	public void flushBuffer() throws IOException {
		if (!committed){
			Socket clientSocket = ThreadPool.getClientSocket();
			//add headers to reponse
			StringBuilder response = getHeaderResponse();
			response.append("\r\n");
			OutputStream out = clientSocket.getOutputStream();
			out.write(response.toString().getBytes());
			StringBuffer body = stringWriter.getBuffer();
			out.write(body.toString().getBytes(charEncoding));
			out.flush();
			out.close();
			clientSocket.close();
			committed = true;
		}

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	public void resetBuffer() {
		stringWriter = new StringWriter(bufferSize);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	public boolean isCommitted() {
		
		return committed;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#reset()
	 */
	public void reset() {
		status = 200; //default value
		headers.clear();
		stringWriter = new StringWriter(0);
		

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale arg0) {
		locale = arg0;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	public Locale getLocale() {
		
		return locale;
	}
	
	

}
