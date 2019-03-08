package generator.algorithm.MAPElites;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import generator.algorithm.ZoneIndividual;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.config.GeneratorConfig;

/***
 * This Cell will contain the information of cell for MAP-Elites, which in our domain means
 * 1) Feasible population
 * 2) Infeasible population
 * 3) Fittest individual
 * 4) Dimensions
 * @author Alberto Alvarez, Malmö University
 *
 */
public class GACell
{
	protected List<ZoneIndividual> feasiblePopulation;
	protected List<ZoneIndividual> infeasiblePopulation;
	
	protected ZoneIndividual best;
	protected int maximumnCapacity = 50; //Shared between feasible and infeasible population
	
//	protected ArrayList<GADimension> cellDimensions;
	
	protected HashMap<DimensionTypes, Double> cellDimensions;
	protected int exploreCounter = 0;
	
	//Something about the dimensions (I have to do it as generic as i can so i can test a lot!)
	
	//Add the dimensions and the corresponding value of this cell 
	public GACell(ArrayList<GADimension> dimensions, int[] indices)
	{
		feasiblePopulation = new ArrayList<ZoneIndividual>();
		infeasiblePopulation = new ArrayList<ZoneIndividual>();
		cellDimensions = new HashMap<DimensionTypes, Double>();
		int index = 0;
		
		for(GADimension dimension : dimensions)
		{
			cellDimensions.put(dimension.GetType(), (double)indices[index] / dimension.GetGranularity());
			index++;
		}
		
		best = null;
	}
	
	public double GetDimensionValue(DimensionTypes dimension)
	{
		return cellDimensions.get(dimension);
	}

	public void BroadcastCellInfo()
	{
		for (Entry<DimensionTypes, Double> entry : cellDimensions.entrySet())
		{
		    System.out.print(entry.getKey().toString() + ": " + entry.getValue() + ", ");
		}
		
		System.out.print("explored: " + exploreCounter);
		
		System.out.println();
	}
	
	/**
	 * Test if the Individual belongs to this cell! 
	 * @param individual
	 * @param feasible
	 * @return
	 */
	public boolean BelongToCell(ZoneIndividual individual, boolean feasible)
	{
		//If the individual do not pass the requirements for this dimension, is false
		
		for(Entry<DimensionTypes, Double> cellDimension : cellDimensions.entrySet())
		{
			if(!GADimension.CorrectDimensionLevel(individual, cellDimension.getKey(), cellDimension.getValue()))
				return false;
		}
		
		//All your individuals belongs to me!
		if(feasible)
			feasiblePopulation.add(individual);
		else
			infeasiblePopulation.add(individual);
		
		return true;
	}
	
	public void ResetPopulationsFitness()
	{
		feasiblePopulation.forEach(ind -> ind.setEvaluate(false));
		infeasiblePopulation.forEach(ind -> ind.setEvaluate(false));
	}
	
	public void ResetPopulation(GeneratorConfig config)
	{
		feasiblePopulation.forEach(ind -> {ind.setEvaluate(false); ind.ResetPhenotype(config);});
		infeasiblePopulation.forEach(ind -> {ind.setEvaluate(false); ind.ResetPhenotype(config);});
	}
	
	public void SortPopulations(boolean ascending)
	{
		 feasiblePopulation.sort((x, y) -> (ascending ? 1 : -1) * Double.compare(x.getFitness(),y.getFitness()));
		 infeasiblePopulation.sort((x, y) -> (ascending ? 1 : -1) * Double.compare(x.getFitness(),y.getFitness()));
	}
	
	public void ApplyElitism()
	{
		int sharedCapacity = maximumnCapacity / 2;
		
		feasiblePopulation = feasiblePopulation.stream().limit(sharedCapacity).collect(Collectors.toList());
		infeasiblePopulation = infeasiblePopulation.stream().limit(sharedCapacity).collect(Collectors.toList());
	}
	
	public List<ZoneIndividual> GetFeasiblePopulation()
	{
		return feasiblePopulation;
	}
	
	public List<ZoneIndividual> GetInfeasiblePopulation()
	{
		return infeasiblePopulation;
	}
	
	public void exploreCell()
	{
		exploreCounter++;
	}
	
	public int getExploration()
	{
		return exploreCounter;
	}
}
