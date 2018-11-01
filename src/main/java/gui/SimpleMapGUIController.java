package gui;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import finder.patterns.Pattern;
import finder.patterns.micro.Connector;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Chamber;
import game.ApplicationConfig;
import game.Game;
import game.Room;
import game.TileTypes;
import generator.algorithm.Algorithm;
import generator.config.GeneratorConfig;
import gui.utils.MapRenderer;
import gui.views.EditViewController;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.ComboBox;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import util.Point;
import util.Util;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapLoaded;
import util.eventrouting.events.RequestRedraw;
import util.eventrouting.events.StatusMessage;

/***
 * A SIMPLE GUI CONTROLLER USED TO LEARN EDDY and FXML AND ALL OF THAT!
 *TODO: I SHOULD OR DELETE THIS OR IMPROVE IT FOR NEW COMERS TO SEE BASIC FUNCTIONALITIES OF THE TOOL
 *TODO: IN A CONTROLLED ENVIRONMENT!!
 * @author Alberto Alvarez, Malmö University
 *
 */
public class SimpleMapGUIController  implements Initializable, Listener 
{
	private int CANVAS_WIDTH = 420;
	private int CANVAS_HEIGHT = 420;
	
	private int roomHeight = 11;
	private int roomWidth = 12;
	
	private int[] manual_map = {0,0,1,0,0,0,0,0,1,0,0,2,
								0,0,0,0,0,1,0,0,0,0,0,2,
								0,0,0,0,1,1,1,0,1,1,1,2,
								0,0,0,1,0,1,0,0,0,0,0,2,
								0,0,1,0,0,1,0,0,0,0,0,2,
								1,1,1,1,1,0,0,0,0,0,0,2,
								1,0,1,0,0,0,1,0,0,0,0,2,
								0,1,1,0,0,0,0,1,0,0,0,2,
								0,0,0,0,1,1,0,0,1,0,0,2,
								0,0,0,1,0,0,0,0,0,1,0,2,
								0,0,0,0,1,0,0,0,0,1,0,2};
	
	@FXML private StackPane mainPane;
	@FXML private ComboBox<String> DisplayCombo;
	@FXML private ToggleButton patternButton;
	@FXML private ToggleButton zoneButton;
	@FXML private Slider zoneSlider;
	
	private GridPane mapPane;
	
	private Room currentMap; //example map to perform evo on
	private GeneratorConfig basicConfig;
	private final MapRenderer renderer = MapRenderer.getInstance(); //neeeded
	private Canvas patternCanvas ;
	private Canvas zoneCanvas ;

	EventHandler<MouseEvent> mouseEventHandler = null;
	
	final static Logger logger = LoggerFactory.getLogger(InteractiveGUIController.class);
	private static EventRouter router = EventRouter.getInstance();
	private ApplicationConfig config;

	@Override
	public void ping(PCGEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		try {
			config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read config file.");
		}
		
		router.registerListener(this, new StatusMessage(null));
		router.registerListener(this, new AlgorithmDone(null, null));
		router.registerListener(this, new RequestRedraw());
		router.registerListener(this, new MapLoaded(null));


		SetUpWindowStack();

		zoneSlider.valueProperty().addListener((obs, oldval, newVal) -> { 
		redrawPatterns(currentMap);
		});
		
	}
	
