package game;

import finder.geometry.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AICoCreator {

    HumanCoCreator humanCoCreator; // needed to see how they used their round

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

    public void prepareTurn()
    {
        tilesPositions.clear();
        permutations.clear();

        amountOfTiles = CalculateAmountOfTilesToContributeWith(HumanCoCreator.getInstance().getAmountOfTilesPlaced());
        tilesPositions = CalculateContributionArea(HumanCoCreator.getInstance().getTilesPlaced());
    }

    // Rename to something better
    // returns a number between 1 and parameter+1
    private int CalculateAmountOfTilesToContributeWith(int humanContribution)
    {
        //if(humanContribution == 0)
        //    return 1;

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

        System.out.println("minPoint: " + minPoint.toString());
        System.out.println("maxPoint: " + maxPoint.toString());
        System.out.println("roomHeight: " + roomHeight);
        System.out.println("roomWidth: " + roomWidth);

        //add all tiles between min and max to list, including margins
        for(int x = minPoint.getX()-locationMargin; x<maxPoint.getX()+locationMargin; x++)
        {
            for(int y=minPoint.getY()-locationMargin; y<maxPoint.getY()+locationMargin; y++)
            {
                if(x >= 0 && y >= 0 && x < roomWidth && y < roomHeight)
                    resultingPositions.add(new Point(x,y));
            }
        }


        System.out.println("resultingPositions: " + resultingPositions.toString());
        return resultingPositions;
    }



}
