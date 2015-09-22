package edu.upenn.cis.cis455.webserver;
/*
 * Main request handler
 * Validates requests; Processes requests;
 * Generates response
 * @param All Headers excluding the first request header
 */
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class RequestHandler{
	static final Logger logger = Logger.getLogger(RequestHandler.class);

	private byte[] body;
	private byte[] response;
	private String RESPONSE_CODE;
	private String RESPONSE_PHRASE;
	private String CONTENT_TYPE;
	private String CONTENT_LENGTH;
	private String SERVER_DATE;
	private Date LAST_MODIFIED;
	private String ACTION;
	private String VERSION;
	private Map<String, String> otherHeaders = new HashMap<>();
	
	//Constructor
	public RequestHandler(Map<String,String> otherHeaders){
		this.otherHeaders = otherHeaders;
	}
	
	//Check whether the requested resources is a valid/accessible path
	public boolean isValidPath(File file){
		
		try{
			if(file.exists()){
				if (file.canRead()){
					return true;
					}
				else {
					logger.error("[Forbidden Access]; Requested resource cannot be accessed");  
					RESPONSE_CODE = "403";
					RESPONSE_PHRASE = "Forbidden";
					return false;
				}
			}
			else {
				logger.error("[File Not Found]; Requested resource does not exist;");
				RESPONSE_CODE = "404";
				RESPONSE_PHRASE = "Not Found";
				return false;
			}
				}
		catch(SecurityException e){
			logger.error("[Forbidden Access]; Requested resource cannot be accessed");  
			RESPONSE_CODE = "403";
			RESPONSE_PHRASE = "Forbidden";
			return false;
		}
	
	}

	
	//Finds the MIME of requested resource
	// returns null if MIME not supported
	public String getMimeType(String resourcePath) {
		Path path = Paths.get(resourcePath);
		try{
		String mime = Files.probeContentType(path);
		return mime;
		}
		catch(IOException e){
			return null;
		}
		
		
	}
	
	//Get the server date
	public void getServerDate(){
		
		SimpleDateFormat sdf =  new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date currDate = new Date();
		SERVER_DATE =  sdf.format(currDate);
	}
	
	//Get last modified date of the requested resource
	public void getLastModified(File file){
	
		SimpleDateFormat sdf =  new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			LAST_MODIFIED =  sdf.parse(sdf.format(file.lastModified()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}
	
	//Check whether the resource has been modified since the last request
	public boolean isModified(){
		Date currentDate = LAST_MODIFIED;
		Date last_modified;
		SimpleDateFormat reqsdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
		reqsdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Calendar calCurr = Calendar.getInstance();
		Calendar calTimestamp = Calendar.getInstance();
		calCurr.setTime(currentDate);
		String formats[] = {"EEEE, dd-MMM-yy HH:mm:ss z","EEE, dd MMM yyyy HH:mm:ss z","EEE MMM dd HH:mm:ss yyyy"};
		for (String format : formats){
			SimpleDateFormat sdf = new SimpleDateFormat(format);
			try{
				last_modified = reqsdf.parse(reqsdf.format(sdf.parse(otherHeaders.get("If-Modified-Since"))));
				calTimestamp.setTime(last_modified);
				if(calCurr.after(calTimestamp)){
					//send modified
					return true;
				}
				else{
					return false;
				}
			}
			catch(Exception e){
				return true;
			}
		}
		return true;
		
	}	
	
	//Fetch and convert requested resource content into bytes
	public byte[] getResource(File file){
		try{
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			byte [] byteStream = new byte[(int) file.length()];
			bis.read(byteStream, 0, byteStream.length);
			bis.close();
			RESPONSE_PHRASE = "OK";
			RESPONSE_CODE = "200";
			
			logger.info("Buffering data");
			return byteStream;
		}
		catch(FileNotFoundException e){
			logger.error("Resource not found");
			RESPONSE_CODE = "404";
			RESPONSE_PHRASE = "Not Found";
			
			return null;
		}
		catch(IOException e){
			logger.error("Error reading the file");
			RESPONSE_CODE = "500";
			RESPONSE_PHRASE = "Server Error";
			return null;
		}
	}
	
	//Generate response for GET request
	public void generateGETresponse(){
		byte[] initialLine = (VERSION + " " + RESPONSE_CODE + " " + RESPONSE_PHRASE+"\r\n").getBytes();
		byte[] headerLine = ("Date: "+ SERVER_DATE + "\r\nContent-Type: " + CONTENT_TYPE + "\r\nContent-Length: " + CONTENT_LENGTH + "\r\nConnection: Close\r\n\r\n").getBytes();
		response = concatenateBytes(concatenateBytes(initialLine, headerLine), body);
	}
	
	//Generate response for HEAD request
	public void generateHEADresponse(){
		byte[] initialLine = (VERSION + " " + RESPONSE_CODE + " " + RESPONSE_PHRASE+"\r\n").getBytes();
		byte[] headerLine = ("Date: "+ SERVER_DATE + "\r\nContent-Type: " + CONTENT_TYPE + "\r\nContent-Length: " + CONTENT_LENGTH + "\r\nConnection: Close\r\n\r\n").getBytes();
		response = concatenateBytes(initialLine, headerLine);
	}
	
	//Utility function concatenates byte arrays
	public byte[] concatenateBytes(byte[] first, byte[] second){
		byte[] result = new byte[first.length + second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	//Set Headers and body for /control request
	public byte[] buildCONTROLresponse(ThreadPool[] threadPool, String version){
		VERSION = version;
		if(!VERSION.equalsIgnoreCase("http/1.1") && !VERSION.equalsIgnoreCase("http/1.0")){
			logger.error("[ERROR] Invalid HTTP Version");
			isBADRequest();
			return response;
		}
		if(VERSION.equalsIgnoreCase("http/1.1") && !otherHeaders.containsKey("Host")){
			logger.error("[ERROR] Host header missing in HTTP/1.1 request");
			isBADRequest();
			return response;
		}
		ResponseMessages responseMsgs = new ResponseMessages();
		body = responseMsgs.getCONTROLhtml(threadPool);
		CONTENT_TYPE = "text/html; charset=utf-8";
		CONTENT_LENGTH = String.valueOf(body.length);
		RESPONSE_CODE = "200";
		RESPONSE_PHRASE = "OK";
		getServerDate();
		generateGETresponse();
		return response;
	}
	
	//Set Headers and body for /shutdown request
	public byte[] buildSHUTDOWNresponse(ThreadPool[] threadPool, String version){
		VERSION = version;
		if(!VERSION.equalsIgnoreCase("http/1.1") && !VERSION.equalsIgnoreCase("http/1.0")){
			logger.error("[ERROR] Invalid HTTP Version");
			isBADRequest();
			return response;
		}
		if(VERSION.equalsIgnoreCase("http/1.1") && !otherHeaders.containsKey("Host")){
			logger.error("[ERROR] Host header missing in HTTP/1.1 request");
			isBADRequest();
			return response;
		}
		ResponseMessages responseMsgs = new ResponseMessages();
		body = responseMsgs.getSHUTDOWNhtml();
		CONTENT_TYPE = "text/html; charset=utf-8";
		CONTENT_LENGTH = String.valueOf(body.length);
		RESPONSE_CODE = "200";
		RESPONSE_PHRASE = "OK";
		getServerDate();
		generateGETresponse();
		return response;
	}
	
	//Sets Headers and body for 400 Bad Request
	public void isBADRequest(){
			RESPONSE_CODE = "400";
			RESPONSE_PHRASE = "Bad Request";
			CONTENT_TYPE = "text/html; charset=utf-8";
			ResponseMessages responseMsgs = new ResponseMessages();
			body = responseMsgs.getERRORhtml(RESPONSE_CODE, RESPONSE_PHRASE);
			CONTENT_LENGTH = String.valueOf(body.length);
			getServerDate();
			generateGETresponse();
	}
	
	//Sets Headers and body for all normal GET and HEAD requests
	public byte[] buildResponse(String path, String version, String action, String url){
		ACTION = action;
		VERSION = version;
		File file = new File(path);
		ResponseMessages responseMsgs = new ResponseMessages();	
		if(!VERSION.equalsIgnoreCase("http/1.1") && !VERSION.equalsIgnoreCase("http/1.0")){
			logger.error("[ERROR] Invalid HTTP Version");
			isBADRequest();
			return response;
		}
		if(VERSION.equalsIgnoreCase("http/1.1") && !otherHeaders.containsKey("Host")){
			logger.error("[ERROR] Host header missing in HTTP/1.1 request");
			isBADRequest();
			return response;
		}
		if(otherHeaders.containsKey("If-Modified-Since") && !isModified()){
			getServerDate();
			response = (VERSION +" 304 Not Modified\r\n" + SERVER_DATE + "\r\n\r\n").getBytes();
			return response;
		}
		if (otherHeaders.containsKey("If-Unmodified-Since") && isModified()){
			response = (VERSION +" 412 Pre Condition Failed\r\n\r\n").getBytes();
			return response;
		}
		
		if (isValidPath(file)){
			logger.info("Resource path correct");
			//check if resource is directory 
			if(file.isDirectory()){
				logger.info("Resource is directory"); //TO-DO
				body = responseMsgs.getDIRhtml(file, url);
				CONTENT_TYPE = "text/html; charset=utf-8";
				CONTENT_LENGTH = String.valueOf(body.length);
				RESPONSE_CODE = "200";
				RESPONSE_PHRASE = "OK";
				getLastModified(file);
				getServerDate();
				if( ACTION.equalsIgnoreCase("GET")){
					generateGETresponse();
				}
				
				else if( ACTION.equalsIgnoreCase("HEAD")){
					generateHEADresponse();
				}
				return response;
			}
			else{
				CONTENT_TYPE = getMimeType(path)+"; charset=utf-8";
				//if mime is null, then file format not valid
				if(CONTENT_TYPE == null){
					RESPONSE_CODE = "404";
					RESPONSE_PHRASE = "Not Found";
					getServerDate();
					body = responseMsgs.getERRORhtml(RESPONSE_CODE, RESPONSE_PHRASE);
					CONTENT_LENGTH = String.valueOf(body.length);
					if( ACTION.equalsIgnoreCase("GET")){
						generateGETresponse();
					}
					
					else if( ACTION.equalsIgnoreCase("HEAD")){
						generateHEADresponse();
					}
				    logger.error("[ERROR] File format not supported");
				    return response;
				}
				else{
					// a valid file with valid MIME
					logger.info("Mime type: " + CONTENT_TYPE);
					body =  getResource(file);
					CONTENT_LENGTH = String.valueOf(body.length);
					getLastModified(file);
					getServerDate();
					
					if(body != null){
						if( ACTION.equalsIgnoreCase("GET")){
							generateGETresponse();
						}
						
						else if( ACTION.equalsIgnoreCase("HEAD")){
							generateHEADresponse();
						}
						logger.info("Data buffered; writing to socket"); 
						return response;
					}
					else{
						body = responseMsgs.getERRORhtml(RESPONSE_CODE, RESPONSE_PHRASE);
						getServerDate();
						if( ACTION.equalsIgnoreCase("GET")){
							generateGETresponse();
						}
						
						else if( ACTION.equalsIgnoreCase("HEAD")){
							generateHEADresponse();
						}
						logger.error("[ERROR] Problem reading file"); 
						return response;
					}
				}
				
			}
		}
		else{
			getServerDate();
			body = responseMsgs.getERRORhtml(RESPONSE_CODE, RESPONSE_PHRASE);
			CONTENT_LENGTH = String.valueOf(body.length);
			CONTENT_TYPE = "text/html; charset=utf-8";
			if( ACTION.equalsIgnoreCase("GET")){
				generateGETresponse();
			}
			
			else if( ACTION.equalsIgnoreCase("HEAD")){
				generateHEADresponse();
			}
			return response; 
			}
	}
}