package gui.views;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import finder.patterns.Pattern;
import finder.patterns.micro.Connector;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Chamber;
import game.ApplicationConfig;
import game.Room;
import game.MapContainer;
import game.TileTypes;
import generator.algorithm.Algorithm.AlgorithmTypes;
import game.Game.MapMutationType;
import gui.controls.Drawer;
import gui.controls.InteractiveMap;
import gui.controls.LabeledCanvas;
import gui.controls.Modifier;
import gui.controls.SuggestionRoom;
import gui.utils.MapRenderer;
import gui.views.RoomViewController.EditViewEventHandler;
import gui.views.RoomViewController.EditViewMouseHover;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.paint.Color;
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
import util.eventrouting.events.SuggestedMapSelected;
import util.eventrouting.events.SuggestedMapsDone;
import util.eventrouting.events.SuggestedMapsLoading;
import util.eventrouting.events.UpdateMiniMap;
import game.DungeonPane;

/**
 * This class controls the interactive application's edit view.
 * 
 * @author Johan Holmberg, Malmö University
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 */
public class RoomViewController extends BorderPane implements Listener 
{

	@FXML public StackPane mapPane;
	@FXML public Pane minimap;

	//left side as well
	@FXML private GridPane legend;
	@FXML private ToggleGroup brushes;
	
	//Suggestions
	@FXML private ScrollPane suggestionsPane;
	@FXML private HBox suggestionsBox;
	private ArrayList<SuggestionRoom> roomDisplays;
	
	//All the buttons to the left
	@FXML private ToggleButton patternButton;
	@FXML private ToggleButton lockBrush;
	@FXML private ToggleButton lockButton;
	@FXML private ToggleButton zoneButton;
	@FXML private ToggleButton floorBtn;
	@FXML private ToggleButton wallBtn;
	@FXML private ToggleButton treasureBtn;
	@FXML private ToggleButton enemyBtn;
	
	//Brush Slider
	@FXML private Slider zoneSlider;
	
	//Abusive amount of labels for info
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

	@FXML private Button updateMiniMapBtn; //no
	@FXML private Button worldGridBtn; //ok
	@FXML private Button genSuggestionsBtn; //bra
	@FXML private Button appSuggestionsBtn; //bra
	
	@FXML private CheckBox symmetryChoicebox; //ok, why not
	@FXML private CheckBox similarChoicebox; //ok, why not

	private boolean symmetry = false; //Probably can use the checkbox
	private boolean similarity = false; //Probably can use the checkbox

	private SuggestionRoom selectedSuggestion;

	//Literally the only thing that should be here
	private InteractiveMap mapView;
	private Room largeMap;
	private Canvas patternCanvas;
	private Canvas warningCanvas;
	private Canvas zoneCanvas;
	private Canvas lockCanvas;
	private Canvas brushCanvas;

	private MapContainer map;

	private boolean isActive = false; //for having the same event listener in different views 
	private boolean isFeasible = true; //How feasible the individual is
	public HashMap<Integer, Room> suggestedRooms = new HashMap<Integer, Room>();
	private int nextRoom = 0;

	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	private final static Logger logger = LoggerFactory.getLogger(RoomViewController.class);
	private ApplicationConfig config;

	private int prevRow;
	private int prevCol;

	private int requestedSuggestion;

	private int RequestCounter = 0;
	public Drawer myBrush;
	
	int mapWidth;
	int mapHeight;
	private int suggestionAmount = 10; //TODO: Probably this value should be from the application config!!

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
		router.registerListener(this, new SuggestedMapsDone());
		router.registerListener(this, new SuggestedMapSelected(null));

		myBrush = new Drawer();
		myBrush.AddmodifierComponent("Lock", new Modifier(lockBrush));

		zoneSlider.valueProperty().addListener((obs, oldval, newVal) -> { 
			redrawPatterns(mapView.getMap());
			});
		
		
		init();
		
//		suggestionsPane.setHbarPolicy(ScrollBarPolicy.AS_NEEDED);
//		suggestionsPane.setVbarPolicy(ScrollBarPolicy.NEVER);
//		suggestionsPane.setPrefSize(120, 120);
		suggestionsPane.setPrefWidth(50);
		roomDisplays = new ArrayList<SuggestionRoom>();
		
