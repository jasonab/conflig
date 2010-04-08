package com.eharmony.configuration;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.configuration.worker.ConfigReloadWork;

/**
 * User: fwu
 */
public abstract class AbstractConfigReloader implements ConfigReloader {
    private Log logReload = LogFactory.getLog(ConfigReloader.class.getName() + ".reload");

    private AtomicLong lastReloadedAt = new AtomicLong(System.currentTimeMillis());

    private AtomicInteger  reloadInterval = new AtomicInteger(0);

    public int getReloadInterval() {
        return reloadInterval.getAndAdd(0);
    }

    public void setReloadInterval(int reloadInterval) {
        this.reloadInterval.getAndSet(reloadInterval);
    }
    
    public void setLastReloadedAt(long lastReloadedAt) {
        this.lastReloadedAt.getAndSet(lastReloadedAt);
    }

    public abstract void reload(ConfigProperties configProperties, boolean force);
    
    public boolean isReloadNecessary() {
        return (System.currentTimeMillis() - lastReloadedAt.getAndAdd(0)) > getReloadInterval();

    }
    
    static public void reloadLocale(ConfigProperties configProperties, Locale locale) {
    	ConfigReloadWork.reloadLocale(configProperties, locale);
    }
    
}
