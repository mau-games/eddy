package gui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

import collectors.ActionLogger;
import collectors.DataSaverLoader;
import game.Dungeon;
import game.Game;
import game.Room;
import game.RoomEdge;
import game.MapContainer;
import generator.config.GeneratorConfig;
import gui.utils.InformativePopupManager;
import gui.views.LaunchViewController;
import gui.views.SandBoxViewController;
import gui.views.SuggestionsViewController;
import gui.views.WorldViewController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.ImageCursor;

import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.ChangeCursor;
import util.eventrouting.events.InitialRoom;
import util.eventrouting.events.MapLoaded;
import util.eventrouting.events.RequestConnection;
import util.eventrouting.events.RequestConnectionRemoval;
import util.eventrouting.events.RequestEmptyRoom;
import util.eventrouting.events.RequestNewRoom;
import util.eventrouting.events.RequestPathFinding;
import util.eventrouting.events.RequestRoomRemoval;
import util.eventrouting.events.RequestRedraw;
import util.eventrouting.events.RequestRoomView;
import util.eventrouting.events.RequestSuggestionsView;
import util.eventrouting.events.RequestWorldView;
import util.eventrouting.events.StartWorld;
import util.eventrouting.events.StatusMessage;
import util.eventrouting.events.Stop;


/**
 * This class is the intermediary between all the different windows. It is essentially a copy of the interactiveGUIController class
 * without some extra unnecesary things. The only difference is that this class will handle the movement of unstable views as well
 * This class and this GUI is only to be used to test different algorithms or arrangements in a non-final way. It was created
 * because of the need for saving arbitrary rooms/dungeons and visualization of different techniques.
 * 
 * Soon there will be a GUI that will be the same as the "develop" GUI but with experimental additions --> Basically a PTR (Public test region)
 * 
 * @author Alberto Alvarez, Malmo University
 *
 */
public class UnstablePlaygroundGUIController implements Initializable, Listener {

	@FXML private AnchorPane mainPane;
	@FXML private MenuItem newItem;
//	@FXML private MenuItem openItem;
//	@FXML private MenuItem saveItem;
//	@FXML private MenuItem saveAsItem;
//	@FXML private MenuItem exportItem;
//	@FXML private MenuItem prefsItem;
//	@FXML private MenuItem exitItem;
//	@FXML private MenuItem aboutItem;
	Stage stage = null;

	SuggestionsViewController suggestionsView = null;
	SandBoxViewController sandboxView = null;
	WorldViewController worldView = null;
	LaunchViewController launchView = null;
	EventHandler<MouseEvent> mouseEventHandler = null;

//	final static Logger logger = LoggerFactory.getLogger(InteractiveGUIController.class);
	private static EventRouter router = EventRouter.getInstance();
	
	//NEW
	private Dungeon dungeonMap = new Dungeon();
	
	public static UUID runID;
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		router.registerListener(this, new ChangeCursor(null));
		router.registerListener(this, new InitialRoom(null, null));
		router.registerListener(this, new RequestPathFinding(null, -1, null, null, null, null));
		router.registerListener(this, new RequestConnection(null, -1, null, null, null, null));
		router.registerListener(this, new RequestNewRoom(null, -1, -1, -1));
		router.registerListener(this, new StatusMessage(null));
		router.registerListener(this, new AlgorithmDone(null, null, null));
		router.registerListener(this, new RequestRedraw());
		router.registerListener(this, new RequestRoomView(null, 0, 0, null));
		router.registerListener(this, new MapLoaded(null));
		router.registerListener(this, new RequestWorldView());
		router.registerListener(this, new RequestEmptyRoom(null, 0, 0, null));
		router.registerListener(this, new RequestSuggestionsView(null, 0));
		router.registerListener(this, new Stop());
		router.registerListener(this, new RequestRoomRemoval(null, null, 0));
		router.registerListener(this, new RequestConnectionRemoval(null, null, 0));
		router.registerListener(this, new StartWorld(0));

		suggestionsView = new SuggestionsViewController();
		sandboxView = new SandBoxViewController();
		worldView = new WorldViewController();
		launchView = new LaunchViewController();

		mainPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
			if (newScene != null) {
				stage = (Stage) newScene.getWindow();
			}

		});

		runID = UUID.randomUUID();

		File file = new File(DataSaverLoader.projectPath + "\\summer-school\\" + runID + "\\algorithm\\");
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(DataSaverLoader.projectPath + "\\summer-school\\" + runID + "\\dungeon\\");
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(DataSaverLoader.projectPath + "\\summer-school\\" + runID + "\\room\\");
		if (!file.exists()) {
			file.mkdirs();
		}
		
		
		ActionLogger.getInstance().init();
