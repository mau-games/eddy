package generator.algorithm;

import java.util.Random;

import game.TileTypes;

//TODO: This class needs quite a bit of attention to get it working.
//TODO: I don't see why this class is handling the provision of random number generation? Break out this functionality
public class Ranges {

	//private JsonData ranges;
	private Random random;
	
	public Ranges(){
		//String json_string = File.ReadAllText(Game.dataPath + "/Resources/" + Game.randomRangesFileName + ".json");
		//ranges = JsonMapper.ToObject(json_string);
		random = new Random();
	}
	
	public float getNextFloat(float min, float max){
		//This is DEFINITELY faulty (i.e. it can generate a number < min)...
		//return (float)random.nextDouble() * max - min;
		//...so I've changed it:
		return min + (float)random.nextDouble()*(max - min);
	}
	
	public int getNextInt(int min, int max){
		return min + random.nextInt(max - min);
	}
	
	public TileTypes getSupervisedRandomType(){
		float value = getNextFloat(0.0f,1.0f);
		int ranges_size = ranges.Count;
		
		for(int i = 0; i < ranges_size; i++){
			if(value >= Float.parseFloat(ranges[i]["from"].toString()) && value < Float.parseFloat(ranges[i]["to"].toString())){
				int index = getNextInt(0, ranges[i]["elements"].Count);
				return Enum.valueOf(TileTypes, ranges[i]["elements"][index].toString())
			}
		}
		
		return TileTypes.WALL;
	}
}
