package gui.controls;

import java.util.List;

import finder.geometry.Bitmap;
import finder.geometry.Point;
import game.Room;
import game.TileTypes;

public class Bucket extends Brush
{

	@Override
	public void UpdateDrawableTiles(int x, int y, Room room)
	{	
		//Avoid updating the tiles if it is inside of the bucket already
		if(!drew && drawableTiles != null && drawableTiles.contains(new Point(x,y)))
		{
			return;
		}
		
		center = new Point(x,y);
		drawableTiles = new Bitmap();
		
		//Update the bitmap depending on the size of the brush
		Fill(center, room.getColCount(), room.getRowCount(), room.getTile(x, y).GetType(), room);
		drew = false;
	}
	
	protected void Fill(Point p, int width, int height, TileTypes target, Room room)
	{
		// TODO Auto-generated method stub
		if(p.getX() < 0 || p.getX() > width - 1 || p.getY() < 0 || p.getY() > height - 1 || drawableTiles.contains(p))
			return;
		
		TileTypes nextType = room.getTile(p.getX(), p.getY()).GetType();
		
		if(nextType != target)
			return;
		
		drawableTiles.addPoint(p);

		
		List<Point> neighborhood = GetNeumannNeighborhood(p);
		
		for(Point neighbor : neighborhood)
		{
			Fill(neighbor, width, height, target, room);
		}
	}

	@Override
	protected void FillDrawable(Point p, int width, int height, int layer) {
		
		
	}

}
