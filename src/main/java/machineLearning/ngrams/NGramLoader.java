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
	public HashMap<UUID, Gram> grams;
	public GramTypes gramType;
	
	public NGramLoader(GramTypes type)
	{
		grams = new HashMap<UUID, Gram>();
		this.gramType = type;
	}
	
	public void addGrams(Object content)
	{
		//Divide the content based on each gram need!
		HashMap<UUID, Gram> dividedContent = Gram.gramSpecificAnalysis(gramType, content, grams);
		
		for (Entry<UUID, Gram> entry : dividedContent.entrySet()) 
		{
			grams.put(entry.getKey(), entry.getValue());
		}
			
//		T a = new T()
//		a.TransformContent(null);
	}
	
	public String getNGram(Object currentGram, Object currentFormed, int N)
	{
		//First we just check if we have the current gram in our grams
		UUID translatedGram = Gram.getUniqueID(gramType, currentGram);
		Queue<UUID> dividedCurrentFormed = Gram.transformCurrentContent(gramType, currentFormed);
		String returnedGram = "";
		
		if(grams.containsKey(translatedGram))
		{
			//If we have the gram, we get all the future keys! and from them we do
			//this can be seen already as bi-grams! (because they are based on the previous!)
			Set<UUID> candidates = grams.get(translatedGram).futureGrams.keySet(); 
			Set<UUID> prevGrams = grams.get(translatedGram).prevGrams.keySet();
			
			for(int i = N-1; i > 0; i--) //From back to front
			{
				//Now we check, we need to do the following:
				//First and most importannt do we have enough? else get closer
				//1) get the current gram we need (unique ID)
				//2) get the nextGrams.
				//3) Filter by the current Gram
				//4) Get
			}
			
			for(int i = 0; i < N -1; i++) //From front to back
			{
				//Now we check, we need to do the following:
				//First and most importannt do we have enough? else get closer
				//1) get the current gram we need (unique ID)
				//2) get the nextGrams.
				//3) Filter by the current Gram
				//4) Get
			}
			
		}
		else
		{
			//Do not know yet what to do!
		}
		
		return returnedGram;
	}
	
	//Not sure if this method is correct!
	private UUID getIDFromProportion(Set<UUID> currentProportions)
	{
		List<UUID> proportions = new ArrayList<UUID>(currentProportions);
		LinkedList<Double> proportionCount = new LinkedList<Double>();
		double rndValue = Math.random();
		double totalSum = 0.0;
		double currentSum = 0.0;
		int selectedIndex = -1;
		
		for(UUID current : proportions)
		{
			proportionCount.add((double) grams.get(current).counter);
			totalSum += grams.get(current).counter;
		}
		
		for(int i = 0; i < proportionCount.size(); i++)
		{
			double val = proportionCount.get(i);
			val /= totalSum;
			
			if(rndValue > currentSum && rndValue < currentSum + val)
				selectedIndex = i;
		}
		
		return proportions.get(selectedIndex);
		
		
	}
	
	public static void main(String[] args)
	{
		NGramLoader nGram = new NGramLoader(GramTypes.CLASSIC_TEXT);
		
		//Add the content to be divided
		nGram.addGrams("today it was a lovely day because it was not the same as a lovely hell");
//		nGram.addGrams("today it was a lovely day because it was not the same as a lovely hell");
		
		for (Gram value : nGram.grams.values()) 
		{
			//Unigram
		    System.out.println(value.gramValue + ", count: " + value.counter + ", pre: ");
		    
		    //Bigram
		    for (Entry<UUID, Integer> prevEntry :  value.prevGrams.entrySet()) 
			{
		    	Gram prev = nGram.grams.get(prevEntry.getKey());
		    	System.out.println("   " + prev.gramValue + ", count: " + prevEntry.getValue() + ", pre:");
		    	
		    	//Trigram
		    	for (Entry<UUID, Integer> prevEntry2 :  prev.prevGrams.entrySet()) 
				{
			    	Gram prev2 = nGram.grams.get(prevEntry2.getKey());
			    	System.out.println("      " + prev2.gramValue + ", count: " + prevEntry2.getValue());
				}
		    	
			}

		    
		}
		
//		for (Entry<UUID, Gram> entry : nGram.grams.entrySet()) 
//		{
//			grams.put(entry.getKey(), entry.getValue());
//		}
		
	}
}
