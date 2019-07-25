package machineLearning.ngrams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.UUID;

import game.Dungeon;
import game.Game;
import game.Room;
import generator.config.GeneratorConfig;
import machineLearning.ngrams.Gram.GramTypes;
import util.config.MissingConfigurationException;

public class NGramLoader
{
	public HashMap<UUID, ArrayList<Gram>> grams;
	public GramTypes gramType;
	
	public NGramLoader(GramTypes type)
	{
		grams = new HashMap<UUID, ArrayList<Gram>>();
		this.gramType = type;
	}
	
	public void addGrams(Object... content)
	{
		//Divide the content based on each gram need!
		HashMap<UUID, ArrayList<Gram>> dividedContent = Gram.gramSpecificAnalysis(gramType, grams, content);
		
		for (Entry<UUID, ArrayList<Gram>> entry : dividedContent.entrySet()) 
		{
			//This is going to give problems, you know it! FIXME!
			if(grams.containsKey(entry.getKey()))
			{
				grams.get(entry.getKey()).addAll(entry.getValue());
			}
			else
			{
				grams.put(entry.getKey(), entry.getValue());
			}
		}

	}
	
	public String getNGram(Object currentFormed, int N)
	{
		if(N < 1)
		{
			System.out.println("N must be minimun 1 [Unigram]");
			return null;
		}
		
		//Transform the gram that has been produced so far [into divided sections]
		LinkedList<UUID> dividedCurrentFormed = Gram.transformCurrentContent(gramType, currentFormed);
		
		//Get the UUID of the last gram created, that means the last one in the !
		UUID translatedGram = null;
		if(dividedCurrentFormed.isEmpty())
			translatedGram = Gram.getUniqueID(gramType, "");
		else
			translatedGram = dividedCurrentFormed.getLast();
		
		String returnedGram = "";
		
		//Sanity Check: if requested N is too big for the amount of current formed objects we resize N (and inform)
		if(dividedCurrentFormed.size() < N-1)
		{
//			System.out.format("Resizing N since Current Formed [%d] is lesser than requested N [%d]", dividedCurrentFormed.size(), N);
//			System.out.println();
			N = dividedCurrentFormed.size() + 1;
		}

		//Now we have all the grams that exist! 
		LinkedList<UUID> candidates = new LinkedList<UUID>(grams.keySet());
		
		//First we just check if we have the current gram in our grams
		if(grams.containsKey(translatedGram))
		{
			candidates.clear();
			
			while(candidates.size() == 0) //If we cannot find candidates with the current N, we reduce the N and search again!
			{
				//List to contain the disregarded candidates
				ArrayList<UUID> disregardedCandidates = new ArrayList<UUID>();
				candidates = new LinkedList<UUID>(grams.keySet());
				
				//We go through each of the candidates and start testing against their previous Gram until N-grams have been checked
				//If at the moment of looking back, one of the prev-grams do not match the right gram, the candidate is discarded.
				for(UUID candidate : candidates)
				{
					UUID prev = candidate;
					ArrayList<Gram> candidateGrams = new ArrayList<Gram>(grams.get(prev));
					
					for(int j = 0; j < N-1; j++)
					{
						prev = dividedCurrentFormed.get((dividedCurrentFormed.size() - 1) - j);
						UUID prevForLambda = prev;

						//Still working on it has a bug!
						candidateGrams.removeIf(g -> g.prevGrams.getKey() == null || !g.prevGrams.getKey().uniqueID.equals(prevForLambda)); 
						
						if(candidateGrams.isEmpty())
						{
							disregardedCandidates.add(candidate);
							break;
						}
						else
						{
							for(int i = 0; i < candidateGrams.size(); i++)
							{
								candidateGrams.set(i, candidateGrams.get(i).prevGrams.getKey());
							}
						}
					}
				}
				
				candidates.removeAll(disregardedCandidates);
				
				if(candidates.size() == 0)
				{
					System.out.println("no candidates!");
				}
				
				N = N-1;
			}
			
		}
		else
		{
			//Do not know yet what to do!
		}
		
		//Once we have gone thorugh all the candidates we choose one based on their proportion in relation with the rest of the candidates
		UUID selected = getRandomIDFromProportion(candidates);
//		UUID selected = getBestIDFromProportion(candidates);
		returnedGram = grams.get(selected).get(0).gramValue;

		return returnedGram;
	}
	
