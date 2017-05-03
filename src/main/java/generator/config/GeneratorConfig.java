package generator.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import game.ApplicationConfig;
import game.TileTypes;
import util.Util;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;

public class GeneratorConfig {
	private static String defaultConfig = "config/generator_config.json";
	private ConfigurationUtility config;

	
	public GeneratorConfig() throws MissingConfigurationException{
		config = new ConfigurationUtility(defaultConfig, true);
	}
	
	public GeneratorConfig(String file) throws MissingConfigurationException{
		config = new ConfigurationUtility(file,true);
	}
	
	public int getPopulationSize(){
		return config.getInt("generator.population_size");
	}
	
	public double getMutationProbability(){
		return config.getDouble("generator.mutation_probability");
	}
	
	public double getOffspringSize(){
		return config.getDouble("generator.offspring_size");
	}
	
	public double getFeasibleProportion(){
		return config.getDouble("generator.feasible_proportion");
	}
	
	public double getRoomProportion(){
		return config.getDouble("generator.weights.room");
	}
	
	public double getCorridorProportion(){
		return config.getDouble("generator.weights.corridor");
	}
	
	public int getCorridorTargetLength(){
		return config.getInt("patterns.corridor.target_length");
	}

	public double getTurnQuality(){
		return config.getDouble("patterns.connector.turn_quality");
	}
	
	public double getIntersectionQuality(){
		return config.getDouble("patterns.connector.intersection_quality");
	}
	
	public int getChamberTargetArea(){
		return config.getInt("patterns.room.desired_area");
	}
	
	public double getChamberTargetSquareness(){
		return config.getDouble("patterns.room.squareness");
	}
	
	public double getChamberAreaCorrectness(){
		return config.getDouble("patterns.room.size");
	}
	
	public int getGenerations(){
		return config.getInt("generator.generations");
	}
	
	public double getEntranceSafety() {
		return config.getDouble("patterns.entrance.entrance_safety");
	}
	
	/**
	 * Returns the setting for entrance greed (a measure of how close to the door treasures should spawn)
	 * 
	 * @return The target entrance greed
	 */
	public double getEntranceGreed() {
		return config.getDouble("patterns.entrance.entrance_greed");
	}
	

	/**
	 * Returns the enemy quantity range.
	 * 
	 * @return The enemy quantity range.
	 */
	public double[] getEnemyQuantityRange() {
		double[] range = new double[2];

		range[0] = config.getDouble("patterns.enemy.enemies_quantity.min");
		range[1] = config.getDouble("patterns.enemy.enemies_quantity.max");

		return range;
	}

	/**
	 * Returns the treasure security variance.
	 * 
	 * @return The treasure security variance.
	 */
	public double getAverageTreasureSafety() {
		return config.getDouble("patterns.entrance.avg_treasure_safety");
	}

	/**
	 * Returns the treasure quantity range.
	 * 
	 * @return The treasure quantity range.
	 */
	public double[] getTreasureQuantityRange() {	
		double[] range = new double[2];
		
		range[0] = config.getDouble("patterns.treasure.treasures_quantity.min");
		range[1] = config.getDouble("patterns.treasure.treasures_quantity.max");

		return range;
	}

	/**
	 * Returns the target treasure safety variance.
	 * 
	 * @return The treasure security variance.
	 */
	public double getTreasureSafetyVariance() {
		return config.getDouble("patterns.entrance.treasure_safety_variance");
	}
	
	/**
	 * Get a random TileTypes weighted according to config ranges
	 * 
	 * @return A weighted random TileType
	 */
	public TileTypes getSupervisedRandomType(){	
		float value = Util.getNextFloat(0.0f,1.0f);
		if(value >= config.getDouble("tile_type_ranges.floor.from") && value < config.getDouble("tile_type_ranges.floor.to")){
			return TileTypes.FLOOR;
		} else if(value >= config.getDouble("tile_type_ranges.wall.from") && value < config.getDouble("tile_type_ranges.wall.to")){
			return TileTypes.WALL;
		} else if(value >= config.getDouble("tile_type_ranges.treasure.from") && value < config.getDouble("tile_type_ranges.treasure.to")){
			return TileTypes.TREASURE;
		} else if(value >= config.getDouble("tile_type_ranges.enemy.from") && value < config.getDouble("tile_type_ranges.enemy.to")){
			return TileTypes.ENEMY;
		}
		
		return TileTypes.WALL;
	}

}
