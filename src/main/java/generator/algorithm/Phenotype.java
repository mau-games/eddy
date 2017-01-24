package generator.algorithm;

import java.util.ArrayList;
import java.util.List;

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
				
				//TODO: This is heavily rewritten - make sure it works. 
				//This whole piece of code seems very wonky. I think this can be much improved.
				String binaryNumber = "";
				for(int j = 0; j < mGenotype.getChromosomeItemBits(); j++){
					binaryNumber += Integer.toString(mGenotype.getChromosome()[i*3 + j]);
				}
				int decimalNumber = Integer.parseInt(binaryNumber,2);
				
				map.add(decimalNumber);
			}
			
			//TODO: UH, no idea if this will come anywhere close to working			
			TileTypes[] types = (TileTypes[])map.stream().map(x -> TileTypes.values()[x]).toArray();
			
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
			
			nMap = new Map(types, Game.sizeN, game.sizeM, Game.sizeDoors, true);
		}
		return mMap;
	}
}
