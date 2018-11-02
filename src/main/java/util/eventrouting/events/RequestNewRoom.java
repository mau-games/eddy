package util.eventrouting.events;

import game.Dungeon;
import util.eventrouting.PCGEvent;

public class RequestNewRoom extends PCGEvent 
{
	/***
	 * The dungeon which you want to create the room in. 
	 * For later when we have several dungeons
	 */
	private int dungeonID;
	private Dungeon dungeon;
	private int width;
	private int height;
	
	public RequestNewRoom(Dungeon payload, int dungeonID, int width, int height)
	{
		this.dungeon = payload;
		this.dungeonID = dungeonID;
		this.width = width;
		this.height = height;
		
		setPayload(payload);
	}
	
	public int getDungeonID()
	{
		return dungeonID;
	}
	
	public Dungeon getDungeon()
	{
		return dungeon;
	}
	
	public int getWidth()
	{
		return width;
	}
	
	public int getHeight()
	{
		return height;
	}
}
