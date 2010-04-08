package com.eharmony.configuration;



import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.eharmony.configuration.ConfigSource.Format;
import com.eharmony.configuration.ConfigSource.Type;

/**
 * Utility methods, such as determine whether inside or outside container, determine scope (server, 
 * host, cluster, environment), construct property file names based on configuration names and scope, etc, etc.
 * @author fwu
 *
 */
public class ConfigUtil {
    private static Log logbuildConsolidatedProperties = LogFactory.getLog(ConfigUtil.class.getName() + ".buildConsolidatedProperties");
    private static Log logGetHost = LogFactory.getLog(ConfigUtil.class.getName() + ".getLog");
    private static Log logGetInputStreamFromFileSystem = LogFactory.getLog(ConfigUtil.class.getName() + ".getInputStreamFromFileSystem");

    //    private static Log log = LogFactory.getLog(ConfigUtil.class);
    private ConfigUtil(){
    }

    private static class ConfigUtilHolder {
        private final static ConfigUtil instance = new ConfigUtil();
    }

    public static ConfigUtil getInstance() {
         return ConfigUtilHolder.instance;
    }

 
    /**
     * determine server name, such as wlserver-umsg-01
     * @return server name.
     */
    public String getServer(){
        // according to setDomainEnv.sh, server name is an environment property named SERVER_NAME
        // SERVER_NAME value is then used as -D property with name of "weblogic.name"
    	String res = System.getProperty("server");
    	if(isEmpty(res)) {
    		res = System.getProperty("weblogic.Name");
    	}

		if (isEmpty(res)) {
			res = System.getProperty("mule.serverId");
		}

    	return res;
    }

    /**
     * determine host name such as dc1-umsg01
     * @return host name.
     */
    public String getHost(){
        Log log = logGetHost;
        java.net.InetAddress localMachine;
        try {
            localMachine = java.net.InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            if (log.isWarnEnabled()) {
                log.warn("Cannot get localhost's InetAddress", e);
            }
            return null;
        }    
        return localMachine.getHostName();
    }

    /**
     * determine cluster name, such as fnd-dev-cluster-01
     * @return cluster name.
     */
    public String getCluster(){
    	String res = System.getProperty("cluster");
    	if(isEmpty(res)) {
    		res = System.getProperty("weblogic.Cluster");
    	}

		if (isEmpty(res)) {
			res = System.getProperty("mule.clusterId");
		}

    	return res;
    }

    /**
     * determine environment name, such as production, dev, etc.
     * @return environment name.
     */
    public String getEnvironment(){
        return System.getProperty("environment");
    }
    
    /**
     * Are we running inside a Java EE container?  For now check server name, if not
     * null or empty then assume it is inside container.
     * @return true if running inside container, false otherwise.
     */
    public boolean isInContainer(){
        return !isEmpty(getServer());
    }

    /** 
     * Use this since we don't want to depend on commons-lang.
     * @param string The string to test.
     * @return true if it is empty or null.
     */
    public boolean isEmpty(String string) {
        return string == null || string.trim().length() == 0;
    }

    /**
     * Provides functionality for textual properties substitution.
     * 
     * Syntax:
     * property1=val1
     * property2=val2
     * property3=${property1}${property2} ;
     * After substitution property3=val1val2 .
     * Symbols '$', '{', '}' can be escaped by '\' .
     * 
     */
    public static class PropertySubstitution {
		private final static Log log = LogFactory
				.getLog(PropertySubstitution.class);

		final static String SEARCH_PATTERN = "\\$\\{[^\\$\\{\\}]+\\}";

		static String unEscape(String source) {
			return source.replaceAll("\\\\\\$", "\\$").replaceAll("\\\\\\{",
					"{").replaceAll("\\\\\\}", "}");
		}

		static String escape(String source) {
			return source.replaceAll("\\$", "\\\\\\$").replaceAll("\\{",
					"\\\\\\{").replaceAll("\\}", "\\\\\\}");
		}