	//Not sure if this method is correct!
	private UUID getBestIDFromProportion(LinkedList<UUID> currentProportions)
	{
//			List<UUID> proportions = new ArrayList<UUID>(currentProportions);
		LinkedList<Double> proportionCount = new LinkedList<Double>();
		double maxValue = Double.MIN_VALUE;
		double totalSum = 0.0;
		int selectedIndex = -1;
		int currentIndex = -1;
		
		//We count how many times a gram is identified in the corpus 
		for(UUID current : currentProportions)
		{
			currentIndex++;
			proportionCount.add(0.0);
			
			//TODO: Perhaps it can be simplified since each gram probably will be visit only once? counter = 1
			for(Gram g : grams.get(current))
				proportionCount.set(currentIndex, proportionCount.get(currentIndex) + g.counter);

			totalSum += proportionCount.get(currentIndex);
		}
		
		//we divide all the counters to place the selected gram 
		for(int i = 0; i < proportionCount.size(); i++)
		{
			double val = proportionCount.get(i);
			val /= totalSum;
			
			if(val > maxValue)
			{
				selectedIndex = i;
				maxValue = val;
			}
		}
		
		//If there is no index, null
		if(selectedIndex == -1)
		{
			return null;
		}
		
		return currentProportions.get(selectedIndex);
	}
	
	//Not sure if this method is correct!
	private UUID getRandomIDFromProportion(LinkedList<UUID> currentProportions)
	{
//		List<UUID> proportions = new ArrayList<UUID>(currentProportions);
		LinkedList<Double> proportionCount = new LinkedList<Double>();
		double rndValue = Math.random();
		double totalSum = 0.0;
		double currentSum = 0.0;
		int selectedIndex = -1;
		int currentIndex = -1;
		
		//We count how many times a gram is identified in the corpus 
		for(UUID current : currentProportions)
		{
			currentIndex++;
			proportionCount.add(0.0);
			
			//TODO: Perhaps it can be simplified since each gram probably will be visit only once? counter = 1
			for(Gram g : grams.get(current))
				proportionCount.set(currentIndex, proportionCount.get(currentIndex) + g.counter);

			totalSum += proportionCount.get(currentIndex);
		}
		
		//we divide all the counters to place the selected gram 
		for(int i = 0; i < proportionCount.size(); i++)
		{
			double val = proportionCount.get(i);
			val /= totalSum;
			
			if(rndValue > currentSum && rndValue < currentSum + val)
			{
				selectedIndex = i;
				break;
			}
			
			currentSum += val;
		}
		
		//If there is no index, null
		if(selectedIndex == -1)
		{
			return null;
		}
		
		return currentProportions.get(selectedIndex);
	}
	
