package gui;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import collectors.ActionLogger;
import collectors.XMLHandler;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import game.AlgorithmSetup;
import game.ApplicationConfig;
import game.Dungeon;
import game.Room;
import game.Tile;
import game.TileTypes;
import game.Game.MapMutationType;
import game.tiles.BossEnemyTile;
import game.tiles.DoorTile;
import game.tiles.EnemyTile;
import game.tiles.FloorTile;
import game.tiles.NullTile;
import game.tiles.TreasureTile;
import game.tiles.WallTile;
import generator.algorithm.Algorithm.AlgorithmTypes;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.config.GeneratorConfig;
import gui.controls.Drawer;
import gui.controls.EvolutionMAPEPane;
import gui.controls.InteractiveMap;
import gui.controls.Modifier;
import gui.controls.RoomPreview;
import gui.controls.SuggestionRoom;
import gui.utils.InformativePopupManager;
import gui.utils.MapRenderer;
import gui.utils.InformativePopupManager.PresentableInformation;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import util.Point;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.ApplySuggestion;
import util.eventrouting.events.MAPEGridUpdate;
import util.eventrouting.events.MAPElitesDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.NextStepSequenceExperiment;
import util.eventrouting.events.RoomEdited;
import util.eventrouting.events.SaveDisplayedCells;
import util.eventrouting.events.StartMapMutate;
import util.eventrouting.events.SuggestedMapSelected;
import util.eventrouting.events.SuggestedMapsDone;
import util.eventrouting.events.intraview.DungeonPreviewSelected;
import util.eventrouting.events.intraview.LoadedXMLRoomSelected;
import util.eventrouting.events.intraview.RestartDimensionsExperiment;
import util.eventrouting.events.intraview.RoomEditionStarted;
import util.eventrouting.events.intraview.SequencePreviewSelected;
import util.eventrouting.events.intraview.SessionRoomSelected;

public class ExperimentsGUIController implements Initializable, Listener {

	public enum SequenceExperiment
	{
		REPEAT,
		OBJECTIVE,
		MAPELITES
	}
	
	////////// MAIN CENTER PANE /////////
	@FXML public StackPane roomPane;
	@FXML public TextField sequenceSaveFilename;
	
	//Evaluation pane!
	@FXML public Text fitnessText;
	@FXML public Text NMesoText;
	@FXML public Text NMicroText;
	@FXML public Text lenText;
	@FXML public Text linText;
	@FXML public Text symText;
	@FXML public TextField evaluationFilename;
	
	//Run Experiment PANE!!!
	@FXML TextField secondsTF;
	@FXML TextField stepsTF;
	@FXML TextField fromTF;
//	@FXML private ToggleGroup experimentToggle;
	@FXML public TextField experimentFilename;
	@FXML public ToggleButton saveDataButton;
	@FXML public ComboBox<SequenceExperiment> experimentTypeCombo;
	private SequenceExperiment experimentType = SequenceExperiment.REPEAT;
	
	///////BOTTOM BUTTONS!! /////////
	@FXML private ToggleButton editButton;
	@FXML private Button continueBtn;
	@FXML private Button saveAllBtn;
	
	///// BOTTOM BOXES ////
	@FXML public HBox fileLister;
	@FXML public HBox editedRoomSteps;
	
	//Left side :: BRUSHES
	@FXML public ToolBar brushesSide;
	@FXML private ToggleGroup brushes;
	public Drawer myBrush;
	EditViewEventHandler mouseEdit= new EditViewEventHandler();
	EditViewMouseHover mouseHover= new EditViewMouseHover();
	//All the buttons to the left
	@FXML private ToggleButton patternButton;
	@FXML private ToggleButton lockBrush;
	@FXML private ToggleButton lockButton;
	@FXML private ToggleButton floorBtn;
	@FXML private ToggleButton wallBtn;
	@FXML private ToggleButton treasureBtn;
	@FXML private ToggleButton enemyBtn;
	@FXML private ToggleButton bossEnemyBtn;
	@FXML private Slider brushSlider;
	
	//For left side visuals
	LinkedList<Room> sessionCreatedRooms = new LinkedList<Room>();
	@FXML private VBox sessionCreations;
	
	//RIGHT SIDE!!! :: ALGORITHMS
	@FXML private VBox rightSidePane;
	
	//THE PANES THAT CAN BE INSTANTIATED:
	private EvolutionMAPEPane evolutionPane;
//	private NGramPane nGramPane;
//	private RoomSequenceVisualizer sequenceVisualizer;
	
	
	DirectoryChooser directoryChooser;
	File selectedDirectory;

	private ApplicationConfig config;
	private GeneratorConfig gc;
	private InteractiveMap roomInteractiveView;
	private Canvas brushCanvas;
	private Dungeon testDungeon = new Dungeon();
	private Room currentEditRoom;
	
	LinkedList<Room> loadedRooms;
	int roomsPerPage = 10;
	int maxLoadedRoomsPages = 0;
	int currentLoadedRoomsPage = 0;
	
	//instances
	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	
	//helpers
	private static DecimalFormat df2 = new DecimalFormat("#.##", DecimalFormatSymbols.getInstance(Locale.US));
	
	//For the experiments
	private int currentCombination = 0;
	private ArrayList<MAPEDimensionFXML[]> possibleCombinations = new ArrayList<MAPEDimensionFXML[]>();
	private boolean useOurCombs = true;
	
	//TEST
	int counter = 10;
	int index = 0;
	Room andAnotherOne;
	boolean runningExperiment = false;
	int currentEditedRoom = 0;
	boolean browsingEditions = false;
	
