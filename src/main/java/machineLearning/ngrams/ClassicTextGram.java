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
	
	public ClassicTextGram(String value) {
		super(value);
		// TODO Auto-generated constructor stub
	}

	public static <T extends Gram> HashMap<UUID, Gram> AnalyzeContent(Object object, HashMap<UUID, T> currentKeys)
	{
		HashMap<UUID, Gram> cont = new HashMap<UUID, Gram>();
		
		//Divide the text by spaces
		String text = (String)object;
		
		//Divide the text by spaces and send it back!:
		String[] splitted = text.split(" ");
		
		
		for(int i = 0; i < splitted.length; ++i)
		{
			UUID id = getUniqueID(splitted[i]);
			Gram gram = (Gram) currentKeys.get(id);
			
			if(gram == null)
			{
				gram = cont.get(id);
				
				if(gram == null)
				{
					gram = new ClassicTextGram(splitted[i]);
					cont.put(id, gram);
				}
			}
			
			gram.counter++;
			
			//Now add the prev and future uuid
			if(i != splitted.length -1 )
				gram.addFutureGram(getUniqueID(splitted[i + 1]));
			else
				gram.addFutureGram(null);
			
			//Now add the prev and future uuid
			if(i != 0 )
				gram.addPrevGram(getUniqueID(splitted[i - 1]));
			else
				gram.addPrevGram(null);
				
		}
		
		return cont;
	}
	
	public static Queue<UUID> transformObjects(Object currentContent)
	{
		//Divide the text by spaces
		String text = (String)currentContent;
		Queue<UUID> transformedObjects = new LinkedList<UUID>();
		
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
