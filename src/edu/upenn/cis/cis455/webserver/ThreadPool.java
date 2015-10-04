package edu.upenn.cis.cis455.webserver;
/**
 * Parses incoming request;
 * Differentiates between request type
 * Responds to client's request
 * @param queue containing client sockets
 * @param home directory of server
 * @param threadPool object reference
 * @param port number the server is listening at
 */
import edu.upenn.cis.cis455.servlet.Request;
import edu.upenn.cis.cis455.servlet.Response;
import edu.upenn.cis.cis455.servlet.Context;
import edu.upenn.cis.cis455.servlet.Session;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;


public class ThreadPool extends Thread{
	static final Logger logger = Logger.getLogger(ThreadPool.class); 
	private LinkedList<Socket>queue;
	private final String HOME;
	private Map<String, String> otherHeaders = new HashMap<>();
	private HashMap<String, String> httpMainHeaders = new HashMap<>();
	private HashMap<String, ArrayList<String>> httpOtherHeaders = new HashMap<>();
	private StringBuilder requestBody = new StringBuilder();
	private HashMap<String,String> servletMapping = new HashMap<>();
	private HashMap<String,HttpServlet> servlets = new HashMap<>();
	private String matchedUrlPattern = null;
	HttpRequest httpRequest;
	private String VERSION;
	private String ACTION;
	private String PATH;
	private ThreadPool[] threadPool;
	private String URL;
	private static int PORT_NO;
	private static volatile boolean STOP = false;
	private static Socket clientSocket;
	
	
	public static int getPORT_NO() {
		return PORT_NO;
	}

	
	public ThreadPool(LinkedList<Socket>queue, String home, ThreadPool[] threadPool){
		this.queue = queue;
		this.threadPool = threadPool;
		HOME = home;
		PORT_NO = HttpServer.getPortNumber();
		URL = "http://localhost:" + String.valueOf(PORT_NO) + "URLNotFormedYet";
		
		this.servletMapping = HttpServer.servletMapping;
	}
	
	public String getURL() {
		return URL;
	}
	public static boolean getSTOP(){	
		return STOP;
	}

	public void setServlets(){
		servlets = HttpServer.getServlets();
	}
	
	public static Socket getClientSocket(){
		return clientSocket;
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

	  
	
	private  void setMainHeaders(HashMap<String, String> mainRequest){
		 logger.info("[Output from log4j] Parsing request..");
		 
		 ACTION = mainRequest.get("action");
		 PATH = mainRequest.get("path");
		 VERSION =  mainRequest.get("version");	 
		 
	 }	
	
	private String getServletMatch(HashMap<String, String> servletMapping){
		String longestMatch = "";
		boolean flag = false;
		for(String urlPattern : servletMapping.keySet()){
			matchedUrlPattern = urlPattern;
			if (urlPattern.contains("*")){
				urlPattern = urlPattern.split("/\\*", 2)[0];
			}
			if (PATH.startsWith(urlPattern) && longestMatch.length() <= urlPattern.length()){
				if ( PATH.length() > urlPattern.length() ){
					if (PATH.charAt(urlPattern.length()) == '/'){
					flag = true;
					longestMatch = urlPattern;
					}
				}
				else {

					flag = true;
					longestMatch = urlPattern;
				}
			}
		}
		
		if (flag)
			return longestMatch;
		else 
			return null;
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
							
							logger.error("[ERROR] Interrupted" + Thread.currentThread().getName() + "while waiting on sharedQueue", e);
						}
						
					}
				}
				else{
					try{
						logger.info("[Output from log4j] Processing a request");
						String requestMsg;
						
						ArrayList<String>requestContent = new ArrayList();
						clientSocket = queue.removeFirst();
					   	
						BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
												
						while((requestMsg = in.readLine()) != null){
							  if(requestMsg.length() == 0){
								  String mainRequest = requestContent.remove(0);
								  httpRequest = new HttpRequest(mainRequest, PORT_NO, requestContent);
								  httpRequest.parseRequestHeaders();
								  httpRequest.parseOtherHeaders();
								  httpMainHeaders = httpRequest.mainRequestHeaders;
								  httpOtherHeaders = httpRequest.otherHeaders;
								  setMainHeaders(httpMainHeaders); 								  
								  break;
							  }
							  System.out.println(requestMsg);
						  	  requestContent.add(requestMsg);
							 				  
						  	}
						if (ACTION.equalsIgnoreCase("POST") ){
							if (httpOtherHeaders.containsKey("Content-Length") && httpOtherHeaders.containsKey("Content-Type")){
								char[] cbuf = new char[Integer.valueOf(httpOtherHeaders.get("Content-Length").get(0))];
								logger.info("Request has a body");
								
								//get content length
								int len = Integer.valueOf(httpOtherHeaders.get("Content-Length").get(0));		
								//read all the characters in the body
								in.read(cbuf, 0, len);
								
								//convert the body into string
								for(char c : cbuf){
									requestBody.append(c);
								}
								
								if (httpOtherHeaders.get("Content-Type").get(0).equalsIgnoreCase("application/x-www-form-urlencoded")){
									httpRequest.parseBody(requestBody.toString());
								}
								
							}							
						}
						
						else {
							if (PATH.contains("?")){
								httpRequest.parseBody(PATH.split("\\?",2)[1]);
							}
						}
						
						logger.info("Handling request");
						servletMapping = HttpServer.getServletMapping();
						servlets = HttpServer.getServlets();
						String servletPath = getServletMatch(servletMapping);

						
						if (servletPath != null){
							Session session = null;
							Request request = new Request(session, httpRequest, servletPath);
							Response response = new Response();
							response.setVersion(VERSION);
							
							if ( servletMapping.containsKey(matchedUrlPattern)){
								String servletName = servletMapping.get(matchedUrlPattern);
								if( servlets.containsKey(servletName)){
									logger.info("Found servlet match:"+ servletName);
									HttpServlet servlet = servlets.get(servletName);
									servlet.service(request, response);
									

								}
							}	
							
							continue;
						}
						
						OutputStream out = clientSocket.getOutputStream();
					
						RequestHandler requestHandler = new RequestHandler(httpOtherHeaders);
						byte[] response;
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
						logger.error("[ERROR] Interrupted thread while processing request", e);
						}
					}
				}
			}
		}
		
		
		
	}

	
}