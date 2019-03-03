package com.surfthevoid.camm.video;
/**
import java.util.Arrays;

import org.bytedeco.javacpp.avcodec.AVPacket;
import org.bytedeco.javacpp.avdevice;
import org.bytedeco.javacpp.avformat;
import org.bytedeco.javacpp.avformat.AVFormatContext;
import org.bytedeco.javacpp.avformat.AVInputFormat;
import org.bytedeco.javacpp.avutil;
import org.bytedeco.javacpp.avutil.AVDictionary;
**/

public class VideoStreamFFMPEG {
	/**
	private String fileName;
	private AVInputFormat inputFormat;
	private byte[] data = new byte[0];
	Boolean ready = false;
	
	public VideoStreamFFMPEG(String filename, String format){
		this.fileName = filename;
		inputFormat = avformat.av_find_input_format(format);
		init();
	}
	
	public byte[] data(){
		synchronized (data) {
			return Arrays.copyOf(data, data.length);
		}
	}
	
	private void init(){
		synchronized(ready){
			avdevice.avdevice_register_all();
	    	AVFormatContext formatContext = new AVFormatContext(null);
	    	
	    	AVDictionary formatOptions = new AVDictionary(null);
	    	avutil.av_dict_set(formatOptions, "framerate", "30", 0);
	    	avutil.av_dict_set(formatOptions, "video_size", "320x240", 0);
	    	avformat.avformat_open_input(formatContext, fileName, inputFormat, formatOptions);
	    	avformat.av_dump_format(formatContext, 0, fileName, 0);
	    	
	    	avutil.av_dict_free(formatOptions);
	    	
	    	
	    	Thread thread = new Thread(new Runnable(){
				@Override
				public void run() {
					try{
						Boolean done = false;
				    	while(!done){
				    		AVPacket pkt = new AVPacket();
				    		int err = avformat.av_read_frame(formatContext, pkt);
				    		if(err > 0){
				    			done = true;
				    		}
				    		synchronized (data) {
					    		data = new byte[pkt.size()];
					    		pkt.data().get(data, 0, data.length);
				    		}
				    		if(!ready){
				    			synchronized(ready){
				    				ready.notify();
				    			}
				    		}
				    	}
					} finally {
						avformat.avformat_free_context(formatContext);
					}
				}
	    	});
	    	thread.setDaemon(true);
	    	thread.start();
    		try {
				ready.wait();
			} catch (InterruptedException e) {}
    	}
	}
	**/
}
