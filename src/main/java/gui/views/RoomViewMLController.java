package gui.views;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import finder.patterns.Pattern;
import finder.patterns.micro.Connector;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Chamber;
import game.ApplicationConfig;
import game.Room;
import game.Tile;
import game.MapContainer;
import game.TileTypes;
import generator.algorithm.Algorithm.AlgorithmTypes;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.MAPElites.GACell;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import game.Game.MapMutationType;
import game.Game.PossibleGAs;
import game.tiles.BossEnemyTile;
import game.tiles.EnemyTile;
import game.tiles.FloorTile;
import game.tiles.TreasureTile;
import game.tiles.WallTile;
import gui.controls.DimensionsTable;
import gui.controls.Drawer;
import gui.controls.InteractiveMap;
import gui.controls.LabeledCanvas;
import gui.controls.MAPEVisualizationPane;
import gui.controls.Modifier;
import gui.controls.SuggestionRoom;
import gui.utils.InformativePopupManager;
import gui.utils.MapRenderer;
import gui.utils.InformativePopupManager.PresentableInformation;
import gui.views.RoomViewController.EditViewEventHandler;
import gui.views.RoomViewController.EditViewMouseHover;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.Slider;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Callback;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import machineLearning.NNPreferenceModel;
import machineLearning.neuralnetwork.MapPreferenceModelTuple;
import util.Point;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.ApplySuggestion;
import util.eventrouting.events.MAPEGridUpdate;
import util.eventrouting.events.MAPElitesBroadcastCells;
import util.eventrouting.events.MAPElitesDone;
import util.eventrouting.events.MapElitesDoneAllRooms;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestAppliedMap;
import util.eventrouting.events.RequestRoomView;
import util.eventrouting.events.RequestWorldView;
import util.eventrouting.events.SaveDisplayedCells;
import util.eventrouting.events.StartGA_MAPE;
import util.eventrouting.events.StartMapMutate;
import util.eventrouting.events.Stop;
import util.eventrouting.events.SuggestedMapSelected;
import util.eventrouting.events.SuggestedMapsDone;
import util.eventrouting.events.SuggestedMapsLoading;
import util.eventrouting.events.TrainNetwork;
import util.eventrouting.events.UpdateMiniMap;
import util.eventrouting.events.UpdatePreferenceModel;
import game.DungeonPane;

/**
 * This class controls the interactive application's edit view.
 * FIXME: A lot of things need to change here! 
 * 
 * @author Johan Holmberg, Malmö University
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 */
public class RoomViewMLController extends BorderPane implements Listener 
{

	@FXML private ComboBox<String> DisplayCombo;
	@FXML private List<LabeledCanvas> mlRoomDisplays;
	private ArrayList<SuggestionRoom> mlRooms;
	
	@FXML public StackPane mapPane;
	@FXML public Pane minimap;

	//left side as well
	@FXML private GridPane legend;
	@FXML private ToggleGroup brushes;
	
	
	@FXML private DimensionsTable MainTable;
	@FXML private DimensionsTable secondaryTable;
	
	//Suggestions
//	@FXML private GridPane suggestionsPane;
	@FXML private MAPEVisualizationPane MAPElitesPane;
	private ArrayList<SuggestionRoom> roomDisplays;
	private ArrayList<ArrayList<Room>> currentCellsRooms;
	
	//All the buttons to the left
	@FXML private ToggleButton patternButton;
	@FXML private ToggleButton lockBrush;
	@FXML private ToggleButton lockButton;
	@FXML private ToggleButton floorBtn;
	@FXML private ToggleButton wallBtn;
	@FXML private ToggleButton treasureBtn;
	@FXML private ToggleButton enemyBtn;
	
	//Brush Slider
	@FXML private Slider brushSlider;
	
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

	@FXML private Button worldGridBtn; //ok
	@FXML private Button genSuggestionsBtn; //bra
	@FXML private Button appSuggestionsBtn; //bra
	@FXML private Button flowControlBtn; //bra
	@FXML private Button stopEABtn; //bra
	@FXML private Button saveGenBtn;

	private SuggestionRoom selectedSuggestion;

	//Literally the only thing that should be here
	private InteractiveMap mapView;
	private Room largeMap;
	private Canvas patternCanvas;
	private Canvas warningCanvas;
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
	private int suggestionAmount = 100; //TODO: Probably this value should be from the application config!!

	private PossibleGAs selectedGA;
	private MAPEDimensionFXML[] currentDimensions = new MAPEDimensionFXML[] {};
	
	//FOR THE NEURAL NETWORK!
	protected ArrayList<MapPreferenceModelTuple> networkTuples = new ArrayList<MapPreferenceModelTuple>();
	public NNPreferenceModel userPreferenceModel;
	
	SortedMap<Double, ArrayList<Integer>> preferenceIndices = new TreeMap<Double, ArrayList<Integer>>(Collections.reverseOrder());
	
	public static int CURRENTSTEP = 0;
	
	//PROVISIONAL FIX!
	public enum EvoState
	{
		STOPPED,
		RUNNING
	}
	
	public EvoState currentState;
	
	/**
	 * Creates an instance of this class.
	 */
	@SuppressWarnings("unchecked")
	public RoomViewMLController() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/ML/RoomViewML.fxml"));
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

		router.registerListener(this, new MAPElitesBroadcastCells());
		router.registerListener(this, new MAPEGridUpdate(null));
		router.registerListener(this, new MAPElitesDone());
		router.registerListener(this, new MapElitesDoneAllRooms());
		router.registerListener(this, new MapUpdate(null));
		router.registerListener(this, new ApplySuggestion(0));
		router.registerListener(this, new SuggestedMapsDone());
		router.registerListener(this, new SuggestedMapSelected(null));

		myBrush = new Drawer();
		myBrush.AddmodifierComponent("Lock", new Modifier(lockBrush));

