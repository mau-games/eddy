package machineLearning.ngrams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class Gram 
{
	public enum GramTypes
	{
		CLASSIC_TEXT,
		TILE_BY_TILE,
		COLUMN_BY_COLUMN,
		ROW_BY_ROW,
		MESO_BY_MESO,
		ROOM_BY_ROOM
	}
	
	public UUID uniqueID;
	public HashMap<UUID, Integer> prevGrams; //Count how many times each!
	public HashMap<UUID, Integer> futureGrams; //Count how many times each!
	public int counter;
	public String gramValue;
	
	public Gram(String value, UUID uniqueID)
	{
		prevGrams = new HashMap<UUID, Integer>();
		futureGrams = new HashMap<UUID, Integer>();
		gramValue = value;
		counter = 0;
		this.uniqueID = uniqueID;
	}
	
	public Gram(Gram copy)
	{
		if(copy != null)
		{
			prevGrams = new HashMap<UUID, Integer>(copy.prevGrams);
			futureGrams = new HashMap<UUID, Integer>(copy.futureGrams);
			gramValue = copy.gramValue;
			counter = copy.counter;
			this.uniqueID = copy.uniqueID;
		}
	}

	public void addPrevGram(UUID prevKey)
	{
		if(prevKey != null)
		{
			if(prevGrams.containsKey(prevKey))
				prevGrams.put(prevKey, prevGrams.get(prevKey) + 1);
			else
				prevGrams.put(prevKey, 1);
			
//			counter++;
		}	
	}

	public void addFutureGram(UUID futKey)
	{
		if(futKey != null)
		{
			if(futureGrams.containsKey(futKey))
				futureGrams.put(futKey, futureGrams.get(futKey) + 1);
			else
				futureGrams.put(futKey, 1);
			
//			counter++;
		}	
	}
	
	public static Gram gramFactory(GramTypes type, String gramValue)
	{
		switch(type)
		{
		case CLASSIC_TEXT:
			return new ClassicTextGram(gramValue, null); //this needs more!
		case COLUMN_BY_COLUMN:
			break;
		case MESO_BY_MESO:
			break;
		case ROOM_BY_ROOM:
			break;
		case ROW_BY_ROW:
			break;
		case TILE_BY_TILE:
			break;
		default:
			break;
		}
		
		return null;
	}
	
	public static HashMap<UUID, ArrayList<Gram>> gramSpecificAnalysis(GramTypes type, Object content, 
			HashMap<UUID, ArrayList<Gram>> currentKeys)
	{
		switch(type)
		{
		case CLASSIC_TEXT:
			return ClassicTextGram.AnalyzeContent(content, currentKeys);
		case COLUMN_BY_COLUMN:
			break;
		case MESO_BY_MESO:
			break;
		case ROOM_BY_ROOM:
			break;
		case ROW_BY_ROW:
			break;
		case TILE_BY_TILE:
			break;
		default:
			break;
		
		}
		
		return null;
	}
	
	public static UUID getUniqueID(GramTypes type, Object content)
	{
		switch(type)
		{
		case CLASSIC_TEXT:
			return ClassicTextGram.getUniqueID(content);
		case COLUMN_BY_COLUMN:
			break;
		case MESO_BY_MESO:
			break;
		case ROOM_BY_ROOM:
			break;
		case ROW_BY_ROW:
			break;
		case TILE_BY_TILE:
			break;
		default:
			break;
		
		}
		
		return null;
	}
	
	public static LinkedList<UUID> transformCurrentContent(GramTypes type, Object current)
	{
		switch(type)
		{
		case CLASSIC_TEXT:
			return ClassicTextGram.transformObjects(current);
		case COLUMN_BY_COLUMN:
			break;
		case MESO_BY_MESO:
			break;
		case ROOM_BY_ROOM:
			break;
		case ROW_BY_ROW:
			break;
		case TILE_BY_TILE:
			break;
		default:
			break;
		
		}
		
		return null;
	}
	
//	public void TransformContent(Gram type)
//	{
//		NGramLoader<ClassicTextGram> nGramAlgorithm = new NGramLoader<ClassicTextGram>();
//	}
	
	public static <T extends Gram> void TransformContent(T type)
    {
		
    }
}
