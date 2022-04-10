package game;

import finder.geometry.Point;
import util.eventrouting.EventRouter;

import java.util.*;

public class AICoCreator {

    private static EventRouter router = EventRouter.getInstance();
    private static HumanCoCreator humanCC = HumanCoCreator.getInstance(); // needed to see how they used their round

    enum ControlVersion {low, medium, high};
    ControlVersion control; // the degree of control this agent has

    int amountOfTiles; // the amount of tiles to contribute with this turn
    List<Point> tilesPositions; //the positions of the tiles that will be contributed to this turn
    List<Room> permutations; // the permutations to run MAP-Elites on
    int locationMargin = 2; // how many tiles bigger than the human's area the contribution area will be
    int roomWidth, roomHeight;

    public AICoCreator(int roomWidth, int roomHeight)
    {
        tilesPositions = new ArrayList<>();
        permutations = new ArrayList<>();

        this.roomHeight = roomHeight;
        this.roomWidth = roomWidth;
    }

    /***
     * Prepares for a new turn
     ***/
    public void prepareTurn()
    {
        tilesPositions.clear();
        permutations.clear();

        amountOfTiles = CalculateAmountOfTilesToContributeWith(humanCC.getInstance().getAmountOfTilesPlaced());
        tilesPositions = CalculateContributionArea(humanCC.getInstance().getTilesPlaced());

        System.out.println("amountOfTiles: " + amountOfTiles);
    }

    /***
     * Does the calculation of what tiles to contribute with
     ***/
    public void CalculateContribution(Room room)
    {
        List<Room> elites = new ArrayList<>();

        // Step 1: Run MAP-Elites for 100 generations to get the best candidates of rooms

        // able to edit human contributions? I think yes, but might result in the human deleting what the human made over and over. Maybe add a queue of actions so that it's never repeated ?
        // all dimensions


        // Step 2: Calculate what specific tiles are the best by checking the contribution area

        // look for the most reoccurring tiles

        //Tile[] bestTiles = new Tile[amountOfTiles]; // the tiles (for position and type)
        //int[] bestTilesCount = new int[amountOfTiles]; // the count


        List<TileTypes>[] blah = new List[tilesPositions.size()]; // this is an array containing a list of tiles for each position in the area

        //for each elite
        for(int i=0; i < elites.size(); i++)
        {
            //for each tile that is in the contribution area
            for (int j=0; j < tilesPositions.size(); j++)
            {
                HashMap<String, Integer> tileTypeAndOccurance = new HashMap<>();

                blah[j].add(elites.get(i).getTile(tilesPositions.get(j).getX(), tilesPositions.get(j).getY()).GetType());
            }
        }


        // for each position that we can contribute to
        for(int k=0; k<blah.length;k++)
        {
            //calculate what tileType was the most common on this position
            
            //and how common

        }
    }

    static Map.Entry<String, Integer> getMostFrequentElement(TileTypes inputArray[])
    {
        //Creating HashMap object with elements as keys and their occurrences as values

        HashMap<String, Integer> elementCountMap = new HashMap<String, Integer>();

        //Inserting all the elements of inputArray into elementCountMap

        for (TileTypes i : inputArray)
        {
            if (elementCountMap.containsKey(i))
            {
                //If an element is present, incrementing its count by 1
                elementCountMap.put(i.name(), elementCountMap.get(i)+1);
            }
            else
            {
                //If an element is not present, put that element with 1 as its value
                elementCountMap.put(i.name(), 1);
            }
        }

        String element = "";

        int frequency = 1;

        //Iterating through elementCountMap to get the most frequent element and its frequency

        Set<Map.Entry<String, Integer>> entrySet = elementCountMap.entrySet();

        for (Map.Entry<String, Integer> entry : entrySet)
        {
            if(entry.getValue() > frequency)
            {
                element = entry.getKey();

                frequency = entry.getValue();
            }
        }

        //Printing the most frequent element in array and its frequency

        if(frequency > 1)
        {
            System.out.println("Input Array : "+Arrays.toString(inputArray));
            System.out.println("The most frequent element : "+element);
            System.out.println("Its frequency : "+frequency);
            System.out.println("========================");
        }
        else
        {
            System.out.println("Input Array : "+Arrays.toString(inputArray));
            System.out.println("No frequent element. All elements are unique.");
            System.out.println("=========================");
        }

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
        Point minPoint, maxPoint;

        List<Point> points = new ArrayList<>();

        for (Tile t: tilesPlaced) {
            points.add(t.GetCenterPosition());
        }

        //add the first position
        minPoint = points.get(0);
        maxPoint = points.get(0);

        //calculate minPoint and maxPoint
        for(int i=1; i<points.size();i++)
        {
            if(points.get(i).getX() < minPoint.getX() || points.get(i).getY() < minPoint.getY())
            {
                minPoint = points.get(i);
            }
            else if(points.get(i).getX() > maxPoint.getX() || points.get(i).getY() > maxPoint.getY())
            {
                maxPoint = points.get(i);
            }
        }

        //System.out.println("minPoint: " + minPoint.toString());
        //System.out.println("maxPoint: " + maxPoint.toString());
        //System.out.println("roomHeight: " + roomHeight);
        //System.out.println("roomWidth: " + roomWidth);

        //add all tiles between min and max to list, including margins
        for(int x = minPoint.getX()-locationMargin; x<maxPoint.getX()+locationMargin; x++)
        {
            for(int y=minPoint.getY()-locationMargin; y<maxPoint.getY()+locationMargin; y++)
            {
                if(x >= 0 && y >= 0 && x < roomWidth && y < roomHeight)
                    resultingPositions.add(new Point(x,y));
            }
        }

        //System.out.println("resultingPositions: " + resultingPositions.toString());
        return resultingPositions;
    }



}
