package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.Stack;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

import finder.patterns.micro.Boss;
import game.tiles.BossEnemyTile;
import generator.config.GeneratorConfig;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.FocusRoom;
import util.eventrouting.events.RequestConnection;
import util.eventrouting.events.RequestRoomView;
import util.eventrouting.events.RequestWorldView;
import util.eventrouting.events.Stop;

/***
 * Dungeon class holds a dungeon in the world of eddy, a dungeon is comprised of:
 * + a Graphical Node (in charged of rendering rooms and edges)
 * + All the rooms (including the initial room and the currently selected room)
 * + The Graph Network that holds the internal information of connections between nodes
 * + The HighLevel Path finder (that scans the rooms to be traversed).
 * @author Alberto Alvarez, Malmö University
 *
 */
public class Dungeon implements Listener
{	
	public DungeonPane dPane;
	private DungeonPathFinder pathfinding;
	
	//Maybe we can add a "unique" identifier
	public static int ID_COUNTER = 0; //Probably not the best
	public int id = 0;
	
	public MutableNetwork<Room, RoomEdge> network; //TODO: Public for now
	
	ArrayList<Room> rooms;
	Room initialRoom;
	Point initialPos;
	Room currentEditedRoom;
	Room selectedRoom;
	
	//how many rooms
	public int size;
	public int defaultWidth;
	public int defaultHeight;
	private int defaultScaleFactor = 30;
	private int defaultMinScaleFactor = 15;
	private int defaultMaxScaleFactor = 45;
	public GeneratorConfig defaultConfig;
	
	//The start from information that is collected from the dungeon as a spatial platform for interconnected rooms.
	private ArrayList<BossEnemyTile> bosses;
	
	//scale factor of the (canvas) view
	private int scaleFactor;

	public Dungeon()
	{
		dPane = new DungeonPane(this);
		pathfinding = new DungeonPathFinder(this);
	}
	
	public Dungeon(GeneratorConfig defaultConfig, int size, int defaultWidth, int defaultHeight)
	{
		this.id = ID_COUNTER;
		ID_COUNTER += 1;
		
		//Initialize neccesary information
		bosses = new ArrayList<BossEnemyTile>();
		
		dPane = new DungeonPane(this);
		pathfinding = new DungeonPathFinder(this);
		network = NetworkBuilder.undirected().allowsParallelEdges(true).build();
		
		//Listening to events
		EventRouter.getInstance().registerListener(this, new FocusRoom(null, null));
		EventRouter.getInstance().registerListener(this, new RequestWorldView());
		
		//Create the amount of rooms with the default values -->
		this.size = size;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
		this.defaultConfig = defaultConfig;
		this.scaleFactor = defaultScaleFactor;
		
		//Create rooms
		rooms = new ArrayList<Room>();
		selectedRoom = null;
		initialRoom = null;
		currentEditedRoom = null;
		
		for(int i = 0; i < size; ++i)
		{
			Room auxR = new Room(this, defaultConfig, defaultHeight, defaultWidth, scaleFactor);
			rooms.add(auxR);
			network.addNode(auxR);
			dPane.addVisualRoom(auxR);
		}
		
		//We set this created room as the initial room
		setInitialRoom(rooms.get(0), new Point(0,0));
	}
	
	@Override
	public void ping(PCGEvent e) 
	{
		if(e instanceof FocusRoom)
		{
			FocusRoom frEvent = (FocusRoom)e;
			if(rooms.contains((frEvent.getRoom())))
			{
				selectedRoom = frEvent.getRoom();
			}
		}	
		else if (e instanceof RequestWorldView) {
			checkInterFeasible(true);
		}
	}
	
	public void editFocusedRoom()
	{
		//maybe something to test if we can/should
		if(selectedRoom == null)
			return;
		
		MapContainer mc = new MapContainer(); // this map container thingy, idk, me not like it
		mc.setMap(getSelectedRoom());
		EventRouter.getInstance().postEvent(new RequestRoomView(mc, 0, 0, null));
	}
	
