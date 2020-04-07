package game.tiles;

import finder.geometry.Point;
import game.Room;
import game.Tile;
import game.TileTypes;
import gui.controls.Brush;
import gui.controls.Drawer;
import gui.controls.InteractiveMap;

public class NpcTile extends Tile {

    public NpcTile()
    {
        m_type = TileTypes.NPC;
        setBrushUsage();
    }

    public NpcTile(Point p, TileTypes type)
    {
        super(p, type);
        setBrushUsage();
    }

    public NpcTile(int x, int y, TileTypes type)
    {
        super(x, y, type);
        setBrushUsage();
    }

    public NpcTile(Point p, int typeValue)
    {
        super(p, typeValue);
        setBrushUsage();
    }

    public NpcTile(int x, int y, int typeValue)
    {
        super(x, y, typeValue);
        setBrushUsage();
    }

    public NpcTile(Tile copyTile)
    {
        super(copyTile);
        m_type = TileTypes.NPC;
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
