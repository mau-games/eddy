package util.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Reads a configuration file and exposes its content to the Java application.
 * It is intended to be used as a Singleton for performance reasons. Should a
 * need for different configuration files arise, the best course of action is
 * to extend the class, expose its protected constructors and reimplement the
 * getInstance() method to something else.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class ConfigurationUtility { 
	
	private static String defaultConfig = "config/config.json";
	final static Logger logger = LoggerFactory.getLogger(ConfigurationUtility.class);
	private static ConfigurationUtility instance = null;
	
	private JsonObject config;
	
	/**
	 * Creates an instance of ConfigurationReader, reading the contents from a
	 * file located at the default location, config/config.json.
	 * @throws MissingConfigurationException if the configuration file wasn't
	 * 		found.
	 */
	protected ConfigurationUtility() throws MissingConfigurationException {
		FileReader file = null;
		ClassLoader loader = getClass().getClassLoader();
		try {
			file = new FileReader(loader.getResource(defaultConfig).getFile());
		} catch (FileNotFoundException e) {
			throw new MissingConfigurationException();
		}
		readFile(file);
	}
	
	/**
	 * Creates an instance of ConfigurationReader.
	 * 
	 * @param location The URL of a configuration file. This could be a local
	 * 		file, as well as a remote HTTP resource.
	 * @throws MissingConfigurationException if the configuration file wasn't
	 * 		found. 
	 */
	protected ConfigurationUtility(String location)
			throws MissingConfigurationException {
		URL url;
		BufferedReader file = null;
		try {
			if (location.startsWith("http://") ||
					location.startsWith("https://")) {
				url = new URL(location);
			} else {
				url = Paths.get(location).toUri().toURL();
			}
			file = new BufferedReader(new InputStreamReader(url.openStream()));
		} catch (MalformedURLException e) {
			throw new MissingConfigurationException();
		} catch (IOException e) {
			logger.error("Couldn't read the configuration file: " + e.getMessage());
		}
		readFile(file);
	}
	
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @return An instance of ConfigurationReader.
	 * @throws MissingConfigurationException
	 */
	public static ConfigurationUtility getInstance()
			throws MissingConfigurationException {
		if (instance == null) {
			instance = new ConfigurationUtility();
		}
		return instance;
	}
	
	/**
	 * Gets the singleton instance of this class.
	 * 
	 * @param location The URL of a configuration file. This could be a local
	 * 		file, as well as a remote HTTP resource.
	 * @return An instance of ConfigurationReader.
	 * @throws MissingConfigurationException
	 */
	public static ConfigurationUtility getInstance(String location)
			throws MissingConfigurationException {
		if (instance == null) {
			instance = new ConfigurationUtility(location);
		}
		return instance;
	}
	
	/**
	 * Returns the entire config tree as a JSON object.
	 * 
	 * @return The config tree.
	 */
	public JsonObject getTree() {
		return config;
	}
	
	/**
	 * Reads a configuration value and tries to parse it as a double.
	 * 
	 * @param path The configuration path, e.g. map.collector.path.
	 * @return A configuration value.
	 */
	public double getDouble(String path) {
		return traverse(path).getAsDouble();
	}
	
	/**
	 * Reads a configuration value and tries to parse it as an integer.
	 * 
	 * @param path The configuration path, e.g. map.collector.path.
	 * @return A configuration value.
	 */
	public int getInt(String path) {
		return traverse(path).getAsInt();
	}
	
	/**
	 * Reads a configuration value and tries to parse it as a string.
	 * 
	 * @param path The configuration path, e.g. map.collector.path.
	 * @return A configuration value.
	 */
	public String getString(String path) {
		return traverse(path).getAsString();
	}
	
	/**
	 * Reads a configuration value and tries to parse it as a boolean.
	 * 
	 * @param path The configuration path, e.g. map.collector.path.
	 * @return A configuration value.
	 */
	public boolean getBoolean(String path) {
		return traverse(path).getAsBoolean();
	}
	
	/**
	 * Reads a configuration file.
	 * 
	 * @param file The configuration file.
	 */
	private void readFile(Reader file) {
		JsonParser parser = new JsonParser();
		config = (JsonObject) parser.parse(file);
	}
	
	/**
	 * Finds a JSON element based on a path by looking for the composite
	 * keys.
	 * 
	 * @param path A path, e.g. "game.profiles.default".
	 * @return A raw JSON element, which can then be accessed by this class.
	 */
	private JsonElement traverse(String path) {
		String[] elems = path.split("\\.");
		JsonObject o = config;
		
		if (elems.length > 1) {
			for (int i = 0; i < elems.length - 1; i++) {
				o = o.get(elems[i]).getAsJsonObject();
			}
		}
		
		return o.get(elems[elems.length - 1]);
	}
}
