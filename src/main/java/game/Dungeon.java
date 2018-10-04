package game;

import java.util.ArrayList;

import generator.config.GeneratorConfig;
import util.Point;

public class Dungeon 
{	
	//Maybe we can add a "unique" identifier
	public static int ID_COUNTER = 0; //Probably not the best
	public int id = 0;
	
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
		this.id = ID_COUNTER;
		ID_COUNTER += 1;
		
		//Create the amount of rooms with the default values -->
		this.size = size;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
		this.defaultConfig = defaultConfig;
		
		//Create rooms
		rooms = new ArrayList<Room>();
		
		for(int i = 0; i < size * size; ++i)
		{
			rooms.add(new Room(defaultConfig, defaultWidth, defaultHeight));
		}
		
	}
	
	public Room getRoomByIndex(int index)
	{
		return rooms.get(index);
	}
	
	public void addRoom(int height, int width)
	{
		this.rooms.add(new Room(defaultConfig, height < 0 ? defaultHeight : height, width < 0 ? defaultWidth : width));
		this.size++;
	}
}
