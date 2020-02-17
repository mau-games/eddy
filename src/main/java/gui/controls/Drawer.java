package gui.controls;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import finder.geometry.Bitmap;
import finder.geometry.Point;
import game.Room;
import game.Tile;
import game.TileTypes;
import gui.controls.Brush.BrushUsage;
import javafx.scene.input.MouseEvent;

/***
 * This class represents the brush to be use to modify the scene
 * @author Alberto Alvarez, Malmö University
 *
 */
public class Drawer 
{
	private List<Brush> brushes;
	protected Brush brush;
	protected HashMap<String, Modifier> modifiers; //Side components to add to the brush
	
	public Drawer()
	{
		InitializeBrushes();
		modifiers = new HashMap<String, Modifier>();
	}
	
	public Drawer(TileTypes brushType)
	{
		InitializeBrushes();
		modifiers = new HashMap<String, Modifier>();
	}
	
	public Drawer(TileTypes brushType, String modifierName, Boolean modifierActivated)
	{
		InitializeBrushes();
		modifiers = new HashMap<String, Modifier>();
		modifiers.put(modifierName, new Modifier(modifierActivated));
	}
	
	private void InitializeBrushes()
	{
		brushes = Arrays.asList(new BasicBrush(), new Bucket(), new CustomBrush());
		brush = brushes.get(BrushUsage.DEFAULT.ordinal());
	}
	
	public void SetMainComponent(TileTypes type)
	{
		for(Brush b : brushes)
		{
			b.SetMainComponent(type);
		}
	}
	
	public void SetMainComponent(Tile type)
	{
		for(Brush b : brushes)
		{
			b.SetMainComponent(type);
		}
		
		brush = brushes.get(type.getBrushUsage().ordinal());
	}
	
	public TileTypes GetMainComponent()
	{
		return brush.GetMainComponent();
	}
	
	public Brush GetBrush()
	{
		return brush;
	}
	
	public void SetBrush(Brush newBrush)
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
			if(!(b instanceof CustomBrush))
				b.SetBrushSize(value);
		}
	}
	
	public boolean possibleToDraw()
	{
		return brush.canBrushDraw();
	}
	
	public Bitmap GetDrawableTiles()
	{
		return brush.GetDrawableTiles();
	}
	
	public void Draw(Point p, Room room, InteractiveMap interactiveCanvas)
	{
		brush.Draw(p, room, this, interactiveCanvas);
	}
	
	public void simulateDrawing(Point p, Room room, InteractiveMap interactiveCanvas)
	{
		brush.simulateDrawing(p, room, this, interactiveCanvas);
	}
	
	
	public void DoneDrawing()
	{
		for(Brush b : brushes)
		{
			b.SetDrew();
		}
//		brush.SetDrew();
	}
	
	/**
	 * Adds or replaces a modifier to the brush (e.g. Lock)
	 * @param modifierName The name of the modifier TODO: Should be an enum
	 * @param modifierActivated The value of the modifier
	 */
	public void AddmodifierComponent(String modifierName, Boolean modifierActivated)
	{
		modifiers.put(modifierName, new Modifier(modifierActivated));
	}
	
	/**
	 * Adds or replaces a modifier to the brush (e.g. Lock)
	 * @param modifierName The name of the modifier TODO: Should be an enum
	 * @param modifier The modifier already initialized
	 */
	public void AddmodifierComponent(String modifierName, Modifier modifier)
	{
		modifiers.put(modifierName, modifier);
	}
	
	/**
	 * Removes a modifier from the brush 
	 * @param modifierName
	 */
	public void RemoveModifier(String modifierName)
	{
		modifiers.remove(modifierName);
	}
	
	/**
	 * Changes the modifier main value
	 * (alternate value overrides temporarily the main value if positive)
	 * @param modifierName The name of the modifier TODO: Should be an enum
	 * @param modifierActivated Value of the modifier
	 */
	public void ChangeModifierMainValue(String modifierName, Boolean modifierActivated)
	{
		if(modifiers.containsKey(modifierName))
			modifiers.get(modifierName).SetMainActive(modifierActivated);
	}	
	
	
	/**
	 * Changes the modifier alternate value
	 * (alternate value overrides temporarily the main value if positive)
	 * @param modifierName The name of the modifier TODO: Should be an enum
	 * @param modifierActivated Alternate value of the modifier
	 */
	public void ChangeModifierAlternateValue(String modifierName, Boolean modifierActivated)
	{
		if(modifiers.containsKey(modifierName))
			modifiers.get(modifierName).SetAlternateActive(modifierActivated);
	}
	
	/**
	 * Searches for the specified modifier in the brush
	 * @param modifierName The name of the modifier TODO: Should be an enum
	 * @return The requested modifier if found
	 */
	public Modifier GetModifier(String modifierName)
	{
		if(modifiers.containsKey(modifierName))
			return modifiers.get(modifierName);
		
		return null;
	}
	
	/**
	 * Searches for the specified modifier and gets its value (main and alternate)
	 * @param modifierName The name of the modifier TODO: Should be an enum
	 * @return The value of the requested modifier if found
	 */
	public Boolean GetModifierValue(String modifierName)
	{
		if(modifiers.containsKey(modifierName))
			return modifiers.get(modifierName).GetActive();
		
		return false;
	}

	
	/**
	 * Update the modifiers based on the alternate mouse events
	 * @param event Mouse event over the map
	 */
	public void UpdateModifiers(MouseEvent event)
	{
		ChangeModifierAlternateValue("Lock", event.isControlDown()); //Just checking
	}
	
	/**
	 * Update the modifiers based on the alternate mouse events and changes the main brush tile
	 * @param event Mouse event over the map
	 * @param type Main brush tile type
	 */
	public void Update(MouseEvent event, TileTypes type)
	{	 
		SetMainComponent(type);
		ChangeModifierAlternateValue("Lock", event.isControlDown()); //Just checking
	}
	
	/**
	 * Update the modifiers based on the alternate mouse events and changes the main brush tile
	 * @param event Mouse event over the map
	 */
	public void Update(MouseEvent event, util.Point p, Room room)
	{
		if(!(brush instanceof CustomBrush))
		{
			if(event.isShiftDown())
				brush = brushes.get(BrushUsage.BUCKET.ordinal());
			else 
				brush = brushes.get(BrushUsage.DEFAULT.ordinal());
		}
		
		if(p != null)
			brush.UpdateDrawableTiles(p.getX(), p.getY(), room);

		ChangeModifierAlternateValue("Lock", event.isControlDown()); //Just checking
	}
}
