package com.surfthevoid.camm;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;

import com.surfthevoid.camm.video.VideoStreamJavaCV;

@WebServlet(urlPatterns = "/state/*", loadOnStartup = 1)
public class HelloStateServlet extends HttpServlet   {
	private static final long serialVersionUID = 1L;
	
	private String camurl;
	private VideoStreamJavaCV videoStream;
	
	public HelloStateServlet(@Value("#{systemProperties.cammurl}") String camurl){
		this.camurl = camurl;
		this.videoStream = new VideoStreamJavaCV(this.camurl);
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
	    doGet(request,response);
	}
	
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("image/png");
    response.getOutputStream().write(videoStream.getBytes());
	}
} 
