package gui.controls;

import java.util.HashMap;

import game.Game;
import game.Map;
import game.Tile;
import game.TileTypes;
import gui.utils.MapRenderer;
import javafx.scene.Group;
import javafx.scene.effect.BlendMode;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import util.Point;

/**
 * InteractiveMap describes a control that may be used to edit maps.
 * 
 * @author Johan Holmberg, Malmö University
 * @modified Alberto Alvarez, Malmö University
 */
public class InteractiveMap extends GridPane {
	
	private Map map;
	private int cols = 0;
	private int rows = 0;
	public double scale = 0;
	
	private final MapRenderer renderer = MapRenderer.getInstance();
	private final HashMap<ImageView, Point> coords = new HashMap<ImageView, Point>();
	private final static HashMap<TileTypes, Image> images = new HashMap<TileTypes, Image>();
	
	/**
	 * Creates an empty instance of InteractiveMap.
	 */
	public InteractiveMap() {
		super();
		
	}
	
	/**
	 * Creates an instance of InteractiveMap, its contents being based on a
	 * provided Map object.
	 * 
	 * @param map A map associated with this object.
	 */
	public InteractiveMap(Map map) {
		super();
		updateMap(map);
	}
	
	/**
	 * Updates a tile on the map.
	 * This, dear reader, is not a beautiful way of doing things, but it works.
	 * @modified Alberto Alvarez
	 * 
	 * @param tile The tile that we want to update.
	 * @param brush The brush that we will use paint the map (the brush contains all the positions that will be modified)
	 */
	public synchronized void updateTile(ImageView tile, Drawer brush) {
		if (map == null) {
			return;
		}

		//Safety checkup
		Point p = coords.get(tile);
		Tile currentTile = map.getTile(p);
		
//		// Let's discard any attempts at erasing the doors
//		if (map.getDoors().contains(new Point(x, y))) {
//			return;
//		}
				
		
		// Let's discard any attempts at erasing the doors
		if (p == null
				|| currentTile.GetType() == TileTypes.DOORENTER
				|| currentTile.GetType() == TileTypes.DOOR) {
			return;
		}
		
		//The brush has all the points that will be modified
		for(finder.geometry.Point position : brush.GetDrawableTiles().getPoints())
		{
			currentTile = map.getTile(position.getX(), position.getY());
			
			// Let's discard any attempts at erasing the doors
			if(currentTile.GetType() == TileTypes.DOORENTER
				|| currentTile.GetType() == TileTypes.DOOR)
				continue;
			
			currentTile.SetImmutable(brush.GetModifierValue("Lock"));
			if(brush.GetMainComponent() != null)
			{
				map.setTile(position.getX(), position.getY(), brush.GetMainComponent());
				drawTile(position.getX(), position.getY(), brush.GetMainComponent());
			}
		}
		
		brush.DoneDrawing();
	}

	public Point CheckTile(ImageView tile)
	{
		if (map == null) {
			return null;
		}

		Point p = coords.get(tile);
		return p;
	} 
	
	/**
	 * Gets the map associated with this control.
	 * 
	 * @return The map associated with this control.
	 */
	public Map getMap() {
		return map;
	}
	
	/**
	 * Populates this control with a new map.
	 * 
	 * @param map A new map.
	 */
	public void updateMap(Map map) {
		this.map = map;
		
		if (cols != map.getColCount() || rows != map.getRowCount()) {
			cols = map.getColCount();
			rows = map.getRowCount();
			images.clear();
		}
		
		initialise();
	}
	
	/**
	 * Initialises the controller.
	 */
	private void initialise() {
		autosize();
		double width = getWidth() / cols;
		double height = getHeight() / rows;
		scale = Math.min(width, height);

		getChildren().clear();
		coords.clear();
		
		
		 for (int j = 0; j < rows; j++)
		 {
			 for (int i = 0; i < cols; i++) 
			 {
				ImageView iv = new ImageView(getImage(map.getTile(i, j).GetType(), scale));
				GridPane.setFillWidth(iv, true);
				GridPane.setFillHeight(iv, true);
				add(iv, i, j);
				coords.put(iv, new Point(i, j));
			}
	 	}
	}
	
	private Image getImage(TileTypes type, double size) 
	{
		Image tile = images.get(type);
		
		if (tile != null) {
			return tile;
		}
		
		tile = renderer.renderTile(type, size, size);
		images.put(type, tile);
		
		return tile;
	}
	
	/**
	 * Gets the image view residing within a cell.
	 * 
	 * @param x The X coordinate of the cell.
	 * @param y The Y coordinate of the cell.
	 * @return The image view in the cell.
	 */
	private ImageView getCell(int x, int y) {
		return (ImageView) getChildren().get(y * cols + x);
	}
	
	/**
	 * Draws a tile onto the interactive map.
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param tile The type of tile to draw.
	 */
	private void drawTile(int x, int y, TileTypes tile) 
	{
		getCell(x, y).setImage(getImage(tile, scale));
	}
}
