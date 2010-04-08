package com.eharmony.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.net.URL;
import java.net.MalformedURLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigManagerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private ConfigManagerImpl configManager;

    @Before
	public void setUp() throws Exception {
		configManager = new ConfigManagerImpl();
        configManager.setConfigUtil(ConfigUtil.getInstance());
	}

	@After
	public void tearDown() throws Exception {
        configManager = null;
	}


    @Test
    public void testBuildConfigScopes() {
    	// server, environment, cluster are determined by -D properties.
    	System.setProperty("weblogic.Name", "wlserver-localhost");
    	System.setProperty("environment", "production");
    	
    	Properties properties = new Properties();
    	
    	properties.setProperty("scope.order", "server,cluster,environment,global");

		properties.setProperty("scope.global.prefix", "global_prefix");
		properties.setProperty("scope.environment.prefix", "environment_prefix");
		properties.setProperty("scope.cluster.prefix", "cluster_prefix");
		properties.setProperty("scope.server.prefix", "server_prefix");
		
		List<ConfigScope> scopes = configManager.buildConfigScopes(properties);
		
		assertEquals(3, scopes.size());
		
		ConfigScope scope = scopes.get(0);
		assertEquals(ConfigScope.ScopeType.global, scope.getType());
		assertNull(scope.getInstanceName());
		assertEquals("global_prefix", scope.getPrefix());

		scope = scopes.get(1);
		assertEquals(ConfigScope.ScopeType.environment , scope.getType());
		assertEquals("production", scope.getInstanceName());
		assertEquals("environment_prefix", scope.getPrefix());

		scope = scopes.get(2);
		assertEquals(ConfigScope.ScopeType.server , scope.getType());
		assertEquals("wlserver-localhost", scope.getInstanceName());
		assertEquals("server_prefix", scope.getPrefix());

    	System.clearProperty("weblogic.Name");
    	System.clearProperty("environment");
    }

    @Test
    public void testBuildConfigSources() {
    	Properties properties = new Properties();
    	
    	properties.setProperty("config.order", "umessage, umessage-xml,overrides");
    	
    	properties.setProperty("nfig.umessage.type", "Classpath");
		properties.setProperty("config.umessage.location", "sample");
		properties.setProperty("config.umessage.format", "Properties");

		properties.setProperty("config.overrides.type", "FileSystem");
		properties.setProperty("config.overrides.location", "/data/deployment/umessage/config");
		properties.setProperty("config.overrides.name", "umessage");
        properties.setProperty("config.overrides.testFile", "/data/deployment/umessage/doNotDelete.txt");

    	properties.setProperty("config.umessage-xml.type", "Classpath");
		properties.setProperty("config.umessage-xml.location", "/sample");
		properties.setProperty("config.umessage-xml.format", "XML");
		properties.setProperty("config.umessage-xml.name", "umessage");
        
        List<ConfigSource> sources = configManager.buildConfigSources(properties);
		assertEquals(3, sources.size());
		ConfigSource source = sources.get(0);
		assertEquals("umessage", source.getName());
		assertEquals("umessage", source.getFileName());
		assertEquals("sample", source.getLocation());
		assertEquals(ConfigSource.Type.Classpath, source.getType());
		assertEquals(ConfigSource.Format.Properties, source.getFormat());
		
		source = sources.get(1);
		assertEquals("umessage-xml", source.getName());
		assertEquals("umessage", source.getFileName());
		assertEquals("/sample", source.getLocation());
		assertEquals(ConfigSource.Type.Classpath, source.getType());
		assertEquals(ConfigSource.Format.XML, source.getFormat());

		source = sources.get(2);
		assertEquals("overrides", source.getName());
		assertEquals("umessage", source.getFileName());
		assertEquals("/data/deployment/umessage/config", source.getLocation());
		assertEquals(ConfigSource.Type.FileSystem, source.getType());
		assertEquals(ConfigSource.Format.Properties, source.getFormat());
        assertEquals("/data/deployment/umessage/doNotDelete.txt", source.getTestFile());
    }

    @Test
    public void testBuildConfigProperties() {
    	// server, environment, cluster are determined by -D properties.
    	setUpSystemProperties();
    	Configuration configProperties = configManager.buildConfigProperties();

        checkSampleProperties(configProperties);
                
        // clean up system properties
    	clearSystemProperties();
    }
    
    @Test
    public void testMuleBuildConfigProperties() {
		System.setProperty("mule.serverId", "server1onHost1");
    	System.setProperty("environment", "prod");
    	System.setProperty("mule.clusterId", "cluster1withEmptyFile");
    	System.setProperty("config.properties", "classpath://sample/eh-config.properties");
    	
    	Configuration configProperties = configManager.buildConfigProperties();
        checkSampleProperties(configProperties);
    	
    	System.clearProperty("mule.serverId");
    	System.clearProperty("environment");
    	System.clearProperty("mule.clusterId");
    	System.clearProperty("config.properties");
    }
    
    @Test
    public void testSingletonBuildConfigProperties() {
    	// server, environment, cluster are determined by -D properties.
    	setUpSystemProperties();
    	Configuration configProperties = SingletonConfigManagerImpl.getInstance().buildConfigProperties();

        checkSampleProperties(configProperties);
                
        Configuration configProperties2 = SingletonConfigManagerImpl.getInstance().buildConfigProperties();
        assertTrue(configProperties == configProperties2);
        // clean up system properties
    	clearSystemProperties();
    }

	private void clearSystemProperties() {
		System.clearProperty("weblogic.Name");
    	System.clearProperty("environment");
    	System.clearProperty("cluster");
    	System.clearProperty("config.properties");
	}

	private void setUpSystemProperties() {
		System.setProperty("weblogic.Name", "server1onHost1");
    	System.setProperty("environment", "prod");
    	System.setProperty("cluster", "cluster1withEmptyFile");
    	System.setProperty("config.properties", "classpath://sample/eh-config.properties");
	}

	private void checkSampleProperties(Configuration configProperties) {
		// testing list(PrintStream out); 
        configProperties.list(System.out);

        assertEquals("sample 3 from global", configProperties.getProperty("sampleProperty.no.override"));
        assertEquals("sample 1 from environment prod", configProperties.getProperty("sampleProperty.override.by.environment"));
        assertEquals("sample 2 from server server1OnHost1", configProperties.getProperty("sampleProperty.override.by.server"));
        assertEquals("server 1 host 1 xml", configProperties.getProperty("sampleProperty.from.xml"));
        
        assertEquals("default", configProperties.getProperty("non.existent.key", "default"));

        ConfigProperties config = (ConfigProperties) configProperties; 
        assertFalse(config.isFailOnError());
        assertTrue(config.isEnableReload());
        assertEquals(60000, config.getReloader().getReloadInterval());
        assertEquals(DoNothingConfigReloader.class, config.getReloader().getClass());
        
        // testing propertyNames();
        Set<String> names = configProperties.propertyNames();
        assertTrue(names.contains("weblogic.Name"));
        assertTrue(names.contains("sampleProperty.no.override"));
    }

    @Test
    public void testUrlWithClasspathColonSlashSlash() throws MalformedURLException {
        // can we create URL with classpath://
        String path = "classpath://com/eharmony/foundation.configuration/eh-config.properties";
        try {
            new URL(path);
            fail("should have thrown MalformedURLException");
        }
        catch (MalformedURLException e){
            // do nothing
        }

    }
}
