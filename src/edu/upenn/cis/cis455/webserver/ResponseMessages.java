package edu.upenn.cis.cis455.webserver;

import java.io.File;

public class ResponseMessages {
	public byte[] getERRORhtml(String code, String phrase){
		byte[] body = ("\r\n<html><body><h1>"+ code + " "+ phrase + "</h1></body></html>").getBytes();
		return body;
	}
	
	public byte[] getDIRhtml(File dir){
		File[] files = dir.listFiles();
		byte[] body;
		String content = "\r\n<html>\n<body>";
		for(File file : files){
			String fname = file.getName();
			System.out.println(fname);
			content = content + fname+"</br>";
						
		}
		body = (content + "\n</body>\n</html>").getBytes();
		System.out.println(body);
		return body;
		
	}

	
	

	
}
