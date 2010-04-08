package com.eharmony.configuration.reloader.standalone;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import com.eharmony.configuration.AbstractConfigReloader;
import com.eharmony.configuration.ConfigProperties;
import com.eharmony.configuration.worker.standalone.StandaloneConfigReloadWork;

public class StandaloneConfigReloader extends AbstractConfigReloader {
    ExecutorService pool;
	public StandaloneConfigReloader() {
		super();
		pool = Executors.newCachedThreadPool();
	}

	public boolean isShutdown() {
		return pool.isShutdown();
	}

	public boolean isTerminated() {
		return pool.isTerminated();
	}


	@Override
	public void reload(ConfigProperties configProperties, boolean force) {
		if (force || isReloadNecessary()) {
			pool.execute(new StandaloneConfigReloadWork(configProperties,this));
		}
	}
	
	public void reload(ConfigProperties configProperties, AtomicInteger notifier, boolean force) {
		if (force || isReloadNecessary()) {
			pool.execute(new StandaloneConfigReloadWork(configProperties, this, notifier));
		}
	}


	public void shutdown() {
		pool.shutdown();
	}

	public List<Runnable> shutdownNow() {
		return pool.shutdownNow();
	}

	public Future<?> submit(Runnable task) {
		return pool.submit(task);
	}
}
