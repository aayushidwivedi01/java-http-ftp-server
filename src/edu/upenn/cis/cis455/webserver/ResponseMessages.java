package edu.upenn.cis.cis455.webserver;

import java.io.File;

public class ResponseMessages {
	public byte[] getERRORhtml(String code, String phrase){
		byte[] body = ("\r\n<html><body><h1>"+ code + " "+ phrase + "</h1></body></html>").getBytes();
		return body;
	}
	
	public byte[] getDIRhtml(File dir, String URL){
		File[] files = dir.listFiles();
		byte[] body;
		String content = "\r\n<html>\n<body>";
		for (File file : files){
			String fname = file.getName();
			content = content + "<a href=\""+ URL + "/" + fname + "\">" + fname + "</a></br>";
		}
		body = (content + "</body></html>").getBytes();
		
				
		return body;
	}
	
	public byte[] getCONTROLhtml(ThreadPool[] threadPool){
		byte[] body;
		String thState;
		String content = "\r\n<html>\n<body>";
		for (ThreadPool thName : threadPool){
			content = content + thName.getName() ;
			thState = String.valueOf(thName.getState());
			if(thState.equals("RUNNABLE")){
				content = content + " " + thState +  " " +  thName.getURL() + "</br>";
			}
			else{
				content = content + " " + thState + "</br>";
			}
		}
		body = (content+ "</body></html>").getBytes();
		return body;
	}
	
	

	
}
