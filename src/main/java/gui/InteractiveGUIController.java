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
import game.Game;
import game.Map;
import game.MapContainer;
import game.TileTypes;
import generator.config.GeneratorConfig;
import gui.utils.MapRenderer;
import gui.views.RoomViewController;
import gui.views.SuggestionsViewController;
import gui.views.WorldViewController;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
import util.eventrouting.events.MapLoaded;
import util.eventrouting.events.RequestEmptyRoom;
import util.eventrouting.events.RequestNullRoom;
import util.eventrouting.events.RequestRedraw;
import util.eventrouting.events.RequestRoomView;
import util.eventrouting.events.RequestSuggestionsView;
import util.eventrouting.events.RequestWorldView;
import util.eventrouting.events.Start;
import util.eventrouting.events.StatusMessage;
import util.eventrouting.events.Stop;
import util.eventrouting.events.SuggestedMapsDone;
import util.eventrouting.events.SuggestedMapsLoading;



public class InteractiveGUIController implements Initializable, Listener {

	@FXML private AnchorPane mainPane;
	@FXML private MenuItem newItem;
	@FXML private MenuItem openItem;
	@FXML private MenuItem saveItem;
	@FXML private MenuItem saveAsItem;
	@FXML private MenuItem exportItem;
	@FXML private MenuItem prefsItem;
	@FXML private MenuItem exitItem;
	@FXML private MenuItem aboutItem;
	//	@FXML private MenuItem saveWorldItem;
	//	@FXML private MenuItem openWorldItem;
	//	@FXML private MenuButton roomSizeBtn;

	Stage stage = null;

	Game game = new Game();

	SuggestionsViewController suggestionsView = null;
	RoomViewController roomView = null;
	WorldViewController worldView = null;
	EventHandler<MouseEvent> mouseEventHandler = null;

	final static Logger logger = LoggerFactory.getLogger(InteractiveGUIController.class);
	private static EventRouter router = EventRouter.getInstance();
	private ApplicationConfig config;

	private MapContainer currentQuadMap = new MapContainer();
	private MapContainer quadMap1 = new MapContainer();
	private MapContainer quadMap2 = new MapContainer();
	private MapContainer quadMap3 = new MapContainer();
	private MapContainer quadMap4 = new MapContainer();
	private MapContainer tempLargeContainer = new MapContainer();

