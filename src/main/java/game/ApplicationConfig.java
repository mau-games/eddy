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

	public void setDimensionM(int m){
		config.updateValue("game.dimensions.m", m);
	}
	
	public int getDimensionN(){
		return config.getInt("game.dimensions.n");
	}
	
	public void setDimensionN(int n){
		config.updateValue("game.dimensions.n", n);
	}
	
	public int getDoors(){
		return config.getInt("game.doors");
	}
	
	public String getGenerationCollectorPath(){
		return config.getString("collectors.generation_collector.path");
	}	
	
	public boolean getGenerationCollectorActive(){
		return config.getBoolean("collectors.generation_collector.active");
	}
	
	public String getMapCollectorPath(){
		return config.getString("collectors.map_collector.path");
	}
	
	public boolean getMapCollectorActive(){
		return config.getBoolean("collectors.map_collector.active");
	}
	
	public boolean getMapCollectorSaveAll(){
		return config.getBoolean("collectors.map_collector.save_all");
	}
	
	public String getImageExporterPath(){
		return config.getString("collectors.image_exporter.path");
	}
	
	public boolean getImageExporterActive(){
		return config.getBoolean("collectors.image_exporter.active");
	}
	
	public int getMapRenderHeight(){
		return config.getInt("map.render.height");
	}
	
	public int getMapRenderWidth(){
		return config.getInt("map.render.width");
	}
	
	public double getPatternOpacity(){
		return config.getDouble("map.pattern_opacity");
	}
	
}
