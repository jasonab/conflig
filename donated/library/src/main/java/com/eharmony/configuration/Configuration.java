package com.eharmony.configuration;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Map;

public interface Configuration {

    /**
     * @param key           key for which to get property value.
     * @param defaultValue  default value if no such key is found.
     * @param locale property locale.
     * @return              value for <code>key</code>, <code>defaultValue</code> is returned if <code>key</code> is not found.
     */
    String getProperty(String key, String defaultValue, Locale locale);

    /**
     * @param key           key for which to get property value.
     * @param defaultValue  default value if no such key is found.
     * @return              value for <code>key</code>, <code>defaultValue</code> is returned if <code>key</code> is not found.
     */
    String getProperty(String key, String defaultValue);

    /**
     * @param key   key for which to get property value.
     * @param locale property locale.
     * @return      value for <code>key</code>, null is returned if <code>key</code> is not found.
     */
    String getProperty(String key, Locale locale);
    
    /**
     * @param key   key for which to get property value.
     * @return      value for <code>key</code>, null is returned if <code>key</code> is not found.
     */
    String getProperty(String key);

    /**
     * Returns property as Integer.  Throws NumberFormatException if the value cannot be intepreted as an Integer.
     * @param key   key for which to get an Integer value
     * @param locale property locale.
     * @throws NumberFormatException
     */
    Integer getPropertyInteger(String key, Locale locale) throws NumberFormatException;

    /**
     * Returns property as Integer.  Throws NumberFormatException if the value cannot be intepreted as an Integer.
     * @param key   key for which to get an Integer value
     * @throws NumberFormatException
     */
    Integer getPropertyInteger(String key) throws NumberFormatException;

    /**
     * Returns property as Integer.  This method will swallow exception and return defaultValue is necessary.
     * @param key   key for which to get an Integer value
     * @param defaultValue
     * @return
     */
    Integer getPropertyInteger(String key, Integer defaultValue);

    /**
     * Returns property as Integer.  This method will swallow exception and return defaultValue is necessary.
     * @param key   key for which to get an Integer value
     * @param defaultValue
     * @param locale property locale.
     * @return
     */
    Integer getPropertyInteger(String key, Integer defaultValue, Locale locale);

    /**
     * Returns property as Long.  Throws NumberFormateException if the value cannot be intepreted as a Long.
     * @param key   key for which to get an Long value
     * @param locale property locale.
     * @throws NumberFormatException
     */
    Long getPropertyLong(String key, Locale locale);
    
    /**
     * Returns property as Long.  Throws NumberFormateException if the value cannot be intepreted as a Long.
     * @param key   key for which to get an Long value
     * @throws NumberFormatException
     */
    Long getPropertyLong(String key);

    /**
     * Returns property as Long.  This method will swallow exception and return defaultValue is necessary.
     * @param key   key for which to get an Long value
     * @param locale property locale.
     */
    Long getPropertyLong(String key, Long defaultValue, Locale locale);

    /**
     * Returns property as Long.  This method will swallow exception and return defaultValue is necessary.
     * @param key   key for which to get an Long value
     * @param defaultValue
     * @return
     */
    Long getPropertyLong(String key, Long defaultValue);

    /**
     * Returns property as Float.  Throws NumberFormatException if the value cannot be intepreted as a Float.
     * @param key   key for which to get an Float value
     * @param locale property locale.
     * @throws NumberFormatException
     */
    Float getPropertyFloat(String key, Locale locale) throws NumberFormatException;
    
    /**
     * Returns property as Float.  Throws NumberFormatException if the value cannot be intepreted as a Float.
     * @param key   key for which to get an Float value
     * @throws NumberFormatException
     */
    Float getPropertyFloat(String key) throws NumberFormatException;

    /**
     * Returns property as Float.  This method will swallow exception and return defaultValue is necessary.
     * @param key   key for which to get an Float value
     * @param defaultValue
     * @param locale property locale.
     * @return
     */
    Float getPropertyFloat(String key, Float defaultValue, Locale locale);

    /**
     * Returns property as Float.  This method will swallow exception and return defaultValue is necessary.
     * @param key   key for which to get an Float value
     * @throws NumberFormatException
     */
    Float getPropertyFloat(String key, Float defaultValue);

    /**
     * Returns property as Double.  Throws NumberFormateException if the value cannot be intepreted as a Double.
     * @param key   key for which to get an Double value
     * @param locale property locale.
     * @throws NumberFormatException
     */
    Double getPropertyDouble(String key, Locale locale);
    
    /**
     * Returns property as Double.  Throws NumberFormateException if the value cannot be intepreted as a Double.
     * @param key   key for which to get an Double value
     * @throws NumberFormatException
     */
    Double getPropertyDouble(String key);

    /**
     * Returns property as Double.  This method will swallow exception and return defaultValue is necessary.
     * @param key   key for which to get an Double value
     * @param locale property locale.
     */
    Double getPropertyDouble(String key, Double defaultValue, Locale locale);

    /**
     * Returns property as Double.  This method will swallow exception and return defaultValue is necessary.
     * @param key   key for which to get an Double value
     */
    Double getPropertyDouble(String key, Double defaultValue);

    /**
     * Returns a list when a property is configured as  comma separated values, i.e., key=value1, value2, value3...
     * Will ignore leading spaces, trailing spaces, empty spaces, trailing commas.
     * @param key   key for which to get a list of strings
     * @param locale property locale.
     * @return      A list of string values, or null if no csv is specified for key
     */
    List<String> getPropertyList(String key, Locale locale);
    
