package game.tiles;

import finder.geometry.Point;
import game.Tile;
import game.TileTypes;
import gui.controls.Brush;

public class FriendTile extends Tile{
	public FriendTile()
    {
        m_type = TileTypes.FRIEND;
        setBrushUsage();
    }

    public FriendTile(Point p, TileTypes type)
    {
        super(p, type);
        setBrushUsage();
    }

    public FriendTile(int x, int y, TileTypes type)
    {
        super(x, y, type);
        setBrushUsage();
    }

    public FriendTile(Point p, int typeValue)
    {
        super(p, typeValue);
        setBrushUsage();
    }

    public FriendTile(int x, int y, int typeValue)
    {
        super(x, y, typeValue);
        setBrushUsage();
    }

    public FriendTile(Tile copyTile)
    {
        super(copyTile);
        m_type = TileTypes.FRIEND;
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
