package com.eharmony.configuration;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

//TODO make the class non-public.
public class ConfigProperties implements Configuration {

    /**
     * generated.
     */
    private static final long serialVersionUID = -1838113994508751939L;

    /**
     * default list delimiter is comma.
     */
    private static final String DEFAULT_DELIMITER = ",";
    /**
     * default list delimiter is comma.
     */
    private static final String DEFAULT_KEY_VALUE_DELIMITER = ":";
    
    public final static Locale DEFAULT_LOCALE = Locale.getDefault();
    
    private final static Locale EMPTY_LOCALE = new Locale("");

    private boolean failOnError = false;

    /**
     * Ordered list of potential configuration sources at each configuration
     * scope. Read from config.order property. For example, myapp, myapp-xml,
     * myapp-overrides.
     */
    private List<ConfigSource> configSources;

    /**
     * Order list of scopes an application is configured for. Read from
     * scope.order property. Default to
     * system,server,host,cluster,environment,global.
     */
    private List<ConfigScope> configScopes;
    
    /**
     * Fully cooked properties. Overrides and interpolation applied.
     */
    private ConcurrentMap<Locale, ConcurrentMap<String, String>> localizedConsolidatedProperties;
    
    private Properties rootProperties;
    /**
     * Strategy to reloadConfigProperties. Default to not reload.
     */
    private ConfigReloader reloader;
    private boolean enableReload = false;
    private boolean reloadInProgress = false;

    private Map<Locale, Map<String, List<String>>> localizedPropertyLists;
    private Map<Locale, Map<String, Map<String, String>>> localizedPropertyMaps;


    public ConfigProperties() {
    	super();
    	localizedConsolidatedProperties = new ConcurrentHashMap<Locale, ConcurrentMap<String, String>>();
    	localizedConsolidatedProperties.put(DEFAULT_LOCALE, new ConcurrentHashMap<String, String>());
    	localizedConsolidatedProperties.put(EMPTY_LOCALE, new ConcurrentHashMap<String, String>());
    	localizedPropertyLists = new ConcurrentHashMap<Locale, Map<String, List<String>>>();
        localizedPropertyMaps = new ConcurrentHashMap<Locale, Map<String, Map<String, String>>>();
        reloader = new DoNothingConfigReloader();
	}
    public ConfigProperties(Properties rootProperties) {
    	this();
		this.rootProperties = rootProperties;
	}

    public ConfigReloader getReloader() {
        return reloader;
    }

    public void setReloader(ConfigReloader reloadStrategy) {
        this.reloader = reloadStrategy;
    }

    public List<ConfigSource> getConfigSources() {
        return configSources;
    }

    public void setConfigSources(List<ConfigSource> configSources) {
        this.configSources = configSources;
    }

    public List<ConfigScope> getConfigScopes() {
        return configScopes;
    }

    public void setConfigScopes(List<ConfigScope> configScopes) {
        this.configScopes = configScopes;
    }
	
	public String getProperty(String key, String defaultValue) {
        return getProperty(key, defaultValue, DEFAULT_LOCALE);
    }
	
	List<Locale> getCandidateLocales(Locale locale) {
		List<Locale> res = new ArrayList<Locale>(3);
		String language = locale.getLanguage();
		String country = locale.getCountry();
		String variant = locale.getVariant();

		if (variant.length() > 0) {
			res.add(locale);
		}
		if (country.length() > 0) {
			res.add((res.size() == 0) ? locale : new Locale(language, country,
					""));
		}
		if (language.length() > 0) {
			res.add((res.size() == 0) ? locale : new Locale(language, "", ""));
		}

		return res;
	}
	
	public String getProperty(String key, String defaultValue, Locale locale) {
        List<Locale> candidateLocales = getLocalesToLookUp(locale);

		String res = null;
		for (Locale candidateLocale : candidateLocales) {
            ConcurrentMap<String, String> consolidatedProperties = localizedConsolidatedProperties
                    .get(candidateLocale);
            if (consolidatedProperties == null) {
                // this particular locale has not been loaded, load it now.
                AbstractConfigReloader.reloadLocale(this, candidateLocale);
            }
            consolidatedProperties = localizedConsolidatedProperties.get(candidateLocale);
            if (consolidatedProperties != null) {
                res = consolidatedProperties.get(key);
            }
            if (res != null) {
                break;
            }
		}
		if (res == null) {
			res = defaultValue;
		}
		reload();

		return res;
	}

