package machineLearning.ngrams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class ClassicTextGram extends Gram 
{
//	
//	@Override
//	public void TransformContent(Gram type)
//	{
//		
//	}
	
	public ClassicTextGram(String value, UUID uniqueID) {
		super(value, uniqueID);
		// TODO Auto-generated constructor stub
	}
	
	public ClassicTextGram(ClassicTextGram copy) {
		super(copy);
		// TODO Auto-generated constructor stub
	}

	
	public static HashMap<UUID, ArrayList<Gram>> AnalyzeContent(Object object, HashMap<UUID, ArrayList<Gram>> currentKeys)
	{
		HashMap<UUID, ArrayList<Gram>> cont = new HashMap<UUID, ArrayList<Gram>>();
		
		//Divide the text by spaces
		String text = (String)object;
		
		//Divide the text by spaces and send it back!:
		String[] splitted = text.split(" ");
		UUID currentID = getUniqueID(splitted[0]);
		
		ClassicTextGram prev = null;
		ClassicTextGram current = null;
		ClassicTextGram next = new ClassicTextGram(splitted[0], currentID);
		
		for(int i = 0; i < splitted.length; ++i)
		{
			prev = new ClassicTextGram(current);
			current =  new ClassicTextGram(next);
			currentID = current.uniqueID;
			next = i+1 < splitted.length ? new ClassicTextGram(splitted[i+1], getUniqueID(splitted[i+1])) : null;
			
			//Now add the prev and future uuid
			if(i != splitted.length -1 )
				current.addFutureGram(getUniqueID(splitted[i + 1]));
			else
				current.addFutureGram(null);
			
			//Now add the prev and future uuid
			if(i != 0 )
				current.addPrevGram(getUniqueID(splitted[i - 1]));
			else
				current.addPrevGram(null);
			
			//we add the info into grams!
			if(!cont.containsKey(currentID))
			{
				cont.put(currentID, new ArrayList<Gram>());
				cont.get(currentID).add(current);
			}
			else
			{
				cont.get(currentID).add(current);
			}
			
			
			current.counter++;
			

		}
		
		return cont;
	}
	
	public static LinkedList<UUID> transformObjects(Object currentContent)
	{
		//Divide the text by spaces
		String text = (String)currentContent;
		LinkedList<UUID> transformedObjects = new LinkedList<UUID>();
		
		//Divide the text by spaces and send it back!:
		String[] splitted = text.split(" ");
		
		for(String split : splitted)
		{
			transformedObjects.add(getUniqueID(split));
		}
		
		return transformedObjects;
	}
	
	public static UUID getUniqueID(String text)
	{
		return UUID.nameUUIDFromBytes(text.getBytes());
	}
	
	public static UUID getUniqueID(Object contentToGet)
	{
		String translatedContent = (String)contentToGet;
		return UUID.nameUUIDFromBytes(translatedContent.getBytes());
	}
	
}
