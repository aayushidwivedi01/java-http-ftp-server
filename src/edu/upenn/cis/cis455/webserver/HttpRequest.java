package edu.upenn.cis.cis455.webserver;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.Logger;


public class HttpRequest {
	static final Logger logger = Logger.getLogger(ThreadPool.class); 
	public HashMap<String, String> mainRequestHeaders = new HashMap<>(); 
	public HashMap<String, ArrayList<String>>otherHeaders = new HashMap<>();
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
		 
		 mainRequestHeaders.put("action", splitRequest[0]);
		 //Check if requested resource is a URL
		 String serverAdd = "http://localhost:" + String.valueOf(portNo);
		 String resourcePath = splitRequest[1];
		 if (resourcePath.startsWith(serverAdd)){
			 resourcePath = resourcePath.substring(serverAdd.length());
			 if ( !resourcePath.startsWith("/")){
				 resourcePath = "/" + resourcePath;
			 }
			 mainRequestHeaders.put("path", resourcePath);
		 }
		 else {
			 mainRequestHeaders.put("path", resourcePath);
		 }
		 
		 mainRequestHeaders.put("version", splitRequest[2]);
		 mainRequestHeaders.put("portNo", String.valueOf(portNo));

		
	 }	
	
	public void parseOtherHeaders(){
		for( String req : otherRequests){
			 String[] pair = req.split(":", 2);
			 if (!otherHeaders.containsKey(pair[0])){
				 ArrayList<String>value = new  ArrayList<String>();
				 value.add(pair[1]);
				 otherHeaders.put(pair[0], value);
			 }
			 else{
				 otherHeaders.get(pair[0]).add(pair[1]);
			 }
			 
		 }
	}
}
