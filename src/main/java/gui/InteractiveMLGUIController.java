package gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import finder.PatternFinder;
import game.ApplicationConfig;
import game.Dungeon;
import game.Game;
import game.Room;
import game.RoomEdge;
import game.MapContainer;
import game.TileTypes;
import generator.config.GeneratorConfig;
import gui.utils.MapRenderer;
import gui.views.LaunchViewController;
import gui.views.RoomViewController;
import gui.views.SuggestionsViewController;
import gui.views.TinderViewController;
import gui.views.WorldViewController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.MenuButton;
//import javafx.scene.control.Alert;
//import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import util.Point;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.ApplySuggestion;
import util.eventrouting.events.InitialRoom;
import util.eventrouting.events.MapLoaded;
import util.eventrouting.events.RequestAppliedMap;
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
import util.eventrouting.events.Start;
import util.eventrouting.events.StartWorld;
import util.eventrouting.events.StatusMessage;
import util.eventrouting.events.Stop;

/*
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 */

//Definetely I agree that this class can be the one "controlling" all the views and have in any moment the most updated version of
//the dungeon. But it is simply doing too much at the moment, It should "create" the dungeon but if another room wants to be incorporated
//It should be the dungeon adding such a room, Basically this should be an intermid, knowing which dungeon, which view, etc.
public class InteractiveMLGUIController implements Initializable, Listener {

	@FXML private AnchorPane mainPane;
	@FXML private MenuItem newItem;
	@FXML private MenuItem openItem;
	@FXML private MenuItem saveItem;
	@FXML private MenuItem saveAsItem;
	@FXML private MenuItem exportItem;
	@FXML private MenuItem prefsItem;
	@FXML private MenuItem exitItem;
	@FXML private MenuItem aboutItem;
	Stage stage = null;

	TinderViewController tinderView = null;
	EventHandler<MouseEvent> mouseEventHandler = null;

	final static Logger logger = LoggerFactory.getLogger(InteractiveGUIController.class);
	private static EventRouter router = EventRouter.getInstance();
	private ApplicationConfig config;
	
	
	//NEW
	private Dungeon dungeonMap = new Dungeon();
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read config file.");
		}

		router.registerListener(this, new InitialRoom(null, null));
		router.registerListener(this, new RequestPathFinding(null, -1, null, null, null, null));
		router.registerListener(this, new RequestConnection(null, -1, null, null, null, null));
		router.registerListener(this, new RequestNewRoom(null, -1, -1, -1));
		router.registerListener(this, new StatusMessage(null));
		router.registerListener(this, new AlgorithmDone(null, null));
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

		tinderView = new TinderViewController();
		
		mainPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
			if (newScene != null) {
				stage = (Stage) newScene.getWindow();

			}

		});

		initLaunchView();


	}

	private void initLaunchView() {
		mainPane.getChildren().clear();
		
		
		
		AnchorPane.setTopAnchor(tinderView, 0.0);
		AnchorPane.setRightAnchor(tinderView, 0.0);
		AnchorPane.setBottomAnchor(tinderView, 0.0);
		AnchorPane.setLeftAnchor(tinderView, 0.0);
		mainPane.getChildren().add(tinderView);
//		
//		tinderView.setActive(true);
		tinderView.SetView();

	}


	@Override
	public synchronized void ping(PCGEvent e) 
	{

	}
	
	/*
	 * Event stuff
	 */

	public void startNewFlow() {
		initLaunchView();
	}

	public void exitApplication() {
		// TODO: Maybe be a bit more graceful than this...

		Platform.exit();
		System.exit(0);
	}

	public void openMap() {
		System.out.println("NOT WORKING :O");
	}

	public void saveMap() {
		System.out.println("NOT SAVING AT THE MOMENT");
	}

	public void exportImage() {
		System.out.println("HA");
	}

	public void openPreferences() {
		System.out.println("Preferences...");
	}

	public void generateNewMap() {
		System.out.println("Generate map");
	}

	private void updateConfigBasedOnMap(Room room) {
		config.setDimensionM(room.getColCount());
		config.setDimensionN(room.getRowCount());
	}



}