		//testing the HBOX --> This set all of the suggestions you want :D 
		//It should be based on the application config file!
		for(int i = 0; i < suggestionAmount; i++) //TODO: This "10" should be changed based on how many suggestions we want 
		{
			SuggestionRoom suggestion = new SuggestionRoom();
			roomDisplays.add(suggestion);
			suggestionsBox.getChildren().add(suggestion.getRoomCanvas());
		}
	}

	/**
	 * Initialises the edit view.
	 */
	private void init() {
		mapWidth = 420;
		mapHeight = 420;
		initMapView();
		initLegend();

	}
	
	//TODO: THAT 42 has to disappear!! 
	public void initializeView(Room roomToBe)
	{
//		width = 420/roomToBe.getColCount();
//		height = 420/roomToBe.getRowCount();
		mapHeight = (int)(42.0 * (float)((float)roomToBe.getRowCount())); //Recalculate map size
		mapWidth = (int)(42.0 * (float)((float)roomToBe.getColCount()));//Recalculate map size

		resetSuggestedRooms(); //reset the canvas of the suggestions
		getAppSuggestionsBtn().setDisable(true); //Disable the apply suggested room
		
		
		for(SuggestionRoom sr : roomDisplays)
		{
			sr.resizeCanvasForRoom(roomToBe);
		}
		
		initMapView();
		initLegend();
		updateMap(roomToBe);	
	}

	/**
	 * Initialises the map view and creates canvases for pattern drawing and
	 * infeasibility notifications.
	 */
	private void initMapView() {
		
		mapPane.getChildren().clear();

		setMapView(new InteractiveMap());
		StackPane.setAlignment(getMapView(), Pos.CENTER);
		getMapView().setMinSize(mapWidth, mapHeight);
		getMapView().setMaxSize(mapWidth, mapHeight);
		getMapView().setPrefSize(mapWidth, mapHeight);
		mapPane.getChildren().add(getMapView());
		
		brushCanvas = new Canvas(mapWidth, mapHeight);
		StackPane.setAlignment(brushCanvas, Pos.CENTER);
		mapPane.getChildren().add(brushCanvas);
		brushCanvas.setVisible(false);
		brushCanvas.setMouseTransparent(true);
		brushCanvas.setOpacity(1.0f);
		
		lockCanvas = new Canvas(mapWidth, mapHeight);
		StackPane.setAlignment(lockCanvas, Pos.CENTER);
		mapPane.getChildren().add(lockCanvas);
		lockCanvas.setVisible(false);
		lockCanvas.setMouseTransparent(true);
		lockCanvas.setOpacity(0.4f);
		
		zoneCanvas = new Canvas(mapWidth, mapHeight);
		StackPane.setAlignment(zoneCanvas, Pos.CENTER);
		mapPane.getChildren().add(zoneCanvas);
		zoneCanvas.setVisible(false);
		zoneCanvas.setMouseTransparent(true);


		patternCanvas = new Canvas(mapWidth, mapHeight);
		StackPane.setAlignment(patternCanvas, Pos.CENTER);
		mapPane.getChildren().add(patternCanvas);
		patternCanvas.setVisible(false);
		patternCanvas.setMouseTransparent(true);

		floorBtn.setMinWidth(75);
		wallBtn.setMinWidth(75);
		enemyBtn.setMinWidth(75);
		treasureBtn.setMinWidth(75);


		getWorldGridBtn().setTooltip(new Tooltip("View your world map"));
		getGenSuggestionsBtn().setTooltip(new Tooltip("Generate new maps according to the current map view"));
		getAppSuggestionsBtn().setTooltip(new Tooltip("Change the current map view with your selected generated map"));
		getUpdateMiniMapBtn().setTooltip(new Tooltip("Refresh your minimap view"));

		getPatternButton().setTooltip(new Tooltip("Toggle the game design patterns for the current map"));

		warningCanvas = new Canvas(mapWidth, mapHeight);
		StackPane.setAlignment(warningCanvas, Pos.CENTER);
		mapPane.getChildren().add(warningCanvas);
		warningCanvas.setVisible(false);
		warningCanvas.setMouseTransparent(true);

		GraphicsContext gc = warningCanvas.getGraphicsContext2D();
		gc.setStroke(Color.rgb(255, 0, 0, 1.0));
		gc.setLineWidth(3);
		gc.strokeRect(1, 1, mapWidth - 1, mapHeight - 1);
		gc.setLineWidth(1);
		gc.setStroke(Color.rgb(255, 0, 0, 0.9));
		gc.strokeRect(3, 3, mapWidth - 6, mapHeight - 6);
		gc.setStroke(Color.rgb(255, 0, 0, 0.8));
		gc.strokeRect(4, 4, mapWidth - 8, mapHeight - 8);
		gc.setStroke(Color.rgb(255, 0, 0, 0.7));
		gc.strokeRect(5, 5, mapWidth - 10, mapHeight - 10);
		gc.setStroke(Color.rgb(255, 0, 0, 0.6));
		gc.strokeRect(6, 6, mapWidth - 12, mapHeight - 12);
		gc.setStroke(Color.rgb(255, 0, 0, 0.5));
		gc.strokeRect(7, 7, mapWidth - 14, mapHeight - 14);
		gc.setStroke(Color.rgb(255, 0, 0, 0.4));
		gc.strokeRect(8, 8, mapWidth - 16, mapHeight - 16);
		gc.setStroke(Color.rgb(255, 0, 0, 0.3));
		gc.strokeRect(9, 9, mapWidth - 18, mapHeight - 18);
		gc.setStroke(Color.rgb(255, 0, 0, 0.2));
		gc.strokeRect(10, 10, mapWidth - 20, mapHeight - 20);
		gc.setStroke(Color.rgb(255, 0, 0, 0.1));
		gc.strokeRect(11, 11, mapWidth - 22, mapHeight - 22);
		
		
		symmetryChoicebox.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				symmetry = !symmetry;
			}
		}
			
		);
		
		similarChoicebox.selectedProperty().addListener(new ChangeListener<Boolean>() {

			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				similarity = !similarity;
			}
		}
			
		);