//		InformativePopupManager.getInstance().setMainPane(mainPane);
//		mainPane.getChildren().add(new Popup(200,200));
		initLaunchView();

	}


	@Override
	public synchronized void ping(PCGEvent e) 
	{
		if(e instanceof ChangeCursor)
		{
			mainPane.getScene().setCursor(new ImageCursor(((ChangeCursor)e).getCursorImage()));
		}
		else if(e instanceof InitialRoom)
		{
			InitialRoom initRoom = (InitialRoom)e;
			
			dungeonMap.setInitialRoom(initRoom.getPickedRoom(), initRoom.getRoomPos());
			worldView.restoreBrush();
			worldView.initWorldMap(dungeonMap);
		}
		else if(e instanceof RequestPathFinding)
		{
			RequestPathFinding requestedPathFinding = (RequestPathFinding)e;
			
			dungeonMap.calculateBestPath(requestedPathFinding.getFromRoom(), 
										requestedPathFinding.getToRoom(), 
										requestedPathFinding.getFromPos(), 
										requestedPathFinding.getToPos());
		}
		else if(e instanceof RequestConnection)
		{
			RequestConnection rC = (RequestConnection)e;
			//TODO: Here you should check for which dungeon
			dungeonMap.addConnection(rC.getFromRoom(), rC.getToRoom(), rC.getFromPos(), rC.getToPos());
			worldView.initWorldMap(dungeonMap);
		}
		else if(e instanceof RequestNewRoom)
		{
			RequestNewRoom rNR = (RequestNewRoom)e;
			//TODO: Here you should check for which dungeon
			dungeonMap.addRoom(rNR.getWidth(), rNR.getHeight());
			worldView.initWorldMap(dungeonMap);
		}
		else if (e instanceof RequestRoomView) {
			
			//Yeah dont care about matrix but we do care about doors!
			
			if(((MapContainer) e.getPayload()).getMap().getDoorCount() > 0)
				initRoomView((MapContainer) e.getPayload());

		} else if (e instanceof RequestSuggestionsView) 
		{
			MapContainer container = (MapContainer) e.getPayload();
			if(container.getMap().getDoorCount() > 0)
			{
				initSuggestionsView(container.getMap());
			}

		} else if (e instanceof RequestWorldView) {
//			router.postEvent(new Stop());
			backToWorldView();

		} else if (e instanceof RequestEmptyRoom) {
			MapContainer container = (MapContainer) e.getPayload();
			initRoomView(container);

		} else if (e instanceof StartWorld) {
			initWorldView();
			worldView.initialSetup();
		}
		 else if (e instanceof RequestRoomRemoval) {

			Room container = (Room) e.getPayload();
			
			//TODO: Here you should check for which dungeon
			dungeonMap.removeRoom(container);
			backToWorldView();
		}
		 else if(e instanceof RequestConnectionRemoval) {

				RoomEdge edge = (RoomEdge) e.getPayload();
				
				//TODO: Here you should check for which dungeon
				dungeonMap.removeEdge(edge);
				backToWorldView();
		 }

	}

	/*
	 * Event stuff
	 */

	public void startNewFlow() {
		//TODO: There is mucho more than this, a lot of things need to be redone!
		
		ActionLogger.getInstance().saveNFlush();
		InformativePopupManager.getInstance().restartPopups();
		
		suggestionsView = new SuggestionsViewController();
		sandboxView = new SandBoxViewController();
		worldView = new WorldViewController();
		launchView = new LaunchViewController();
		dungeonMap = null;
		
		runID = UUID.randomUUID();
		File file = new File(DataSaverLoader.projectPath + "\\summer-school\\" + runID + "\\algorithm\\");
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(DataSaverLoader.projectPath + "\\summer-school\\" + runID + "\\dungeon\\");
		if (!file.exists()) {
			file.mkdirs();
		}
		file = new File(DataSaverLoader.projectPath + "\\summer-school\\" + runID + "\\room\\");
		if (!file.exists()) {
			file.mkdirs();
		}
		
		ActionLogger.getInstance().init();
		
		initLaunchView();
	}

	public void exitApplication() {
		// TODO: Maybe be a bit more graceful than this...

		Platform.exit();
		System.exit(0);
	}

	/*
	 * Initialisation methods
	 */

	/**
	 * Initialises the suggestions view.
	 */
	private void initSuggestionsView(Room room) {
		mainPane.getChildren().clear();

		AnchorPane.setTopAnchor(suggestionsView, 0.0);
		AnchorPane.setRightAnchor(suggestionsView, 0.0);
		AnchorPane.setBottomAnchor(suggestionsView, 0.0);
		AnchorPane.setLeftAnchor(suggestionsView, 0.0);
		mainPane.getChildren().add(suggestionsView);

//		saveItem.setDisable(true);
//		saveAsItem.setDisable(true);
//		exportItem.setDisable(true);


		suggestionsView.setActive(true);
		sandboxView.setActive(false);
		worldView.setActive(false);
		launchView.setActive(false);


		suggestionsView.initialise(room);
	}

	/**
	 * Initialises the world view.
	 */

	private void initWorldView() {
		mainPane.getChildren().clear();
		AnchorPane.setTopAnchor(worldView, 0.0);
		AnchorPane.setRightAnchor(worldView, 0.0);
		AnchorPane.setBottomAnchor(worldView, 0.0);
		AnchorPane.setLeftAnchor(worldView, 0.0);
		mainPane.getChildren().add(worldView);

		worldView.initWorldMap(initDungeon());

//		saveItem.setDisable(false);
//		saveAsItem.setDisable(false);
//		exportItem.setDisable(false);

		suggestionsView.setActive(false);
		sandboxView.setActive(false);
		worldView.setActive(true);
		launchView.setActive(false);

	}

	private void initLaunchView() {
//		mainPane.getChildren().clear();
//		AnchorPane.setTopAnchor(worldView, 0.0);
//		AnchorPane.setRightAnchor(worldView, 0.0);
//		AnchorPane.setBottomAnchor(worldView, 0.0);
//		AnchorPane.setLeftAnchor(worldView, 0.0);
//		mainPane.getChildren().add(worldView);
//
//		launchView.initGui();
//		suggestionsView.setActive(false);
//		roomView.setActive(false);
//		worldView.setActive(true);
//		launchView.setActive(false);
		
		initWorldView();
		worldView.initialSetup();

	}


	private void backToWorldView() {
		mainPane.getChildren().clear();
		AnchorPane.setTopAnchor(worldView, 0.0);
		AnchorPane.setRightAnchor(worldView, 0.0);
		AnchorPane.setBottomAnchor(worldView, 0.0);
		AnchorPane.setLeftAnchor(worldView, 0.0);
		mainPane.getChildren().add(worldView);

		worldView.initWorldMap(dungeonMap);

//		saveItem.setDisable(false);
//		saveAsItem.setDisable(false);
//		exportItem.setDisable(false);

		suggestionsView.setActive(false);
		sandboxView.setActive(false);
		worldView.setActive(true);
		launchView.setActive(false);

	}

	/**
	 * Initialises the edit view and starts a new generation run.
	 */
	private void initRoomView(MapContainer map) {
		mainPane.getChildren().clear();
		AnchorPane.setTopAnchor(sandboxView, 0.0);
		AnchorPane.setRightAnchor(sandboxView, 0.0);
		AnchorPane.setBottomAnchor(sandboxView, 0.0);
		AnchorPane.setLeftAnchor(sandboxView, 0.0);
		mainPane.getChildren().add(sandboxView);
		
		sandboxView.initializeView(map.getMap());
		sandboxView.roomMouseEvents();
		
		//TODO: Crazyness to create mini map based on the dungeon...
		//It would need to have different dimensions for the room view and for the world view
		
//		
//		roomView.minimap.getChildren().clear();
//		roomView.minimap.getChildren().add(dungeonMap.dPane);
//		dungeonMap.dPane.setPrefSize(roomView.minimap.getPrefWidth(), roomView.minimap.getPrefHeight());
		

//		saveItem.setDisable(false);
//		saveAsItem.setDisable(false);
//		exportItem.setDisable(false);

		worldView.setActive(false);
		sandboxView.setActive(true);		
		launchView.setActive(false);
		suggestionsView.setActive(false);


	}

	/*
	 * Mouse methods for controllers
	 */
	private Dungeon initDungeon() 
	{
		int width = Game.defaultWidth;
		int height = Game.defaultHeight;

		GeneratorConfig gc = null;
		try {
			gc = new GeneratorConfig();

		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		dungeonMap = new Dungeon(gc, 2, width, height);
		
		return dungeonMap;
	}

}
