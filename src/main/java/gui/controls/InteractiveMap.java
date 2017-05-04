package gui.controls;

import game.Map;
import game.TileTypes;
import gui.utils.MapRenderer;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;

/**
 * InteractiveMap describes a control that may be used to edit maps.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class InteractiveMap extends GridPane {
	
	private Map map;
	private int cols = 0;
	private int rows = 0;
	
	private final MapRenderer renderer = MapRenderer.getInstance();
	
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
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param tile  The new tile.
	 */
	public synchronized void updateTile(int x, int y, TileTypes tile) {
		if (map == null) {
			return;
		}
		
		map.setTile(x, y, tile);
		drawTile(x, y, tile);
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
		cols = map.getColCount();
		rows = map.getRowCount();
		
		initialise();
	}
	
	/**
	 * Initialises the controller.
	 */
	private void initialise() {
		autosize();
		double width = getWidth() / cols;
		double height = getHeight() / rows;
		double scale = Math.min(width, height);
		
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				ImageView iv = new ImageView(renderer.renderTile(map.getTile(i, j)));
				iv.setFitWidth(scale);
				iv.setPreserveRatio(true);
				iv.setSmooth(true);
				iv.setCache(true);
				GridPane.setFillWidth(iv, true);
				GridPane.setFillHeight(iv, true);
				add(iv, i, j);
			}
		}
	}
	
	/**
	 * Gets the image view residing within a cell.
	 * 
	 * @param x The X coordinate of the cell.
	 * @param y The Y coordinate of the cell.
	 * @return The image view in the cell.
	 */
	private ImageView getCell(int x, int y) {
		return (ImageView) getChildren().get(x * cols + y);
	}
	
	/**
	 * Draws a tile onto the interactive map.
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param tile The type of tile to draw.
	 */
	private void drawTile(int x, int y, TileTypes tile) {
		System.out.println("Drawing " + tile.toString());
		getCell(x, y).setImage(renderer.renderTile(tile));
	}
}
