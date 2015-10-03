package edu.upenn.cis.cis455.webserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.log4j.Logger;


public class ThreadPool extends Thread{
	static final Logger logger = Logger.getLogger(ThreadPool.class); 
	private LinkedList<Socket>queue;
	private final String HOME;
	private Map<String, String> otherHeaders = new HashMap<>();
	private String VERSION;
	private String ACTION;
	private String PATH;
	private ThreadPool[] threadPool;
	private String URL;
	private static int PORT_NO;
	private static volatile boolean STOP = false;
	
	
	public static int getPORT_NO() {
		return PORT_NO;
	}

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
public static boolean getSTOP(){	
		return STOP;
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

	  
	
	private  void parseRequestHeaders(String mainRequest, ArrayList<String> otherRequests){
		 logger.info("[Output from log4j] Parsing request..");
		  
		 String[] splitRequest = mainRequest.split(" ");
		 
		 ACTION = splitRequest[0];
		 PATH = splitRequest[1];
		 VERSION =  splitRequest[2];
		 
		 
		 for( String req : otherRequests){
			 String[] pair = req.split(":", 2);
			 
			 otherHeaders.put(pair[0], pair[1]);
		 }
		 System.out.println(otherHeaders);
		 
		 
		 
	 }	
	

	
	
	public void run()
	{
		while(!STOP){
			synchronized(queue){
				if(queue.isEmpty()){
					try{
						logger.info("[Output from log4j] sharedQueue empty; No new requests");
						queue.wait();
					}
					catch(InterruptedException e){
						if(STOP){
							logger.info("Exiting " + Thread.currentThread().getName());
							break;
						}
						else{
							
							logger.error("Interrupted" + Thread.currentThread().getName() + "while waiting on sharedQueue", e);
						}
						
					}
				}
				else{
					try{
						logger.info("[Output from log4j] Processing a request");
						String request;
						byte[] response;
						ArrayList<String>requestContent = new ArrayList();
						
						Socket clientSocket = queue.removeFirst();
					   	OutputStream out = clientSocket.getOutputStream();
						BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
												
						while((request = in.readLine()) != null){
							  if(request.length() == 0){
								  String mainRequest = requestContent.remove(0);
								  parseRequestHeaders(mainRequest, requestContent);
								  break;
							  }
							  System.out.println(request);
						  	  requestContent.add(request);
							 				  
						  	}
						System.out.println("Handling request");
						
						RequestHandler requestHandler = new RequestHandler(otherHeaders);
						
						switch(ACTION){
							case "GET":
								if(PATH.equalsIgnoreCase("/control")){
									if (otherHeaders.containsKey("Expect") && VERSION.equalsIgnoreCase("http/1.1")){
										out.write("HTTP/1.1 100 Continue \r\n".getBytes());
									}
										
									URL = "http://localhost:" + String.valueOf(PORT_NO) +PATH;
									logger.info("Building CONTROL response");
									response = requestHandler.buildCONTROLresponse(threadPool, VERSION);
									out.write(response);
									logger.info("Done");
									out.flush();
									out.close();
								}
								else if (PATH.equalsIgnoreCase("/shutdown")){
									
									logger.info("Preparing to shutdown the server");
									response = requestHandler.buildSHUTDOWNresponse(threadPool, VERSION);
									out.write(response);
									out.flush();
									out.close();
									STOP = true;
									logger.info("Shutdown initiated by " + Thread.currentThread().getName());
									for (Thread th : threadPool){
										String state = String.valueOf(th.getState());
										if(!state.equalsIgnoreCase("RUNNABLE")){
											th.interrupt();
										}
								
									}
									HttpServer.getServerSocket().close();
									break;
								}
								else{
									if (otherHeaders.containsKey("Expect") && VERSION.equalsIgnoreCase("http/1.1")){
										out.write("HTTP/1.1 100 Continue \r\n".getBytes());
									}
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
								if (otherHeaders.containsKey("Expect") && VERSION.equalsIgnoreCase("http/1.1")){
									out.write("HTTP/1.1 100 Continue \r\n".getBytes());
								}
								URL = "http://localhost:" + PORT_NO +PATH;
								String resourcePath = HOME + PATH;
								System.out.println(resourcePath);
								logger.info("Building response");
								response = requestHandler.buildResponse(resourcePath, VERSION, ACTION, URL);
								out.write(response);
								logger.info("Done");
								out.flush();
								out.close();
								break;
							case "POST":
								System.out.println("Milestone 2");
								break;
						}
							
								
					}
					
					catch(Exception e){
						if (STOP){
							logger.info("Exiting "+ Thread.currentThread().getName());
						}
						else{
						logger.error("Interrupted thread while processing request", e);
						}
					}
				}
			}
		}
		
		
		
	}

	
}