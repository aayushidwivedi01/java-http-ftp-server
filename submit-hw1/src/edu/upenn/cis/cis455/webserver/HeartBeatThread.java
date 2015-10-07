package edu.upenn.cis.cis455.webserver;
/**
* Thread to remove sessionId from a map that store all valid sessions
* Stop when interrupted by the main thread;
*
*/

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.servlet.Session;

public class HeartBeatThread extends Thread{
	static final Logger logger = Logger.getLogger(HttpServer.class);
	private int maxInterval;
	private long lastAccessedTime;
	private HashMap<String, Session> sessionMap;
	
	//update sessionMap when the thread wakes up
	public void updateSessionMap(){
		this.sessionMap = HttpServer.getSessionMap();		
	}
	
	
	public void updateMaxInterval(Session session){
		maxInterval = session.getMaxInactiveInterval()  * 1000;
	}
	
	public void updateLastAcessedTime(Session session){
		lastAccessedTime = session.getLastAccessedTime();
	}
	
	//Sleeps for 10 secs and then checks for expired or invalid cookies
	public void run(){
		try {
			while(!interrupted()){
				Thread.sleep(10000);
				updateSessionMap();
				synchronized(sessionMap){
					
					if (sessionMap.isEmpty()){
						continue;
					}
					Session session = null;
					for (String sessionId : sessionMap.keySet()){
						session = sessionMap.get(sessionId);
						updateMaxInterval(session);
						if (maxInterval == -1){
							logger.info("[HEART BEAT]Max interval set to -1 for this session");
							continue;
						}
						
						updateLastAcessedTime(session);
						long currentTime = new Date().getTime();
						if ((currentTime - lastAccessedTime) > maxInterval){
							logger.info("[HEART BEAT]Session expired: Invalidating");
							session.invalidate();
							sessionMap.remove(sessionId);
						}
					}
				}
				
			}
		} catch (InterruptedException e) {
			logger.info("[INFO] Exiting heart beat thread");
		}
				
		
	}

}
