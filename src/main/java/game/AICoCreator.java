package game;

import gui.views.CCRoomViewController;
import util.Point;

import java.util.ArrayList;
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

    public AICoCreator(HumanCoCreator humanCoCreator, int roomWidth, int roomHeight)
    {
        this.humanCoCreator = humanCoCreator;
        tilesPositions = new ArrayList<>();
        permutations = new ArrayList<>();

        this.roomHeight = roomHeight;
        this.roomWidth = roomWidth;
    }

    public void prepareTurn()
    {
        tilesPositions.clear();
        permutations.clear();
        amountOfTiles = CalculateAmountOfTilesToContributeWith(humanCoCreator.getAmountOfTilesPlaced());
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
        List<Point> temp = new ArrayList<>();

        //loop through the humans contributions
        //if the tile is within the current area, ignore
        //if not, expand the area accordingly

        return temp;
    }
}
