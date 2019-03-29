package com.surfthevoid.camm;

import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {
    	System.out.println(System.getProperty("java.library.path"));
    	System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    	SpringApplication.run(Application.class, args);
    }
}
