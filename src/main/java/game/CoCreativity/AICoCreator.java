package game.CoCreativity;

import finder.geometry.Point;
import game.Room;
import game.Tile;
import game.TileTypes;
import util.eventrouting.EventRouter;

import java.util.*;

public class AICoCreator {

    private static EventRouter router = EventRouter.getInstance();
    private static HumanCoCreator humanCC = HumanCoCreator.getInstance(); // needed to see how they used their round

    private static AICoCreator singleton = null;

    public enum ControlLevel {LOW, MEDIUM, HIGH};
    ControlLevel controlLevel; // the degree of control this agent has

    int amountOfTiles; // the amount of tiles to contribute with this turn
    List<Point> tilesPositions; //the positions of the tiles that will be contributed to this turn
    List<Room> permutations; // the permutations to run MAP-Elites on
    int locationMargin = 1; // how many tiles bigger than the human's area the contribution area will be // maybe dynamic of room size?
    int roomWidth, roomHeight;
    List<Tile> contributions; // the contributions for the round
    Room currentTargetRoom;
    private List<Room> kNearestElites; // contains the elites that are the K-nearest neighbours to the targetroom

    public static AICoCreator getInstance()
    {
        if(singleton == null)
            singleton = new AICoCreator(0, 0);
        return singleton;
    }

    private AICoCreator(int roomWidth, int roomHeight)
    {
        setControlLevel(ControlLevel.LOW);

        tilesPositions = new ArrayList<>();
        permutations = new ArrayList<>();
        contributions = new ArrayList<>();

        this.roomHeight = roomHeight;
        this.roomWidth = roomWidth;
    }

    public void initAiCoCreator(int roomWidth, int roomHeight)
    {
        singleton = new AICoCreator(roomWidth, roomHeight);
    }

    /***
     * Prepares for a new turn
     ***/
    public void prepareTurn(Room room)
    {
        tilesPositions.clear();
        permutations.clear();

        currentTargetRoom = room;
        amountOfTiles = CalculateAmountOfTilesToContributeWith(humanCC.getInstance().getAmountOfTilesPlaced());
        tilesPositions = CalculateContributionArea(humanCC.getInstance().getTilesPlaced());

        /***
         * Step 1: Run MAP-Elites for 100 generations to get the best candidates of rooms
         ***/
        List<Room> elites = new ArrayList<>();

        // get the K-nearest elites to the target room
        // able to edit human contributions? I think yes, but might result in the human deleting what the human made over and over. Maybe add a queue of actions so that it's never repeated ?
        // all dimensions

        // result should be put into kNearestElites
    }

    /***
     * Does the calculation of what tiles to contribute with
     ***/
    public void CalculateContribution(List<Room> elites)
    {
        /***
         * Step 2: Calculate what specific tiles are the best
         *
         * The best tile = the most reoccurring tile at each position within the contribution area in the elites
         ***/

        List<TileTypes>[] blah = new List[tilesPositions.size()]; // this is an array containing a list of tiles for each position in the area

        int elitesThatAReNull = 0;
        int totalAMountOfElites = elites.size();

        //for each elite
        for(int i = 0; i < elites.size(); i++) //kNearestElites
        {
            if(elites.get(i) == null) // .getTile(tilesPositions.get(j).getX(), tilesPositions.get(j).getY()).GetType()
            {
                elitesThatAReNull++;
            }
            else
            {
                //for each tile that is in the contribution area
                for (int j=0; j < tilesPositions.size(); j++)
                {
                    if(blah[j] == null)
                    {
                        blah[j] = new ArrayList<TileTypes>();
                    }

                    blah[j].add(elites.get(i).getTile(tilesPositions.get(j).getX(), tilesPositions.get(j).getY()).GetType()); // kNearestElites
                }
            }
        }

        System.out.println("Elites That Are Null: " + elitesThatAReNull + " / " + totalAMountOfElites + " = " + String.format("%."+3+"f",(float)elitesThatAReNull / (float)totalAMountOfElites *100)+"%");


        Tile[] bestContributions = new Tile[amountOfTiles];
        int[] maxAmounts = new int[amountOfTiles];

        // for each position that we can contribute to
        for(int k=0; k<blah.length;k++)
        {
            //calculate what tileType was the most common on this position
            Map.Entry<String, Integer> mostCommon = getMostFrequentElement(blah[k]);

            Point pos = tilesPositions.get(k);
            String maxType = mostCommon.getKey();
            int max = mostCommon.getValue();

            //update contributions if any of the current ones has a lower frequency
            for(int n=0; n<maxAmounts.length;n++)
            {
                if(max > maxAmounts[n])
                {
                    bestContributions[n] = new Tile(pos, TileTypes.getTypeByName(maxType)); // TileTypes.valueOf(maxType)
                    maxAmounts[n] = max;
                    break;
                }
            }
        }

        // contributions for this round
        System.out.println("contributions this run: ");
        for(Tile t : bestContributions)
        {
            System.out.println(t.GetCenterPosition() + " " + t.GetType().name());
        }

        System.out.println("");
        contributions = Arrays.asList(bestContributions);
        router.postEvent(new AICalculateContributionsDone());
    }

