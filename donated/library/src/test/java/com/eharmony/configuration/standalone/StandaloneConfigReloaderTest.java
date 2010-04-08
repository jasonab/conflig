package com.eharmony.configuration.standalone;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.eharmony.configuration.ConfigProperties;
import com.eharmony.configuration.ConfigScope;
import com.eharmony.configuration.ConfigSource;
import com.eharmony.configuration.ConfigUtil;
import com.eharmony.configuration.reloader.standalone.StandaloneConfigReloader;

public class StandaloneConfigReloaderTest {
	private final Log log = LogFactory.getLog(this.getClass());
	
	final static int SAMPLES_COUNT = 55;
	
	final static String SYS_TMP = System.getProperty("java.io.tmpdir");
	final static String location = SYS_TMP + File.separator
			+ StandaloneConfigReloaderTest.class.getCanonicalName();
	

	List<ConfigSample> samples = new LinkedList<ConfigSample>();

	class ConfigSample {
		static final String PROPERTY_KEY = "property";
		static final String PROPERTY_VAL = "value_from_";
		final ConfigScope.ScopeType[] scopeTypes = ConfigScope.ScopeType
				.values();

		String name;
		int propertiesCount = scopeTypes.length;
		int sourceCount = scopeTypes.length;

		List<ConfigScope> scopes = new LinkedList<ConfigScope>();
		List<ConfigSource> sources = new LinkedList<ConfigSource>();

		ConfigProperties properties = new ConfigProperties();

		public ConfigSample(String name) {
			super();
			this.name = name;
			properties.setConfigScopes(scopes);
			properties.setConfigSources(sources);
		}

		private void buildScopes() {

			for (int i = scopeTypes.length - 1; i >= 0; i--) {
				ConfigScope scope = new ConfigScope();
				ConfigScope.ScopeType scopeType = scopeTypes[i];
				scope.setType(scopeType);
				scope.setPrefix(scopeType.toString());
				scopes.add(scope);
			}
		}

		private void buildSources() {
			for (int i = 1; i <= sourceCount; i++) {
				ConfigSource source = new ConfigSource();
				source.setName(name + "_source" + i);
				source.setFileName(name + "_source" + i);
				source.setType(ConfigSource.Type.FileSystem);
				source.setLocation(location + File.separator + name);
				source.setFormat(ConfigSource.Format.Properties);
				sources.add(source);
			}
		}

		void buildConfig() throws IOException {
			buildScopes();
			buildSources();
			ConfigUtil cu = ConfigUtil.getInstance();
			for (ConfigSource source : sources) {
				int scopeLevel = 1;
				for (ConfigScope scope : scopes) {
					Properties properties = new Properties();
					for (int i = scopeLevel; i <= scopes.size(); i++) {
						properties.put(PROPERTY_KEY + i, PROPERTY_VAL
								+ scope.getType());
					}
					scopeLevel++;

					new File(source.getLocation()).mkdirs();

					String filePath = cu.buildFullPath(source, cu
							.buildFileNameWithScope(scope, source));

					File sourceFile = new File(filePath);
					sourceFile.createNewFile();

					FileOutputStream fos = new FileOutputStream(sourceFile);
					properties.store(fos, "");
					fos.close();
				}
			}
		}

	}

	@Before
	public void buildConfigSamples() throws IOException {
		for (int i = 1; i <= SAMPLES_COUNT; i++) {
			ConfigSample sample = new ConfigSample("sample" + i);
			sample.buildConfig();
			samples.add(sample);
		}
	}

	@After
	public void clean() {
		ConfigUtil cu = ConfigUtil.getInstance();
		for (ConfigSample sample : samples) {
			for (ConfigSource source : sample.sources) {
				for (ConfigScope scope : sample.scopes) {
					String filePath = cu.buildFullPath(source, cu
							.buildFileNameWithScope(scope, source));
					new File(filePath).delete();
				}
			}
			new File(location + File.separator + sample.name).delete();
		}
		new File(location).delete();
	}

	void check() {		
		if(log.isTraceEnabled()) {
			log.trace("Strarted");
		}
		for (ConfigSample sample : samples) {
			for (int i = 1; i <= sample.propertiesCount; i++) {
				String expected = sample.properties.getProperty(ConfigSample.PROPERTY_KEY + i);
				String val = (ConfigSample.PROPERTY_VAL + sample.scopeTypes[sample.scopeTypes.length
				                            								- i].toString());
				
				Assert.assertTrue(val + " not equals to " + expected, val.equals(expected));
			}
		}
		if(log.isTraceEnabled()) {
			log.trace("Finished");
		}
	}

	@Test
	public void test() throws InterruptedException {
		StandaloneConfigReloader reloader = new StandaloneConfigReloader();
		reloader.setReloadInterval(-1);
		AtomicInteger notifier = new AtomicInteger(0);
		for (ConfigSample sample : samples) {
			reloader.reload(sample.properties, notifier, true);
		}

		synchronized (notifier) {
			while (notifier.get() < samples.size()) {
				notifier.wait();
			}
		}
		if(log.isTraceEnabled()) {
			log.trace("Finished " + notifier.get() + " workers");
		}
		check();
	}

}
