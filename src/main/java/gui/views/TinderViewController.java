package gui.views;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import game.ApplicationConfig;
import game.Room;
import game.TileTypes;
import game.Game.PossibleGAs;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.config.GeneratorConfig;
import gui.controls.Drawer;
import gui.controls.InteractiveMap;
import gui.controls.LabeledCanvas;
import gui.controls.Modifier;
import gui.controls.SuggestionRoom;
import gui.utils.MapRenderer;
import gui.views.RoomViewController.EvoState;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import machineLearning.NNPreferenceModel;
import machineLearning.PreferenceModel;
import util.Point;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.ApplySuggestion;
import util.eventrouting.events.MAPEGridUpdate;
import util.eventrouting.events.MAPElitesDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.StartGA_MAPE;
import util.eventrouting.events.Stop;
import util.eventrouting.events.SuggestedMapSelected;
import util.eventrouting.events.SuggestedMapsDone;

public class TinderViewController extends BorderPane implements Listener 
{

	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
//	private final static Logger logger = LoggerFactory.getLogger(RoomViewController.class);
	private ApplicationConfig config;
	
	@FXML private StackPane roomPane; 
	@FXML private VBox centerPane;
	@FXML private TextField userName;
	private List<Label> dimensionLabels;
	private InteractiveMap mapView;
	private Room originalRoom;
	private Room currentRoom;
	private GeneratorConfig gc;
	
	private ArrayList<Room> EARooms;
	private MAPEDimensionFXML[] currentDimensions;
	private PreferenceModel userPreferenceModel;
	
	private boolean isActive = false;
	
	private static DecimalFormat df2 = new DecimalFormat("#.##");
	private static DecimalFormat df3 = new DecimalFormat("#.###");
	public TinderViewController() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/ML/TinderView.fxml"));
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
		
		gc = null;
		try {
			gc = new GeneratorConfig();

		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		EARooms = new ArrayList<Room>();
		currentDimensions = new MAPEDimensionFXML[] {new MAPEDimensionFXML(DimensionTypes.SYMMETRY, 5), 
//													new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, 5),
//													new MAPEDimensionFXML(DimensionTypes.LINEARITY, 5),
													new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, 5)};
		
		dimensionLabels = new ArrayList<Label>();
		
		for(int i = 0; i < currentDimensions.length; i++)
		{
			Label x = new Label();
			x.setStyle("-fx-text-fill: white;");
			x.setAlignment(Pos.CENTER);
			dimensionLabels.add(x);
		}
		
		Label d1 = new Label();
		d1.setStyle("-fx-text-fill: white;");
		d1.setAlignment(Pos.CENTER);
		dimensionLabels.add(d1);
		
		Label d2 = new Label();
		d2.setStyle("-fx-text-fill: white;");
		d2.setAlignment(Pos.CENTER);
		dimensionLabels.add(d2);
		
		Label d3 = new Label();
		d3.setStyle("-fx-text-fill: white;");
		d3.setAlignment(Pos.CENTER);
		dimensionLabels.add(d3);
		
		Label d4 = new Label();
		d4.setStyle("-fx-text-fill: white;");
		d4.setAlignment(Pos.CENTER);
		dimensionLabels.add(d4);
		
		Label d5 = new Label();
		d5.setStyle("-fx-text-fill: white;");
		d5.setAlignment(Pos.CENTER);
		dimensionLabels.add(d5);
		
		Label d6 = new Label();
		d6.setStyle("-fx-text-fill: white;");
		d6.setAlignment(Pos.CENTER);
		dimensionLabels.add(d6);
		
		Label d7 = new Label();
		d7.setStyle("-fx-text-fill: white;");
		d7.setAlignment(Pos.CENTER);
		dimensionLabels.add(d7);
		
		
		
