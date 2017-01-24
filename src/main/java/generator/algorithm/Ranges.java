package generator.algorithm;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.Random;

import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import game.Game;
import game.TileTypes;
import generator.config.MissingConfigurationException;

//TODO: This class needs quite a bit of attention to get it working.
//TODO: I don't see why this class is handling the provision of random number generation? Break out this functionality
//TODO: Consider merging this with Config.java
public class Ranges {

	private JsonArray ranges;
	private Random random;
	
	public Ranges(){
		readRanges(fetchRangesAsFile(Game.randomRangesFileName));
		random = new Random();
	}
	
	public float getNextFloat(float min, float max){
		return min + (float)random.nextDouble()*(max - min);
	}
	
	public int getNextInt(int min, int max){
		return min + random.nextInt(max - min);
	}
	
	//Seems to get a random TileTypes weighted according to rangesSupervised.json
	public TileTypes getSupervisedRandomType(){
		float value = getNextFloat(0.0f,1.0f);
		int ranges_size = ranges.size();
		
		for(int i = 0; i < ranges_size; i++){
			if(value >= ranges.get(i).getAsJsonObject().get("from").getAsFloat() && value < ranges.get(i).getAsJsonObject().get("to").getAsFloat()){
				int index = getNextInt(0, ranges.get(i).getAsJsonObject().get("elements").getAsJsonArray().size());
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
		String fileName = "ranges/" + ranges + ".json";
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