		brushSlider.valueProperty().addListener((obs, oldval, newVal) -> { 
			redrawPatterns(mapView.getMap());
			});
		
		
		init();
		
		roomDisplays = new ArrayList<SuggestionRoom>();
		
		for(int i = 0; i < suggestionAmount; i++) 
		{
			SuggestionRoom suggestion = new SuggestionRoom();
			roomDisplays.add(suggestion);
		}

		mlRooms = new ArrayList<SuggestionRoom>();
		
		for(int i = 0; i < 3; i++) 
		{
			SuggestionRoom suggestion = new SuggestionRoom(mlRoomDisplays.get(i));
			mlRooms.add(suggestion);
		}
		
			
//		suggestionsPane.setVisible(false);
		MAPElitesPane.init(roomDisplays, "","",0,0);
		
		
		MainTable.setup(2);
		MainTable.InitMainTable(MAPElitesPane);
		MainTable.setEventListeners();
		
		secondaryTable.setup(DimensionTypes.values().length);
		
		for(DimensionTypes dimension : DimensionTypes.values())
        {
        	if(dimension != DimensionTypes.SIMILARITY && dimension != DimensionTypes.SYMMETRY
        			&& dimension != DimensionTypes.DIFFICULTY
        			&& dimension != DimensionTypes.GEOM_COMPLEXITY && dimension != DimensionTypes.REWARD)
        	{
        		secondaryTable.getItems().add(new MAPEDimensionFXML(dimension, 5));
        	}
            
        }
		
		secondaryTable.setEventListeners();
		
		currentState = EvoState.STOPPED;
		selectedGA = PossibleGAs.MAP_ELITES;
		saveGenBtn.setDisable(false);
		
