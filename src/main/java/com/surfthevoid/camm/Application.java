package com.surfthevoid.camm;

import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {
    	System.out.println(System.getProperty("java.library.path"));
	System.out.println("Start loading libs");
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	System.out.println("End loading libs");
    	SpringApplication.run(Application.class, args);
    }
}
