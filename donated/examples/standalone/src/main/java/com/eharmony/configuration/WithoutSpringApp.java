package com.eharmony.configuration;

import com.eharmony.configuration.ConfigProperties;
import com.eharmony.configuration.SingletonConfigManagerImpl;
import com.eharmony.configuration.controller.Controller;

public class WithoutSpringApp extends App {

	public static void main(String[] args) {
		
		SingletonConfigManagerImpl manager = SingletonConfigManagerImpl
		.getInstance();
		configuration = (ConfigProperties) manager.buildConfigProperties("classpath://root-config.properties");
		
		Controller controller = new Controller();
		controller.control();
		
	}
}