		static Set<String> analyze(String source) {
			log.trace("Analyzing source: " + source);

			Set<String> res = new HashSet<String>();

			Pattern pattern = Pattern.compile(SEARCH_PATTERN);
			Matcher matcher = pattern.matcher(source);
			while (matcher.find()) {
				String property = source.substring(matcher.start() + 2, matcher
						.end() - 1);
				res.add(property);
				log.trace("Source: " + source + " is matched; from: "
						+ matcher.start() + "; to:" + matcher.end()
						+ "; value: " + property);
			}

			return res;
		}

		static HashMap<String, List<String>> buildDependenceMap(
				Map<String, String> properties) {
			HashMap<String, List<String>> res = new HashMap<String, List<String>>();
			for (Map.Entry<String, String> entry : properties.entrySet()) {
				LinkedList<String> list = new LinkedList<String>();
				String s = entry.getValue();
				for (String p : analyze(s)) {
					if (properties.containsKey(p)) {
						list.add(p);
					}
				}
				res.put(entry.getKey(), list);
			}

			return res;
		}

		static LinkedHashMap<String, List<String>> buildSortedDependenceMap(
				Map<String, String> properties) {
			LinkedHashMap<String, List<String>> res = new LinkedHashMap<String, List<String>>();
			HashMap<String, List<String>> depMap = buildDependenceMap(properties);

			while (res.size() != properties.size()) {
				boolean cyclic = true;
				Iterator<String> it = depMap.keySet().iterator();
				while (it.hasNext()) {
					String key = it.next();
					List<String> list = depMap.get(key);
					int count = list.size();
					for (String k : list) {
						if (res.containsKey(k)) {
							count--;
						}
					}
					if (count == 0) {
						cyclic = false;
						res.put(key, list);
						it.remove();
						continue;
					}
				}
				if (cyclic) {
					throw new RuntimeException(
							"Properties contains cyclic dependence!");
				}
			}

			return res;
		}
		
		static void substitute(Map<String, String> properties) {
			LinkedHashMap<String, List<String>> map = buildSortedDependenceMap(properties);
			for (Map.Entry<String, List<String>> entry : map.entrySet()) {
				String key = entry.getKey();
				List<String> list = entry.getValue();
				String value = properties.get(key);
				for (String p : list) {
					StringBuilder sb = new StringBuilder();
					sb.append("${").append(p).append("}");					
					value = value.replace(sb.toString(), properties.get(p));
				}
				properties.put(key, unEscape(value));
			}
		}

	}
    
    public ConcurrentMap<String, String> buildConsolidatedProperties(Locale locale, List<ConfigSource> configSources,
            List<ConfigScope> configScopes, boolean failOnError) {
        Log log = logbuildConsolidatedProperties;
        ConcurrentMap<String, String> consolidatedProperties = new ConcurrentHashMap<String, String>();
            for (ConfigSource source : configSources) {
                if (log.isDebugEnabled()) {
                    log.debug("Building ConfigProperties from source " + source.toString());
                }
                for (ConfigScope scope : configScopes) {
                    if (log.isDebugEnabled()) {
                        log.debug("Building ConfigProperties from scope " + scope.toString() + " for source " + source.toString());
                    }
                    String fileNameWithScope = buildFileNameWithScope(locale, scope, source);
                    // e.g., myappConfig.host.dc1-host01
                    String fullPath = buildFullPath(source, fileNameWithScope);
                    // e.g., /com/abc/def/myappConfig.host.dc1-host01.properties
                    // or /data/xyz/myappConfig.host.dc1-host01.properties

                    InputStream in = null;
                    if (Type.FileSystem.equals(source.getType())) {
                        in = getInputStreamFromFileSystem(fullPath);
                    } else {
                        in = getInputStreamFromClasspath(fullPath);
                    }

                    if (in != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("Reading from " + fullPath);
                        }
                        Properties properties = new Properties();
                        try {
                            if (Format.XML.equals(source.getFormat())) {
                                properties.loadFromXML(in);
                            } else {
                                properties.load(in);
                            }
                        } catch (IOException e) {
                            String msg = "Cannot read configuration source at " + fileNameWithScope + source.getExtension();
                            if (failOnError) {
                                if (log.isErrorEnabled()) {
                                    log.error(msg, e);
                                }
                                throw new ConfigurationRuntimeException(msg, e);
                            } else {
                                if (log.isWarnEnabled()) {
                                    log.warn(msg, e);
                                }
                            }
                        }

                        consolidateProperties(consolidatedProperties, properties);
                    }
                    else {
                        if (log.isDebugEnabled()) {
                            log.debug("No file found at " + fullPath); 
                        }
                    }

                }
            }
            // lastly we need to layer in system properties.
            consolidateProperties(consolidatedProperties, System.getProperties());
            if (consolidatedProperties.size() > System.getProperties().size()) {
                // perform substitution only when there is more than just System properties
                PropertySubstitution.substitute(consolidatedProperties);
            }
        
