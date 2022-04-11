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

    enum ControlLevel {low, medium, high};
    ControlLevel controlLevel; // the degree of control this agent has

    int amountOfTiles; // the amount of tiles to contribute with this turn
    List<Point> tilesPositions; //the positions of the tiles that will be contributed to this turn
    List<Room> permutations; // the permutations to run MAP-Elites on
    int locationMargin = 1; // how many tiles bigger than the human's area the contribution area will be // maybe dynamic of room size?
    int roomWidth, roomHeight;
    List<Tile> contributions; // the contributions for the round
    Room currentTargetRoom;
    private List<Room> kNearestElites; // contains the elites that are the K-nearest neighbours to the targetroom

    public AICoCreator(int roomWidth, int roomHeight)
    {
        tilesPositions = new ArrayList<>();
        permutations = new ArrayList<>();
        contributions = new ArrayList<>();

        this.roomHeight = roomHeight;
        this.roomWidth = roomWidth;
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
    public void CalculateContribution()
    {
        /***
         * Step 2: Calculate what specific tiles are the best
         *
         * The best tile = the most reoccurring tile at each position within the contribution area in the elites
         ***/

        List<TileTypes>[] blah = new List[tilesPositions.size()]; // this is an array containing a list of tiles for each position in the area

        //for each elite
        for(int i = 0; i < kNearestElites.size(); i++)
        {
            //for each tile that is in the contribution area
            for (int j=0; j < tilesPositions.size(); j++)
            {
                HashMap<String, Integer> tileTypeAndOccurance = new HashMap<>();

                blah[j].add(kNearestElites.get(i).getTile(tilesPositions.get(j).getX(), tilesPositions.get(j).getY()).GetType());
            }
        }

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
            for(int m=0; m<maxAmounts.length;m++)
            {
                if(max > maxAmounts[m])
                {
                    bestContributions[m] = new Tile(pos, TileTypes.valueOf(maxType));
                    maxAmounts[m] = max;
                    break;
                }
            }

        }

        // contributions for this round
        contributions = Arrays.asList(bestContributions);
    }

    static Map.Entry<String, Integer> getMostFrequentElement(List<TileTypes> inputArray)
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

        System.out.println("CONTRIBUTION AREA: " + resultingPositions.toString());
        return resultingPositions;

        /**
         * OTHER VERSION
         * **/
        //List<Point> resultingPositions = new ArrayList<>();
        //Point minPoint, maxPoint;
//
        //List<Point> points = new ArrayList<>();
//
        //for (Tile t: tilesPlaced) {
        //    points.add(t.GetCenterPosition());
        //}
//
        ////add the first position
        //minPoint = points.get(0);
        //maxPoint = points.get(0);
//
        ////calculate minPoint and maxPoint
        //for(int i=1; i<points.size();i++)
        //{
        //    if(points.get(i).getX() < minPoint.getX() || points.get(i).getY() < minPoint.getY())
        //    {
        //        minPoint = points.get(i);
        //    }
        //    else if(points.get(i).getX() > maxPoint.getX() || points.get(i).getY() > maxPoint.getY())
        //    {
        //        maxPoint = points.get(i);
        //    }
        //}
//
        //System.out.println("minPoint: " + minPoint.toString());
        //System.out.println("maxPoint: " + maxPoint.toString());
//
        ////add all tiles between min and max to list, including margins
        //for(int x = minPoint.getX()-locationMargin; x<maxPoint.getX()+locationMargin; x++)
        //{
        //    for(int y=minPoint.getY()-locationMargin; y<maxPoint.getY()+locationMargin; y++)
        //    {
        //        if(x >= 0 && y >= 0 && x < roomWidth && y < roomHeight)
        //            resultingPositions.add(new Point(x,y));
        //    }
        //}
//
        //System.out.println("resultingPositions: " + resultingPositions.toString());
        //return resultingPositions;
    }

    public List<Tile> GetContributions() { return contributions; }

    public ControlLevel getControlLevel() { return controlLevel; }

    public void setControlLevel(ControlLevel controlLevel) { this.controlLevel = controlLevel; }
}
