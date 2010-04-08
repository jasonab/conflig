package com.eharmony.configuration;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.eharmony.configuration.Configuration;
import com.eharmony.configuration.controller.Controller;


public class WithSpringApp extends App {
	
	public static void main(String[] args) {
		
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		configuration = (Configuration) context.getBean("configProperties");
		
		Controller controller = new Controller();
		controller.control();
		
	}

}
