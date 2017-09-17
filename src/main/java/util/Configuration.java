package util;

import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

/**
 * Singleton Configuration
 * Usage example: 
 * Configuration config = Configuration.getInstance("/config.properties");
 * Properties properties = config.getProperties();
 * 
 * @author don_bacon
 *
 */
public class Configuration {

	private static Configuration configuration = null;
	private Properties properties = null;
	private static String configurationFilename = null;
	
	private Configuration() {}
	private Configuration(String configFilename) {
		properties = new Properties();
	}
	
	public static Configuration getInstance(String configFile) {
		if(configuration == null) {
			synchronized(Configuration.class){
				if(configuration == null) {
					configuration = new Configuration(configFile);
					configurationFilename = configFile;
					configuration.loadProperties();
				}
			}
		}
		return configuration;
	}
	
	private void loadProperties() {
        URL url = getClass().getResource(configurationFilename);
        if(url == null) {
            throw new IllegalArgumentException("Could not load resource: \"" + configurationFilename + "\"");
        }
        try {
        	InputStream stream = url.openStream();
        	properties.load(stream);
            stream.close();
        }
        catch(Exception e) {
        	System.err.println("Could not load " + configurationFilename + " " + e.toString());
        }
	}
	
	public Properties getProperties() {
		return this.properties;
	}
}
