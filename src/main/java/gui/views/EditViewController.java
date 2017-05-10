package gui.views;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import finder.patterns.Pattern;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Room;
import game.Map;
import game.TileTypes;
import game.Game.MapMutationType;
import gui.controls.InteractiveMap;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.StartMapMutate;

/**
 * his class controls the interactive application's edit view.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class EditViewController extends BorderPane implements Listener {
	
	@FXML private List<LabeledCanvas> mapDisplays;
	@FXML private StackPane mapPane;
	@FXML private ToggleGroup brushes;
	@FXML private ToggleButton patternButton;
	private InteractiveMap mapView;
	private Canvas patternCanvas;
	private Canvas warningCanvas;
	
	private boolean isActive = false;
	private boolean isFeasible = true;
	private TileTypes brush = null;
	private HashMap<Integer, Map> maps = new HashMap<Integer, Map>();
	private int nextMap = 0;
	
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
	
	/**
	 * Initialises the edit view.
	 */
	private void init() {
		initMapView();
		initMiniMaps();
	}

	/**
	 * Initialises the map view and creates canvases for pattern drawing and
	 * infeasibility notifications.
	 */
	private void initMapView() {
		int width = 420;
		int height = 420;
		
		mapView = new InteractiveMap();
		StackPane.setAlignment(mapView, Pos.CENTER);
		mapView.setMinSize(width, height);
		mapView.setMaxSize(width, height);
		mapPane.getChildren().add(mapView);
		
		patternCanvas = new Canvas(width, height);
		StackPane.setAlignment(patternCanvas, Pos.CENTER);
		mapPane.getChildren().add(patternCanvas);
		patternCanvas.setVisible(false);
		patternCanvas.setMouseTransparent(true);
		
		warningCanvas = new Canvas(width, height);
		StackPane.setAlignment(warningCanvas, Pos.CENTER);
		mapPane.getChildren().add(warningCanvas);
		warningCanvas.setVisible(false);
		warningCanvas.setMouseTransparent(true);
		
		GraphicsContext gc = warningCanvas.getGraphicsContext2D();
		gc.setStroke(Color.rgb(255, 0, 0, 1.0));
		gc.setLineWidth(3);
		gc.strokeRect(1, 1, width - 1, height - 1);
		gc.setLineWidth(1);
		gc.setStroke(Color.rgb(255, 0, 0, 0.9));
		gc.strokeRect(3, 3, width - 6, height - 6);
		gc.setStroke(Color.rgb(255, 0, 0, 0.8));
		gc.strokeRect(4, 4, width - 8, height - 8);
		gc.setStroke(Color.rgb(255, 0, 0, 0.7));
		gc.strokeRect(5, 5, width - 10, height - 10);
		gc.setStroke(Color.rgb(255, 0, 0, 0.6));
		gc.strokeRect(6, 6, width - 12, height - 12);
		gc.setStroke(Color.rgb(255, 0, 0, 0.5));
		gc.strokeRect(7, 7, width - 14, height - 14);
		gc.setStroke(Color.rgb(255, 0, 0, 0.4));
		gc.strokeRect(8, 8, width - 16, height - 16);
		gc.setStroke(Color.rgb(255, 0, 0, 0.3));
		gc.strokeRect(9, 9, width - 18, height - 18);
		gc.setStroke(Color.rgb(255, 0, 0, 0.2));
		gc.strokeRect(10, 10, width - 20, height - 20);
		gc.setStroke(Color.rgb(255, 0, 0, 0.1));
		gc.strokeRect(11, 11, width - 22, height - 22);
	}

	/**
	 * Intialises the mini map view.
	 */
	private void initMiniMaps() {
		mapView.addEventFilter(MouseEvent.MOUSE_CLICKED, new EditViewEventHandler());
		getMap(0).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
			replaceMap(0);
		});
		getMap(1).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
			replaceMap(0);
		});
		getMap(2).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
			replaceMap(0);
		});
		getMap(3).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
			replaceMap(0);
		});
		resetMiniMaps();
	}
	
	/**
	 * Resets the mini maps for a new run of map generation.
	 */
	private void resetMiniMaps() {
		nextMap = 0;
		
		getMap(0).draw(null);
		getMap(0).setText("Waiting for map...");

		getMap(1).draw(null);
		getMap(1).setText("Waiting for map...");

		getMap(2).draw(null);
		getMap(2).setText("Waiting for map...");

		getMap(3).draw(null);
		getMap(3).setText("Waiting for map...");
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof MapUpdate) {
			if (isActive) {
				Map map = (Map) ((MapUpdate) e).getPayload();
				UUID uuid = ((MapUpdate) e).getID();
				LabeledCanvas canvas = mapDisplays.get(nextMap);
//				canvas.setText("Got map:\n" + uuid);
				canvas.setText("");
				maps.put(nextMap, map);
				
				Platform.runLater(() -> {
					int[][] matrix = map.toMatrix();
					canvas.draw(renderer.renderMap(matrix));
				});
				
				nextMap++;
			}
		}
	}
	
	/**
	 * Gets the interactive map.
	 * 
	 * @return An instance of InteractiveMap, if it exists.
	 */
	public InteractiveMap getMap() {
		return mapView;
	}
	
	/**
	 * Gets one of the maps (i.e. a labeled view displaying a map) being under
	 * this object's control.
	 * 
	 * @param index An index of a map.
	 * @return A map if it exists, otherwise null.
	 */
	public LabeledCanvas getMap(int index) {
		return mapDisplays.get(index);
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
		redrawPatterns(map);
		mapIsFeasible(map.isFeasible());
		resetMiniMaps();
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
	
	/**
	 * Selects a brush.
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 * I'm sorry, this is a disgusting way of handling things...
	 */
	public void selectBrush() {
		if (brushes.getSelectedToggle() == null) {
			brush = null;
			mapView.setCursor(Cursor.DEFAULT);
		} else {
			mapView.setCursor(Cursor.HAND);
			
			switch (((ToggleButton) brushes.getSelectedToggle()).getText()) {
			case "Floor":
				brush = TileTypes.FLOOR;
				break;
			case "Wall":
				brush = TileTypes.WALL;
				break;
			case "Treasure":
				brush = TileTypes.TREASURE;
				break;
			case "Enemy":
				brush = TileTypes.ENEMY;
				break;
			}
		}
	}
	
	/**
	 * Toggles the display of patterns on top of the map.
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	public void togglePatterns() {
		if (patternButton.isSelected()) {
			patternCanvas.setVisible(true);
		} else {
			patternCanvas.setVisible(false);
		}
	}
	
	/**
	 * Generates four new mini maps.
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	public void generateNewMaps() {
		resetMiniMaps();
		generateNewMaps(mapView.getMap());
	}
	
	/**
	 * Marks the map as being infeasible.
	 * 
	 * @param state
	 */
	public void mapIsFeasible(boolean state) {
		isFeasible = state;
		
		warningCanvas.setVisible(!isFeasible);
	}
	
	/**
	 * Generates four new mini maps.
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	public void generateNewMaps(Map map) {
		// TODO: If we want more diversity in the generated maps, then send more StartMapMutate events.
		router.postEvent(new StartMapMutate(map, MapMutationType.Preserving, 4, true)); //TODO: Move some of this hard coding to ApplicationConfig
	}
	
	/**
	 * Replaces the map with one of the generated ones.
	 * 
	 * @param index The new map's index.
	 */
	private void replaceMap(int index) {
		Map map = maps.get(index);
		if (map != null) {
			generateNewMaps(map);
			updateMap(map);
		}
	}
	
	/**
	 * Composes a list of micro patterns with their respective colours for the
	 * map renderer to use.
	 * 
	 * @param patterns The patterns to analyse.
	 * @return A map that maps each pattern instance to a colour.
	 */
	private HashMap<Pattern, Color> colourPatterns(List<Pattern> patterns) {
		HashMap<Pattern, Color> patternMap = new HashMap<Pattern, Color>();
		
		patterns.forEach((pattern) -> {
			if (pattern instanceof Room) {
				patternMap.put(pattern, Color.BLUE);
			} else if (pattern instanceof Corridor) {
				patternMap.put(pattern, Color.RED);
			}
		});
		
		return patternMap;
	}

	/**
	 * Redraws the pattern, based on the current map layout.
	 * 
	 * @param container
	 */
	private void redrawPatterns(Map map) {
		patternCanvas.getGraphicsContext2D().clearRect(0, 0, 420, 420);
		renderer.drawPatterns(patternCanvas.getGraphicsContext2D(), map.toMatrix(), colourPatterns(map.getPatternFinder().findMicroPatterns()));
		renderer.drawGraph(patternCanvas.getGraphicsContext2D(), map.toMatrix(), map.getPatternFinder().getPatternGraph());
		renderer.drawMesoPatterns(patternCanvas.getGraphicsContext2D(), map.toMatrix(), map.getPatternFinder().getMesoPatterns());
	}
	
	/*
	 * Event handlers
	 */
	private class EditViewEventHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			if (event.getTarget() instanceof ImageView && brush != null) {
				// Edit the map
				ImageView tile = (ImageView) event.getTarget();
				mapView.updateTile(tile, brush);
				mapView.getMap().forceReevaluation();
				mapIsFeasible(mapView.getMap().isFeasible());
				redrawPatterns(mapView.getMap());
			}
		}
		
	}
}
