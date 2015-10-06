package edu.upenn.cis.cis455.testServlet;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

public class TestServlet extends HttpServlet {

	

	
	  public void doGet(HttpServletRequest request, HttpServletResponse response) 
	       throws java.io.IOException
	  {
//	    response.setContentType("text/html");
//	    PrintWriter out = response.getWriter();
//	    out.println("<html><head><title>Test</title></head><body>");
//	    out.println("RequestURL: ["+request.getRequestURL()+"]<br>");
//	    out.println("RequestURI: ["+request.getRequestURI()+"]<br>");
//	    out.println("PathInfo: ["+request.getPathInfo()+"]<br>");
//	    out.println("Context path: ["+request.getContextPath()+"]<br>");
//	    out.println("Header: ["+request.getHeader("Accept-Language")+"]<br>");
//	    out.println("</body></html>");
		  
		 String html = "<html><body> Test Servlet Response</body></html>"; 
//		 System.out.println("Creation time:" + request.getSession(false).getCreationTime());
//		 System.out.println("Session id:" + request.getSession(true).getId());
		 //request.getSession(true).setMaxInactiveInterval(4);
		 response.setContentType("text/html");
		 response.setContentLength(html.length());
		 
		 PrintWriter out = response.getWriter();
		 out.write(html);
		 response.sendError(400, "BAD Request");
		 //out.println("Parameternames :"+ request.getParameterMap().toString());
		 response.flushBuffer();
		 
		 
	  }

	  

	    
}
	  


