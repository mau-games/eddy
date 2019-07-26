package gui.views;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import collectors.ActionLogger;
import collectors.XMLHandler;
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
import game.tiles.BossEnemyTile;
import game.tiles.EnemyTile;
import game.tiles.FloorTile;
import game.tiles.TreasureTile;
import game.tiles.WallTile;
import gui.controls.Drawer;
import gui.controls.EvolutionMAPEPane;
import gui.controls.InteractiveMap;
import gui.controls.Modifier;
import gui.controls.NGramPane;
import gui.controls.RoomPreview;
import gui.controls.SuggestionRoom;
import gui.utils.InformativePopupManager;
import gui.utils.InformativePopupManager.PresentableInformation;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.ApplySuggestion;
import util.eventrouting.events.MAPEGridUpdate;
import util.eventrouting.events.MAPElitesDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestWorldView;
import util.eventrouting.events.Stop;
import util.eventrouting.events.SuggestedMapSelected;
import util.eventrouting.events.SuggestedMapsDone;
import util.eventrouting.events.SuggestionApplied;
import util.eventrouting.events.intraview.DungeonPreviewSelected;
import util.eventrouting.events.intraview.RoomEditionStarted;

/**
 * This class controls the interactive application's edit view.
 * FIXME: A lot of things need to change here! 
 * 
 * @author Johan Holmberg, Malmö University
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 */
public class SandBoxViewController extends BorderPane implements Listener 
{
	@FXML private ComboBox<String> generationTypeFX;
	
	@FXML public StackPane mapPane;
	@FXML public HBox dungeonRoomsPane;

	//left side as well
	@FXML private GridPane legend;
	@FXML private ToggleGroup brushes;
	
	//RIGHT SIDE!
	@FXML private VBox rightSidePane;
	
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
	
	@FXML private Button worldGridBtn; //ok
	@FXML private Button genSuggestionsBtn; //bra
	@FXML private Button appSuggestionsBtn; //bra
	@FXML private Button flowControlBtn; //bra
	@FXML private Button stopEABtn; //bra
	@FXML private Button saveGenBtn;


	//Literally the only thing that should be here
	private InteractiveMap mapView;
	private Canvas patternCanvas;
	private Canvas warningCanvas;
	private Canvas lockCanvas;
	private Canvas brushCanvas;
	private Canvas tileCanvas;

	private MapContainer map;

	private boolean isActive = false; //for having the same event listener in different views 
	private boolean isFeasible = true; //How feasible the individual is
	public HashMap<Integer, Room> suggestedRooms = new HashMap<Integer, Room>();

	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
//	private final static Logger logger = LoggerFactory.getLogger(RoomViewController.class);
	private ApplicationConfig config;

	public Drawer myBrush;
	
	int mapWidth;
	int mapHeight;
	
	//THE PANES THAT CAN BE INSTANTIATED:
	@FXML private EvolutionMAPEPane evolutionPane;
	private NGramPane nGramPane;
	
	/**
	 * Creates an instance of this class.
	 */
	@SuppressWarnings("unchecked")
	public SandBoxViewController() {
		
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/sandbox/SandBoxView.fxml"));
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
		router.registerListener(this, new SuggestionApplied(null));
		router.registerListener(this, new DungeonPreviewSelected(null));

		myBrush = new Drawer();
		myBrush.AddmodifierComponent("Lock", new Modifier(lockBrush));

		brushSlider.valueProperty().addListener((obs, oldval, newVal) -> { 
			redrawPatterns(mapView.getMap());
			ActionLogger.getInstance().storeAction(ActionType.CHANGE_VALUE, 
													View.ROOM, 
													TargetPane.BRUSH_PANE,
													false,
													oldval,
													newVal); //Point 
			});
		
		nGramPane = new NGramPane();
		
		init();
		
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

	}
	
