package edu.upenn.cis.cis455.webserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

public class RequestHandler{
	//private final Map<String,String> requestHeader;
	static final Logger logger = Logger.getLogger(RequestHandler.class);

	private byte[] body;
	private byte[] response;
	private String RESPONSE_CODE;
	private String RESPONSE_PHRASE;
	private String CONTENT_TYPE;
	private String CONTENT_LENGTH;
	private String SERVER_DATE;
	private String LAST_MODIFIED;
	private String ACTION;
	private String PATH;
	private String VERSION;
	private Map<String, String> otherHeaders = new HashMap<>();
	
	public RequestHandler(Map<String,String> otherHeaders){
		this.otherHeaders = otherHeaders;
	}
	
	
	public boolean isValidPath(File file){
		
		try{
			if(file.exists()){
				if (file.canRead()){
					return true;
					}
				else {
					logger.error("Forbidden Access; Requested resource cannot be accessed");  
					RESPONSE_CODE = "403";
					RESPONSE_PHRASE = "Forbidden";
					return false;
				}
			}
			else {
				logger.error("File not found; Requested resource does not exist;");
				RESPONSE_CODE = "404";
				RESPONSE_PHRASE = "Not Found";
				return false;
			}
				}
		catch(SecurityException e){
			logger.error("Forbidden Access; Requested resource cannot be accessed");  
			RESPONSE_CODE = "403";
			RESPONSE_PHRASE = "Forbidden";
			return false;
		}
	
	}

	
	
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
	
	public void getServerDate(){
		
		SimpleDateFormat sdf =  new SimpleDateFormat("D, dd MMM yyyy HH:mm:ss z");
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		Date currDate = new Date();
		SERVER_DATE =  sdf.format(currDate);
	}
	
	public void getLastModified(File file){
	
		SimpleDateFormat sdf =  new SimpleDateFormat("D, dd MMM yyyy HH:mm:ss z");
		TimeZone oldZone = sdf.getTimeZone();
		sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
		LAST_MODIFIED =  sdf.format(file.lastModified());
	}
	
	
	
	public byte[] getResource(File file){
		try{
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			byte [] byteStream = new byte[(int) file.length()];
			bis.read(byteStream, 0, byteStream.length);
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
	
	public void generateGETresponse(){
		byte[] initialLine = (VERSION + " " + RESPONSE_CODE + " " + RESPONSE_PHRASE+"\r\n").getBytes();
		byte[] headerLine = ("Date: "+ SERVER_DATE + "\r\nContent-Type: " + CONTENT_TYPE + "\r\nContent-Length: " + CONTENT_LENGTH + "\r\nConnection: Close\r\n").getBytes();
		response = concatenateBytes(concatenateBytes(initialLine, headerLine), body);
	}
	
	public void generateHEADresponse(){
		byte[] initialLine = (VERSION + " " + RESPONSE_CODE + " " + RESPONSE_PHRASE+"\r\n").getBytes();
		byte[] headerLine = ("Date: "+ SERVER_DATE + "\r\nContent-Type: " + CONTENT_TYPE + "\r\nContent-Length: " + CONTENT_LENGTH + "\r\nConnection: Close\r\n").getBytes();
		response = concatenateBytes(initialLine, headerLine);
	}
	
	public byte[] concatenateBytes(byte[] first, byte[] second){
		byte[] result = new byte[first.length + second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	public byte[] buildCONTROLresponse(ThreadPool[] threadPool, String version){
		VERSION = version;
		if((response = isBADRequest()) != null){
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
	
	public byte[] buildSHUTDOWNresponse(ThreadPool[] threadPool, String version){
		VERSION = version;
		if((response = isBADRequest()) != null){
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
	
	public byte[] isBADRequest(){
		if(VERSION.equalsIgnoreCase("http/1.1") && !otherHeaders.containsKey("Host")){
			RESPONSE_CODE = "400";
			RESPONSE_PHRASE = "Bad Request";
			CONTENT_TYPE = "text/html; charset=utf-8";
			ResponseMessages responseMsgs = new ResponseMessages();
			body = responseMsgs.getERRORhtml(RESPONSE_CODE, RESPONSE_PHRASE);
			CONTENT_LENGTH = String.valueOf(body.length);
			getServerDate();
			generateGETresponse();
			return response;
		}
		return null;
	}
	public byte[] buildResponse(String path, String version, String action, String url){
		ACTION = action;
		VERSION = version;
		if((response = isBADRequest()) != null){
			return response;
		}
		File file = new File(path);
		ResponseMessages responseMsgs = new ResponseMessages();
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
				//if mime is null, then file format not vlid
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
				    logger.error("File format not supported");
				    return response;
				}
				else{
					logger.info("Mime type: " + CONTENT_TYPE);
					body =  concatenateBytes("\r\n".getBytes(),getResource(file));
					getLastModified(file);
					getServerDate();
					CONTENT_LENGTH = String.valueOf(body.length);
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
						logger.info("Writing ERROR msg to socket"); 
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