package gui.views;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.Map;
import gui.controls.InteractiveMap;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;

/**
 * his class controls the interactive application's edit view.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class EditViewController extends BorderPane implements Listener {
	
	@FXML private List<LabeledCanvas> maps;
	@FXML private StackPane mapPane;
	private InteractiveMap mapView;
	
	private boolean isActive = false;
	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	private final static Logger logger = LoggerFactory.getLogger(EditViewController.class);

	/**
	 * Creates an instance of this class.
	 */
	public EditViewController() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/interactive/EditView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		
		router.registerListener(this, new MapUpdate(null));
		
		init();
	}
	
	private void init() {
		mapView = new InteractiveMap();
		StackPane.setAlignment(mapView, Pos.CENTER);
		mapView.setMinSize(400, 400);
		mapView.setMaxSize(400, 400);
		mapPane.getChildren().add(mapView);
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof MapUpdate) {
			if (isActive) {
				updateMap((Map) e.getPayload());
			}
		}
	}
	
	/**
	 * Gets one of the maps (i.e. a labeled view displaying a map) being under
	 * this object's control.
	 * 
	 * @param index An index of a map.
	 * @return A map if it exists, otherwise null.
	 */
	public LabeledCanvas getMap(int index) {
		return maps.get(index);
	}
	
	/**
	 * Marks this control as being in an active or inactive state.
	 * 
	 * @param state The new state.
	 */
	public void setActive(boolean state) {
		isActive = state;
	}
	
	/**
	 * Updates this control's map.
	 * 
	 * @param map The new map.
	 */
	public void updateMap(Map map) {
		mapView.updateMap(map);
	}
	
	/**
	 * Gets the current map being controlled by this controller.
	 * 
	 * @return The current map.
	 */
	public Map getCurrentMap() {
		return mapView.getMap();
	}
	
	/**
	 * Renders the map, making it possible to export it.
	 * 
	 * @return A rendered version of the map.
	 */
	public Image getRenderedMap() {
		return renderer.renderMap(mapView.getMap().toMatrix());
	}
}
