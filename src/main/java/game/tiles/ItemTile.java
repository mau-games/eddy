package game.tiles;

import finder.geometry.Point;
import game.Tile;
import game.TileTypes;
import gui.controls.Brush;

public class ItemTile extends Tile{

    public ItemTile()
    {
        m_type = TileTypes.ITEM;
        setBrushUsage();

    }

    public ItemTile(Point p, TileTypes type)
    {
        super(p, type);
        setBrushUsage();
    }

    public ItemTile(int x, int y, TileTypes type)
    {
        super(x, y, type);
        setBrushUsage();
    }

    public ItemTile(Point p, int typeValue)
    {
        super(p, typeValue);
        setBrushUsage();
    }

    public ItemTile(int x, int y, int typeValue)
    {
        super(x, y, typeValue);
        setBrushUsage();
    }

    public ItemTile(Tile copyTile)
    {
        super(copyTile);
        setBrushUsage();
    }

    public Brush modification(Brush brush)
    {
        return brush;
    }

}
