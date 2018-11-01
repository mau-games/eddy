package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.Stack;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

import generator.config.GeneratorConfig;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.FocusRoom;
import util.eventrouting.events.RequestConnection;
import util.eventrouting.events.RequestRoomView;

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
		
		dPane = new DungeonPane(this);
		pathfinding = new DungeonPathFinder(this);
		network = NetworkBuilder.undirected().allowsParallelEdges(true).build();
		
		//Listening to events
		EventRouter.getInstance().registerListener(this, new FocusRoom(null, null));
		
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
			Room auxR = new Room(defaultConfig, defaultWidth, defaultHeight, scaleFactor);
			rooms.add(auxR);
			network.addNode(auxR);
			dPane.addVisualRoom(auxR);
		}
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
	
	public void addRoom(int height, int width)
	{
		Room auxR = new Room(defaultConfig, height < 0 ? defaultHeight : height, width < 0 ? defaultWidth : width, scaleFactor);
		rooms.add(auxR);
		network.addNode(auxR);
		dPane.addVisualRoom(auxR);
		this.size++;
	}
	
	//Rooms could be an ID
	public void addConnection(Room from, Room to, Point fromPosition, Point toPosition)
	{
		
		from.createDoor(fromPosition);
		to.createDoor(toPosition);

		RoomEdge edge = new RoomEdge(from, to, fromPosition, toPosition);
		network.addEdge(from, to, edge);
		dPane.addVisualConnector(edge);
		
		for(RoomEdge e : network.edges())
		{
			System.out.println(e.print());
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
	
	///////////////////////// TODO: TESTING TRAVERSAL AND RETRIEVAL OF ALL THE PATHS FROM A ROOM TO ANOTHER ROOM ///////////////////////////	

	Stack<Room> ConnectionPath = new Stack<Room>();
	ArrayList<Stack<Room>> connectionPaths = new ArrayList<Stack<Room>>();
	
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
