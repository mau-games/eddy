package gui.views;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import finder.patterns.Pattern;
import finder.patterns.micro.Connector;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Room;
import game.ApplicationConfig;
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
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
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
	@FXML private GridPane legend;
	@FXML private ToggleGroup brushes;
	@FXML private ToggleButton patternButton;
	@FXML private ToggleButton zoneButton;
	@FXML private Slider zoneSlider;
	
	private InteractiveMap mapView;
	private Canvas patternCanvas;
	private Canvas warningCanvas;
	private Canvas zoneCanvas;
	private Canvas lockCanvas;
	
	private boolean isActive = false;
	private boolean isFeasible = true;
	private TileTypes brush = null;
	private HashMap<Integer, Map> maps = new HashMap<Integer, Map>();
	private int nextMap = 0;
	
	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	private final static Logger logger = LoggerFactory.getLogger(EditViewController.class);
	private ApplicationConfig config;

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
			config = ApplicationConfig.getInstance();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read config file.");
		}
		
		router.registerListener(this, new MapUpdate(null));
		
		init();
		
		zoneSlider.valueProperty().addListener((obs, oldval, newVal) -> { 
			redrawPatterns(mapView.getMap());
			});
	}
	
	/**
	 * Initialises the edit view.
	 */
	private void init() {
		initMapView();
		initMiniMaps();
		initLegend();
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
		
		lockCanvas = new Canvas(width, height);
		StackPane.setAlignment(lockCanvas, Pos.CENTER);
		mapPane.getChildren().add(lockCanvas);
		lockCanvas.setVisible(true);
		lockCanvas.setMouseTransparent(true);
		lockCanvas.setOpacity(0.5f);
		
//		lockCanvas.getGraphicsContext2D().draw
		
		zoneCanvas = new Canvas(width, height);
		StackPane.setAlignment(zoneCanvas, Pos.CENTER);
		mapPane.getChildren().add(zoneCanvas);
		zoneCanvas.setVisible(false);
		zoneCanvas.setMouseTransparent(true);
		
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
			replaceMap(1);
		});
		getMap(2).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
			replaceMap(2);
		});
		getMap(3).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
			replaceMap(3);
		});
		resetMiniMaps();
	}
	
	/**
	 * Initialises the legend view.
	 */
	private void initLegend() {
		ConfigurationUtility c = config.getInternalConfig();
		
		legend.setVgap(10);
		legend.setHgap(10);
		legend.setPadding(new Insets(10, 10, 10, 10));
		
		Label title = new Label("Pattern legend");
		title.setStyle("-fx-font-weight: bold");
		legend.add(title, 0, 0, 2, 1);
		
		legend.add(new ImageView(new Image(c.getString("map.tiles.doorenter"), 40, 40, false, false)), 0, 1);
		legend.add(new Label("Entrance door"), 1, 1);
		
		legend.add(new ImageView(new Image(c.getString("map.tiles.door"), 40, 40, false, false)), 0, 2);
		legend.add(new Label("Door"), 1, 2);
		
		legend.add(new ImageView(new Image(c.getString("map.mesopatterns.ambush"), 40, 40, false, false)), 0, 3);
		legend.add(new Label("Ambush"), 1, 3);
		
		legend.add(new ImageView(new Image(c.getString("map.mesopatterns.guard_room"), 40, 40, false, false)), 0, 4);
		legend.add(new Label("Guard chamber"), 1, 4);
		
		legend.add(new ImageView(new Image(c.getString("map.mesopatterns.guarded_treasure"), 40, 40, false, false)), 0, 5);
		legend.add(new Label("Guarded treasure"), 1, 5);
		
		legend.add(new ImageView(new Image(c.getString("map.mesopatterns.treasure_room"), 40, 40, false, false)), 0, 6);
		legend.add(new Label("Treasure chamber"), 1, 6);
		
		legend.add(new ImageView(new Image(c.getString("map.examples.chamber"), 40, 40, true, true)), 0, 7);
		legend.add(new Label("Chamber"), 1, 7);
		
		legend.add(new ImageView(new Image(c.getString("map.examples.corridor"), 40, 40, true, true)), 0, 8);
		legend.add(new Label("Corridor"), 1, 8);
		
		legend.add(new ImageView(new Image(c.getString("map.examples.connector"), 40, 40, true, true)), 0, 9);
		legend.add(new Label("Connector"), 1, 9);
		
		legend.add(new ImageView(new Image(c.getString("map.examples.dead_end"), 40, 40, true, true)), 0, 10);
		legend.add(new Label("Dead end"), 1, 10);
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
	public void ping(PCGEvent e) {
		if (e instanceof MapUpdate) {
			if (isActive) {
				Map map = (Map) ((MapUpdate) e).getPayload();
				UUID uuid = ((MapUpdate) e).getID();
				LabeledCanvas canvas;
				synchronized (mapDisplays) {
					canvas = mapDisplays.get(nextMap);
//					canvas.setText("Got map:\n" + uuid);
					canvas.setText("");
					maps.put(nextMap, map);
					nextMap++;
				}
				
				Platform.runLater(() -> {
					int[][] matrix = map.toMatrix();
					canvas.draw(renderer.renderMap(matrix));
				});
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
	 * Toggles the display of zones on top of the map.
	 * 
	 */
	public void toggleZones() {
		if (zoneButton.isSelected()) {
			zoneCanvas.setVisible(true);
		} else {
			zoneCanvas.setVisible(false);
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
		router.postEvent(new StartMapMutate(map, MapMutationType.Preserving, 2, true)); //TODO: Move some of this hard coding to ApplicationConfig
		router.postEvent(new StartMapMutate(map, MapMutationType.ComputedConfig, 2, true)); //TODO: Move some of this hard coding to ApplicationConfig
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
			} else if (pattern instanceof Connector) {
				patternMap.put(pattern, Color.YELLOW);
			}
		});
		
		return patternMap;
	}

	/**
	 * Redraws the pattern, based on the current map layout.
	 * 
	 * @param container
	 */
	private synchronized void redrawPatterns(Map map) {
		//Change those 2 width and height hardcoded values (420,420)
		patternCanvas.getGraphicsContext2D().clearRect(0, 0, 420, 420);
		zoneCanvas.getGraphicsContext2D().clearRect(0, 0, 420, 420);
		
		renderer.drawPatterns(patternCanvas.getGraphicsContext2D(), map.toMatrix(), colourPatterns(map.getPatternFinder().findMicroPatterns()));
		renderer.drawGraph(patternCanvas.getGraphicsContext2D(), map.toMatrix(), map.getPatternFinder().getPatternGraph());
		renderer.drawMesoPatterns(patternCanvas.getGraphicsContext2D(), map.toMatrix(), map.getPatternFinder().getMesoPatterns());
		renderer.drawZones(zoneCanvas.getGraphicsContext2D(), map.toMatrix(), map.root, (int)(zoneSlider.getValue()),Color.BLACK);
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
				mapView.updateTile(tile, brush, event.getButton() == MouseButton.SECONDARY);
				mapView.getMap().forceReevaluation();
				mapIsFeasible(mapView.getMap().isFeasible());
				redrawPatterns(mapView.getMap());
				redrawLocks(mapView.getMap());
			}
		}
		
	}
	
	private void redrawLocks(Map map)
	{
		lockCanvas.getGraphicsContext2D().clearRect(0, 0, 420, 420);
		
		for(int i = 0; i < map.getRowCount(); ++i)
		{
			for(int j = 0; j < map.getColCount(); ++j)
			{
				if(map.getTile(j, i).GetImmutable())
				{
					lockCanvas.getGraphicsContext2D().drawImage(renderer.GetLock(mapView.scale * 0.75f, mapView.scale * 0.75f), j * mapView.scale, i * mapView.scale);
				}
			}
		}
	}
}
