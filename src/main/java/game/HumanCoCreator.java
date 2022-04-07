package game;

import java.util.*;

public class HumanCoCreator {

    private int amountOfTilesPlaced;
    private List<Tile> tilesPlaced;

    public HumanCoCreator()
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

}
