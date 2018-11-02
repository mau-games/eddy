package game;

import util.Point;

public class PathFindingEdge implements Comparable<PathFindingEdge>
{
	public Room fromRoom;
	public Point start;
	public Point end;
	public Point positionConnectedRoom;
	public Room connectedRoom;
	public PathFindingEdge parent;
	public float hScore;
	public float value;
	
	public PathFindingEdge(Room room, Room connected, Point start, Point end, Point connectionConnectedRoom)
	{
		this.fromRoom = room;
		this.connectedRoom = connected;
		this.start = start;
		this.end = end;
		this.positionConnectedRoom = connectionConnectedRoom;
		hScore = getManhattanDistance(start, end);
		value = hScore;
	}
	
	public PathFindingEdge(PathFindingEdge parent, Room room, Room connected, Point start, Point end, Point connectionConnectedRoom)
	{
		this.parent = parent;
		this.fromRoom = room;
		this.connectedRoom = connected;
		this.start = start;
		this.end = end;
		this.positionConnectedRoom = connectionConnectedRoom;
		hScore = getManhattanDistance(start, end);
		value = hScore + this.parent.value;
	}
	
	public void updateParent(PathFindingEdge newParent)
	{
		this.parent = newParent;
		value = hScore + this.parent.value;
	}
	
	public boolean testFScore(PathFindingEdge next)
	{
		float prevScore = value;
		float newScore = next.value + hScore;
		
		return newScore < prevScore;		
	}
	
	private float getManhattanDistance(Point fromPos, Point toPos)
	{
		 return Math.abs(toPos.getX() - fromPos.getX()) + Math.abs(toPos.getY() - fromPos.getY());
	}
	
	public boolean reallyEquals(PathFindingEdge otherEdge)
	{
		return this.end.equals(otherEdge.end) && this.positionConnectedRoom.equals(otherEdge.positionConnectedRoom);
	}

	@Override
	public int compareTo(PathFindingEdge o) 
	{
		return (int) (this.value - o.value);
	}
}