	public ExperimentsGUIController()
	{
		super();
		gc = null;
		try {
			gc = new GeneratorConfig();

		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		////////////// ALLL EVENTS!! ////////////////////
		
		router.registerListener(this, new MAPEGridUpdate(null));
		router.registerListener(this, new MAPElitesDone());
		router.registerListener(this, new MapUpdate(null));
		router.registerListener(this, new ApplySuggestion(0));
		router.registerListener(this, new SuggestedMapsDone());
		router.registerListener(this, new SuggestedMapSelected(null));
//		router.registerListener(this, new SuggestionApplied(null));
		router.registerListener(this, new DungeonPreviewSelected(null));
		router.registerListener(this, new SequencePreviewSelected(null));
		router.registerListener(this, new LoadedXMLRoomSelected(null));
		router.registerListener(this, new NextStepSequenceExperiment());
		router.registerListener(this, new SessionRoomSelected(null));
		
		/////////////// Should we do this?!!! ////////////////////
		testDungeon = new Dungeon(gc, 1, 13, 7, false);
		currentEditRoom = testDungeon.addRoom(13, 7);
		currentEditRoom.setTile(0, 0, TileTypes.WALL);
		currentEditRoom.setTile(1, 0, TileTypes.WALL);
		currentEditRoom.setTile(0, 1, TileTypes.WALL);
		currentEditRoom.setTile(2, 0, TileTypes.WALL);
		currentEditRoom.setTile(0, 2, TileTypes.WALL);
		currentEditRoom.setTile(1, 1, TileTypes.WALL);
		currentEditRoom.setTile(10, 0, TileTypes.WALL);
		currentEditRoom.setTile(10, 1, TileTypes.WALL);
		currentEditRoom.setTile(9, 0, TileTypes.WALL);
		currentEditRoom.setTile(8, 0, TileTypes.WALL);
		currentEditRoom.setTile(10, 2, TileTypes.WALL);
		currentEditRoom.setTile(9, 1, TileTypes.WALL);
		currentEditRoom.setTile(1, 2, TileTypes.ENEMY);
		currentEditRoom.setTile(1, 3, TileTypes.TREASURE);
		currentEditRoom.createDoor(new Point(0, 5));
		currentEditRoom.createDoor(new Point(5, 6));
		
		//Initialize the folder to search
		directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("my-data"));
        
        //Initiatilize the brush to paint the room
        myBrush = new Drawer();
		myBrush.AddmodifierComponent("Lock", new Modifier(lockBrush));
		myBrush.AddmodifierComponent("No-Rules", new Modifier(true, true));
		
		//Algorithm panes!
		evolutionPane = new EvolutionMAPEPane();
		router.postEvent(new RoomEditionStarted(currentEditRoom));
		
		//All the combinations I want
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});




		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});

		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});




		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});

		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});

		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});

		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});




		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});

		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});

		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});
		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5)});

		