		userPreferenceModel = new NNPreferenceModel(true); //To create a network without starting data
//		userPreferenceModel = new NNPreferenceModel(); //To create a network with starting data
	}
	
	@FXML
	private void OnChangeTab()
	{
		//TODO: Debugging here!
		if(getMapView() != null && getMapView().getMap() != null)
		{
			int paths = getMapView().getMap().LinearityWithinRoom();
			double doors =  getMapView().getMap().getDoors().size();
			double maxPaths = ((double) getMapView().getMap().getPatternFinder().getPatternGraph().countNodes()) + (double)(doors * 3) + doors;
			double finalValue = Math.min((double)paths/maxPaths, 1.0);
			finalValue = (1.0 - finalValue);			
			
			System.out.println(paths);
			System.out.println(finalValue);
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
	
	private void ProduceVerticalLabel()
	{
//		Label bl2 = new Label("SIMILARITY");
//		bl2.setTextFill(Color.WHITE);
//		bl2.setFont(Font.font("Monospaced", 30));
//		bl2.setWrapText(true);
//		bl2.setMinWidth(5);
//		bl2.setPrefWidth(5);
//		bl2.setMaxWidth(5);
//		bl2.setAlignment(Pos.CENTER);
//		StackPane p = new StackPane();
//		p.setPrefWidth(50);
//		p.getChildren().add(bl2);
//		StackPane.setAlignment(bl2, Pos.CENTER);
//		bl2.setStyle("-fx-font-weight: bold");
//		sugs.setLeft(p);
//		BorderPane.setAlignment(p, Pos.CENTER);
	}
	
	//TODO: THAT 42 has to disappear!! 
	public void initializeView(Room roomToBe)
	{
//		width = 420/roomToBe.getColCount();
//		height = 420/roomToBe.getRowCount();
		mapHeight = (int)(42.0 * (float)((float)roomToBe.getRowCount())); //Recalculate map size
		mapWidth = (int)(42.0 * (float)((float)roomToBe.getColCount()));//Recalculate map size

		for(SuggestionRoom sr : roomDisplays)
		{
			sr.resizeCanvasForRoom(roomToBe);
		}
		
		for(SuggestionRoom sr : mlRooms)
		{
			sr.resizeCanvasForRoom(roomToBe);
		}
		
		preferenceIndices.clear();
		
		resetMiniMaps();
		initMapView();
		initLegend();
		resetView();
		roomToBe.forceReevaluation();
		updateMap(roomToBe);	
		generateNewMaps();
//		
//		OnChangeTab();
//		generateNewMaps();
	}
	
	/**
	 * Resets the mini maps for a new run of map generation.
	 */
	private void resetMiniMaps() {
		int nextMap = 0;
		
		getMLMap(0).draw(null);
		getMLMap(0).setText("Waiting for map...");

		getMLMap(1).draw(null);
		getMLMap(1).setText("Waiting for map...");

		getMLMap(2).draw(null);
		getMLMap(2).setText("Waiting for map...");

//		getMap(3).draw(null);
//		getMap(3).setText("Waiting for map...");
	}
	
	/**
	 * Gets one of the maps (i.e. a labeled view displaying a map) being under
	 * this object's control.
	 * 
	 * @param index An index of a map.
	 * @return A map if it exists, otherwise null.
	 */
	public LabeledCanvas getMLMap(int index) {
		return mlRoomDisplays.get(index);
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
		
//		
//		mapView.addEventFilter(MouseEvent.MOUSE_CLICKED, new EditViewEventHandler());
//		mapView.addEventFilter(MouseEvent.MOUSE_MOVED, new EditViewMouseHover());

	}
	
	public void resetView()
	{
		//Reset all selectables
		brushes.selectToggle(null);
		getPatternButton().setSelected(false);
		lockBrush.setSelected(false);
		
		selectBrush();
		togglePatterns();
		selectLockModifier();		
		
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
		legend.setHgap(11);
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
		
		legend.add(new ImageView(new Image(c.getString("map.examples.lock"), 20, 20, true, true)), 0, 11);
		Label lock = new Label("Lock tile");
		lock.setStyle("-fx-text-fill: white;");
		legend.add(lock, 1, 11);
	}
	
	public void renderCell(List<Room> generatedRooms, int dimension, float [] dimensionSizes, int[] indices)
	{
		if(dimension < 0)
		{
//			MAPElitesPane.GetGridCell(, row);
//			this.cells.add(new GACell(MAPElitesDimensions, indices));
			
			roomDisplays.get((int) (indices[1] * dimensionSizes[0] + indices[0])).setSuggestedRoom(generatedRooms.get((int) (indices[1] * dimensionSizes[0] + indices[0])));
			roomDisplays.get((int) (indices[1] * dimensionSizes[0] + indices[0])).setOriginalRoom(getMapView().getMap());
//			roomDisplays.get(nextRoom).setOriginalRoom(); //Maybe this does not make sense? Idk
			return;
		}
		
		for(int i = 0; i < dimensionSizes[dimension]; i++)
		{
			indices[dimension] = i;
			renderCell(generatedRooms, dimension-1, dimensionSizes, indices);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void ping(PCGEvent e) {
		
		if(e instanceof MAPElitesBroadcastCells)
		{
			currentCellsRooms = new ArrayList<ArrayList<Room>>((ArrayList<ArrayList<Room>>) e.getPayload());
			
			System.out.println(currentCellsRooms.size());
			int finalCount = 0;
			
			for(ArrayList<Room> rooms : currentCellsRooms)
			{
				if(rooms != null)
				{
					finalCount += rooms.size();
				}
			}
			
			System.out.println(finalCount);
		}
		else if(e instanceof MapElitesDoneAllRooms)
		{
			if (isActive) {
			List<Room> populationRooms = ((MapElitesDoneAllRooms) e).GetRooms();
			calculateFromAll(populationRooms);
			}
		}
		else if(e instanceof MAPEGridUpdate)
		{
//			suggestionCanvasOnUse = new SuggestionRoom[0];
			MAPElitesPane.dimensionsUpdated(roomDisplays, ((MAPEGridUpdate) e).getDimensions());
			currentDimensions = ((MAPEGridUpdate) e).getDimensions(); 
			OnChangeTab();
		}
		else if(e instanceof MAPElitesDone)
		{
			if (isActive) {
				//THIS NEED TO BE IMPROVED!
				List<Room> generatedRooms = ((MAPElitesDone) e).GetRooms();
//				Room room = (Room) ((MapUpdate) e).getPayload();
//				UUID uuid = ((MapUpdate) e).getID();
				LabeledCanvas canvas;
				synchronized (roomDisplays) {
					
					renderCell(generatedRooms, currentDimensions.length - 1, 
							new float [] {currentDimensions[0].getGranularity(), 
									currentDimensions[1].getGranularity()}, 
							new int[] {0,0});
				}
				
				Platform.runLater(() -> {
					int i = 0;
					for(SuggestionRoom sugRoom : roomDisplays)
					{
						if(sugRoom.getSuggestedRoom() != null)
						{
							sugRoom.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(sugRoom.getSuggestedRoom(), i));
						}
						else
						{
							sugRoom.getRoomCanvas().draw(null);
						}
						i++;
					}

//					System.out.println("CANVAS WIDTH: " + canvas.getWidth() + ", CANVAS HEIGHT: " + canvas.getHeight());
				});
				
			}
		}
		else if (e instanceof MapUpdate) {
			//FIXME: I REALLY HAVE TO GO BACK HERE TO FIX THIS TO BE ABLE TO CREATEROOMS THE OLD WAY
			if (isActive) {
				//System.out.println(nextMap);
				Room room = (Room) ((MapUpdate) e).getPayload();
				UUID uuid = ((MapUpdate) e).getID();
				LabeledCanvas canvas;
				nextRoom = 0;
				synchronized (roomDisplays) {
//					suggestionsBox.getChildren().add(suggestion.getRoomCanvas());
					
//					SuggestionRoom current = (SuggestionRoom)suggestionsBox.getChildren().get(nextRoom);
					
					roomDisplays.get(nextRoom).setSuggestedRoom(room);
					roomDisplays.get(nextRoom).setOriginalRoom(getMapView().getMap()); //Maybe this does not make sense? Idk
					
					canvas = roomDisplays.get(nextRoom).getRoomCanvas();
					canvas.setText("");
					
					suggestedRooms.put(nextRoom, room);
					nextRoom++;
				}

				Platform.runLater(() -> {
					canvas.draw(renderer.renderMiniSuggestedRoom(room, nextRoom - 1));
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
			getWorldGridBtn().setDisable(false);
			getGenSuggestionsBtn().setDisable(false);	
		}
		else if(e instanceof SuggestedMapSelected)
		{
			clearStats();
			
			if(selectedSuggestion != null)
				selectedSuggestion.setSelected(false);
			
			selectedSuggestion = (SuggestionRoom) ((SuggestedMapSelected) e).getPayload();
			
			if(selectedSuggestion.getSuggestedRoom() == null )
			{
				selectedSuggestion.setSelected(false);
				return;
			}
			
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
		mapIsFeasible(room.isIntraFeasible());
	}

	public void updateRoom(Room room) {
		getMapView().updateMap(room);

		redrawPatterns(room);
		mapIsFeasible(room.isIntraFeasible());
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
		
//		OnChangeTab();
		
		
		if (brushes.getSelectedToggle() == null) {
			mapView.setCursor(Cursor.DEFAULT);
			myBrush.SetMainComponent(new Tile());
			
		} else {
			mapView.setCursor(Cursor.HAND);
			switch (((ToggleButton) brushes.getSelectedToggle()).getText()) {
			case "Floor":
				myBrush.SetMainComponent(new FloorTile());
				break;
			case "Wall":
				myBrush.SetMainComponent(new WallTile());
				break;
			case "Treasure":
				myBrush.SetMainComponent(new TreasureTile());
				break;
			case "Enemy":
				myBrush.SetMainComponent(new EnemyTile());
				break;		
			case "BOSS":
				myBrush.SetMainComponent(new BossEnemyTile());
				break;
			}
		
			ActionLogger.getInstance().storeAction(ActionType.CHANGE_VALUE, 
													View.ROOM, 
													TargetPane.TILE_PANE,
													false,
													myBrush.GetMainComponent()); //tile type
		
		}
		
		
		
	}

	
	/**
	 * Toggles the main use of the lock modifier in the brush
	 */
	public void selectLockModifier()
	{
		myBrush.ChangeModifierMainValue("Lock", lockBrush.isSelected());
		lockButton.setSelected(lockBrush.isSelected());
		toggleLocks();
		lockButton.setDisable(lockBrush.isSelected());
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
	private void saveCurrentGeneration() //TODO: some changes here!
	{	
		userPreferenceModel.broadcastPreferences();
		
//		switch(currentState)
//		{
//		case STOPPED:
//			//Nothing happens here :D 
//			break;
//		case RUNNING:
//			
//			router.postEvent(new SaveDisplayedCells());
////			MAPElitesPane.SaveDimensionalGrid();
////			MapRenderer.getInstance().saveCurrentEditedRoom(getMapView());
//			
//			break;
//		}

	}
	
	private void generateNewMaps()
	{
		resetSuggestedRooms();
		generateNewMaps(getMapView().getMap());
	}

	/**
	 * Generates as many suggested rooms as specified
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	@FXML
	private void restartEA() 
	{	
		selectedSuggestion = null;
		//FIXME: CHECK THIS
//		storeSuggestions(100);
		generateNewMaps();
		
//		switch(currentState)
//		{
//		case STOPPED:
//			router.postEvent(new SuggestedMapsLoading());
//			resetSuggestedRooms();
////			prepareViewForSuggestions();
//			generateNewMaps(getMapView().getMap());
//			
//			clearStats();
//			getWorldGridBtn().setDisable(true);
////			getGenSuggestionsBtn().setText("Stop Suggestions");
//			getAppSuggestionsBtn().setDisable(true);
//			saveGenBtn.setDisable(false);
//			currentState = EvoState.RUNNING;
//			break;
//		case RUNNING:
//			
//			router.postEvent(new Stop());
//			
//			clearStats();
//			getWorldGridBtn().setDisable(false);
////			getGenSuggestionsBtn().setText("Generate Suggestions");
//			getAppSuggestionsBtn().setDisable(false);
//			saveGenBtn.setDisable(true);
//			currentState = EvoState.STOPPED;
//			
//			break;
//		}
//		
	}

	/***
	 * Send event that is captured in the InteractiveGUIController and returns to the world view
	 * @param event
	 * @throws IOException
	 */
	@FXML
	private void backWorldView(ActionEvent event) throws IOException 
	{
		router.postEvent(new Stop());
		router.postEvent(new RequestWorldView());	
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
	
	
	/***
	 * Applies the selected suggestion!
	 * @param event
	 * @throws IOException
	 */
	@FXML
	private void stopEA(ActionEvent event) throws IOException 
	{
	}
	
	/***
	 * Applies the selected suggestion!
	 * @param event
	 * @throws IOException
	 */
	@FXML
	private void controlEAFlow(ActionEvent event) throws IOException 
	{
	}
	
	
	private void prepareViewForSuggestions()
	{
//		getMap(0).setStyle("-fx-background-color:#2c2f33;");
//		getMap(1).setStyle("-fx-background-color:#2c2f33;");
//		getMap(2).setStyle("-fx-background-color:#2c2f33;");
//		getMap(3).setStyle("-fx-background-color:#2c2f33;");
		clearStats();
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
		router.postEvent(new Stop());
		
		if(selectedSuggestion != null)
			selectedSuggestion.setSelected(false);
		
		selectedSuggestion = null;
		clearStats();
		
		for(SuggestionRoom sr : roomDisplays)
		{
//			sr.getRoomCanvas().resizeRotatingThingie();
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
		
		switch(selectedGA)
		{
		case FI_2POP:
			int firstAmount = suggestionAmount/2;
			int secondAmount = suggestionAmount - firstAmount;
			
			router.postEvent(new StartMapMutate(room, MapMutationType.Preserving, AlgorithmTypes.Native, firstAmount, true)); //TODO: Move some of this hard coding to ApplicationConfig
			router.postEvent(new StartMapMutate(room, MapMutationType.ComputedConfig, AlgorithmTypes.Native, secondAmount, true)); //TODO: Move some of this hard coding to ApplicationConfig

			break;
		case MAP_ELITES:
			if(currentDimensions.length > 1)
			{
				router.postEvent(new StartGA_MAPE(room, currentDimensions));
			}
			
			break;
		case CVT_MAP_ELITES:
			break;
		default:
			break;
		
		}
	}

	public void replaceRoom()
	{
		//pass the info from one room to the other one

		if(selectedSuggestion != null)
		{
			mapView.getMap().applySuggestion(selectedSuggestion.getSuggestedRoom());
			updateMap(mapView.getMap());
//			router.postEvent(new Stop());
//			storeSuggestions(5);
			storeSuggestionsContinouos2();
			CURRENTSTEP++;
		}
	}
	
	private void storeSuggestionsContinouos2()
	{
		//Move around the matrix
		ArrayList<Integer> indices = new ArrayList<Integer>();

		
		if(selectedSuggestion == null)
		{
			System.out.println("No selected suggestion... what are you doing?");
			return;
		}
		
		int xIndex = selectedSuggestion.getRendererIndices(0);
		int yIndex = selectedSuggestion.getRendererIndices(1);

		//Add the current
		int curIndex = (xIndex) + currentDimensions[0].getGranularity() * (yIndex);
//		double pref = Math.abs(i - cur)
		
		if(roomDisplays.get(curIndex).getSuggestedRoom() != null)
		{
			for(Room room : currentCellsRooms.get(curIndex))
			{
				userPreferenceModel.updateContinuousModel(1.0, room, CURRENTSTEP);
			}
			
//			userPreferenceModel.updateContinuousModel(1.0, roomDisplays.get(curIndex).getSuggestedRoom(), CURRENTSTEP);
			indices.add(curIndex);
		}
		
		String[] colors = {"purple","green", "blue", "yellow", "red", "cyan"};
		String color = "";
		
		for(double xStep = 0; xStep < currentDimensions[0].getGranularity(); xStep+= 1.0)
		{
			for(double yStep = 0; yStep < currentDimensions[1].getGranularity(); yStep+= 1.0)
			{
				double pValue = 1.0 - ((0.2 * xStep) + (0.2 * yStep));
				pValue = Math.round(pValue * 10.0) / 10.0;
				
				if(xStep == 2 && yStep == 1)
					System.out.println("HERE!");
				
				color = colors[(int) Math.min(5, (xStep + yStep))];
				
				if(xIndex + xStep < currentDimensions[0].getGranularity())
				{
					int i = (int)((xIndex + xStep) + currentDimensions[0].getGranularity() * (yIndex));
//					double pref = Math.abs(i - cur)

					
					if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
					{
						roomDisplays.get(i).testColor(color);
						indices.add(i);
						for(Room room : currentCellsRooms.get(i))
						{
							userPreferenceModel.updateContinuousModel(pValue, room, CURRENTSTEP);
						}
					}

				}

				if(xIndex -xStep > -1)
				{
					int i = (int)(xIndex - xStep) + currentDimensions[0].getGranularity() * (yIndex);
//					double pref = Math.abs(i - cur)

					if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
					{
						indices.add(i);
						roomDisplays.get(i).testColor(color);
						for(Room room : currentCellsRooms.get(i))
						{
							userPreferenceModel.updateContinuousModel(pValue, room, CURRENTSTEP);
						}
					}
				}
				
				if(yIndex-yStep > -1)
				{
					int i = (int)((xIndex) + currentDimensions[0].getGranularity() * (yIndex -yStep));
//					double pref = Math.abs(i - cur)

					if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
					{
						roomDisplays.get(i).testColor(color);
						indices.add(i);
						for(Room room : currentCellsRooms.get(i))
						{
							userPreferenceModel.updateContinuousModel(pValue, room, CURRENTSTEP);
						}
					}
					
					if(xIndex + xStep < currentDimensions[0].getGranularity())
					{
						i =(int)((xIndex + xStep) + currentDimensions[0].getGranularity() * (yIndex -yStep));
//						double pref = Math.abs(i - cur)

						if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
						{
							roomDisplays.get(i).testColor(color);
							indices.add(i);
							for(Room room : currentCellsRooms.get(i))
							{
								userPreferenceModel.updateContinuousModel(pValue, room, CURRENTSTEP);
							}
						}

					}

					if(xIndex -xStep > -1)
					{
						i = (int)((xIndex - xStep) + currentDimensions[0].getGranularity() * (yIndex -yStep));
//						double pref = Math.abs(i - cur)
						
						if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
						{
							roomDisplays.get(i).testColor(color);
							indices.add(i);
							for(Room room : currentCellsRooms.get(i))
							{
								userPreferenceModel.updateContinuousModel(pValue, room, CURRENTSTEP);
							}
						}
					}

				}
				
				if(yIndex+yStep < currentDimensions[1].getGranularity())
				{			
					int i = (int)((xIndex) + currentDimensions[0].getGranularity() * (yIndex + yStep));
//					double pref = Math.abs(i - cur)
					if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
					{
						roomDisplays.get(i).testColor(color);
						indices.add(i);
						for(Room room : currentCellsRooms.get(i))
						{
							userPreferenceModel.updateContinuousModel(pValue, room, CURRENTSTEP);
						}
					}
					
					if(xIndex + xStep < currentDimensions[0].getGranularity())
					{
						i = (int)((xIndex + xStep) + currentDimensions[0].getGranularity() * (yIndex + yStep));
//						double pref = Math.abs(i - cur)

						if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
						{
							roomDisplays.get(i).testColor(color);
							indices.add(i);
							for(Room room : currentCellsRooms.get(i))
							{
								userPreferenceModel.updateContinuousModel(pValue, room, CURRENTSTEP);
							}
						}
					}
					
					if(xIndex - xStep > -1)
					{
						i = (int)((xIndex - xStep) + currentDimensions[0].getGranularity() * (yIndex + yStep));
//						double pref = Math.abs(i - cur)

						if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
						{
							roomDisplays.get(i).testColor(color);
							indices.add(i);
							for(Room room : currentCellsRooms.get(i))
							{
								userPreferenceModel.updateContinuousModel(pValue, room, CURRENTSTEP);
							}
						}
					}
				}
			}
		}

		router.postEvent(new TrainNetwork());
	
	}
	
	private void storeSuggestionsContinouos()
	{
		//Move around the matrix
		ArrayList<Integer> indices = new ArrayList<Integer>();

		
		if(selectedSuggestion == null)
		{
			System.out.println("No selected suggestion... what are you doing?");
			return;
		}
		
		int xIndex = selectedSuggestion.getRendererIndices(0);
		int yIndex = selectedSuggestion.getRendererIndices(1);

		//Add the current
		int curIndex = (xIndex) + currentDimensions[0].getGranularity() * (yIndex);
//		double pref = Math.abs(i - cur)
		
		if(roomDisplays.get(curIndex).getSuggestedRoom() != null)
		{
			userPreferenceModel.updateContinuousModel(1.0, roomDisplays.get(curIndex).getSuggestedRoom(), CURRENTSTEP);
			indices.add(curIndex);
		}
		
		String[] colors = {"purple","green", "blue", "yellow", "red", "cyan"};
		String color = "";
		
		for(double xStep = 0; xStep < currentDimensions[0].getGranularity(); xStep+= 1.0)
		{
			for(double yStep = 0; yStep < currentDimensions[1].getGranularity(); yStep+= 1.0)
			{
				double pValue = 1.0 - ((0.2 * xStep) + (0.2 * yStep));
				pValue = Math.round(pValue * 10.0) / 10.0;
				
				if(xStep == 2 && yStep == 1)
					System.out.println("HERE!");
				
				color = colors[(int) Math.min(5, (xStep + yStep))];
				
				if(xIndex + xStep < currentDimensions[0].getGranularity())
				{
					int i = (int)((xIndex + xStep) + currentDimensions[0].getGranularity() * (yIndex));
//					double pref = Math.abs(i - cur)

					
					if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
					{
						roomDisplays.get(i).testColor(color);
						indices.add(i);
						userPreferenceModel.updateContinuousModel(pValue, roomDisplays.get(i).getSuggestedRoom(), CURRENTSTEP);
					}

				}

				if(xIndex -xStep > -1)
				{
					int i = (int)(xIndex - xStep) + currentDimensions[0].getGranularity() * (yIndex);
//					double pref = Math.abs(i - cur)

					if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
					{
						indices.add(i);
						roomDisplays.get(i).testColor(color);
						userPreferenceModel.updateContinuousModel(pValue, roomDisplays.get(i).getSuggestedRoom(), CURRENTSTEP);
					}
				}
				
				if(yIndex-yStep > -1)
				{
					int i = (int)((xIndex) + currentDimensions[0].getGranularity() * (yIndex -yStep));
//					double pref = Math.abs(i - cur)

					if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
					{
						roomDisplays.get(i).testColor(color);
						indices.add(i);
						userPreferenceModel.updateContinuousModel(pValue, roomDisplays.get(i).getSuggestedRoom(), CURRENTSTEP);
					}
					
					if(xIndex + xStep < currentDimensions[0].getGranularity())
					{
						i =(int)((xIndex + xStep) + currentDimensions[0].getGranularity() * (yIndex -yStep));
//						double pref = Math.abs(i - cur)

						if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
						{
							roomDisplays.get(i).testColor(color);
							indices.add(i);
							userPreferenceModel.updateContinuousModel(pValue, roomDisplays.get(i).getSuggestedRoom(), CURRENTSTEP);
						}

					}

					if(xIndex -xStep > -1)
					{
						i = (int)((xIndex - xStep) + currentDimensions[0].getGranularity() * (yIndex -yStep));
//						double pref = Math.abs(i - cur)
						
						if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
						{
							roomDisplays.get(i).testColor(color);
							indices.add(i);
							userPreferenceModel.updateContinuousModel(pValue, roomDisplays.get(i).getSuggestedRoom(), CURRENTSTEP);
						}
					}

				}
				
				if(yIndex+yStep < currentDimensions[1].getGranularity())
				{			
					int i = (int)((xIndex) + currentDimensions[0].getGranularity() * (yIndex + yStep));
//					double pref = Math.abs(i - cur)
					if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
					{
						roomDisplays.get(i).testColor(color);
						indices.add(i);
						userPreferenceModel.updateContinuousModel(pValue, roomDisplays.get(i).getSuggestedRoom(), CURRENTSTEP);
					}
					
					if(xIndex + xStep < currentDimensions[0].getGranularity())
					{
						i = (int)((xIndex + xStep) + currentDimensions[0].getGranularity() * (yIndex + yStep));
//						double pref = Math.abs(i - cur)

						if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
						{
							roomDisplays.get(i).testColor(color);
							indices.add(i);
							userPreferenceModel.updateContinuousModel(pValue, roomDisplays.get(i).getSuggestedRoom(), CURRENTSTEP);
						}
					}
					
					if(xIndex - xStep > -1)
					{
						i = (int)((xIndex - xStep) + currentDimensions[0].getGranularity() * (yIndex + yStep));
//						double pref = Math.abs(i - cur)

						if(!indices.contains(i) && roomDisplays.get(i).getSuggestedRoom() != null)
						{
							roomDisplays.get(i).testColor(color);
							indices.add(i);
							userPreferenceModel.updateContinuousModel(pValue, roomDisplays.get(i).getSuggestedRoom(), CURRENTSTEP);
						}
					}
				}
			}
		}

		router.postEvent(new TrainNetwork());
	
	}
	
	//TODO: READ THIS METHOD
	private void storeSuggestions(int maxNegative)
	{
		System.out.println("TRAINING AFTER CHOOSING!");
		System.out.println("READ METHOD: storeSuggestion  from Roomviewmlcontroller");
		int currentNeg = maxNegative;
		
		int selSug = -1;
		
		if(selectedSuggestion != null)
		{
			//Get the index of the selected suggestion
			for(int i = 0; i < roomDisplays.size(); i++)
			{
				if(roomDisplays.get(i).equals(selectedSuggestion))
				{
					selSug = i;
					break;
				}
			}
		}
		
		ArrayList<SuggestionRoom> copyRooms = new ArrayList<SuggestionRoom>(roomDisplays);
		int positiveNeighbors = updateModelWithNeighborhood(selSug, copyRooms);
		
		
		for(SuggestionRoom sr : copyRooms)
		{
			if(sr.getSuggestedRoom() != null)
			{
				if(currentNeg > 0)
				{
					userPreferenceModel.UpdateModel(false, sr.getSuggestedRoom(), CURRENTSTEP);
					currentNeg--;
				}
//					networkTuples.add(new MapPreferenceModelTuple(sr.getSuggestedRoom(), false));
			}
		}
//		
//		for(SuggestionRoom sr : roomDisplays)
//		{
//			if(sr.getSuggestedRoom() != null)
//			{
//				if(sr.equals(selectedSuggestion))
//				{
//					userPreferenceModel.UpdateModel(true, sr.getSuggestedRoom());
////					networkTuples.add(new MapPreferenceModelTuple(sr.getSuggestedRoom(), true));
//				}
//				else //Here we could add the other suggestions!
//				{
//					if(maxNegative > 0)
//					{
//						userPreferenceModel.UpdateModel(false, sr.getSuggestedRoom());
//						maxNegative--;
//					}
//
////					networkTuples.add(new MapPreferenceModelTuple(sr.getSuggestedRoom(), false));
//				}
//			}
//		}
		
		//We have new data! now we train!
		System.out.println("TRAIN!");
//		userPreferenceModel.trainNetwork(CURRENTSTEP);
		router.postEvent(new TrainNetwork());

	}
	
	private int updateModelWithNeighborhood(int index, ArrayList<SuggestionRoom> copyRoom)
	{
		ArrayList<Integer> indices = new ArrayList<Integer>();
		ArrayList<SuggestionRoom> toAdd = new ArrayList<SuggestionRoom>();
		
		if(index == -1) //no selected suggestion!
		{
			return -1;
		}
		
		
		indices.add(index);
		int xIndex= (index % currentDimensions[0].getGranularity());
		int yIndex =  (index / currentDimensions[1].getGranularity());
		
		System.out.println(xIndex);
		System.out.println(yIndex);
		
		if(xIndex + 1 < currentDimensions[0].getGranularity())
		{
			indices.add((xIndex + 1) + currentDimensions[0].getGranularity() * (yIndex));
		}

		if(xIndex -1 > -1)
		{
			indices.add((xIndex - 1) + currentDimensions[0].getGranularity() * (yIndex));
		}
		
		if(yIndex-1 > -1)
		{
			indices.add((xIndex) + currentDimensions[0].getGranularity() * (yIndex -1));
			
			if(xIndex + 1 < currentDimensions[0].getGranularity())
			{
				indices.add((xIndex + 1) + currentDimensions[0].getGranularity() * (yIndex -1));
			}

			if(xIndex -1 > -1)
			{
				indices.add((xIndex - 1) + currentDimensions[0].getGranularity() * (yIndex -1));
			}

		}
		
		if(yIndex+1 < currentDimensions[1].getGranularity())
		{
			indices.add((xIndex) + currentDimensions[0].getGranularity() * (yIndex + 1));
			
			if(xIndex + 1 < 5)
				indices.add((xIndex + 1) + currentDimensions[0].getGranularity() * (yIndex +1));
			
			if(xIndex -1 > -1)
				indices.add((xIndex - 1) + currentDimensions[0].getGranularity() * (yIndex +1));
		}
		
		for(int ind : indices)
		{
			toAdd.add(copyRoom.get(ind));
//			userPreferenceModel.UpdateModel(true, .getSuggestedRoom());
		}
		
		copyRoom.removeAll(toAdd);
		
		for(SuggestionRoom sr : toAdd)
		{
//			sr.highlight(true);
			if(sr.getSuggestedRoom() != null)
				userPreferenceModel.UpdateModel(true, sr.getSuggestedRoom(), CURRENTSTEP);
		}
		
		return toAdd.size();
	}
	
	private void calculateFromAll(List<Room> population)
	{
		preferenceIndices.clear();
		int index = 0;
		
		////////////FIRST EVALUATE ALL THE SUGGESTIONS /////////////////
		for(Room sr : population)
		{
			double value = userPreferenceModel.testWithPreference(sr);
			
			if(preferenceIndices.containsKey(value))
			{
				preferenceIndices.get(value).add(index);
			}
			else
			{
				preferenceIndices.put(value, new ArrayList<Integer>());
				preferenceIndices.get(value).add(index);
			}
			
			index++;
		}
		
		
		//////////////// THEN PICK!!! //////////////////
		int maxCounter = 0;
		boolean breakSearch = false;
		for(Map.Entry<Double, ArrayList<Integer>> entry : preferenceIndices.entrySet())
		{
			if(maxCounter >= mlRooms.size())
				break;
			
			for(Integer ind : entry.getValue())
			{
				if(maxCounter >= mlRooms.size())
				{
					breakSearch = true;
					break;
				}
	
				mlRooms.get(maxCounter).setSuggestedRoom(population.get(ind.intValue()));
				maxCounter++;
			}
			
			if(breakSearch) break;
			
			System.out.println(entry.getKey());
		}
		
		//TODO: THIS IS WHERE IS PICKING FROM THE AVAILABLE!!!
		System.out.println("CHECK THIS! Calculate from all method at roomviewmlcontroller");
		System.out.println("DONE picking");
		
		/////////////////////// THEN RENDER!!! /////////////////
		
		Platform.runLater(() -> {
			int i = 0;
			for(SuggestionRoom sugRoom : mlRooms)
			{
				if(sugRoom.getSuggestedRoom() != null)
				{
					sugRoom.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(sugRoom.getSuggestedRoom(), i));
				}
				else
				{
					sugRoom.getRoomCanvas().draw(null);
				}
				i++;
			}

//			System.out.println("CANVAS WIDTH: " + canvas.getWidth() + ", CANVAS HEIGHT: " + canvas.getHeight());
		});
		
//		selectAndRenderNNRooms();
	}
	
	private void calculateFromCurrent()
	{
		preferenceIndices.clear();
		int index = 0;
		
		////////////FIRST EVALUATE ALL THE SUGGESTIONS /////////////////
		for(SuggestionRoom sr : roomDisplays)
		{
			
			if(sr.getSuggestedRoom() != null)
			{
				double value = userPreferenceModel.testWithPreference(sr.getSuggestedRoom());
				
				if(preferenceIndices.containsKey(value))
				{
					preferenceIndices.get(value).add(index);
				}
				else
				{
					preferenceIndices.put(value, new ArrayList<Integer>());
					preferenceIndices.get(value).add(index);
				}
			}
			
			index++;
		}
		
		
		//////////////// THEN PICK!!! //////////////////
		int maxCounter = 0;
		boolean breakSearch = false;
		for(Map.Entry<Double, ArrayList<Integer>> entry : preferenceIndices.entrySet())
		{
			if(maxCounter >= mlRooms.size())
				break;
			
			for(Integer ind : entry.getValue())
			{
				if(maxCounter >= mlRooms.size())
				{
					breakSearch = true;
					break;
				}
	
				mlRooms.get(maxCounter).setSuggestedRoom(roomDisplays.get(ind.intValue()).getSuggestedRoom());
				maxCounter++;
			}
			
			if(breakSearch) break;
			
			System.out.println(entry.getKey());
		}
		
		System.out.println("DONE picking");
		
		/////////////////////// THEN RENDER!!! /////////////////
		
		Platform.runLater(() -> {
			int i = 0;
			for(SuggestionRoom sugRoom : mlRooms)
			{
				if(sugRoom.getSuggestedRoom() != null)
				{
					sugRoom.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(sugRoom.getSuggestedRoom(), i));
				}
				else
				{
					sugRoom.getRoomCanvas().draw(null);
				}
				i++;
			}

//			System.out.println("CANVAS WIDTH: " + canvas.getWidth() + ", CANVAS HEIGHT: " + canvas.getHeight());
		});
		
//		selectAndRenderNNRooms();
	}
	
	private void selectAndRenderNNRooms()
	{

	}
	
	public void broadcastPreferences()
	{
//		EventRouter.getInstance().postEvent(new UpdatePreferenceModel(this));
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

		renderer.drawPatterns(patternCanvas.getGraphicsContext2D(), room.toMatrix(), colourPatterns(room.getPatternFinder().findMicroPatterns()));
		renderer.drawGraph(patternCanvas.getGraphicsContext2D(), room.toMatrix(), room.getPatternFinder().getPatternGraph());
		renderer.drawMesoPatterns(patternCanvas.getGraphicsContext2D(), room.toMatrix(), room.getPatternFinder().getMesoPatterns());
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
		
		int originalEnemies = 0;
		int compareEnemies = 0;
		
//		getMapView().getMap().createLists();
//		toCompare.createLists();
		
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

		str.append(round(getMapView().getMap().getDoorSafeness()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		entranceSafety.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(toCompare.getDoorSafeness()* 100, 2 ));
		str.append("%");

		if (getMapView().getMap().getDoorSafeness() > toCompare.getDoorSafeness()) {
			str.append(" ▼");
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: red");
		} else if (getMapView().getMap().getDoorSafeness() < toCompare.getDoorSafeness()) {			
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
		
		if (safeties.length != 0) {
			totalSafety2 = totalSafety2/safeties.length;
		}

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
	
	public void roomMouseEvents() 
	{
		getMapView().addEventFilter(MouseEvent.MOUSE_CLICKED, new EditViewEventHandler());
		getMapView().addEventFilter(MouseEvent.MOUSE_MOVED, new EditViewMouseHover());
	}
	
	public boolean checkInfeasibleLockedRoom(ImageView tile)
	{
		Room auxRoom = new Room(mapView.getMap());
		mapView.updateTileInARoom(auxRoom, tile, myBrush);
		
		if(!auxRoom.walkableSectionsReachable())
		{
			System.out.println("I DETECTED IT!!");
			InformativePopupManager.getInstance().requestPopup(mapView, PresentableInformation.ROOM_INFEASIBLE_LOCK, "");
			return true;
		}
		
		return false;
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
				
				if(!myBrush.possibleToDraw() || (myBrush.GetModifierValue("Lock") && checkInfeasibleLockedRoom(tile)))
					return;
				
				if(myBrush.GetModifierValue("Lock"))
				{
					InformativePopupManager.getInstance().requestPopup(mapView, PresentableInformation.LOCK_RESTART, "");

				}
				
				mapView.updateTile(tile, myBrush);
				mapView.getMap().forceReevaluation();
				mapView.getMap().getRoomXML("room\\");
				mapIsFeasible(mapView.getMap().isIntraFeasible());
				redrawPatterns(mapView.getMap());
				redrawLocks(mapView.getMap());
				
				//FIXME: Added for presentation
//				mapView.getMap().calculateAllDimensionalValues();
//				System.out.println(mapView.getMap().getDimensionValue(DimensionTypes.LENIENCY));         
//				System.out.println(mapView.getMap().getDimensionValue(DimensionTypes.LINEARITY) + ";");        
////				System.out.println(room.getDimensionValue(DimensionTypes.SIMILARITY) + ";");       
//				System.out.println(mapView.getMap().getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN) + ";");
//				System.out.println(mapView.getMap().getDimensionValue(DimensionTypes.NUMBER_PATTERNS) + ";");  
//				System.out.println(mapView.getMap().getDimensionValue(DimensionTypes.SYMMETRY) + ";"); 
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
				myBrush.SetBrushSize((int)(brushSlider.getValue()));
				brushCanvas.getGraphicsContext2D().clearRect(0, 0, mapWidth, mapHeight);
				brushCanvas.setVisible(true);
				util.Point p = mapView.CheckTile(tile);
				myBrush.Update(event, p, mapView.getMap());
				
				renderer.drawBrush(brushCanvas.getGraphicsContext2D(), mapView.getMap().toMatrix(), myBrush, 
						myBrush.possibleToDraw() ? Color.WHITE : Color.RED);
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