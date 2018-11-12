package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import com.google.common.graph.MutableNetwork;
import util.Point;

public class DungeonPathFinder 
{
	Dungeon owner;
	private Room provisionalStartingRoom;
	private Room provisionalEndingRoom;
	private Point provisionalStartingPoint;
	private Point provisionalEndingPoint;
	
	public ArrayList<PathFindingEdge> path;
	
	public DungeonPathFinder(Dungeon owner)
	{
		this.owner = owner;
		path = new ArrayList<PathFindingEdge>();
		
		provisionalStartingRoom = null;
		provisionalEndingRoom = null;
		provisionalStartingPoint = null;
		provisionalEndingPoint = null;
	}
	
	/**
	 * Calculate the Shortest path between two points in different rooms 
	 * @param initRoom
	 * @param goalRoom
	 * @param initPoint
	 * @param goalPoint
	 * @return if there was a path
	 */
	public boolean calculateBestPath(Room initRoom, Room goalRoom, Point initPoint, Point goalPoint, MutableNetwork<Room, RoomEdge> network)
	{
		provisionalStartingRoom = initRoom;
		provisionalEndingRoom = goalRoom;
		provisionalStartingPoint = initPoint;
		provisionalEndingPoint = goalPoint;
		
		path.clear();
		
		//Basic needs for A*
		ArrayList<PathFindingEdge> openList = new ArrayList<PathFindingEdge>();
		ArrayList<PathFindingEdge> closeList = new ArrayList<PathFindingEdge>();
		
		PathFindingEdge nextEdge = null; //Next edge to be tested
		PathFindingEdge finishEdge = null; //Final edge to consider
		
		//Special case where the pathfinding is to be done within the same room
		if(initRoom.equals(goalRoom) && initRoom.pathExists(initPoint, goalPoint))
		{
			finishEdge = new PathFindingEdge(initRoom, goalRoom, initPoint, goalPoint, null);
		}
		
		Set<RoomEdge> edges = network.incidentEdges(initRoom); //"NEIGHBORS"
		
		for(RoomEdge edge : edges)
		{
			//this is needed because edges can be stored opposite based on from where it was drawn first
			//TODO: if wanted connections that are not bidirectional then here it should be checked if EDGE is bidirectional!!!!
			Point nextRoomDoor = edge.toPosition;
			Point fromRoomDoor = edge.fromPosition;
			Room toRoom = edge.to;
			
			if(edge.to.equals(initRoom))
			{
				fromRoomDoor = edge.toPosition;
				nextRoomDoor = edge.fromPosition;
				toRoom = edge.from;
			}
			
			if(initRoom.pathExists(initPoint, fromRoomDoor))
			{
				PathFindingEdge adjacentDoor = new PathFindingEdge(initRoom, toRoom, initPoint, fromRoomDoor, nextRoomDoor);
				
				if(!closeList.contains(adjacentDoor))
					openList.add(adjacentDoor);
			}
		}

		//THIS IS WHERE A* STARTS FOR REAL
		while(!openList.isEmpty())
		{
			Collections.sort(openList);
			
			//If we already have a path to the end we discard any other that have worst (higher) score
			if(finishEdge != null)
			{
				ArrayList<PathFindingEdge> badEdges = new ArrayList<PathFindingEdge>();
				for(int i = 0; i < openList.size(); i++)
				{
					if(openList.get(i).value >= finishEdge.value)
						badEdges.add(openList.get(i));
				}
				
				for(PathFindingEdge badEdge : badEdges)
				{
					openList.remove(badEdge);
					closeList.add(badEdge);
				}
			}
			
			//Test if after the purge of bad edges we have any edge to check!
			if(openList.isEmpty())
				break;
			
			//Gather a new edge
			nextEdge = openList.remove(0);
			closeList.add(nextEdge);
			
			//If the new gathered edge is already in the goal room we may have a WINNER edge!
			//TODO: This may change due to the fact that we want to consider going in and out of the room,
			//or if the goal position is not actually reachable
			if(nextEdge.connectedRoom.equals(goalRoom) && nextEdge.connectedRoom.pathExists(nextEdge.positionConnectedRoom, goalPoint))
			{
				
				PathFindingEdge auxEdge = new PathFindingEdge(	nextEdge, 
																nextEdge.connectedRoom, 
																nextEdge.connectedRoom, 
																nextEdge.positionConnectedRoom, 
																goalPoint, 
																null);
				
				if(finishEdge == null) //If we have not yet find any solution
				{
					finishEdge = auxEdge;
				}
				else if(finishEdge.value > auxEdge.value) //If the new path is better than the old one
				{
					finishEdge = auxEdge;
				}
				
				continue;
			}

			edges = network.incidentEdges(nextEdge.connectedRoom); //"NEIGHBORS"
			
			for(RoomEdge edge : edges)
			{
				//this is needed because edges can be stored opposite based on from where it was drawn first
				//TODO: if wanted connections that are not bidirectional then here it should be checked if EDGE is bidirectional!!!!
				Point nextRoomDoor = edge.toPosition;
				Room toRoom = edge.to;
				Point thisRoomDoor = edge.fromPosition;
				
				if(edge.to.equals(nextEdge.connectedRoom))
				{
					nextRoomDoor = edge.fromPosition;
					toRoom = edge.from;
					thisRoomDoor = edge.toPosition;
				}
				
				//Not going backwards
				if(!edge.to.equals(nextEdge.fromRoom) || !edge.from.equals(nextEdge.fromRoom)) //TODO: This is for now
				{
					//new edge!
					PathFindingEdge auxEdge = new PathFindingEdge(	nextEdge, 
																	nextEdge.connectedRoom, 
																	toRoom, 
																	nextEdge.positionConnectedRoom, 
																	thisRoomDoor, 
																	nextRoomDoor);
					
					PathFindingEdge adjacentEdge = isThere(auxEdge, closeList); //If the edge have been visited already
					
					if(adjacentEdge == null)
					{
						adjacentEdge = isThere(auxEdge, openList); //Do we have this edge already pending to visit?
						
						if(adjacentEdge == null && auxEdge.fromRoom.pathExists(auxEdge.start, auxEdge.end))
						{
							openList.add(auxEdge); //Now we will visit you :D 
						}
						else if(adjacentEdge != null)//If it is pending on visiting, is it better now with this path? if it is update!
						{
							if(adjacentEdge.testFScore(nextEdge))
								adjacentEdge.updateParent(nextEdge);
						}
					}
				}
			}
		}
		
		//If we never found a path then vi ses
		if(finishEdge == null)
			return false;
		
		fillPath(finishEdge);

		return true;
	}
	
