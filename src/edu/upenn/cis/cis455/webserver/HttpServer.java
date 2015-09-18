package edu.upenn.cis.cis455.webserver;

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

import org.apache.log4j.Logger;

class HttpServer {
	static final Logger logger = Logger.getLogger(HttpServer.class);
	private static int MAX_POOL_SIZE = 100;
	private static ThreadPool[] threadPool = new ThreadPool[MAX_POOL_SIZE];

    public static void generateThreadPool(LinkedList<Socket> sharedQueue, String home){
    	logger.info("[Output from log4j] Creating thread pool");
    	for(int i = 0; i < MAX_POOL_SIZE; i++){
    		threadPool[i] = new ThreadPool(sharedQueue, home);
    		threadPool[i].start();
    	}
    	logger.info("[Output from log4j] Thread pool ready");
    }
 
    public static void main(String args[]){
    	
    	
    	
    	LinkedList<Socket> sharedQueue = new LinkedList<>();
    	if (args.length == 1){
    		//TO-DO: Output full name and pennkey
    		generateThreadPool(sharedQueue, null);
    	}
    	else if (args.length == 2){
    		File file = new File(args[1]);
    		if(file.isDirectory()){
    			generateThreadPool(sharedQueue, args[1]);
    		}
    	}
     
      	int portNumber = Integer.parseInt(args[0]);
      	try{
      		ServerSocket serverSocket = new ServerSocket(portNumber);
      		//keep listening
      		while(true){
			  
      			Socket clientSocket = serverSocket.accept();
		  
      			//Add socket to queue
      			synchronized(sharedQueue){
      				sharedQueue.add(clientSocket);
      				logger.info("Client socket added to sharedQueue");
      				sharedQueue.notify();
      			} 
      		}
		  
	  }
	  catch(Exception e){
		logger.error("Interrupt Exception in daemon thread.");
		System.exit(-1);
	  }
	  
  }
  
}