//		
//		mapView.addEventFilter(MouseEvent.MOUSE_CLICKED, new EditViewEventHandler());
//		mapView.addEventFilter(MouseEvent.MOUSE_MOVED, new EditViewMouseHover());

	}

	public void setContainer(MapContainer map) {
		map = this.map;
	}

	/**
	 * Initialises the legend view.
	 */
	private void initLegend() {
		ConfigurationUtility c = config.getInternalConfig();

		legend.setVgap(5);
		legend.setHgap(10);
		legend.setPadding(new Insets(5, 10, 5, 10));

		Label title = new Label("Pattern legend");
		title.setStyle("-fx-font-weight: bold");
		title.setStyle("-fx-text-fill: white;");
		legend.add(title, 0, 0, 2, 1);

		legend.add(new ImageView(new Image(c.getString("map.tiles.doorenter"), 20, 20, false, false)), 0, 1);
		Label entrance = new Label("Entrance door");
		entrance.setStyle("-fx-text-fill: white;");
		legend.add(entrance, 1, 1);

		legend.add(new ImageView(new Image(c.getString("map.tiles.door"), 20, 20, false, false)), 0, 2);
		Label door = new Label("Door");
		door.setStyle("-fx-text-fill: white;");
		legend.add(door, 1, 2);

		legend.add(new ImageView(new Image(c.getString("map.mesopatterns.ambush"), 20, 20, false, false)), 0, 3);
		Label ambush = new Label("Ambush");
		ambush.setStyle("-fx-text-fill: white;");
		legend.add(ambush, 1, 3);

		legend.add(new ImageView(new Image(c.getString("map.mesopatterns.guard_room"), 20, 20, false, false)), 0, 4);
		Label guardChamber = new Label("Guard chamber");
		guardChamber.setStyle("-fx-text-fill: white;");
		legend.add(guardChamber, 1, 4);

		legend.add(new ImageView(new Image(c.getString("map.mesopatterns.guarded_treasure"), 20, 20, false, false)), 0, 5);
		Label guardTreasure = new Label("Guarded treasure");
		guardTreasure.setStyle("-fx-text-fill: white;");
		legend.add(guardTreasure, 1, 5);

		legend.add(new ImageView(new Image(c.getString("map.mesopatterns.treasure_room"), 20, 20, false, false)), 0, 6);
		Label treasureChamber = new Label("Treasure Chamber");
		treasureChamber.setStyle("-fx-text-fill: white;");
		legend.add(treasureChamber, 1, 6);

		legend.add(new ImageView(new Image(c.getString("map.examples.chamber"), 20, 20, true, true)), 0, 7);
		Label chamber = new Label("Chamber");
		chamber.setStyle("-fx-text-fill: white;");
		legend.add(chamber, 1, 7);

		legend.add(new ImageView(new Image(c.getString("map.examples.corridor"), 20, 20, true, true)), 0, 8);
		Label corridor = new Label("Corridor");
		corridor.setStyle("-fx-text-fill: white;");
		legend.add(corridor, 1, 8);

		legend.add(new ImageView(new Image(c.getString("map.examples.connector"), 20, 20, true, true)), 0, 9);
		Label connector = new Label("Connector");
		connector.setStyle("-fx-text-fill: white;");
		legend.add(connector, 1, 9);

		legend.add(new ImageView(new Image(c.getString("map.examples.dead_end"), 20, 20, true, true)), 0, 10);
		Label deadEnd = new Label("Dead end");
		deadEnd.setStyle("-fx-text-fill: white;");
		legend.add(deadEnd, 1, 10);
	}


	@Override
	public void ping(PCGEvent e) {
		if (e instanceof MapUpdate) {

			if (isActive) {
				//System.out.println(nextMap);
				Room room = (Room) ((MapUpdate) e).getPayload();
				UUID uuid = ((MapUpdate) e).getID();
				LabeledCanvas canvas;
				synchronized (roomDisplays) {

					roomDisplays.get(nextRoom).setSuggestedRoom(room);
					roomDisplays.get(nextRoom).setOriginalRoom(getMapView().getMap()); //Maybe this does not make sense? Idk
					
					canvas = roomDisplays.get(nextRoom).getRoomCanvas();
					canvas.setText("");
					
					suggestedRooms.put(nextRoom, room);
					nextRoom++;
				}

				Platform.runLater(() -> {
					canvas.draw(renderer.renderMiniSuggestedRoom(room));
//					System.out.println("CANVAS WIDTH: " + canvas.getWidth() + ", CANVAS HEIGHT: " + canvas.getHeight());
				});
			}
		} 
		else if (e instanceof ApplySuggestion ) 
		{
			requestedSuggestion = (int) ((ApplySuggestion) e).getPayload();
		}
		else if(e instanceof SuggestedMapsDone) //All the evolutionary algorithms have finish their run and returned the best rooms!
		{
			getUpdateMiniMapBtn().setDisable(false);
			getWorldGridBtn().setDisable(false);
			getGenSuggestionsBtn().setDisable(false);	
		}
		else if(e instanceof SuggestedMapSelected)
		{
			if(selectedSuggestion != null)
				selectedSuggestion.setSelected(false);
			
			selectedSuggestion = (SuggestionRoom) ((SuggestedMapSelected) e).getPayload();
			clearStats();
			displayStats();
			getAppSuggestionsBtn().setDisable(false);
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
		return roomDisplays.get(index).getRoomCanvas();
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
	 * @param room The new map.
	 */
	public void updateMap(Room room) {
		getMapView().updateMap(room);
		redrawPatterns(room);
		redrawLocks(room);
		mapIsFeasible(room.isFeasible()); //TODO: Lets try with normal feasible
	}

	public void updateRoom(Room room) {
		getMapView().updateMap(room);

		redrawPatterns(room);
		mapIsFeasible(room.isFeasible()); //TODO: Lets try with normal feasible
	}

	/**
	 * Gets the current map being controlled by this controller.
	 * 
	 * @return The current map.
	 */
	public Room getCurrentMap() {

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
			mapView.setCursor(Cursor.DEFAULT);
			myBrush.SetMainComponent(null);
			
		} else {
			mapView.setCursor(Cursor.HAND);
			switch (((ToggleButton) brushes.getSelectedToggle()).getText()) {
			case "Floor":
				myBrush.SetMainComponent(TileTypes.FLOOR);
				break;
			case "Wall":
				myBrush.SetMainComponent(TileTypes.WALL);
				break;
			case "Treasure":
				myBrush.SetMainComponent(TileTypes.TREASURE);
				break;
			case "Enemy":
				myBrush.SetMainComponent(TileTypes.ENEMY);
				break;
			}
		}
		
	}

	
	/**
	 * Toggles the main use of the lock modifier in the brush
	 */
	public void selectLockModifier()
	{
		myBrush.ChangeModifierMainValue("Lock", lockBrush.isSelected());
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
	 * Toggles the display of zones on top of the map.
	 * 
	 */
	public void toggleLocks() {
		if (lockButton.isSelected()) {
			lockCanvas.setVisible(true);
		} else {
			lockCanvas.setVisible(false);
		}
	}


	/**
	 * Generates as many suggested rooms as specified
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	@FXML
	private void generateNewMaps()
	{	
		router.postEvent(new SuggestedMapsLoading());
		resetSuggestedRooms();
		prepareViewForSuggestions();
		generateNewMaps(getMapView().getMap());
	}

	/***
	 * Send event that is captured in the InteractiveGUIController and returns to the world view
	 * @param event
	 * @throws IOException
	 */
	@FXML
	private void backWorldView(ActionEvent event) throws IOException 
	{
		router.postEvent(new RequestWorldView());	
	}

	/***
	 * TODO: This method is very close to be extincted!! :D 
	 * @param event
	 * @throws IOException
	 */
	@FXML
	private void updateMiniMap(ActionEvent event) throws IOException 
	{
		router.postEvent(new UpdateMiniMap());
	}

	/***
	 * Applies the selected suggestion!
	 * @param event
	 * @throws IOException
	 */
	@FXML
	private void selectSuggestion(ActionEvent event) throws IOException {
		
		replaceRoom();
//		replaceMap(requestedSuggestion);
//		router.postEvent(new RequestAppliedMap(selectedSuggestion.getSuggestedRoom(), prevRow, prevCol));
//		getMap(0).setStyle("-fx-background-color:#2c2f33");
//		getMap(1).setStyle("-fx-background-color:#2c2f33");
//		getMap(2).setStyle("-fx-background-color:#2c2f33");
//		getMap(3).setStyle("-fx-background-color:#2c2f33");

	}
	
	private void prepareViewForSuggestions()
	{
//		getMap(0).setStyle("-fx-background-color:#2c2f33;");
//		getMap(1).setStyle("-fx-background-color:#2c2f33;");
//		getMap(2).setStyle("-fx-background-color:#2c2f33;");
//		getMap(3).setStyle("-fx-background-color:#2c2f33;");
		clearStats();
		getUpdateMiniMapBtn().setDisable(true);
		getWorldGridBtn().setDisable(true);
		getGenSuggestionsBtn().setDisable(true);
		getAppSuggestionsBtn().setDisable(true);
	}
	
	/**
	 * Resets the mini suggestions for a new run of map generation
	 */
	private void resetSuggestedRooms() 
	{
		nextRoom = 0;
		
		if(selectedSuggestion != null)
			selectedSuggestion.setSelected(false);
		
		selectedSuggestion = null;
		clearStats();
		
		for(SuggestionRoom sr : roomDisplays)
		{
			sr.getRoomCanvas().draw(null);
			sr.getRoomCanvas().setText("Waiting for map...");
		}
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
	public void generateNewMaps(Room room) {
		// TODO: If we want more diversity in the generated maps, then send more StartMapMutate events.

		router.postEvent(new StartMapMutate(room, MapMutationType.Preserving, AlgorithmTypes.Native, suggestionAmount, true));
		
//		if (!similarity && !symmetry ) {
//		router.postEvent(new StartMapMutate(room, MapMutationType.Preserving, AlgorithmTypes.Native, 2, true)); //TODO: Move some of this hard coding to ApplicationConfig
//		router.postEvent(new StartMapMutate(room, MapMutationType.ComputedConfig, AlgorithmTypes.Native, 2, true)); //TODO: Move some of this hard coding to ApplicationConfig
//		}
//		else if (similarity && !symmetry) {
//			router.postEvent(new StartMapMutate(room, MapMutationType.Preserving, AlgorithmTypes.Similarity, 4, true));
//		}
//		else if (!similarity && symmetry) {
//			router.postEvent(new StartMapMutate(room, MapMutationType.Preserving, AlgorithmTypes.Symmetry, 2, true));
//			router.postEvent(new StartMapMutate(room, MapMutationType.ComputedConfig, AlgorithmTypes.Symmetry, 2, true));
//		}
//		else if (similarity && symmetry) {
//			router.postEvent(new StartMapMutate(room, MapMutationType.Preserving, AlgorithmTypes.Similarity, 2, true));
//			router.postEvent(new StartMapMutate(room, MapMutationType.ComputedConfig, AlgorithmTypes.Symmetry, 2, true));
//		}
	}

	public void replaceRoom()
	{
		//pass the info from one room to the other one
		
		if(selectedSuggestion != null)
		{
			mapView.getMap().applySuggestion(selectedSuggestion.getSuggestedRoom());
			updateMap(mapView.getMap());
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
			if (pattern instanceof Chamber) {
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
	private synchronized void redrawPatterns(Room room) {
		//Change those 2 width and height hardcoded values (420,420)
		//And change zone to its own method
		patternCanvas.getGraphicsContext2D().clearRect(0, 0, mapWidth, mapHeight);
		zoneCanvas.getGraphicsContext2D().clearRect(0, 0, mapWidth, mapHeight);

		renderer.drawPatterns(patternCanvas.getGraphicsContext2D(), room.toMatrix(), colourPatterns(room.getPatternFinder().findMicroPatterns()));
		renderer.drawGraph(patternCanvas.getGraphicsContext2D(), room.toMatrix(), room.getPatternFinder().getPatternGraph());
		renderer.drawMesoPatterns(patternCanvas.getGraphicsContext2D(), room.toMatrix(), room.getPatternFinder().getMesoPatterns());
		renderer.drawZones(zoneCanvas.getGraphicsContext2D(), room.toMatrix(), room.root, (int)(zoneSlider.getValue()),Color.BLACK);
	}

	/***
	 * Redraw the lock in the map --> TODO: I am afraid this should be in the renderer
	 * @param room
	 */
	private void redrawLocks(Room room)
	{
		lockCanvas.getGraphicsContext2D().clearRect(0, 0, mapWidth, mapHeight);
		
		for(int i = 0; i < room.getRowCount(); ++i)
		{
			for(int j = 0; j < room.getColCount(); ++j)
			{
				if(room.getTile(j, i).GetImmutable())
				{
					lockCanvas.getGraphicsContext2D().drawImage(renderer.GetLock(mapView.scale * 0.75f, mapView.scale * 0.75f), j * mapView.scale, i * mapView.scale);
				}
			}
		}
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
	public void displayStats() 
	{
		Room original = getMapView().getMap();
		Room toCompare = selectedSuggestion.getSuggestedRoom();
		
		StringBuilder str = new StringBuilder();
		str.append("Number of enemies: ");

		str.append(getMapView().getMap().getEnemyCount());
		str.append(" ➤  ");
		enemyNumbr.setText(str.toString());	
		str = new StringBuilder();

		str.append(toCompare.getEnemyCount());
		if (getMapView().getMap().getEnemyCount() > toCompare.getEnemyCount()) {
			str.append(" ▼");
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: red");
		} else if (getMapView().getMap().getEnemyCount() < toCompare.getEnemyCount()) {			
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

		str.append(toCompare.getTreasureCount());
		if (getMapView().getMap().getTreasureCount() > toCompare.getTreasureCount()) {
			str.append(" ▼");
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: red");
		} else if (getMapView().getMap().getTreasureCount() < toCompare.getTreasureCount()) {			
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

		str.append(round(toCompare.getTreasurePercentage()* 100, 2 ));
		str.append("%");

		if (getMapView().getMap().getTreasurePercentage() > toCompare.getTreasurePercentage()) {
			str.append(" ▼");
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: red");
		} else if (getMapView().getMap().getTreasurePercentage() < toCompare.getTreasurePercentage()) {			
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

		str.append(round(toCompare.getEnemyPercentage()* 100, 2 ));
		str.append("%");

		if (getMapView().getMap().getEnemyPercentage() > toCompare.getEnemyPercentage()) {
			str.append(" ▼");
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: red");
		} else if (getMapView().getMap().getEnemyPercentage() < toCompare.getEnemyPercentage()) {			
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

		str.append(round(toCompare.getEntranceSafety()* 100, 2 ));
		str.append("%");

		if (getMapView().getMap().getEntranceSafety() > toCompare.getEntranceSafety()) {
			str.append(" ▼");
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: red");
		} else if (getMapView().getMap().getEntranceSafety() < toCompare.getEntranceSafety()) {			
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
		safeties = toCompare.getAllTreasureSafeties();

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
	
	//TODO: things are going to change probs
	public void roomMouseEvents() 
	{
		getMapView().addEventFilter(MouseEvent.MOUSE_CLICKED, new EditViewEventHandler());
		getMapView().addEventFilter(MouseEvent.MOUSE_MOVED, new EditViewMouseHover());
	}

	/*
	 * Event handlers
	 */
	public class EditViewEventHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) 
		{
			
			if (event.getTarget() instanceof ImageView) {
				// Edit the map
				ImageView tile = (ImageView) event.getTarget();
				
				//TODO: This should go to its own class or function at least
//				if(event.isControlDown())
//					lockBrush.setSelected(true);
//				else if()
				myBrush.UpdateModifiers(event);
//				mapView.updateTile(tile, brush, event.getButton() == MouseButton.SECONDARY, lockBrush.isSelected() || event.isControlDown());
				mapView.updateTile(tile, myBrush);
				mapView.getMap().forceReevaluation();
				mapIsFeasible(mapView.getMap().isFeasible()); //TODO: Lets try with normal feasible
				redrawPatterns(mapView.getMap());
				redrawLocks(mapView.getMap());
//				redrawHeatMap(mapView.getMap());
			}
		}
		
	}
	
	/*
	 * Event handlers
	 */
	public class EditViewMouseHover implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) 
		{
			brushCanvas.setVisible(false);
			
			if (event.getTarget() instanceof ImageView) 
			{
				// Show the brush canvas
				ImageView tile = (ImageView) event.getTarget();
				myBrush.SetBrushSize((int)(zoneSlider.getValue()));
				brushCanvas.getGraphicsContext2D().clearRect(0, 0, mapWidth, mapHeight);
				brushCanvas.setVisible(true);
				util.Point p = mapView.CheckTile(tile);
				myBrush.Update(event, p, mapView.getMap());
				
				renderer.drawBrush(brushCanvas.getGraphicsContext2D(), mapView.getMap().toMatrix(), myBrush, Color.WHITE);
			}
		}
		
	}
	
	public Room getSelectedMiniMap() {
		return selectedSuggestion.getSuggestedRoom();
	}

	public void setSelectedMiniMap(Room selectedMiniMap) {
		this.selectedSuggestion.setSuggestedRoom(selectedMiniMap);
	}

	public InteractiveMap getMapView() {
		return mapView;
	}

	public void setMapView(InteractiveMap mapView) {
		this.mapView = mapView;
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
}
