package com.eharmony.configuration.weblogic;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.eharmony.configuration.ConfigProperties;
import com.eharmony.configuration.ConfigSource;
import com.eharmony.configuration.reloader.weblogic.WLConfigReloader;
import com.eharmony.configuration.worker.weblogic.WLConfigReloadWork;

public class ConfigReloadWorkTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

    @Before
    public void setUp() throws Exception {
    }

	@After
	public void tearDown() throws Exception {
	}


    @Test
    public void testAllTestFilesPresent() throws IOException {
        List<ConfigSource> sources = new ArrayList<ConfigSource>();
        ConfigSource source = new ConfigSource();
        source.setName("umessage");
        source.setType(ConfigSource.Type.Classpath);
        sources.add(source);

        String userHome = System.getProperty("user.home");

        String testFile = userHome + "/doNotDelete.txt";
        source = new ConfigSource();
        source.setName("overrides");
        source.setType(ConfigSource.Type.FileSystem);
        source.setTestFile(testFile);
        sources.add(source);

        String testFile2 =userHome + "/doNotDeleteEither.txt"; 
        source = new ConfigSource();
        source.setName("umessage-xml");
        source.setType(ConfigSource.Type.FileSystem);
        source.setTestFile(testFile2);
        sources.add(source);

        // now set up the test files
        File file1 = new File(testFile);
        file1.createNewFile();

        File file2 = new File(testFile2);
        file2.createNewFile();

        WLConfigReloadWork work = new WLConfigReloadWork(new ConfigProperties(), new WLConfigReloader());


        assertTrue(work.allTestFilesPresent(sources));

        // now delete the test file1
        file1.delete();
        assertFalse(work.allTestFilesPresent(sources));
    }
}