    static Map.Entry<String, Integer> getMostFrequentElement(List<TileTypes> inputArray)
    {
        //Creating HashMap object with elements as keys and their occurrences as values

        HashMap<String, Integer> elementCountMap = new HashMap<String, Integer>();

        //Inserting all the elements of inputArray into elementCountMap

        for (TileTypes i : inputArray)
        {
            if (elementCountMap.containsKey(i.name()))
            {
                //If an element is present, incrementing its count by 1
                int newValue = elementCountMap.get(i.name())+1;

                elementCountMap.put(i.name(), newValue);
            }
            else if(!elementCountMap.containsKey(i.name()) && i != TileTypes.FLOOR && i != TileTypes.NONE && i != TileTypes.DOOR && i != TileTypes.HERO)// ignore NONE, FLOOR, HERO and DOOR
            {
                //If an element is not present, put that element with 1 as its value
                elementCountMap.put(i.name(), 1);
            }
        }

        String element = "";

        int frequency = 0;

        //Iterating through elementCountMap to get the most frequent element and its frequency
        for (Map.Entry<String, Integer> entry : elementCountMap.entrySet())
        {
            //System.out.println("entry.GetKey() " + entry.getKey());
            if(entry.getValue() > frequency)
            {
                element = entry.getKey();
                frequency = entry.getValue();

            }
        }

        //System.out.println("MOST COMMON KEY: "+ element + " VALUE " + frequency);
        Map.Entry<String, Integer> temp = new AbstractMap.SimpleEntry<String, Integer>(element, frequency);
        return temp;
    }


    // Rename to something better
    // returns a number between 1 and parameter+1
    private int CalculateAmountOfTilesToContributeWith(int humanContribution)
    {
        if(humanContribution == 0)
            return 1;

        int max = humanContribution + 1;
        int min = Math.max(humanContribution - 1, 1);
        return (int)(Math.random() * ((max-min)) + min);
    }

    private List<Point> CalculateContributionArea(List<Tile> tilesPlaced)
    {
        List<Point> resultingPositions = new ArrayList<>();

        for(Tile t: tilesPlaced)
        {
            resultingPositions.add(t.GetCenterPosition());

            for(int x = -locationMargin; x<=locationMargin;x++)
            {
                if(t.GetCenterPosition().getX()+x >= 0 && t.GetCenterPosition().getX()+x < roomWidth)
                {
                    for(int y = -locationMargin; y<=locationMargin;y++)
                    {
                        if(t.GetCenterPosition().getY()+y >= 0 && t.GetCenterPosition().getY()+y < roomHeight)
                        {
                            Point p = new Point (t.GetCenterPosition().getX()+x, t.GetCenterPosition().getY()+y);
                            if(!resultingPositions.contains(p))
                            {
                                resultingPositions.add(p);
                            }
                        }
                    }
                }
            }
        }

        List<Point> finalList = removeDuplicates(resultingPositions);

        //IF HUMAN CONTRIBUTION IS NOT EDITABLE, REMOVE POSITION FROM CONTRIBUTION AREA
        for(Tile ti:tilesPlaced)
        {
            if(!ti.getEditable() && finalList.contains(ti.GetCenterPosition()))
                System.out.println("EXCLUDES UNEDITABLE TILES");
                finalList.remove(ti.GetCenterPosition());
        }

        System.out.println("CONTRIBUTION AREA: " + finalList.toString());
        return finalList;

    }

    public static <T> List<T> removeDuplicates(List<T> list)
    {
        // Create a new ArrayList
        List<T> newList = new ArrayList<T>();

        // Traverse through the first list
        for (T element : list) {

            // If this element is not present in newList
            // then add it
            if (!newList.contains(element)) {

                newList.add(element);
            }
        }

        // return the new list
        return newList;
    }

    public List<Tile> GetContributions() { return contributions; }

    public ControlLevel getControlLevel() { return controlLevel; }

    public void setControlLevel(ControlLevel controlLevel) { this.controlLevel = controlLevel; }


}
