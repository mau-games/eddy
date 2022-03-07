package collectors;

import java.io.File;
import java.io.IOException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import game.Dungeon;
import game.Room;
import game.Tile;
import game.TileTypes;
import game.tiles.BossEnemyTile;
import generator.algorithm.MAPElites.MAPEliteAlgorithm;
import generator.config.GeneratorConfig;
import util.Point;
import util.config.MissingConfigurationException;

class XMLRoom
{
	String ID = "";
	LinkedList<FileKeyDUO> sequence;
	
	public XMLRoom(String ID)
	{
		this.ID = ID;
		sequence = new LinkedList<FileKeyDUO>();
	}
	
	public LinkedList<FileKeyDUO> getSequence() {return sequence;}
	
	public void addToSequence(FileKeyDUO sequenceElement)
	{
		sequence.add(sequenceElement);
	}
	
	public void sortSequence()
	{
		Collections.sort(sequence);
	}
}

class FileKeyDUO implements Comparable<FileKeyDUO>
{
	int rank;
	File xmlFile;
	
	public FileKeyDUO(int k, File f)
	{
		this.rank = k;
		this.xmlFile = f;
	}

	@Override
	public int compareTo(FileKeyDUO other) {
		// TODO Auto-generated method stub
		
		return other.rank == this.rank ? 0 : other.rank > this.rank ? -1 : 1;
	}
}

public class XMLHandler 
{
	private static XMLHandler instance = null;
	private boolean saveInformation = true; //enabled by default
	public static String projectPath = System.getProperty("user.dir") +
			File.separator + File.separator + "my-data" + File.separator + File.separator;
	
	//This is for testing! Pls remove!
	public HashMap<String, XMLRoom> xmlRooms = new HashMap<String, XMLRoom>();
	
	//TESTING!
	public LinkedList<Room> roomsInFile = new LinkedList<Room>();
	
	
	private XMLHandler()
	{
		
	}
	
	/***
	 * get the static instance of this class --> Singleton
	 * @return static instance of this class
	 */
	public static XMLHandler getInstance()
	{
		if(instance == null)
		{
			instance = new XMLHandler();
		}
		
		return instance;
	}
	
	/***
	 * Save the room XML individually each time the user does a modification to it OR
	 * every time the click/apply a suggestion
	 * @param roomToSave the room to save
	 * @param prefix folders
	 * @return if saving is enabled
	 */
	public boolean saveIndividualRoomXML(Room roomToSave, String directory)
	{
		if(saveInformation)
		{
			roomToSave.getRoomXML(directory);
		}
		
		return saveInformation;
	}
	
	/***
	 * Save the room XML individually each time the user does a modification to it OR
	 * every time the click/apply a suggestion
	 * @param roomToSave the room to save
	 * @param prefix folders
	 * @return if saving is enabled
	 */
	public boolean saveIndividualRoomSequence(Room roomToSave, String directory)
	{
		if(saveInformation)
		{
			for(Room edition : roomToSave.getEditionSequence())
			{
				edition.saveRoomXMLSequence(directory, roomToSave.toString());
			}
		}
		
		return saveInformation;
	}
	
	/***
	 * Save the room XML from the MAP-Elites algorithm each time the user applies a suggestion
	 * @param roomToSave the room to save
	 * @param prefix folders
	 * @return
	 */
	public boolean saveMAPERoomXML(Room roomToSave, String prefix)
	{
		if(saveInformation)
		{
			roomToSave.saveRoomXMLMapElites(prefix);
		}
		
		return saveInformation;
	}
	
	/***
	 * Save the room XML from the dungeon view each time the user add/remove a room or a door.
	 * @param roomToSave The room to be saved
	 * @param prefix folders
	 * @return
	 */
	public boolean saveDungeonRoomXML(Room roomToSave, String prefix)
	{
		if(saveInformation)
		{
			roomToSave.getRoomFromDungeonXML(prefix);
		}
		
		return saveInformation;
	}
	
	/***
	 * Save the top individual of each feasible population of each cell + some info about the algorithm (dimensions, fitness, etc.)
	 * @param algorithm The MAP-Elite algorithm
	 * @return
	 */
	public boolean saveMAPEXML(MAPEliteAlgorithm algorithm)
	{
		if(saveInformation)
		{
			
			algorithm.storeMAPELITESXml();
		}
		
		return saveInformation;
	}
	
	/**
	 * Save all the rooms in the dungeon and how they are connected
	 * @param dungeon the current dungeon
	 * @return
	 */
	public boolean saveDungeonXML(Dungeon dungeon)
	{
		if(saveInformation)
		{
			
			dungeon.saveDungeonXML();
		}
		
		return saveInformation;
	}
	
	public void clearLoaded()
	{
		xmlRooms.clear();
		roomsInFile.clear();
	}
	
	public void sortRoomsToLoad()
	{
		for(XMLRoom xmlRoom : xmlRooms.values())
		{
			xmlRoom.sortSequence();
		}
	}
	
	public boolean loadRooms(String folder, boolean roomPath)
	{
		File file = new File(folder);
		
		 for (final File fileEntry : file.listFiles())
		 {
			 //If the file is a folder, we check if we are entering the correct folder and we recursively keep going into the folders.
			if (fileEntry.isDirectory()) 
			{
				if(!roomPath && fileEntry.getName().equals("room"))
				{
					roomPath = true;
				}
				
				loadRooms(fileEntry.getAbsolutePath(), roomPath);
			} 
			else if(fileEntry.getName().endsWith(".xml"))
			{
				//If we already found a file, we see if it is a new room file. If it is, we create a new instance. else we add to the existing file
				if(roomPath)
				{
					String[] a = fileEntry.getName().split("\\.|_");
					
					if(xmlRooms.containsKey(a[0]))
					{
						xmlRooms.get(a[0]).addToSequence(new FileKeyDUO(Integer.parseInt(a[a.length - 2]), fileEntry));
					}
					else
					{
						xmlRooms.put(a[0], new XMLRoom(a[0]));
						xmlRooms.get(a[0]).addToSequence(new FileKeyDUO(Integer.parseInt(a[a.length - 2]), fileEntry));
					}
				}
			}
		 }
		
		return true;
	}
	
