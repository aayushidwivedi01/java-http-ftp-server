package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.Cookie;

import org.apache.log4j.Logger;


public class HttpRequest {
	static final Logger logger = Logger.getLogger(ThreadPool.class); 
	public HashMap<String, String> mainRequestHeaders = new HashMap<>(); 
	public HashMap<String, ArrayList<String>>otherHeaders = new HashMap<>();
	public HashMap<String, ArrayList<String>> m_params = new HashMap<>();
	public Cookie[] cookieArr;
	private String mainRequest;
	private int portNo;
	private ArrayList<String>otherRequests = new ArrayList<String>();
	public HttpRequest(String mainRequest, int portNo, ArrayList<String>otherRequests){
		this.mainRequest = mainRequest;
		this.portNo = portNo;
		this.otherRequests = otherRequests;
	}
	
	public  void parseRequestHeaders(){
		 logger.info("[Output from log4j] Parsing request..");
		  
		 
		 String[] splitRequest = mainRequest.split(" ");
		 
		 mainRequestHeaders.put("action", splitRequest[0].trim());
		 //Check if requested resource is a URL
		 String serverAdd = "http://localhost:" + String.valueOf(portNo);
		 String resourcePath = splitRequest[1].trim();
		 if (resourcePath.startsWith(serverAdd)){
			 resourcePath = resourcePath.substring(serverAdd.length());
			 if ( !resourcePath.startsWith("/")){
				 resourcePath = "/" + resourcePath;
			 }
			 
			 
			 mainRequestHeaders.put("path", resourcePath);
		 }
		 else {
//			 if(resourcePath.contains("?")){
//				 System.out.println("Contains a question");
//				 String[] pair = resourcePath.split("\\?", 2);
//				 resourcePath = pair[0];
//				 parseBody(pair[1]);
//				 
//			 }
			 mainRequestHeaders.put("path", resourcePath);
		 }
		 
		 mainRequestHeaders.put("version", splitRequest[2].trim());
		 mainRequestHeaders.put("portNo", String.valueOf(portNo));

		
	 }	
	
	public void parseOtherHeaders(){
		for( String req : otherRequests){
			 String[] pair = req.split(":", 2);
			 if (!otherHeaders.containsKey(pair[0].trim())){
				 ArrayList<String>value = new  ArrayList<String>();
				 value.add(pair[1].trim());
				 otherHeaders.put(pair[0].trim(), value);
			 }
			 else{
				 otherHeaders.get(pair[0].trim()).add(pair[1].trim());
			 }
			 
		 }
	}
	
	public void parseBody(String body) {
		if ( body.contains("&")){
		String[] params = body.split("&");
		
		for(String param : params){
			String[] pair = param.split("=");
			if(m_params.containsKey(pair[0])){
				m_params.get(pair[0]).add(pair[1]);
			}
			else{
				ArrayList<String> val = new ArrayList<>();
				val.add(pair[1]);
				m_params.put(pair[0], val);
			}
			
			}
		}
		else{
			String[] pair = body.split("=");
			if(m_params.containsKey(pair[0])){
				m_params.get(pair[0]).add(pair[1]);
			}
			else{
				ArrayList<String> val = new ArrayList<>();
				val.add(pair[1]);
				m_params.put(pair[0], val);
			}
			
		}
		
		
	}
	public void parseCookie(){
		if (otherHeaders.containsKey("Cookie")){
			
			String[] cookies = otherHeaders.get("Cookie").get(0).split(";") ;
			
			cookieArr = new Cookie[cookies.length];
			for (int i = 0; i < cookies.length ; i++){
				String[] cookiePair = cookies[i].split("=");
				Cookie cookie = new Cookie(cookiePair[0].trim(), cookiePair[1].trim());
				cookieArr[i] = cookie;
			}
		
		}
	}
}
