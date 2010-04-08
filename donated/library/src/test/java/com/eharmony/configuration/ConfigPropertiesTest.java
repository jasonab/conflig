package com.eharmony.configuration;

import org.junit.*;
import static org.junit.Assert.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * User: fwu
 */
public class ConfigPropertiesTest {
    private static ConfigProperties configProperties = new ConfigProperties();
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        ConcurrentMap<String, String> consolidated = new ConcurrentHashMap<String, String>();
        consolidated.put("goodInteger123", "123");
        consolidated.put("goodFloat1.23", "1.23");
        consolidated.put("spaces", " ");
        consolidated.put("empty", " ");
        consolidated.put("tooBig", "12345678901234567890");
        consolidated.put("alpha", "a12345");
        configProperties.setConsolidatedProperties(consolidated);
        configProperties.setConfigSources(new ArrayList<ConfigSource>());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        configProperties = null;
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testFromCsvToList() {
        String csv = " 1, 2,3, 4,  ,5 ,6 , 7, , 8 9 ,  ";
        List<String> result = configProperties.fromCsvToList(csv, ",");
        List<String> expected = new ArrayList<String>();
        expected.add("1");
        expected.add("2");
        expected.add("3");
        expected.add("4");
        expected.add("5");
        expected.add("6");
        expected.add("7");
        expected.add("8 9");
        assertEquals(expected.toString(), result.toString());

        csv = "";
        result = configProperties.fromCsvToList(csv, ",");
        assertTrue(result.size() == 0);

        // Using | as delimiter
        csv = "1,2:3,4:5,6:";
        result = configProperties.fromCsvToList(csv, ":");
        expected.clear();
        expected.add("1,2");
        expected.add("3,4");
        expected.add("5,6");
        assertEquals(expected.toString(), result.toString());
    }

    @Test
    public void testFromCsvToMap() {
        String csv = " 1:v1, 2 :v2,3: v3, 4 :v4 ,  ,5: v5 ,6 : v6 , 7:, , 8 9 : v8 9,  :  ,";
        Map<String , String> result = configProperties.fromCsvToMap(csv, ",", ":");
        Map<String, String> expected = new HashMap<String, String>();
        expected.put("1", "v1");
        expected.put("2", "v2");
        expected.put("3", "v3");
        expected.put("4", "v4");
        expected.put("5", "v5");
        expected.put("6", "v6");
        expected.put("7", null);
        expected.put("8 9", "v8 9");
        assertEquals(expected.toString(), result.toString());

        csv = "";
        result = configProperties.fromCsvToMap(csv, ",", ":");
        assertTrue(result.size() == 0);

        // test custom delimiters, # as list delimiter, ~ as key value delimiter
        csv = " 1~v1# 2 ~v2#3~ v3# 4 ~v4 #  #5~ v5 #6 ~ v6 # 7~# # 8 9 ~ v8 9#  ~  #";
        result.clear();
        result = configProperties.fromCsvToMap(csv, "#", "~");
        assertEquals(expected.toString(), result.toString());
        
    }

    @Test
    public void testGetPropertyInteger() {

        assertEquals(123, configProperties.getPropertyInteger("goodInteger123"));

        try {
            assertEquals(1, configProperties.getPropertyInteger("spaces"));
            fail("Should have thrown NumberFormatException");
        }
        catch (NumberFormatException e) {
            // do nothing
        }
        try {
            assertEquals(1, configProperties.getPropertyInteger("empty"));
            fail("Should have thrown NumberFormatException");
        }
        catch (NumberFormatException e) {
            // do nothing
        }
        try {
            assertEquals(1, configProperties.getPropertyInteger("tooBig"));
            fail("Should have thrown NumberFormatException");
        }
        catch (NumberFormatException e) {
            // do nothing
        }
        try {
            assertEquals(1, configProperties.getPropertyInteger("alpha"));
            fail("Should have thrown NumberFormatException");
        }
        catch (NumberFormatException e) {
            // do nothing
        }
    }

    @Test
    public void testGetCandidateLocales() {
        Locale emptyLang = new Locale("");
        Locale emptyLandAndCountry = new Locale("", "");
        assertTrue(emptyLandAndCountry.equals(emptyLandAndCountry));

        List<Locale> candidates = configProperties.getCandidateLocales(emptyLandAndCountry);
        assertTrue(candidates.size() == 0);

    }

    @Test
    public void testGetLocalesToLookUp() {
        Locale emptyLang = new Locale("");

        List<Locale> candidates = configProperties.getLocalesToLookUp(emptyLang);
        assertTrue(candidates.contains(emptyLang));

        candidates = configProperties.getLocalesToLookUp(null);
        assertTrue(candidates.contains(emptyLang));
    }

    @Test
    public void testCanBeIntepretedAsBoolean() {
        assertTrue(!configProperties.canBeIntepretedAsTrue(null));

        assertTrue(configProperties.canBeIntepretedAsTrue("TRUE"));
        assertTrue(configProperties.canBeIntepretedAsTrue("t"));
        assertTrue(configProperties.canBeIntepretedAsTrue("YeS"));
        assertTrue(configProperties.canBeIntepretedAsTrue("Y"));
        assertTrue(configProperties.canBeIntepretedAsTrue("oN"));
        assertTrue(configProperties.canBeIntepretedAsTrue("1"));

        assertTrue(!configProperties.canBeIntepretedAsTrue(""));
    }

    @Test
    public void testGetProeprtyWithDefaultValue() {
        Integer defaultValue = new Integer(123);
        assertEquals(123, configProperties.getPropertyInteger("goodInteger123"));
        assertEquals(defaultValue, configProperties.getPropertyInteger("badkey", defaultValue));
        assertEquals(defaultValue, configProperties.getPropertyInteger("tooBig", defaultValue));
        assertEquals(defaultValue, configProperties.getPropertyInteger("empty", defaultValue));
        assertEquals(defaultValue, configProperties.getPropertyInteger("alpha", defaultValue));

        Float defaultFloat = 5.234F;
        assertEquals(1.23F, configProperties.getPropertyFloat("goodFloat1.23", defaultFloat));
        assertEquals(defaultFloat, configProperties.getPropertyFloat("badKey", defaultFloat));
        assertEquals(defaultFloat, configProperties.getPropertyFloat("empty", defaultFloat));
        assertEquals(defaultFloat, configProperties.getPropertyFloat("spaces", defaultFloat));
        assertEquals(defaultFloat, configProperties.getPropertyFloat("alpha", defaultFloat));
    }

}