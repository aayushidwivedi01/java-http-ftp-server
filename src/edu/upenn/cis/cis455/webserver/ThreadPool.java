package edu.upenn.cis.cis455.webserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;


public class ThreadPool extends Thread{
	static final Logger logger = Logger.getLogger(ThreadPool.class); 
	private LinkedList<Socket>queue;
	private final String HOME;
	private Map<String, String> requestBody;
	private  Map<String, String> requestHeaders;
	private FileInputStream fis;
	private BufferedInputStream bis;
	private File file;
	private String VERSION;
	private String RESPONSE_CODE;
	private String RESPONSE_PHRASE;
	private String CONTENT_TYPE;
	private String CONTENT_LENGTH;
	
	
	
	
	public ThreadPool(LinkedList<Socket>queue, String home){
		this.queue = queue;
		HOME = home;
	}
	
/**
 	[GET /hello HTTP/1.1, 
	Host: localhost:8080, 
	User-Agent: Mozilla/5.0 (X11; Ubuntu; Linux i686; rv:28.0) Gecko/20100101 Firefox/28.0, 
	Accept: ,
	Accept-Language: en-US,en;q=0.5, 
	Accept-Encoding: gzip, deflate, 
	Connection: keep-alive]
**/

	  
	//TO-D: Modify the function to iterate through entire request payload
	private  void parseRequestHeaders(String request){
		 logger.info("[Output from log4j] Parsing request..");
		  
		 String[] splitRequest = request.split(" ");
		 requestHeaders = new HashMap<String,String>();
		 requestHeaders.put("action", splitRequest[0]);
		 requestHeaders.put("path", splitRequest[1]);
		 requestHeaders.put("version", splitRequest[2]);
		 
		 	 
		 System.out.println(request);
		 System.out.println(requestHeaders);
	 }	
	
	private void parseRequestBody(ArrayList<String> request){
		logger.info("[Output from log4j] Parsing request body");
		for(String req : request){
			String[] pair = req.split(":", 2);
			requestBody.put(pair[0], pair[1]);
		}
	}
	public void run()
	{
		while(true){
			synchronized(queue){
				if(queue.isEmpty()){
					try{
						logger.info("[Output from log4j] sharedQueue empty; No new requests");
						queue.wait();
					}
					catch(InterruptedException e){
						logger.error("Interrupted thread while waiting on sharedQueue");
						RESPONSE_CODE = "404";
						RESPONSE_PHRASE = "Not Found";
						
					}
				}
				else{
					try{
						logger.info("[Output from log4j] Processing a request");
						String request;
						byte[] response;
						ArrayList<String>requestContent = new ArrayList();
						
						Socket clientSocket = queue.removeFirst();
					    //PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
						OutputStream out = clientSocket.getOutputStream();
						BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
												
						while((request = in.readLine()) != null){
							  if(request.length() == 0){
								  System.out.println(requestContent);
								  parseRequestHeaders(requestContent.remove(0));  //TO-DO: add all headers in map
								  break;
							  }
						  	  requestContent.add(request);
							  //System.out.println(request);					  
						  	}
						System.out.println("Handling request");
						
						RequestHandler requestHandler = new RequestHandler();
						
						logger.info("Created request handler");
						//Step 1: check if path is valid
						switch(requestHeaders.get("action")){
							case "GET":
									if(HOME != null){ //args[1] present
										String resourcePath = HOME + requestHeaders.get("path");
										System.out.println(resourcePath);
										logger.info("Building response");
										response = requestHandler.buildResponse(resourcePath, VERSION);
										out.write(response);
										logger.info("Done");
										out.flush();
										
									}
									break;
							case "HEAD":
									System.out.println("TO DO");
									break;
							case "POST":
								System.out.println("Milestone 2");
								break;
						}
							
								
					}
					catch(Exception e){
						logger.error("Interrupted thread while processing request");
					}
				}
			}
		}
	}
}