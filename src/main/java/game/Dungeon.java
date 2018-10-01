package game;

import java.util.ArrayList;

import generator.config.GeneratorConfig;
import util.Point;

public class Dungeon 
{
	ArrayList<Room> rooms;
	Room initialRoom;
	Room currentEditedRoom;
	Room selectedRoom;
	
	//how many rooms
	public int size;
	public int defaultWidth;
	public int defaultHeight;
	public GeneratorConfig defaultConfig;
	
	public Dungeon()
	{
		
	}
	
	public Dungeon(GeneratorConfig defaultConfig, int size, int defaultWidth, int defaultHeight)
	{
		//Create the amount of rooms with the default values -->
		this.size = size;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
		this.defaultConfig = defaultConfig;
		
		//Create rooms
		rooms = new ArrayList<Room>();
		
		for(int i = 0; i < size * size; ++i)
		{
			rooms.add(new Room(defaultConfig, defaultWidth, defaultHeight, null, null, null, null));
		}
		
	}
	
	public Room getRoomByIndex(int index)
	{
		return rooms.get(index);
	}
}