	public void createRooms()
	{
		for(XMLRoom xmlRoom : xmlRooms.values())
		{
			LinkedList<FileKeyDUO> sequence = xmlRoom.getSequence();
			LinkedList<Room> currentRooms = new LinkedList<Room>();
			
			//Create al lthe rooms
			for(FileKeyDUO fkDUO : sequence)
			{
				currentRooms.add(loadRoom(fkDUO.xmlFile));
			}
			
			//Get the last room, assigned it as the main and add the rest as part of the sequence!
			roomsInFile.add(currentRooms.get(currentRooms.size() - 1));
			
			for(int index = 0; index < currentRooms.size(); index++)
			{
				currentRooms.get(index).addEditions(currentRooms);
				currentRooms.get(index).indexEditionStep = index;
			}			
			
//			roomsInFile.get(roomsInFile.size() - 1).addEditions(currentRooms);
		}
	}
	
	//We load the room from the xml!
	public Room loadRoom(File roomFile)
	{
		ArrayList<String> rolev = new ArrayList<String>();
        Document dom;
        // Make an  instance of the DocumentBuilderFactory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            
            // use the factory to take an instance of the document builder
            DocumentBuilder db = dbf.newDocumentBuilder();
            
            // parse using the builder to get the DOM mapping of the    
            // XML file
            dom = db.parse(roomFile);
            Element doc = dom.getDocumentElement();
            
            //Create the room
            GeneratorConfig gc = null;
    		try {
    			gc = new GeneratorConfig();
    		} catch (MissingConfigurationException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    		
            Room xmlRoom = new Room(gc, Integer.parseInt(doc.getAttribute("height")), Integer.parseInt(doc.getAttribute("width")));
            xmlRoom.specificID = UUID.fromString(doc.getAttribute("ID"));
            		
            NodeList children = doc.getChildNodes();
            Node tiles = null;

            for(int i = 0; i<children.getLength(); i++)
            {
            	if(children.item(i).getNodeName().equals("Tiles"))
            	{
            		tiles = children.item(i);
            		break;
            	}
            }
            
            //Iterate the children!
            children = tiles.getChildNodes();
            
            for(int i = 0; i<children.getLength(); i++)
            {
            	if(children.item(i).getNodeName().equals("Tile"))
            	{
            		int x = Integer.parseInt(children.item(i).getAttributes().getNamedItem("PosX").getNodeValue());
            		int y= Integer.parseInt(children.item(i).getAttributes().getNamedItem("PosY").getNodeValue());
            		TileTypes tileValue = TileTypes.valueOf(children.item(i).getAttributes().getNamedItem("value").getNodeValue());
            		
            		if(tileValue == TileTypes.DOOR)
            		{
            			xmlRoom.createDoor(new Point(x, y));
            		}
            		else if(tileValue == TileTypes.HERO)
            		{
            			xmlRoom.setTile(x, y, new Tile(x,y, TileTypes.FLOOR));	
            		}
            		else
            		{
                		xmlRoom.setTile(x, y, new Tile(x,y, tileValue));	
            		}

//            		System.out.println(children.item(i).getAttributes().getNamedItem("PosX").getNodeValue() + ", " +
//            							children.item(i).getAttributes().getNamedItem("PosY").getNodeValue() + ", " +
//            							children.item(i).getAttributes().getNamedItem("immutable").getNodeValue() + ", " +
//            							children.item(i).getAttributes().getNamedItem("value").getNodeValue());
            	}
            }
            
            //NOW GET ALL THE CUSTOMS TILES
            children = doc.getChildNodes();
            Node customs = null;

            for(int i = 0; i<children.getLength(); i++)
            {
            	if(children.item(i).getNodeName().equals("Customs"))
            	{
            		customs = children.item(i);
            		break;
            	}
            }
            
            //Iterate the children!
            children = customs.getChildNodes();
            
            for(int i = 0; i<children.getLength(); i++)
            {
            	if(children.item(i).getNodeName().equals("Custom"))
            	{
            		int x = Integer.parseInt(children.item(i).getAttributes().getNamedItem("centerX").getNodeValue());
            		int y= Integer.parseInt(children.item(i).getAttributes().getNamedItem("centerY").getNodeValue());
            		boolean immutable = Boolean.parseBoolean(children.item(i).getAttributes().getNamedItem("immutable").getNodeValue());
            		TileTypes tileValue = TileTypes.valueOf(children.item(i).getAttributes().getNamedItem("value").getNodeValue());
            		
            		if(tileValue == TileTypes.ENEMY_BOSS)
            		{
            			BossEnemyTile bossTile = new BossEnemyTile(x, y, tileValue);
            			bossTile.SetImmutable(immutable);
            			xmlRoom.addCustomTileFromXML(bossTile);
            		}
            	}
            }
            
            
            xmlRoom.setupRoom();
//            roomsInFile.add(xmlRoom);

            return xmlRoom;

        } catch (ParserConfigurationException pce) {
            System.out.println(pce.getMessage());
        } catch (SAXException se) {
            System.out.println(se.getMessage());
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }

        return null;
	}
	
	public boolean shouldSave() {return saveInformation;}
}
