package com.eharmony.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eharmony.configuration.ConfigScope.ScopeType;

public class ConfigurationUtilTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	private ConfigUtil configUtil = ConfigUtil.getInstance();

	@Before
	public void setUp() throws Exception {
		
	}

	@After
	public void tearDown() throws Exception {
	}

    @Test
    public void testBuildFileNameWithScope() {
    	ConfigSource source =  new ConfigSource();
    	source.setName("myAppConfig");
    	source.setLocation("/");
    	
    	ConfigScope scope = new ConfigScope();
    	scope.setType(ScopeType.cluster);
    	String configKey = configUtil.buildFileNameWithScope(scope, source);
    	String suffix = ConfigProperties.DEFAULT_LOCALE.toString();
    	suffix = "".equals(suffix) ? suffix : "_" + suffix;
    	assertEquals("myAppConfig.cluster" + suffix, configKey);

    	scope.setInstanceName("fnd-dev01-cms-cluster");
    	configKey = configUtil.buildFileNameWithScope(scope, source);
    	assertEquals("myAppConfig.cluster.fnd-dev01-cms-cluster" + suffix, configKey);

    	scope.setPrefix("cluster-prefix");
    	configKey = configUtil.buildFileNameWithScope(scope, source);
    	assertEquals("myAppConfig.cluster-prefix.fnd-dev01-cms-cluster" + suffix, configKey);
    }

    @Test
    public void testBuildFullPath(){
    	String fileNameWithScope = "myAppConfig.host.dc1-host01";

    	// default to Classpath type and Properties format
    	ConfigSource source =  new ConfigSource(); 
    	source.setName("myAppConfig");

    	source.setLocation(null);
    	String fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("myAppConfig.host.dc1-host01.properties", fullPath);
    	
    	source.setLocation("");
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("myAppConfig.host.dc1-host01.properties", fullPath);
    	
    	source.setLocation("/");
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("myAppConfig.host.dc1-host01.properties", fullPath);

    	source.setLocation("\\");
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("myAppConfig.host.dc1-host01.properties", fullPath);

    	source.setLocation("/some/package");
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("some/package/myAppConfig.host.dc1-host01.properties", fullPath);

    	source.setLocation("/some/package/");
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("some/package/myAppConfig.host.dc1-host01.properties", fullPath);
    
    	source.setLocation("//\\some/package/");
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("some/package/myAppConfig.host.dc1-host01.properties", fullPath);

    	// test FileSystem path
    	String fileSeparator = File.separator;
    	source.setType(ConfigSource.Type.FileSystem);
    	source.setLocation(null);
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals(fileSeparator + "myAppConfig.host.dc1-host01.properties", fullPath);
    	
    	source.setLocation("");
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals(fileSeparator + "myAppConfig.host.dc1-host01.properties", fullPath);
    	
    	source.setLocation("/");  // non-windows file system root
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("/myAppConfig.host.dc1-host01.properties", fullPath);

    	source.setLocation("\\");  // windowz file system root
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("\\myAppConfig.host.dc1-host01.properties", fullPath);

    	source.setLocation("/some/folder/at/somewhere");
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("/some/folder/at/somewhere" + fileSeparator + "myAppConfig.host.dc1-host01.properties", fullPath);

    	source.setLocation("\\some\\folder\\at\\somewhere");
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("\\some\\folder\\at\\somewhere" + fileSeparator + "myAppConfig.host.dc1-host01.properties", fullPath);
    	
    	source.setLocation("/some/folder/at/somewhere/");
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("/some/folder/at/somewhere/myAppConfig.host.dc1-host01.properties", fullPath);
    	
    	source.setLocation("\\some\\folder\\at\\somewhere\\");
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("\\some\\folder\\at\\somewhere\\myAppConfig.host.dc1-host01.properties", fullPath);
    	
    	source.setFormat(ConfigSource.Format.XML);
    	fullPath = configUtil.buildFullPath(source, fileNameWithScope);
    	assertEquals("\\some\\folder\\at\\somewhere\\myAppConfig.host.dc1-host01.xml", fullPath);
    }
    @Test
    public void testConsolidateProperties() {
    	ConcurrentMap<String, String> consolidated = new ConcurrentHashMap<String, String>();
    	Properties properties = new Properties();
    	properties.setProperty("key_1", "value_1");
    	properties.setProperty("key_2", "value_2");
    	configUtil.consolidateProperties(consolidated, properties);
    	assertEquals("value_1", consolidated.get("key_1"));
    	assertEquals("value_2", consolidated.get("key_2"));

    	Properties properties2 = new Properties();
    	properties2.setProperty("key_1", "value_1_from_properties2");
    	configUtil.consolidateProperties(consolidated, properties2);
    	assertEquals("value_1_from_properties2", consolidated.get("key_1"));
    	assertEquals("value_2", consolidated.get("key_2"));
    	
    	Properties properties3 = new Properties();
    	properties3.setProperty("key_2", "value_2_from_properties3");
    	configUtil.consolidateProperties(consolidated, properties3);
    	assertEquals("value_1_from_properties2", consolidated.get("key_1"));
    	assertEquals("value_2_from_properties3", consolidated.get("key_2"));
    }

    @Test
    public void testGetInputStreamFromClasspath() throws IOException {
    	InputStream in = null; 
    	try {
    		in = configUtil
				.getInputStreamFromClasspath("sample/eh-config.properties");
	        // classpath location cannot start with a leading foward slash.
	        
	        assertTrue(in != null);
	        int i;
	        while ((i = in.read()) > 0) {
	        	System.out.write(i);
	        }
    	}
    	finally {
    		if (in != null) {
    			in.close();
    		}
    	}
	}

    @Test
    public void testGetInputStreamFromFileSystem() throws IOException {
        String userHome = System.getProperty("user.home");
    	PrintWriter out = null;
    	try {
    		out = new PrintWriter(new FileOutputStream(userHome + "/safeToDelete.txt"));
        	out.write("This file is safe to delete\n");
    	}
    	finally {
    		if (out != null) {
    			out.close();
    		}
    	}
    	
    	InputStream in = null;
    	try {
    		in = configUtil.getInputStreamFromFileSystem(userHome + "/safeToDelete.txt");
	        assertTrue(in != null);
	        // in debug mode I can see the file is being read fine, however the following
	        // code does not print the content out in console.  ????
	        int i;
	        while ((i = in.read()) > 0) {
	        	System.out.write(i);
	        }
		}
		finally {
			if (in != null) {
				in.close();
			}
		}
    }
}
