package com.eharmony.configuration.action;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.configuration.view.View;

public abstract class Action {
	private final static Log log = LogFactory.getLog(Action.class);
	public final static String MSG_ERRROR = "Incorrect input!";
	View view;
	
	protected abstract Action execute(int command);

	public View getView() {
		return view;
	}

	public Action perform() {
		if (view != null) {
			view.display();
			view.setError(null);
		}
		return execute(readCommand());
	}

	protected int readCommand() {
		String command = readText();
		if (StringUtils.isEmpty(command) || !StringUtils.isNumeric(command)) {
			view.setError(MSG_ERRROR);
			return -1;
		}

		return Integer.parseInt(command);
	}

	protected String readText() {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String text = null;
		try {
			text = br.readLine();
		} catch (IOException e) {
			if (log.isDebugEnabled()) {
				log.debug("catn't read user input", e);
			}
		}

		return text;
	}

	public void setView(View view) {
		this.view = view;
	}

}
