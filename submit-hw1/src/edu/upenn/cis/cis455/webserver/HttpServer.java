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
	private static int MAX_POOL_SIZE = 5;
	private static int portNumber;
	private static ThreadPool[] threadPool = new ThreadPool[MAX_POOL_SIZE];
	private static volatile boolean STOP = false;
	private static ServerSocket serverSocket;

    public static void generateThreadPool(LinkedList<Socket> sharedQueue, String home){
    	logger.info("[Output from log4j] Creating thread pool");
    	for(int i = 0; i < MAX_POOL_SIZE; i++){
    		threadPool[i] = new ThreadPool(sharedQueue, home, threadPool, portNumber);
    		threadPool[i].setName("Thread"+i);
    		
    		threadPool[i].start();
    	}
    	logger.info("[Output from log4j] Thread pool ready");
    }
 
    public static ServerSocket getServerSocket() {
		return serverSocket;
	}

	public static void main(String args[]){
    	
    	LinkedList<Socket> sharedQueue = new LinkedList<>();
    	if (args.length <= 1){
    		// Output full name and pennkey 
    		System.out.println("Check number of arguments\nAayushi Dwivedi\naayushi");
    		System.exit(-1);
    	}
    	else if (args.length == 2){
    		try{
    		portNumber = Integer.parseInt(args[0]);
    		}
    		catch(Exception e){
    			logger.error("Invalid port number", e);
    		}
    		File file = new File(args[1]);
    		if(file.isDirectory()){
    			generateThreadPool(sharedQueue, args[1]);
    		}
    		else{
    			System.out.println("Not a directory;Exiting.");
    			System.exit(-1);
    		}
    	}
     
      	
      	try{
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
