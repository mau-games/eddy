package gui.views;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import collectors.DataSaverLoader;
import game.*;
import game.CoCreativity.*;
import game.Game.MapMutationType;
import game.tiles.*;
import generator.algorithm.Algorithm.AlgorithmTypes;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import gui.controls.*;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;
import util.eventrouting.events.intraview.EditedRoomToggleLocks;
import util.eventrouting.events.intraview.EditedRoomTogglePatterns;
import util.eventrouting.events.intraview.InteractiveRoomBrushUpdated;
import util.eventrouting.events.intraview.UserEditedRoom;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;

/**
 * This class controls the Co-Creative Room edit view
 *
 * 
 * @author Johan Holmberg, Malmö University
 * @author Alberto Alvarez, Malmö University
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 * @author Tinea Larsson, Malmö University
 */
public class CCRoomViewController extends BorderPane implements Listener
{
	@FXML private ComboBox<String> DisplayCombo;

	@FXML public EditedRoomStackPane editedRoomPane;

	@FXML public StackPane mapPane;
	@FXML public Pane minimap;

	//left side as well
	@FXML private GridPane legend; //
	@FXML private ToggleGroup brushes; //


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

	@FXML private Label turnLabel;
	@FXML private Label tilesLeftLabel;

	@FXML private Button worldGridBtn; //ok
	@FXML private Button genSuggestionsBtn; //bra
	@FXML private Button appSuggestionsBtn; //bra
	@FXML private Button endTurnBtn; //
	@FXML private Button continueBtn; //
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
	private Canvas tileCanvas;

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

	private String file_name;

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
	public CCRoomViewController() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/interactive/CCRoomView.fxml"));
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
		router.registerListener(this, new AIPrepareContributionsDone());
		router.registerListener(this, new AICalculateContributionsDone());
		router.registerListener(this, new AITurnDone());
		router.registerListener(this, new MapUpdate(null));
		router.registerListener(this, new ApplySuggestion(0));                  //
		router.registerListener(this, new SuggestedMapsDone());
		router.registerListener(this, new SuggestedMapSelected(null));             //
		router.registerListener(this, new UserEditedRoom(null, null));
		router.registerListener(this, new MetricUpdate());

		myBrush = new Drawer();
		myBrush.AddmodifierComponent("Lock", new Modifier(lockBrush));

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

		//suggestionsPane.setVisible(false);

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
		
		currentState = EvoState.RUNNING;

		saveGenBtn.setDisable(false);

