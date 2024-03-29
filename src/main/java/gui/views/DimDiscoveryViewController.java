package gui.views;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import game.*;
import game.Game.MapMutationType;
import game.tiles.*;
import generator.algorithm.Algorithm.AlgorithmTypes;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.GADimensionsGranularity;
import generator.algorithm.MetricAlgorithm;
import generator.algorithm.MetricExampleRooms;
import generator.algorithm.MetricIndividual;
import gui.controls.*;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;
import util.eventrouting.events.intraview.*;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This class controls the interactive application's edit view.
 * FIXME: A lot of things need to change here! 
 *
 * @author Alberto Alvarez, Malmö University
 */
public class DimDiscoveryViewController extends BorderPane implements Listener
{
	@FXML private ComboBox<String> DisplayCombo;

	//This all need to be EditedRoomStackPane!
	@FXML public EditedRoomStackPane editedRoomPane;
	@FXML public EditedRoomStackPane verylow_example_pane;
	@FXML public EditedRoomStackPane low_example_pane;
	@FXML public EditedRoomStackPane mid_example_pane;
	@FXML public EditedRoomStackPane high_example_pane;
	@FXML public EditedRoomStackPane veryhigh_example_pane;

	@FXML public TextField dimension_name;

	//left side as well
	@FXML private GridPane legend;
	@FXML private ToggleGroup brushes;

	@FXML private DimensionsTable MainTable;
	@FXML private DimensionsTable secondaryTable;

	//RIGHT SIDE!
	@FXML private VBox rightSidePane;

	//Suggestions
//	@FXML private GridPane suggestionsPane;
	@FXML private MAPEVisualizationPane MAPElitesPane;
	private ArrayList<SuggestionRoom> roomDisplays;

	//All the buttons to the left
	@FXML private ToggleButton patternButton;
	@FXML private ToggleButton lockBrush;
	@FXML private ToggleButton lockButton;
	@FXML private ToggleButton floorBtn;
	@FXML private ToggleButton wallBtn;
	@FXML private ToggleButton treasureBtn;
	@FXML private ToggleButton enemyBtn;
	@FXML private ToggleButton bossEnemyBtn;

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
	@FXML private Button runDimDiscovery;

	private SuggestionRoom selectedSuggestion;

	//Literally the only thing that should be here
//	private InteractiveMap mapView;
	private Room largeMap;


	private MapContainer map;

	private boolean isActive = false; //for having the same event listener in different views
	private boolean isFeasible = true; //How feasible the individual is
	public HashMap<Integer, Room> suggestedRooms = new HashMap<Integer, Room>();
	private int nextRoom = 0;

	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
//	private final static Logger logger = LoggerFactory.getLogger(RoomViewController.class);
	private ApplicationConfig config;

	private int prevRow;
	private int prevCol;

	private int requestedSuggestion;

	private int RequestCounter = 0;
	public Drawer myBrush;

	int mapWidth;
	int mapHeight;
	private int suggestionAmount = 101; //TODO: Probably this value should be from the application config!!

	private MAPEDimensionFXML[] currentDimensions = new MAPEDimensionFXML[] {};

	@FXML
	public GridPane metricSuggestions;
	private HBox sp;
	private ArrayList<MetricPossibleRoom> metricDisplays;

	//PROVISIONAL FIX!
	public enum EvoState
	{
		STOPPED,
		RUNNING
	}

	public EvoState currentState;

	int currentEditionStep = 0;

	/**
	 * Creates an instance of this class.
	 */
	@SuppressWarnings("unchecked")
	public DimDiscoveryViewController() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/interactive/DimDiscoveryView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
			config = ApplicationConfig.getInstance();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		} catch (MissingConfigurationException e) {
//			logger.error("Couldn't read config file.");
		}

		router.registerListener(this, new MAPEGridUpdate(null));
		router.registerListener(this, new MAPElitesDone());
		router.registerListener(this, new MapUpdate(null));
		router.registerListener(this, new ApplySuggestion(0));
		router.registerListener(this, new SuggestedMapsDone());
		router.registerListener(this, new SuggestedMapSelected(null));
		router.registerListener(this, new UserEditedRoom(null, null));
		router.registerListener(this, new MetricUpdate());

//		router.registerListener(this, new InteractiveRoomHovered(null, null, null, null));
//		router.registerListener(this, new InteractiveRoomEdited(null, null, null, null));


		myBrush = new Drawer();
		myBrush.AddmodifierComponent("Lock", new Modifier(lockBrush));

