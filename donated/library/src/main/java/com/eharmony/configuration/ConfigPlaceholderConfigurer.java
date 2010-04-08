package com.eharmony.configuration;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.util.Properties;

/**
 * This class does what Spring's PropertyPlaceholderConfigurer does, except that it checks the configuration
 * library first, assuming a Configuration dependency is injected.  Otherwise it will behave exactly the same
 * as PropertyPlaceholderConfigurer.
 * <p>
 * There are some limitations with regard to how this class works with configuration-library's reloading
 * feature.  The short of that is it does not.  Since PropertyPlacholderConfigurer works right after
 * ApplicationContext is initialized, property resolution only happen once at that time.  Subsequent
 * properties reloading will have no effect as any beans configured this way have already have their
 * properties resolved right after ApplicationContext creation.
 * <p>
 * For the same reason, this class only works with base locale which, in this case, is the empty locale.
 * <p>
 * Finally we are only using configuration-library to resolve properties as String.  Any type conversion
 * is done by Spring.
 */
public class ConfigPlaceholderConfigurer extends PropertyPlaceholderConfigurer {

    private Configuration configuration;

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    protected String resolvePlaceholder(String key, Properties properties) {
        if (configuration != null) {
            String result =  configuration.getProperty(key);
            if (result != null) {
                return result;
            }
        }
        return super.resolvePlaceholder(key, properties);
    }
}
