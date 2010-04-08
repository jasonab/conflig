package com.eharmony.configuration.worker.weblogic;

import com.eharmony.configuration.ConfigProperties;
import com.eharmony.configuration.ConfigReloader;
import com.eharmony.configuration.worker.ConfigReloadWork;

import commonj.work.Work;

public class WLConfigReloadWork extends ConfigReloadWork implements Work {

	public WLConfigReloadWork(ConfigProperties configProperties,
			ConfigReloader configReloader) {
		super(configProperties, configReloader);		
	}

	public boolean isDaemon() {
		return false;
	}

	public void release() {
		// Nothing to release.
	}
}
