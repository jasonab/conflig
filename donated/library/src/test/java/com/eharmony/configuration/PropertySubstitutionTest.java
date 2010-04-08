package com.eharmony.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class PropertySubstitutionTest {
	static Map<String, String> map = new HashMap<String, String>();
	static {
		map.put("key1", "1");
		map.put("key2", "2");
		map.put("key3", "0${key1}0${key2}0");
		map.put("key4", ConfigUtil.PropertySubstitution.escape("${key1}${key2}"));
        map.put("key5-1", "${key1}");
        map.put("key5-3", "${key3}");
        map.put("key6", "${key1}${key3}");
        map.put("key7-1", "${key1}-${key3}-${key5-1}");
        map.put("key7-3", "${key1}-${key3}-${key5-3}");
        map.put("key8", "${key1}-${key3}-${key6}");
    }

	void checkForeCycle() {
		map.clear();
		map.put("key1", "1");
		map.put("key2", "${key1}${key4}");
		map.put("key3", "${key1}${key2}");
		map.put("key4", "${key3}");
		RuntimeException cyclePresentEx = null;
		try {
			ConfigUtil.PropertySubstitution.substitute(map);
		} catch (RuntimeException ex) {
			cyclePresentEx = ex;
		}
		assertNotNull(cyclePresentEx);
	}
	
	@Test
	public void test() throws FileNotFoundException, IOException {
		ConfigUtil.PropertySubstitution.substitute(map);
		assertEquals("1", map.get("key1"));
		assertEquals("2", map.get("key2"));
		assertEquals("01020", map.get("key3"));
		assertEquals("${key1}${key2}", map.get("key4"));
        assertEquals("1", map.get("key5-1"));
        assertEquals("01020", map.get("key5-3"));
        assertEquals("101020", map.get("key6"));

        assertEquals("1-01020-1", map.get("key7-1"));
        assertEquals("1-01020-01020", map.get("key7-3"));

        assertEquals("1-01020-101020", map.get("key8"));
        
        checkForeCycle();
    }

}
