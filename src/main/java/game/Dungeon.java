package game;

import java.util.ArrayList;
import java.util.Set;
import java.util.Stack;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

import generator.config.GeneratorConfig;
import util.Point;

public class Dungeon 
{	
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
	public GeneratorConfig defaultConfig;
	
	public Dungeon()
	{
		
	}
	
	public Dungeon(GeneratorConfig defaultConfig, int size, int defaultWidth, int defaultHeight)
	{
		this.id = ID_COUNTER;
		ID_COUNTER += 1;
		
		network = NetworkBuilder.undirected().allowsParallelEdges(true).build();
		
		//Create the amount of rooms with the default values -->
		this.size = size;
		this.defaultWidth = defaultWidth;
		this.defaultHeight = defaultHeight;
		this.defaultConfig = defaultConfig;
		
		//Create rooms
		rooms = new ArrayList<Room>();
		
		for(int i = 0; i < size * size; ++i)
		{
			Room auxR = new Room(defaultConfig, defaultWidth, defaultHeight);
			rooms.add(auxR);
			network.addNode(auxR);
		}
		
	}
	
	public Room getRoomByIndex(int index)
	{
		return rooms.get(index);
	}
	
	public void addRoom(int height, int width)
	{
		Room auxR = new Room(defaultConfig, height < 0 ? defaultHeight : height, width < 0 ? defaultWidth : width);
		rooms.add(auxR);
		network.addNode(auxR);
//		this.rooms.add(new Room(defaultConfig, height < 0 ? defaultHeight : height, width < 0 ? defaultWidth : width));
		this.size++;
	}
	
	//TODO: STILL INSECURE OVER NETWORK/GRAPH CODE
	//Rooms could be an ID
	public void addConnection(Room from, Room to, Point fromPosition, Point toPosition)
	{
		
		from.createDoor(fromPosition);
		to.createDoor(toPosition);
		
		//Here it should be RoomEdge the edge
		
		String testEdge = "Edge between rooms " + rooms.indexOf(from) + "---" + rooms.indexOf(to) + ", at pos: " + fromPosition;
		RoomEdge edge = new RoomEdge(from, to, fromPosition, toPosition);
		network.addEdge(from, to, edge);
		
		for(RoomEdge e : network.edges())
		{
			System.out.println(e.print());
		}
	}
	
	public ArrayList<Room> getAllRooms() { return rooms; }
	
	public void testTraverseNetwork(Room init, Room end)
	{
		Set<Room> initAdjacentRooms = network.adjacentNodes(init);
		
		for(Room r : initAdjacentRooms)
		{
			Stack<Room> steps = new Stack<Room>();
			steps.push(init);
			steps.push(r);
			
			Set<Room> adjRooms = network.adjacentNodes(r);
			adjRooms.remove(init);
			
			if(adjRooms.contains(end))
			{
				steps.push(r);
				System.out.println("REACHED");
				printRoomNumbers(steps);
			}
			else
			{
				
			}
		}
		
		int counter = 0;
		for(Room r : initAdjacentRooms)
		{
			System.out.println(counter++);
		}
	}
	
	private void printRoomNumbers(Stack<Room> rooms)
	{
		System.out.print("PATH: ");
		while(!rooms.isEmpty())
		{
			System.out.print("ROOM: " + rooms.indexOf(rooms.pop()) + ", ");
		}
		System.out.println();
//		System.out.println("PATH: ROOM: " + rooms.indexOf(rooms.pop()));
	}
}
