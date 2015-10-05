package edu.upenn.cis.cis455.webserver;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import edu.upenn.cis.cis455.servlet.Session;

public class HeartBeatThread extends Thread{
	static final Logger logger = Logger.getLogger(HttpServer.class);
	private int maxInterval;
	private long lastAccessedTime;
	private HashMap<String, Session> sessionMap;
	
	public void updateSessionMap(){
		this.sessionMap = HttpServer.getSessionMap();		
	}
	public void updateMaxInterval(Session session){
		maxInterval = session.getMaxInactiveInterval()  * 1000;
	}
	
	public void updateLastAcessedTime(Session session){
		lastAccessedTime = session.getLastAccessedTime();
	}
	
	public void run(){
		try {
			while(!interrupted()){
				Thread.sleep(5000);
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
			e.printStackTrace();
		}
				
		
	}

}
