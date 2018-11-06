package util.eventrouting.events;

import game.Dungeon;
import game.Room;
import util.Point;
import util.eventrouting.PCGEvent;

public class RequestPathFinding extends PCGEvent
{
	private int dungeonID;
	private Dungeon dungeon;
	private Room from;
	private Point fromPos;
	private Room to;
	private Point toPos;
	
	public RequestPathFinding(Dungeon payload, int dungeonID, Room from, Room to, Point fromPos, Point toPos)
	{
		this.dungeon = payload;
		this.dungeonID = dungeonID;
		this.from = from;
		this.to = to;
		this.fromPos = fromPos;
		this.toPos = toPos;
	}
	
	public int getDungeonID()
	{
		return dungeonID;
	}
	
	public Dungeon getDungeon()
	{
		return dungeon;
	}
	
	public Room getFromRoom()
	{
		return from;
	}
	
	public Room getToRoom()
	{
		return to;
	}
	
	public Point getFromPos()
	{
		return fromPos;
	}
	
	public Point getToPos()
	{
		return toPos;
	}
}
