package machineLearning.ngrams;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import game.Room;

public class RoomColumnGram extends Gram 
{
	
	public RoomColumnGram(String value, UUID uniqueID) {
		super(value, uniqueID);
	}
	
	public RoomColumnGram(RoomColumnGram copy) {
		super(copy);
	}
	
	/***
	 * We receive an array of Rooms so we must divide them and parse each of the columns
	 * @param currentKeys
	 * @param object
	 * @return
	 */
	public static HashMap<UUID, ArrayList<Gram>> AnalyzeContent(HashMap<UUID, ArrayList<Gram>> currentKeys, Object... object)
	{
		HashMap<UUID, ArrayList<Gram>> analyzedContent = new HashMap<UUID, ArrayList<Gram>>();
		ArrayList<Room> roomsToDeconstruct = new ArrayList<Room>();
		
		ArrayList<Room> roomsToLoad = (ArrayList<Room>)object[0];
		
		//Load the rooms and put them in individual containers
		for(Room o : roomsToLoad)
		{
			roomsToDeconstruct.add(o);
		}
		
		//We create the array already with the correct amount of reserve cells
		//!!!!! TODO: IMPORTANT: This is taking for granted that all the incoming rooms will be of the same size! 
		//Might be possible to change it
		String[] columnsHolder = new String[roomsToDeconstruct.size() * roomsToDeconstruct.get(0).getColCount()];
		int currentColumnStepper = 0;
		int columnStep = roomsToDeconstruct.get(0).getColCount();
		int currentColumnStep = columnStep * currentColumnStepper;
		
		for(int i = 0; i < columnsHolder.length; i++)
		{
			columnsHolder[i] = "";
		}
		
		//Extract the columns
		for(Room room : roomsToDeconstruct)
		{
			for (int j = 0; j < room.getRowCount(); j++)
			{
				for (int i = 0; i < room.getColCount(); i++) 
				{
					columnsHolder[currentColumnStep + i] += Integer.toString(room.matrix[j][i]);
				}
			}	
			currentColumnStepper++;
			currentColumnStep = columnStep * currentColumnStepper;
		}
		
		//Get unique ID of the current gram
		UUID currentID = getUniqueID(columnsHolder[0]);
		
		//Create the 3 needed steps
		RoomColumnGram prev = null;
		RoomColumnGram current = null;
		RoomColumnGram next = new RoomColumnGram(columnsHolder[0], currentID);
		
		//Reset counters!
		currentColumnStepper = 1;
		columnStep = roomsToDeconstruct.get(0).getColCount();
		currentColumnStep = columnStep * currentColumnStepper;
		
		//NEXT becomes the CURRENT gram,
		//CURRENT becomes the PREV gram,
		//And PREV is discarded
		
		for(int i = 0; i < columnsHolder.length; ++i)
		{
			
			//This is needed because many rooms can come and they belong to different sets
			if(i == currentColumnStep)
			{
				//increase
				currentColumnStepper++;
				currentColumnStep = columnStep * currentColumnStepper;
				
				prev = null;
				currentID = getUniqueID(columnsHolder[i]);
				current =  new RoomColumnGram(columnsHolder[i], currentID);
				next = i+1 < columnsHolder.length ? new RoomColumnGram(columnsHolder[i+1], getUniqueID(columnsHolder[i+1])) : null;
			}
			else if(i == currentColumnStep-1)
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
				next = i+1 < columnsHolder.length ? new RoomColumnGram(columnsHolder[i+1], getUniqueID(columnsHolder[i+1])) : null;
			}
			
			
			//Now add the prev and future uuid
			if(i != columnsHolder.length -1 )
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
		String translatedContent = (String)contentToGet;
		translatedContent = translatedContent.toLowerCase();
		return UUID.nameUUIDFromBytes(translatedContent.getBytes());
	}
}
