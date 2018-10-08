package gui.controls;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import finder.geometry.Bitmap;
import game.Room;
import game.TileTypes;
import javafx.scene.input.MouseEvent;

public abstract class AbstractDrawer<BrushType extends Brush>
{
	protected List<BrushType> brushes;
	protected BrushType brush;
	
	public AbstractDrawer()
	{
		InitializeBrushes();
	}
	
	protected abstract void InitializeBrushes();
	
	public BrushType GetBrush()
	{
		return (BrushType) brush;
	}
	
	public void SetBrush(BrushType newBrush)
	{
		brush = newBrush;
	}
	
	public void SetBrush(int brushType)
	{
		brush = brushes.get(brushType);
	}
	
	public int GetBrushSize()
	{
		return brush.GetBrushSize();
	}
	
	public void SetBrushSize(int value)
	{
		for(Brush b : brushes)
		{
			b.SetBrushSize(value);
		}
	}
	
	public Bitmap GetDrawableTiles()
	{
		return brush.GetDrawableTiles();
	}
	
	public void DoneDrawing()
	{
		for(Brush b : brushes)
		{
			b.SetDrew();
		}
//		brush.SetDrew();
	}
	
	public void Update(MouseEvent event)
	{
		
	}
	
	/**
	 * Update the modifiers based on the alternate mouse events and changes the main brush tile
	 * @param event Mouse event over the map
	 */
	public void Update(MouseEvent event, util.Point p, Room room)
	{
		if(brushes.size() > 1)
		{
			if(event.isShiftDown())
				brush = brushes.get(1);
			else 
				brush = brushes.get(0);
		}

		if(p != null)
			brush.UpdateDrawableTiles(p.getX(), p.getY(), room);
	}
}
