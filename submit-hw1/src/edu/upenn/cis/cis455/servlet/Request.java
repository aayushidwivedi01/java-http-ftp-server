package edu.upenn.cis.cis455.servlet;

import edu.upenn.cis.cis455.webserver.HttpRequest;
import edu.upenn.cis.cis455.webserver.ThreadPool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.security.Principal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import edu.upenn.cis.cis455.webserver.HttpServer;

/**
 * @author Todd J. Green
 */
public class Request implements HttpServletRequest {
	private Properties m_props = new Properties();
	private Session m_session;
	private String m_method;
	private String servletPath;
	private String contentType = "text/html";
	private Cookie[] cookieArr;
	HashMap<String, String>httpMainHeaders = new HashMap<>();
	HashMap<String, ArrayList<String>>m_params = new HashMap<>();;

	HashMap<String, ArrayList<String>>httpOtherHeaders = new HashMap<>();
	private String charEncoding = "ISO-8859-1";
	private Socket clientSocket = null;
	private Locale locale = null;

	HashMap<String, String>m_servlets;
	
	public Request() {
	}
	
	public Request(Session session) {
		m_session = session;
	}
	
	
	public Request(Session session, HttpRequest httpRequest, String servletPath) {
		this.httpMainHeaders = httpRequest.mainRequestHeaders;
		this.httpOtherHeaders = httpRequest.otherHeaders;
		this.cookieArr = httpRequest.cookieArr;
		this.m_params = httpRequest.m_params;
		this. servletPath = servletPath;
		this.m_session = session;
	}
	
	public HashMap<String, ArrayList<String>>getHttpOtherHeaders(){
		return httpOtherHeaders;
	}
	
