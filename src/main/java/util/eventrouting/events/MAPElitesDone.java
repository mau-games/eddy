package util.eventrouting.events;

import java.util.ArrayList;
import java.util.List;

import game.Room;

public class MAPElitesDone extends AlgorithmEvent 
{
	//We shuold add information relevant to the algorithm, like time, fitness, different cells, how many reach maximum fitness, etc.
	
	// for starter
	private List<Room> fittestRooms;
	
	public MAPElitesDone()
	{
		fittestRooms = new ArrayList<Room>();
	}
	
	public void addRoom(Room room)
	{
		fittestRooms.add(room);
	}
	
	public List<Room> GetRooms()
	{
		return fittestRooms;
	}
}