	public Room getRoomByIndex(int index)
	{
		return rooms.get(index);
	}
	
	
	public void addRoom(int width, int height)
	{
		Room auxR = new Room(this, defaultConfig, height < 0 ? defaultHeight : height, width < 0 ? defaultWidth : width, scaleFactor);
		rooms.add(auxR);
		network.addNode(auxR);
		dPane.addVisualRoom(auxR);
		this.size++;
		
		if(initialRoom == null)
		{
			setInitialRoom(auxR, new Point(0,0));
		}
		
		checkInterFeasible(true);
	}
	
	/**
	 * Remove the selected room from the dungeon
	 * +++ Remove the general information the dungeon have about the room!
	 * @param roomToRemove
	 */
	public void removeRoom(Room roomToRemove)
	{
		//Actually, first remove any general info we had about the room!!!
		for(Tile custom : roomToRemove.customTiles)
		{
			if(custom instanceof BossEnemyTile)
			{
				bosses.remove(custom);
			}
		}
		
		
		//FIRST REMOVE ALL DOORS CONNECTING TO THE ROOM
		Set<RoomEdge> edgesToRemove = network.incidentEdges(roomToRemove);
		
		//Check for edges in the network and also remove visual connecting lines
		for(RoomEdge edge : edgesToRemove)
		{
			if(edge.from.equals(roomToRemove))
			{
				edge.to.removeDoor(edge.toPosition);
			}
			else if(edge.to.equals(roomToRemove))
			{
				edge.from.removeDoor(edge.fromPosition);
			}
			
			dPane.removeVisualConnector(edge);
		}
		
		//remove this room and its visual representation
		dPane.removeVisualRoom(roomToRemove);
		rooms.remove(roomToRemove);
		network.removeNode(roomToRemove);
		selectedRoom = null;
		
		//check if this was the initial room, in which case the next one should be the initial one
		if(initialRoom.equals(roomToRemove))
		{
			if(!rooms.isEmpty())
			{
				setInitialRoom(rooms.get(0), new Point(0,0));
			}
			else
			{
				setInitialRoom(null, null);
			}
			
		}
		checkInterFeasible(true);
		//probably this should be connected to the size value of the rooms LIST
		this.size--;
	}
	
	/**
	 * Remove the selected edge from the dungeon
	 * @param edgeToRemove
	 */
	public void removeEdge(RoomEdge edgeToRemove)
	{
		edgeToRemove.from.removeDoor(edgeToRemove.fromPosition);
		edgeToRemove.to.removeDoor(edgeToRemove.toPosition);
		
		//remove this room and its visual representation
		dPane.removeVisualConnector(edgeToRemove);
		network.removeEdge(edgeToRemove);
		
		//TODO: THIS IS WORK IN PROGRESS
		checkInterFeasible(true);
	}
	
	//Rooms could be an ID
	public void addConnection(Room from, Room to, Point fromPosition, Point toPosition)
	{
		
		from.createDoor(fromPosition);
		to.createDoor(toPosition);

		RoomEdge edge = new RoomEdge(from, to, fromPosition, toPosition);
		network.addEdge(from, to, edge);
		dPane.addVisualConnector(edge);
		
		//TODO: THIS IS WORK IN PROGRESS
		checkInterFeasible(true);
		
		for(RoomEdge e : network.edges())
		{
			System.out.println(e.print());
		}
	}
	
	public void setInitialRoom(Room initRoom, Point initialPos)
	{	
		this.initialRoom = initRoom;
		this.initialPos = initialPos;
		
		checkInterFeasible(true);
	}
	
	public Room getInitialRoom()
	{
		return this.initialRoom;
	}
	
	public Point getInitialPosition()
	{
		return this.initialPos;
	}
	
	//TODO: This is the method
	public void checkInterFeasible(boolean interFeasibilityCanvas)
	{
		for(Room room : rooms)
		{
			room.isInterFeasible(interFeasibilityCanvas);
		}
	}
	
	public Room getSelectedRoom()
	{
		return selectedRoom;
	}
	
