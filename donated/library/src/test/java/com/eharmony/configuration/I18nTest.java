package com.eharmony.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import junit.framework.Assert;
import static junit.framework.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.eharmony.configuration.standalone.StandaloneConfigReloaderTest;

public class I18nTest {
	final static String LOCATION = System.getProperty("java.io.tmpdir")
			+ File.separator
			+ I18nTest.class.getCanonicalName();

	final static String PROPRETIES_ROOT = "root.properties";
	final static String PROPRETIES_DEFAULT = "configTest.global.properties";
	final static String PROPRETIES_FRFR = "configTest.global_fr_FR.properties";
	final static String PROPRETIES_FRCA = "configTest.global_fr_CA.properties";

	final static Locale LOCALE_DEFAULT = ConfigProperties.DEFAULT_LOCALE;
	final static Locale LOCALE_FRFR = new Locale("fr", "FR");
	final static Locale LOCALE_FRCA = new Locale("fr", "CA");
    final static Locale EMPTY_LOCALE = new Locale("", "");

	ConfigProperties config;

	void buildRootProperies() throws IOException {
		new File(LOCATION).mkdirs();
		File rootConfigFile = new File(LOCATION + File.separator
				+ PROPRETIES_ROOT);

		Properties properties = new Properties();
		properties.put("failOnError", "false");
		properties.put("scope.order", "global");
		properties.put("scope.global.prefix", "global");
		properties.put("config.order", "configTest");
		properties.put("config.configTest.type", "FileSystem");
		properties.put("config.configTest.location", LOCATION);
		properties.put("config.configTest.name", "configTest");
        properties.put("enable.reload", "true");
        properties.put("reload.class", "com.eharmony.configuration.reloader.standalone.StandaloneConfigReloader");
        properties.put("reload.interval", "300000");

		properties.store(new FileOutputStream(rootConfigFile),
				"root config sample");
	}
	
	void addConfig(String fileName, String key, String val) throws FileNotFoundException, IOException {
		File propFile = new File(LOCATION + File.separator + fileName);
		Properties properties = new Properties();
		if(propFile.exists()) {
			properties.load(new FileInputStream(propFile));
		}		
		properties.put(key, val);
		properties.store(new FileOutputStream(propFile), "");
		
	}

	@Before
	public void buildProperties() throws IOException {
		buildRootProperies();
		
		addConfig(PROPRETIES_DEFAULT, "locale", "default");
		addConfig(PROPRETIES_DEFAULT, "key_default", "val_default");
		addConfig(PROPRETIES_FRFR, "locale", LOCALE_FRFR.toString());

		config = (ConfigProperties) SingletonConfigManagerImpl
		.getInstance()
		.buildConfigProperties(
				"file://" + LOCATION + File.separator + PROPRETIES_ROOT);

	}	
	
	@Test
	public void test() throws IOException {
        assertEquals("default", config.getProperty("locale"));
		assertEquals(LOCALE_FRFR.toString(), config.getProperty("locale", LOCALE_FRFR));

		assertEquals("default", config.getProperty("locale", LOCALE_FRCA ));

		addConfig(PROPRETIES_FRCA, "locale", LOCALE_FRCA.toString());
        /* since we are caching locale specific properties, we need to manually call
         AbstractConfigReloader.reloadLocale() here as we just dynamically added a
         new property file for the lcoale LOCALE_FRCA
         */
        ((AbstractConfigReloader) config.getReloader()).reloadLocale(config, LOCALE_FRCA);

        assertEquals(LOCALE_FRCA.toString(), config.getProperty("locale", LOCALE_FRCA));

		assertEquals(config.getProperty("key_default"), config.getProperty("key_default", LOCALE_FRCA));

        // default locale, in this case en_US, will get "default"
        assertEquals("default", config.getProperty("locale", Locale.US));

        // empty locale should get "default"
        assertEquals("default", config.getProperty("locale", new Locale("", "")));
	}

	@After
	public void clean() {
		new File((LOCATION + File.separator + PROPRETIES_ROOT)).delete();
		new File((LOCATION + File.separator + PROPRETIES_DEFAULT)).delete();
		new File((LOCATION + File.separator + PROPRETIES_FRFR)).delete();
		new File((LOCATION + File.separator + PROPRETIES_FRCA)).delete();
		new File(LOCATION).delete();
	}

}
