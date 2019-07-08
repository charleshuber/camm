package com.surfthevoid.camm;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.springframework.stereotype.Component;

import com.surfthevoid.camm.video.VideoSource;

@Component
public class NormalizedDifferenceTracker {
	
	private final Log log = LogFactory.getLog(NormalizedDifferenceTracker.class);
	
	private MailSender mailSender;
	private VideoSource videoSource;
	private Mat I1;
	private Mat I2;
	Mat psnrDiff = new Mat();
	
	public NormalizedDifferenceTracker(VideoSource videoSource, MailSender mailSender){
		this.videoSource = videoSource;
		this.mailSender = mailSender;
	}
	
	//@Scheduled(fixedRate = 1000)
	public void checkDiff(){
		if(initPhase()){
			return;
		}
		I2.copyTo(I1);
		videoSource.grabGrayFrame(false, I2);
		double normalizedDifference = getNormalizedDifference();
		if(normalizedDifference < AlarmeController.threshold.get()){
			log.info("Normalized difference at " + normalizedDifference);
			if(AlarmeController.enable.get()){
				log.info("Sending email for psnr " + normalizedDifference);
				String msg = "<p>Warning !!! Normalized difference: " + normalizedDifference + "</p><a href=\"https://83.194.12.58\">CAMERA</a>";
				mailSender.sendMail(msg, videoSource.toJPEG(I1), videoSource.toJPEG(I2));
			}
		}
	}
	
	private Boolean initPhase(){
		return initPrev() || initCurrent();
	}
	
	private Boolean initPrev(){
		if(I1 == null){
			I1 = new Mat();
			videoSource.grabGrayFrame(false, I1);
			return true;
		}
		return false;
	}
	
	private Boolean initCurrent(){
		if(I2 == null){
			I2 = new Mat();
			videoSource.grabGrayFrame(false, I2);
		}
		return false;
	}

	private double getNormalizedDifference() {
		try{
			Core.absdiff(I1, I2, psnrDiff); // |I1 - I2|
			Scalar s = Core.sumElems(psnrDiff); // sum elements per channel
			double sse = s.val[0] + s.val[1] + s.val[2]; // sum channels
			if (sse <= 1e-10) // for small values return zero
				return 0;
			else {
				double mse = sse / (double) (I1.channels() * I1.total());
				double normalizedDifference = 10.0 * Math.log10(255 / mse);
				return normalizedDifference;
			}
		} catch(Exception e){
			log.error("Exception during difference computation !", e);
		}
		// do not detect anything in case of an exception;
		return 100.0;
	}
}