		file_name = generateNewFileName();
	}
	
	@FXML
	private void OnChangeTab()
	{
		//TODO: Debugging here!
		if(editedRoomPane.editedPane != null && editedRoomPane.editedPane.getMap() != null)
		{
			int paths = editedRoomPane.editedPane.getMap().LinearityWithinRoom();
			double doors =  editedRoomPane.editedPane.getMap().getDoors().size();
			double maxPaths = ((double) editedRoomPane.editedPane.getMap().getPatternFinder().getPatternGraph().countNodes()) + (double)(doors * 3) + doors;
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
		mapHeight = (int)(42.0 * (float)((float)roomToBe.getRowCount())); //Recalculate map size
		mapWidth = (int)(42.0 * (float)((float)roomToBe.getColCount()));//Recalculate map size

		for(SuggestionRoom sr : roomDisplays)
		{
			sr.resizeCanvasForRoom(roomToBe);
		}

		editedRoomPane.init(mapWidth, mapHeight, roomToBe);

		initButtons();
		initLegend();
		initCCLabels();

		AICoCreator.getInstance().initAiCoCreator(roomToBe.getColCount(), roomToBe.getRowCount(), this);
		resetView();
		roomToBe.forceReevaluation();
		updateRoom(roomToBe);

		generateNewMaps();
	}

	private void initCCLabels()
	{
		turnLabel.setStyle("-fx-font-weight: bold");
		turnLabel.setStyle("-fx-text-fill: white;");
		turnLabel.setFont(Font.font("Arial", 16));

		tilesLeftLabel.setStyle("-fx-font-weight: bold");
		tilesLeftLabel.setStyle("-fx-text-fill: white;");
		tilesLeftLabel.setFont(Font.font("Arial", 16));

		updateLabels();
	}

	public void updateLabels()
	{
		if(AICoCreator.getInstance().getActive())
		{
			System.out.println("INNE I UPDATE AI ACTIVE");
			turnLabel.setVisible(false);
			turnLabel.setText("AI's Turn");
			turnLabel.setVisible(true);

			tilesLeftLabel.setVisible(false);
		}
		else
		{
			turnLabel.setVisible(false);
			turnLabel.setText("Your Turn");
			turnLabel.setVisible(true);

			int max = HumanCoCreator.getInstance().getMaxTilesPerRound();
			int num = HumanCoCreator.getInstance().getAmountOfTilesPlaced();

			tilesLeftLabel.setVisible(false);
			tilesLeftLabel.setText("Tiles left this round: " + (max-num) +"/" + max);
			tilesLeftLabel.setVisible(true);
		}
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

		disableSuggestionView();
	}

	/**
	 * Displays the continue button and label
	 */
	private void enableSuggestionView()
	{
		continueBtn.setVisible(true);

		continueBtn.setTooltip(new Tooltip("Continue to human's turn"));
	}

	/**
	 * Hides the continue button and label
	 */
	private void disableSuggestionView()
	{
		continueBtn.setVisible(false);
		editedRoomPane.clearAISuggestionView();
	}

	/**
	 * Displays the End Turn Button and label
	 */
	private void enableTurnBasedView()
	{
		endTurnBtn.setVisible(true);

		continueBtn.setTooltip(new Tooltip("Continue to human's turn"));
	}

	/**
	 * Hides the continue button and label
	 */
	private void disableTurnBasedView()
	{
		endTurnBtn.setVisible(false);

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

		if(e instanceof UserEditedRoom)
		{
			UserEditedRoom(((UserEditedRoom) e).uniqueCanvasID, ((UserEditedRoom) e).editedRoom);
			updateLabels();
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
		else if(e instanceof MAPElitesDone) // change here to what the AI will do which is calculate his turn
		{
			if (isActive) {
				//THIS NEEDS TO BE IMPROVED!

				System.out.println("MAP-ELITES DONE");

				List<Room> generatedRooms = ((MAPElitesDone) e).GetRooms();

				System.out.println("generatedRooms: " + generatedRooms.size());
				System.out.println("currentDimensions: " + currentDimensions.length);
				System.out.println();

				AICoCreator.getInstance().setGeneratedElites(generatedRooms);

				generateNewMaps(editedRoomPane.editedPane.getMap());



//				Room room = (Room) ((MapUpdate) e).getPayload();
//				UUID uuid = ((MapUpdate) e).getID();                       
				//LabeledCanvas canvas;
				//synchronized (roomDisplays) {
//
				//	renderCell(generatedRooms, currentDimensions.length - 1,
				//			new float [] {currentDimensions[0].getGranularity(), currentDimensions[1].getGranularity()}, new int[] {0,0});
				//}

				//Platform.runLater(() -> {
				//	int i = 0;
				//	for(SuggestionRoom sugRoom : roomDisplays)
				//	{
				//		if(sugRoom.getSuggestedRoom() != null)
				//		{
				//			sugRoom.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(sugRoom.getSuggestedRoom(), i));
				//		}
				//		else
				//		{
				//			sugRoom.getRoomCanvas().draw(null);
				//		}
				//		i++;
				//	}
//
//				//	System.out.println("CANVAS WIDTH: " + canvas.getWidth() + ", CANVAS HEIGHT: " + canvas.getHeight());
				//});

			}
		}
		else if(e instanceof AIPrepareContributionsDone)
		{

			System.out.println("AICOCREATOR.GETACTIVE: " + AICoCreator.getInstance().getActive());
			AICoCreator.getInstance().CalculateContribution();
		}
		else if(e instanceof AICalculateContributionsDone)
		{
			if(isActive)
			{
				System.out.println("AI Contributions are ready");

				switch(AICoCreator.getInstance().getControlLevel())
				{
					case LOW:
						// Suggest
						System.out.println("AICC SUGGESTIONS STARTED");
						disableTurnBasedView();
						enableSuggestionView();
						editedRoomPane.displaySuggestions(AICoCreator.getInstance().GetContributions(), this);
						break;

					case MEDIUM:
					case HIGH:
						//place
						System.out.println("AICC EDIT ROOM STARTED");
						editedRoomPane.CCRoomEdited(editedRoomPane.editedPane.getMap(), AICoCreator.getInstance().GetContributions());
						router.postEvent(new AITurnDone());
						break;
				}
			}
		}
		else if(e instanceof AITurnDone)
		{
			System.out.println("AI FINISHED - HUMAN'S TURN AGAIN");
			disableSuggestionView();
			enableTurnBasedView();
			AICoCreator.getInstance().setActive(false);
			HumanCoCreator.getInstance().resetRound();
			//AICoCreator.getInstance().resetRound(); //
			updateLabels();
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

			selectedSuggestion.getSuggestedRoom().getRoomXML("clicked-suggestion\\");

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
	@FXML
	public void updateRoom(Room room) {

		editedRoomPane.editedPane.updateMap(room);
		togglePatterns();
		selectLockModifier();
		router.postEvent(new RoomEdited(editedRoomPane.editedPane.getMap())); //Not sure of this

//		getMapView().updateMap(room);
//		redrawPatterns(room);
//		redrawLocks(room);
//		mapIsFeasible(room.isIntraFeasible());
//		router.postEvent(new RoomEdited(mapView.getMap()));
		
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
			myBrush.SetMainComponent(new Tile());
			router.postEvent(new InteractiveRoomBrushUpdated(myBrush, Cursor.DEFAULT));
			
		} else {
//			editedRoomPane.editedPane.setCursor(Cursor.HAND);
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
		router.postEvent(new EditedRoomToggleLocks(lockButton.isSelected()));
//		if (lockButton.isSelected()) {
//			lockCanvas.setVisible(true);
//		} else {
//			lockCanvas.setVisible(false);
//		}
	}

	/**
	 * Function connected to End Turn button
	 *
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	@FXML
	private void endTurn()
	{
		System.out.println("END TURN PRESSED");

		System.out.println("AICC PREPARE TURN");
		AICoCreator.getInstance().setActive(true);
		updateLabels();
		AICoCreator.getInstance().prepareTurn(editedRoomPane.editedPane.getMap());



		//generateNewMaps(editedRoomPane.editedPane.getMap());

		//System.out.println("AICC CALCULATE CONTRIBUTIONS");
		//aiCC.CalculateContribution();
		//aiCC.GetContributions() (Tiles in a list)
		//place them down one by one (with 0.1-0.5 seconds between for visibility)

		//System.out.println("AICC EDIT ROOM");
		//editedRoomPane.CCRoomEdited(editedRoomPane.editedPane, editedRoomPane.editedPane.getMap()); //this would be replaced by for loop for each contribution
		//// switch case with aiCC.getControlLevel for placing or displaying
//

//
		//System.out.println("HUMAN'S TURN AGAIN");
		//HumanCoCreator.getInstance().resetRound();
	}

	/**
	 * Function connected to Continue button
	 *
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	@FXML
	private void aiContinue()
	{
		router.postEvent(new AITurnDone());
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

				break;
			case MAP_ELITES:
				if(currentDimensions.length > 1)
				{

					System.out.println("generateNewMaps called");
					System.out.println("currentDimensions: " + currentDimensions.length);
					//router.postEvent(new StartGA_MAPE(room, currentDimensions));
					router.postEvent(new StartGA_MAPE(room));
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
			selectedSuggestion.getSuggestedRoom().getRoomXML("picked-room\\");

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

	@FXML
	public void displayStats() 
	{

		Room original = editedRoomPane.editedPane.getMap();
		Room toCompare = selectedSuggestion.getSuggestedRoom();
		
		int originalEnemies = 0;
		int compareEnemies = 0;
		
		editedRoomPane.editedPane.getMap().createLists();
		toCompare.createLists();
		
		StringBuilder str = new StringBuilder();
		str.append("Number of enemies: ");
		
		str.append(editedRoomPane.editedPane.getMap().getEnemyCount());
		str.append(" ➤  ");
		enemyNumbr.setText(str.toString());	
		str = new StringBuilder();

		str.append(toCompare.getEnemyCount());
		if (editedRoomPane.editedPane.getMap().getEnemyCount() > toCompare.getEnemyCount()) {
			str.append(" ▼");
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: red");
		} else if (editedRoomPane.editedPane.getMap().getEnemyCount() < toCompare.getEnemyCount()) {
			str.append(" ▲");
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: green");
		} else {
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Number of treasures: ");

		str.append(editedRoomPane.editedPane.getMap().getTreasureCount());
		str.append(" ➤  ");
		treasureNmbr.setText(str.toString());	
		str = new StringBuilder();

		str.append(toCompare.getTreasureCount());
		if (editedRoomPane.editedPane.getMap().getTreasureCount() > toCompare.getTreasureCount()) {
			str.append(" ▼");
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: red");
		} else if (editedRoomPane.editedPane.getMap().getTreasureCount() < toCompare.getTreasureCount()) {
			str.append(" ▲");
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: green");
		} else {
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Treasure percentage: ");

		str.append(round(editedRoomPane.editedPane.getMap().getTreasurePercentage()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		treasurePercent.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(toCompare.getTreasurePercentage()* 100, 2 ));
		str.append("%");

		if (editedRoomPane.editedPane.getMap().getTreasurePercentage() > toCompare.getTreasurePercentage()) {
			str.append(" ▼");
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: red");
		} else if (editedRoomPane.editedPane.getMap().getTreasurePercentage() < toCompare.getTreasurePercentage()) {
			str.append(" ▲");
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: green");
		} else {
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Enemy percentage: ");

		str.append(round(editedRoomPane.editedPane.getMap().getEnemyPercentage()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		enemyPercent.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(toCompare.getEnemyPercentage()* 100, 2 ));
		str.append("%");

		if (editedRoomPane.editedPane.getMap().getEnemyPercentage() > toCompare.getEnemyPercentage()) {
			str.append(" ▼");
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: red");
		} else if (editedRoomPane.editedPane.getMap().getEnemyPercentage() < toCompare.getEnemyPercentage()) {
			str.append(" ▲");
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: green");
		} else {
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Entrance safety: ");

		str.append(round(editedRoomPane.editedPane.getMap().getDoorSafeness()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		entranceSafety.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(toCompare.getDoorSafeness()* 100, 2 ));
		str.append("%");

		if (editedRoomPane.editedPane.getMap().getDoorSafeness() > toCompare.getDoorSafeness()) {
			str.append(" ▼");
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: red");
		} else if (editedRoomPane.editedPane.getMap().getDoorSafeness() < toCompare.getDoorSafeness()) {
			str.append(" ▲");
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: green");
		} else {
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Treasure safety: ");

		Double[] safeties = editedRoomPane.editedPane.getMap().getAllTreasureSafeties();

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

	
	public void saveEditedRoomInfo()
	{
		String DIRECTORY= System.getProperty("user.dir") + "\\my-data\\custom-save\\";
		StringBuilder sb = new StringBuilder();

		editedRoomPane.editedPane.getMap().calculateAllDimensionalValues();
		sb.append(editedRoomPane.editedPane.getMap().getDimensionValue(DimensionTypes.LENIENCY) + ";");
		sb.append(editedRoomPane.editedPane.getMap().getDimensionValue(DimensionTypes.LINEARITY) + ";");
		sb.append("1.0;");  //similarity
		sb.append(editedRoomPane.editedPane.getMap().getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN) + ";");
		sb.append(editedRoomPane.editedPane.getMap().getDimensionValue(DimensionTypes.NUMBER_PATTERNS) + ";");
		sb.append(editedRoomPane.editedPane.getMap().getDimensionValue(DimensionTypes.SYMMETRY) + ";");
		sb.append("1.0;");  //Inner_similarity
		sb.append("1.0;");  //fitness
		sb.append("1.0;");  //score
		sb.append(currentDimensions[0].getDimension() + ";");
		sb.append(currentDimensions[1].getDimension() + ";");
		sb.append(currentEditionStep + ";");  //current STEP
		sb.append("ER" + System.lineSeparator()); //TYPE	    
		
		currentEditionStep++;
		

//		File file = new File(DIRECTORY + "expressive_range-" + dimensions[0].getDimension() + "_" + dimensions[1].getDimension() + ".csv");
		File file = new File(DIRECTORY + "custom-room-edition.csv");
		try {
			FileUtils.write(file, sb, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}


	private String generateNewFileName()
	{
		String filename = "";
		long millis = System.currentTimeMillis();
		String datetime = new Date().toGMTString();
		datetime = datetime.replace(" ", "");
		datetime = datetime.replace(":", "");

		int i = (int)(Math.random() * 100);
		String rndchars = "" + i;
		filename = rndchars + "_" + datetime + "_" + millis;
		return filename;
	}

	/**
	 * Saving data from Co-Creation
	 * */
	public void saveActionData(Tile prev_tile, Tile new_tile)
	{
		//open file

		//if file doesn't exist, create it

		//write the action
		// if prev_tile is floor it is human placed

		// if



		Document dom;
		Element e = null;
		Element next = null;

		String fileString = DataSaverLoader.projectPath + "\\cc-data-collection\\" + AICoCreator.getInstance().getControlLevel().name() +"\\"+ file_name;
		System.out.println("Writing to: " + fileString);

		//File file = new File(DataSaverLoader.projectPath + "\\" + direction + "\\room\\" + room_id);

		File file = new File(fileString);
		if (!file.exists()) {
			file.mkdirs();
		}
//
		//String xml = System.getProperty("user.dir") + "\\my-data\\" + direction + "\\room\\" + room_id + "\\room-" + room_id + "_" + indexEditionStep + ".xml";
//
		//// instance of a DocumentBuilderFactory
		//DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		//try {
		//	// use factory to get an instance of document builder
		//	DocumentBuilder db = dbf.newDocumentBuilder();
		//	// create instance of DOM
		//	dom = db.newDocument();
//
		//	// create the root element
		//	Element rootEle = dom.createElement("Room");
		//	rootEle.setAttribute("ID", this.toString());
		//	rootEle.setAttribute("width", Integer.toString(this.getColCount()));
		//	rootEle.setAttribute("height", Integer.toString(this.getRowCount()));
		//	rootEle.setAttribute("time", new Timestamp(System.currentTimeMillis()).toString());
//	    //    rootEle.setAttribute("type", "SUGGESTIONS OR MAIN");
//
		//	// create data elements and place them under root
		//	e = dom.createElement("Dimensions");
		//	rootEle.appendChild(e);
//
		//	//DIMENSIONS --> THIS IS IMPORTANT TO CHANGE!! TODO:!!
		//	next = dom.createElement("Dimension");
		//	next.setAttribute("name", DimensionTypes.SIMILARITY.toString());
		//	next.setAttribute("value", Double.toString(getDimensionValue(DimensionTypes.SIMILARITY)));
		//	e.appendChild(next);
//
		//	next = dom.createElement("Dimension");
		//	next.setAttribute("name", DimensionTypes.SYMMETRY.toString());
		//	next.setAttribute("value", Double.toString(getDimensionValue(DimensionTypes.SYMMETRY)));
		//	e.appendChild(next);
//
		//	//TILES
		//	e = dom.createElement("Tiles");
		//	rootEle.appendChild(e);
//
		//	for (int j = 0; j < height; j++)
		//	{
		//		for (int i = 0; i < width; i++)
		//		{
		//			next = dom.createElement("Tile");
		//			next.setAttribute("value", getTile(i, j).GetType().toString());
		//			next.setAttribute("immutable", Boolean.toString(getTile(i, j).GetImmutable()));
		//			next.setAttribute("PosX", Integer.toString(i));
		//			next.setAttribute("PosY", Integer.toString(j));
		//			e.appendChild(next);
		//		}
		//	}
//
		//	e = dom.createElement("Customs");
		//	rootEle.appendChild(e);
//
		//	for(Tile custom : customTiles)
		//	{
		//		next = dom.createElement("Custom");
		//		next.setAttribute("value", custom.GetType().toString());
		//		next.setAttribute("immutable", Boolean.toString(custom.GetImmutable()));
		//		next.setAttribute("centerX", Integer.toString(custom.GetCenterPosition().getX()));
		//		next.setAttribute("centerY", Integer.toString(custom.GetCenterPosition().getY()));
		//		e.appendChild(next);
		//	}
//
		//	dom.appendChild(rootEle);
//
		//	try {
		//		Transformer tr = TransformerFactory.newInstance().newTransformer();
		//		tr.setOutputProperty(OutputKeys.INDENT, "yes");
		//		tr.setOutputProperty(OutputKeys.METHOD, "xml");
		//		tr.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
		//		tr.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, "room.dtd");
		//		tr.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
//
		//		// send DOM to file
		//		tr.transform(new DOMSource(dom),
		//				new StreamResult(new FileOutputStream(xml)));
//
		//	} catch (TransformerException te) {
		//		System.out.println(te.getMessage());
		//	} catch (IOException ioe) {
		//		System.out.println(ioe.getMessage());
		//	}
		//} catch (ParserConfigurationException pce) {
		//	System.out.println("UsersXML: Error trying to instantiate DocumentBuilder " + pce);
		//}

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
