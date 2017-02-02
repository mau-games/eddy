package generator.algorithm;

import java.util.ArrayList;
import java.util.List;

import game.Game;
import game.Map;
import game.TileTypes;

public class Phenotype {
	Genotype mGenotype;
	Map mMap;
	
	public Phenotype(Genotype genotype){
		mGenotype = genotype;
		mMap = null;
	}
	
	public Phenotype(Map map){
		mGenotype = null;
		mMap = map;
	}
	
	public Map getMap() {
		if(mMap == null){
			int size = mGenotype.getSizeChromosome() / mGenotype.getChromosomeItemBits();
			List<Integer> map = new ArrayList<Integer>(size);
			for(int i = 0; i < size; i++){
				
				String binaryNumber = "";
				for(int j = 0; j < mGenotype.getChromosomeItemBits(); j++){
					binaryNumber += Integer.toString(mGenotype.getChromosome()[i*3 + j]);
				}
				int decimalNumber = Integer.parseInt(binaryNumber,2);
				
				map.add(decimalNumber);
			}
			
			TileTypes[] types = map.stream().map(x -> TileTypes.values()[x]).toArray(TileTypes[]::new);
			
			//TODO: Some debug crap we can ignore for now
			/*if(Game.debug){
				string s = "";
                int map_types_size = types.Count();

                for (int i = 0; i < map_types_size; i++)
                {
                    //s += types[i].ToString() + ",";
                    s += "types[" + i + "] = TYPES." + types[i].ToString() + ";" + Environment.NewLine;
                }

                File.WriteAllText("lastmap.txt", s);
			}*/
			
			mMap = new Map(types, Game.sizeN, Game.sizeM, Game.sizeDoors, true);
		}
		return mMap;
	}
}
