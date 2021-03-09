package game.tiles;

import finder.geometry.Point;
import game.Tile;
import game.TileTypes;
import gui.controls.Brush;

public class BountyhunterTile extends NpcTile{
	public BountyhunterTile()
    {
        m_type = TileTypes.BOUNTYHUNTER;
        setBrushUsage();
    }

    public BountyhunterTile(Point p, TileTypes type)
    {
        super(p, type);
        setBrushUsage();
    }

    public BountyhunterTile(int x, int y, TileTypes type)
    {
        super(x, y, type);
        setBrushUsage();
    }

    public BountyhunterTile(Point p, int typeValue)
    {
        super(p, typeValue);
        setBrushUsage();
    }

    public BountyhunterTile(int x, int y, int typeValue)
    {
        super(x, y, typeValue);
        setBrushUsage();
    }

    public BountyhunterTile(Tile copyTile)
    {
        super(copyTile);
        m_type = TileTypes.BOUNTYHUNTER;
        setBrushUsage();
    }

//    @Override
//    public void PaintTile(Point currentCenter, Room room, Drawer drawer, InteractiveMap interactiveCanvas)
//    {
//        interactiveCanvas.getCell(currentCenter.getX(), currentCenter.getY()).
//                setImage(interactiveCanvas.getImage(m_type, interactiveCanvas.scale));
//    }

    @Override
    public Brush modification(Brush brush)
    {
        brush.setImmutable(true);
        return brush;
    }

//    @Override
//    protected void setBrushUsage()
//    {
//        super.setBrushUsage();
//        SetImmutable(true);
//    }
//
//    @Override
//    public Tile copy()
//    {
//        return new NpcTile(this);
//    }
}
