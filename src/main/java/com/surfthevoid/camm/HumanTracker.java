package com.surfthevoid.camm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencv.core.Mat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.surfthevoid.camm.video.VideoSource;

@Component
public class HumanTracker {
	
	private final Log log = LogFactory.getLog(HumanTracker.class);
	private MailSender mailSender;
	private VideoSource videoSource;
	private String HRHost;
	private Integer HRPort;

	Mat psnrDiff = new Mat();
	
	public HumanTracker(
			VideoSource videoSource, 
			MailSender mailSender,
			@Value("${hr.process.host}") String HRHost,
			@Value("${hr.process.port}") Integer HRPort){
		this.videoSource = videoSource;
		this.mailSender = mailSender;
		this.HRHost = HRHost;
		this.HRPort = HRPort;
	}
	
	//@Scheduled(fixedRate = 1000)
	public void checkDiff(){
		Optional<byte[]> optHumanCapture = trackHuman(getBytes());
		if(optHumanCapture.isPresent()){
			log.info("Human tracked");
			if(AlarmeController.enable.get()){
				log.info("Sending email for human captured !");
				String msg = "<p>INVASION !!! </p><br><p>Your camera has detected a human presence !</p><br><a href=\"https://83.194.12.58\">CAMERA</a>";
				mailSender.sendMail(msg, optHumanCapture.get());
			}
		}
	}
	
	public Optional<byte[]> trackHuman(byte[] bytes) {
		try(Socket clientSocket = new Socket(HRHost, HRPort);
				InputStream is = clientSocket.getInputStream();
				OutputStream os = clientSocket.getOutputStream()){
			if(bytes.length > 0){
				os.write(bytes);
			}
			byte[] size_bytes = new byte[4];
			if(is.read(size_bytes) < 0){
				System.out.println("Could not read image size");
			}
			ByteBuffer wrapped = ByteBuffer.wrap(size_bytes); // big-endian by default
			int size = wrapped.getInt();
			if(size > 0){
				byte[] data = new byte[size];
				for(int byt=is.read(), pos=0; byt>-1; ++pos, byt=is.read()){
					data[pos] = (byte)byt;
				}
				return Optional.of(data);
			}
		} catch(IOException | RuntimeException e){
			log.error("could not process img", e);
		}
		return Optional.empty();
	}

	protected byte[] getBytes(){
		byte[] file = videoSource.getJPEGStreamBytes();
		int int_size = 4;
		int fileSize = new Long(file.length).intValue();
		ByteBuffer b = ByteBuffer.allocate(int_size);
		b.putInt(fileSize);
		byte[] size_bytes = b.array();
		byte[] bytes = new byte[fileSize + int_size];
		// copy size bytes
		for (int i = 0; i < int_size; i++) {
			bytes[i] = size_bytes[i];
		}
		// copy images bytes
		for (int i = 0; i < fileSize; i++) {
			bytes[i + int_size] = file[i];
		}
		return bytes;
	}
	
}