	public HashMap<String,String>getHttpMainHeaders(){
		return httpMainHeaders;
	}
	
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getAuthType()
	 */
	public String getAuthType() {
		// TODO Auto-generated method stub
		return BASIC_AUTH;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getCookies()
	 */
	public Cookie[] getCookies() {
		if (httpOtherHeaders.containsKey("Cookie")){
			return cookieArr;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getDateHeader(java.lang.String)
	 */
	public long getDateHeader(String arg0) {
		// TODO Auto-generated method stub
		
		if (httpOtherHeaders.containsKey(arg0)){
			SimpleDateFormat sdf =  new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
			try {
				Date date =  sdf.parse(sdf.format(arg0));
				
				return date.getTime();
			} catch (ParseException e) {
				//e.printStackTrace();
				throw new IllegalArgumentException();
			}catch (IllegalArgumentException e) {
				e.printStackTrace();
				throw new IllegalArgumentException();
			}
			
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 */
	public String getHeader(String arg0) {
		// TODO Auto-generated method stub
		if (httpOtherHeaders.containsKey(arg0.toLowerCase())){
			return httpOtherHeaders.get(arg0.toLowerCase()).remove(0);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 */
	public Enumeration<String> getHeaders(String arg0) {
		// TODO Auto-generated method stub
		if ( httpOtherHeaders.containsKey(arg0)){
			Enumeration<String> headers = Collections.enumeration(httpOtherHeaders.get(arg0)) ;
			return headers;
		}
		
		
		return Collections.emptyEnumeration();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 */
	public Enumeration<String> getHeaderNames() {
		if(!httpOtherHeaders.isEmpty()){
			Enumeration<String> headerNames= Collections.enumeration(httpOtherHeaders.keySet());
			return headerNames;
		}
		
		return Collections.emptyEnumeration();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getIntHeader(java.lang.String)
	 */
	public int getIntHeader(String arg0) {
		if (httpOtherHeaders.containsKey(arg0)){
			try{
				int header = Integer.valueOf(getHeader(arg0));
				return header;
			}
			catch(Exception e){
				throw new NumberFormatException();
			}
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getMethod()
	 */
	public String getMethod() {
		m_method = httpMainHeaders.get("action");
		return m_method;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathInfo()
	 */
	public String getPathInfo() {
		if ( httpMainHeaders.containsKey("path")){
			String path = httpMainHeaders.get("path");
			String pathInfo = path;
			
			if (path.contains("?")){
				path = path.split("\\?", 2)[0];
			}
			if ( path.startsWith(servletPath)){
				pathInfo = path.substring(servletPath.length());
			}
			
			return pathInfo;
		}
		
		return null;
	}

	/* NOT REQUIRED
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getPathTranslated()
	 */
	public String getPathTranslated() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getContextPath()
	 */
	public String getContextPath() {
		//return empty string
		return "";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getQueryString()
	 */
	public String getQueryString() {
		
		if ( httpMainHeaders.containsKey("path")){
			String path = httpMainHeaders.get("path");
			if (path.contains("?")){
				String query = path.split("\\?", 2)[1];
				return query;
			}
		}
		return null;
	}

	public void setClientSocket(Socket socket){
		clientSocket = socket;
	
	}
	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRemoteUser()
	 */
	public String getRemoteUser() {
		// TODO Auto-generated method stub
	
		return null;
	}

	/* NOT REQUIRED
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isUserInRole(java.lang.String)
	 */
	public boolean isUserInRole(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* NOT REQUIRED
	 * (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getUserPrincipal()
	 */
	public Principal getUserPrincipal() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestedSessionId()
	 */
	public String getRequestedSessionId() {
	
		return ThreadPool.getSessionId();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURI()
	 */
	public String getRequestURI() {
		
		if (httpMainHeaders.get("path").contains("?")){
			return httpMainHeaders.get("path").split("\\?", 2)[0];
		}
		else{
			return httpMainHeaders.get("path");
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getRequestURL()
	 */
	public StringBuffer getRequestURL() {
		StringBuffer url = new StringBuffer();
		url.append("http://localhost:" + httpMainHeaders.get("portNo") + getRequestURI());
		return url;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getServletPath()
	 */
	public String getServletPath() {
		return servletPath;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession(boolean)
	 */
	public HttpSession getSession(boolean arg0) {
		if (arg0) {
			if (! hasSession()) {
				m_session = new Session();
				//add this session to sessionMap
				synchronized(HttpServer.getSessionMap()){
					HttpServer.getSessionMap().put(m_session.getId(), m_session);
				}
			}
		} else {
			if (! hasSession()) {
				m_session = null;
			}
		}
		return m_session;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#getSession()
	 */
	public HttpSession getSession() {
		return getSession(true);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdValid()
	 */
	public boolean isRequestedSessionIdValid() {
		String sessionId = getRequestedSessionId();
		 
		synchronized(HttpServer.getSessionMap()){
			HashMap<String, Session> map = HttpServer.getSessionMap();
			if(map.containsKey(sessionId)){
				return map.get(sessionId).isValid();
			}
			else 
				return false;
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromCookie()
	 */
	public boolean isRequestedSessionIdFromCookie() {
		if(getRequestedSessionId() != null)
			return true;
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromURL()
	 */
	public boolean isRequestedSessionIdFromURL() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServletRequest#isRequestedSessionIdFromUrl()
	 */
	public boolean isRequestedSessionIdFromUrl() {
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttribute(java.lang.String)
	 */
	public Object getAttribute(String arg0) {
		return m_props.get(arg0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getAttributeNames()
	 */
	public Enumeration getAttributeNames() {
		return m_props.keys();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getCharacterEncoding()
	 */
	public String getCharacterEncoding() {
		// TODO Auto-generated method stub
		
		return charEncoding;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setCharacterEncoding(java.lang.String)
	 */
	public void setCharacterEncoding(String arg0)
			throws UnsupportedEncodingException {
		
		charEncoding = arg0;

	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentLength()
	 */
	public int getContentLength() {
		if (httpOtherHeaders.containsKey("Content-Length")){
			return Integer.valueOf(httpOtherHeaders.get("Content-Length").get(0));
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getContentType()
	 */
	public String getContentType() {
		if (httpOtherHeaders.containsKey("Content-Type")){
			return httpOtherHeaders.get("Content-Type").get(0);
		}
		return contentType;
	}

	/* NOT REQUIRED
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getInputStream()
	 */
	public ServletInputStream getInputStream() throws IOException {
		
		return null;
	}

	/* 
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameter(java.lang.String)
	 */
	public String getParameter(String arg0) {
		return m_params.get(arg0).get(0);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterNames()
	 */
	public Enumeration getParameterNames() {
		return  Collections.enumeration(m_params.keySet());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterValues(java.lang.String)
	 */
	public String[] getParameterValues(String arg0) {
		
		if (m_params.containsKey(arg0)){
			String[] paramValues = new String[m_params.get(arg0).size()];
			for(int i = 0; i < m_params.get(arg0).size() ; i++){
				paramValues[i]=  m_params.get(arg0).get(i);
			}
			
			return paramValues;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getParameterMap()
	 */
	public Map getParameterMap() {
		
		return m_params;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getProtocol()
	 */
	public String getProtocol() {
		// TODO Auto-generated method stub
		return httpMainHeaders.get("version");
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getScheme()
	 */
	public String getScheme() {
		// TODO Auto-generated method stub
		return "http";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerName()
	 */
	public String getServerName() {
		if (httpOtherHeaders.containsKey("host")) {
			String host = httpOtherHeaders.get("host").get(0);
			if (host.contains(":")){
				host = host.split(":",2)[0];
			}
			else{
				InetAddress inetAddr = HttpServer.getServerSocket().getInetAddress();
				host = inetAddr.getHostName();
			}
			return host;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getServerPort()
	 */
	public int getServerPort() {
		return Integer.valueOf(httpMainHeaders.get("portNo"));
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getReader()
	 */
	public BufferedReader getReader() throws IOException {
		String body = ThreadPool.getRequestBody();
		if (body == null){
			BufferedReader br = new BufferedReader(new StringReader(""));
			return br;
		}
		else {
			BufferedReader br = new BufferedReader(new StringReader(body));
			return br;
		}
		
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteAddr()
	 */
	public String getRemoteAddr() {
		Socket clientSocket = ThreadPool.getClientSocket();
		String clientAddr = clientSocket.getInetAddress().getHostAddress();
		return clientAddr;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemoteHost()
	 */
	public String getRemoteHost() {
		Socket clientSocket = ThreadPool.getClientSocket();
		String host = clientSocket.getInetAddress().getCanonicalHostName();
		return host;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#setAttribute(java.lang.String, java.lang.Object)
	 */
	public void setAttribute(String arg0, Object arg1) {
		m_props.put(arg0, arg1);
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#removeAttribute(java.lang.String)
	 */
	public void removeAttribute(String arg0) {
		// TODO Auto-generated method stub
		if ( m_props.contains(arg0)){
			m_props.remove(arg0);
		}

	}
	
	public Locale setLocale(Locale locale){
		this.locale = locale;
		return locale;
	}

	/* NOT REQUIRED
	 *  (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocale()
	 */
	public Locale getLocale() {// TODO Auto-generated method stub
		return locale;
	}

	/* NOT-REQUIRED
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocales()
	 */
	public Enumeration getLocales() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#isSecure()
	 */
	public boolean isSecure() {
		// TODO Auto-generated method stub
		return false;
	}

	/* NOT REQUIRED
	 * (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRequestDispatcher(java.lang.String)
	 */
	public RequestDispatcher getRequestDispatcher(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRealPath(java.lang.String)
	 */
	public String getRealPath(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getRemotePort()
	 */
	public int getRemotePort() {
		String socketAddr = clientSocket.getRemoteSocketAddress().toString();
		if ( socketAddr.contains(":")){
			int port = Integer.valueOf(socketAddr.split(":", 2)[1]);
			return port;
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalName()
	 */
	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalAddr()
	 */
	public String getLocalAddr() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequest#getLocalPort()
	 */
	public int getLocalPort() {
		// TODO Auto-generated method stub
		int portNo = Integer.valueOf(httpMainHeaders.get("portNo"));
		return portNo;
	}

	public void setMethod(String method) {
		m_method = method;
	}
	
	public void setParameter(String key, String value) {
		if (m_params.containsKey(key)){
			m_params.get(key).add(value);
		}
		else{
			ArrayList<String> val = new ArrayList<>();
			val.add(value);
			m_params.put(key,  val);
		}
	}
	
	public void clearParameters() {
		m_params.clear();
	}
	
	public boolean hasSession() {
		return ((m_session != null) && m_session.isValid());
	}
		
	
}