    /**
     * Returns a list when a property is configured as  comma separated values, i.e., key=value1, value2, value3...
     * Will ignore leading spaces, trailing spaces, empty spaces, trailing commas.
     * @param key   key for which to get a list of strings
     * @return      A list of string values, or null if no csv is specified for key
     */
    List<String> getPropertyList(String key);
    
    /**
     * Returns a list when a property is configured as  <code>delimiter</code> separated values, i.e., key=value1, value2, value3...
     * Will ignore leading spaces, trailing spaces, empty spaces, trailing <code>delimiter</code>.
     * <p/>
     * String.split() is used therefore the delimiter is used as regular expression.  Choose your delimiter carefully, e.g., don't use one that have
     * special meeanings in regular experssion.  colon(:), pound(#), comma(,), tilda(~) are good ones to choose from.
     * @param key   key for which to get a list of strings
     * @param delimiter The delimter to use to split the string into a list.
     * @param locale property locale.
     * @return      A list of string values, or null if no csv is specified for key
     */
    List<String> getPropertyList(String key, String delimiter, Locale locale);

    /**
     * Returns a list when a property is configured as  <code>delimiter</code> separated values, i.e., key=value1, value2, value3...
     * Will ignore leading spaces, trailing spaces, empty spaces, trailing <code>delimiter</code>.
     * <p/>
     * String.split() is used therefore the delimiter is used as regular expression.  Choose your delimiter carefully, e.g., don't use one that have
     * special meeanings in regular experssion.  colon(:), pound(#), comma(,), tilda(~) are good ones to choose from.
     * @param key   key for which to get a list of strings
     * @param delimiter The delimter to use to split the string into a list.
     * @return      A list of string values, or null if no csv is specified for key
     */
    List<String> getPropertyList(String key, String delimiter);

    /**
     * Returns a Map<String, String> when a property is configured as comma separated key:value pairs,
     * i.e., key=key1:value1,key2:value2:key3:value3...
     * Will ignore leading spaces, trailing spaces for key names.  empty spaces for value will result in a null value for key.
     * @param key
     * @param locale property locale.
     * @return  The configured map, or null if no key:values are configured for key.
     */
    Map<String, String> getPropertyMap(String key, Locale locale);
    
    /**
     * Returns a Map<String, String> when a property is configured as comma separated key:value pairs,
     * i.e., key=key1:value1,key2:value2:key3:value3...
     * Will ignore leading spaces, trailing spaces for key names.  empty spaces for value will result in a null value for key.
     * @param key
     * @return  The configured map, or null if no key:values are configured for key.
     */
    Map<String, String> getPropertyMap(String key);

    /**
     * Returns a Map<String, String> when a property is configured as <code>delimiter</code> separated key<code>keyValueDelimiter</code>value pairs,
     * i.e., key=key1:value1,key2:value2:key3:value3...
     * Will ignore leading spaces, trailing spaces for key names.  empty spaces for value will result in a null value for key.
     * <p/>
     * String.split() is used therefore the delimiter is used as regular expression.  Choose your delimiter carefully, e.g., don't use one that have
     * special meeanings in regular experssion.  colon(:), pound(#), comma(,), tilda(~) are good ones to choose from.
     * @param key
     * @param delimiter The delimiter to use to split the string property into key value pairs.
     * @param keyValueDelimiter The delimter to use to split the key value pairs into key and value.
     * @param locale property locale.
     * @return  The configured map, or null if no key:values are configured for key.
     */
    Map<String, String> getPropertyMap(String key, String delimiter, String keyValueDelimiter, Locale locale);
    
    /**
     * Returns a Map<String, String> when a property is configured as <code>delimiter</code> separated key<code>keyValueDelimiter</code>value pairs,
     * i.e., key=key1:value1,key2:value2:key3:value3...
     * Will ignore leading spaces, trailing spaces for key names.  empty spaces for value will result in a null value for key.
     * <p/>
     * String.split() is used therefore the delimiter is used as regular expression.  Choose your delimiter carefully, e.g., don't use one that have
     * special meeanings in regular experssion.  colon(:), pound(#), comma(,), tilda(~) are good ones to choose from.
     * @param key
     * @param delimiter The delimiter to use to split the string property into key value pairs.
     * @param keyValueDelimiter The delimter to use to split the key value pairs into key and value.
     * @return  The configured map, or null if no key:values are configured for key.
     */
    Map<String, String> getPropertyMap(String key, String delimiter, String keyValueDelimiter);

    /**
     * Returns property as boolean. Will intepret true/yes/y/t/on (case insensitive) as true, anything else as false.
     * @param key   key for which to get an Integer value
     * @param locale property locale.
     */
    boolean getPropertyBoolean(String key, Locale locale);

    /**
     * Returns property as boolean.
     * @param key   key for which to get an Integer value
     */
    boolean getPropertyBoolean(String key);

    /**
     * Returns property as boolean. Will intepret true/yes/y/t/on (case insensitive) as true, anything else as false.
     * @param key   key for which to get an Integer value
     * @param locale property locale.
     */
    boolean getPropertyBoolean(String key, boolean defaultValue, Locale locale);

    /**
     * Returns property as boolean.
     * @param key   key for which to get an Integer value
     */
    boolean getPropertyBoolean(String key, boolean defaultValue);

    void list(PrintWriter out);
    void list(PrintStream out);

    
    /**
     * @param locale property locale.
     * @return A set of all properties names that is configured for the application.
     */
    Set<String> propertyNames(Locale locale);
    
    /**
     * @return A set of all properties names that is configured for the application.
     */
    Set<String> propertyNames();
}
