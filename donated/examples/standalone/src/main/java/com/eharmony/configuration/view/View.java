package com.eharmony.configuration.view;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

public class View {
	String error;
	Map<Integer, String> menuItems = new HashMap<Integer, String>();
	String prompt;
	String title;

	public void display() {
		if (!StringUtils.isEmpty(title)) {
			System.out.println("\n\n\n");
			System.out.println(title);
			System.out.println("\n");
		}
		if (!StringUtils.isEmpty(error)) {
			System.out.println(error);
			System.out.println();
		}
		System.out.println(prompt);
		Set<Integer> keys = menuItems.keySet();

		for (int key : keys) {
			StringBuilder sb = new StringBuilder();
			sb.append(key).append(": ").append(menuItems.get(key));
			System.out.println(sb.toString());
		}
	}

	public String getError() {
		return error;
	}

	public String getPrompt() {
		return prompt;
	}

	public String getTitle() {
		return title;
	}

	public void setError(String error) {
		this.error = error;
	}

	public void setItems(Map<Integer, String> items) {
		this.menuItems = items;
	}

	public void setPrompt(String prompt) {
		this.prompt = prompt;
	}

	public Map<Integer, String> getMenuItems() {
		return menuItems;
	}

	public void setMenuItems(Map<Integer, String> menuItems) {
		this.menuItems = menuItems;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
