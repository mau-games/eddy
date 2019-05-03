package game.tiles;

import finder.geometry.Point;
import game.Tile;
import game.TileTypes;
import gui.controls.Brush;
import gui.controls.Brush.BrushUsage;
import gui.controls.Brush.NeighborhoodStyle;

public class BossEnemyTile extends EnemyTile
{
	public BossEnemyTile()
	{
		m_type = TileTypes.ENEMY_BOSS;
		setBrushUsage();
	}
	
	public BossEnemyTile(Point p, TileTypes type)
	{
		super(p, type);
		setBrushUsage();
	}
	
	public BossEnemyTile(int x, int y, TileTypes type)
	{
		super(x, y, type);
		setBrushUsage();
	}
	
	public BossEnemyTile(Point p, int typeValue)
	{
		super(p, typeValue);
		setBrushUsage();
	}
	
	public BossEnemyTile(int x, int y, int typeValue)
	{
		super(x, y, typeValue);
		setBrushUsage();
	}
	
	public BossEnemyTile(Tile copyTile)
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
		usage = BrushUsage.CUSTOM;
	}
}
