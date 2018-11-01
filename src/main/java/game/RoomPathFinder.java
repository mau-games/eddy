package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import util.Point;

public class RoomPathFinder 
{
	Room owner;
	public ArrayList<PathFindingTile> path;
	public Point provisionalStart;
	public Point provisionalEnd;
	
	public RoomPathFinder(Room owner)
	{
		this.owner = owner;
		this.path = new ArrayList<PathFindingTile>();
	}
	
	public boolean calculateBestPath(Point start, Point end)
	{
		this.provisionalStart = start;
		this.provisionalEnd = end;
		boolean reached = false;
		path.clear();
		
		ArrayList<PathFindingTile> openList = new ArrayList<PathFindingTile>();
		ArrayList<PathFindingTile> closeList = new ArrayList<PathFindingTile>();
		PathFindingTile next = new PathFindingTile(start, end);
		
		closeList.add(next);
		
		List<Point> adjacent = owner.getAvailableCoords(next.position);
		
		for(Point adj :adjacent)
		{
			PathFindingTile adjacentTile = new PathFindingTile(next, adj, end);
			
			if(!closeList.contains(adjacentTile))
				openList.add(adjacentTile);
		}

		while(!openList.isEmpty())
		{
			Collections.sort(openList);
			
			next = openList.remove(0);
			closeList.add(next);
			
			if(next.position.equals(end)) //We reached the end!
			{
				reached = true;
				break;
			}
	
			adjacent = owner.getAvailableCoords(next.position);
			
			for(Point adj :adjacent)
			{
				//Check if closed list
				PathFindingTile adjacentTile = isThere(adj, closeList);
				
				if(adjacentTile == null)
				{
					//Checki if open lsit
					adjacentTile = isThere(adj, openList);
					
					if(adjacentTile == null)
					{
						openList.add(new PathFindingTile(next, adj, end));
					}
					else
					{
						if(adjacentTile.testFScore(next))
							adjacentTile.updateTile(next);
					}
				}
			}

		}
		
		if(!reached)
			return false;

		while(next != null)
		{
			path.add(next);
			next = next.parent;
		}
		
		return true;
	}
	
	private PathFindingTile isThere(Point position, ArrayList<PathFindingTile> list) 
	{
		for(PathFindingTile pft : list)
		{
			if(pft.position.equals(position))
				return pft;
		}
		
		return null;
	}
	
	public void printPath()
	{
		for(PathFindingTile step : path)
		{
			System.out.println(step.position.toString());
		}
	}
	

}
