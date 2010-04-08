package com.eharmony.configuration.worker.standalone;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.configuration.ConfigProperties;
import com.eharmony.configuration.ConfigReloader;
import com.eharmony.configuration.worker.ConfigReloadWork;

public class StandaloneConfigReloadWork extends ConfigReloadWork implements
		Runnable {
	private Log log = LogFactory.getLog(this.getClass());

	private AtomicInteger notifier;

	public StandaloneConfigReloadWork(ConfigProperties configProperties,
			ConfigReloader configReloader) {
		super(configProperties, configReloader);
	}

	public StandaloneConfigReloadWork(ConfigProperties configProperties,
			ConfigReloader configReloader, AtomicInteger notifier) {
		super(configProperties, configReloader);
		this.notifier = notifier;
	}

	@Override
	public void run() {
		if (log.isTraceEnabled()) {
			log.trace("started");
		}
		super.run();

		if (log.isTraceEnabled()) {
			log.trace("finished");
		}
		
		if (notifier != null) {
			synchronized (notifier) {
				notifier.incrementAndGet();
				notifier.notifyAll();
			}
		}
	}
}
