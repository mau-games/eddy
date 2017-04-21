package game;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;

public class ApplicationConfig {
	final static Logger logger = LoggerFactory.getLogger(ConfigurationUtility.class);
	private static String defaultConfig = "config/application_config.json";
	private static ApplicationConfig instance = null;
	private ConfigurationUtility config;
	
	protected ApplicationConfig() throws MissingConfigurationException{
		config = new ConfigurationUtility(defaultConfig, true);
	}
	
	public static ApplicationConfig getInstance() throws MissingConfigurationException{
		if (instance == null)
			instance = new ApplicationConfig();
		return instance;
	}
	
	public ConfigurationUtility getInternalConfig(){
		return config;
	}
	
	public int getDimensionM(){
		return config.getInt("game.dimensions.m");
	}
	
	public int getDimensionN(){
		return config.getInt("game.dimensions.n");
	}
	
	public int getDoors(){
		return config.getInt("game.doors");
	}
	
}
