package gui.controls;

import java.util.List;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import finder.geometry.Bitmap;
import finder.geometry.Point;
import game.Room;
import game.Tile;
import game.TileTypes;
import game.tiles.BossEnemyTile;
import gui.controls.Brush.BrushUsage;

public class CustomBrush extends Brush 
{
	private boolean canDraw = true;
	
	@Override
	public void SetMainComponent(Tile type)
	{
		brushTile = type;
		mainComponent = type.GetType();
		
		if(type.getBrushUsage().equals(BrushUsage.CUSTOM))
		{
			type.modification(this);		
		}

	}

	@Override
	public void UpdateDrawableTiles(int x, int y, Room room) 
	{
		Tile hoveredTile = room.getTile(x, y);
		int new_x = hoveredTile.GetCenterPosition().getX();
		int new_y = hoveredTile.GetCenterPosition().getY();
		
		//Avoid updating the tiles if we are hovering the same tile
		if(center != null && new_x == center.getX() && new_y == center.getY())
			return;
		
		center = new Point(new_x, new_y);
		drawableTiles = new Bitmap();
		canDraw = true;
		
		//Update the bitmap depending on the size of the brush
		FillDrawable(center, room.getColCount(), room.getRowCount(), size);
		
	}
	
	@Override
	public boolean canBrushDraw()
	{
		return canDraw;
	}

	@Override
	protected void FillDrawable(Point p, int width, int height, int layer) 
	{
		if(p.getX() < 0 || p.getX() > width - 1 || p.getY() < 0 || p.getY() > height - 1)
		{
			canDraw = false;
			return;
		}
		
		// TODO Auto-generated method stub
		if(layer == 0 || drawableTiles.contains(p))
			return;
		
		drawableTiles.addPoint(p);
		
		if(layer - 1 <= 0)
			return;
		
//		List<Point> neighborhood = GetNeumannNeighborhood(p);
		List<Point> neighborhood = null;
		
		if(neighborStyle == NeighborhoodStyle.NEUMANN) neighborhood = GetNeumannNeighborhood(p);
		else neighborhood = GetMooreNeighborhood(p);
		
		for(Point neighbor : neighborhood)
		{
			FillDrawable(neighbor, width, height, layer - 1);
		}
	}

	@Override
	public void Draw(Point currentCenter, Room room, Drawer boss, InteractiveMap interactiveCanvas) 
	{
		if(GetMainComponent() == null)
			return;
		
		Tile currentTile = null;
		
		if(!this.immutable)
			this.immutable = boss.GetModifierValue("Lock");
		
		brushTile = brushTile.copy();
		brushTile.SetImmutable(this.immutable);
		brushTile.setCenterPosition(center);
		brushTile.setPositions(GetDrawableTiles().getPoints());
		
		Tile prev = room.addCustomTile(brushTile, brushTile.maxAmountPerRoom);
		
		if(prev != null) //We actually erased something
		{
			//"ERASE" TILES
			for(Point position :prev.GetPositions())
			{
				prev.PaintTile(position, room, boss, interactiveCanvas);
			}
		}
			
		for(Point position : GetDrawableTiles().getPoints())
		{
			currentTile = room.getTile(position.getX(), position.getY());
			
			ActionLogger.getInstance().storeAction(ActionType.CHANGE_TILE, 
													View.ROOM, 
													TargetPane.MAP_PANE,
													true,
													room, //ROOM A
													position, //Pos A
													currentTile.GetType(), //TILE A
													GetMainComponent()); //TILE B
			
			// Let's discard any attempts at erasing the doors
			if(currentTile.GetType() == TileTypes.DOOR)
				continue;
			
//			currentTile.SetImmutable(immutable);
			room.setTile(position.getX(), position.getY(), brushTile);
		}
		
		brushTile.PaintTile(center, room, boss, interactiveCanvas);
		
	}
	

}
