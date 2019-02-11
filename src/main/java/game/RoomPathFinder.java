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
		PathFindingTile current = new PathFindingTile(start, end, owner.getTile(start).GetType(), owner.getColCount(), owner.getRowCount());
		
		//special case that start and end are the same point
		if(start.equals(end))
		{
			path.add(current);
			return true;
		}
		
		closeList.add(current);
		
		List<Point> adjacent = owner.getAvailableCoords(current.position);
		
		for(Point adj :adjacent)
		{
			PathFindingTile adjacentTile = new PathFindingTile(current, adj, end,  owner.getTile(adj).GetType(), owner.getColCount(), owner.getRowCount());
			
			if(!closeList.contains(adjacentTile))
				openList.add(adjacentTile);
		}

		while(!openList.isEmpty())
		{
			Collections.sort(openList);
			
			current = openList.remove(0);
			closeList.add(current);
			
			if(current.position.equals(end)) //We reached the end!
			{
				reached = true;
				break;
			}
	
			adjacent = owner.getAvailableCoords(current.position);
			
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
						openList.add(new PathFindingTile(current, adj, end, owner.getTile(adj).GetType(), owner.getColCount(), owner.getRowCount()));
					}
					else
					{
						if(adjacentTile.testFScore(current))
							adjacentTile.updateTile(current);
					}
				}
			}

		}
		
		if(!reached)
			return false;

		while(current != null)
		{
			path.add(current);
			current = current.parent;
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
