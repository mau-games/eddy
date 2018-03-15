package gui.controls;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

import finder.geometry.Bitmap;
import finder.geometry.Point;
import finder.graph.Edge;
import finder.patterns.Pattern;
import game.Tile;
import game.TileTypes;
import javafx.scene.input.MouseEvent;

public class Brush 
{
	private TileTypes mainComponent; //Main "Color" of the brush
	private HashMap<String, Modifier> modifiers; //Side components to add to the brush
	private Bitmap drawableTiles;
	private int size;
	private Point center;
	
	public Brush()
	{
		mainComponent =  null;
		modifiers = new HashMap<String, Modifier>();
		drawableTiles = new Bitmap();
		size = 1;
	}
	
	public Brush(TileTypes brushType)
	{
		mainComponent = brushType;
		modifiers = new HashMap<String, Modifier>();
		drawableTiles = new Bitmap();
		size = 1;
	}
	
	public Brush(TileTypes brushType, String modifierName, Boolean modifierActivated)
	{
		mainComponent = brushType;
		modifiers = new HashMap<String, Modifier>();
		modifiers.put(modifierName, new Modifier(modifierActivated));
		drawableTiles = new Bitmap();
		size = 1;
	}
	
	public void SetMainComponent(TileTypes type)
	{
		mainComponent = type;
	}
	
	public TileTypes GetMainComponent()
	{
		return mainComponent;
	}
	
	public void AddmodifierComponent(String modifierName, Boolean modifierActivated)
	{
		modifiers.put(modifierName, new Modifier(modifierActivated));
	}
	
	public void AddmodifierComponent(String modifierName, Modifier modifier)
	{
		modifiers.put(modifierName, modifier);
	}
	
	public void ChangeModifierMainValue(String modifierName, Boolean modifierActivated)
	{
		if(modifiers.containsKey(modifierName))
			modifiers.get(modifierName).SetMainActive(modifierActivated);
	}	
	
	public void ChangeModifierAlternateValue(String modifierName, Boolean modifierActivated)
	{
		if(modifiers.containsKey(modifierName))
			modifiers.get(modifierName).SetAlternateActive(modifierActivated);
	}
	
	public Modifier GetModifier(String modifierName)
	{
		if(modifiers.containsKey(modifierName))
			return modifiers.get(modifierName);
		
		return null;
	}
	
	public Boolean GetModifierValue(String modifierName)
	{
		if(modifiers.containsKey(modifierName))
			return modifiers.get(modifierName).GetActive();
		
		return null;
	}
	
	public int GetBrushSize()
	{
		return size;
	}
	
	public void SetBrushSize(int value)
	{
		size = value;
	}
	
	public Bitmap GetDrawableTiles()
	{
		return drawableTiles;
	}
	
	public void UpdateDrawableTiles(int x, int y, int[][] matrix)
	{
		
		if(center != null && x == center.getX() && y == center.getY())
			return;
		
		center = new Point(x,y);
		drawableTiles = new Bitmap();
//		drawableTiles.addPoint(center); //Add the first point
		
		//Update the bitmap depending on the size of the brush
		FillDrawable(center, matrix[0].length, matrix.length, size);
	}
	
	private void FillDrawable(Point p, int width, int height, int layer)
	{
		if(layer == 0 || p.getX() < 0 || p.getX() > width - 1 || p.getY() < 0 || p.getY() > height - 1 || drawableTiles.contains(p))
			return;
		
		drawableTiles.addPoint(p);
		
		FillDrawable(new Point(p.getX() + 1, p.getY()), width, height, layer - 1);
		FillDrawable(new Point(p.getX() - 1, p.getY()), width, height, layer - 1);
		FillDrawable(new Point(p.getX(), p.getY() + 1), width, height, layer - 1);
		FillDrawable(new Point(p.getX(), p.getY() - 1), width, height, layer - 1);
	}
	
	public void UpdateModifiers(MouseEvent event)
	{
		ChangeModifierAlternateValue("Lock", event.isControlDown()); //Just checking
	}
	
	public void Update(MouseEvent event, TileTypes type)
	{
		SetMainComponent(type);
		ChangeModifierAlternateValue("Lock", event.isControlDown()); //Just checking
	}
}
