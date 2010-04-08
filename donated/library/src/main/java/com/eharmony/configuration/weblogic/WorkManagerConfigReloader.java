package com.eharmony.configuration.weblogic;

import com.eharmony.configuration.AbstractConfigReloader;
import com.eharmony.configuration.ConfigProperties;
import com.eharmony.configuration.reloader.weblogic.WLConfigReloader;

import commonj.work.WorkException;
import commonj.work.WorkManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Properties;

/**
 * @deprecated use {@link WLConfigReloader}
 */
@Deprecated
public class WorkManagerConfigReloader extends AbstractConfigReloader {
	WLConfigReloader reloader = new WLConfigReloader();
	
	@Override
	public void reload(ConfigProperties configProperties, boolean force) {		
		reloader.reload(configProperties, force);		
	}

	@Override
    public boolean isReloadNecessary() {    	
    	return  reloader.isReloadNecessary();
    }
	
	@Override
    public void setLastReloadedAt(long lastReloadedAt) {
       reloader.setLastReloadedAt(lastReloadedAt);
    }
}
