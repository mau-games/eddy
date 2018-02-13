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
import game.MapContainer;
import game.TileTypes;
import game.Game.MapMutationType;
import gui.InteractiveGUIController;
import gui.controls.InteractiveMap;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import runners.InteractiveGUI;
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
	//@FXML private Pane root;
	@FXML private GridPane legend;
	@FXML private ToggleGroup brushes;
	@FXML private ToggleButton patternButton;
	
	private Button rightButton = new Button();
	private Button leftButton = new Button();
	private Button upButton = new Button();
	private Button downButton = new Button();
	
	private boolean mousePressed = false;
	private Map selectedMiniMap;

	//@FXML private AnchorPane interactivePane;


	private InteractiveMap mapView;
	private Map largeMap;
	private Canvas patternCanvas;
	private Canvas warningCanvas;
	private Canvas buttonCanvas;
	
	private MapContainer map;

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
	}

	/**
	 * Initialises the edit view.
	 */
	private void init() {
		initMapView();
		//initMiniMaps();
		initLegend();

	}

	/**
	 * Initialises the map view and creates canvases for pattern drawing and
	 * infeasibility notifications.
	 */
	private void initMapView() {
		int width = 420;
		int height = 420;

		Pane root = new Pane();

		setMapView(new InteractiveMap());
		StackPane.setAlignment(getMapView(), Pos.CENTER);
		getMapView().setMinSize(width, height);
		getMapView().setMaxSize(width, height);
		mapPane.getChildren().add(getMapView());

		
		patternCanvas = new Canvas(width, height);
		StackPane.setAlignment(patternCanvas, Pos.CENTER);
		mapPane.getChildren().add(patternCanvas);
		patternCanvas.setVisible(false);
		patternCanvas.setMouseTransparent(true);

		buttonCanvas = new Canvas(width + 120, height + 120);
		StackPane.setAlignment(buttonCanvas, Pos.CENTER);
		mapPane.getChildren().add(buttonCanvas);
		buttonCanvas.setVisible(false);
		buttonCanvas.setMouseTransparent(true);

	

		getRightButton().setText("right");
		getLeftButton().setText("left");
		getUpButton().setText("up");
		getDownButton().setText("bot");
		
		
		

		getRightButton().setTranslateX(300);
		//rightButton.setTranslateY(100);

		getLeftButton().setTranslateX(-300);
		//leftButton.setTranslateY(-100);

		//upButton.setTranslateX(300);
		getUpButton().setTranslateY(-250);

		//botButton.setTranslateX(300);
		getDownButton().setTranslateY(250);




		mapPane.getChildren().add(getUpButton());
		mapPane.getChildren().add(getDownButton());
		mapPane.getChildren().add(getRightButton());
		mapPane.getChildren().add(getLeftButton());

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
//	private void initMiniMaps() {
//		getMapView().addEventFilter(MouseEvent.MOUSE_CLICKED, new EditViewEventHandler());
//		getMap(0).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
//			replaceMap(0);
//			setMousePressed(true);
//			System.out.println("THE MOUSE IS PRESSED: " + mousePressed);
//		});
//		getMap(1).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
//			replaceMap(1);
//			setMousePressed(true);
//			System.out.println("THE MOUSE IS PRESSED: " + mousePressed);
//
//
//		});
//		getMap(2).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
//			replaceMap(2);
//			setMousePressed(true);
//			System.out.println("THE MOUSE IS PRESSED: " + mousePressed);
//
//
//		});
//		getMap(3).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
//			replaceMap(3);
//			setMousePressed(true);
//			System.out.println("THE MOUSE IS PRESSED: " + mousePressed);
//
//
//		});
//		resetMiniMaps();
//		setMousePressed(false);
//
//	}
	
	public void setContainer(MapContainer map) {
		map = this.map;
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
	public void resetMiniMaps() {
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
		return getMapView();
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
		getMapView().updateMap(map);
		System.out.println("CURRENT MAP VIEW: ");
		System.out.println(getMapView().getMap());
		redrawPatterns(map);
		mapIsFeasible(map.isFeasible());
		resetMiniMaps();
	}
	
	public void updateRoom(Map map) {
		getMapView().updateMap(map);
		
		redrawPatterns(map);
		mapIsFeasible(map.isFeasible());
		//resetMiniMaps();
	}
	
	public void updateLargeMap(Map map) {
		setLargeMap(map);				
	}


	/**
	 * Gets the current map being controlled by this controller.
	 * 
	 * @return The current map.
	 */
	public Map getCurrentMap() {
		
		return getMapView().getMap();
	}

	/**
	 * Renders the map, making it possible to export it.
	 * 
	 * @return A rendered version of the map.
	 */
	public Image getRenderedMap() {
		return renderer.renderMap(getMapView().getMap().toMatrix());
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
			getMapView().setCursor(Cursor.DEFAULT);
		} else {
			getMapView().setCursor(Cursor.HAND);

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
		generateNewMaps(getMapView().getMap());
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
	public void replaceMap(int index) {
		selectedMiniMap = maps.get(index);
//		Map map = maps.get(index);
		System.out.println("SELECTED MINI MAP INSIDE EDITVIEW: ");		
		System.out.println(selectedMiniMap.toString());
		if (selectedMiniMap != null) {
			generateNewMaps(selectedMiniMap);
			updateMap(selectedMiniMap);

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
		patternCanvas.getGraphicsContext2D().clearRect(0, 0, 420, 420);
		renderer.drawPatterns(patternCanvas.getGraphicsContext2D(), map.toMatrix(), colourPatterns(map.getPatternFinder().findMicroPatterns()));
		renderer.drawGraph(patternCanvas.getGraphicsContext2D(), map.toMatrix(), map.getPatternFinder().getPatternGraph());
		renderer.drawMesoPatterns(patternCanvas.getGraphicsContext2D(), map.toMatrix(), map.getPatternFinder().getMesoPatterns());
	}

	/*
	 * Event handlers
	 */
	public class EditViewEventHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			if (event.getTarget() instanceof ImageView && brush != null) {
				// Edit the map
				ImageView tile = (ImageView) event.getTarget();
				getMapView().updateTile(tile, brush);
				getMapView().getMap().forceReevaluation();
				mapIsFeasible(getMapView().getMap().isFeasible());
				redrawPatterns(getMapView().getMap());
			}
		}

	}


	@FXML
	private String handleButtonAction(ActionEvent event) throws IOException {

		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/WorldMap.fxml"));
		Parent root1 = (Parent) fxmlLoader.load();
		Stage stage = new Stage();
		stage.setTitle("ABC");
		stage.setScene(new Scene(root1));
		stage.show();

		return null;

	}
	
	
	





	public void testMethod() {
		System.out.println("hello");
	}

	public Button getRightButton() {
		return rightButton;
	}

	

	public Button getLeftButton() {
		return leftButton;
	}

	

	public Button getUpButton() {
		return upButton;
	}

	

	public Button getDownButton() {
		return downButton;
	}

	public boolean isMousePressed() {
		return mousePressed;
	}

	public void setMousePressed(boolean mousePressed) {
		this.mousePressed = mousePressed;
	}

	public Map getSelectedMiniMap() {
		return selectedMiniMap;
	}

	public void setSelectedMiniMap(Map selectedMiniMap) {
		this.selectedMiniMap = selectedMiniMap;
	}

	public InteractiveMap getMapView() {
		return mapView;
	}

	public void setMapView(InteractiveMap mapView) {
		this.mapView = mapView;
	}

	public Map getLargeMap() {
		return largeMap;
	}

	public void setLargeMap(Map largeMap) {
		this.largeMap = largeMap;
	}

	
}
