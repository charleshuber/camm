package com.surfthevoid.camm.video;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VideoStreamJavaCV {
	
		private String cameraId;
		private VideoCapture capture = new VideoCapture();
		
		public VideoStreamJavaCV(@Value("${cammurl}") String cameraId){
			this.cameraId = cameraId;
		}
		
		public byte[] getBytes(){
			try{
				if (open()){
					Mat original = grabFrame();
					MatOfByte buf = new MatOfByte();
					Imgcodecs.imencode(".jpg", original, buf);
					return buf.toArray();
				} else {
					// log the error
					System.err.println("Impossible to open the camera connection...");
				}
			} catch(RuntimeException e){
				System.err.println("Oups");
			}
			return new byte[0];
		}
		
		public void close(){
			if(this.capture.isOpened()){
				this.capture.release();
			}
		}
		
		private Boolean open(){
			if (this.capture.isOpened()){
				return true;
			}
			this.capture.open(cameraId, Videoio.CAP_ANY);
			return this.capture.isOpened();
		}
		
		/**
		 * Get a frame from the opened video stream (if any)
		 *
		 * @return the {@link Mat} to show
		 */
	
		private Mat grabFrame() {
			// init everything
			Mat frame = new Mat();
			// check if the capture is open
			try{
				// read the current frame
				this.capture.read(frame);
				// if the frame is not empty, process it
				if (!frame.empty()){
					Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
				}		
			}
			catch (Exception e){
				System.err.println("Exception during the image elaboration: " + e);
			}
			return frame;
		}
		
		@Override
		protected void finalize() throws Throwable {
			super.finalize();
			this.close();
		}
		
}
