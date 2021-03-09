package game.tiles;

import finder.geometry.Point;
import game.Tile;
import game.TileTypes;
import gui.controls.Brush;

public class BlacksmithTile extends NpcTile{
	public BlacksmithTile()
    {
        m_type = TileTypes.BLACKSMITH;
        setBrushUsage();
    }

    public BlacksmithTile(Point p, TileTypes type)
    {
        super(p, type);
        setBrushUsage();
    }

    public BlacksmithTile(int x, int y, TileTypes type)
    {
        super(x, y, type);
        setBrushUsage();
    }

    public BlacksmithTile(Point p, int typeValue)
    {
        super(p, typeValue);
        setBrushUsage();
    }

    public BlacksmithTile(int x, int y, int typeValue)
    {
        super(x, y, typeValue);
        setBrushUsage();
    }

    public BlacksmithTile(Tile copyTile)
    {
        super(copyTile);
        m_type = TileTypes.BLACKSMITH;
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
