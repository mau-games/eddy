package game.tiles;

import finder.geometry.Point;
import game.Tile;
import game.TileTypes;
import gui.controls.BasicBrush;
import gui.controls.Brush;
import gui.controls.Brush.BrushUsage;
import gui.controls.Brush.NeighborhoodStyle;

public class EnemyTile extends Tile {
	
	public EnemyTile()
	{
		m_type = TileTypes.ENEMY;
		setBrushUsage();
	}
	
	public EnemyTile(Point p, TileTypes type)
	{
		super(p, type);
		setBrushUsage();
	}
	
	public EnemyTile(int x, int y, TileTypes type)
	{
		super(x, y, type);
		setBrushUsage();
	}
	
	public EnemyTile(Point p, int typeValue)
	{
		super(p, typeValue);
		setBrushUsage();
	}
	
	public EnemyTile(int x, int y, int typeValue)
	{
		super(x, y, typeValue);
		setBrushUsage();
	}
	
	public EnemyTile(Tile copyTile)
	{
		super(copyTile);
		setBrushUsage();
	}
	
	@Override
	public Brush modification(Brush brush)
	{
		brush.SetBrushSize(2);
		brush.setImmutable(true);
		brush.setNeighborhoodStyle(NeighborhoodStyle.MOORE);
		return brush;
	}
	
	@Override
	protected void setBrushUsage()
	{
		usage = BrushUsage.DEFAULT;
	}
	// Jesper
	@Override
	public void SetUsed()
	{
		this.used = true;
	}
	
}