	private void SetConfiguration()
	{
		String c = "config/generator_config.json";
		try {
			basicConfig = new GeneratorConfig(c);
		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void SetUpWindowStack()
	{
		SetConfiguration();
		
		mapPane = new GridPane();
		mapPane.setMinSize(CANVAS_WIDTH, CANVAS_HEIGHT);
		mapPane.setMaxSize(CANVAS_WIDTH, CANVAS_HEIGHT);
		StackPane.setAlignment(mapPane, Pos.CENTER);
		mainPane.getChildren().add(mapPane);
//		
//		
		zoneCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
		StackPane.setAlignment(zoneCanvas, Pos.CENTER);
		mainPane.getChildren().add(zoneCanvas);
		zoneCanvas.setVisible(false);
		zoneCanvas.setMouseTransparent(true);
		
		patternCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
		StackPane.setAlignment(patternCanvas, Pos.CENTER);
		mainPane.getChildren().add(patternCanvas);
		patternCanvas.setVisible(false);
		patternCanvas.setMouseTransparent(true);
	}
	
	private void InitializeSampleMap(boolean manual)
	{

		TileTypes[] ex = new TileTypes[roomWidth * roomHeight];
		
		if(manual) //Fill the map with the manual map
		{
			for(int y = 0; y < roomHeight; y++)
			{
				for(int x = 0; x < roomWidth; x++)
				{
					ex[y * roomWidth + x] = TileTypes.toTileType(manual_map[y * roomWidth + x]);
				}
			}
		}
		else 		//Fill a sample map with random 
		{
			for(int y = 0; y < roomHeight; y++)
			{
				for(int x = 0; x < roomWidth; x++)
				{
					ex[y * roomWidth + x] = TileTypes.toTileType(Util.getNextInt(0, 6));
				}
			}
		}

//		
//		for(int y = 0; y < roomHeight; y++)
//		{
//			for(int x = 0; x < roomWidth; x++)
//			{
//				System.out.print(ex[y * roomWidth + x] );
//			}
//			
//			System.out.println();
//		}
		
//		System.out.println();
		currentMap = new Room(basicConfig, ex, roomHeight, roomWidth, /*Doors positions*/ null, /*Entrance position*/ null);
		RenderMap(currentMap);
	}
	
	//Renders any map object you send to it
	private void RenderMap(Room room)
	{
		mapPane.autosize();
		int cols = room.getColCount();
		int rows = room.getRowCount();
		double width = mapPane.getWidth() / cols;
		double height = mapPane.getHeight() / rows;
		double scale = Math.min(width, height);

		mapPane.getChildren().clear();
//		coords.clear();
		
		for (int j = 0; j < rows; j++){ //y
			for (int i = 0; i < cols; i++)  { //x
				ImageView iv = new ImageView(getImage(room.getTile(i, j).GetType(), scale));
				GridPane.setFillWidth(iv, true);
				GridPane.setFillHeight(iv, true);
				mapPane.add(iv, i,j);
//				coords.put(iv, new Point(i, j));
			}
		}
	}
	
	private Image getImage(TileTypes type, double size)
	{
//		Image tile = images.get(type);
//		
//		if (tile != null) {
//			return tile;
//		}
//		
//		tile = renderer.renderTile(type, size, size);
//		images.put(type, tile);
		
		return renderer.renderTile(type, size, size);
	}
	
	@FXML public void OnChange() 
	{
		switch(DisplayCombo.getValue())
		{
		case "Example":
				InitializeSampleMap(true);
				break;
		case "RndExample":
				InitializeSampleMap(false);
				break;
		case "Grammar":
				break;
		case "evo":
				break;
			default:
				System.out.println("Something went wrong, the displayed value is " + DisplayCombo.getValue());
				break;
		}
		
		redrawPatterns(currentMap);
	}
	
	@FXML public void ChangeDepth() {
		System.out.println(zoneSlider.getValue());
	}
	
	@FXML public void toggleZones() {
		if (zoneButton.isSelected()) {
			zoneCanvas.setVisible(true);
		} else {
			zoneCanvas.setVisible(false);
		}
	}
	
	@FXML public void togglePatterns()
	{
		if (patternButton.isSelected()) {
			patternCanvas.setVisible(true);
		} else {
			patternCanvas.setVisible(false);
		}
	}
	
	/****
	 * Redraw the patterns in the canvas 
	 * @param room Extract the patterns from the map
	 */
	private synchronized void redrawPatterns(Room room)
	{
		patternCanvas.getGraphicsContext2D().clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
		zoneCanvas.getGraphicsContext2D().clearRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
		renderer.drawPatterns(patternCanvas.getGraphicsContext2D(), room.toMatrix(), colourPatterns(room.getPatternFinder().findMicroPatterns()));
		renderer.drawGraph(patternCanvas.getGraphicsContext2D(), room.toMatrix(), room.getPatternFinder().getPatternGraph());
		renderer.drawMesoPatterns(patternCanvas.getGraphicsContext2D(), room.toMatrix(), room.getPatternFinder().getMesoPatterns());
		renderer.drawZones(zoneCanvas.getGraphicsContext2D(), room.toMatrix(), room.root, (int)(zoneSlider.getValue()), Color.BLACK);
	}
	
	/**
	 * Collect the micro patterns
	 * @param patterns
	 * @return
	 */
	private HashMap<Pattern, Color> colourPatterns(List<Pattern> patterns) 
	{
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

}
