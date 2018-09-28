package gui.controls;

import java.util.List;

import finder.geometry.Bitmap;
import finder.geometry.Point;
import game.Map;
import game.TileTypes;

public class Bucket extends Brush
{

	@Override
	public void UpdateDrawableTiles(int x, int y, Map map)
	{	
		//Avoid updating the tiles if it is inside of the bucket already
		if(!drew && drawableTiles != null && drawableTiles.contains(new Point(x,y)))
		{
			return;
		}
		
		center = new Point(x,y);
		drawableTiles = new Bitmap();
		
		//Update the bitmap depending on the size of the brush
		Fill(center, map.getColCount(), map.getRowCount(), map.getTile(x, y).GetType(), map);
		drew = false;
	}
	
	protected void Fill(Point p, int width, int height, TileTypes target, Map map)
	{
		// TODO Auto-generated method stub
		if(p.getX() < 0 || p.getX() > width - 1 || p.getY() < 0 || p.getY() > height - 1 || drawableTiles.contains(p))
			return;
		
		TileTypes nextType = map.getTile(p.getX(), p.getY()).GetType();
		
		if(nextType != target)
			return;
		
		drawableTiles.addPoint(p);

		
		List<Point> neighborhood = GetNeumannNeighborhood(p);
		
		for(Point neighbor : neighborhood)
		{
			Fill(neighbor, width, height, target, map);
		}
	}

	@Override
	protected void FillDrawable(Point p, int width, int height, int layer) {
		
		
	}

}
