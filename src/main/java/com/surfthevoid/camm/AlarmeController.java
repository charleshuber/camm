package com.surfthevoid.camm;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/alarm")
public class AlarmeController {
	
	public static final AtomicBoolean enable = new AtomicBoolean(false);
	public static final AtomicInteger threshold = new AtomicInteger(15);
	
	@GetMapping(value="/enable", produces = "application/json")	
	public boolean enable(){
		return enable.get();
	}
	
	@GetMapping(value="/enable/{enable}", produces = "application/json")	
	public boolean enable(@PathVariable boolean enable){
		AlarmeController.enable.set(enable);
		return AlarmeController.enable.get();
	}
	
	@GetMapping(value="/threadshold", produces = "application/json")	
	public int threadshold(){
		return threshold.get();
	}
	
	@GetMapping(value="/threadshold/{threadshold}", produces = "application/json")	
	public int threadshold(@PathVariable int threadshold){
		AlarmeController.threshold.set(threadshold);
		return AlarmeController.threshold.get();
	}
}
