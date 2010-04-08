package com.eharmony.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.configuration.ConfigScope.ScopeType;
import com.eharmony.configuration.ConfigSource.Format;
import com.eharmony.configuration.ConfigSource.Type;

/**
 * A helper Singleton class that always return the same instance of ConfigProperties.  For use outside container, 
 * or in container but not with Spring.
 *
 */
class ConfigManagerImpl implements ConfigManager {
    private static Log logBuildConfigProperties = LogFactory.getLog(ConfigManagerImpl.class.getName() + ".buildConfigProperties");
    private static final String DEFAULT_ROOT_CONFIG = "classpath://eh-config.properties";
    private static final String DEFAULT_SCOPE_TYPES = "server,host,cluster,environment,global";
    private static ConcurrentMap<String, ConfigProperties> configs = new ConcurrentHashMap<String, ConfigProperties>();
	private ConfigUtil configUtil;

    /**
     * Allows specifying the path to root config file.  Returns a ConfigProperties with the specified
     * root config file.  <code>rootConfig</code> should start with file:// for file system path, 
     * otherwise it is treated as classpath path.
     * @param rootConfig    Path to root configuration file.  
     * @return              A ConfigProperties configured with rootConfig
     */
    public Configuration buildConfigProperties(String rootConfig) {
        Log log = logBuildConfigProperties;
        if (configs.get(rootConfig) == null) {
            long start = System.currentTimeMillis();
            Properties rootProperties = loadRootProperties(rootConfig);
        	ConfigProperties configProperties = new ConfigProperties(rootProperties);

            if (!configUtil.isEmpty(rootProperties.getProperty("failOnError"))) {
                configProperties.setFailOnError(Boolean.parseBoolean(rootProperties.getProperty("failOnError")));
            }

            if (!configUtil.isEmpty(rootProperties.getProperty("enable.reload"))) {
            	configProperties.setEnableReload(Boolean.parseBoolean(rootProperties.getProperty("enable.reload")));
                if (configProperties.isEnableReload()) {
                    String reloaderName = rootProperties.getProperty("reload.class");
                    if (configUtil.isEmpty(reloaderName)) {
                        String msg = "Reload is enabled but reload.class is not defined or is empty.";
                        if (configProperties.isFailOnError()) {
                            if (log.isErrorEnabled()) {
                                log.error(msg);
                            }
                            throw new ConfigurationRuntimeException(msg);
                        }
                        else {
                        	configProperties.setReloader(new DoNothingConfigReloader());
                            if (log.isWarnEnabled()) {
                                log.warn(msg);
                                log.warn("Configuration will not be refreshed");
                            }
                        }
                    }
                    else {
                        try {
                            Class<?> t = Class.forName(reloaderName);
                            ConfigReloader reloader = (ConfigReloader) t.newInstance();
                            try {
                                reloader.setReloadInterval(Integer.parseInt(rootProperties.getProperty("reload.interval")));
                            } catch (NumberFormatException e) {
                                reloader.setReloadInterval(ConfigReloader.DEFAULT_RELOAD_INTERVAL);
                            }
                            configProperties.setReloader(reloader);
                        } catch (ClassNotFoundException e) {
                            String msg = "Cannot find reloader class " + reloaderName;
                            if (configProperties.isFailOnError()) {
                                if(log.isErrorEnabled()) {
                                    log.error(msg, e);
                                }
                                throw new ConfigurationRuntimeException(msg, e);
                            }
                            else {
                                if (log.isWarnEnabled()) {
                                    log.warn(msg);
                                    log.warn("Configuration will not be refreshed");
                                }
                            }
                        } catch (InstantiationException e) {
                            String msg = "Cannot instantiate reloader class " + reloaderName;
                            if (configProperties.isFailOnError()) {
                                if (log.isErrorEnabled()) {
                                    log.error(msg, e);
                                }
                                throw new ConfigurationRuntimeException(msg, e);
                            }
                            else {
                                if (log.isWarnEnabled()) {
                                    log.warn(msg);
                                    log.warn("Configuration will not be refreshed");
                                }
                            }
                        } catch (IllegalAccessException e) {
                            String msg = "Cannot instantiate reloader class " + reloaderName + ".  Does it have a nullary constructor?";
                            if (configProperties.isFailOnError()) {
                                if (log.isErrorEnabled()) {
                                    log.error(msg, e);
                                }
                                throw new ConfigurationRuntimeException(msg, e);
                            }
                            else {
                                if (log.isWarnEnabled()) {
                                    log.warn(msg);
                                    log.warn("Configuration will not be refreshed");
                                }
                            }
                        }
                    }
                }
            }            
            configProperties.setConfigSources(buildConfigSources(rootProperties));
            configProperties.setConfigScopes(buildConfigScopes(rootProperties));
            
            configProperties.setLocalizedConsolidatedProperties(configUtil
					.buildLocalizedConsolidatedProperties(configProperties
							.getLocalizedConsolidatedProperties().keySet(),
							configProperties.getConfigSources(),
							configProperties.getConfigScopes(),
							configProperties.isFailOnError()));
            configs.put(rootConfig, configProperties);
            if (log.isDebugEnabled()) {
                log.debug(rootConfig + "took " + (System.currentTimeMillis() - start) + " milliseconds to load");
            }
        }
        return configs.get(rootConfig);
    }
    
