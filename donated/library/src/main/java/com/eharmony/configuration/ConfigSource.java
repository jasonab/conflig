package com.eharmony.configuration;

import java.io.File;

/**
 * Represents a configuration source.
 */
public class ConfigSource {
    public enum Format {
        Properties(".properties"), XML(".xml");
        private String defaultExtension;

        private Format(String defaultExtension) {
            this.defaultExtension = defaultExtension;
        }

        public String getDefaultExtension() {
            return defaultExtension;
        }
    }

    public enum Type {
        Classpath(""), FileSystem(File.separator);
        private String defaultLocation;

        private Type(String defaultLocation) {
            this.defaultLocation = defaultLocation;
        }

        public String getDefaultLocation() {
            return defaultLocation;
        }
    }
    
    private final Format DEFAULT_FORMAT = Format.Properties;
    private final Type DEFAULT_TYPE = Type.Classpath;
    /**
     * Each ConfigSource should have a (logical) name.   Mandatory.
     * This is specified as part of config.order in eh-config.properties.
     */
    private String name;
    /**
     * Actual file name of the config source, without the extension.  If no fileName is specified then the logic
     * name will be used as actual file name.   Optional.
     */
    private String fileName; // the actual file name
    /**
     * The type of the ConfigSource, either a Classpath or FileSystem.
     * @see com.eharmony.configuration.ConfigSource.Type
     */
    private Type type;
    /**
     * Format of the ConfigSource, either Properties (i.e., java.util.Properties format or XML properties format.
     * @see com.eharmony.configuration.ConfigSource.Format
     */
    private Format format;
    /**
     * Path to config source.  It can be either a file system path or a classpath.
     */
    private String location;
    /**
     * The optional testFile property specifies a file/directory that must exist for reloading to take effect.
     * If it is specified but could not be found (e.g., in the rare case when we loose NFS), the whole configuration
     * reloading process will be skipped, the old configuration will be kept intact.  This is an optional property
     *  and only applies to FileSystem based config sources.
     */
    private String testFile;

    public String getTestFile() {
        return testFile;
    }

    public void setTestFile(String testFile) {
        this.testFile = testFile;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        if (fileName == null || fileName.trim().length() == 0) {
            return getName();
        }
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Type getType() {
        if (type != null) {
            return type;
        }
        return DEFAULT_TYPE;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public Format getFormat() {
        if (format != null) {
            return format;
        }
        return DEFAULT_FORMAT;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public String getLocation() {
        if (location == null) {
            return getType().getDefaultLocation();
        }
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getExtension() {
        return getFormat().getDefaultExtension();
    }
    
    public String toString(){
        return getName() + "|" + getFileName() + "|" + getLocation() + "|" + getFormat()
                + "|" + getType() + "|" + getTestFile();
    }
}