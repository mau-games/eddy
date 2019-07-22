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
		HashMap<UUID, ArrayList<Gram>> analyzedContent = new HashMap<UUID, ArrayList<Gram>>();
		
		//Load the text and put everything in lowercase!
		String text = (String)object;
		text = text.toLowerCase();
		
		//Divide the text by spaces:
//		String[] splitted = text.split("(?<=[,.])|(?=[,.])|\\s+");
		String[] splitted2 = text.split("(?<=[,.]) |(?<=[,.])|(?=[,.])|\\s+");
		List<String> myList = new ArrayList<String>(Arrays.asList(splitted2));
		myList.removeIf(str -> str.equals(""));
		String splitted[] =  myList.toArray(new String[0]);
		
//		myStrArray = myList.toArray(new String[0]);
		
		//Get unique ID of the current gram
		UUID currentID = getUniqueID(splitted[0]);
		
		//Create the 3 needed steps
		ClassicTextGram prev = null;
		ClassicTextGram current = null;
		ClassicTextGram next = new ClassicTextGram(splitted[0], currentID);
		
		//NEXT becomes the CURRENT gram,
		//CURRENT becomes the PREV gram,
		//And PREV is discarded
		
		for(int i = 0; i < splitted.length; ++i)
		{
			prev = current;
			current = next;
//			prev = (ClassicTextGram) next.prevGrams.getKey();
//			
//			prev = new ClassicTextGram(current);
//			current =  new ClassicTextGram(next);
			currentID = current.uniqueID;
			next = i+1 < splitted.length ? new ClassicTextGram(splitted[i+1], getUniqueID(splitted[i+1])) : null;
			
			//Now add the prev and future uuid
			if(i != splitted.length -1 )
				current.addFutureGram(next);
			else
				current.addFutureGram(null);
			
			//Now add the prev and future uuid
			if(i != 0 )
				current.addPrevGram(prev);
			else
				current.addPrevGram(null);
			
			//we add the info into grams!
			if(!analyzedContent.containsKey(currentID))
			{
				analyzedContent.put(currentID, new ArrayList<Gram>());
				analyzedContent.get(currentID).add(current);
			}
			else
			{
				analyzedContent.get(currentID).add(current);
			}
			
			
			current.counter++;
			

		}
		
		return analyzedContent;
	}
	
	public static LinkedList<UUID> transformObjects(Object currentContent)
	{
		////Load the text and put everything in lowercase!
		String text = (String)currentContent;
		text = text.toLowerCase();
		
		LinkedList<UUID> transformedObjects = new LinkedList<UUID>();
		
		//Divide the text by spaces
		String[] splitted2 = text.split("(?<=[,.]) |(?<=[,.])|(?=[,.])|\\s+");
		List<String> myList = new ArrayList<String>(Arrays.asList(splitted2));
		myList.removeIf(str -> str.equals(""));
		String splitted[] =  myList.toArray(new String[0]);
		
		//transform to ID
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