		centerPane.getChildren().addAll(dimensionLabels);
		originalRoom = new Room(null, gc, 7, 13, 30);
		originalRoom.setTile(0, 0, TileTypes.WALL);
		originalRoom.setTile(1, 0, TileTypes.WALL);
		originalRoom.setTile(0, 1, TileTypes.WALL);
		originalRoom.setTile(2, 0, TileTypes.WALL);
		originalRoom.setTile(0, 2, TileTypes.WALL);
		originalRoom.setTile(1, 1, TileTypes.WALL);
		originalRoom.setTile(12, 0, TileTypes.WALL);
		originalRoom.setTile(12, 1, TileTypes.WALL);
		originalRoom.setTile(11, 0, TileTypes.WALL);
		originalRoom.setTile(10, 0, TileTypes.WALL);
		originalRoom.setTile(12, 2, TileTypes.WALL);
		originalRoom.setTile(11, 1, TileTypes.WALL);
//		originalRoom.setTile(5, 0, TileTypes.WALL);
//		originalRoom.setTile(5, 1, TileTypes.WALL);
//		originalRoom.setTile(10, 10, TileTypes.WALL);
//		originalRoom.setTile(10, 0, TileTypes.WALL);
//		originalRoom.setTile(0, 10, TileTypes.WALL);
//		originalRoom.setTile(5, 5, TileTypes.WALL);
		originalRoom.setTile(1, 2, TileTypes.ENEMY);
		originalRoom.setTile(1, 3, TileTypes.TREASURE);
		originalRoom.createDoor(new Point(0, 5));
		originalRoom.createDoor(new Point(5, 6));
//		originalRoom.addDoor(new Point(5, 9));
		
//		userPreferenceModel = new PreferenceModel();
		userPreferenceModel = new NNPreferenceModel();

	}

	@Override
	public void ping(PCGEvent e) {
		//THIS NEED TO BE IMPROVED!
		if(e instanceof MAPElitesDone)
		{
			List<Room> generatedRooms = ((MAPElitesDone) e).GetRooms();
//			System.out.println("IT IS DONE");
			synchronized(EARooms)
			{
				EARooms.clear();
				
				for(Room gr : generatedRooms)
				{
					if(gr != null)
					{
						EARooms.add(gr);
					}
				}
			}
		}
	}

	
	public void SetView()
	{
		
		InitializeView();
		router.postEvent(new StartGA_MAPE(originalRoom, currentDimensions));
//		Arrays.stream(genotype.getChromosome()).boxed().map(x -> TileTypes.toTileType(x)).toArray(TileTypes[]::new);
		currentRoom = originalRoom;
		getMapView().updateMap(currentRoom); 
		SetStats();
	}
	
	public void SetStats()
	{
		for(int i = 0; i < currentDimensions.length; i++)
		{
			dimensionLabels.get(i).setText(currentDimensions[i].getDimension() + ": " + 
											df2.format(currentRoom.getDimensionValue(currentDimensions[i].getDimension())));
		}
		
		dimensionLabels.get(dimensionLabels.size() - 7).setText("Enemy density: " + 
				df2.format(currentRoom.calculateEnemyDensity()));
		
		dimensionLabels.get(dimensionLabels.size() - 6).setText("Enemy sparsity: " + 
				df2.format(currentRoom.calculateEnemySparsity()));
		
		dimensionLabels.get(dimensionLabels.size() - 5).setText("Treasure density: " + 
				df2.format(currentRoom.calculateTreasureDensity()));
		
		dimensionLabels.get(dimensionLabels.size() - 4).setText("Treasure sparsity: " + 
				df2.format(currentRoom.calculateTreasureSparsity()));
		
		dimensionLabels.get(dimensionLabels.size() - 3).setText("Wall density: " + 
				df2.format(currentRoom.calculateWallDensity()));
		
		dimensionLabels.get(dimensionLabels.size() - 2).setText("Wall sparsity: " + 
				df2.format(currentRoom.calculateWallSparsity()));
		
		
		dimensionLabels.get(dimensionLabels.size() - 1).setText("");
	}
	
	public void InitializeView()
	{
		roomPane.getChildren().clear();

		setMapView(new InteractiveMap());
		StackPane.setAlignment(getMapView(), Pos.CENTER);
		getMapView().setMinSize(420, 420);
		getMapView().setMaxSize(420, 420);
		getMapView().setPrefSize(420, 420);
		roomPane.getChildren().add(getMapView());
	}
	
	public void onUserNameChanged()
	{
		
	}
	
	/***
	 * Generate rooms following the preference model!
	 */
	public void generateForMe()
	{
		//This should be to generate rooms but first it will be to test the room
		dimensionLabels.get(dimensionLabels.size() - 1).setText("Preference: " + 
				df3.format(userPreferenceModel.testWithPreference(currentRoom)));
		
//		userPreferenceModel.broadcastPreferences();
		
	}
	
	//When the user liked the image
	public void userDisliked()
	{
		System.out.println("dislike");
		System.out.println(EARooms.size());
		userPreferenceModel.UpdateModel(false, currentRoom);
		currentRoom = EARooms.get((int)(Math.random() * EARooms.size()));
		getMapView().updateMap(currentRoom); 
		SetStats();
//		getMapView().updateMap(currentRoom); 
	}
	
	//When the user liked the image
	public void userLiked()
	{
		System.out.println("like");
		System.out.println(EARooms.size());
		userPreferenceModel.UpdateModel(true, currentRoom);
		currentRoom = EARooms.get((int)(Math.random() * EARooms.size()));
		getMapView().updateMap(currentRoom); 
		SetStats();
	}
	
	public void showStats()
	{
//		router.postEvent(new Stop());
		userPreferenceModel.printAllStates();
		userPreferenceModel.SaveDataset(userName.getText());
	}
	
	public InteractiveMap getMapView() {
		return mapView;
	}

	public void setMapView(InteractiveMap roomView) {
		this.mapView = roomView;
	}

}
