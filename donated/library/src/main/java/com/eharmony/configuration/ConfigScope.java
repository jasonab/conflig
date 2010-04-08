package com.eharmony.configuration;
public class ConfigScope {
    
    public enum ScopeType {
        server("server"), host("host"), cluster("cluster"), environment("environment"), global("global");
        
        private String defaultPrefix;
        
        private ScopeType(String defaultPrefix) {
            this.defaultPrefix = defaultPrefix;
        }
        public String getDefaultPrefix() {
            return defaultPrefix;
        }
    }
    
    private ScopeType type;
    
    /**
     * Instance name for a particular scope type, e.g., dc1-umsg01 is an instance name
     * for the scope of host.
     */
    private String instanceName;
    private String prefix;
    
    public ScopeType getType() {
        return type;
    }
    public void setType(ScopeType type) {
        this.type = type;
    }
    public String getInstanceName() {
        return instanceName;
    }
    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }
    public String getPrefix() {
    	if (prefix == null || prefix.trim().length() == 0){
    		return getType().getDefaultPrefix();
    	}
        return prefix;
    }
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    public String toString(){
        return getType() + "|" + getPrefix() + "|" + getInstanceName();
    }
    
}