	public ArrayList<Room> getAllRooms() { return rooms; }
	
	
	/**
	 * Used to scale the resolution of each individual room canvas
	 * @param value
	 * @deprecated get access to internal Dungeon pane {@link #dPane} and use {@link #dPane.tryScale(Scale)} instead
	 */
	@Deprecated
	public void scaleRoomsWorldView(int value)
	{
		if(this.scaleFactor + value > defaultMaxScaleFactor || this.scaleFactor + value < defaultMinScaleFactor)
			return;
		
		this.scaleFactor += value;
		
		for(Room room : rooms)
		{
			room.localConfig.getWorldCanvas().setViewSize(this.scaleFactor * room.getColCount(), this.scaleFactor * room.getRowCount());
		}
	}

	
	//TODO: Testing A* between rooms!!!!!!!!
	public void getBestPathBetweenRooms(Room init, Room end)
	{
		pathfinding.calculateBestPath(init, end, new Point(0,0), new Point(2,0), network);
		pathfinding.printPath();
	}
	
	public void calculateBestPath(Room init, Room end, Point initPos, Point endPos)
	{
		if(pathfinding.calculateBestPath(init, end, initPos, endPos, network))
		{
			//Clear all the paths in all the rooms
			for(Room room : rooms)
			{
				room.clearPath();
			}
			
//			pathfinding.printPath();
			pathfinding.innerCalculation();
		}
	}
	
	public void addBoss(BossEnemyTile bossTile)
	{
		bosses.add(bossTile);
	}
	
	public void replaceBoss(BossEnemyTile bossTile, BossEnemyTile prevbossTile)
	{
		bosses.remove(prevbossTile);
		bosses.add(bossTile);
	}
	
	public ArrayList<BossEnemyTile> getBosses()
	{
		return bosses;
	}
	
	///////////////////////// TODO: TESTING TRAVERSAL AND RETRIEVAL OF ALL THE PATHS FROM A ROOM TO ANOTHER ROOM ///////////////////////////	

	Stack<Room> ConnectionPath = new Stack<Room>();
	ArrayList<Stack<Room>> connectionPaths = new ArrayList<Stack<Room>>();
	
	public boolean ttNetwork(Room end)
	{
		testTraverseNetwork(initialRoom, end);
		
		if(!connectionPaths.isEmpty())
		{
			printRoomsPath();
			return true;
		}
		else
		{
			printRoomsPath();
			return false;
		}
	}
	
	public boolean traverseTillDoor(Room end, Point endPos)
	{
		return pathfinding.calculateBestPath(initialRoom, end, initialPos, endPos, network);
	}
	
	public void testTraverseNetwork(Room init, Room end)
	{
		ConnectionPath.push(init);
		Set<Room> initAdjacentRooms = network.adjacentNodes(init);
		
		for(Room r : initAdjacentRooms)
		{
			if(r == end) //Is done
			{
				Stack<Room> temp = new Stack<Room>();
				for(Room rcp : ConnectionPath)
				{
					temp.push(rcp);
//					System.out.println("ROOM " + rooms.indexOf(rcp));
				}
				temp.push(r);
//				temp.push(init);
				connectionPaths.add(temp);
			}
			else if(!ConnectionPath.contains(r))
			{
//				ConnectionPath.push(r);
				testTraverseNetwork(r, end);
				ConnectionPath.pop();
			}
			
//			Stack<Room> steps = new Stack<Room>();
//			steps.push(init);
//			steps.push(r);
//			
//			Set<Room> adjRooms = network.adjacentNodes(r);
//			adjRooms.remove(init);
//			
//			if(adjRooms.contains(end))
//			{
//				steps.push(r);
//				System.out.println("REACHED");
//				printRoomNumbers(steps);
//			}
//			else
//			{
//				
//			}
		}
		
//		int counter = 0;
//		for(Room r : initAdjacentRooms)
//		{
//			System.out.println(counter++);
//		}
	}
	
	public void printRoomsPath()
	{
		int counter = 0;
		for(Stack<Room> path : connectionPaths)
		{
			System.out.print("PATH " + counter++ + ": ");
			while(!path.isEmpty())
			{
				System.out.print("ROOM: " + rooms.indexOf(path.pop()) + ", ");
			}
			System.out.println();
		}
		
		connectionPaths.clear();
		ConnectionPath.clear();
		
//		System.out.println("PATH: ROOM: " + rooms.indexOf(rooms.pop()));
	}
	
///////////////////////// TESTING TRAVERSAL AND RETRIEVAL OF ALL THE PATHS FROM A ROOM TO ANOTHER ROOM ///////////////////////////	

}
