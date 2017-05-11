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
	private static final float mutationAmount = 0.2f;
	private ConfigurationUtility config;
	
	public GeneratorConfig() throws MissingConfigurationException{
		config = new ConfigurationUtility(defaultConfig, true);
	}
	
	public GeneratorConfig(String file) throws MissingConfigurationException{
		config = new ConfigurationUtility(file,true);
	}
	
	public GeneratorConfig(GeneratorConfig gc){
		config = new ConfigurationUtility(gc.config);
	}
	
	public void mutate(){
		mutateRoomCorridorRatio();
		mutateSquarenessSize();
		mutateBendiness();
		mutateArea();
	}
	
	public void mutateRoomCorridorRatio(){
		//wobble room/corridor proportions
		double roomProportion = getRoomProportion();
		roomProportion += Util.getNextFloat(-mutationAmount, mutationAmount);
		roomProportion = Math.min(1,Math.max(0,roomProportion));
		setRoomProportion(roomProportion);
		setCorridorProportion(1.0-roomProportion);
	}
	
	public void mutateSquarenessSize(){
		//wobble chamber squareness:size ratio
		double squareness = getChamberTargetSquareness();
		squareness += Util.getNextFloat(-mutationAmount, mutationAmount);
		squareness = Math.min(1,Math.max(0,squareness));
		setChamberTargetSquareness(squareness);
		setChamberAreaCorrectness(1.0-squareness);
	}
	
	public void mutateBendiness(){
		//wobble turn quality
		double turnQuality = getTurnQuality();
		turnQuality += Util.getNextFloat(-mutationAmount, mutationAmount);
		turnQuality = Math.min(1,Math.max(0,turnQuality));
		setTurnQuality(turnQuality);
		
		//wobble intersection quality
		double intersectionQuality = getIntersectionQuality();
		intersectionQuality += Util.getNextFloat(-mutationAmount, mutationAmount);
		intersectionQuality = Math.min(1,Math.max(0,intersectionQuality));
		setIntersectionQuality(intersectionQuality);
		
	}
	
	public void mutateArea(){
		//wobble chamber target area
		int area = getChamberTargetArea();
		area += Util.getNextInt(-5, 5);
		try {
			area = Math.min(ApplicationConfig.getInstance().getDimensionM()*ApplicationConfig.getInstance().getDimensionN(),Math.max(9,area));
		} catch (MissingConfigurationException e) {
			e.printStackTrace();
		}
		setChamberTargetArea(area);
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
	
	public void setRoomProportion(double roomProportion){
		config.updateValue("generator.weights.room",roomProportion);
	}
	
	public double getCorridorProportion(){
		return config.getDouble("generator.weights.corridor");
	}
	
	public void setCorridorProportion(double corridorProportion){
		config.updateValue("generator.weights.corridor", corridorProportion);
	}
	
	public int getCorridorTargetLength(){
		return config.getInt("patterns.corridor.target_length");
	}
	
	public void setCorridorTargetLength(int length){
		config.updateValue("patterns.corridor.target_length", length);
	}

	public double getTurnQuality(){
		return config.getDouble("patterns.connector.turn_quality");
	}
	
	public void setTurnQuality(double quality){
		config.updateValue("patterns.connector.turn_quality",quality);
	}
	
	public double getIntersectionQuality(){
		return config.getDouble("patterns.connector.intersection_quality");
	}
	
	public void setIntersectionQuality(double quality){
		config.updateValue("patterns.connector.intersection_quality",quality);
	}
	
	public int getChamberTargetArea(){
		return config.getInt("patterns.room.desired_area");
	}
	
	public void setChamberTargetArea(int area){
		config.updateValue("patterns.room.desired_area", area);
	}
	
	public double getChamberTargetSquareness(){
		return config.getDouble("patterns.room.squareness");
	}
	
	public void setChamberTargetSquareness(double squareness){
		config.updateValue("patterns.room.squareness", squareness);
	}
	
	public double getChamberAreaCorrectness(){
		return config.getDouble("patterns.room.size");
	}
	
	public void setChamberAreaCorrectness(double size){
		config.updateValue("patterns.room.size",size);
	}
	
	public int getGenerations(){
		return config.getInt("generator.generations");
	}
	
	public double getEntranceSafety() {
		return config.getDouble("patterns.entrance.entrance_safety");
	}
	
	public int getTreasureRoomTargetTreasureAmount(){
		return config.getInt("patterns.treasure_room.target_treasure_amount");
	}
	
	public int getGuardRoomTargetEnemyAmount(){
		return config.getInt("patterns.guard_room.target_enemy_amount");
	}
	
	public double getDeadEndFilledness(){
		return config.getDouble("patterns.dead_end.filledness");
	}
	
	public void setDeadEndFilledness(double filledness){
		config.updateValue("patterns.dead_end.filledness", filledness);
	}
	
	public double getDeadEndBadness(){
		return config.getDouble("patterns.dead_end.badness");
	}
	
	public void setDeadEndBadness(double badness){
		config.updateValue("patterns.dead_end.badness", badness);
	}
	
	public int getGuardedTreasureEnemies(){
		return config.getInt("patterns.guarded_treasure.target_enemy_amount");
	}
	
	public void setGuardedTreasureEnemies(int enemies){
		config.updateValue("patterns.guarded_treasure.target_enemy_amount", enemies);
	}
	
	public int getAmbushEnemies(){
		return config.getInt("patterns.ambush.target_enemy_amount");
	}
	
	public void setAmbushEnemies(int enemies){
		config.updateValue("patterns.ambush.target_enemy_amount", enemies);
	}
	
	public double getChokePointRoomToRoomQuality(){
		return config.getDouble("patterns.choke_point.room_to_room_quality");
	}
	
	public void setChokePointRoomToRoomQuality(double quality){
		config.updateValue("patterns.choke_point.room_to_room_quality", quality);
	}
	
	public double getChokePointRoomToCorridorQuality(){
		return config.getDouble("patterns.choke_point.room_to_corridor_quality");
	}
	
	public void setChokePointRoomToCorridorQuality(double quality){
		config.updateValue("patterns.choke_point.room_to_corridor_quality", quality);
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
	
	public void setEnemyQuantityRange(double min, double max){
		config.updateValue("patterns.enemy.enemies_quantity.min", min);
		config.updateValue("patterns.enemy.enemies_quantity.max", max);
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
	
	public void setTreasureQuantityRange(double min, double max){
		config.updateValue("patterns.treasure.treasures_quantity.min", min);
		config.updateValue("patterns.treasure.treasures_quantity.max", max);
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
