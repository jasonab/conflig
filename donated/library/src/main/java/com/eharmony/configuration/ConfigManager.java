package com.eharmony.configuration;

public interface ConfigManager {
    Configuration buildConfigProperties();
    Configuration buildConfigProperties(String rootConfig);
}
