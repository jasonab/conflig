package com.eharmony.configuration.worker;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.configuration.ConfigProperties;
import com.eharmony.configuration.ConfigReloader;
import com.eharmony.configuration.ConfigSource;
import com.eharmony.configuration.ConfigUtil;

public abstract class ConfigReloadWork {
	private static Log log = LogFactory.getLog(ConfigReloadWork.class.getName());
	private Log logRun = LogFactory.getLog(ConfigReloadWork.class.getName()
			+ ".run");
	private static Log logAllTestFilesPresent = LogFactory
			.getLog(ConfigReloadWork.class.getName() + ".allTestFilesPresent");
	private ConfigUtil configUtil;
	private ConfigProperties configProperties;
	private ConfigReloader configReloader;

	public ConfigReloadWork(ConfigProperties configProperties,
			ConfigReloader configReloader) {
		this.configProperties = configProperties;
		this.configReloader = configReloader;
		configUtil = ConfigUtil.getInstance();
	}
	
	public static void reloadLocale(ConfigProperties configProperties,
			Locale locale) {
        log.debug("Reloading for locale " + locale);

        if (!allTestFilesPresent(configProperties.getConfigSources())) {
			if (log.isInfoEnabled()) {
				log
						.info("Not all test file present, aborting configuration reload!!!");
			}
			return;
		}

		ConfigUtil configUtil = ConfigUtil.getInstance();
		ConcurrentMap<String, String> consolidatedProperties = configUtil
				.buildConsolidatedProperties(locale, configProperties
						.getConfigSources(),
						configProperties.getConfigScopes(), configProperties
								.isFailOnError());
        // do the swap even though it may be empty for the specific locale, so that
        // we do not reload for this locale again until next reloading cycle.
        configProperties.swap(consolidatedProperties, locale);
	}

	public void run() {
		Log log = logRun;
		boolean reloadInProgress = configProperties.isReloadInProgress();
		boolean reloadNecessary = configReloader.isReloadNecessary();
		if (reloadInProgress
				|| !reloadNecessary) {
			if (log.isDebugEnabled()) {
				log.debug("someone got here before us, don't do it, quit now.");
			}
			return;
		}

		if (!allTestFilesPresent(configProperties.getConfigSources())) {
			if (log.isInfoEnabled()) {
				log
						.info("Not all test file present, aborting configuration reload!!!");
			}
			return;
		}

		if (log.isDebugEnabled()) {
			log.debug("Refreshing all properties");
		}
		try {
			configProperties.setReloadInProgress(true);
			Map<Locale, ConcurrentMap<String, String>> localizedConsolidatedProperties = configUtil
					.buildLocalizedConsolidatedProperties(configProperties
							.getLocalizedConsolidatedProperties().keySet(), configProperties.getConfigSources(),
							configProperties.getConfigScopes(),
							configProperties.isFailOnError());
			for (Map.Entry<Locale, ConcurrentMap<String, String>> entry : localizedConsolidatedProperties
					.entrySet()) {
				ConcurrentMap<String, String> consolidated = entry.getValue();
				if (consolidated != null && consolidated.size() > 0) {
					configProperties.swap(consolidated, entry.getKey());
				}
			}
		} catch (Exception ex) {
			if (log.isDebugEnabled()) {
				log.debug(ex.getMessage(), ex);
			}
		} finally {
			configProperties.setReloadInProgress(false);
			configReloader.setLastReloadedAt(System.currentTimeMillis());
		}
		if (log.isDebugEnabled()) {
			log.debug("All properties refreshed");
		}		
	}

	public static boolean allTestFilesPresent(List<ConfigSource> sources) {
		Log log = logAllTestFilesPresent;
		boolean present = true;
        if (sources == null) {
            return false; // no ConfigSource cannot reload then.  This should never happen
        }
		for (ConfigSource source : sources) {
			if (ConfigSource.Type.FileSystem.equals(source.getType())) {
				String testFile = source.getTestFile();
				if (testFile != null) {
					File file = new File(testFile);
					if (!file.exists()) {
						present = false;
						if (log.isInfoEnabled()) {
							log
									.info("Test file does not exist.  Is NFS down?  Test file is : "
											+ testFile);
						}
						break;
					}
				}
			}
		}
		return present;
	}
}