    /**
     * Null locale will be treated as EMPTY_LOCALE.
     * @param locale
     * @return
     */
    protected List<Locale> getLocalesToLookUp(Locale locale) {
        List<Locale> candidateLocales = new ArrayList<Locale>(7);

        if (locale == null) {
            candidateLocales.add(EMPTY_LOCALE);
            return candidateLocales;
        }

        candidateLocales.addAll(getCandidateLocales(locale));
        if (!DEFAULT_LOCALE.equals(locale)) {
            candidateLocales.addAll(getCandidateLocales(DEFAULT_LOCALE));
        }
        if (!candidateLocales.contains(EMPTY_LOCALE)) {
            candidateLocales.add(EMPTY_LOCALE);
		}
        return candidateLocales;
    }

    public String getProperty(String key) {
		return getProperty(key, DEFAULT_LOCALE);
	}
    
    public String getProperty(String key, Locale locale) {
		return getProperty(key, null, locale);
	}


    private String trimStringProperty(String key, Locale locale) {
        String temp = getProperty(key, locale);
        if (temp != null) {
            temp = temp.trim();
        }
        return temp;
    }

    private String trimStringProperty(String key) {
        String temp = getProperty(key);
        if (temp != null) {
            temp = temp.trim();
        }
        return temp;
    }

    public Integer getPropertyInteger(String key, Locale locale) {
        return Integer.valueOf(trimStringProperty(key, locale));
    }

    public Integer getPropertyInteger(String key) {
        return getPropertyInteger(key, DEFAULT_LOCALE);
    }

    public Integer getPropertyInteger(String key, Integer defaultValue, Locale locale) {
        String temp = trimStringProperty(key, locale);
        if (temp == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(temp);
        }
        catch(NumberFormatException e) {
            return defaultValue;
        }
    }

    public Integer getPropertyInteger(String key, Integer defaultValue) {
        return getPropertyInteger(key, defaultValue, DEFAULT_LOCALE);
    }

    public Long getPropertyLong(String key, Locale locale) {
        return Long.valueOf(trimStringProperty(key, locale));
    }
    
    public Long getPropertyLong(String key) {
        return getPropertyLong(key, DEFAULT_LOCALE);
    }

    public Long getPropertyLong(String key, Long defaultValue, Locale locale) {
        String temp = trimStringProperty(key, locale);
        if (temp == null) {
            return defaultValue;
        }
        try {
            return Long.valueOf(temp);
        }
        catch (NumberFormatException e) {
            return defaultValue;            
        }
    }

    public Long getPropertyLong(String key, Long defaultValue) {
        return getPropertyLong(key, defaultValue, DEFAULT_LOCALE);    
    }

    public Float getPropertyFloat(String key, Locale locale) {
        return Float.valueOf(trimStringProperty(key, locale));
    }

    public Float getPropertyFloat(String key) {
        return getPropertyFloat(key, DEFAULT_LOCALE);
    }