    /**
     * System property always takes precedence !!!  Be extra careful when there are two applications in the same JVM.
     * In that kind of situation it's probably best that both applications use non-system property way of specifying 
     * root config.  Since setting up config.properties in System properties will override both application's 
     * root config.  This is not an ideal situation!!!  Therefore it is probably not a good idea to even try to 
     * override root config.  I think in reality this situation should rarely arise, but what if it does happen?
     *
     * Returns ConfigProperties with default root config file, i.e., eh-config.properties at root of classpath.
     */
    public Configuration buildConfigProperties() {
        Log log = logBuildConfigProperties;
        String rootConfigFile = System.getProperty("config.properties");
        if (!configUtil.isEmpty(rootConfigFile)) {
            if (log.isDebugEnabled()) {
                log.debug("Using root config defined as system property: " + rootConfigFile );
            }
        }
        else {
        	rootConfigFile = DEFAULT_ROOT_CONFIG;
            if (log.isDebugEnabled()) {
                log.debug("no rootConfig specified, using default at " + DEFAULT_ROOT_CONFIG);
            }
        }
        
        return buildConfigProperties(rootConfigFile);
    }

    /**
     * For a configured scope (other than the global scope), if we cannot find
     * the instance name, then we will simple skip the scope all together, as if
     * the scope is not even configured at all. The assumption is the immediate
     * broader scope will suffice in this case.
     * 
     * @param rootProperties root configuration properties
     * @return A list of com.eharmony.base.configuration.ConfigScope that is configured.
     */
    protected List<ConfigScope> buildConfigScopes(Properties rootProperties) {
        String configuredTypes = rootProperties.getProperty("scope.order");
        if (configUtil.isEmpty(configuredTypes)) {
            configuredTypes = DEFAULT_SCOPE_TYPES;
        }
        String[] scopes = configuredTypes.split(",");
        List<ConfigScope> configScopes = new ArrayList<ConfigScope>();
        FOR: for (int i = scopes.length - 1; i >= 0; i--) {
            // reversing order so scope goes from broad to narrow in the list
            String scopeType = scopes[i].trim();
            ConfigScope scope = new ConfigScope();
            scope.setType(ConfigScope.ScopeType.valueOf(scopeType));

            // for each scope type, find out prefix and actual instance name,
            // such as dc1-umsg01 for host, wl-umsg01 for server, etc.
            String prefix = null;
            String instanceName;
            ScopeType type = scope.getType();
            switch (type) {
            case global:
                prefix = rootProperties.getProperty("scope.global.prefix");
                break;

            case environment:
                instanceName = configUtil.getEnvironment();
                if (configUtil.isEmpty(instanceName)) {
                    continue FOR; // skip this scope since instance name is
                    // not there
                }
                scope.setInstanceName(instanceName);

                prefix = rootProperties.getProperty("scope.environment.prefix");
                break;

            case cluster:
                instanceName = configUtil.getCluster();
                if (configUtil.isEmpty(instanceName)) {
                    continue FOR; // skip this scope since instance name is
                    // not there
                }
                scope.setInstanceName(instanceName);

                prefix = rootProperties.getProperty("scope.cluster.prefix");
                break;

            case host:
                instanceName = configUtil.getHost();
                if (configUtil.isEmpty(instanceName)) {
                    continue FOR; // skip this scope since instance name is
                    // not there
                }
                scope.setInstanceName(instanceName);

                prefix = rootProperties.getProperty("scope.host.prefix");
                break;

            case server:
                instanceName = configUtil.getServer();
                if (configUtil.isEmpty(instanceName)) {
                    continue FOR; // skip this scope since instance name is
                    // not there
                }
                scope.setInstanceName(instanceName);

                prefix = rootProperties.getProperty("scope.server.prefix");
                break;

            default:
                break;
            }
            if (configUtil.isEmpty(prefix)) {
                prefix = type.getDefaultPrefix();
            }
            else {
                prefix = prefix.trim();
            }
            scope.setPrefix(prefix);
            configScopes.add(scope);
        }
        return configScopes;
    }
    
