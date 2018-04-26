package gui.views;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
import gui.views.RoomViewController.EditViewEventHandler;
import gui.views.WorldViewController.MouseEventHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
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
import util.eventrouting.events.ApplySuggestion;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestAppliedMap;
import util.eventrouting.events.RequestRoomView;
import util.eventrouting.events.RequestWorldView;
import util.eventrouting.events.StartMapMutate;
import util.eventrouting.events.Stop;
import util.eventrouting.events.SuggestedMapsDone;
import util.eventrouting.events.SuggestedMapsLoading;
import util.eventrouting.events.UpdateMiniMap;

/**
 * This class controls the interactive application's edit view.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class RoomViewController extends BorderPane implements Listener {

	@FXML private List<LabeledCanvas> mapDisplays;
	@FXML public StackPane mapPane;
	@FXML private StackPane buttonsPane;

	//@FXML private Pane root;
	@FXML private GridPane legend;
	@FXML private ToggleGroup brushes;
	@FXML private ToggleButton patternButton;
	@FXML private ToggleButton floorBtn;
	@FXML private ToggleButton wallBtn;
	@FXML private ToggleButton treasureBtn;
	@FXML private ToggleButton enemyBtn;

	@FXML private Label enemyNumbr;
	@FXML private Label enemyNumbr2;
	@FXML private Label treasureNmbr;
	@FXML private Label treasureNmbr2;
	@FXML private Label treasurePercent;
	@FXML private Label treasurePercent2;
	@FXML private Label enemyPercent;
	@FXML private Label enemyPercent2;
	@FXML private Label entranceSafety;
	@FXML private Label entranceSafety2;
	@FXML private Label treasureSafety;
	@FXML private Label treasureSafety2;

	@FXML private Button updateMiniMapBtn;
	@FXML private Button worldGridBtn;
	@FXML private Button genSuggestionsBtn;
	@FXML private Button appSuggestionsBtn;


	@FXML GridPane minimap;

	private Node oldNode;

	private Button rightButton = new Button();
	private Button leftButton = new Button();
	private Button upButton = new Button();
	//	private Button upButton;
	private Button downButton = new Button();

	private boolean mousePressed = false;
	private Map selectedMiniMap;

	//@FXML private AnchorPane interactivePane;


	private InteractiveMap mapView;
	private Map largeMap;
	private Canvas patternCanvas;
	private Canvas warningCanvas;

	private MapContainer map;

	private boolean isActive = false;
	private boolean isFeasible = true;
	private TileTypes brush = null;
	public HashMap<Integer, Map> maps = new HashMap<Integer, Map>();
	private int nextMap = 0;

	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	private final static Logger logger = LoggerFactory.getLogger(RoomViewController.class);
	private ApplicationConfig config;

	private int prevRow;
	private int prevCol;

	private int requestedSuggestion;

	private boolean minimapBoolean = false;





	/**
	 * Creates an instance of this class.
	 */
	public RoomViewController() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/interactive/RoomView.fxml"));
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
		router.registerListener(this, new ApplySuggestion(0));

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


		getRightButton().setText("right");
		getLeftButton().setText("left");
		getUpButton().setText("up");
		getDownButton().setText("bot");


		getRightButton().setTranslateX(300);

		getLeftButton().setTranslateX(-300);

		getUpButton().setTranslateY(-300);

		getDownButton().setTranslateY(300);

		getPatternButton().setMinWidth(75);
		floorBtn.setMinWidth(75);
		wallBtn.setMinWidth(75);
		enemyBtn.setMinWidth(75);
		treasureBtn.setMinWidth(75);


		StackPane.setAlignment(getUpButton(), Pos.CENTER);
		StackPane.setAlignment(getDownButton(), Pos.CENTER);
		StackPane.setAlignment(getRightButton(), Pos.CENTER);
		StackPane.setAlignment(getLeftButton(), Pos.CENTER);

		mapPane.getChildren().add(getUpButton());
		mapPane.getChildren().add(getDownButton());
		mapPane.getChildren().add(getRightButton());
		mapPane.getChildren().add(getLeftButton());

		getWorldGridBtn().setTooltip(new Tooltip("View your world map"));
		getGenSuggestionsBtn().setTooltip(new Tooltip("Generate new maps according to the current map view"));
		getAppSuggestionsBtn().setTooltip(new Tooltip("Change the current map view with your selected generated map"));
		getUpdateMiniMapBtn().setTooltip(new Tooltip("Refresh your minimap view"));

		getPatternButton().setTooltip(new Tooltip("Toggle the game design patterns for the current map"));

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

	public void updateMiniMap(MapContainer[][] minimapMatrix) {
		minimap.getChildren().clear();
		int size = minimapMatrix.length;
		int viewSize = 450/size;
		for (int i = 0; i < minimapMatrix.length; i++) {
			for (int j = 0; j < minimapMatrix.length; j++) {

				for (int o = 0; o < minimapMatrix[i][j].getMap().toMatrix().length; o++) {
					for (int p = 0; p < minimapMatrix[i][j].getMap().toMatrix().length; p++) {
					}
				}

				LabeledCanvas canvas = new LabeledCanvas();
				canvas.setText("");
				canvas.setPrefSize(viewSize, viewSize);
				canvas.draw(renderer.renderMap(minimapMatrix[j][i].getMap().toMatrix()));
				for (int outer = 0; outer < minimapMatrix[i][j].getMap().toMatrix().length; outer++) {
					for (int inner = 0; inner < minimapMatrix[i][j].getMap().toMatrix().length; inner++) {
					}
				}
				minimap.add(canvas, i, j);
				canvas.addEventFilter(MouseEvent.MOUSE_CLICKED,
						new MouseEventHandler());
			}
		}
	}

	public void updatePosition(int row, int col) {
		for (Node node : minimap.getChildren()) {
			if (GridPane.getColumnIndex(node) == prevCol && GridPane.getRowIndex(node) == prevRow) {
				node.setStyle("-fx-background-color:#2c2f33;");

			}
		}
		prevRow = row;
		prevCol = col;
		for (Node node : minimap.getChildren()) {
			if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
				node.setStyle("-fx-background-color:#fcdf3c;");
				node.setOnMouseExited(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						node.setStyle("-fx-background-color:#fcdf3c;");

					}
				});
				node.setOnMouseEntered(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						node.setStyle("-fx-background-color:#fcdf3c;");

					}
				});
			}
		}
	}

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
		title.setStyle("-fx-text-fill: white;");
		legend.add(title, 0, 0, 2, 1);

		legend.add(new ImageView(new Image(c.getString("map.tiles.doorenter"), 40, 40, false, false)), 0, 1);
		Label entrance = new Label("Entrance door");
		entrance.setStyle("-fx-text-fill: white;");
		legend.add(entrance, 1, 1);

		legend.add(new ImageView(new Image(c.getString("map.tiles.door"), 40, 40, false, false)), 0, 2);
		Label door = new Label("Door");
		door.setStyle("-fx-text-fill: white;");
		legend.add(door, 1, 2);

		legend.add(new ImageView(new Image(c.getString("map.mesopatterns.ambush"), 40, 40, false, false)), 0, 3);
		Label ambush = new Label("Ambush");
		ambush.setStyle("-fx-text-fill: white;");
		legend.add(ambush, 1, 3);

		legend.add(new ImageView(new Image(c.getString("map.mesopatterns.guard_room"), 40, 40, false, false)), 0, 4);
		Label guardChamber = new Label("Guard chamber");
		guardChamber.setStyle("-fx-text-fill: white;");
		legend.add(guardChamber, 1, 4);

		legend.add(new ImageView(new Image(c.getString("map.mesopatterns.guarded_treasure"), 40, 40, false, false)), 0, 5);
		Label guardTreasure = new Label("Guarded treasure");
		guardTreasure.setStyle("-fx-text-fill: white;");
		legend.add(guardTreasure, 1, 5);

		legend.add(new ImageView(new Image(c.getString("map.mesopatterns.treasure_room"), 40, 40, false, false)), 0, 6);
		Label treasureChamber = new Label("Treasure Chamber");
		treasureChamber.setStyle("-fx-text-fill: white;");
		legend.add(treasureChamber, 1, 6);

		legend.add(new ImageView(new Image(c.getString("map.examples.chamber"), 40, 40, true, true)), 0, 7);
		Label chamber = new Label("Chamber");
		chamber.setStyle("-fx-text-fill: white;");
		legend.add(chamber, 1, 7);

		legend.add(new ImageView(new Image(c.getString("map.examples.corridor"), 40, 40, true, true)), 0, 8);
		Label corridor = new Label("Corridor");
		corridor.setStyle("-fx-text-fill: white;");
		legend.add(corridor, 1, 8);

		legend.add(new ImageView(new Image(c.getString("map.examples.connector"), 40, 40, true, true)), 0, 9);
		Label connector = new Label("Connector");
		connector.setStyle("-fx-text-fill: white;");
		legend.add(connector, 1, 9);

		legend.add(new ImageView(new Image(c.getString("map.examples.dead_end"), 40, 40, true, true)), 0, 10);
		Label deadEnd = new Label("Dead end");
		deadEnd.setStyle("-fx-text-fill: white;");
		legend.add(deadEnd, 1, 10);
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
					if (nextMap == 4) {
						router.postEvent(new Stop());	
						router.postEvent(new SuggestedMapsDone());
					}
				}

				Platform.runLater(() -> {
					int[][] matrix = map.toMatrix();
					canvas.draw(renderer.renderMap(matrix));
				});
			}
		} else if (e instanceof ApplySuggestion ) {
			requestedSuggestion = (int) ((ApplySuggestion) e).getPayload();

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
		redrawPatterns(map);
		mapIsFeasible(map.isFeasibleTwo());
		//		resetMiniMaps();
	}

	public void updateRoom(Map map) {
		getMapView().updateMap(map);

		redrawPatterns(map);
		mapIsFeasible(map.isFeasibleTwo());
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
		if (getPatternButton().isSelected()) {
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
		router.postEvent(new SuggestedMapsLoading());
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
		if (selectedMiniMap != null) {
			generateNewMaps(selectedMiniMap);
			updateMap(selectedMiniMap);
		}
		generateNewMaps();
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

	public static double round(double value, int places) {
		if (places < 0) {
			throw new IllegalArgumentException();
		} else if (value == 0) {
			return 0;
		}

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
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

	@FXML
	public void clearStats() {
		enemyNumbr.setText("");
		enemyNumbr2.setText("");
		treasureNmbr.setText("");
		treasureNmbr2.setText("");
		treasurePercent.setText("");
		treasurePercent2.setText("");
		enemyPercent.setText("");
		enemyPercent2.setText("");
		entranceSafety.setText("");
		entranceSafety2.setText("");
		treasureSafety.setText("");
		treasureSafety2.setText("");	

	}

	@FXML
	public void displayStats() {
		StringBuilder str = new StringBuilder();
		str.append("Number of enemies: ");

		str.append(getMapView().getMap().getEnemyCount());
		str.append(" ➤  ");
		enemyNumbr.setText(str.toString());	
		str = new StringBuilder();

		str.append(selectedMiniMap.getEnemyCount());
		if (getMapView().getMap().getEnemyCount() > selectedMiniMap.getEnemyCount()) {
			str.append(" ▼");
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: red");
		} else if (getMapView().getMap().getEnemyCount() < selectedMiniMap.getEnemyCount()) {			
			str.append(" ▲");
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: green");
		} else {
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Number of treasures: ");

		str.append(getMapView().getMap().getTreasureCount());
		str.append(" ➤  ");
		treasureNmbr.setText(str.toString());	
		str = new StringBuilder();

		str.append(selectedMiniMap.getTreasureCount());
		if (getMapView().getMap().getTreasureCount() > selectedMiniMap.getTreasureCount()) {
			str.append(" ▼");
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: red");
		} else if (getMapView().getMap().getTreasureCount() < selectedMiniMap.getTreasureCount()) {			
			str.append(" ▲");
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: green");
		} else {
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Treasure percentage: ");

		str.append(round(getMapView().getMap().getTreasurePercentage()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		treasurePercent.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(selectedMiniMap.getTreasurePercentage()* 100, 2 ));
		str.append("%");

		if (getMapView().getMap().getTreasurePercentage() > selectedMiniMap.getTreasurePercentage()) {
			str.append(" ▼");
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: red");
		} else if (getMapView().getMap().getTreasurePercentage() < selectedMiniMap.getTreasurePercentage()) {			
			str.append(" ▲");
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: green");
		} else {
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Enemy percentage: ");

		str.append(round(getMapView().getMap().getEnemyPercentage()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		enemyPercent.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(selectedMiniMap.getEnemyPercentage()* 100, 2 ));
		str.append("%");

		if (getMapView().getMap().getEnemyPercentage() > selectedMiniMap.getEnemyPercentage()) {
			str.append(" ▼");
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: red");
		} else if (getMapView().getMap().getEnemyPercentage() < selectedMiniMap.getEnemyPercentage()) {			
			str.append(" ▲");
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: green");
		} else {
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Entrance safety: ");

		str.append(round(getMapView().getMap().getEntranceSafety()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		entranceSafety.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(selectedMiniMap.getEntranceSafety()* 100, 2 ));
		str.append("%");

		if (getMapView().getMap().getEntranceSafety() > selectedMiniMap.getEntranceSafety()) {
			str.append(" ▼");
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: red");
		} else if (getMapView().getMap().getEntranceSafety() < selectedMiniMap.getEntranceSafety()) {			
			str.append(" ▲");
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: green");
		} else {
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Treasure safety: ");

		Double[] safeties = getMapView().getMap().getAllTreasureSafeties();

		double totalSafety = 0;

		for (double d : safeties) {
			totalSafety += d;
		}

		if (safeties.length != 0) {
			totalSafety = totalSafety/safeties.length;
		}
		safeties = selectedMiniMap.getAllTreasureSafeties();

		double totalSafety2 = 0;

		for (double d : safeties) {
			totalSafety2 += d;
		}
		totalSafety2 = totalSafety2/safeties.length;

		str.append(round(totalSafety * 100, 2));
		str.append("%");

		str.append(" ➤  ");
		treasureSafety.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(totalSafety2 * 100, 2));
		str.append("%");
		if (totalSafety > totalSafety2) {
			str.append(" ▼");
			treasureSafety2.setText(str.toString());
			treasureSafety2.setStyle("-fx-text-fill: red");
		} else if (totalSafety < totalSafety2) {			
			str.append(" ▲");
			treasureSafety2.setText(str.toString());
			treasureSafety2.setStyle("-fx-text-fill: green");
		} else {
			treasureSafety2.setText(str.toString());
			treasureSafety2.setStyle("-fx-text-fill: white");
		}

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
				mapIsFeasible(getMapView().getMap().isFeasibleTwo());
				redrawPatterns(getMapView().getMap());
			}
		}

	}


	@FXML
	private void handleButtonAction(ActionEvent event) throws IOException {


		router.postEvent(new RequestWorldView());	



	}


	@FXML
	private void updateMiniMap(ActionEvent event) throws IOException {

		router.postEvent(new UpdateMiniMap());

	}

	@FXML
	private void selectSuggestion(ActionEvent event) throws IOException {

		replaceMap(requestedSuggestion);
		router.postEvent(new RequestAppliedMap(selectedMiniMap, prevRow, prevCol));
		getMap(0).setStyle("-fx-background-color:#2c2f33");
		getMap(1).setStyle("-fx-background-color:#2c2f33");
		getMap(2).setStyle("-fx-background-color:#2c2f33");
		getMap(3).setStyle("-fx-background-color:#2c2f33");

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
	public void setUpButton(Button btn) {
		upButton = btn;
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


	public Button getUpdateMiniMapBtn() {
		return updateMiniMapBtn;
	}

	public void setUpdateMiniMapBtn(Button updateMiniMapBtn) {
		this.updateMiniMapBtn = updateMiniMapBtn;
	}

	public Button getWorldGridBtn() {
		return worldGridBtn;
	}

	public void setWorldGridBtn(Button worldGridBtn) {
		this.worldGridBtn = worldGridBtn;
	}

	public Button getGenSuggestionsBtn() {
		return genSuggestionsBtn;
	}

	public void setGenSuggestionsBtn(Button genSuggestionsBtn) {
		this.genSuggestionsBtn = genSuggestionsBtn;
	}

	public Button getAppSuggestionsBtn() {
		return appSuggestionsBtn;
	}

	public void setAppSuggestionsBtn(Button appSuggestionsBtn) {
		this.appSuggestionsBtn = appSuggestionsBtn;
	}

	public ToggleButton getPatternButton() {
		return patternButton;
	}

	public void setPatternButton(ToggleButton patternButton) {
		this.patternButton = patternButton;
	}

	public class MouseEventHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent event) {

			if (minimapBoolean) {

				Node source = (Node)event.getSource();
				Integer colIndex = GridPane.getColumnIndex(source);
				Integer rowIndex = GridPane.getRowIndex(source);

				source.setOnMouseClicked(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {

						int row = rowIndex;
						int col = colIndex;
						router.postEvent(new RequestRoomView(null, row, col, null));

					}

				});
			}

		}

	}
	public void setMinimapBoolean(boolean bool) {
		minimapBoolean = bool;
	}


}
