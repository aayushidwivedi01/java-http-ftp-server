package edu.upenn.cis.cis455.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class ServletWriter extends PrintWriter{
	Response response;
	public ServletWriter(Writer out, boolean autoFlush, Response response) {
		super(out, autoFlush);
		this.response = response;
	}

	@Override
	public void flush(){
		try {
			response.flushBuffer();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
