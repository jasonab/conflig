package com.eharmony.configuration.action;

import com.eharmony.configuration.App;
import com.eharmony.configuration.ConfigProperties;
import com.eharmony.configuration.ConfigReloader;
import com.eharmony.configuration.Configuration;

public class DefaultAction extends Action {

	void viewConfig() {
		Configuration config = App.getConfiguration();
		System.out.println("*****Loaded properties from "
				+ ((ConfigProperties) config).getRootProperties().getProperty(
						"config.order") + " *****");
		config.list(System.out);
		config.getProperty("line.separator");  // this should trigger the reload when interval has passed.

	}

	void reloadConfig() {
		Configuration config = App.getConfiguration();
		ConfigReloader reloader = ((ConfigProperties) config).getReloader();
		reloader.reload(((ConfigProperties) config), true);
	}

	@Override
	protected Action execute(int command) {
		Action res = this;

		switch (command) {
		case 0:
			res = null;
			break;
		case 1:
			viewConfig();
			break;
		case 2:
			reloadConfig();
			break;
		default:
			view.setError(MSG_ERRROR);
		}
		return res;
	}
}