	// VARIABLE FOR PICKING THE SIZE OF THE WORLD MAP (3 = 3x3 map)
	private int size = 3;
	// ArrayList<MapContainer> worldMapList = new ArrayList<MapContainer>();
	private MapContainer[][] worldMapMatrix = new MapContainer[size][size];
	private int row = 0;
	private int col = 0;

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof RequestRoomView) {
			worldMapMatrix = ((RequestRoomView) e).getMatrix();
			row = ((RequestRoomView) e).getRow();
			col = ((RequestRoomView) e).getCol();
			MapContainer container = (MapContainer) e.getPayload();
			initRoomView(container);	
			
		} else if (e instanceof RequestSuggestionsView) {
			worldMapMatrix = ((RequestSuggestionsView) e).getMatrix();
			row = ((RequestSuggestionsView) e).getRow();
			col = ((RequestSuggestionsView) e).getCol();
			MapContainer container = (MapContainer) e.getPayload();
			initSuggestionsView();
		} else if (e instanceof RequestWorldView) {

			backToWorldView();
		} else if (e instanceof RequestEmptyRoom) {
			worldMapMatrix = ((RequestEmptyRoom) e).getMatrix();
			row = ((RequestEmptyRoom) e).getRow();
			col = ((RequestEmptyRoom) e).getCol();
			MapContainer container = (MapContainer) e.getPayload();
			initRoomView(container);

		} else if (e instanceof SuggestedMapsDone) {
			restrictNav();
//			roomView.getRightButton().setDisable(false);
//			roomView.getLeftButton().setDisable(false);
//			roomView.getDownButton().setDisable(false);
//			roomView.getUpButton().setDisable(false);
		} else if (e instanceof SuggestedMapsLoading) {

			roomView.getRightButton().setDisable(true);
			roomView.getLeftButton().setDisable(true);
			roomView.getDownButton().setDisable(true);
			roomView.getUpButton().setDisable(true);
		} else if (e instanceof RequestNullRoom) {
			worldMapMatrix = ((RequestNullRoom) e).getMatrix();
			row = ((RequestNullRoom) e).getRow();
			col = ((RequestNullRoom) e).getCol();
			MapContainer container = (MapContainer) e.getPayload();

			if (!worldMapMatrix[row][col].getMap().getNull()) {
				Map nullMap = new Map(11, 11, 0);
				MapContainer nullCont = new MapContainer();
				nullCont.setMap(nullMap);
				worldMapMatrix[row][col] = nullCont;
			}
			else {
				// South
				Point south = new Point(11/2, 11-1);
				// East
				Point east = new Point(11-1, 11/2);
				// North
				Point north = new Point(11/2, 0);
				// West
				Point west = new Point(0, 11/2);
				System.out.println("else");
				GeneratorConfig gc = null;
				try {
					gc = new GeneratorConfig();

				} catch (MissingConfigurationException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}
				Map tempMap = null;
				// 1
				if (row == 0 && col == 0) {
					tempMap = new Map(gc, 11, 11, null, east, south, null);
					System.out.println("1");
				}
				// 3
				if (row == 0 && col == (size - 1)) {
					tempMap = new Map(gc, 11, 11, null, null, south, west);
					System.out.println("3");
				}
				// 7
				if (row == (size - 1) && col == 0) {
					tempMap = new Map(gc, 11, 11, north, east, null, null);
					System.out.println("7");
				}
				// 9
				if (row == (size - 1) && col == (size - 1)) {
					tempMap = new Map(gc, 11, 11, north, null, null, west);
					System.out.println("9");
				}
				// top
				if (row == 0 && col != (size - 1) && col != 0) {
					tempMap = new Map(gc, 11, 11, null, east, south, west);
					System.out.println("top");
				}
				// left
				if (row != 0 && col == 0 && row != (size - 1)) {
					tempMap = new Map(gc, 11, 11, north, east, south, null);
					System.out.println("left");
				}
				// right
				if (row != 0 && row != (size - 1) && col == (size - 1)) {
					tempMap = new Map(gc, 11, 11, north, null, south, west);
					System.out.println("right");
				}
				// bottom
				if (col != 0 && col != (size - 1) && row == (size - 1)) {
					tempMap = new Map(gc, 11, 11, north, east, null, west);
					System.out.println("bottom");
				}
				// other
				else if (col != 0 && col != (size - 1) && row != 0 && row != (size - 1)) {
					tempMap = new Map(gc, 11, 11, north, east, south, west);
					System.out.println("other");
				}
				MapContainer revertCont = new MapContainer();
				revertCont.setMap(tempMap);
				worldMapMatrix[row][col] = revertCont;
			}
			evaluateNullChange();
			backToWorldView();
		}

	}
	
	private void restrictNav() {
		MapRenderer renderer = MapRenderer.getInstance();
		if (row != 0) {
			if (!worldMapMatrix[row - 1][col].getMap().getNull()) {
				Platform.runLater(() -> {
				ImageView image = new ImageView(renderer.renderMap(worldMapMatrix[row - 1][col].getMap().toMatrix()));
				image.setScaleX(0.85);
				image.setScaleY(0.85);
				roomView.getUpButton().setGraphic(image);
				roomView.getUpButton().setScaleX(0.15);
				roomView.getUpButton().setScaleY(0.15);
				});
				roomView.getUpButton().setDisable(false);
				
			}
		}
		else {
			Platform.runLater(() -> {
			roomView.getUpButton().setGraphic(null);
			});
		}
		if (row != (size-1)) {
			if (!worldMapMatrix[row + 1][col].getMap().getNull()) {
				Platform.runLater(() -> {
					ImageView image = new ImageView(renderer.renderMap(worldMapMatrix[row + 1][col].getMap().toMatrix()));
					image.setScaleX(0.85);
					image.setScaleY(0.85);
					roomView.getDownButton().setGraphic(image);
					roomView.getDownButton().setScaleX(0.15);
					roomView.getDownButton().setScaleY(0.15);
					});
				roomView.getDownButton().setDisable(false);
			}
		}
		else {
			Platform.runLater(() -> {
			roomView.getDownButton().setGraphic(null);
			});
		}
		if (col != 0) {
			if (!worldMapMatrix[row][col - 1].getMap().getNull()) {
				Platform.runLater(() -> {
					ImageView image = new ImageView(renderer.renderMap(worldMapMatrix[row][col - 1].getMap().toMatrix()));
					image.setScaleX(0.85);
					image.setScaleY(0.85);
					roomView.getLeftButton().setGraphic(image);
					roomView.getLeftButton().setScaleX(0.15);
					roomView.getLeftButton().setScaleY(0.15);
					});
				roomView.getLeftButton().setDisable(false);
			}
		}
		else {
			Platform.runLater(() -> {
			roomView.getLeftButton().setGraphic(null);
			});
		}
		if (col != (size-1)) {
			if (!worldMapMatrix[row][col + 1].getMap().getNull()) {
				Platform.runLater(() -> {
					ImageView image = new ImageView(renderer.renderMap(worldMapMatrix[row][col + 1].getMap().toMatrix()));
					image.setScaleX(0.85);
					image.setScaleY(0.85);
					roomView.getRightButton().setGraphic(image);
					roomView.getRightButton().setScaleX(0.15);
					roomView.getRightButton().setScaleY(0.15);
					});
				roomView.getRightButton().setDisable(false);
			}
		}
		else {
			Platform.runLater(() -> {
			roomView.getRightButton().setGraphic(null);
			});
		}
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read config file.");
		}

		router.registerListener(this, new StatusMessage(null));
		router.registerListener(this, new AlgorithmDone(null));
		router.registerListener(this, new RequestRedraw());
		router.registerListener(this, new RequestRoomView(null, 0, 0, null));
		router.registerListener(this, new MapLoaded(null));
		//		router.registerListener(this, new RequestSuggestionsView());
		router.registerListener(this, new RequestWorldView());
		router.registerListener(this, new RequestEmptyRoom(null, 0, 0, null));
		router.registerListener(this, new RequestSuggestionsView(null, 0, 0, null, 0));
		router.registerListener(this, new Stop());
		router.registerListener(this, new SuggestedMapsDone());
		router.registerListener(this, new SuggestedMapsLoading());
		router.registerListener(this, new RequestNullRoom(null, 0, 0, null));

		suggestionsView = new SuggestionsViewController();
		roomView = new RoomViewController();
		worldView = new WorldViewController();

		mainPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
			if (newScene != null) {
				stage = (Stage) newScene.getWindow();
			}
		});

		initWorldView();


	}

	/*
	 * Event stuff
	 */

	public void startNewFlow() {
		//		router.postEvent(new Start(6));
		initWorldView();
	}

	//	public void goToWorldView() {
	//		router.postEvent(new RequestWorldView());
	//	}

	public void exitApplication() {
		// TODO: Maybe be a bit more graceful than this...

		Platform.exit();
		System.exit(0);
	}

	public void openMap() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Map");
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("Map Files", "*.map"),
				new ExtensionFilter("All Files", "*.*"));
		File selectedFile = fileChooser.showOpenDialog(stage);
		if (selectedFile != null) {
			try {

				FileReader reader = new FileReader(selectedFile);
				String mapString = "";
				while(reader.ready()){
					char c = (char) reader.read();

					mapString += c;
				}
				worldMapMatrix = updateLargeMap(mapString);
				Map map = worldMapMatrix[col][row].getMap();
				PatternFinder finder = map.getPatternFinder();
				MapContainer result = new MapContainer();
				currentQuadMap = result;
				roomView.updateMap(map);
				result.setMap(map);
				result.setMicroPatterns(finder.findMicroPatterns());
				result.setMesoPatterns(finder.findMesoPatterns());
				result.setMacroPatterns(finder.findMacroPatterns());
				//EventRouter.getInstance().postEvent(new MapLoaded(result));
				router.postEvent(new RequestWorldView());
				//Map.LoadMap(selectedFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void saveMap() {
		//		tempLargeContainer = updateLargeMap();
		roomView.updateLargeMap(tempLargeContainer.getMap());
		DateTimeFormatter format =
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-s-n");
		String name = "map_" +
				LocalDateTime.now().format(format) + ".map";

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Save Map");
		fileChooser.setInitialFileName(name);
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("Map Files", "*.map"),
				new ExtensionFilter("All Files", "*.*"));
		File selectedFile = fileChooser.showSaveDialog(stage);
		if (selectedFile != null) {
			logger.debug("Writing map to " + selectedFile.getPath());
			try {
				Files.write(selectedFile.toPath(), matrixToString().getBytes());
			} catch (IOException e) {
				logger.error("Couldn't write map to " + selectedFile +
						":\n" + e.getMessage());
			}
		}
	}

	public void exportImage() {
		DateTimeFormatter format =
				DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-s-n");
		String name = "renderedmap_" +
				LocalDateTime.now().format(format) + ".png";

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open Map");
		fileChooser.setInitialFileName(name);
		fileChooser.getExtensionFilters().addAll(
				new ExtensionFilter("PNG Files", "*.png"),
				new ExtensionFilter("All Files", "*.*"));
		File selectedFile = fileChooser.showSaveDialog(stage);
		if (selectedFile != null && roomView.getCurrentMap() != null) {
			logger.debug("Exporting map to " + selectedFile.getPath());
			BufferedImage image = SwingFXUtils.fromFXImage(roomView.getRenderedMap(), null);

			try {
				ImageIO.write(image, "png", selectedFile);
			} catch (IOException e1) {
				logger.error("Couldn't export map to " + selectedFile +
						":\n" + e1.getMessage());
			}
		}
	}

	public void openPreferences() {
		System.out.println("Preferences...");
	}

	public void generateNewMap() {
		System.out.println("Generate map");
	}

	private void updateConfigBasedOnMap(Map map) {
		config.setDimensionM(map.getColCount());
		config.setDimensionN(map.getRowCount());
	}

	/*
	 * Initialisation methods
	 */

	/**
	 * Initialises the suggestions view.
	 */
	private void initSuggestionsView() {
		mainPane.getChildren().clear();

		AnchorPane.setTopAnchor(suggestionsView, 0.0);
		AnchorPane.setRightAnchor(suggestionsView, 0.0);
		AnchorPane.setBottomAnchor(suggestionsView, 0.0);
		AnchorPane.setLeftAnchor(suggestionsView, 0.0);
		mainPane.getChildren().add(suggestionsView);

		saveItem.setDisable(true);
		saveAsItem.setDisable(true);
		exportItem.setDisable(true);


		suggestionsView.setActive(true);
		roomView.setActive(false);
		worldView.setActive(false);

		suggestionsView.initialise();
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


		//		createWorldMatrix();

		worldView.initWorldMap(initMatrix());

		saveItem.setDisable(false);
		saveAsItem.setDisable(false);
		exportItem.setDisable(false);

		suggestionsView.setActive(false);
		roomView.setActive(false);
		worldView.setActive(true);
	}


	private void backToWorldView() {
		mainPane.getChildren().clear();
		AnchorPane.setTopAnchor(worldView, 0.0);
		AnchorPane.setRightAnchor(worldView, 0.0);
		AnchorPane.setBottomAnchor(worldView, 0.0);
		AnchorPane.setLeftAnchor(worldView, 0.0);
		mainPane.getChildren().add(worldView);


		//		createWorldMatrix();

		//evaluateNullChange();
		worldView.initWorldMap(worldMapMatrix);

		saveItem.setDisable(false);
		saveAsItem.setDisable(false);
		exportItem.setDisable(false);

		suggestionsView.setActive(false);
		roomView.setActive(false);
		worldView.setActive(true);
	}

	/**
	 * Initialises the edit view and starts a new generation run.
	 */
	private void initRoomView(MapContainer map) {
		mainPane.getChildren().clear();
		AnchorPane.setTopAnchor(roomView, 0.0);
		AnchorPane.setRightAnchor(roomView, 0.0);
		AnchorPane.setBottomAnchor(roomView, 0.0);
		AnchorPane.setLeftAnchor(roomView, 0.0);
		mainPane.getChildren().add(roomView);
		roomView.updateLargeMap(map.getMap());
		roomView.updateMap(map.getMap());	
		setCurrentQuadMap(map);

		roomMouseEvents();
		roomButtonEvents();

		roomView.generateNewMaps();

		saveItem.setDisable(false);
		saveAsItem.setDisable(false);
		exportItem.setDisable(false);

		roomView.setActive(false);
		roomView.setActive(true);
	}



	private void roomButtonEvents() {


		roomView.getRightButton().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				worldMapMatrix[row][col] = currentQuadMap;

				if (col != (size - 1)) {
					col++;


					currentQuadMap = worldMapMatrix[row][col];
					roomView.updateRoom(currentQuadMap.getMap());
					roomView.generateNewMaps();


				}
				roomView.getRightButton().setDisable(true);
				roomView.getLeftButton().setDisable(true);
				roomView.getDownButton().setDisable(true);
				roomView.getUpButton().setDisable(true);

			}
		}); 

		roomView.getLeftButton().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				worldMapMatrix[row][col] = currentQuadMap;

				if (col != 0) {
					col--;

					currentQuadMap = worldMapMatrix[row][col];
					roomView.updateRoom(currentQuadMap.getMap());
					roomView.generateNewMaps();



				}
				roomView.getRightButton().setDisable(true);
				roomView.getLeftButton().setDisable(true);
				roomView.getDownButton().setDisable(true);
				roomView.getUpButton().setDisable(true);
			}
		}); 

		roomView.getDownButton().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				worldMapMatrix[row][col] = currentQuadMap;

				if (row != (size - 1)) {
					row++;

					currentQuadMap = worldMapMatrix[row][col];
					roomView.updateRoom(currentQuadMap.getMap());
					roomView.generateNewMaps();


				}
				roomView.getRightButton().setDisable(true);
				roomView.getLeftButton().setDisable(true);
				roomView.getDownButton().setDisable(true);
				roomView.getUpButton().setDisable(true);
			}

		}); 
		roomView.getUpButton().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				worldMapMatrix[row][col] = currentQuadMap;

				if (row != 0) {
					row--;

					currentQuadMap = worldMapMatrix[row][col];
					roomView.updateRoom(currentQuadMap.getMap());
					roomView.generateNewMaps();



				}
				roomView.getRightButton().setDisable(true);
				roomView.getLeftButton().setDisable(true);
				roomView.getDownButton().setDisable(true);
				roomView.getUpButton().setDisable(true);

			}
		}); 

	}

	/*
	 * Mouse methods for controllers
	 */

	private void roomMouseEvents() {
		roomView.getMapView().addEventFilter(MouseEvent.MOUSE_CLICKED, roomView.new EditViewEventHandler());
		roomView.getMap(0).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
			roomView.replaceMap(0);
			MapContainer selectedMiniCont = new MapContainer();
			selectedMiniCont.setMap(roomView.getSelectedMiniMap());	
			currentQuadMap = selectedMiniCont;

			worldMapMatrix[row][col] = selectedMiniCont;
			roomView.setMousePressed(true);


		});
		roomView.getMap(1).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
			roomView.replaceMap(1);
			MapContainer selectedMiniCont = new MapContainer();
			selectedMiniCont.setMap(roomView.getSelectedMiniMap());
			currentQuadMap = selectedMiniCont;

			worldMapMatrix[row][col] = selectedMiniCont;
			roomView.setMousePressed(true);


		});
		roomView.getMap(2).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
			roomView.replaceMap(2);
			MapContainer selectedMiniCont = new MapContainer();
			selectedMiniCont.setMap(roomView.getSelectedMiniMap());
			currentQuadMap = selectedMiniCont;

			worldMapMatrix[row][col] = selectedMiniCont;
			roomView.setMousePressed(true);


		});
		roomView.getMap(3).addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> {
			roomView.replaceMap(3);
			MapContainer selectedMiniCont = new MapContainer();
			selectedMiniCont.setMap(roomView.getSelectedMiniMap());

			currentQuadMap = selectedMiniCont;
			worldMapMatrix[row][col] = selectedMiniCont;
			roomView.setMousePressed(true);


		});
		roomView.resetMiniMaps();
		roomView.setMousePressed(false);
	}

	private void evaluateNullChange() {
		// South
		Point south = new Point(11/2, 11-1);
		// East
		Point east = new Point(11-1, 11/2);
		// North
		Point north = new Point(11/2, 0);
		// West
		Point west = new Point(0, 11/2);
		for (int rows = 0; rows < size; rows++) {
			for (int cols = 0; cols < size; cols++) {
				if (!worldMapMatrix[rows][cols].getMap().getNull()) {
					if (rows != 0) {
						//north
						if (worldMapMatrix[rows - 1][cols].getMap().getNull() && (worldMapMatrix[rows][cols].getMap().matrix[north.getX()][north.getY()] == 5 || 
								worldMapMatrix[rows][cols].getMap().matrix[north.getX()][north.getY()] == 4)) {
							worldMapMatrix[rows][cols].getMap().matrix[north.getX()][north.getY()] = 0;
							worldMapMatrix[rows][cols].getMap().setNumberOfDoors(worldMapMatrix[rows][cols].getMap().getNumberOfDoors() - 1);
							worldMapMatrix[rows][cols].getMap().setNorth(false);
						}
						else if (!worldMapMatrix[rows - 1][cols].getMap().getNull() && !(worldMapMatrix[rows][cols].getMap().matrix[north.getX()][north.getY()] == 5 || 
								worldMapMatrix[rows][cols].getMap().matrix[north.getX()][north.getY()] == 4)) {
							worldMapMatrix[rows][cols].getMap().matrix[north.getX()][north.getY()] = 4;
							worldMapMatrix[rows][cols].getMap().setNumberOfDoors(worldMapMatrix[rows][cols].getMap().getNumberOfDoors() + 1);
							worldMapMatrix[rows][cols].getMap().setNorth(true);
						}
					}
					if (cols != (size - 1)) {
						//east
						if (worldMapMatrix[rows][cols + 1].getMap().getNull() && (worldMapMatrix[rows][cols].getMap().matrix[east.getX()][east.getY()] == 5 || 
								worldMapMatrix[rows][cols].getMap().matrix[east.getX()][east.getY()] == 4)) {
							worldMapMatrix[rows][cols].getMap().matrix[east.getX()][east.getY()] = 0;
							worldMapMatrix[rows][cols].getMap().setNumberOfDoors(worldMapMatrix[rows][cols].getMap().getNumberOfDoors() - 1);
							worldMapMatrix[rows][cols].getMap().setEast(false);
						}
						else if (!worldMapMatrix[rows][cols + 1].getMap().getNull() && !(worldMapMatrix[rows][cols].getMap().matrix[east.getX()][east.getY()] == 5 || 
								worldMapMatrix[rows][cols].getMap().matrix[east.getX()][east.getY()] == 4)) {
							worldMapMatrix[rows][cols].getMap().matrix[east.getX()][east.getY()] = 4;
							worldMapMatrix[rows][cols].getMap().setNumberOfDoors(worldMapMatrix[rows][cols].getMap().getNumberOfDoors() + 1);
							worldMapMatrix[rows][cols].getMap().setEast(true);
						}

					}
					if (rows != (size - 1)) {
						//south
						if (worldMapMatrix[rows + 1][cols].getMap().getNull() && (worldMapMatrix[rows][cols].getMap().matrix[south.getX()][south.getY()] == 5 || 
								worldMapMatrix[rows][cols].getMap().matrix[south.getX()][south.getY()] == 4)) {
							worldMapMatrix[rows][cols].getMap().matrix[south.getX()][south.getY()] = 0;
							worldMapMatrix[rows][cols].getMap().setNumberOfDoors(worldMapMatrix[rows][cols].getMap().getNumberOfDoors() - 1);
							worldMapMatrix[rows][cols].getMap().setSouth(false);
						}
						else if (!worldMapMatrix[rows + 1][cols].getMap().getNull() && !(worldMapMatrix[rows][cols].getMap().matrix[south.getX()][south.getY()] == 5 || 
								worldMapMatrix[rows][cols].getMap().matrix[south.getX()][south.getY()] == 4)) {
							worldMapMatrix[rows][cols].getMap().matrix[south.getX()][south.getY()] = 4;
							worldMapMatrix[rows][cols].getMap().setNumberOfDoors(worldMapMatrix[rows][cols].getMap().getNumberOfDoors() + 1);
							worldMapMatrix[rows][cols].getMap().setSouth(true);
						}

					}
					if (cols != 0) {
						//west
						if (worldMapMatrix[rows][cols - 1].getMap().getNull() && (worldMapMatrix[rows][cols].getMap().matrix[west.getX()][west.getY()] == 5 || 
								worldMapMatrix[rows][cols].getMap().matrix[west.getX()][west.getY()] == 4)) {
							worldMapMatrix[rows][cols].getMap().matrix[west.getX()][west.getY()] = 0;
							worldMapMatrix[rows][cols].getMap().setNumberOfDoors(worldMapMatrix[rows][cols].getMap().getNumberOfDoors() - 1);
							worldMapMatrix[rows][cols].getMap().setWest(false);
						}
						else if (!worldMapMatrix[rows][cols - 1].getMap().getNull() && !(worldMapMatrix[rows][cols].getMap().matrix[west.getX()][west.getY()] == 5 || 
								worldMapMatrix[rows][cols].getMap().matrix[west.getX()][west.getY()] == 4)) {
							worldMapMatrix[rows][cols].getMap().matrix[west.getX()][west.getY()] = 4;
							worldMapMatrix[rows][cols].getMap().setNumberOfDoors(worldMapMatrix[rows][cols].getMap().getNumberOfDoors() + 1);
							worldMapMatrix[rows][cols].getMap().setWest(true);
						}

					}
					//System.out.println(worldMapMatrix[rows][cols].getMap().getNumberOfDoors() + " doors for: " + rows + ", " + cols);
					if (worldMapMatrix[rows][cols].getMap().getNumberOfDoors() == 0) {
						Map nullMap = new Map(11, 11, 0);
						MapContainer nullCont = new MapContainer();
						nullCont.setMap(nullMap);
						worldMapMatrix[rows][cols] = nullCont;
					}
				}
				System.out.println(worldMapMatrix[rows][cols].getMap().getNumberOfDoors());
			}
		}

	}


	private MapContainer[][] initMatrix() {
		//empty room doors thingy

		// South
		Point south = new Point(11/2, 11-1);
		// East
		Point east = new Point(11-1, 11/2);
		// North
		Point north = new Point(11/2, 0);
		// West
		Point west = new Point(0, 11/2);

		MapContainer[][] worldMapMatrix3 = new MapContainer[size][size];
		int nbrDoors = 4;
		GeneratorConfig gc = null;
		try {
			gc = new GeneratorConfig();

		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (int rows = 0; rows < size; rows++) {
			for (int cols = 0; cols < size; cols++) {
				Map tempMap = null;
				// 1
				if (rows == 0 && cols == 0) {
					tempMap = new Map(gc, 11, 11, null, east, south, null);
					System.out.println("1");
				}
				// 3
				if (rows == 0 && cols == (size - 1)) {
					tempMap = new Map(gc, 11, 11, null, null, south, west);
					System.out.println("3");
				}
				// 7
				if (rows == (size - 1) && cols == 0) {
					tempMap = new Map(gc, 11, 11, north, east, null, null);
					System.out.println("7");
				}
				// 9
				if (rows == (size - 1) && cols == (size - 1)) {
					tempMap = new Map(gc, 11, 11, north, null, null, west);
					System.out.println("9");
				}
				// top
				if (rows == 0 && cols != (size - 1) && cols != 0) {
					tempMap = new Map(gc, 11, 11, null, east, south, west);
					System.out.println("top");
				}
				// left
				if (rows != 0 && cols == 0 && rows != (size - 1)) {
					tempMap = new Map(gc, 11, 11, north, east, south, null);
					System.out.println("left");
				}
				// right
				if (rows != 0 && rows != (size - 1) && cols == (size - 1)) {
					tempMap = new Map(gc, 11, 11, north, null, south, west);
					System.out.println("right");
				}
				// bottom
				if (cols != 0 && cols != (size - 1) && rows == (size - 1)) {
					tempMap = new Map(gc, 11, 11, north, east, null, west);
					System.out.println("bottom");
				}
				// other
				else if (cols != 0 && cols != (size - 1) && rows != 0 && rows != (size - 1)) {
					tempMap = new Map(gc, 11, 11, north, east, south, west);
					System.out.println("other");
				}

				MapContainer temp = new MapContainer();
				temp.setMap(tempMap);
				worldMapMatrix3[rows][cols] = temp;


			}
		}
		return worldMapMatrix3;
	}

	private void createWorldMatrix() {
		//START OF MATRIX STUFF		
		//fill matrix
		for (MapContainer[] outer : worldMapMatrix) {
			for (int i = 0; i < outer.length; i++) {
				outer[i] = quadMap1;
			}
		}								
	}


	private String matrixToString() {
		//create large string
		String largeString = "";
		int j = 1;

		for (MapContainer[] outer : worldMapMatrix) {

			for (int k = 0; k < outer[0].getMap().toString().length(); k++) {

				if (outer[0].getMap().toString().charAt(k) != '\n') {
					largeString += outer[0].getMap().toString().charAt(k);

				}
				if (outer[0].getMap().toString().charAt(k) == '\n') {
					while (j < size) {

						for (int i = (k - 11); i < k; i++) {
							largeString += outer[j].getMap().toString().charAt(i);

						}
						j++;
					}
					j = 1;
					largeString += outer[0].getMap().toString().charAt(k);
				}

			}

		}
		return largeString;
	}


	private MapContainer[][] updateLargeMap(String loadedMap) {


		String largeString = loadedMap;
		//fill matrix from string
		int charNbr = 0;
		while (largeString.charAt(charNbr) != '\n') {
			charNbr++;
		}
		int actualCharNbr = charNbr / 11;
		MapContainer[][] worldMapMatrix2 = new MapContainer[actualCharNbr][actualCharNbr];
		String[] stringArray = new String[actualCharNbr];

		for (int s = 0; s < stringArray.length; s++) {
			stringArray[s] = "";
		}

		int p = 0;
		int charAmount = 0;
		int newLineCount = 0;
		int q = 0;

		while (q < actualCharNbr) {

			for (int i = 0; i < largeString.length(); i++) {

				if (largeString.charAt(i) == '\n') {
					newLineCount++;
					for (int s = 0; s < stringArray.length; s++) {
						stringArray[s] += largeString.charAt(i);
					}

					if ((newLineCount%11) == 0) {

						for (int s = 0; s < stringArray.length; s++) {
							MapContainer helpContainer = new MapContainer();
							helpContainer.setMap(Map.fromString(stringArray[s]));

							worldMapMatrix2[q][s] = helpContainer;
							stringArray[s] = "";

						}
						q++;

					}

					p = 0;
					charAmount = 0;
				}

				if ((charAmount%11) == 0 && charAmount != 0) {
					p++;
				}
				if (largeString.charAt(i) != '\n') {
					charAmount++;

					stringArray[p] += largeString.charAt(i);
				}

			}
		}

		return worldMapMatrix2;
	}

	private MapContainer getCurrentQuadMap() {
		return currentQuadMap;
	}

	private void setCurrentQuadMap(MapContainer currentQuadMap) {
		this.currentQuadMap = currentQuadMap;
	}



}
