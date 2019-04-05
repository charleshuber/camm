package com.surfthevoid.camm.video;

import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VideoSource {
	
	private final Log log = LogFactory.getLog(VideoSource.class);

	private String cameraId;
	private VideoCapture capture = new VideoCapture();
	private Boolean streaming = new Boolean(false);
	private Mat frame = new Mat();

	public VideoSource(@Value("${cammurl}") String cameraId) {
		this.cameraId = cameraId;
	}
	
	public byte[] getRawStreamBytes() {
		Optional<Mat> original = grabFrame(true);
		if (original.isPresent()) {
			Mat img = original.get();
			img.type();
			img.channels();
			Size size = img.size();
			int bytes = (int) (size.width * size.height);
			byte[] data = new byte[bytes];
			img.get(0, 0, data);
			return data;
		}
		return new byte[0];
	}
	
	public byte[] getJPEGStreamBytes() {
		Optional<Mat> original = grabFrame(true);
		if (original.isPresent()) {
			return toJPEG(original.get());
		}
		return new byte[0];
	}
	
	public byte[] toJPEG(Mat img){
		MatOfByte buf = new MatOfByte();
		Imgcodecs.imencode(".jpg", img, buf);
		return buf.toArray();
	}

	/**
	 * Get a frame from the opened video stream (if any)
	 *
	 * @return the {@link Mat} to show
	 */

	public Optional<Mat> grabFrame(Boolean streaming) {
		synchronized (cameraId) {
			if(streaming){
				this.streaming = true;
			}
			if (open()) {
				// check if the capture is open
				try {
					// read the current frame
					this.capture.read(frame);
					// if the frame is not empty, process it
					if (!frame.empty()) {
						Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2GRAY);
					}
					return Optional.of(frame);
				} catch (Exception e) {
					log.error("Exception during the image elaboration", e);
				}
			}
			return Optional.empty();
		}
	}
	
	public void close(Boolean streaming) {
		synchronized (cameraId) {
			if(!streaming && this.streaming){
				return;
			}
			if(streaming){
				this.streaming = false;
			}
			if (this.capture.isOpened()) {
				this.capture.release();
			}
		}
	}

	private Boolean open() {
		if (this.capture.isOpened()) {
			return true;
		}
		this.capture.open(cameraId, Videoio.CAP_V4L2);
		return this.capture.isOpened();
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		this.close(true);
	}

}
