package machineLearning.ngrams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import game.Room;

public class RoomTileGram extends Gram {

	public RoomTileGram(Gram copy)
	{
		super(copy);
	}
	
	public RoomTileGram(String value, UUID uniqueID)
	{
		super(value, uniqueID);
	}

	/***
	 * We receive an array of Rooms, and we need to retrieve their sequence and from there we can subdivide
	 * @param currentKeys
	 * @param object
	 * @return
	 */
	public static HashMap<UUID, ArrayList<Gram>> AnalyzeContent(HashMap<UUID, ArrayList<Gram>> currentKeys, Object... object)
	{
		HashMap<UUID, ArrayList<Gram>> analyzedContent = new HashMap<UUID, ArrayList<Gram>>();
		LinkedList<Room> roomsToDeconstruct = (LinkedList<Room>)object[0];
		
		//Get unique ID of the current gram
		UUID currentID = null;
		
		//Create the 3 needed steps
		RoomTileGram prev = null;
		RoomTileGram current = null;
		RoomTileGram next = null;
		
		for(Room mainSequenceRoom : roomsToDeconstruct)
		{
			int editionSequenceSize = mainSequenceRoom.getEditionSequence().size();
			
			currentID = getUniqueID( mainSequenceRoom.getEditionSequence().get(0).matrixToString(true));
			
			//Create the 3 needed steps
			prev = null;
			current = null;
			next = new RoomTileGram(mainSequenceRoom.getEditionSequence().get(0).matrixToString(true), currentID);
			
			for(int i = 0; i < editionSequenceSize; i++)
			{
				if(i == editionSequenceSize-1)
				{
					prev = current;
					current = next;
					currentID = current.uniqueID;
					next = null;
				}
				else
				{
					prev = current;
					current = next;
					currentID = current.uniqueID;
					next = i+1 < editionSequenceSize ? new RoomTileGram(mainSequenceRoom.getEditionSequence().get(i+1).matrixToString(true), 
							getUniqueID(mainSequenceRoom.getEditionSequence().get(i+1).matrixToString(true))) : null;
				}
				
				
				//Now add the prev and future uuid
				if(i != editionSequenceSize -1 )
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
		}
		
		
		return analyzedContent;
	}
	
	//Using rooms rather than string
//	public static LinkedList<UUID> transformObjects(Object currentContent)
//	{
//		LinkedList<Room> roomsToDeconstruct = (LinkedList<Room>)currentContent;
//		LinkedList<UUID> transformedObjects = new LinkedList<UUID>();
//		
//		for(Room room : roomsToDeconstruct)
//		{
//			transformedObjects.add(getUniqueID(room.matrixToString()));
//		}
//		
//		return transformedObjects;
//	}
	
	public static LinkedList<UUID> transformObjects(Object currentContent)
	{
		////Load the text and put everything in lowercase!
		String text = (String)currentContent;
		text = text.toLowerCase();
		
		LinkedList<UUID> transformedObjects = new LinkedList<UUID>();
		
		//Divide the text by spaces
		String[] splitted = text.split(" ");
		
		//transform to ID
		for(String split : splitted)
		{
			transformedObjects.add(getUniqueID(split));
		}
		
		return transformedObjects;
	}
	
	/***
	 * Get the unique ID from the specified text (column) 
	 * @param text In this case, the text is a column of a room
	 * @return
	 */
	public static UUID getUniqueID(String text)
	{
		text = text.toLowerCase();
		return UUID.nameUUIDFromBytes(text.getBytes());
	}
	
	/**
	 * Get the unique ID from the specified content 
	 * @param contentToGet In this case, the content is a column of a room
	 * @return Unique ID
	 */
	public static UUID getUniqueID(Object contentToGet)
	{
		Room current = (Room)contentToGet;
		String translatedContent = current.matrixToString(true);
		translatedContent = translatedContent.toLowerCase();
		return UUID.nameUUIDFromBytes(translatedContent.getBytes());
	}
}