	public static void main(String[] args)
	{
		NGramLoader nGram = new NGramLoader(GramTypes.CLASSIC_TEXT);
		
//		int width = Game.defaultWidth;
//		int height = Game.defaultHeight;
//
//		GeneratorConfig gc = null;
//		try {
//			gc = new GeneratorConfig();
//
//		} catch (MissingConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		
//		Dungeon dungeonMap = new Dungeon(gc, 2, width, height);	
//		nGram.addGrams(dungeonMap.getAllRooms());
//		
//		
//		
//		NGramLoader nGram = new NGramLoader(GramTypes.CLASSIC_TEXT);
		
		//Add the content to be divided
		//nGram.addGrams("today it was a lovely day because you know, a lovely day have many days, but have more love");
//		nGram.addGrams("today it was a lovely day because it was not the same as a lovely hell");
		
//		nGram.addGrams("French cooking is really the result of peasants figuring out how to extract flavor from pedestrian ingredients."
//				+ "So most of the food that we think of as elite didn't start out that way.");
//		
//		System.out.println("Original: ");
//		System.out.println("French cooking is really the result of peasants figuring out how to extract flavor from pedestrian ingredients. "
//				+ "So most of the food that we think of as elite didn't start out that way.");
//		
//		for(int N = 1; N < 4; N++)
//		{
//			String newText = "French cooking is ";
//			String prevWord = "is";
//			for(int words = 0; words < 20; words++)
//			{
//				prevWord = nGram.getNGram(prevWord, newText, N);
//				newText += prevWord + " ";
//			}
//			
//			System.out.format("Generated Text using %d-gram: ", N);
//			System.out.println();
//			System.out.println(newText);
//		}
//		
//		nGram.addGrams("If you do a quantity challenge, the problem you'd face would be a starchy challenge."
//				+ "If it has a lot of potatoes, a lot of bread or fried elements, that's difficult."
//				+ "With heat challenges, challenges that use the whole pepper are much, much easier than ones that use pepper extract."
//				+ "That's concentrated, and also devoid of flavour.It's just heat.");
//		
//		System.out.println("Original: ");
//		System.out.println("If you do a quantity challenge, the problem you'd face would be a starchy challenge. "
//				+ "If it has a lot of potatoes, a lot of bread or fried elements, that's difficult. "
//				+ "With heat challenges, challenges that use the whole pepper are much, much easier than ones that use pepper extract. "
//				+ "That's concentrated, and also devoid of flavour. It's just heat.");
//		
//		for(int N = 1; N < 6; N++)
//		{
//			String newText = "";
//			String prevWord = "";
//			for(int words = 0; words < 50; words++)
//			{
//				prevWord = nGram.getNGram(prevWord, newText, N);
//				newText += prevWord + " ";
//			}
//			
//			System.out.format("Generated Text using %d-gram: ", N);
//			System.out.println();
//			System.out.println(newText);
//		}
		
		nGram.addGrams("EDD uses a single-objective fitness function with a FI2Pop genetic algorithm where fitness is a weighted sum divided equally between (1) the inventorial aspect of the rooms, which relates to the placement of enemies and treasures in relation to doors and target ratios, and (2) the spatial distribution of the design patterns, which relates to the distribution between corridors and rooms, and the meso-patterns that those encompass." + 
				"The overarching goal of MI-CC is to collaborate with the user to produce content, either to optimize (i.e. exploit) their current design towards some goal or to foster (i.e. explore) their creativity by surprising them with diverse proposals. By implementing MAP-Elites and continuous evolution into EDD, our algorithm can (1) account for the many dimensions that a user can be interested, (2) explore multiple areas of the search space and produce a diverse amount of high-quality suggestions to the user, and (3) still evaluate how interesting and useful the tile distribution is within a specific room. Henceforth, we name the presented approach Interactive Constrained MAP-Elites. " + 
				"MAP-Elites explores the search space more vastly by separating certain interesting dimensions, that affect different aspects of the room such as playability or visual aesthetics, from the fitness function, using them to categorize rooms into niches (cells)." + 
				"Dimensions in MAP-Elites are identified as those aspects of the individuals that can be calculated in the behavioral space, and that are independent of the fitness calculation. " + 
				"EDD offers the designer the possibility to choose among the following dimensions, two at a time:" + 
				"We choose Symmetry as a consideration of the aesthetic aspects of the edited room since symmetric structures tend to be more visually pleasing. Similarity is used to present the user variations of their design but still preserving their aesthetical edits. Symmetry is evaluated along the X and Y axes, backslash and front slash diagonal and the highest value is used as to how symmetric a room is. Similarity is calculated through comparing tile by tile with the target room." + 
				"The number of meso-patterns correlates to the type and amount of encounters the designer wants the user to have in the room in a more ordered manner. The considered patterns are the treasure room, guard rooms, and ambushes. Meso-patterns associate utility to a set of tiles in the room, for instance, a long chamber filled with enemies and treasures could be divided into 2 chambers, the first one with enemies and the second one with treasures so the risk-reward encounter is more understandable for the player. Since we already analyze the rooms for all possible patterns, the number of meso-patterns is simply presents the dimensional value, and since the used meso-patterns can only exist in a chamber, we normalize by the maximum amount of chambers in a room." + 
				"By spatial-patterns we mean chambers, corridors, connectors, and nothing. We identify the number of spatial-pattern relates to how individual tiles group (or not) together to form spatial structures in the room. The higher the amount of spatial-patterns the lesser tiles will be group together in favor of more individualism. For instance, a room with one spatial-pattern can be one with no walls and just an open chamber, while a room with a higher number of spatial-patterns would subdivide the space with walls, using tiles for more specific patterns. The equation presents how we calculate the value for such a dimension." + 
				"Linearity represents the number of paths that exist between the doors in the room. This relates to the type of gameplay the designer would like the room to have by the distributions of walls among the room. Having high linearity in a room does not need to only be by having a narrow corridor between doors but could also be generated by having all doors in the same open space (i.e. the user would not need to traverse other areas) or by simply disconnecting all paths between doors. The linearity equation shows the linearity calculation. Due to the use of patterns, we calculate the paths between doors as the number of paths that exist from a spatial-pattern containing a door to another. Finally, this is normalized by the number of spatial patterns in combination with the number of doors and their possible neighbors." + 
				"EDD implements continuous evolution in two ways. First, the EA constantly updates the target room and configuration with the most recent version of the user’s design, and once the suggestions are broadcasted, that room is incorporated without changes to the population of individuals in the corresponding cell. Secondly, by changing the dimension information and their granularity for the MAP-Elites, which can be done at any given time by the designer." + 
				"Provided that EDD already uses a FI2Pop, we took as a starting point the constrained MAP-Elites presented by Khalifa et al, where the illuminating capabilities of MAP-Elites explore the search space with the constraints aspects of FI2Pop. This approach manages two different populations within each cell, a feasible and an infeasible one. Individuals move across cells when their dimension values change, or between the feasible and infeasible population according to their fulfillment of the feasibility constraint." + 
				"");
		
		System.out.println("Original: ");
		System.out.println("EDD uses a single-objective fitness function with a FI2Pop genetic algorithm where fitness is a weighted sum divided equally between (1) the inventorial aspect of the rooms, which relates to the placement of enemies and treasures in relation to doors and target ratios, and (2) the spatial distribution of the design patterns, which relates to the distribution between corridors and rooms, and the meso-patterns that those encompass." + 
				"The overarching goal of MI-CC is to collaborate with the user to produce content, either to optimize (i.e. exploit) their current design towards some goal or to foster (i.e. explore) their creativity by surprising them with diverse proposals. By implementing MAP-Elites and continuous evolution into EDD, our algorithm can (1) account for the many dimensions that a user can be interested, (2) explore multiple areas of the search space and produce a diverse amount of high-quality suggestions to the user, and (3) still evaluate how interesting and useful the tile distribution is within a specific room. Henceforth, we name the presented approach Interactive Constrained MAP-Elites. " + 
				"MAP-Elites explores the search space more vastly by separating certain interesting dimensions, that affect different aspects of the room such as playability or visual aesthetics, from the fitness function, using them to categorize rooms into niches (cells)." + 
				"Dimensions in MAP-Elites are identified as those aspects of the individuals that can be calculated in the behavioral space, and that are independent of the fitness calculation. " + 
				"EDD offers the designer the possibility to choose among the following dimensions, two at a time:" + 
				"We choose Symmetry as a consideration of the aesthetic aspects of the edited room since symmetric structures tend to be more visually pleasing. Similarity is used to present the user variations of their design but still preserving their aesthetical edits. Symmetry is evaluated along the X and Y axes, backslash and front slash diagonal and the highest value is used as to how symmetric a room is. Similarity is calculated through comparing tile by tile with the target room." + 
				"The number of meso-patterns correlates to the type and amount of encounters the designer wants the user to have in the room in a more ordered manner. The considered patterns are the treasure room, guard rooms, and ambushes. Meso-patterns associate utility to a set of tiles in the room, for instance, a long chamber filled with enemies and treasures could be divided into 2 chambers, the first one with enemies and the second one with treasures so the risk-reward encounter is more understandable for the player. Since we already analyze the rooms for all possible patterns, the number of meso-patterns is simply presents the dimensional value, and since the used meso-patterns can only exist in a chamber, we normalize by the maximum amount of chambers in a room." + 
				"By spatial-patterns we mean chambers, corridors, connectors, and nothing. We identify the number of spatial-pattern relates to how individual tiles group (or not) together to form spatial structures in the room. The higher the amount of spatial-patterns the lesser tiles will be group together in favor of more individualism. For instance, a room with one spatial-pattern can be one with no walls and just an open chamber, while a room with a higher number of spatial-patterns would subdivide the space with walls, using tiles for more specific patterns. The equation presents how we calculate the value for such a dimension." + 
				"Linearity represents the number of paths that exist between the doors in the room. This relates to the type of gameplay the designer would like the room to have by the distributions of walls among the room. Having high linearity in a room does not need to only be by having a narrow corridor between doors but could also be generated by having all doors in the same open space (i.e. the user would not need to traverse other areas) or by simply disconnecting all paths between doors. The linearity equation shows the linearity calculation. Due to the use of patterns, we calculate the paths between doors as the number of paths that exist from a spatial-pattern containing a door to another. Finally, this is normalized by the number of spatial patterns in combination with the number of doors and their possible neighbors." + 
				"EDD implements continuous evolution in two ways. First, the EA constantly updates the target room and configuration with the most recent version of the user’s design, and once the suggestions are broadcasted, that room is incorporated without changes to the population of individuals in the corresponding cell. Secondly, by changing the dimension information and their granularity for the MAP-Elites, which can be done at any given time by the designer." + 
				"Provided that EDD already uses a FI2Pop, we took as a starting point the constrained MAP-Elites presented by Khalifa et al, where the illuminating capabilities of MAP-Elites explore the search space with the constraints aspects of FI2Pop. This approach manages two different populations within each cell, a feasible and an infeasible one. Individuals move across cells when their dimension values change, or between the feasible and infeasible population according to their fulfillment of the feasibility constraint." + 
				"");
		
		for(int N = 1; N < 10; N++)
		{
			String newText = "Map-elites ";
			String prevWord = "Map-elites";
			for(int words = 0; words < 300; words++)
			{
				prevWord = nGram.getNGram(newText, N);
				newText += prevWord + " ";
			}
			
			System.out.format("Generated Text using %d-gram: ", N);
			System.out.println();
			System.out.println(newText);
		}
		
		
//		nGram.getNGram("day", "a lovely day", 200);
	
//		//there is some issue with the keys and lists! but it is getting betteer :D 
//		for (ArrayList<Gram> values : nGram.grams.values()) 
//		{
//			for (Gram value : values) 
//			{
//				//Unigram
////				System.out.println("Prevs? " + value.prevGrams.size() + ", Future? " + value.futureGrams.size() );
//			    System.out.println(value.gramValue + ", count: " + value.counter + ", pre: ");
////			    System.out.println();
//			    //Bigram
//			    
//			    if(value.prevGrams.getKey() != null)
//			    {
//			    	ArrayList<Gram> prevs = nGram.grams.get(value.prevGrams.getKey().uniqueID);
//			    	
//			    	for (Gram prev : prevs) 
//					{
//			    		System.out.println("   " + prev.gramValue + ", count: " + 
//			    							value.prevGrams.getValue() + ",correct: " + prev.futureGrams.getKey().equals(value));
//			    		System.out.println("   " + prev.gramValue + ", count: " + 
//			    				value.prevGrams.getValue()  + ",correct: " + prev.equals(value.prevGrams.getKey()));
//			    		System.out.println("   pre:");
//			    		
//			    		if(prev.prevGrams.getKey() != null)
//			    		{
//			    			//Trigram
//			    			ArrayList<Gram> secondOrderPrevs = nGram.grams.get(prev.prevGrams.getKey().uniqueID);
//		 			    	
//		 			    	for (Gram secondOrderPrev : secondOrderPrevs) 
//		 					{
//		 			    		System.out.println("      " + secondOrderPrev.gramValue + ", count: " + 
//		 			    				secondOrderPrev.prevGrams.getValue() + ",correct: " + secondOrderPrev.futureGrams.getKey().equals(prev));
//		 			    		System.out.println("      " + secondOrderPrev.gramValue + ", count: " + 
//		 			    				secondOrderPrev.prevGrams.getValue()  + ",correct: " + secondOrderPrev.equals(prev.prevGrams.getKey()));
////		 			    		System.out.println(", pre:");
//		 					}
//			    		}
////				    	
////				    	//Trigram
////				    	for (Entry<UUID, Integer> prevEntry2 :  prev.prevGrams.entrySet()) 
////						{
////				    		ArrayList<Gram> prevs2 = nGram.grams.get(prevEntry2.getKey());
////				    		
////					    	for (Gram prev2 : prevs2) 
////							{
////						    	System.out.println("      " + prev2.gramValue + ", count: " + prevEntry2.getValue());
////							}
////
////						}
//					}
//
//			    }
//			    	
//			}
//			
//
//		    
//		}
		
//		for (Entry<UUID, Gram> entry : nGram.grams.entrySet()) 
//		{
//			grams.put(entry.getKey(), entry.getValue());
//		}
		
	}
}
