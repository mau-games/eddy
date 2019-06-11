package util.eventrouting.events;

import java.util.ArrayList;
import java.util.List;

import game.Room;

public class MapElitesDoneAllRooms extends AlgorithmEvent 
{
	//We shuold add information relevant to the algorithm, like time, fitness, different cells, how many reach maximum fitness, etc.
	
	// for starter
	private List<Room> population;
	
	public MapElitesDoneAllRooms()
	{
		population = new ArrayList<Room>();
	}
	
	public void addRoom(Room room)
	{
		population.add(room);
	}
	
	public List<Room> GetRooms()
	{
		return population;
	}
}
