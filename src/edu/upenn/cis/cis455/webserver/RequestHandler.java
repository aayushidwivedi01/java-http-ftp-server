package edu.upenn.cis.cis455.webserver;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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
	private String ACTION;
	private String PATH;
	private String VERSION;
	
	
	
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
	
	
	public boolean isValidPath(File file){
		
		try{
			if(file.exists()){
				return true;
			}
			else {
				return false;
			}
				}
		catch(SecurityException se){
			System.out.println("Forbidden Access");  // Is this a 404?
			return false;
		}
	
	}
	
	public byte[] getResource(File file){
		try{
			FileInputStream fis = new FileInputStream(file);
			BufferedInputStream bis = new BufferedInputStream(fis);
			byte [] byteStream = new byte[(int) file.length()];

			logger.info("Buffering data");
			return byteStream;
		}
		catch(FileNotFoundException e){
			logger.error("Resource not found");
			return null;
		}
	}
	
	public void generateGETresponse(){
		byte[] initialLine = (VERSION + " " + RESPONSE_CODE + " " + RESPONSE_PHRASE+"\r\n").getBytes();
		byte[] headerLine = ("Content-Type: " + CONTENT_TYPE + "\r\nContent-Length: " + CONTENT_LENGTH + "\r\n").getBytes();
		response = concatenateBytes(concatenateBytes(initialLine, headerLine), body);
	}
	
	public byte[] concatenateBytes(byte[] first, byte[] second){
		byte[] result = new byte[first.length + second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	public byte[] buildResponse(String path, String version){
		VERSION = version;
		File file = new File(path);
		if (isValidPath(file)){
			logger.info("Resource path correct");
			//check if resource is directory 
			if(file.isDirectory()){
				logger.info("Resource is directory"); //TO-DO
				return null;
			}
			else{
				CONTENT_TYPE = getMimeType(path);
				//if mime is null, then file format not vlid
				if(CONTENT_TYPE == null){
					RESPONSE_CODE = "404";
					RESPONSE_PHRASE = "Not Found";
				    logger.error("File format not supported");
				    return null;
				}
				else{
					logger.info("Mime type" + CONTENT_TYPE);
					body =  getResource(file);
					CONTENT_LENGTH = String.valueOf(body.length);
					generateGETresponse();
					logger.info("Data buffered; writing to socket"); 
					return response;
					
					
				}
				
			}
		}
		else{
			logger.error("Resource does not exist");
			RESPONSE_CODE = "404";
			RESPONSE_PHRASE = "Not Found";
			return null; //TO-DO. Not correct
		}
	}
}