	//TODO: This method is in dungeon!
	public void calculateAllPaths(Room initRoom, Room goalRoom)
	{
		
	}
	
	private void fillPath(PathFindingEdge finishEdge)
	{
		while(finishEdge != null)
		{
			path.add(finishEdge);
			finishEdge = finishEdge.parent;
		}
	}
	
	/***
	 * Test if a list contains the edge we are testing
	 * @param edge
	 * @param list
	 * @return
	 */
	private PathFindingEdge isThere(PathFindingEdge edge, ArrayList<PathFindingEdge> list) 
	{
		for(PathFindingEdge pfe : list)
		{
			if(pfe.reallyEquals(edge))
				return pfe;
		}
		
		return null;
	}
	
	public void innerCalculation()
	{
		if(path == null)
			return;
		
		for(PathFindingEdge pfe : path)
		{
			pfe.fromRoom.applyPathfinding(pfe.start, pfe.end);
			pfe.fromRoom.paintPath(true);
		}			
	}
	
	public void printPath()
	{
		if(path != null)
		{
			for(PathFindingEdge pfe : path)
			{
				System.out.print("TO POSITION: " + pfe.end.toString() + " at ROOM (" + owner.rooms.indexOf(pfe.connectedRoom) + ") ");
				System.out.println("FROM POSITION: " + pfe.start.toString() + " FROM ROOM (" + owner.rooms.indexOf(pfe.fromRoom) + ")");
			}
		}
	}
}
