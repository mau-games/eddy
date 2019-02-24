package generator.algorithm.MAPElites.Dimensions;

import game.Room;
import generator.algorithm.ZoneIndividual;

public class SymmetryGADimension extends GADimension {
	
	public SymmetryGADimension(float granularity) {
		// TODO Auto-generated constructor stub
		super();
		dimension = DimensionTypes.SYMMETRY;
		this.granularity = granularity;
	}


	@Override
	public double CalculateValue(ZoneIndividual individual, Room target) 
	{
		Room individualRoom = individual.getPhenotype().getMap(-1, -1, null, null);
    	int rowCounter = individualRoom.getRowCount();
    	int colCounter = individualRoom.getColCount();
    	int totalWalls = individualRoom.getWallCount();
    	int[][] mapMatrix = individualRoom.toMatrix();
    	
    	
    	// Vertical Symmetry Check
    	int middlePoint = rowCounter / 2;
    	int identicalVerticalSplit = 0;
    	for(int i = 0; i < middlePoint; ++i)
    	{
    		for(int j = 0; j < colCounter; ++j)
    		{
    			if(mapMatrix[i][j] == 1 && mapMatrix[rowCounter - 1 - i][j] == 1)
    			{
    				identicalVerticalSplit += 2;
    			}
    		}
    	}
    	
    	// Horizontal Symmetry Check
    	middlePoint = colCounter / 2;
    	int identicalHorizontalSplit = 0;
    	for(int i = 0; i < rowCounter; ++i)
    	{
    		for(int j = 0; j < middlePoint; ++j)
    		{
    			if(mapMatrix[i][j] == 1 && mapMatrix[i][colCounter - 1 - j] == 1)
    			{
    				identicalHorizontalSplit += 2;
    			}
    		}
    	}

    	// Frontslash Diagonal Symmetry Check
    	int identicalFrontslashDiagonalSplit = 0;
    	double k = colCounter / rowCounter;
    	for(int i = 0; i < rowCounter; ++i)
    	{
    		middlePoint = (int)(k * i);
    		for(int j = 0; j < middlePoint; ++j)
    		{
    			if(mapMatrix[i][j] == 1 && mapMatrix[colCounter - 1 - i][rowCounter - 1 - j] == 1)
    			{
    				identicalFrontslashDiagonalSplit += 2;
    			}
    		}
    	}
    	
    	// Backslash Diagonal Symmetry Check
    	int identicalBackslashDiagonalSplit = 0;
    	k = colCounter / rowCounter;
    	for(int i = 0; i < rowCounter; ++i)
    	{
    		middlePoint = (int)(k * i);
    		for(int j = 0; j < middlePoint; ++j)
    		{
    			if(mapMatrix[i][j] == 1 && mapMatrix[colCounter - 1 - i][rowCounter - 1 - j] == 1)
    			{
    				identicalBackslashDiagonalSplit += 2;
    			}
    		}
    	}
    	
    	// Find the highest symmetry
    	int highestSymmetric = 0;
    	highestSymmetric = highestSymmetric < identicalVerticalSplit ? identicalVerticalSplit : highestSymmetric;
    	highestSymmetric = highestSymmetric < identicalHorizontalSplit ? identicalHorizontalSplit : highestSymmetric;
    	highestSymmetric = highestSymmetric < identicalFrontslashDiagonalSplit ? identicalFrontslashDiagonalSplit : highestSymmetric;
    	highestSymmetric = highestSymmetric < identicalBackslashDiagonalSplit ? identicalBackslashDiagonalSplit : highestSymmetric;
    	
    	double symmetricFitness = (double)highestSymmetric / (double)totalWalls;
    	
		//logger.info("rowCounter " + rowCounter + " colCounter " + colCounter + " middlePoint " + middlePoint + " highestSymmetric " + highestSymmetric + " totalWalls " + totalWalls + " symmetricFitness " + symmetricFitness);
    	
		return symmetricFitness;

	}

}