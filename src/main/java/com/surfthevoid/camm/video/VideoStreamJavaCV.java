package com.surfthevoid.camm.video;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_imgcodecs;
import org.bytedeco.javacpp.opencv_imgproc;
import org.bytedeco.javacpp.opencv_videoio.VideoCapture;
import org.opencv.imgproc.Imgproc;

public class VideoStreamJavaCV {
	
		
		// the OpenCV object that realizes the video capture
		private VideoCapture capture = new VideoCapture();
		
		
		public VideoStreamJavaCV(String cameraId){
			// start the video capture
			this.capture.open(cameraId);
		}
		
		public byte[] getBytes(){
			try{
				// is the video stream available?
				if (this.capture.isOpened()){
					// effectively grab and process a single frame
					Mat frame = grabFrame();
					byte[] result = new byte[frame.arraySize()];
					opencv_imgcodecs.imencode(".png", frame, result);
					return result;
				} else {
					// log the error
					System.err.println("Impossible to open the camera connection...");
				}
			} catch(RuntimeException e){
				System.err.println("Oups");
			}
			return new byte[0];
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
					opencv_imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
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
			if(this.capture.isOpened()){
				this.capture.release();
			}
		}
}
