package generator.algorithm.MAPElites;

import java.util.*;
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
	
//	protected LinkedHashMap <DimensionTypes, Double> cellDimensions;
	protected LinkedHashMap <DimensionTypes, GADimensionsGranularity> cellDimensions;
	protected LinkedHashMap <DimensionTypes, Integer> cellIndices;
	protected int exploreCounter = 0;
	protected int exploreCounterInfeasible = 0;

	protected int globalFeasibleChildrenCounter = 0;
	protected boolean selected = false;
	
	//Something about the dimensions (I have to do it as generic as i can so i can test a lot!)
	
	//Add the dimensions and the corresponding value of this cell 
	public GACell(ArrayList<GADimension> dimensions, int[] indices)
	{
		feasiblePopulation = new ArrayList<ZoneIndividual>();
		infeasiblePopulation = new ArrayList<ZoneIndividual>();
		cellDimensions = new LinkedHashMap<DimensionTypes, GADimensionsGranularity>();
		cellIndices = new LinkedHashMap<DimensionTypes, Integer>();
		int index = 0;
		
		for(GADimension dimension : dimensions)
		{
			cellDimensions.put(dimension.GetType(), 
					new GADimensionsGranularity((double)indices[index] / dimension.GetGranularity(), dimension.GetGranularity(), indices[index]));
//			cellDimensions.put(dimension.GetType(), (double)indices[index] / dimension.GetGranularity());
			cellIndices.put(dimension.GetType(), indices[index]);
			index++;
		}
		
		best = null;
	}
	
	public double GetDimensionValue(DimensionTypes dimension)
	{
		return cellDimensions.get(dimension).getDimensionValue();
	}

	public void BroadcastCellInfo()
	{
		for (Entry<DimensionTypes, GADimensionsGranularity> entry : cellDimensions.entrySet())
		{
		    System.out.print(entry.getKey().toString() + ": " + entry.getValue().getDimensionValue() + ", ");
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
		
		for(Entry<DimensionTypes, GADimensionsGranularity> cellDimension : cellDimensions.entrySet())
		{
			if(!GADimension.CorrectDimensionLevel(individual, cellDimension.getKey(), cellDimension.getValue()))
				return false;
		}
		
		//All your individuals belongs to me!
		if(feasible)
			feasiblePopulation.add(individual);
		else
			infeasiblePopulation.add(individual);

		//fixme: probably change!
		individual.belongingCell = this;

		return true;
	}
	
	public DimensionTypes getDimensionType(int axisIndex)
	{
		return null;
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

		for(int i = sharedCapacity; i < feasiblePopulation.size(); ++i)
		{
			//This only happens if you have more that maximun capacity:
			feasiblePopulation.get(i).Destructor();
		}

		for(int i = sharedCapacity; i < infeasiblePopulation.size(); ++i)
		{
			//This only happens if you have more that maximun capacity:
			infeasiblePopulation.get(i).Destructor();
		}

		List<ZoneIndividual> feas = new ArrayList<ZoneIndividual>();
		List<ZoneIndividual> infeas = new ArrayList<ZoneIndividual>();

		for(int i = 0; i < sharedCapacity && i < feasiblePopulation.size(); ++i)
		{
			//This only happens if you have more that maximun capacity:
			feas.add(feasiblePopulation.get(i));
		}

		for(int i = 0; i < sharedCapacity && i < infeasiblePopulation.size(); ++i)
		{
			//This only happens if you have more that maximun capacity:
			infeas.add(infeasiblePopulation.get(i));
		}

		feasiblePopulation = null; feasiblePopulation=feas;
		infeasiblePopulation = null; infeasiblePopulation=infeas;

//		feasiblePopulation = feasiblePopulation.stream().limit(sharedCapacity).collect(Collectors.toList());
//		infeasiblePopulation = infeasiblePopulation.stream().limit(sharedCapacity).collect(Collectors.toList());
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

	public void increaseGlobalFeasibleCount()
	{
		globalFeasibleChildrenCounter++;
	}

	public int getGlobalFeasibleCount()
	{
		return globalFeasibleChildrenCounter;
	}

	public void setSelected(boolean selected)
	{
		this.selected = selected;
	}

	public boolean getSelected()
	{
		return selected;
	}

	/***
	 * The mean reward of the cell is the avg. fitness of the population {feasible}
	 * @return
	 */
	public float getMaxRewardAvgFitness(boolean feasibleInfeasible)
	{
		float avgFit = 0.0f;

		for(ZoneIndividual feasible : feasiblePopulation)
		{
			avgFit += feasible.getFitness();
		}

		if(feasibleInfeasible)
		{
			for(ZoneIndividual infeasible : infeasiblePopulation)
			{
				avgFit += infeasible.getChildren().size();
			}

			avgFit /= ((float)feasiblePopulation.size() + (float)infeasiblePopulation.size());
		}
		else
		{
			avgFit /= (float)feasiblePopulation.size();
		}

		return avgFit;
	}

	/***
	 * The mean reward of the cell is the avg. children output of population{feasible} (at the moment)
	 * @return
	 */
	public float getMaxRewardActualExplorationSuccess(boolean feasibleInfeasible)
	{
		float exp_suc = 0.0f;

		for(ZoneIndividual feasible : feasiblePopulation)
		{
			exp_suc += feasible.getChildren().size();
		}

		if(feasibleInfeasible)
		{
			for(ZoneIndividual infeasible : infeasiblePopulation)
			{
				exp_suc += infeasible.getChildren().size();
			}
		}

		exp_suc /= (float)getExploration();
		return exp_suc;
	}

	/***
	 * The mean reward of the cell is the avg. children output of population{feasible} (in global)
	 * @return
	 */
	public float getMaxRewardGlobalExplorationSuccess(boolean feasibleInfeasible)
	{
		float exp_suc = getGlobalFeasibleCount();

		exp_suc /= (float)getExploration();
		return exp_suc;
	}

}
