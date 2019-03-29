package com.surfthevoid.camm;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.surfthevoid.camm.video.VideoSource;

@Component
public class PSNRTracker {
	
	private final Log log = LogFactory.getLog(PSNRTracker.class);
	
	private MailSender mailSender;
	private VideoSource videoSource;
	private Mat I1;
	private Mat I2;
	
	public PSNRTracker(VideoSource videoSource, MailSender mailSender){
		this.videoSource = videoSource;
		this.mailSender = mailSender;
	}
	
	@Scheduled(fixedRate = 1000)
	public void checkDiff(){
		if(initPhase()){
			return;
		}
		I1 = I2;
		loadI2();
		double psnr = getPSNR();
		if(psnr < 15){
			log.info("Sending email for psnr " + psnr);
			mailSender.sendMail("<p>Warning !!! PSNR: " + psnr + "</p><a href=\"https://83.194.12.58\">CAMERA</a>");
		}
	}
	
	private Boolean initPhase(){
		return initPrev() || initCurrent();
	}
	
	private Boolean initPrev(){
		if(I1 == null){
			return loadI1();
		}
		return false;
	}
	
	private Boolean initCurrent(){
		if(I2 == null){
			return loadI2();
		}
		return false;
	}
	
	private Boolean loadI1(){
		Optional<Mat> optMat = videoSource.grabFrame(false);
		videoSource.close(false);
		if(optMat.isPresent()){
			I1 = optMat.get();
			return true;
		}
		return false;	
	}
	
	private Boolean loadI2(){
		Optional<Mat> optMat = videoSource.grabFrame(false);
		videoSource.close(false);
		if(optMat.isPresent()){
			I2 = optMat.get();
			return true;
		}
		return false;	
	}

	private double getPSNR() {
		Mat s1 = new Mat();
		Core.absdiff(I1, I2, s1); // |I1 - I2|
		s1.convertTo(s1, CvType.CV_32FC1); // cannot make a square on 8 bits
		s1 = s1.mul(s1); // |I1 - I2|^2
		Scalar s = Core.sumElems(s1); // sum elements per channel
		double sse = s.val[0] + s.val[1] + s.val[2]; // sum channels
		if (sse <= 1e-10) // for small values return zero
			return 0;
		else {
			double mse = sse / (double) (I1.channels() * I1.total());
			double psnr = 10.0 * Math.log10((255 * 255) / mse);
			return psnr;
		}
	}
}
