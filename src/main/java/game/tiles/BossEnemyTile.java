package game.tiles;

import finder.geometry.Point;
import game.Room;
import game.Tile;
import game.TileTypes;
import gui.controls.Brush;
import gui.controls.Drawer;
import gui.controls.InteractiveMap;
import gui.controls.Brush.BrushUsage;
import gui.controls.Brush.NeighborhoodStyle;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;

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
	public void PaintTile(Point currentCenter, Room room, Drawer drawer, InteractiveMap interactiveCanvas)
	{
		int scale = (int) interactiveCanvas.scale;
		Image m = interactiveCanvas.getCustomSizeImage(m_type, scale * width, scale * height);
		
		for(int i = 0, spaceY = -1; i < width; i++, spaceY++)
		{
			for(int j = 0, spaceX = -1; j< height;j++, spaceX++)
			{
				WritableImage a = new WritableImage(m.getPixelReader(), scale*j, scale*i, scale, scale);
				interactiveCanvas.getCell(GetCenterPosition().getX() + spaceX, GetCenterPosition().getY() + spaceY).setImage(a);
			}
		}
	}
	
	@Override
	public void PaintCanvasTile(Image tileImage, GraphicsContext ctx, double tileSize)
	{
		for(int K = 0, spaceY = -1; K < 3; K++, spaceY++)
		{
			for(int D = 0, spaceX = -1; D< 3;D++, spaceX++)
			{
			WritableImage a = new WritableImage(tileImage.getPixelReader(), (int)tileSize*D, (int)tileSize*K, (int)tileSize, (int)tileSize);
		//						getCell(x + spaceX, y + spaceY).setImage(a);
				ctx.drawImage(a, (GetCenterPosition().getX() + spaceX) * tileSize, (GetCenterPosition().getY() + spaceY) * tileSize, tileSize, tileSize);
				
			}
		}
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
		maxAmountPerRoom = 2;
		width = 3;
		height = 3;
	}
	
	@Override
	public Tile copy()
	{
		return new BossEnemyTile();
	}
	
}
