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
	private FileInputStream fis;
	private BufferedInputStream bis;
	private File file;
	private String VERSION;
	private String ACTION;
	private String PATH;
	private String RESPONSE_CODE;
	private String RESPONSE_PHRASE;
	private String CONTENT_TYPE;
	private String CONTENT_LENGTH;
	private ThreadPool[] threadPool;
	private String URL;
	private int PORT_NO;
	
	
	public ThreadPool(LinkedList<Socket>queue, String home, ThreadPool[] threadPool, int portNo ){
		
		this.queue = queue;
		this.threadPool = threadPool;
		HOME = home;
		PORT_NO = portNo;
		URL = "http://localhost:" + String.valueOf(PORT_NO) + "URLNotFormedYet";
		
	}
	
public String getURL() {
		return URL;
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

	  
	
	private  void parseRequestHeaders(String request){
		 logger.info("[Output from log4j] Parsing request..");
		  
		 String[] splitRequest = request.split(" ");
		 
		 ACTION = splitRequest[0];
		 PATH = splitRequest[1];
		 VERSION =  splitRequest[2];
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
						logger.error("Interrupted thread while waiting on sharedQueue", e);
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
								  System.out.println(request);
								  //System.out.println(requestContent);
								  parseRequestHeaders(requestContent.remove(0));  //TO-DO: add all headers in map
								  break;
							  }
							  System.out.println(request);
						  	  requestContent.add(request);
							  //System.out.println(request);					  
						  	}
						System.out.println("Handling request");
	
						//Step 1: check if path is valid
						switch(ACTION){
							case "GET":
										RequestHandler requestHandler = new RequestHandler();
										if(PATH.equals("/control")){
											URL = "http://localhost:" + String.valueOf(PORT_NO) +PATH;
											logger.info("Building CONTROL response");
											response = requestHandler.buildCONTROLresponse(threadPool, VERSION);
											out.write(response);
											logger.info("Done");
											out.flush();
											out.close();
										}
										else{
											URL = "http://localhost:" + PORT_NO +PATH;
											String resourcePath = HOME + PATH;
											System.out.println(resourcePath);
											logger.info("Building response");
											response = requestHandler.buildResponse(resourcePath, VERSION, ACTION, URL);
											out.write(response);
											logger.info("Done");
											out.flush();
											out.close();
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
						logger.error("Interrupted thread while processing request", e);
					}
				}
			}
		}
	}
}