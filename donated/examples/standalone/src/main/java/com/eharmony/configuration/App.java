package com.eharmony.configuration;

import com.eharmony.configuration.Configuration;
import com.eharmony.configuration.action.Action;
import com.eharmony.configuration.action.DefaultAction;
import com.eharmony.configuration.view.View;

public class App {
	static View defaultVew = new View();
	static Action defaultAction = new DefaultAction();
	static Configuration configuration = null;
	
	static {
		defaultVew.setTitle("This is example application, that demonstrates out of container reloading feature."
				+ "  To demo reload, create a /Data/tmp/configTest/conf/doNotDelete.txt file, and override "
				+ "any properties or add a new property ih /Data/tmp/configTest/conf/configTest.global.properties");
		defaultVew.setPrompt("Please, make selection:");
		defaultVew.getMenuItems().put(0, "quit");
		defaultVew.getMenuItems().put(1, "view config");
		defaultVew.getMenuItems().put(2, "reload config");
		
		defaultAction.setView(defaultVew);
		
		System.setProperty("server", "subscriptionServer");
		System.setProperty("cluster", "cluster1");
		System.setProperty("environment", "prod");
	}
	
	public static View getDefaultVew() {
		return defaultVew;
	}
	
	public static Action getDefaultAction() {
		return defaultAction;
	}

	public static Configuration getConfiguration() {
		return configuration;
	}		
}
