package com.eharmony.configuration;

public interface ConfigReloader {
    int DEFAULT_RELOAD_INTERVAL = 5 * 60 * 1000; // 5 minutes

    /**
     * Schedule configuration reload at specified interval
     * @param configProperties The ConfigProperties object that is going to be refreshed.
     * @param force
     */
    void reload(ConfigProperties configProperties, boolean force);

    /**
     * Milliseconds beyond which a configuration reload should occur.
     */
    int getReloadInterval();
    void setReloadInterval(int defaultReloadInterval);

    void setLastReloadedAt(long l);

    boolean isReloadNecessary();
}