        return consolidatedProperties;
    }

    
    public ConcurrentHashMap<Locale, ConcurrentMap<String, String>> buildLocalizedConsolidatedProperties(Set<Locale> locales, List<ConfigSource> configSources,
            List<ConfigScope> configScopes, boolean failOnError) {
        ConcurrentHashMap<Locale, ConcurrentMap<String, String>> localizedConsolidatedProperties = new ConcurrentHashMap<Locale, ConcurrentMap<String, String>>();
        for (Locale locale : locales) {
			ConcurrentMap<String, String> consolidated = buildConsolidatedProperties(
					locale, configSources, configScopes, failOnError);
			localizedConsolidatedProperties.put(locale, consolidated);
		}
        
        return localizedConsolidatedProperties;
    }

    public String buildFileNameWithScope(ConfigScope scope, ConfigSource source) {
        return buildFileNameWithScope(ConfigProperties.DEFAULT_LOCALE, scope, source);
    }

    public String buildFileNameWithScope(Locale locale, ConfigScope scope, ConfigSource source) {
        String separator = ".";
        StringBuilder builder = new StringBuilder(source.getFileName()).append(separator).append(scope.getPrefix());
        if (!isEmpty(scope.getInstanceName())) {
            builder.append(separator).append(scope.getInstanceName());
        }
        if(locale != null && !"".equals(locale.toString())) {
        	builder.append('_').append(locale);
        }
        
        return builder.toString();
    }

    public String buildFullPath(ConfigSource source, String fileNameWithScope) {
        // for classpath ConfigSource, strip leading forward/back slashes until there is none
        if (Type.Classpath.equals(source.getType())) {
            return buildFullPathForClasspath(source, fileNameWithScope);
        }
        return buildFullPathForFileSystem(source, fileNameWithScope);
    }
    private String buildFullPathForClasspath(ConfigSource source, String fileNameWithScope) {
        StringBuilder builder = new StringBuilder(source.getLocation());
        if (!source.getLocation().endsWith("/")) {
            builder.append("/");
        }
        builder.append(fileNameWithScope).append(source.getExtension());
        String fullPath = builder.toString();
        
        // for classpath ConfigSource, strip leading forward/back slashes until there is none
        fullPath = stripLeadingSlashs(fullPath);
        return fullPath;
    }

    private String stripLeadingSlashs(String fullPath) {
		if (fullPath.startsWith("/") || fullPath.startsWith("\\")){
			fullPath = stripLeadingSlashs(fullPath.substring(1));
		}
		return fullPath;
	}

	private String buildFullPathForFileSystem(ConfigSource source, String fileNameWithScope) {
		String location = source.getLocation();
        StringBuilder builder = new StringBuilder(location);
        if (!location.endsWith("/") && !location.endsWith("\\")) {
            builder.append(File.separator);
        }
        builder.append(fileNameWithScope).append(source.getExtension());
        return builder.toString();
    }

    protected void consolidateProperties(ConcurrentMap<String, String> consolidatedProperties, Properties properties) {
        Set<Object> keys = properties.keySet();
        for (Object obj : keys) {
            String key = (String) obj;
            consolidatedProperties.put(key, properties.getProperty(key));
        }
    }
    
    protected InputStream getInputStreamFromClasspath(String path) {
        InputStream stream = null;
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url != null) {
            stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
        }
        return stream;
    }

    protected InputStream getInputStreamFromFileSystem(String path) {
        Log log = logGetInputStreamFromFileSystem;
        InputStream stream = null;
        File file = new File(path);
        if (file.exists()) {
            try {
                stream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                if (log.isErrorEnabled()) {
                    log.error("cannot find specified root config file at " + path);
                }
            }
        }
        return stream;
    }
}
