package edu.upenn.cis.cis455.webserver;

import edu.upenn.cis.cis455.servlet.Handler;
import edu.upenn.cis.cis455.servlet.Config;
import edu.upenn.cis.cis455.servlet.Context;
import edu.upenn.cis.cis455.servlet.Request;
import edu.upenn.cis.cis455.servlet.Response;
import edu.upenn.cis.cis455.servlet.Session;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;

public class HttpServer {
	static final Logger logger = Logger.getLogger(HttpServer.class);
	private static int MAX_POOL_SIZE = 5;
	private static int portNumber;
	private static ThreadPool[] threadPool = new ThreadPool[MAX_POOL_SIZE];
	private static volatile boolean STOP = false;
	private static ServerSocket serverSocket;
	private static HashMap<String,HttpServlet> servlets;
	static HashMap<String,String> servletMapping;
	private static HashMap<String, Session> sessionMap = new HashMap<>();
	
	public static HashMap<String, Session> getSessionMap(){
		return sessionMap;
	}
	public static int getPortNumber() {
		return portNumber;
	}
    public static void generateThreadPool(LinkedList<Socket> sharedQueue, String home){
    	logger.info("[Output from log4j] Creating thread pool");
    	for(int i = 0; i < MAX_POOL_SIZE; i++){
    		threadPool[i] = new ThreadPool(sharedQueue, home, threadPool);
    		threadPool[i].setName("Thread"+i);
    		
    		threadPool[i].start();
    	}
    	logger.info("[Output from log4j] Thread pool ready");
    }
 
    public static ServerSocket getServerSocket() {
		return serverSocket;
	}
    
    public static HashMap<String, HttpServlet> getServlets(){
    	return servlets;
    }
    
    public static HashMap<String, String> getServletMapping(){
    	return servletMapping;
    }
     
    
    private static Handler parseWebdotxml(String webdotxml) throws Exception {
		Handler h = new Handler();
		File file = new File(webdotxml);
		if (file.exists() == false) {
			System.err.println("error: cannot find " + file.getPath());
			System.exit(-1);
		}
		SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		parser.parse(file, h);
		
		return h;
	}
	
	private static Context createContext(Handler h) {
		Context fc = new Context();
		for (String param : h.m_contextParams.keySet()) {
			fc.setInitParam(param, h.m_contextParams.get(param));
		}
		return fc;
	}
	
	private static HashMap<String,HttpServlet> createServlets(Handler h, Context fc) throws Exception {
		HashMap<String,HttpServlet> servlets = new HashMap<String,HttpServlet>();
		for (String servletName : h.m_servlets.keySet()) {
			Config config = new Config(servletName, fc);
			String className = h.m_servlets.get(servletName);
			Class servletClass = Class.forName(className);
			HttpServlet servlet = (HttpServlet) servletClass.newInstance();
			HashMap<String,String> servletParams = h.m_servletParams.get(servletName);
			if (servletParams != null) {
				for (String param : servletParams.keySet()) {
					config.setInitParam(param, servletParams.get(param));
				}
			}
			servlet.init(config);
			servlets.put(servletName, servlet);
		}
		return servlets;
	}

	

	public static void main(String args[]){
    	
    	LinkedList<Socket> sharedQueue = new LinkedList<>();
    	
    	if (args.length < 3) {
    		// Output full name and pennkey 
    		System.out.println("Check number of arguments\nAayushi Dwivedi\naayushi");
			System.exit(-1);
		}
    	else {
    		try{
    		portNumber = Integer.parseInt(args[0]);
    		}
    		catch(Exception e){
    			logger.error("Invalid port number", e);
    		}
    		File file = new File(args[1]);
    		if(file.isDirectory()){
    			generateThreadPool(sharedQueue, args[1]);
    			HeartBeatThread heartBeat = new HeartBeatThread();
    			heartBeat.setName("Heart Beat");
    			heartBeat.start();
    		}
    		else{
    			System.out.println("Not a directory;Exiting.");
    			System.exit(-1);
    		}
    	}
     
      	
      	try{
      		Handler h = parseWebdotxml(args[2]);
    		Context context = createContext(h);
    		servlets = createServlets(h, context);
    		servletMapping = h.m_servletMappings;
    	
      		serverSocket = new ServerSocket(portNumber);
      		/* keep listening while STOP ==false; STOP is a static volatile 
      		 * of class ThreadPool; STOP is set to true when /shutdown request arrives
      		 */
      		while(!ThreadPool.getSTOP()){
			  
      			Socket clientSocket = serverSocket.accept();
		  
      			//Add socket to queue
      			synchronized(sharedQueue){
      				sharedQueue.add(clientSocket);
      				logger.info("Client socket added to sharedQueue");
      				sharedQueue.notify();
      			} 
      		}
      		System.out.println("Main thread exiting");
		  
	  }
	  catch(Exception e){
		/**
		 * Interrupted when socket closed on /shutdown request by a ThreadPool thread
		 * main() waits for RUNNABLE threads (not interrupted during /shutdown) to join
		 * before exiting 
		 */
		 
		if(ThreadPool.getSTOP()){
			for( Thread th : threadPool){
				try {
					th.join();
				} catch (InterruptedException e1) {
					logger.error("[ERROR] Unable to join thread" + th.getName());
				}
			}
		}
		else{
			logger.error("[ERROR] Interrupt Exception in daemon thread\n",e);
			
		}
		
		System.exit(-1);
	  }
      
	  
  }

	

	
  
}
