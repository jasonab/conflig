package com.eharmony.configuration.controller;

import com.eharmony.configuration.App;
import com.eharmony.configuration.action.Action;

public class Controller {

	public void control() {
		Action action = App.getDefaultAction();
		for (;;) {
			action = action.perform();
			if (action == null) {
				break;
			}			
		}
		System.out.println("Bye ;)");
	}
}
