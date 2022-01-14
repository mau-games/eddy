package util.eventrouting.events;

import game.Room;

import java.util.ArrayList;
import java.util.List;

public class MAPElitesMetricDone extends AlgorithmEvent
{
	//We shuold add information relevant to the algorithm, like time, fitness, different cells, how many reach maximum fitness, etc.

	// for starter
	private List<Room> fittestRooms;

	public MAPElitesMetricDone()
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
