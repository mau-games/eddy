package collectors;

import game.Dungeon;
import game.Room;
import generator.algorithm.MAPElites.MAPEliteAlgorithm;

public class XMLHandler 
{
	private static XMLHandler instance = null;
	private boolean saveInformation = false;
	
	private XMLHandler()
	{
		
	}
	
	public static XMLHandler getInstance()
	{
		if(instance == null)
		{
			instance = new XMLHandler();
		}
		
		return instance;
	}
	
	public boolean saveIndividualRoomXML(Room roomToSave, String prefix)
	{
		if(saveInformation)
		{
			roomToSave.getRoomXML(prefix);
		}
		
		return saveInformation;
	}
	
	public boolean saveMAPERoomXML(Room roomToSave, String prefix)
	{
		if(saveInformation)
		{
			roomToSave.saveRoomXMLMapElites(prefix);
		}
		
		return saveInformation;
	}
	
	public boolean saveDungeonRoomXML(Room roomToSave, String prefix)
	{
		if(saveInformation)
		{
			roomToSave.getRoomFromDungeonXML(prefix);
		}
		
		return saveInformation;
	}
	
	public boolean saveMAPEXML(MAPEliteAlgorithm algorithm)
	{
		if(saveInformation)
		{
			
			algorithm.storeMAPELITESXml();
		}
		
		return saveInformation;
	}
	
	public boolean saveDungeonXML(Dungeon dungeon)
	{
		if(saveInformation)
		{
			
			dungeon.saveDungeonXML();
		}
		
		return saveInformation;
	}
	
	
	public boolean shouldSave() {return saveInformation;}
}
