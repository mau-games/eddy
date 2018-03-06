package gui.controls;

import java.util.HashMap;

import game.Game;
import game.Map;
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
	
	private GridPane immutableGrid;
	
	/**
	 * Creates an empty instance of InteractiveMap.
	 */
	public InteractiveMap() {
		super();
		
//		immutableGrid = new GridPane();
//		immutableGrid.setMinSize(420, 420);
//		immutableGrid.setMaxSize(420, 420);
//		this.getChildren().add(immutableGrid);
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
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param tileType The new tile type.
	 */
	public synchronized void updateTile(int x, int y, TileTypes tileType) {
		if (map == null) {
			return;
		}
		// Let's discard any attempts at erasing the doors
		if (map.getDoors().contains(new Point(x, y))) {
			return;
		}
		
		map.setTile(x, y, tileType);
		drawTile(x, y, tileType);
	}
	
	/**
	 * Updates a tile on the map.
	 * This, dear reader, is not a beautiful way of doing things, but it works.
	 * 
	 * @param tile The tile that we want to update.
	 * @param tileType The new tile type.
	 * @param bucket If Right-clicked we perform a bucket filling instead of individual
	 */
	public synchronized void updateTile(ImageView tile, TileTypes tileType, boolean bucket) {
		if (map == null) {
			return;
		}

		Point p = coords.get(tile);
		
		// Let's discard any attempts at erasing the doors
		if (p == null
				|| map.getTile(p) == TileTypes.DOORENTER
				|| map.getTile(p) == TileTypes.DOOR) {
			return;
		}
		
		TileTypes tt= map.getTile(p);
		
		if(bucket)
		{
			//BucketFill(p, map.getTile(p), tileType);
			
			tt.SetInmutable(true);
			map.setTile(p.getX(), p.getY(), tt);
			drawTile(p.getX(), p.getY(), tt);
			return;
		}
		
		map.setTile(p.getX(), p.getY(), tileType);
		drawTile(p.getX(), p.getY(), tileType);
	}
	
	/**
	 * Flood-fill algorithm to change an area of the same target type for another
	 * @param p Position in the map
	 * @param target Target TileType that will be replaced
	 * @param replacement TileType that will replace the target tile
	 */
	private void BucketFill(Point p, TileTypes target, TileTypes replacement)
	{
		if(p.getX() < 0 || p.getX() > cols -1 || p.getY() < 0 || p.getY() > rows -1)
			return;
		
		TileTypes prev = map.getTile(p);
		
		if(prev != target || prev == replacement)
			return;
		
		map.setTile(p.getX(), p.getY(), replacement);
		drawTile(p.getX(), p.getY(), replacement);
		
		BucketFill(new Point(p.getX() + 1, p.getY()), target, replacement);
		BucketFill(new Point(p.getX() - 1, p.getY()), target, replacement);
		BucketFill(new Point(p.getX(), p.getY() + 1), target, replacement);
		BucketFill(new Point(p.getX(), p.getY() - 1), target, replacement);
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
		
		
		 for (int j = 0; j < rows; j++){
			 for (int i = 0; i < cols; i++) {
				ImageView iv = new ImageView(getImage(map.getTile(i, j), scale));
				GridPane.setFillWidth(iv, true);
				GridPane.setFillHeight(iv, true);
				add(iv, i, j);
				coords.put(iv, new Point(i, j));
				
//				immutableGrid.add(new ImageView(), i,j);
				
//				if(map.getTile(i, j).IsInmutable())
//				{
//					ImageView lockView = new ImageView(renderer.GetLock(scale, scale));
//					add(lockView, i,j);
//				}
			}
		}
	}
	
//	private Group getImageView(TileTypes type, double size) 
//	{
//		Image basicTile = images.get(type);
//		ImageView basicIV;
//		
//		if (basicTile != null) 
//		{
//			basicIV = new ImageView(basicTile);
//		}
//		else
//		{
//			basicTile = renderer.renderTile(type, size, size);
//			images.put(type, basicTile);
//			basicIV = new ImageView(basicTile);
//		}
//		
//		if(type.IsInmutable())
//		{
//			ImageView lockIV = new ImageView(renderer.GetLock(size, size));
//			lockIV.setBlendMode(BlendMode.OVERLAY);
//			Group blend = new Group(basicIV, lockIV);
//			return blend;
//		}
//		else
//		{
//			return basicIV;
//		}
//	}
	
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
//		getCell(x, y).setImage(renderer.renderTile(TileTypes.TREASURE, scale, scale));
		getCell(x, y).setImage(getImage(tile, scale));
//		if(tile.IsInmutable())
//		{
//			((ImageView)immutableGrid.getChildren().get(y * cols + x)).setImage(renderer.GetLock(scale, scale));
//		}
	}
}
