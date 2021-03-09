package game;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import game.quest.Quest;
import game.tiles.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import generator.config.GeneratorConfig;
import gui.InteractiveGUIController;
import gui.utils.InformativePopupManager;
import gui.utils.InformativePopupManager.PresentableInformation;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;

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
	public UUID id;
	private int saveCounter = 1;
	
	public MutableNetwork<Room, RoomEdge> network; //TODO: Public for now
	private Quest quest;
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
	private ArrayList<QuestPositionUpdate> bossesPositions;
	private ArrayList<QuestPositionUpdate> enemiesPositions;
	private ArrayList<QuestPositionUpdate> npcsPositions;
	private ArrayList<QuestPositionUpdate> itemsPositions;
	private ArrayList<QuestPositionUpdate> treasuresPositions;
	private ArrayList<QuestPositionUpdate> knightPositions;
	private ArrayList<QuestPositionUpdate> wizardPositions;
	private ArrayList<QuestPositionUpdate> druidPositions;
	private ArrayList<QuestPositionUpdate> bountyhunterPositions;
	private ArrayList<QuestPositionUpdate> blacksmithPositions;
	private ArrayList<QuestPositionUpdate> merchantPositions;
	private ArrayList<QuestPositionUpdate> thiefPositions;

	
	//scale factor of the (canvas) view
	private int scaleFactor;

	public Dungeon()
	{
		dPane = new DungeonPane(this);
		pathfinding = new DungeonPathFinder(this);
		quest = new Quest(this);
		bosses = new ArrayList<BossEnemyTile>();
		bossesPositions = new ArrayList<QuestPositionUpdate>();
		enemiesPositions = new ArrayList<QuestPositionUpdate>();
		npcsPositions = new ArrayList<QuestPositionUpdate>();
		itemsPositions = new ArrayList<QuestPositionUpdate>();
		treasuresPositions = new ArrayList<QuestPositionUpdate>();
		knightPositions = new ArrayList<QuestPositionUpdate>();
		wizardPositions = new ArrayList<QuestPositionUpdate>();
		druidPositions = new ArrayList<QuestPositionUpdate>();
		bountyhunterPositions = new ArrayList<QuestPositionUpdate>();
		blacksmithPositions = new ArrayList<QuestPositionUpdate>();
		merchantPositions = new ArrayList<QuestPositionUpdate>();
		thiefPositions = new ArrayList<QuestPositionUpdate>();
	}
	
	public Dungeon(GeneratorConfig defaultConfig, int size, int defaultWidth, int defaultHeight)
	{
		this.id = UUID.randomUUID();
		ID_COUNTER += 1;
		
		//Initialize neccesary information
		bosses = new ArrayList<BossEnemyTile>();
		bossesPositions = new ArrayList<QuestPositionUpdate>();
		enemiesPositions = new ArrayList<QuestPositionUpdate>();
		npcsPositions = new ArrayList<QuestPositionUpdate>();
		itemsPositions = new ArrayList<QuestPositionUpdate>();
		treasuresPositions = new ArrayList<QuestPositionUpdate>();
		knightPositions = new ArrayList<QuestPositionUpdate>();
		wizardPositions = new ArrayList<QuestPositionUpdate>();
		druidPositions = new ArrayList<QuestPositionUpdate>();
		bountyhunterPositions = new ArrayList<QuestPositionUpdate>();
		blacksmithPositions = new ArrayList<QuestPositionUpdate>();
		merchantPositions = new ArrayList<QuestPositionUpdate>();
		thiefPositions = new ArrayList<QuestPositionUpdate>();

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

		quest = new Quest(this);
		
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
		EventRouter.getInstance().postEvent(new MapQuestUpdate());
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
		InformativePopupManager.getInstance().requestPopup(dPane, PresentableInformation.ROOMS_CONNECTED, "");
		
		ActionLogger.getInstance().storeAction(ActionType.CREATE_ROOM, 
												View.WORLD, 
												TargetPane.WORLD_MAP_PANE, 
												false,
												auxR,
												width,
												height);
		
		saveDungeonXML();
		EventRouter.getInstance().postEvent(new MapQuestUpdate());
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
			
			ActionLogger.getInstance().storeAction(ActionType.REMOVE_CONNECTION, 
													View.WORLD, 
													TargetPane.WORLD_MAP_PANE, 
													true,
													edge.from,
													edge.fromPosition,
													edge.to,
													edge.toPosition);
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
		
		ActionLogger.getInstance().storeAction(ActionType.REMOVE_ROOM, 
												View.WORLD, 
												TargetPane.WORLD_MAP_PANE, 
												false,
												roomToRemove);
		saveDungeonXML();
		EventRouter.getInstance().postEvent(new MapQuestUpdate());
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
		
		ActionLogger.getInstance().storeAction(ActionType.REMOVE_CONNECTION, 
												View.WORLD, 
												TargetPane.WORLD_MAP_PANE, 
												false,
												edgeToRemove.from,
												edgeToRemove.fromPosition,
												edgeToRemove.to,
												edgeToRemove.toPosition);
		
		saveDungeonXML();
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
		
//		for(RoomEdge e : network.edges())
//		{
//			System.out.println(e.print());
//		}
		

		ActionLogger.getInstance().storeAction(ActionType.CREATE_CONNECTION, 
												View.WORLD, 
												TargetPane.WORLD_MAP_PANE, 
												false,
												from,
												fromPosition,
												to,
												toPosition);
		
		saveDungeonXML();

	}
	
	public void setInitialRoom(Room initRoom, Point initialPos)
	{	
		if(this.initialRoom != null)
		{
			this.initialRoom.removeHero(this.initialPos);
			this.initialRoom.localConfig.getWorldCanvas().setRendered(false);
		}
		
		this.initialRoom = initRoom;
		this.initialPos = initialPos;
		
		ActionLogger.getInstance().storeAction(ActionType.CHANGE_VALUE,
												View.WORLD, 
												TargetPane.WORLD_MAP_PANE, 
												false,
												initRoom,
												initialPos
												);
		
		checkInterFeasible(true);
		
		initRoom.setHeroPosition(initialPos);
		this.initialRoom.localConfig.getWorldCanvas().setRendered(false);
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

	public void calculateAndPaintBestPath(Room init, Room end, Point initPos, Point endPos)
	{
		if(pathfinding.calculateBestPath(init, end, initPos, endPos, network))
		{
			//Clear all the paths in all the rooms
			for(Room room : rooms)
			{
//				room.clearPath();
				room.paintPath(true);
			}

//			pathfinding.printPath();
			pathfinding.innerCalculation();
		}
	}

	public void addBoss(BossEnemyTile bossTile)
	{
		System.out.println("boss added - old");
		bosses.add(bossTile);
	}

	public void removeBoss(BossEnemyTile bossTile)
	{
		System.out.println("boss removed - old");

		bosses.remove(bossTile);
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

	public void addBoss(BossEnemyTile bossTile,Room room)
	{
		System.out.println("boss added - new");
		int centerX = bossTile.GetCenterPosition().getX();
		int centerY = bossTile.GetCenterPosition().getY();
		List<finder.geometry.Point> points = new LinkedList<>();
		points.add(bossTile.GetCenterPosition());
		points.add(new finder.geometry.Point(centerX+1,centerY+1));
		points.add(new finder.geometry.Point(centerX,centerY+1));
		points.add(new finder.geometry.Point(centerX+1,centerY));
		points.add(new finder.geometry.Point(centerX-1,centerY-1));
		points.add(new finder.geometry.Point(centerX-1,centerY));
		points.add(new finder.geometry.Point(centerX,centerY-1));
		points.add(new finder.geometry.Point(centerX+1,centerY-1));
		points.add(new finder.geometry.Point(centerX-1,centerY+1));
		room.bossTiles.AddAllPoints(points);
		bossesPositions.add(new QuestPositionUpdate(bossTile.GetCenterPosition(), room, false));
	}

	public void removeBoss(BossEnemyTile tile, Room room)
	{
		System.out.println("boss removed - new");
		bossesPositions.removeIf(bossEnemyTile -> tile.GetCenterPosition().getY() == tile.GetCenterPosition().getY() &&
				tile.GetCenterPosition().getX() == tile.GetCenterPosition().getX());
	}

	public void replaceBoss(BossEnemyTile bossTile, BossEnemyTile prevbossTile, Room room)
	{
		removeBoss(prevbossTile,room);
		addBoss(bossTile,room);
	}

	public ArrayList<QuestPositionUpdate> getBossesPositions()
	{
		return bossesPositions;
	}

	public void addEnemy(EnemyTile enemyTile, Room room)
	{
		room.enemyTiles.addPoint(enemyTile.GetCenterPosition());
		enemiesPositions.add(new QuestPositionUpdate(enemyTile.GetCenterPosition(),room, false));
	}

	public void removeEnemy(Tile tile, Room room)
	{
		room.enemyTiles.getPoints().removeIf(point -> point.getY() == tile.GetCenterPosition().getY() &&
				point.getX() == tile.GetCenterPosition().getX());
		enemiesPositions.removeIf(enemyTiles -> enemyTiles.getPoint().getX() == tile.GetCenterPosition().getX() &&
				enemyTiles.getPoint().getY() == tile.GetCenterPosition().getY());
	}


	public ArrayList<QuestPositionUpdate> getEnemies()
	{
		return enemiesPositions;
	}
	
	public void addThief(ThiefTile thiefTile, Room room)
	{
		room.thiefTiles.addPoint(thiefTile.GetCenterPosition());
		thiefPositions.add(new QuestPositionUpdate(thiefTile.GetCenterPosition(),room, false));
	}
	
	public void removeThief(Tile tile, Room room)
	{
		System.out.println("npc removed");
		room.thiefTiles.getPoints().removeIf(point -> point.getY() == tile.GetCenterPosition().getY() &&
				point.getX() == tile.GetCenterPosition().getX());
		thiefPositions.removeIf(npctile -> npctile.getPoint().getX() == tile.GetCenterPosition().getX() &&
				npctile.getPoint().getY() == tile.GetCenterPosition().getY());
	}
	
	public ArrayList<QuestPositionUpdate> getThiefs()
	{
		return thiefPositions;
	}
	
	public void addMerchant(MerchantTile merchantTile, Room room)
	{
		room.merchantTiles.addPoint(merchantTile.GetCenterPosition());
		merchantPositions.add(new QuestPositionUpdate(merchantTile.GetCenterPosition(),room, false));
	}
	
	public void removeMerchant(Tile tile, Room room)
	{
		System.out.println("npc removed");
		room.merchantTiles.getPoints().removeIf(point -> point.getY() == tile.GetCenterPosition().getY() &&
				point.getX() == tile.GetCenterPosition().getX());
		merchantPositions.removeIf(npctile -> npctile.getPoint().getX() == tile.GetCenterPosition().getX() &&
				npctile.getPoint().getY() == tile.GetCenterPosition().getY());
	}
	
	public ArrayList<QuestPositionUpdate> getMerchants()
	{
		return merchantPositions;
	}
	
	public void addBlacksmith(BlacksmithTile blacksmithTile, Room room)
	{
		room.blacksmithTiles.addPoint(blacksmithTile.GetCenterPosition());
		blacksmithPositions.add(new QuestPositionUpdate(blacksmithTile.GetCenterPosition(),room, false));
	}
	
	public void removeBlacksmith(Tile tile, Room room)
	{
		System.out.println("npc removed");
		room.blacksmithTiles.getPoints().removeIf(point -> point.getY() == tile.GetCenterPosition().getY() &&
				point.getX() == tile.GetCenterPosition().getX());
		blacksmithPositions.removeIf(npctile -> npctile.getPoint().getX() == tile.GetCenterPosition().getX() &&
				npctile.getPoint().getY() == tile.GetCenterPosition().getY());
	}
	
	public ArrayList<QuestPositionUpdate> getBlacksmiths()
	{
		return blacksmithPositions;
	}
	
	public void addBountyhunter(BountyhunterTile bountyhunterTile, Room room)
	{
		room.bountyhunterTiles.addPoint(bountyhunterTile.GetCenterPosition());
		bountyhunterPositions.add(new QuestPositionUpdate(bountyhunterTile.GetCenterPosition(),room, false));
	}
	
	public void removeBountyhunter(Tile tile, Room room)
	{
		System.out.println("npc removed");
		room.bountyhunterTiles.getPoints().removeIf(point -> point.getY() == tile.GetCenterPosition().getY() &&
				point.getX() == tile.GetCenterPosition().getX());
		bountyhunterPositions.removeIf(npctile -> npctile.getPoint().getX() == tile.GetCenterPosition().getX() &&
				npctile.getPoint().getY() == tile.GetCenterPosition().getY());
	}
	
	public ArrayList<QuestPositionUpdate> getBountyHunters()
	{
		return bountyhunterPositions;
	}
	
	public void addDruid(DruidTile druidTile, Room room)
	{
		room.druidTiles.addPoint(druidTile.GetCenterPosition());
		druidPositions.add(new QuestPositionUpdate(druidTile.GetCenterPosition(),room, false));
	}
	
	public void removeDruid(Tile tile, Room room)
	{
		System.out.println("npc removed");
		room.druidTiles.getPoints().removeIf(point -> point.getY() == tile.GetCenterPosition().getY() &&
				point.getX() == tile.GetCenterPosition().getX());
		druidPositions.removeIf(npctile -> npctile.getPoint().getX() == tile.GetCenterPosition().getX() &&
				npctile.getPoint().getY() == tile.GetCenterPosition().getY());
	}
	
	public ArrayList<QuestPositionUpdate> getDruids()
	{
		return druidPositions;
	}
	
	public void addWizard(WizardTile wizardTile, Room room)
	{
		room.wizardTiles.addPoint(wizardTile.GetCenterPosition());
		wizardPositions.add(new QuestPositionUpdate(wizardTile.GetCenterPosition(),room, false));
	}
	
	public void removeWizard(Tile tile, Room room)
	{
		System.out.println("npc removed");
		room.wizardTiles.getPoints().removeIf(point -> point.getY() == tile.GetCenterPosition().getY() &&
				point.getX() == tile.GetCenterPosition().getX());
		wizardPositions.removeIf(npctile -> npctile.getPoint().getX() == tile.GetCenterPosition().getX() &&
				npctile.getPoint().getY() == tile.GetCenterPosition().getY());
	}
	
	public ArrayList<QuestPositionUpdate> getWizards()
	{
		return wizardPositions;
	}
	
	public void addKnight(KnightTile knightTile, Room room)
	{
		room.knightTiles.addPoint(knightTile.GetCenterPosition());
		knightPositions.add(new QuestPositionUpdate(knightTile.GetCenterPosition(),room, false));
	}
	
	public void removeKnight(Tile tile, Room room)
	{
		System.out.println("npc removed");
		room.knightTiles.getPoints().removeIf(point -> point.getY() == tile.GetCenterPosition().getY() &&
				point.getX() == tile.GetCenterPosition().getX());
		knightPositions.removeIf(npctile -> npctile.getPoint().getX() == tile.GetCenterPosition().getX() &&
				npctile.getPoint().getY() == tile.GetCenterPosition().getY());
	}
	
	public ArrayList<QuestPositionUpdate> getKnights()
	{
		return knightPositions;
	}

	public void addNpc(NpcTile npcTile, Room room)
	{
		room.npcTiles.addPoint(npcTile.GetCenterPosition());
		npcsPositions.add(new QuestPositionUpdate(npcTile.GetCenterPosition(),room, false));
	}

	public void removeNpc(Tile tile, Room room)
	{
		System.out.println("npc removed");
		room.npcTiles.getPoints().removeIf(point -> point.getY() == tile.GetCenterPosition().getY() &&
				point.getX() == tile.GetCenterPosition().getX());
		npcsPositions.removeIf(npctile -> npctile.getPoint().getX() == tile.GetCenterPosition().getX() &&
				npctile.getPoint().getY() == tile.GetCenterPosition().getY());
	}

	public ArrayList<QuestPositionUpdate> getNpcs()
	{
		return npcsPositions;
	}

	public void addItem(ItemTile itemTile, Room room)
	{
		System.out.println("item added");
		room.itemTiles.addPoint(itemTile.GetCenterPosition());
		itemsPositions.add(new QuestPositionUpdate(itemTile.GetCenterPosition(),room,false));
	}

	public void removeItem(Tile tile, Room room)
	{
		room.itemTiles.getPoints().removeIf(point -> point.getY() == tile.GetCenterPosition().getY() &&
				point.getX() == tile.GetCenterPosition().getX());
		itemsPositions.removeIf(itemTile -> itemTile.getPoint().getX() == tile.GetCenterPosition().getX() &&
				itemTile.getPoint().getY() == tile.GetCenterPosition().getY());
	}

	public ArrayList<QuestPositionUpdate> getItems() {
		return itemsPositions;
	}

	public void addTreasure(TreasureTile treasureTile, Room room)
	{
		System.out.println("treasure added");
		room.treasureTiles.addPoint(treasureTile.GetCenterPosition());
		treasuresPositions.add(new QuestPositionUpdate(treasureTile.GetCenterPosition(), room, false));
	}

	public void removeTreasure(Tile tile, Room room)
	{
		room.treasureTiles.getPoints().removeIf(point -> point.getY() == tile.GetCenterPosition().getY() &&
				point.getX() == tile.GetCenterPosition().getX());
		treasuresPositions.removeIf(treasureTile -> treasureTile.getPoint().getX() == tile.GetCenterPosition().getX() &&
				treasureTile.getPoint().getY() == tile.GetCenterPosition().getY());
	}

	public ArrayList<QuestPositionUpdate> getTreasures() {
		return treasuresPositions;
	}
	
	public int getAllNpcs()
	{
		int exactNumberOfNpcs;
		exactNumberOfNpcs = getKnights().size() + getWizards().size() + getDruids().size() + getBountyHunters().size() + getBlacksmiths().size() + getMerchants().size() + getThiefs().size();
		return exactNumberOfNpcs;
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

	//TODO: use or tweak for togglePath
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
	
	public void saveDungeonXML()
	{
		Document dom;
	    Element e = null;
	    Element next = null;
	    String xml = System.getProperty("user.dir") + "\\my-data\\summer-school\\" + InteractiveGUIController.runID + "\\dungeon\\dungeon-" + this.id.toString() + "_" + saveCounter++ + ".xml";

	    // instance of a DocumentBuilderFactory
	    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    try {
	        // use factory to get an instance of document builder
	        DocumentBuilder db = dbf.newDocumentBuilder();
	        // create instance of DOM
	        dom = db.newDocument();

	        // create the root element
	        Element rootEle = dom.createElement("Dungeon");
	        rootEle.setAttribute("ID",  this.id.toString());
//	        rootEle.setAttribute("TIME", this.toString());
//	        rootEle.setAttribute("type", "SUGGESTIONS OR MAIN");
	        
	        // create data elements and place them under root
	        e = dom.createElement("Rooms");
	        rootEle.appendChild(e);
	        
	       for(Room node : network.nodes())
	       {
	    	   node.getRoomFromDungeonXML("dungeon\\");
	    	   next = dom.createElement("Room");
	    	   next.setAttribute("ID",  node.toString());
	    	   e.appendChild(next);
	       }

	        //Connections
	        e = dom.createElement("Connections");
	        rootEle.appendChild(e);
	        
	        for(RoomEdge edge : network.edges())
	        {
	    	   next = dom.createElement("Connection");
	    	   next.setAttribute("ID",  edge.toString());
	    	   next.setAttribute("From",  edge.from.toString());
	    	   next.setAttribute("FromPosX",  Integer.toString(edge.fromPosition.getX()));
	    	   next.setAttribute("FromPosY",  Integer.toString(edge.fromPosition.getY()));
	    	   next.setAttribute("to",  edge.to.toString());
	    	   next.setAttribute("toPosX",  Integer.toString(edge.toPosition.getX()));
	    	   next.setAttribute("toPosY",  Integer.toString(edge.toPosition.getY()));
	    	   e.appendChild(next);
	        }
	        
	        //TILES
	        e = dom.createElement("Init");
	        if(initialRoom != null)
	        {
	        	e.setAttribute("ROOM_ID",  initialRoom.toString());
	 	        e.setAttribute("PosX",  Integer.toString(initialPos.getX()));
	 	        e.setAttribute("PosY",  Integer.toString(initialPos.getY()));
	        }
	        else
	        {
	        	e.setAttribute("ROOM_ID",  "NULL");
	        }
	       
 	        rootEle.appendChild(e);

	        dom.appendChild(rootEle);

	        try {
	            Transformer tr = TransformerFactory.newInstance().newTransformer();
	            tr.setOutputProperty(OutputKeys.INDENT, "yes");
	            tr.setOutputProperty(OutputKeys.METHOD, "xml");
	            tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	            tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "room.dtd");
	            tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

	            // send DOM to file
	            tr.transform(new DOMSource(dom), 
	                                 new StreamResult(new FileOutputStream(xml)));

	        } catch (TransformerException te) {
	            System.out.println(te.getMessage());
	        } catch (IOException ioe) {
	            System.out.println(ioe.getMessage());
	        }
	    } catch (ParserConfigurationException pce) {
	        System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
	    }
	}

	public Quest getQuest() {
		return quest;
	}

	///////////////////////// TESTING TRAVERSAL AND RETRIEVAL OF ALL THE PATHS FROM A ROOM TO ANOTHER ROOM ///////////////////////////

}
