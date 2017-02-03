package generator.algorithm;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import game.TileTypes;
import generator.config.Config;
import util.Util;
import util.config.ConfigurationReader;
import util.config.MissingConfigurationException;

//TODO: Consider merging this with Config.java
public class Ranges {
	private final Logger logger = LoggerFactory.getLogger(Config.class);
	private ConfigurationReader config;
	private JsonArray ranges;
	
	public Ranges(){
		try {
			config = ConfigurationReader.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read configuration file:\n" + e.getMessage());
		}
		
		readRanges(fetchRangesAsFile(config.getString("game.ranges.default")));
	}

	/**
	 * Get a random TileTypes weighted according to config ranges
	 * 
	 * @return A weighted random TileType
	 */
	public TileTypes getSupervisedRandomType(){
		float value = Util.getNextFloat(0.0f,1.0f);
		int ranges_size = ranges.size();
		
		for(int i = 0; i < ranges_size; i++){
			if(value >= ranges.get(i).getAsJsonObject().get("from").getAsFloat() && value < ranges.get(i).getAsJsonObject().get("to").getAsFloat()){
				int index = Util.getNextInt(0, ranges.get(i).getAsJsonObject().get("elements").getAsJsonArray().size());
				return Enum.valueOf(TileTypes.class, ranges.get(i).getAsJsonObject().get("elements").getAsJsonArray().get(index).getAsString());
			}
		}
		
		return TileTypes.WALL;
	}
	
	
	/**
	 * Reads a ranges file.
	 * 
	 * @param rangesReader A reader object pointing at a JSON ranges file.
	 */
	private void readRanges(Reader rangesReader) {
		JsonParser parser = new JsonParser();
		try {
			ranges = parser.parse(rangesReader).getAsJsonArray();
		} catch (JsonIOException | NullPointerException e) {
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Fetches a profile configuration file and makes it readable by the
	 * readRanges method.
	 * 
	 * @param ranges The name of the ranges file to look for.
	 * @return A Reader object, allowing the contents to be pulled from the file.
	 */
	private Reader fetchRangesAsFile(String ranges) {
		String fileName = config.getString("game.ranges.location") + ranges + ".json";
		FileReader file = null;
		ClassLoader loader = getClass().getClassLoader();
		try {
			file = new FileReader(loader.getResource(fileName).getFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return file;
	}
	
}
