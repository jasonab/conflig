package com.eharmony.configuration;

/**
 * A Singleton wrapper around ConfigManager, for use outside container or 
 * in container but without using Spring integration.
 * @author fwu
 *
 */
public class SingletonConfigManagerImpl extends ConfigManagerImpl {	
	/**
	 * private constructor.
	 */
	private SingletonConfigManagerImpl(){
		setConfigUtil(ConfigUtil.getInstance());
	}
	
	private static class SingletonConfigManagerHolder { 
		private final static SingletonConfigManagerImpl instance = new SingletonConfigManagerImpl();
	}
	
	public static SingletonConfigManagerImpl getInstance() {
	     return SingletonConfigManagerHolder.instance;
	}
}