//		Check here!!!
		brushSlider.valueProperty().addListener((obs, oldval, newVal) -> {
//			redrawPatterns(mapView.getMap());
			myBrush.SetBrushSize((int)(brushSlider.getValue()));
			ActionLogger.getInstance().storeAction(ActionType.CHANGE_VALUE,
													View.ROOM,
													TargetPane.BRUSH_PANE,
													false,
													oldval,
													newVal); //Point
			});

		init();
		
		roomDisplays = new ArrayList<SuggestionRoom>();
		
		for(int i = 0; i < suggestionAmount; i++) 
		{
			SuggestionRoom suggestion = new SuggestionRoom();
			roomDisplays.add(suggestion);
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
		saveGenBtn.setDisable(false);

		metricDisplays = new ArrayList<MetricPossibleRoom>();

		for(int i = 0; i < 10; i++)
		{
			MetricPossibleRoom suggestion = new MetricPossibleRoom();
			metricDisplays.add(suggestion);
		}

//		metricSuggestions = new GridPane();
//		metricSuggestions.setStyle("-fx-background-color: transparent;");
		metricSuggestions.setHgap(5.0);
		metricSuggestions.setVgap(5.0);
		metricSuggestions.setAlignment(Pos.CENTER);
		SetupInnerGrid(metricDisplays, 5, 2);
//		sp = new HBox(metricSuggestions);

	}

	public void SetupInnerGrid(ArrayList<MetricPossibleRoom> displays, int xResolution, int yResolution)
	{
		metricSuggestions.getChildren().clear();

		for(int j = 0, red = yResolution; j < yResolution; j++, red--)
		{
			Label boxLabel = new Label(String.valueOf((float)red/yResolution));
			boxLabel.setTextFill(Color.WHITE);
			GridPane.clearConstraints(boxLabel);
			GridPane.setConstraints(boxLabel,0, j);
			metricSuggestions.getChildren().add(boxLabel);

			for(int i = 1; i < xResolution + 1; i++)
			{
				GridPane.clearConstraints(displays.get((i - 1) + j * xResolution).getRoomCanvas());
				GridPane.setConstraints(displays.get((i - 1) + j * xResolution).getRoomCanvas(), i, (red - 1));
				metricSuggestions.getChildren().add(displays.get((i - 1) + j * xResolution).getRoomCanvas());
			}
		}

		for(int i = 1; i < xResolution + 1; i++)
		{
			Label boxLabel = new Label(String.valueOf((float)(i)/xResolution));
			boxLabel.setTextFill(Color.WHITE);
			GridPane.clearConstraints(boxLabel);
			GridPane.setConstraints(boxLabel, i, yResolution);
			GridPane.setHalignment(boxLabel, HPos.CENTER);
			metricSuggestions.getChildren().add(boxLabel);
		}
	}

	private void CreateTestScenario(Room room)
	{
		Room edited_room = new Room(room);
		edited_room.setTile(3, 0, TileTypes.WALL);
		edited_room.setTile(3, 1, TileTypes.WALL);
		edited_room.setTile(3, 2, TileTypes.WALL);
		edited_room.setTile(3, 3, TileTypes.WALL);
		edited_room.setTile(3, 5, TileTypes.WALL);
		edited_room.setTile(3, 6, TileTypes.WALL);
		edited_room.setTile(4, 5, TileTypes.WALL);
		edited_room.setTile(4, 2, TileTypes.WALL);
		edited_room.setTile(4, 6, TileTypes.TREASURE);
		edited_room.setTile(6, 4, TileTypes.ENEMY);
		editedRoomPane.editedPane.updateMap(edited_room);

		edited_room = new Room(room);
		edited_room.setTile(4, 5, TileTypes.WALL);
		edited_room.setTile(5, 5, TileTypes.WALL);
		edited_room.setTile(7, 5, TileTypes.WALL);
		edited_room.setTile(8, 5, TileTypes.WALL);

		edited_room.setTile(4, 4, TileTypes.WALL);
		edited_room.setTile(8, 4, TileTypes.WALL);

		edited_room.setTile(4, 3, TileTypes.WALL);
		edited_room.setTile(8, 3, TileTypes.WALL);

		edited_room.setTile(4, 2, TileTypes.WALL);
		edited_room.setTile(8, 2, TileTypes.WALL);

		edited_room.setTile(4, 1, TileTypes.WALL);
		edited_room.setTile(5, 1, TileTypes.WALL);
		edited_room.setTile(6, 1, TileTypes.WALL);
		edited_room.setTile(7, 1, TileTypes.WALL);
		edited_room.setTile(8, 1, TileTypes.WALL);

		edited_room.setTile(6, 2, TileTypes.TREASURE);

		verylow_example_pane.editedPane.updateMap(edited_room);

		edited_room = new Room(room);
		edited_room.setTile(1, 4, TileTypes.WALL);
		edited_room.setTile(2, 3, TileTypes.WALL);
		edited_room.setTile(1, 2, TileTypes.WALL);

		edited_room.setTile(11, 4, TileTypes.WALL);
		edited_room.setTile(10, 3, TileTypes.WALL);
		edited_room.setTile(11, 2, TileTypes.WALL);

		edited_room.setTile(11, 3, TileTypes.TREASURE);
		edited_room.setTile(6, 3, TileTypes.ENEMY);

		veryhigh_example_pane.editedPane.updateMap(edited_room);
	}
	
	@FXML
	private void OnChangeTab()
	{
		//TODO: Debugging here!
//		if(getMapView() != null && getMapView().getMap() != null)
//		{
//			int paths = getMapView().getMap().LinearityWithinRoom();
//			double doors =  getMapView().getMap().getDoors().size();
//			double maxPaths = ((double) getMapView().getMap().getPatternFinder().getPatternGraph().countNodes()) + (double)(doors * 3) + doors;
//			double finalValue = Math.min((double)paths/maxPaths, 1.0);
//			finalValue = (1.0 - finalValue);
//
//
//			System.out.println(paths);
//			System.out.println(finalValue);
//		}
	}
	
	/**
	 * Initialises the edit view.
	 */
	private void init() {
		mapWidth = 420;
		mapHeight = 420;
//		initMapView();
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
		mapHeight = (int)(30.0 * (float)((float)roomToBe.getRowCount())); //Recalculate map size
		mapWidth = (int)(30.0 * (float)((float)roomToBe.getColCount()));//Recalculate map size

		for(SuggestionRoom sr : roomDisplays)
		{
			sr.resizeCanvasForRoom(roomToBe);
		}

		for(MetricPossibleRoom md : metricDisplays)
		{
			md.resizeCanvasForRoom(roomToBe);
		}

		editedRoomPane.init(mapWidth, mapHeight, roomToBe);

		mapHeight = (int)(15.0 * (float)((float)roomToBe.getRowCount())); //Recalculate map size
		mapWidth = (int)(15.0 * (float)((float)roomToBe.getColCount()));//Recalculate map size

		verylow_example_pane.init(mapWidth, mapHeight, new Room(roomToBe));
		veryhigh_example_pane.init(mapWidth, mapHeight, new Room(roomToBe));



		initButtons();
		initLegend();
		resetView();
		roomToBe.forceReevaluation();
		updateRoom(roomToBe);

		//This is for testing purposes
		CreateTestScenario(roomToBe);


		generateNewMaps();


	}

	private void initRoomStackPanes(StackPane currentPane)
	{
//		if(getMapView() != null)
//		{
//			getMapView().destructor();
//		}


	}

	/**
	 * Initialises the map view and creates canvases for pattern drawing and
	 * infeasibility notifications.
	 */
	private void initButtons() {

		floorBtn.setMinWidth(75);
		wallBtn.setMinWidth(75);
		enemyBtn.setMinWidth(75);
		bossEnemyBtn.setMinWidth(75);
		treasureBtn.setMinWidth(75);

		getWorldGridBtn().setTooltip(new Tooltip("View your world map"));
		getGenSuggestionsBtn().setTooltip(new Tooltip("Generate new maps according to the current map view"));
		getAppSuggestionsBtn().setTooltip(new Tooltip("Change the current map view with your selected generated map"));

		getPatternButton().setTooltip(new Tooltip("Toggle the game design patterns for the current map"));
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
			roomDisplays.get((int) (indices[1] * dimensionSizes[0] + indices[0])).setOriginalRoom(editedRoomPane.editedPane.getMap());
//			roomDisplays.get((int) (indices[1] * dimensionSizes[0] + indices[0])).setOriginalRoom(getMapView().getMap()); //FIXME: Will need to be fixed
//			roomDisplays.get(nextRoom).setOriginalRoom(); //Maybe this does not make sense? Idk
			return;
		}
		
		for(int i = 0; i < dimensionSizes[dimension]; i++)
		{
			indices[dimension] = i;
			renderCell(generatedRooms, dimension-1, dimensionSizes, indices);
		}
	}

	private void UserEditedRoom(UUID editCanvasID, Room editedRoom)
	{
		//This could change, but we are at the moment only interested in the room that is the edited room pange
		if(editCanvasID == editedRoomPane.uniqueID)
		{
			router.postEvent(new RoomEdited(editedRoom));
		}
	}

	@Override
	public void ping(PCGEvent e) {
		//Lets see if it works!

		if(e instanceof MetricUpdate)
		{
			if (isActive) {
				System.out.println("NEW ROUND OF UPDATES!");
				List<MetricIndividual> top_candidates = ((MetricUpdate) e).top_individuals;
				int display_counter = 0;

//				LinkedHashMap<Room, Double> candidate_0_results = top_candidates.get(0).getPhenotype().results;

				List<MetricExampleRooms> candidate_0_results = top_candidates.get(0).getPhenotype().results;

				synchronized (candidate_0_results)
				{
					for(MetricExampleRooms result : candidate_0_results) {
						metricDisplays.get(display_counter).setSuggestedRoom(result.room);
						metricDisplays.get(display_counter).setOriginalRoom(result.room);
						metricDisplays.get(display_counter).setExampleRoom(result.room, result.granularity_value, result.metric_value);
						display_counter++;
					}
//
//								new GADimensionsGranularity(((display_counter%5) + 1.0)/5.0, 5.0, (display_counter%5) + 1),
//								candidate_0_results.get(result));
//						display_counter++;
//					}
//					for (Room result : candidate_0_results.keySet())
//					{
//
//					}
				}

					List<MetricExampleRooms> candidate_1_results = top_candidates.get(1).getPhenotype().results;

					synchronized (candidate_1_results) {
						for (MetricExampleRooms result : candidate_1_results) {
							metricDisplays.get(display_counter).setSuggestedRoom(result.room);
							metricDisplays.get(display_counter).setOriginalRoom(result.room);
							metricDisplays.get(display_counter).setExampleRoom(result.room, result.granularity_value, result.metric_value);
							display_counter++;
						}
					}


//				LinkedHashMap<Room, Double> candidate_1_results = top_candidates.get(1).getPhenotype().results;
//
//				synchronized (candidate_1_results)
//				{
//					for (Room result : candidate_1_results.keySet())
//					{
//						metricDisplays.get(display_counter).setSuggestedRoom(result);
//						metricDisplays.get(display_counter).setOriginalRoom(result);
//						metricDisplays.get(display_counter).setExampleRoom(result,
//								new GADimensionsGranularity(((display_counter%5) + 1.0)/5.0, 5.0, (display_counter%5) + 1),
//								candidate_1_results.get(result));
//						display_counter++;
//					}
//				}

				Platform.runLater(() -> {
					int i = 0;
					for(MetricPossibleRoom possibleRoom : metricDisplays)
					{
						if(possibleRoom.getSuggestedRoom() != null)
						{
							possibleRoom.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(possibleRoom.getSuggestedRoom(), i));
						}
						else
						{
							possibleRoom.getRoomCanvas().draw(null);
						}
						i++;
					}

//					System.out.println("CANVAS WIDTH: " + canvas.getWidth() + ", CANVAS HEIGHT: " + canvas.getHeight());
				});

			}
		}
		else if(e instanceof UserEditedRoom)
		{
			UserEditedRoom(((UserEditedRoom) e).uniqueCanvasID, ((UserEditedRoom) e).editedRoom);
		}
		else if(e instanceof MAPEGridUpdate)
		{
			if(currentDimensions != null && currentDimensions.length > 0)
			{
				ActionLogger.getInstance().storeAction(ActionType.CHANGE_VALUE,
														View.ROOM, 
														TargetPane.SUGGESTION_PANE, 
														false,
														currentDimensions[0].getDimension(),
														currentDimensions[0].getGranularity(),
														currentDimensions[1].getDimension(),
														currentDimensions[1].getGranularity(),
														((MAPEGridUpdate) e).getDimensions()[0].getDimension(),
														((MAPEGridUpdate) e).getDimensions()[0].getGranularity(),
														((MAPEGridUpdate) e).getDimensions()[1].getDimension(),
														((MAPEGridUpdate) e).getDimensions()[1].getGranularity()
														);
			}
			
			MAPElitesPane.dimensionsUpdated(roomDisplays, ((MAPEGridUpdate) e).getDimensions());
			currentDimensions = ((MAPEGridUpdate) e).getDimensions(); 
			OnChangeTab();
		}
		else if(e instanceof MAPElitesDone)
		{
			if (isActive) {
				//THIS NEEDS TO BE IMPROVED!
				List<Room> generatedRooms = ((MAPElitesDone) e).GetRooms();
//				Room room = (Room) ((MapUpdate) e).getPayload();
//				UUID uuid = ((MapUpdate) e).getID();
				LabeledCanvas canvas;
				synchronized (roomDisplays) {
					
					renderCell(generatedRooms, currentDimensions.length - 1, 
							new float [] {currentDimensions[0].getGranularity(), currentDimensions[1].getGranularity()}, new int[] {0,0});
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
//				nextRoom = 0;
				synchronized (roomDisplays) {
					roomDisplays.get(0).setSuggestedRoom(room);
//					roomDisplays.get(0).setOriginalRoom(getMapView().getMap()); //FIXME: Fix here!

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
			if(selectedSuggestion != null)
				selectedSuggestion.setSelected(false);
			
			selectedSuggestion = (SuggestionRoom) ((SuggestedMapSelected) e).getPayload();
			clearStats();
			
			if(selectedSuggestion == null || selectedSuggestion.getSuggestedRoom() == null)
			{
				getAppSuggestionsBtn().setDisable(true);
				return;
			}
			
			ActionLogger.getInstance().storeAction(ActionType.CLICK,
													View.ROOM, 
													TargetPane.SUGGESTION_PANE, 
													false,
													currentDimensions[0].getDimension(),
													currentDimensions[0].getGranularity(),
													selectedSuggestion.getSuggestedRoom().getDimensionValue(currentDimensions[0].getDimension()),
													currentDimensions[1].getDimension(),
													currentDimensions[1].getGranularity(),
													selectedSuggestion.getSuggestedRoom().getDimensionValue(currentDimensions[1].getDimension()),
													selectedSuggestion.getSuggestedRoom());
			
			selectedSuggestion.getSuggestedRoom().getRoomXML("clicked-suggestion" + File.separator + File.separator);
			
//			clearStats();
			displayStats();
			getAppSuggestionsBtn().setDisable(false);
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
	public void updateRoom(Room room) {

//		TODO: CHECK THIS

		editedRoomPane.editedPane.updateMap(room);
		togglePatterns();
		selectLockModifier();
		router.postEvent(new RoomEdited(editedRoomPane.editedPane.getMap())); //Not sure of this

//		mapIsFeasible(room.isIntraFeasible());

		//FIXME: Added for presentation
//		room.calculateAllDimensionalValues();
//		
//		System.out.println(room.getDimensionValue(DimensionTypes.LENIENCY));         
//		System.out.println(room.getDimensionValue(DimensionTypes.LINEARITY) + ";");        
////		System.out.println(room.getDimensionValue(DimensionTypes.SIMILARITY) + ";");       
//		System.out.println(room.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN) + ";");
//		System.out.println(room.getDimensionValue(DimensionTypes.NUMBER_PATTERNS) + ";");  
//		System.out.println(room.getDimensionValue(DimensionTypes.SYMMETRY) + ";");         
//		System.out.println(room.getDimensionValue(DimensionTypes.INNER_SIMILARITY) + ";"); 
	}

	/**
	 * Gets the current map being controlled by this controller.
	 * 
	 * @return The current map.
	 */
	public Room getCurrentMap() {

//		return getMapView().getMap();
		return null; //TODO: This one can become quite handy
	}

	/**
	 * Renders the map, making it possible to export it.
	 * 
	 * @return A rendered version of the map.
	 */
	public Image getRenderedMap() { //TODO: THERE CAN BE SOME PROBLEM HERE
//		return renderer.renderMap(getMapView().getMap());
		return null; //TODO: This one can become quite handy
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
//			mapView.setCursor(Cursor.DEFAULT);
			myBrush.SetMainComponent(new Tile());
			router.postEvent(new InteractiveRoomBrushUpdated(myBrush, Cursor.DEFAULT));

		} else {
//			mapView.setCursor(Cursor.HAND);
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
			router.postEvent(new InteractiveRoomBrushUpdated(myBrush, Cursor.HAND));
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
		lockButton.setDisable(lockBrush.isSelected());
		router.postEvent(new EditedRoomToggleLocks(lockBrush.isSelected()));
	}	
	
	/**
	 * Toggles the display of patterns on top of the map.
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	public void togglePatterns() { //FIXME: Add this as an event, received by EditedRoomStackPane
		router.postEvent(new EditedRoomTogglePatterns(getPatternButton().isSelected()));
	}
	
	/**
	 * Toggles the display of zones on top of the map.
	 * 
	 */
	public void toggleLocks() { //FIXME: Add this as an event, received by EditedRoomStackPane
//		if (lockButton.isSelected()) {
//			lockCanvas.setVisible(true);
//		} else {
//			lockCanvas.setVisible(false);
//		}
	}

	/**
	 * Generates as many suggested rooms as specified
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	@FXML
	private void saveCurrentGeneration() //TODO: some changes here!
	{	
		switch(currentState)
		{
		case STOPPED:
			//Nothing happens here :D 
			break;
		case RUNNING:
			
			router.postEvent(new SaveDisplayedCells());
//			MAPElitesPane.SaveDimensionalGrid();
//			MapRenderer.getInstance().saveCurrentEditedRoom(getMapView());
			
			break;
		}

	}

	 @FXML
	 private void onContinueMetricSearch(ActionEvent event) throws IOException, InterruptedException
	 {
		ArrayList<MetricExampleRooms> new_examples = new ArrayList<MetricExampleRooms>();

		for(MetricPossibleRoom possible : metricDisplays)
		{
			if(possible.getSuggestedRoom() != null)
			{
				new_examples.add(possible.getPossibleRoom());
			}
		}

		router.postEvent(new MetricContinue(new_examples));
	 }

	@FXML
	private void onRunDimDiscovery(ActionEvent event) throws IOException, InterruptedException {
		System.out.println("DISCOVER DIMS!");
		ArrayList<MetricExampleRooms> example_rooms  = new ArrayList<MetricExampleRooms>();
		MetricExampleRooms very_low = new MetricExampleRooms(verylow_example_pane.editedPane.getMap(),
				new GADimensionsGranularity(0.2, 5.0, 1),
				0.0f,
				true);
		MetricExampleRooms very_high = new MetricExampleRooms(veryhigh_example_pane.editedPane.getMap(),
				new GADimensionsGranularity(1.0, 5.0, 4),
				1.0f,
				true);

		example_rooms.add(very_low);
		example_rooms.add(very_high);

		MetricAlgorithm dim_discovery = new MetricAlgorithm(example_rooms, editedRoomPane.editedPane.getMap());
		dim_discovery.initPopulations(editedRoomPane.editedPane.getMap());
		dim_discovery.start();

//		dim_discovery.join();

//		ExecutorService es = Executors.newCachedThreadPool();
//		for(int i=0;i<1;i++)
//			es.execute(dim_discovery);
//		es.shutdown();
//
//		try {
//			if (!es.awaitTermination(60, TimeUnit.SECONDS)) {
//				es.shutdownNow();
//			}
//		} catch (InterruptedException ex) {
//			es.shutdownNow();
//			Thread.currentThread().interrupt();
//		}

//		boolean finished = es.awaitTermination(1, TimeUnit.MINUTES);
	}

	/**
	 * Generates as many suggested rooms as specified
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	@FXML
	private void generateNewMaps() //TODO: some changes here!
	{	
		resetSuggestedRooms();
		generateNewMaps(editedRoomPane.editedPane.getMap()); //Don't know about this

		//FIXME: COME HERE! how do we generate new maps without knowing which map??
		
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
	 * Generates four new mini maps.
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	public void generateNewMaps(Room room) {
		// TODO: If we want more diversity in the generated maps, then send more StartMapMutate events.
		
		switch(AlgorithmSetup.getInstance().algorithm_type)
		{
			case OBJECTIVE:
			case NOVELTY_SEARCH:

				router.postEvent(new StartMapMutate(room, MapMutationType.Preserving, AlgorithmTypes.Native, 1, true));


	//			int firstAmount = suggestionAmount/2;
	//			int secondAmount = suggestionAmount - firstAmount;
	//
	//			router.postEvent(new StartMapMutate(room, MapMutationType.Preserving, AlgorithmTypes.Native, firstAmount, true)); //TODO: Move some of this hard coding to ApplicationConfig
	//			router.postEvent(new StartMapMutate(room, MapMutationType.ComputedConfig, AlgorithmTypes.Native, secondAmount, true)); //TODO: Move some of this hard coding to ApplicationConfig

				break;
			case MAP_ELITES:
				if(currentDimensions.length > 1)
				{
					router.postEvent(new StartGA_MAPE(room, currentDimensions));

					//Start the evolution using all the dimensions!
	//				MAPEDimensionFXML[] rndDims = new MAPEDimensionFXML[7];
	//				rndDims[0] = new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5);
	//				rndDims[1] = new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5);
	//				rndDims[2] = new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5);
	//				rndDims[3] = new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5);
	//				rndDims[4] = new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5);
	//				rndDims[5] = new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5);
	//				rndDims[6] = new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5);
	//				router.postEvent(new StartGA_MAPE(room, rndDims));
				}

				break;
//		case CVT_MAP_ELITES:
//			break;
		default:
			break;
		
		}
	}

	public void replaceRoom()
	{
		//pass the info from one room to the other one
		
		if(selectedSuggestion != null)
		{
			ActionLogger.getInstance().storeAction(ActionType.CHANGE_VALUE,
													View.ROOM, 
													TargetPane.SUGGESTION_PANE, 
													false,
													currentDimensions[0].getDimension(),
													currentDimensions[0].getGranularity(),
													selectedSuggestion.getSuggestedRoom().getDimensionValue(currentDimensions[0].getDimension()),
													currentDimensions[1].getDimension(),
													currentDimensions[1].getGranularity(),
													selectedSuggestion.getSuggestedRoom().getDimensionValue(currentDimensions[1].getDimension()),
													selectedSuggestion.getSuggestedRoom());
			
			
//			selectedSuggestion.getSuggestedRoom()(prefix);
			selectedSuggestion.getSuggestedRoom().getRoomXML("picked-room" + File.separator + File.separator);
			
			router.postEvent(new SaveCurrentGeneration());

			//FIXME: Change when applying suggestion!
			// Not sure of this
			editedRoomPane.editedPane.getMap().applySuggestion(selectedSuggestion.getSuggestedRoom());
			updateRoom(editedRoomPane.editedPane.getMap());
		}
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

	//FIXME: This has never been good! so it needs to change now! (but not today!)
	@FXML
	public void displayStats() 
	{
//		Room original = getMapView().getMap();
//		Room toCompare = selectedSuggestion.getSuggestedRoom();
//
//		int originalEnemies = 0;
//		int compareEnemies = 0;
//
//		getMapView().getMap().createLists();
//		toCompare.createLists();
//
//		StringBuilder str = new StringBuilder();
//		str.append("Number of enemies: ");
//
//		str.append(getMapView().getMap().getEnemyCount());
//		str.append(" ➤  ");
//		enemyNumbr.setText(str.toString());
//		str = new StringBuilder();
//
//		str.append(toCompare.getEnemyCount());
//		if (getMapView().getMap().getEnemyCount() > toCompare.getEnemyCount()) {
//			str.append(" ▼");
//			enemyNumbr2.setText(str.toString());
//			enemyNumbr2.setStyle("-fx-text-fill: red");
//		} else if (getMapView().getMap().getEnemyCount() < toCompare.getEnemyCount()) {
//			str.append(" ▲");
//			enemyNumbr2.setText(str.toString());
//			enemyNumbr2.setStyle("-fx-text-fill: green");
//		} else {
//			enemyNumbr2.setText(str.toString());
//			enemyNumbr2.setStyle("-fx-text-fill: white");
//		}
//
//		str = new StringBuilder();
//
//
//		str.append("Number of treasures: ");
//
//		str.append(getMapView().getMap().getTreasureCount());
//		str.append(" ➤  ");
//		treasureNmbr.setText(str.toString());
//		str = new StringBuilder();
//
//		str.append(toCompare.getTreasureCount());
//		if (getMapView().getMap().getTreasureCount() > toCompare.getTreasureCount()) {
//			str.append(" ▼");
//			treasureNmbr2.setText(str.toString());
//			treasureNmbr2.setStyle("-fx-text-fill: red");
//		} else if (getMapView().getMap().getTreasureCount() < toCompare.getTreasureCount()) {
//			str.append(" ▲");
//			treasureNmbr2.setText(str.toString());
//			treasureNmbr2.setStyle("-fx-text-fill: green");
//		} else {
//			treasureNmbr2.setText(str.toString());
//			treasureNmbr2.setStyle("-fx-text-fill: white");
//		}
//
//		str = new StringBuilder();
//
//
//		str.append("Treasure percentage: ");
//
//		str.append(round(getMapView().getMap().getTreasurePercentage()* 100, 2 ));
//		str.append("%");
//
//		str.append(" ➤  ");
//		treasurePercent.setText(str.toString());
//		str = new StringBuilder();
//
//		str.append(round(toCompare.getTreasurePercentage()* 100, 2 ));
//		str.append("%");
//
//		if (getMapView().getMap().getTreasurePercentage() > toCompare.getTreasurePercentage()) {
//			str.append(" ▼");
//			treasurePercent2.setText(str.toString());
//			treasurePercent2.setStyle("-fx-text-fill: red");
//		} else if (getMapView().getMap().getTreasurePercentage() < toCompare.getTreasurePercentage()) {
//			str.append(" ▲");
//			treasurePercent2.setText(str.toString());
//			treasurePercent2.setStyle("-fx-text-fill: green");
//		} else {
//			treasurePercent2.setText(str.toString());
//			treasurePercent2.setStyle("-fx-text-fill: white");
//		}
//
//		str = new StringBuilder();
//
//
//		str.append("Enemy percentage: ");
//
//		str.append(round(getMapView().getMap().getEnemyPercentage()* 100, 2 ));
//		str.append("%");
//
//		str.append(" ➤  ");
//		enemyPercent.setText(str.toString());
//		str = new StringBuilder();
//
//		str.append(round(toCompare.getEnemyPercentage()* 100, 2 ));
//		str.append("%");
//
//		if (getMapView().getMap().getEnemyPercentage() > toCompare.getEnemyPercentage()) {
//			str.append(" ▼");
//			enemyPercent2.setText(str.toString());
//			enemyPercent2.setStyle("-fx-text-fill: red");
//		} else if (getMapView().getMap().getEnemyPercentage() < toCompare.getEnemyPercentage()) {
//			str.append(" ▲");
//			enemyPercent2.setText(str.toString());
//			enemyPercent2.setStyle("-fx-text-fill: green");
//		} else {
//			enemyPercent2.setText(str.toString());
//			enemyPercent2.setStyle("-fx-text-fill: white");
//		}
//
//		str = new StringBuilder();
//
//
//		str.append("Entrance safety: ");
//
//		str.append(round(getMapView().getMap().getDoorSafeness()* 100, 2 ));
//		str.append("%");
//
//		str.append(" ➤  ");
//		entranceSafety.setText(str.toString());
//		str = new StringBuilder();
//
//		str.append(round(toCompare.getDoorSafeness()* 100, 2 ));
//		str.append("%");
//
//		if (getMapView().getMap().getDoorSafeness() > toCompare.getDoorSafeness()) {
//			str.append(" ▼");
//			entranceSafety2.setText(str.toString());
//			entranceSafety2.setStyle("-fx-text-fill: red");
//		} else if (getMapView().getMap().getDoorSafeness() < toCompare.getDoorSafeness()) {
//			str.append(" ▲");
//			entranceSafety2.setText(str.toString());
//			entranceSafety2.setStyle("-fx-text-fill: green");
//		} else {
//			entranceSafety2.setText(str.toString());
//			entranceSafety2.setStyle("-fx-text-fill: white");
//		}
//
//		str = new StringBuilder();
//
//
//		str.append("Treasure safety: ");
//
//		Double[] safeties = getMapView().getMap().getAllTreasureSafeties();
//
//		double totalSafety = 0;
//
//		for (double d : safeties) {
//			totalSafety += d;
//		}
//
//		if (safeties.length != 0) {
//			totalSafety = totalSafety/safeties.length;
//		}
//		safeties = toCompare.getAllTreasureSafeties();
//
//		double totalSafety2 = 0;
//
//		for (double d : safeties) {
//			totalSafety2 += d;
//		}
//
//		if (safeties.length != 0) {
//			totalSafety2 = totalSafety2/safeties.length;
//		}
//
//		str.append(round(totalSafety * 100, 2));
//		str.append("%");
//
//		str.append(" ➤  ");
//		treasureSafety.setText(str.toString());
//		str = new StringBuilder();
//
//		str.append(round(totalSafety2 * 100, 2));
//		str.append("%");
//		if (totalSafety > totalSafety2) {
//			str.append(" ▼");
//			treasureSafety2.setText(str.toString());
//			treasureSafety2.setStyle("-fx-text-fill: red");
//		} else if (totalSafety < totalSafety2) {
//			str.append(" ▲");
//			treasureSafety2.setText(str.toString());
//			treasureSafety2.setStyle("-fx-text-fill: green");
//		} else {
//			treasureSafety2.setText(str.toString());
//			treasureSafety2.setStyle("-fx-text-fill: white");
//		}

	}
	
	public void roomMouseEvents() 
	{
//		getMapView().addEventFilter(MouseEvent.MOUSE_CLICKED, new EditViewEventHandler());
//		getMapView().addEventFilter(MouseEvent.MOUSE_MOVED, new EditViewMouseHover());
	}


	//FIXME: ALSO CHECK HERE, saving the room will be a bit harder (this probably shouold be in interactiveMAP)
	public void saveEditedRoomInfo()
	{
//		String DIRECTORY= System.getProperty("user.dir") + "\\my-data\\custom-save\\";
//		StringBuilder sb = new StringBuilder();
//
//		mapView.getMap().calculateAllDimensionalValues();
//		sb.append(mapView.getMap().getDimensionValue(DimensionTypes.LENIENCY) + ";");
//		sb.append(mapView.getMap().getDimensionValue(DimensionTypes.LINEARITY) + ";");
//		sb.append("1.0;");  //similarity
//		sb.append(mapView.getMap().getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN) + ";");
//		sb.append(mapView.getMap().getDimensionValue(DimensionTypes.NUMBER_PATTERNS) + ";");
//		sb.append(mapView.getMap().getDimensionValue(DimensionTypes.SYMMETRY) + ";");
//		sb.append("1.0;");  //Inner_similarity
//		sb.append("1.0;");  //fitness
//		sb.append("1.0;");  //score
//		sb.append(currentDimensions[0].getDimension() + ";");
//		sb.append(currentDimensions[1].getDimension() + ";");
//		sb.append(currentEditionStep + ";");  //current STEP
//		sb.append("ER" + System.lineSeparator()); //TYPE
//
//		currentEditionStep++;
//
//
////		File file = new File(DIRECTORY + "expressive_range-" + dimensions[0].getDimension() + "_" + dimensions[1].getDimension() + ".csv");
//		File file = new File(DIRECTORY + "custom-room-edition.csv");
//		try {
//			FileUtils.write(file, sb, true);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	public void someRoomHovered(Room room)
	{

	}

	public void someRoomEdited(Room room)
	{

	}
	
	public Room getSelectedMiniMap() {
		return selectedSuggestion.getSuggestedRoom();
	}

	public void setSelectedMiniMap(Room selectedMiniMap) {
		this.selectedSuggestion.setSuggestedRoom(selectedMiniMap);
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
