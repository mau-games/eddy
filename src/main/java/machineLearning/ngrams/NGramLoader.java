package machineLearning.ngrams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import machineLearning.ngrams.Gram.GramTypes;

public class NGramLoader
{
	public HashMap<UUID, ArrayList<Gram>> grams;
	public GramTypes gramType;
	
	public NGramLoader(GramTypes type)
	{
		grams = new HashMap<UUID, ArrayList<Gram>>();
		this.gramType = type;
	}
	
	public void addGrams(Object content)
	{
		//Divide the content based on each gram need!
		HashMap<UUID, ArrayList<Gram>> dividedContent = Gram.gramSpecificAnalysis(gramType, content, grams);
		
		for (Entry<UUID, ArrayList<Gram>> entry : dividedContent.entrySet()) 
		{
			//This is going to give problems, you know it! FIXME!
			grams.put(entry.getKey(), entry.getValue());
		}
			
//		T a = new T()
//		a.TransformContent(null);
	}
	
	public String getNGram(Object currentGram, Object currentFormed, int N)
	{
		if(N < 1)
		{
			System.out.println("N must be minimun 1 [Unigram]");
			return null;
		}
		
		//Get the UUID of the last gram created!
		UUID translatedGram = Gram.getUniqueID(gramType, currentGram);
		
		//Transform the gram that has been produced so far [into divided sections]
		LinkedList<UUID> dividedCurrentFormed = Gram.transformCurrentContent(gramType, currentFormed);
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
				
				for(UUID candidate : candidates)
				{
					UUID prev = candidate;
					UUID current = null;
	//				UUID next = null;
					
	//				UUID toGet = candidate;
					ArrayList<Gram> candidateGrams = new ArrayList<Gram>(grams.get(prev));
	//				candidateGrams = grams.get(prev);
					
					for(int j = 0; j < N-1; j++)
					{
	//					next = current;
						current = prev;
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
		
//		List<UUID> proportions = new ArrayList<UUID>(candidates);
		UUID selected = getIDFromProportion(candidates);
//		System.out.println(grams.get(selected).get(0).gramValue);
//		System.out.println(grams.get(candidates.get(0)).get(0).gramValue);
//		
//		if(selected == null) //This means that there was no candidate!
//		{
//			//then we do it again but with all candidates! a.k.a. random from distribution
//			candidates = new LinkedList<UUID>(grams.keySet());
//			selected = getIDFromProportion(candidates);
//		}
		
		returnedGram = grams.get(selected).get(0).gramValue;

		return returnedGram;
	}
	
	//Not sure if this method is correct!
	private UUID getIDFromProportion(LinkedList<UUID> currentProportions)
	{
//		List<UUID> proportions = new ArrayList<UUID>(currentProportions);
		LinkedList<Double> proportionCount = new LinkedList<Double>();
		double rndValue = Math.random();
		double totalSum = 0.0;
		double currentSum = 0.0;
		int selectedIndex = -1;
		int currentIndex = -1;
		
		for(UUID current : currentProportions)
		{
			currentIndex++;
			proportionCount.add(0.0);
			
			//TODO: Perhaps it can be simplified since each gram probably will be visit only once? counter = 1
			for(Gram g : grams.get(current))
				proportionCount.set(currentIndex, proportionCount.get(currentIndex) + g.counter);

			totalSum += proportionCount.get(currentIndex);
		}
		
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
		
		if(selectedIndex == -1)
		{
			return null;
		}
		
		return currentProportions.get(selectedIndex);
		
		
	}
	
	public static void main(String[] args)
	{
		NGramLoader nGram = new NGramLoader(GramTypes.CLASSIC_TEXT);
		
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
		nGram.addGrams("If you do a quantity challenge, the problem you'd face would be a starchy challenge."
				+ "If it has a lot of potatoes, a lot of bread or fried elements, that's difficult."
				+ "With heat challenges, challenges that use the whole pepper are much, much easier than ones that use pepper extract."
				+ "That's concentrated, and also devoid of flavour.It's just heat.");
		
		System.out.println("Original: ");
		System.out.println("If you do a quantity challenge, the problem you'd face would be a starchy challenge. "
				+ "If it has a lot of potatoes, a lot of bread or fried elements, that's difficult. "
				+ "With heat challenges, challenges that use the whole pepper are much, much easier than ones that use pepper extract. "
				+ "That's concentrated, and also devoid of flavour. It's just heat.");
		
		for(int N = 1; N < 6; N++)
		{
			String newText = "";
			String prevWord = "";
			for(int words = 0; words < 50; words++)
			{
				prevWord = nGram.getNGram(prevWord, newText, N);
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
