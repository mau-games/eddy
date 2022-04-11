package game.CoCreativity;

import game.Tile;
import java.util.*;

public class HumanCoCreator {

    private static HumanCoCreator singleton = null;

    private int maxTilesPerRound = 6; // remember to change this
    private int amountOfTilesPlaced;
    private List<Tile> tilesPlaced;

    public static HumanCoCreator getInstance()
    {
        if(singleton == null)
            singleton = new HumanCoCreator();
        return singleton;
    }

    private HumanCoCreator()
    {
        amountOfTilesPlaced = 0;
        tilesPlaced = new ArrayList<Tile>();
    }

    public void RegisterContributionInfo(Tile t)
    {
        amountOfTilesPlaced++;
        tilesPlaced.add(t);
    }

    public void resetRound()
    {
        amountOfTilesPlaced = 0;
        tilesPlaced.clear();
    }

    public int getAmountOfTilesPlaced() {
        return amountOfTilesPlaced;
    }

    public List<Tile> getTilesPlaced() {
        return tilesPlaced;
    }

    public int getMaxTilesPerRound() { return maxTilesPerRound; }

    public void setMaxTilesPerRound(int i) { maxTilesPerRound = i; }

}
