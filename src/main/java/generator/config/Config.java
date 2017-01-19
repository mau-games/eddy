package generator.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class Config {

	public enum TLevel {
		EASY,
		MEDIUM,
		HARD
	}

	private JsonObject config;
	private TLevel level;

	/**
	 * Reads and applies a new profile to the current project.
	 * 
	 * @param profile The name of the profile to use.
	 */
	public Config(String profile) {
		try {
			readConfig(fetchProfileAsFile(profile));
		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * TODO: FIX THIS THING! Is the mystic Game just a dynamic setting? If
		 * so, where do we find it? And most importantly, why don't we set it
		 * dynamically? This code smells.
		 */
		// level = Game.getLevel();
	}

	/**
	 * Returns the name of the game profile.
	 * 
	 * @return The game profile name.
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public String getProfileName() throws MissingConfigurationException {
		if (config == null) {
			throw new MissingConfigurationException();
		}

		return config.get("game_name").getAsString();
	}

	/**
	 * TODO: Explain this more in detail once we understand it.
	 * 
	 * Returns the security area variance setting.
	 * 
	 * @return The security area variance setting.
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public double getSecurityAreaVariance() throws MissingConfigurationException {
		if (config == null) {
			throw new MissingConfigurationException();
		}

		return config.get("security_area").getAsJsonObject()
				.get(getDifficultyString(level)).getAsDouble();
	}

	/**
	 * TODO: Explain this more in detail once we understand it.
	 * 
	 * Returns the enemy quantity range.
	 * 
	 * @return The enemy quantity range.
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public double[] getEnemyQuantityRange() throws MissingConfigurationException {
		if (config == null) {
			throw new MissingConfigurationException();
		}

		double[] range = new double[2];

		range[0] = config.get("enemies_quantity").getAsJsonObject()
				.get(getDifficultyString(level)).getAsJsonObject()
				.get("min").getAsDouble();
		range[1] = config.get("enemies_quantity").getAsJsonObject()
				.get(getDifficultyString(level)).getAsJsonObject()
				.get("max").getAsDouble();

		return range;
	}

	/**
	 * TODO: Explain this more in detail once we understand it.
	 * 
	 * Returns the treasure security variance.
	 * 
	 * @return The treasure security variance.
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public double getAverageTreasureSecurity() throws MissingConfigurationException {
		if (config == null) {
			throw new MissingConfigurationException();
		}

		return config.get("avg_treasures_security").getAsJsonObject()
				.get(getDifficultyString(level)).getAsDouble();
	}

	/**
	 * TODO: Explain this more in detail once we understand it.
	 * 
	 * Returns the treasure quantity range.
	 * 
	 * @return The treasure quantity range.
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public double[] getTreasureQuantityRange() throws MissingConfigurationException {
		if (config == null) {
			throw new MissingConfigurationException();
		}
		
		double[] range = new double[2];
		
		range[0] = config.get("treasures_quantity").getAsJsonObject()
				.get(getDifficultyString(level)).getAsJsonObject()
				.get("min").getAsDouble();
		range[1] = config.get("treasures_quantity").getAsJsonObject()
				.get(getDifficultyString(level)).getAsJsonObject()
				.get("max").getAsDouble();

		return range;
	}

	/**
	 * TODO: Explain this more in detail once we understand it.
	 * 
	 * Returns the treasure security variance.
	 * 
	 * @return The treasure security variance.
	 * @throws MissingConfigurationException if no configuration has yet been
	 * 		 read and applied.
	 */
	public double getTreasureSecurityVariance() throws MissingConfigurationException {
		if (config == null) {
			throw new MissingConfigurationException();
		}

		return config.get("treasures_security_variance").getAsJsonObject()
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
			config = (JsonObject) parser.parse(configReader);
		} catch (JsonIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		String fileName = "profiles/" + profile + ".json";
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
	private String getDifficultyString(TLevel level) {
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