	//TODO: THAT 42 has to disappear!! 
	public void initializeView(Room roomToBe)
	{
//		width = 420/roomToBe.getColCount();
//		height = 420/roomToBe.getRowCount();
		mapHeight = (int)(42.0 * (float)((float)roomToBe.getRowCount())); //Recalculate map size
		mapWidth = (int)(42.0 * (float)((float)roomToBe.getColCount()));//Recalculate map size
		router.postEvent(new RoomEditionStarted(roomToBe));
		initMapView();
		resetView();
		roomToBe.forceReevaluation();
		updateRoom(roomToBe);
		
		//Add the info of the dungeon 
		dungeonRoomsPane.getChildren().clear();
		List<Room> dungeonRooms = roomToBe.owner.getAllRooms();
		for(int i = 0; i < dungeonRooms.size(); i++) 
		{
			RoomPreview roomPreview = new RoomPreview(dungeonRooms.get(i));
			dungeonRoomsPane.getChildren().add(roomPreview.getRoomCanvas());
			
			Platform.runLater(() -> {
				roomPreview.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(roomPreview.getPreviewRoom(), -1));
			});
		}
	}

	/**
	 * Initialises the map view and creates canvases for pattern drawing and
	 * infeasibility notifications.
	 */
	private void initMapView() {
		
		if(getMapView() != null)
		{
			getMapView().destructor();
		}
		
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
		
		tileCanvas = new Canvas(mapWidth, mapHeight);
		StackPane.setAlignment(tileCanvas, Pos.CENTER);
		mapPane.getChildren().add(tileCanvas);
		tileCanvas.setVisible(true);
		tileCanvas.setMouseTransparent(true);

		floorBtn.setMinWidth(75);
		wallBtn.setMinWidth(75);
		enemyBtn.setMinWidth(75);
		bossEnemyBtn.setMinWidth(75);
		treasureBtn.setMinWidth(75);

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
	
	@FXML public void OnChangeGenerationType() 
	{
		switch(generationTypeFX.getValue())
		{
		case "Evolution":
				evolutionPane.setVisible(true);
				evolutionPane.setActive(true);
				rightSidePane.getChildren().clear();
				rightSidePane.getChildren().add(evolutionPane);
				break;
		case "N-Gram":
				nGramPane.setVisible(true);
//				evolutionPane.setActive(false);
				nGramPane.setActive(true);
				rightSidePane.getChildren().clear();
				rightSidePane.getChildren().add(nGramPane);
				break;
		case "Random":
				break;
		case "Example":
				break;
		case "Preference Learning":
				break;
		case "GAN":
				break;
		case "Autoencoder":
				break;
			default:
				System.out.println("Something went wrong, the displayed value is " + generationTypeFX.getValue());
				break;
		}
	}
	
	public void setContainer(MapContainer map) {
		map = this.map;
	}

	@Override
	public void ping(PCGEvent e) {
		
		if (e instanceof MapUpdate) {
		} 
		else if(e instanceof SuggestionApplied)
		{
			replaceRoom((Room)e.getPayload());
		}
		else if(e instanceof DungeonPreviewSelected)
		{
			initializeView((Room)e.getPayload());
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
		getMapView().updateMap(room);
		redrawPatterns(room);
		redrawLocks(room);
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
	public Image getRenderedMap() { //TODO: THERE CAN BE SOME PROBLEM HERE
		return renderer.renderMap(getMapView().getMap());
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
	
	/**
	 * Marks the map as being infeasible.
	 * 
	 * @param state
	 */
	public void mapIsFeasible(boolean state) {
		isFeasible = state;
		warningCanvas.setVisible(!isFeasible);
		
		if(!state)
			InformativePopupManager.getInstance().requestPopup(mapView, PresentableInformation.ROOM_INFEASIBLE, "");
	}

	public void replaceRoom(Room selectedRoom)
	{
		//pass the info from the selected suggested room to the main one!!
		mapView.getMap().applySuggestion(selectedRoom);
		updateRoom(mapView.getMap());
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
//					lockCanvas.getGraphicsContext2D().drawImage(renderer.GetLock(mapView.scale * 3.0f, mapView.scale * 3.0f), (j-1) * mapView.scale, (i-1) * mapView.scale);
				}
			}
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
				XMLHandler.getInstance().saveIndividualRoomXML(mapView.getMap(), "room\\");
				mapIsFeasible(mapView.getMap().isIntraFeasible());
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

	public InteractiveMap getMapView() {
		return mapView;
	}

	public void setMapView(InteractiveMap mapView) {
		this.mapView = mapView;
	}


	public ToggleButton getPatternButton() {
		return patternButton;
	}

	public void setPatternButton(ToggleButton patternButton) {
		this.patternButton = patternButton;
	}
}
