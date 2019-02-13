package generator.algorithm.MAPElites.Dimensions;

import game.Room;
import generator.algorithm.ZoneIndividual;

public class SimilarityGADimension extends GADimension 
{
	public SimilarityGADimension(float granularity)
	{
		super();
		dimension = DimensionTypes.SIMILARITY;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(ZoneIndividual individual, Room target)
	{
		Room individualRoom = individual.getPhenotype().getMap(-1, -1, null, null);
		
    	int[][] oldMatrix = individualRoom.toMatrix();
    	int[][] newMatrix = target.toMatrix();
    	double totalTiles = individualRoom.getColCount() * individualRoom.getRowCount();
    	double similarTiles = totalTiles;
    	
    	// Calculates how many tiles that are similar between the two maps
    	//TODO: THis looks wrong!
    	for(int i = 0; i < individualRoom.getColCount(); ++i)
    	{
    		for(int j = 0; j < individualRoom.getRowCount(); ++j)
    		{
    			if(oldMatrix[j][i] != newMatrix[j][i])
    				similarTiles--;
    			
//    			switch (oldMatrix[j][i])
//    			{
//	    			case 1: // Just walls. Checking if both maps have a wall in the same place.
//	        			if(newMatrix[j][i] != 1)
//	        			{
//	        				similarTiles--;
//	        			}
//	        			break;
//        			default: // Every other floor tile. Checking if that there is no wall.
//        				if(newMatrix[j][i] == 1)
//	        			{
//	        				similarTiles--;
//	        			}
//        				break;
//    			}
    		}
    	}
    	double procentSimilar = similarTiles / totalTiles;
    	
    	// Calculates the simularityFitness with the idealProcentSimilarity to be able to control how much they change
    	double similarityFitness = 1.0;
    	similarityFitness = 1.0 - Math.abs(1.0 - procentSimilar);
//    	if(procentSimilar < idealProcentSimilarity)
//		{
//    		similarityFitness = procentSimilar / idealProcentSimilarity;
//		}
//    	else
//    	{
//    		similarityFitness = (1 - procentSimilar) / (1 - idealProcentSimilarity);    	
//    	}
    	return procentSimilar;

	}
}
