package game.tiles;

import finder.geometry.Point;
import game.Tile;
import game.TileTypes;
import gui.controls.Brush;

public class ThiefTile extends NpcTile{
	public ThiefTile()
    {
        m_type = TileTypes.THIEF;
        setBrushUsage();
    }

    public ThiefTile(Point p, TileTypes type)
    {
        super(p, type);
        setBrushUsage();
    }

    public ThiefTile(int x, int y, TileTypes type)
    {
        super(x, y, type);
        setBrushUsage();
    }

    public ThiefTile(Point p, int typeValue)
    {
        super(p, typeValue);
        setBrushUsage();
    }

    public ThiefTile(int x, int y, int typeValue)
    {
        super(x, y, typeValue);
        setBrushUsage();
    }

    public ThiefTile(Tile copyTile)
    {
        super(copyTile);
        m_type = TileTypes.THIEF;
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