    protected List<ConfigSource> buildConfigSources(Properties rootProperties) {
        String sourceNames = rootProperties.getProperty("config.order");
        String[] sources = sourceNames.split(",");
        List<ConfigSource> configSources = new ArrayList<ConfigSource>();
        for (String name : sources) {
            name = name.trim();
            String prefix = "config." + name + ".";

            ConfigSource source = new ConfigSource();

            source.setName(name);
            source.setLocation(rootProperties.getProperty(prefix + "location"));

            // file name is optional, default to config source name
            String temp = rootProperties.getProperty(prefix + "name");
            if (!configUtil.isEmpty(temp)) {
                source.setFileName(temp);
            }

            // format is optional, default to Properties
            temp = rootProperties.getProperty(prefix + "format");
            if (!configUtil.isEmpty(temp)) {
                source.setFormat(Format.valueOf(temp));
            }

            // type is optional, default to classpath
            temp = rootProperties.getProperty(prefix + "type");
            if (!configUtil.isEmpty(temp)) {
                source.setType(Type.valueOf(temp));
            }

            // testFile is optional, default is null
            temp = rootProperties.getProperty(prefix + "testFile");
            if (!configUtil.isEmpty(temp)) {
                source.setTestFile(temp);
            }

            configSources.add(source);
        }
        return configSources;
    }
    
    private Properties loadRootProperties(String rootConfigFile) {
        InputStream stream;
        if (rootConfigFile.startsWith("file://")) {
            stream = configUtil.getInputStreamFromFileSystem(rootConfigFile.substring(7));
        } 
        else if (rootConfigFile.startsWith("classpath://"))  {
            stream = configUtil.getInputStreamFromClasspath(rootConfigFile.substring(12));
        }
        else {
            throw new ConfigurationRuntimeException("Path to root configuration file must start with either file:// or classpath://.  Path: " + rootConfigFile);
        }
        if (stream == null) {
            throw new ConfigurationRuntimeException("Cannot configure application because rootConfig cannot be located at "
                    + rootConfigFile);
        }

        Properties rootProperties = new Properties();
        try {
            rootProperties.load(stream);
        } catch (IOException e) {
            throw new ConfigurationRuntimeException("Cannot configure application because rootConfig cannot be read at "
                    + rootConfigFile);
        }
        return rootProperties;
    }

	public void setConfigUtil(ConfigUtil util) {
		this.configUtil = util;
		
	}
    
}
