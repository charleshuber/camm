package com.surfthevoid.camm;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.surfthevoid.camm.video.VideoStreamJavaCV;

@WebServlet(urlPatterns = "/state/*", loadOnStartup = 1)
public class HelloStateServlet extends HttpServlet   {
	private static final long serialVersionUID = 1L;
	
	private VideoStreamJavaCV videoStream = new VideoStreamJavaCV("/dev/video1");
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
	    doGet(request,response);
	}
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            response.setContentType("image/png");
	    response.getOutputStream().write(videoStream.getBytes());
	}
} 
