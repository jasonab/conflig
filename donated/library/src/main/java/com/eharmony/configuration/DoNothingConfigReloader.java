package com.eharmony.configuration;

public class DoNothingConfigReloader extends AbstractConfigReloader {

    public void reload(ConfigProperties configProperties, boolean force) {
        // Do nothing, do not reload
    }

    public void setLastReloadedAt(long l) {
        // no op
    }

    public boolean isReloadNecessary() {
        return false;
    }
}