//		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
//				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5),
//				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
//
//		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
//				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5),
//				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
//
//		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
//				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5),
//				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
//
//		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
//				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5),
//				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
//
//		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
//				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5),
//				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
//
//		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
//				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5),
//				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
//
//		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
//				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5),
//				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
//
//		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
//				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5),
//				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
//
//		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
//				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5),
//				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
//
//		possibleCombinations.add(new MAPEDimensionFXML[]{new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5),
//				new MAPEDimensionFXML(DimensionTypes.SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5),
//				new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5),
//				new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LENIENCY, 5),
//				new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5)});
	}
	
	/***
	 * Initialize the view! 
	 */
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
        
        brushesSide.setVisible(false);
        continueBtn.setDisable(true);
        evolutionPane.setVisible(true);
		evolutionPane.setActive(true);
		rightSidePane.getChildren().clear();
		rightSidePane.getChildren().add(evolutionPane);
		
		experimentTypeCombo.getItems().setAll(experimentType.values());	
		initializeView();

	}
	
	@Override
	public void ping(PCGEvent e) {
		
		//TODO: We are missing information here!!!!!!!
		if (e instanceof MapUpdate) {
		} 
//		else if(e instanceof SuggestionApplied)
//		{
//			replaceRoom((Room)e.getPayload());
//		}
		else if(e instanceof LoadedXMLRoomSelected)
		{
			xmlLoadedSelected((Room)e.getPayload());
		}
		else if(e instanceof NextStepSequenceExperiment)
		{
			if (runningExperiment) 
			{
				if(counter == 0)
				{
					currentCombination++;
					if(!useOurCombs || currentCombination >= possibleCombinations.size())
					{
						runningExperiment = false;
						return;
					}
					
					runExperiment();
					return;
				}
				
				Platform.runLater(() -> {

					experimentNextStep(currentEditRoom.getEditionSequence().get(index++));
					System.out.println("SAVE");
					router.postEvent(new SaveDisplayedCells());
				});
				counter--;
	        }
		}
		else if(e instanceof SessionRoomSelected)
		{
			sesRoomSelected((Room)e.getPayload());
		}
		else if(e instanceof SequencePreviewSelected)
		{
			sequenceStepSelected((Room)e.getPayload());
		}
		
	}
	
	public void onSaveChange()
	{
//		System.out.println(saveDataButton.isSelected());
		AlgorithmSetup.getInstance().setSaveData(saveDataButton.isSelected());
	}
	
	/**
	 * This method is to fill the Scrollbar to the left with the rooms that have been created! easier access 
	 */
	private void fillSessionList()
	{
		sessionCreations.getChildren().clear();
		for(int i = 0; i < sessionCreatedRooms.size(); i++)
		{
			RoomPreview<SessionRoomSelected> roomPreview = new RoomPreview<SessionRoomSelected>(sessionCreatedRooms.get(i), SessionRoomSelected.class);
			sessionCreations.getChildren().add(roomPreview.getRoomCanvas());
			
			double mapHeight = 15.0;
			double mapWidth = 15.0;
			
			mapHeight = (int)(10.0 * (float)((float)sessionCreatedRooms.get(i).getRowCount())); //Recalculate map size
			mapWidth = (int)(10.0 * (float)((float)sessionCreatedRooms.get(i).getColCount()));//Recalculate map size
			
//			StackPane.setMargin(centerPane, new Insets(8,8,8,8));
			
			roomPreview.getRoomCanvas().setMinSize(mapWidth, mapHeight);
			roomPreview.getRoomCanvas().setMaxSize(mapWidth, mapHeight);
			roomPreview.getRoomCanvas().setPrefSize(mapWidth, mapHeight);
			
			roomPreview.getRoomCanvas().draw(null);
			roomPreview.getRoomCanvas().setText("Waiting for map...");
//			counter++;
			
			Platform.runLater(() -> {
				roomPreview.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(roomPreview.getPreviewRoom(), -1));
			});
		}
	}
	
	/***
	 * Work in progress for when we select a room from the left side pane
	 * @param selectedSessionRoom
	 */
	private void sesRoomSelected(Room selectedSessionRoom)
	{
		//Remove first the room from the list
		sessionCreatedRooms.remove(selectedSessionRoom);
		
		if(!containRoom(currentEditRoom.specificID))
		{
			Room editedRoom = new Room(currentEditRoom);
			editedRoom.addEditions(currentEditRoom.getEditionSequence());
			editedRoom.indexEditionStep = currentEditRoom.indexEditionStep;
			editedRoom.specificID = currentEditRoom.specificID;
			sessionCreatedRooms.add(editedRoom);
		}
		
		testDungeon.removeRoom(currentEditRoom);
		currentEditRoom = selectedSessionRoom;
		testDungeon.addRoom(currentEditRoom);
		initRoomInteractiveView();
		evaluateRoom();
		fillRoomSequence();
		
		//We are not browsing editions anymore, and we can render the session rooms
		browsingEditions = false;
		fillSessionList();
		
		Platform.runLater(() -> 
		{
			editButton.setSelected(true);
			internallyChangedEdit();
		});
	}
	
	/***
	 * We are now just browsing around the sequence of editions
	 * editions are not allowed (unless you want to start fresh)
	 * @param selectedXMLRoom : select room
	 ***/
	private void xmlLoadedSelected(Room selectedXMLRoom)
	{
		editButton.setDisable(true);
		continueBtn.setStyle("-fx-background-color: green; -fx-text-fill: white;");
		continueBtn.setDisable(false);
		
		//first of all we create a copy of whatever room we were creating
		//and add it to the list of current rooms
		if(!browsingEditions && !containRoom(currentEditRoom.specificID) && currentEditRoom.getEditionSequence().size() > 1)
		{
			Room editedRoom = new Room(currentEditRoom);
			editedRoom.addEditions(currentEditRoom.getEditionSequence());
			editedRoom.indexEditionStep = currentEditRoom.indexEditionStep;
			editedRoom.specificID = currentEditRoom.specificID;
			sessionCreatedRooms.add(editedRoom);
			
			
			browsingEditions = true;
		}
		
		//Then we do the basic stuff, just remove the room from the dungeon,
		//assign the selected edition, render, and evaluate.
		testDungeon.removeRoom(currentEditRoom);
		currentEditRoom = selectedXMLRoom;
		testDungeon.addRoom(currentEditRoom);
		
		if(!containRoom(currentEditRoom.specificID))
		{
			Room editedRoom = new Room(currentEditRoom);
			editedRoom.addEditions(currentEditRoom.getEditionSequence());
			editedRoom.indexEditionStep = currentEditRoom.indexEditionStep;
			editedRoom.specificID = currentEditRoom.specificID;
			sessionCreatedRooms.add(editedRoom);
		}

		fillSessionList();
		initRoomInteractiveView();
		evaluateRoom();
		fillRoomSequence();
		
		Platform.runLater(() -> 
		{
			editButton.setSelected(true);
			internallyChangedEdit();
		});
	}
	
	/***
	 * We are now just browsing around the sequence of editions
	 * editions are not allowed (unless you want to start fresh)
	 * @param selectedSequence : select room
	 ***/
	private void sequenceStepSelected(Room selectedSequence)
	{
		//first of all we create a copy of whatever room we were creating
		//and add it to the list of current rooms
		if(!browsingEditions && !containRoom(currentEditRoom.specificID))
		{
			Room editedRoom = new Room(currentEditRoom);
			editedRoom.addEditions(currentEditRoom.getEditionSequence());
			editedRoom.indexEditionStep = currentEditRoom.indexEditionStep;
			editedRoom.specificID = currentEditRoom.specificID;
			sessionCreatedRooms.add(editedRoom);
			
			continueBtn.setDisable(false);
			browsingEditions = true;
		}
		
		//Then we do the basic stuff, just remove the room from the dungeon,
		//assign the selected edition, render, and evaluate.
		testDungeon.removeRoom(currentEditRoom);
		currentEditRoom = selectedSequence;
		testDungeon.addRoom(currentEditRoom);
		getRoomInteractiveView().updateMap(currentEditRoom); //render
		evaluateRoom();
		
		Platform.runLater(() -> 
		{
			editButton.setSelected(false);
			internallyChangedEdit();
		});
	}
	
	/**
	 * We want to edit a specific step of the sequence
	 * Very specific method that is called
	 */
	private void editSequenceStep()
	{
		//Since we want to start from this point onwards (a new room and sequence of steps)
		//We collect the sequence and cut it exactly with the new room edit index! 
		LinkedList<Room> sequence = new LinkedList<Room>();
		int finalEditStep = currentEditRoom.indexEditionStep;
		for(int i = 0; i< finalEditStep; i++)
		{
			sequence.add(currentEditRoom.getEditionSequence().get(i));
		}
		
		//We create a copy of the room we are visualizing, add the editions
		//to its index point, and init every as usual for changes! 
		testDungeon.removeRoom(currentEditRoom);
		currentEditRoom = new Room(currentEditRoom);
		currentEditRoom.addEditions(sequence);
		currentEditRoom.indexEditionStep = finalEditStep;
		currentEditRoom.addEdition();
		currentEditRoom.indexEditionStep++;
		
		//We are not browsing editions anymore, and we can render the session rooms
		browsingEditions = false;
		fillSessionList();
		
		//Initialize everything! 
		testDungeon.addRoom(currentEditRoom);
		getRoomInteractiveView().updateMap(currentEditRoom); 		
		evaluateRoom();
		
		//Inform the generation algorithms that there is a new room in town! 
		//and fill the sequence view! 
		router.postEvent(new RoomEditionStarted(currentEditRoom));
		fillRoomSequence();
		
		//This is needed to have events in the new pane!! 
		Platform.runLater(() -> 
		{
			editButton.setSelected(true);
			internallyChangedEdit();
		});
	}
	
	/**
	 * Helper method to know if we have a room in the list  based on the UUID
	 * since the room is a copy (i.e. not the same object)
	 * @param id : ID of the room we want to test
	 * @return
	 */
	private boolean containRoom(UUID id)
	{
		for(Room r : sessionCreatedRooms)
		{
			if(r.specificID.toString().equals(id.toString()))
				return true;
		}
		
		return false;
	}
	
	/**
	 * This method is called when we press "RECORD" 
	 * thus, we start "recording" a new room starting from that point.
	 */
	private void recordNewSequence()
	{
		//Save the current room into the session list and render the list
		if(!containRoom(currentEditRoom.specificID))
		{
			Room editedRoom = new Room(currentEditRoom);
			editedRoom.addEditions(currentEditRoom.getEditionSequence());
			editedRoom.indexEditionStep = currentEditRoom.indexEditionStep;
			editedRoom.specificID = currentEditRoom.specificID;
			sessionCreatedRooms.add(editedRoom);
		}
		

		
		//Since we want to start from this point recording, we just need to copy the current room
		// and forget about the sequence, and init every as usual for changes! 
		testDungeon.removeRoom(currentEditRoom);
		currentEditRoom = new Room(currentEditRoom);
		currentEditRoom.indexEditionStep = 0;
		currentEditRoom.addEdition();
		currentEditRoom.indexEditionStep++;
		
		browsingEditions = false;
		fillSessionList();
		
		//Initialize everything! 
		testDungeon.addRoom(currentEditRoom);
		getRoomInteractiveView().updateMap(currentEditRoom); 
		evaluateRoom();
		
		//Inform the generation algorithms that there is a new room in town! 
		//and fill the sequence view! 
		router.postEvent(new RoomEditionStarted(currentEditRoom));
		fillRoomSequence();
		
		
		//This is needed to have events in the new pane!! 
		Platform.runLater(() -> 
		{
			editButton.setSelected(true);
			internallyChangedEdit();
		});
	}
	
	/**
	 * Should only be called when we are initializing the view!!!!
	 * @param selectedRoom
	 */
	private void initializeView()
	{
		initRoomInteractiveView();
		evaluateRoom();
		
		router.postEvent(new RoomEditionStarted(currentEditRoom));
		fillRoomSequence();
	}

	/***
	 * Method called when we create a new room
	 * @param newRoom
	 */
	private void createNewRoom(Room newRoom)
	{
		//Save the current room into the session list and render the list
		if(!containRoom(currentEditRoom.specificID) && currentEditRoom.getEditionSequence().size() > 1)
		{
			Room editedRoom = new Room(currentEditRoom);
			editedRoom.addEditions(currentEditRoom.getEditionSequence());
			editedRoom.indexEditionStep = currentEditRoom.indexEditionStep;
			editedRoom.specificID = currentEditRoom.specificID;
			sessionCreatedRooms.add(editedRoom);
		}
		
		//Since we want to start from this point recording, we just need to copy the current room
		// and forget about the sequence, and init every as usual for changes! 
		testDungeon.removeRoom(currentEditRoom);
		currentEditRoom = new Room(currentEditRoom);
		currentEditRoom.indexEditionStep = 0;
		currentEditRoom.addEdition();
		currentEditRoom.indexEditionStep++;
		
		browsingEditions = false;
		fillSessionList();
		
		//Initialize everything! 
		testDungeon.addRoom(currentEditRoom);
		getRoomInteractiveView().updateMap(currentEditRoom); 
		evaluateRoom();
		
		//Inform the generation algorithms that there is a new room in town! 
		//and fill the sequence view! 
		router.postEvent(new RoomEditionStarted(currentEditRoom));
		fillRoomSequence();
		
		//This is needed to have events in the new pane!! 
		Platform.runLater(() -> 
		{
			editButton.setSelected(true);
			internallyChangedEdit();
		});
		testDungeon.removeRoom(currentEditRoom);
		currentEditRoom = newRoom;
		testDungeon.addRoom(currentEditRoom);
		getRoomInteractiveView().updateMap(currentEditRoom); 
		evaluateRoom();
		
		router.postEvent(new RoomEditionStarted(currentEditRoom));
		fillRoomSequence();
	}
	
	/**
	 * following steps in the experiment
	 * @param nextSeq
	 */
	private void experimentNextStep(Room nextSeq)
	{
		testDungeon.removeRoom(currentEditRoom);
		currentEditRoom = nextSeq;
		testDungeon.addRoom(currentEditRoom);
		initRoomInteractiveView();
		evaluateRoom();
		
		saveExperimentRoom();
		
		router.postEvent(new RoomEdited(currentEditRoom));
	}
	
	/**
	 * Initiatilize the experiment when the method is called
	 * @param startingEdition
	 */
	private void initializeExperiment(Room startingEdition)
	{
		Platform.runLater(() -> {

			testDungeon.removeRoom(currentEditRoom);
			currentEditRoom = startingEdition;
			testDungeon.addRoom(currentEditRoom);
			initRoomInteractiveView();
			evaluateRoom();
			
			saveExperimentRoom(0);
//			saveEvaluation();
			
			router.postEvent(new RoomEditionStarted(currentEditRoom));
	//		fillRoomSequence();
		});
	}
	
	/**
	 * Fill the center hbox with the sequences of this room!
	 */
	private void fillRoomSequence()
	{
		double mapHeight = (int)(10.0 * (float)((float)currentEditRoom.getRowCount())); //Recalculate map size
		double mapWidth = (int)(10.0 * (float)((float)currentEditRoom.getColCount()));//Recalculate map size
		LinkedList<Room> sequence = currentEditRoom.getEditionSequence();
		editedRoomSteps.getChildren().clear();
		for(int i = 0; i < sequence.size(); i++)
		{
			RoomPreview<SequencePreviewSelected> roomPreview = new RoomPreview<SequencePreviewSelected>(sequence.get(i), SequencePreviewSelected.class);
			editedRoomSteps.getChildren().add(roomPreview.getRoomCanvas());

			roomPreview.getRoomCanvas().setMinSize(mapWidth, mapHeight);
			roomPreview.getRoomCanvas().setMaxSize(mapWidth, mapHeight);
			roomPreview.getRoomCanvas().setPrefSize(mapWidth, mapHeight);
			
			roomPreview.getRoomCanvas().draw(null);
			roomPreview.getRoomCanvas().setText("Waiting for map...");
//			counter++;
			
			Platform.runLater(() -> {
				roomPreview.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(roomPreview.getPreviewRoom(), -1));
			});
		}
	}


	
	private void RenderSpecificBatch()
	{
		int from = currentLoadedRoomsPage * roomsPerPage;
		int to = Math.min(from + roomsPerPage, loadedRooms.size());
		
		fileLister.getChildren().clear();
		for(int i = from; i < to; i++)
		{
			RoomPreview<LoadedXMLRoomSelected> roomPreview = new RoomPreview<LoadedXMLRoomSelected>(loadedRooms.get(i), LoadedXMLRoomSelected.class);
			fileLister.getChildren().add(roomPreview.getRoomCanvas());
			
			double mapHeight = 15.0;
			double mapWidth = 15.0;
			
			mapHeight = (int)(10.0 * (float)((float)loadedRooms.get(i).getRowCount())); //Recalculate map size
			mapWidth = (int)(10.0 * (float)((float)loadedRooms.get(i).getColCount()));//Recalculate map size
			
//			StackPane.setMargin(centerPane, new Insets(8,8,8,8));
			
			roomPreview.getRoomCanvas().setMinSize(mapWidth, mapHeight);
			roomPreview.getRoomCanvas().setMaxSize(mapWidth, mapHeight);
			roomPreview.getRoomCanvas().setPrefSize(mapWidth, mapHeight);
			
			roomPreview.getRoomCanvas().draw(null);
			roomPreview.getRoomCanvas().setText("Waiting for map...");
//			counter++;
			
			Platform.runLater(() -> {
				roomPreview.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(roomPreview.getPreviewRoom(), -1));
			});
		}
	}
	
	
	private void evaluateRoom()
	{
		currentEditRoom.calculateAllDimensionalValues();
		fitnessText.setText(df2.format(currentEditRoom.fitnessEvaluation()).toString());
		NMesoText.setText(df2.format(currentEditRoom.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN)).toString());
		NMicroText.setText(df2.format(currentEditRoom.getDimensionValue(DimensionTypes.NUMBER_PATTERNS)).toString());
		lenText.setText(df2.format(currentEditRoom.getDimensionValue(DimensionTypes.LENIENCY)).toString());
		linText.setText(df2.format(currentEditRoom.getDimensionValue(DimensionTypes.LINEARITY)).toString());
		symText.setText(df2.format(currentEditRoom.getDimensionValue(DimensionTypes.SYMMETRY)).toString());
	}

	/**
	 * Initialises the map view and creates canvases for pattern drawing and
	 * infeasibility notifications.
	 */
	private void initRoomInteractiveView() {
		
		Platform.runLater(() -> 
		{
			if(getRoomInteractiveView() != null)
			{
				getRoomInteractiveView().destructor();
			}
			
			roomPane.getChildren().clear();
	
			setRoomInteractiveView(new InteractiveMap());
			StackPane.setAlignment(getRoomInteractiveView(), Pos.CENTER);
			getRoomInteractiveView().setMinSize(300, 300);
			getRoomInteractiveView().setMaxSize(300, 300);
			getRoomInteractiveView().setPrefSize(300, 300);
			roomPane.getChildren().add(getRoomInteractiveView());
	
			//Needed calculation
			double width = 300.0 / currentEditRoom.getColCount();
			double height = 300.0 / currentEditRoom.getRowCount();
			double scale = Math.min(width, height);
			
			brushCanvas = new Canvas(Math.min(300, scale * currentEditRoom.getColCount()), 
					Math.min(300, scale * currentEditRoom.getRowCount()));
	
			StackPane.setAlignment(brushCanvas, Pos.CENTER);
			roomPane.getChildren().add(brushCanvas);
			brushCanvas.setVisible(true);
			brushCanvas.setMouseTransparent(true);
			brushCanvas.setOpacity(1.0f);
			
			getRoomInteractiveView().updateMap(currentEditRoom); 
			
		});
	}
	
	/**
	 * Used to test an update before applying it
	 * @param tile
	 * @return
	 */
	public boolean checkInfeasibleLockedRoom(ImageView tile)
	{
		Room auxRoom = new Room(roomInteractiveView.getMap());
		roomInteractiveView.updateTileInARoom(auxRoom, tile, myBrush);
		
		if(!auxRoom.walkableSectionsReachable())
		{
			System.out.println("I DETECTED IT!!");
			InformativePopupManager.getInstance().requestPopup(roomInteractiveView, PresentableInformation.ROOM_INFEASIBLE_LOCK, "");
			return true;
		}
		
		return false;
	}
	
	/////////////// START BUTTONS /////////////
	@FXML
	public void onSaveSequence()
	{
		if(currentEditRoom != null)
		{
			XMLHandler.getInstance().saveIndividualRoomSequence(currentEditRoom, sequenceSaveFilename.getText());
		}

	}

	/**
	 * When we press to select folder, the selected folder 
	 * is used to load xmls into EDD rooms!
	 * OBS!!! We only render specfic batches
	 */
	@FXML
	public void onPressedSelectFolder()
	{
		selectedDirectory = directoryChooser.showDialog(roomPane.getScene().getWindow());
//        System.out.println(selectedDirectory.getAbsolutePath());
//        File[] fileList = selectedDirectory.listFiles(new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String name) {
//                return name.endsWith(".xml");
//            }
//        });
//        
        if(selectedDirectory == null)
        	return;
        
//        selectedDirectory.listFiles(new Acc)
        fileLister.getChildren().clear();
//        
//        for(File file : fileList)
//        {
//        	fileLister.getChildren().add(new Text(file.getName()));
//        }
//        
        XMLHandler.getInstance().clearLoaded();
		XMLHandler.getInstance().loadRooms(selectedDirectory.getAbsolutePath(), false);
		XMLHandler.getInstance().sortRoomsToLoad();
		XMLHandler.getInstance().createRooms();
		
		loadedRooms = new LinkedList<Room>();
		loadedRooms = XMLHandler.getInstance().roomsInFile;
		
		//First calculate how many pages will be created
		maxLoadedRoomsPages = loadedRooms.size()/roomsPerPage;
		currentLoadedRoomsPage = 0;
		
		//check if we have actually any file
		if(!loadedRooms.isEmpty())
		{
			saveAllBtn.setDisable(false);
		}
		
		RenderSpecificBatch();	
	}
	
	/**
	 * Method called when toggling the different brushes in the left pane
	 */
	@FXML
	public void onExperimentTypeChanged()
	{
		
		System.out.println(experimentTypeCombo.getValue());
		System.out.println(experimentType);
		experimentType = experimentTypeCombo.getValue();
		System.out.println(experimentType);
//		System.out.println(experimentToggle.getSelectedToggle());
//		System.out.println(((RadioButton)experimentToggle.getSelectedToggle()).getText());
	}

	/**
	 * Method called when toggling the different brushes in the left pane
	 */
	@FXML
	public void selectBrush()
	{
		if (brushes.getSelectedToggle() == null) 
		{
			roomInteractiveView.setCursor(Cursor.DEFAULT);  
			myBrush.SetMainComponent(new NullTile());
			
		} else {
			roomInteractiveView.setCursor(Cursor.HAND);
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
			case "Door":
				myBrush.SetMainComponent(new DoorTile());
				break;
			case "Enemy":
				myBrush.SetMainComponent(new EnemyTile());
				break;		
			case "Boss":
				myBrush.SetMainComponent(new BossEnemyTile());
				break;
			}
		}
	}
	
	/**
	 * Add locks!
	 */
	@FXML
	public void selectLockModifier()
	{
		myBrush.ChangeModifierMainValue("Lock", lockBrush.isSelected());
		lockButton.setSelected(lockBrush.isSelected());
		toggleLocks();
		lockButton.setDisable(lockBrush.isSelected());
	}
	
	/**
	 * TODO: Not implemented
	 */
	@FXML
	public void togglePatterns()
	{
		System.out.println("LOCK");
	}
	
	/**
	 * TODO: Not implemented
	 */
	@FXML
	public void toggleLocks()
	{
		System.out.println("LOCK");
	}
	
	/**
	 * Renders the previous page of loaded rooms
	 */
	@FXML
	public void prevLoadedRooms()
	{
		if(currentLoadedRoomsPage != 0)
		{
			currentLoadedRoomsPage--;
			RenderSpecificBatch();
		}
	}
	
	/**
	 * Renders the next page of loaded rooms
	 */
	@FXML
	public void nextLoadedRooms()
	{
		if(currentLoadedRoomsPage + 1 <= maxLoadedRoomsPages)
		{
			currentLoadedRoomsPage++;
			RenderSpecificBatch();
		}
	}
	
	/**
	 * RUN the experiment! usually it will be to run the algorithm but can be in seconds to visualize
	 * the editions!
	 */
	@FXML
	public void onPressedRunExperiment()
	{
		if(runningExperiment)
			return;
		
		currentCombination = 0;
		runExperiment();
	}
	
	private void runExperiment()
	{
		if(stepsTF.getText().toLowerCase().equals("all"))
		{
			counter = currentEditRoom.getEditionSequence().size();
		}
		else
		{
			counter = Integer.parseInt(stepsTF.getText());
		}
		
		index = Integer.parseInt(fromTF.getText());
		counter = counter - index;
		
//		index = 0;
//		counter = 50;
//		
        counter--;
        runningExperiment = true;
        
        
        if(experimentType == SequenceExperiment.REPEAT) //THIS is using timer
        {
    		final Timer tt = new Timer();
    		
    		tt.schedule(new TimerTask(){
    		    public void run() {
    		    	
    		    	if (counter == 0) {
    		        	runningExperiment = false;
    		            tt.cancel();
    		        }
    		    	
    		        //your job- Should be changed to a specific method.
//    		    	initializeExperiment(currentEditRoom.getEditionSequence().get(index)); //This for not changing
    		    	initializeExperiment(currentEditRoom.getEditionSequence().get(index++));
    		        counter--;
    		        
    		    }
    		}, 500l, Long.parseLong(secondsTF.getText()));
        }
        else if(experimentType == SequenceExperiment.OBJECTIVE) //THE ACTUAL EXPERIMENT!
        {
        	initializeExperiment(currentEditRoom.getEditionSequence().get(index++));
//	    	initializeExperiment(currentEditRoom.getEditionSequence().get(index)); //This for not changing
        	Platform.runLater(() -> {
        		if(useOurCombs)
            	{
//            		router.postEvent(new RestartDimensionsExperiment(possibleCombinations.get(currentCombination)));
            		router.postEvent(new StartMapMutate(currentEditRoom, MapMutationType.Preserving, AlgorithmTypes.Native, 1, false));
            	}
			});     	
        }
        else if(experimentType == SequenceExperiment.MAPELITES) //THE ACTUAL EXPERIMENT!
        {
        	initializeExperiment(currentEditRoom.getEditionSequence().get(index++));
//	    	initializeExperiment(currentEditRoom.getEditionSequence().get(index)); //This for not changing
        	Platform.runLater(() -> {
        		if(useOurCombs)
            	{
            		router.postEvent(new RestartDimensionsExperiment(possibleCombinations.get(currentCombination)));
//            		router.postEvent(new StartMapMutate(currentEditRoom, MapMutationType.Preserving, AlgorithmTypes.Native, 1, false));
            	}
			});     	
        }
 
	}

	/**
	 * Start recording a new room from the specific point!
	 */
	@FXML
	public void onPressedRecord()
	{
		recordNewSequence();
	}
	
	/**
	 * When pressed continue we can continue editing the room
	 */
	@FXML
	public void onPressedContinue()
	{
		editButton.setDisable(false);
		continueBtn.setStyle("");
		continueBtn.setDisable(true);	
		sesRoomSelected(sessionCreatedRooms.get(sessionCreatedRooms.size() - 1));
		
	}
	
	@FXML
	public void onPressedEdit()
	{
		if(editButton.isSelected())
		{
			brushesSide.setVisible(true);
			myBrush.ChangeModifierMainValue("No-Rules", true);
			selectBrush();
			
			roomInteractiveView.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEdit);
			roomInteractiveView.addEventFilter(MouseEvent.MOUSE_MOVED, mouseHover);
			
			//EXTRA!
			editSequenceStep();
		}
		else
		{
			brushesSide.setVisible(false);
			roomInteractiveView.removeEventFilter(MouseEvent.MOUSE_CLICKED, mouseEdit);
			roomInteractiveView.removeEventFilter(MouseEvent.MOUSE_MOVED, mouseHover);
		}
	}
	
	private void internallyChangedEdit()
	{
		if(editButton.isSelected())
		{
			brushesSide.setVisible(true);
			myBrush.ChangeModifierMainValue("No-Rules", true);
			selectBrush();
			
			roomInteractiveView.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEdit);
			roomInteractiveView.addEventFilter(MouseEvent.MOUSE_MOVED, mouseHover);
			
		}
		else
		{
			brushesSide.setVisible(false);
			roomInteractiveView.removeEventFilter(MouseEvent.MOUSE_CLICKED, mouseEdit);
			roomInteractiveView.removeEventFilter(MouseEvent.MOUSE_MOVED, mouseHover);
		}
	}
	
	@FXML
	public void onPressedNew()
	{
		createNewRoom(new Room(null, gc, 7, 13, 30));
		
		Platform.runLater(() -> 
		{
			editButton.setSelected(true);
			internallyChangedEdit();
		});
	}
	
	@FXML
	public void saveEvaluationAllLoaded()
	{
		String DIRECTORY= selectedDirectory.getAbsolutePath();
		File file = new File(DIRECTORY + "\\evaluation.csv");
		StringBuilder data = new StringBuilder();
		
		if(!file.exists())
		{
			data.append("filename;Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;"
					+ "Wall Count;Wall Density;Wall Sparsity;"
					+ "Enemy Count;Enemy Density;Enemy Sparsity;"
					+ "Treasure Count;Treasure Density;Treasure Sparsity" + System.lineSeparator());
		}
		
		for(Room loadedRoom : loadedRooms)
		{
			testDungeon.addRoom(loadedRoom);
			loadedRoom.calculateAllDimensionalValues();

			data.append(loadedRoom.specificID.toString() + ";");
			data.append(df2.format(loadedRoom.getDimensionValue(DimensionTypes.LENIENCY)).toString() + ";");
			data.append(df2.format(loadedRoom.getDimensionValue(DimensionTypes.LINEARITY)).toString() + ";");
			data.append(1.0 + ";");
			data.append(df2.format(loadedRoom.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN)).toString() + ";");
			data.append(df2.format(loadedRoom.getDimensionValue(DimensionTypes.NUMBER_PATTERNS)).toString() + ";");
			data.append(df2.format(loadedRoom.getDimensionValue(DimensionTypes.SYMMETRY)).toString() + ";");
			data.append(1.0 + ";");
			data.append(df2.format(loadedRoom.fitnessEvaluation()).toString() + ";");
			
			data.append(df2.format(loadedRoom.getWallCount()) + ";");
			data.append(df2.format(loadedRoom.calculateWallDensity()) + ";");
			data.append(df2.format(loadedRoom.calculateWallSparsity()) + ";");
			
			data.append(df2.format(loadedRoom.getEnemyCount()) + ";");
			data.append(df2.format(loadedRoom.calculateEnemyDensity())+ ";");
			data.append(df2.format(loadedRoom.calculateEnemySparsity()) + ";");
			
			data.append(df2.format(loadedRoom.getTreasureCount()) + ";");
			data.append(df2.format(loadedRoom.calculateTreasureDensity()) + ";");
			data.append(df2.format(loadedRoom.calculateTreasureSparsity()) + System.lineSeparator());
			testDungeon.removeRoom(loadedRoom);
		}
		

		try {
			FileUtils.write(file, data, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		saveAllBtn.setDisable(true);
	}
	
	@FXML
	public void saveEvaluation()
	{
		String DIRECTORY= System.getProperty("user.dir") + "\\my-data\\" + evaluationFilename.getText();
		File file = new File(DIRECTORY);
		StringBuilder data = new StringBuilder();
		
		if(!file.exists())
		{
			data.append("filename;Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;"
					+ "Wall Count;Wall Density;Wall Sparsity;"
					+ "Enemy Count;Enemy Density;Enemy Sparsity;"
					+ "Treasure Count;Treasure Density;Treasure Sparsity" + System.lineSeparator());
		}
		
		data.append(currentEditRoom.specificID.toString() + ";");
		data.append(lenText.getText() + ";");
		data.append(linText.getText() + ";");
		data.append(1.0 + ";");
		data.append(NMesoText.getText() + ";");
		data.append(NMicroText.getText() + ";");
		data.append(symText.getText() + ";");
		data.append(1.0 + ";");
		data.append(fitnessText.getText() + ";");
		
		data.append(df2.format(currentEditRoom.getWallCount()) + ";");
		data.append(df2.format(currentEditRoom.calculateWallDensity()) + ";");
		data.append(df2.format(currentEditRoom.calculateWallSparsity()) + ";");
		
		data.append(df2.format(currentEditRoom.getEnemyCount()) + ";");
		data.append(df2.format(currentEditRoom.calculateEnemyDensity())+ ";");
		data.append(df2.format(currentEditRoom.calculateEnemySparsity()) + ";");
		
		data.append(df2.format(currentEditRoom.getTreasureCount()) + ";");
		data.append(df2.format(currentEditRoom.calculateTreasureDensity()) + ";");
		data.append(df2.format(currentEditRoom.calculateTreasureSparsity()) + System.lineSeparator());


		try {
			FileUtils.write(file, data, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/////////////// END BUTTONS /////////////

	private void saveExperimentRoom(int ident)
	{
		String DIRECTORY= System.getProperty("user.dir") + "\\my-data\\" + experimentFilename.getText();
//		String DIRECTORY= experimentFilename.getText();
		File file = new File(DIRECTORY);
		StringBuilder data = new StringBuilder();

//		if(!file.exists())
//		{
//			data.append("filename;Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;"
//					+ "Type" + System.lineSeparator());
//		}
//
		if(!file.exists())
		{
			data.append("identifier;filename;Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;"
					+ "Wall Count;Wall Density;Wall Sparsity;"
					+ "Enemy Count;Enemy Density;Enemy Sparsity;"
					+ "Treasure Count;Treasure Density;Treasure Sparsity;"
					+ "Floor Count;Floor Density;Floor Sparsity;Type" + System.lineSeparator());
		}

		data.append(ident + ";");
		data.append(currentEditRoom.specificID.toString() + ";");
		data.append(lenText.getText() + ";");
		data.append(linText.getText() + ";");
		data.append(1.0 + ";");
		data.append(NMesoText.getText() + ";");
		data.append(NMicroText.getText() + ";");
		data.append(symText.getText() + ";");
		data.append(1.0 + ";");
		data.append(fitnessText.getText() + ";");

		data.append(df2.format(currentEditRoom.getWallCount()) + ";");
		data.append(df2.format(currentEditRoom.calculateWallDensity()) + ";");
		data.append(df2.format(currentEditRoom.calculateWallSparsity()) + ";");

		data.append(df2.format(currentEditRoom.getEnemyCount()) + ";");
		data.append(df2.format(currentEditRoom.calculateEnemyDensity())+ ";");
		data.append(df2.format(currentEditRoom.calculateEnemySparsity()) + ";");

		data.append(df2.format(currentEditRoom.getTreasureCount()) + ";");
		data.append(df2.format(currentEditRoom.calculateTreasureDensity()) + ";");
		data.append(df2.format(currentEditRoom.calculateTreasureSparsity()) + ";");

		data.append(df2.format(currentEditRoom.floorCount()) + ";");
		data.append(df2.format(currentEditRoom.calculateFloorDensity()) + ";");
		data.append(df2.format(currentEditRoom.calculateFloorSparsity()) + ";");

		data.append("ER" + System.lineSeparator());


		try {
			FileUtils.write(file, data, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void saveExperimentRoom()
	{
		String DIRECTORY= System.getProperty("user.dir") + "\\my-data\\" + experimentFilename.getText();
		File file = new File(DIRECTORY);
		StringBuilder data = new StringBuilder();
		
		if(!file.exists())
		{
			data.append("filename;Leniency;Linearity;Similarity;NMesoPatterns;NSpatialPatterns;Symmetry;Inner Similarity;Fitness;"
					+ "Score;DIM X;DIM Y;STEP;Gen;Type" + System.lineSeparator());
		}
		
		data.append(currentEditRoom.specificID.toString() + ";");
		data.append(lenText.getText() + ";");
		data.append(linText.getText() + ";");
		data.append(1.0 + ";");
		data.append(NMesoText.getText() + ";");
		data.append(NMicroText.getText() + ";");
		data.append(symText.getText() + ";");
		data.append(1.0 + ";");
		data.append(fitnessText.getText() + ";");
		data.append("1.0;");
		data.append(possibleCombinations.get(currentCombination)[0].getDimension() + ";");
	    data.append(possibleCombinations.get(currentCombination)[1].getDimension() + ";");
	    data.append(index - 1 + ";");
	    data.append(0 + ";");
		data.append("ER" + System.lineSeparator());

		;
		try {
			FileUtils.write(file, data, true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public InteractiveMap getRoomInteractiveView() {
		return roomInteractiveView;
	}

	public void setRoomInteractiveView(InteractiveMap mapView) {
		this.roomInteractiveView = mapView;
	}
	
	////////////////////// MOUSE HANDLERS! ///////////
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
				
				// I AM MISSING HERE TO ADD TO THTE SEQUENCE!!!!!! 
				
				roomInteractiveView.updateTile(tile, myBrush);
				roomInteractiveView.getMap().forceReevaluation();
				evaluateRoom();
				roomInteractiveView.getMap().getRoomXML("room\\");
//				mapIsFeasible(mapView.getMap().isIntraFeasible());
//				redrawPatterns(mapView.getMap());
//				redrawLocks(mapView.getMap());
				
				roomInteractiveView.getMap().addEdition();
				roomInteractiveView.getMap().indexEditionStep++;
				
				for(Room edition : roomInteractiveView.getMap().getEditionSequence())
				{
					edition.clearEditionSequence();
					edition.addEditions(roomInteractiveView.getMap().getEditionSequence());
				}
//				mapView.getMap().getEditionSequence().get(
//						mapView.getMap().getEditionSequence().size() - 1).addEditions(mapView.getMap().getEditionSequence());
				
				fillRoomSequence();
				
				
				//TODO: UNCOMMENT TO SAVE EACH STEP!!
//				saveEditedRoomInfo();
				
//				System.out.println(mapView.getMap().fitnessEvaluation());
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
				brushCanvas.getGraphicsContext2D().clearRect(0, 0, 300, 300);
				brushCanvas.setVisible(true);
				util.Point p = roomInteractiveView.CheckTile(tile);
				myBrush.Update(event, p, roomInteractiveView.getMap());
				
				renderer.drawBrush(brushCanvas.getGraphicsContext2D(), roomInteractiveView.getMap().toMatrix(), myBrush, 
						myBrush.possibleToDraw() ? Color.WHITE : Color.RED);
			}
		}
		
	}
	
	
}
