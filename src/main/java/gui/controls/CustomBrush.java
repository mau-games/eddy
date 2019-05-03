package gui.controls;

import java.util.List;

import finder.geometry.Bitmap;
import finder.geometry.Point;
import game.Room;
import game.Tile;
import game.TileTypes;
import gui.controls.Brush.BrushUsage;

public class CustomBrush extends Brush 
{
	@Override
	public void SetMainComponent(Tile type)
	{
		mainComponent = type.GetType();
		
		if(type.getBrushUsage().equals(BrushUsage.CUSTOM))
		{
			type.modification(this);		
		}

	}

	@Override
	protected void createCopy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void UpdateDrawableTiles(int x, int y, Room room) 
	{
		//Avoid updating the tiles if we are hovering the same tile
		if(center != null && x == center.getX() && y == center.getY())
			return;
		
		center = new Point(x,y);
		drawableTiles = new Bitmap();
		
		//Update the bitmap depending on the size of the brush
		FillDrawable(center, room.getColCount(), room.getRowCount(), size);
		
	}

	@Override
	protected void FillDrawable(Point p, int width, int height, int layer) 
	{
		// TODO Auto-generated method stub
		if(layer == 0 || p.getX() < 0 || p.getX() > width - 1 || p.getY() < 0 || p.getY() > height - 1 || drawableTiles.contains(p))
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

}
