package gui.controls;

import game.Map;
import game.TileTypes;
import gui.utils.MapRenderer;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;

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
	 * Creates an instance of InteractiveMap, its contents being based on a
	 * provided Map object.
	 * 
	 * @param map A map associated with this object.
	 */
	public InteractiveMap(Map map) {
		super();
		
		this.map = map;
		cols = map.getColCount();
		rows = map.getRowCount();
		
		initialise();
	}
	
	/**
	 * Updates a tile on the map.
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param tile  The new tile.
	 */
	public synchronized void updateTile(int x, int y, TileTypes tile) {
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
	 * Initialises the controller.
	 */
	private void initialise() {
		ColumnConstraints cc = new ColumnConstraints();
		RowConstraints rc = new RowConstraints();

		setMinSize(0, 0);
		cc.setFillWidth(true);
		cc.setHgrow(Priority.ALWAYS);
		rc.setFillHeight(true);
		rc.setVgrow(Priority.ALWAYS);
		
		for (int i = 0; i < cols; i++) {
			for (int j = 0; j < rows; j++) {
				ResizableCanvas c = new ResizableCanvas();
				GridPane.setFillWidth(c, true);
				GridPane.setFillHeight(c, true);
				add(c, i, j);
				drawTile(i, j, map.getTile(i, j));
			}
		}
	}
	
	/**
	 * Gets the resizable canvas residing within a cell.
	 * 
	 * @param x The X coordinate of the cell.
	 * @param y The Y coordinate of the cell.
	 * @return The resizable canvas in the cell.
	 */
	private ResizableCanvas getCell(int x, int y) {
		return (ResizableCanvas) getChildren().get(x * cols + y);
	}
	
	/**
	 * Draws a tile onto the interactive map.
	 * 
	 * @param x The X coordinate.
	 * @param y The Y coordinate.
	 * @param tile The type of tile to draw.
	 */
	private void drawTile(int x, int y, TileTypes tile) {
		getCell(x, y).draw(renderer.renderTile(tile));
	}
}
