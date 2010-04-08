package com.eharmony.configuration;

import java.io.IOException;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;


/**
 * A very simple Spring FactoryBean to create a ConfigProperties.  Configure in Spring as follows:
 * <p/>
 * <code>
 * &lt;bean id="configUtil" class="com.eharmony.configuration.ConfigUtil" factory-method="getInstance"/&gt;<br/>
 * <p/>
 * &lt;bean id="configManager" class="com.eharmony.base.configuration.ConfigManagerImpl"&gt;<br/>
 *		&nbsp;&nbsp;&lt;property name="configUtil"&gt;<br/>
 *			&nbsp;&nbsp;&nbsp;&nbsp;&lt;ref bean="configUtil" /&gt;<br/>
 *		&nbsp;&nbsp;&lt;/property&gt;<br/>
 * &lt;/bean&gt;
 * <p/>
 * &lt;bean id="configProperties" class="com.eharmony.configuration.ConfigPropertiesFactoryBean" scope="singleton"&gt;<br/>
 *      &nbsp;&nbsp;&lt;property name="singleton" value="true"/&gt;<br/>
 *      &nbsp;&nbsp;&lt;property name="rootConfig" value="path/to/eh-config.properties"/&gt;<br/>
 *		&nbsp;&nbsp;&lt;property name="manager"&gt;<br/>
 *			&nbsp;&nbsp;&nbsp;&nbsp;&lt;ref bean="configManager" /&gt;<br/>
 *		&nbsp;&nbsp;&lt;/property&gt;<br/>
 * &lt;/bean&gt;
 * </code>
 */
public class ConfigPropertiesFactoryBean implements FactoryBean, InitializingBean {
    private boolean singleton = true;
    private Object singletonInstance;
    private String rootConfig;  // The properties file that configures ConfigProperties
    private ConfigManagerImpl manager;

    public Object getObject() throws Exception {
        if (this.singleton) {
            return this.singletonInstance;
        }
        else {
            return createInstance();
        }
    }

    @SuppressWarnings("unchecked")
    public Class getObjectType() {
        return ConfigProperties.class;
    }

    /**
     * Set whether a shared 'singleton' ConfigProperties instance should be
     * created, or rather a new ConfigProperties instance on each request.
     * <p>Default is "true" (a shared singleton).
     * @param singleton
     */
    public final void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public final boolean isSingleton() {
        return this.singleton;
    }

    public final void afterPropertiesSet() throws IOException {
        if (this.singleton) {
            this.singletonInstance = createInstance();
        }
    }

    private Object createInstance() {
        String rootConfigFile = System.getProperty("config.properties");
        if (rootConfigFile != null && rootConfigFile.trim().length() > 0) {
            return manager.buildConfigProperties(rootConfigFile);
        }

        return manager.buildConfigProperties(getRootConfig());
    }

	public String getRootConfig() {
		return rootConfig;
	}

	public void setRootConfig(String rootConfig) {
		this.rootConfig = rootConfig;
	}

	public void setManager(ConfigManagerImpl manager) {
		this.manager = manager;
	}
}
