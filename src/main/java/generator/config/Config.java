package generator.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import game.Game;
import util.config.ConfigurationUtility;

import util.config.MissingConfigurationException;

// TODO: Maybe merge this with the common config object?
/**
 * Config represents a configuration to be used by the generator. It is
 * game-specific, meaning that each game type should have its own dedicated
 * configuration file.
 * 
 * Even though it is possible to use non file-based configurations (e.g. by
 * calling a remote resource), the default behaviour is to look for
 * configurations in the resources/profiles directory.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class Config {

	final Logger logger = LoggerFactory.getLogger(Config.class);
	private ConfigurationUtility config;
	
	public enum DifficultyLevel {
		EASY,
		MEDIUM,
		HARD;
		
		public static DifficultyLevel parseDifficulty(String difficulty){
			DifficultyLevel level;
			if (difficulty.equals("medium")) {
				level = DifficultyLevel.MEDIUM;
			} else if (difficulty.equals("hard")) {
				level = DifficultyLevel.HARD;
			} else {
				level = DifficultyLevel.EASY;
			}
			return level;
		}
	}
	
	private JsonObject configJson;
	//TODO: Consider changing handling of difficulty level
	//to cope better with switching configs
	private DifficultyLevel level;

	/**
	 * Reads and applies a new profile to the current project.
	 * 
	 * @param profile The name of the profile to use.
	 */
	public Config(String profile) {
		try {
			config = ConfigurationUtility.getInstance();
			readConfig(fetchProfileAsFile(profile));
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't load the configuration file");
		}
		level = DifficultyLevel.parseDifficulty(config.getString("game.difficulty"));
	}

	/**
	 * Returns the name of the game profile.
	 * 
	 * @return The game profile name.
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public String getProfileName() throws MissingConfigurationException {
		if (configJson == null) {
			throw new MissingConfigurationException();
		}

		return configJson.get("game_name").getAsString();
	}

	/**
	 * Returns the target entrance safety setting.
	 * 
	 * @return The target entrance safety
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public double getEntranceSafety() throws MissingConfigurationException {
		if (configJson == null) {
			throw new MissingConfigurationException();
		}

		return configJson.get("entrance_safety").getAsJsonObject()
				.get(getDifficultyString(level)).getAsDouble();
	}
	
	
	/**
	 * Returns the setting for entrance greed (a measure of how close to the door treasures should spawn)
	 * 
	 * @return The target entrance greed
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public double getEntranceGreed() throws MissingConfigurationException {
		if (configJson == null) {
			throw new MissingConfigurationException();
		}

		return configJson.get("entrance_greed").getAsJsonObject()
				.get(getDifficultyString(level)).getAsDouble();
	}
	

	/**
	 * Returns the enemy quantity range.
	 * 
	 * @return The enemy quantity range.
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public double[] getEnemyQuantityRange() throws MissingConfigurationException {
		if (configJson == null) {
			throw new MissingConfigurationException();
		}

		double[] range = new double[2];

		range[0] = configJson.get("enemies_quantity").getAsJsonObject()
				.get(getDifficultyString(level)).getAsJsonObject()
				.get("min").getAsDouble();
		range[1] = configJson.get("enemies_quantity").getAsJsonObject()
				.get(getDifficultyString(level)).getAsJsonObject()
				.get("max").getAsDouble();

		return range;
	}

	/**
	 * Returns the treasure security variance.
	 * 
	 * @return The treasure security variance.
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public double getAverageTreasureSafety() throws MissingConfigurationException {
		if (configJson == null) {
			throw new MissingConfigurationException();
		}

		return configJson.get("avg_treasure_safety").getAsJsonObject()
				.get(getDifficultyString(level)).getAsDouble();
	}

	/**
	 * Returns the treasure quantity range.
	 * 
	 * @return The treasure quantity range.
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public double[] getTreasureQuantityRange() throws MissingConfigurationException {
		if (configJson == null) {
			throw new MissingConfigurationException();
		}
		
		double[] range = new double[2];
		
		range[0] = configJson.get("treasures_quantity").getAsJsonObject()
				.get(getDifficultyString(level)).getAsJsonObject()
				.get("min").getAsDouble();
		range[1] = configJson.get("treasures_quantity").getAsJsonObject()
				.get(getDifficultyString(level)).getAsJsonObject()
				.get("max").getAsDouble();

		return range;
	}

	/**
	 * Returns the target treasure safety variance.
	 * 
	 * @return The treasure security variance.
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public double getTreasureSafetyVariance() throws MissingConfigurationException {
		if (configJson == null) {
			throw new MissingConfigurationException();
		}

		return configJson.get("treasure_safety_variance").getAsJsonObject()
				.get(getDifficultyString(level)).getAsDouble();
	}

	/**
	 * TODO: Document the configuration file layout
	 * 
	 * Reads a configuration file.
	 * 
	 * @param configReader A reader object pointing at a JSON configuration file.
	 */
	private void readConfig(Reader configReader) {
		JsonParser parser = new JsonParser();
		try {
			configJson = (JsonObject) parser.parse(configReader);
		} catch (JsonIOException | NullPointerException e) {
			logger.error("Couldn't load the configuration file:\n" + e.getMessage());
		} catch (JsonSyntaxException e) {
			logger.error("Couldn't parse the configuration file. Is it valid?\n" + e.getMessage());
		}
	}

	/**
	 * Fetches a profile configuration file and makes it readable by the
	 * readConfig method.
	 * 
	 * The method looks for configuration profiles in the project's resource
	 * folder. As an example, the profile name "zelda" will match a file named
	 * "profiles/zelda.json". Do be careful about the casing of letters.
	 * 
	 * @param profile The name of the profile to look for.
	 * @return A Reader object, allowing the contents to be pulled from the file.
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	private Reader fetchProfileAsFile(String profile) throws MissingConfigurationException {
		String fileName = config.getString("game.profiles.location") + profile + ".json";
		FileReader file = null;
		ClassLoader loader = getClass().getClassLoader();
		try {
			file = new FileReader(loader.getResource(fileName).getFile());
		} catch (FileNotFoundException e) {
			throw new MissingConfigurationException();
		}

		return file;
	}

	/**
	 * Translates the difficulty enumeration values to human readable values.
	 * 
	 * @param level The level value to be translated.
	 * @return A string representation of the value.
	 */
	private String getDifficultyString(DifficultyLevel level) {
		switch (level) {
			case EASY:
				return "easy";
			case MEDIUM:
				return "medium";
			case HARD:
				return "hard";
			default:
				return "easy";
		}
	}
}
