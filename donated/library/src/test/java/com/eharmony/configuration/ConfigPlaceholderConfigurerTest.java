package com.eharmony.configuration;

import org.junit.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ConfigPlaceholderConfigurerTest {
    private static ApplicationContext context;

    @BeforeClass
	public static void setUpBeforeClass() throws Exception {
        System.setProperty("environment", "local");
	    context = new ClassPathXmlApplicationContext("configurer/application-context-test.xml");
    }

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
        System.clearProperty("environment");
        context = null;
	}

    @Test
    public void testConfigPlaceholderConfigurer() {
        Foo foo = (Foo) context.getBean("foo");
        assertEquals("bar", foo.getFoo());
        assertEquals(1001, foo.getCount());
        assertTrue(foo.isHappy());
    }
}
