package gui.controls;

import java.util.List;

import finder.geometry.Bitmap;
import finder.geometry.Point;
import game.Map;

public class BasicBrush extends Brush
{

	@Override
	public void UpdateDrawableTiles(int x, int y, Map map) 
	{
		//Avoid updating the tiles if we are hovering the same tile
		if(center != null && x == center.getX() && y == center.getY())
			return;
		
		center = new Point(x,y);
		drawableTiles = new Bitmap();
		
		//Update the bitmap depending on the size of the brush
		FillDrawable(center, map.getColCount(), map.getRowCount(), size);
		
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
		
		List<Point> neighborhood = GetNeumannNeighborhood(p);
		
		for(Point neighbor : neighborhood)
		{
			FillDrawable(neighbor, width, height, layer - 1);
		}
	}
	
}
