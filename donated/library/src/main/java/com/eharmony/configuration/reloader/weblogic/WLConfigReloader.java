package com.eharmony.configuration.reloader.weblogic;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.configuration.AbstractConfigReloader;
import com.eharmony.configuration.ConfigProperties;
import com.eharmony.configuration.worker.weblogic.WLConfigReloadWork;

import commonj.work.WorkException;
import commonj.work.WorkManager;

public class WLConfigReloader extends AbstractConfigReloader {
    private Log logReload = LogFactory.getLog(WLConfigReloader.class.getName() + ".reload");
    private String DEFAULT_WORK_MANAGER_NAME = "wm/ConfigWorkManager";

    private String workManagerName;

    public void reload(ConfigProperties configProperties, boolean force) {
        Log log = logReload;
        if (force || isReloadNecessary()) {
            if (workManagerName == null || workManagerName.trim().length() == 0) {
                Properties rootProperties = configProperties.getRootProperties();
                String temp = rootProperties.getProperty("work.manager.name");
                if (temp == null || temp.trim().length() == 0) {
                    workManagerName = DEFAULT_WORK_MANAGER_NAME;
                    if (log.isDebugEnabled()) {
                        log.debug("No WorkManager configured, using default " + DEFAULT_WORK_MANAGER_NAME);
                    }
                }
                else {
                    workManagerName = temp;
                }
            }

            try {
                InitialContext ic = new InitialContext();
                WorkManager manager  = (WorkManager) ic.lookup("java:comp/env/" + workManagerName);
                if (log.isDebugEnabled()) {
                    log.debug("Found WorkManager " + workManagerName);
                }
                manager.schedule(new WLConfigReloadWork(configProperties, this));
            } catch (NamingException e) {
                if (log.isErrorEnabled()) {
                    log.error("Cannot locate WorkManager by the name " + workManagerName, e);
                }
            } catch (IllegalArgumentException e) {
                if (log.isErrorEnabled()) {
                    log.error("Cannot reload configuration.", e);
                }
            } catch (WorkException e) {
                if (log.isErrorEnabled()) {
                    log.error("Cannot reload configuration.", e);
                }
            }
        }
    }

}