    public Float getPropertyFloat(String key, Float defaultValue, Locale locale) {
        String temp = trimStringProperty(key, locale);
        if (temp == null) {
            return defaultValue;
        }
        try {
            return Float.valueOf(temp);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Float getPropertyFloat(String key, Float defaultValue) {
        return getPropertyFloat(key, defaultValue, DEFAULT_LOCALE);
    }

    public Double getPropertyDouble(String key, Locale locale) {
        return Double.valueOf(getProperty(key, locale));
    }

    public Double getPropertyDouble(String key) {
        return getPropertyDouble(key, DEFAULT_LOCALE);
    }

    public Double getPropertyDouble(String key, Double defaultValue, Locale locale) {
        String temp = trimStringProperty(key, locale);
        if (temp == null) {
            return defaultValue;
        }
        try {
            return Double.valueOf(temp);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Double getPropertyDouble(String key, Double defaultValue) {
        return getPropertyDouble(key, defaultValue, DEFAULT_LOCALE);
    }

    public List<String> getPropertyList(String key, Locale locale) {
        return getPropertyList(key, DEFAULT_DELIMITER, locale);
    }
    
    public List<String> getPropertyList(String key) {
        return getPropertyList(key, DEFAULT_DELIMITER);
    }

    public List<String> getPropertyList(String key, String delimiter) {
    	return getPropertyList(key, delimiter, DEFAULT_LOCALE);
    }
    
    public List<String> getPropertyList(String key, String delimiter, Locale locale) {
        // check internal cache first    	
    	Map<String, List<String>> propertyLists = localizedPropertyLists.get(locale);
    	if (propertyLists == null) {
    		propertyLists = new ConcurrentHashMap<String, List<String>>();
    	}
        List<String> list = propertyLists.get(key);
        if (list != null) {
            return list;
        }
        if (getProperty(key, locale) == null) {
            return null;
        }
        list = fromCsvToList(getProperty(key, locale), delimiter);
        propertyLists.put(key, list);
        localizedPropertyLists.put(locale, propertyLists);
        return list;
    }

    protected List<String> fromCsvToList(String csv, String delimiter) {
        String[] values = csv.split(delimiter);
        List<String> list = new ArrayList<String>();
        for (String value : values) {
            String trimmed = value.trim();
            if (trimmed.length() != 0) {
                list.add(trimmed);
            }
        }
        return list;
    }

    public Map<String, String> getPropertyMap(String key) {
        return getPropertyMap(key, DEFAULT_DELIMITER, DEFAULT_KEY_VALUE_DELIMITER);
    }

    public Map<String, String> getPropertyMap(String key, Locale locale) {
    	return getPropertyMap(key, DEFAULT_DELIMITER, DEFAULT_KEY_VALUE_DELIMITER, locale);
    }

    public Map<String, String> getPropertyMap(String key, String delimiter, String keyValueDelimiter) {
    	return getPropertyMap(key, delimiter, keyValueDelimiter, DEFAULT_LOCALE);
    }

    public boolean getPropertyBoolean(String key, Locale locale) {
        String temp = getProperty(key, locale);
        return canBeIntepretedAsTrue(temp);
    }

    public boolean getPropertyBoolean(String key) {
        return getPropertyBoolean(key, DEFAULT_LOCALE);
    }

    public boolean getPropertyBoolean(String key, boolean defaultValue, Locale locale) {
        String temp = trimStringProperty(key, locale);
        if (temp == null) {
            return defaultValue;
        }
        return Boolean.valueOf(temp);
    }

    public boolean getPropertyBoolean(String key, boolean defaultValue) {
        return getPropertyBoolean(key, defaultValue, DEFAULT_LOCALE);
    }

    protected boolean canBeIntepretedAsTrue(String value) {
        if (value == null) {
            return false;
        }
        value = value.toLowerCase();
        if ("true".equals(value) || "t".equals(value)
                || "yes".equals(value) || "y".equals(value)
                || "1".equals(value) || "on".equals(value)) {
            return true;
        }
        return false;
    }

    public Map<String, String> getPropertyMap(String key, String delimiter, String keyValueDelimiter, Locale locale) {
    	Map<String, Map<String, String>> propertyMaps = localizedPropertyMaps.get(locale);
    	if(propertyMaps == null) {
    		propertyMaps = new HashMap<String, Map<String, String>>();
    	}
        // check internal cache first    	
        Map<String, String> map = propertyMaps.get(key);
        if (map != null) {
            return map;
        }
        if (getProperty(key, locale) == null) {
            return null;
        }
        map = fromCsvToMap(getProperty(key, locale), delimiter, keyValueDelimiter);
        propertyMaps.put(key, map);
        localizedPropertyMaps.put(locale, propertyMaps);
        return map;
    }


    protected Map<String, String> fromCsvToMap(String keyValuePairs, String delimiter, String keyValueDelimiter) {
        List<String> pairs = fromCsvToList(keyValuePairs, delimiter);
        Map<String, String> map = new HashMap<String, String>();
        for (String pair : pairs) {
            String[] keyValue = pair.split(keyValueDelimiter);
            String tempKey = null;
            if (keyValue.length > 0) {
                tempKey = keyValue[0].trim();
            }
            String tempValue = null;
            if (keyValue.length > 1) {
                tempValue = keyValue[1].trim();
            }
            if (tempKey != null && tempKey.length() != 0) {
                map.put(tempKey, tempValue);
            }
        }
        return map;
    }

    private synchronized void reload() {
        if (isEnableReload() && !isReloadInProgress()) {
            reloader.reload(this, false);
        }
    }

    public void list(PrintStream out) {
        out.println("-- listing properties --");
        out.println(this.toString());
    }

    public void list(PrintWriter out) {
        out.println("-- listing properties --");
        out.println(this.toString());
    }

    public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<Locale, ConcurrentMap<String, String>> lcpEntry : localizedConsolidatedProperties
				.entrySet()) {
			builder.append("locale=").append(lcpEntry.getKey()).append(
					System.getProperty("line.separator"));
			ConcurrentMap<String, String> consolidatedProperties = lcpEntry
					.getValue();

			Set<Map.Entry<String, String>> entries = consolidatedProperties
					.entrySet();
			for (Map.Entry<String, String> entry : entries) {
				String key = entry.getKey();
				String value = entry.getValue();
				if (value.length() > 80) {
					value = value.substring(0, 77) + "...";
				}
				builder.append(key).append("=").append(value).append(
						System.getProperty("line.separator"));

			}
		}
		return builder.toString();
	}

    public Set<String> propertyNames() {
       return propertyNames(DEFAULT_LOCALE);
    }
    
    public Set<String> propertyNames(Locale locale) {
		Set<String> res = new HashSet<String>();
		ConcurrentMap<String, String> consolidatedProperties = localizedConsolidatedProperties
				.get(locale);
		if (consolidatedProperties != null) {
			res.addAll(consolidatedProperties.keySet());
		}
		if (DEFAULT_LOCALE.equals(locale)) {
			consolidatedProperties = localizedConsolidatedProperties
					.get(EMPTY_LOCALE);
			if (consolidatedProperties != null) {
				res.addAll(consolidatedProperties.keySet());
			}
		}
		return res;
	}

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean isEnableReload() {
        return enableReload;
    }

    public void setEnableReload(boolean enableReload) {
        this.enableReload = enableReload;
    }

    public Properties getRootProperties() {
        return rootProperties;
    }

    void setRootProperties(Properties rootProperties) {
        this.rootProperties = rootProperties;
    }

    public void swap(ConcurrentMap<String, String> consolidated) {
    	swap(consolidated, DEFAULT_LOCALE);
    }

    public void swap(ConcurrentMap<String, String> consolidated, Locale locale) {
		localizedConsolidatedProperties.put(locale, consolidated);
		Map<String, List<String>> propertyLists = localizedPropertyLists.get(locale);
		if (propertyLists != null) {
			propertyLists.clear();
		}
		Map<String, Map<String, String>>propertyMaps = localizedPropertyMaps.get(locale);
		if (propertyMaps != null) {
			propertyMaps.clear();
		}
	}

	void setConsolidatedProperties(
			ConcurrentMap<String, String> consolidatedProperties) {		
		setConsolidatedProperties(consolidatedProperties, DEFAULT_LOCALE);
	}

	void setConsolidatedProperties(
			ConcurrentMap<String, String> consolidatedProperties, Locale locale) {		
		localizedConsolidatedProperties.put(locale, consolidatedProperties);
	}

	
    public ConcurrentMap<Locale, ConcurrentMap<String, String>> getLocalizedConsolidatedProperties() {
		return localizedConsolidatedProperties;
	}
    
	public void setLocalizedConsolidatedProperties(
			ConcurrentMap<Locale, ConcurrentMap<String, String>> localizedConsolidatedProperties) {
		this.localizedConsolidatedProperties = localizedConsolidatedProperties;
	}
	
	public void setReloadInProgress(boolean reloadInProgress) {
        this.reloadInProgress = reloadInProgress;
    }

   public boolean isReloadInProgress() {
        return reloadInProgress;
    }
   
   public boolean isEmpty() {
		return isEmpty(DEFAULT_LOCALE);
	}
   
   public boolean isEmpty (Locale locale) {
	   ConcurrentMap<String, String>consolidatedProperties = localizedConsolidatedProperties.get(locale);
	   return consolidatedProperties == null || consolidatedProperties.size() == 0	;
   }

}
