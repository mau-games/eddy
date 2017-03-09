package gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import finder.geometry.Bitmap;
import finder.geometry.Geometry;
import finder.geometry.Point;
import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import finder.patterns.micro.Connector;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Room;
import game.Map;
import game.TileTypes;
import gui.controls.NumberTextField;
import gui.controls.PatternInstanceControl;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestRedraw;
import util.eventrouting.events.Start;
import util.eventrouting.events.StatusMessage;
import util.eventrouting.events.Stop;

/**
 * This class controls our fantastic GUI.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class GUIController implements Initializable, Listener {
	public enum Type {
		STRING, NUMBER, BOOLEAN;
	};

	@FXML private Accordion patternAccordion;
	@FXML private Text messageDisplayer;
	@FXML private Canvas mapCanvas;
	@FXML private Button runButton;
	@FXML private Button cancelButton;
	@FXML private CheckBox renderMapBox;
	@FXML private TitledPane messageSlab;
	@FXML private TitledPane configSlab;

	final static Logger logger = LoggerFactory.getLogger(GUIController.class);
	private static EventRouter router = EventRouter.getInstance();
	private ConfigurationUtility config;
	
	private Map currentMap;
	private ArrayList<Image> tiles = new ArrayList<Image>();
	
	private List<Pattern> micropatterns;
	private List<CompositePattern> mesopatterns;
	private List<CompositePattern> macropatterns;
	private IdentityHashMap<Pattern, Color> activePatterns = new IdentityHashMap<Pattern, Color>();
	
	private double patternOpacity = 0;
	private boolean render = false;
	private int nbrOfTiles = 6;

	/**
	 * Creates an instance of GUIController. This method is implicitly called
	 * when the GUI is created.
	 */
	public GUIController() {
		try {
			config = ConfigurationUtility.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read config: " + e.getMessage());
		}
		
		patternOpacity = config.getDouble("map.visual.pattern_opacity");
		
		// Set up the image list
		for (int i = 0; i < nbrOfTiles; i++) {
			tiles.add(i, null);
		}
	}

	/**
	 * Handles the run button's action events.
	 * 
	 * @param ev The action event that triggered this call. 
	 */
	@FXML
	protected void runButtonPressed(ActionEvent ev) {
		messageDisplayer.setText("");
		activePatterns.clear();
		runButton.setDisable(true);
		cancelButton.setDisable(false);
		router.postEvent(new Start());
	}

	/**
	 * Handles the cancel button's action events.
	 * 
	 * @param ev The action event that triggered this call. 
	 */
	@FXML
	protected void cancelButtonPressed(ActionEvent ev) {
		router.postEvent(new Stop());
		runButton.setDisable(false);
		cancelButton.setDisable(true);
	}
	
	/**
	 * Handles the select all patterns button's action events.
	 * 
	 * @param ev The action event that triggered this call.
	 */
	@FXML
	protected void selectAllPatternsButtonPressed(ActionEvent ev) {
		PatternInstanceControl pic = null;
		for (TitledPane pane : patternAccordion.getPanes()) {
			VBox box = (VBox) ((ScrollPane) pane.getContent()).getContent();
			for (Node node : box.getChildren()) {
				if (node instanceof PatternInstanceControl) {
					pic = (PatternInstanceControl) node;
					Platform.runLater(() -> {
						((PatternInstanceControl) node).setSelected(true);
					});
					activePatterns.put(pic.getPattern(), pic.getColour());
				}
			}
		}
		
		restoreMap();
	}
	
	/**
	 * Handles the de-select all patterns button's action events.
	 * 
	 * @param ev The action event that triggered this call.
	 */
	@FXML
	protected void deselectAllPatternsButtonPressed(ActionEvent ev) {
		activePatterns.clear();
		for (TitledPane pane : patternAccordion.getPanes()) {
			VBox box = (VBox) ((ScrollPane) pane.getContent()).getContent();
			for (Node node : box.getChildren()) {
				if (node instanceof PatternInstanceControl) {
					Platform.runLater(() -> {
						((PatternInstanceControl) node).setSelected(false);
					});
				}
			}
		}
		
		restoreMap();
	}
	
	/**
	 * Handles the render map check box.
	 * 
	 * @param ev The action event that triggered this call.
	 */
	@FXML
	protected void renderMapBoxToggled(ActionEvent ev) {
		render = renderMapBox.isSelected();
		
		if (currentMap != null) {
			Platform.runLater(() -> {
				drawMatrix(currentMap.toMatrix());
			});
		}
	}

	/**
	 * Handles the config slab's action events.
	 * 
	 * @param ev The action that triggered this call.
	 */
	@FXML
	protected void configSlabPressed(MouseEvent ev) {
		readAndBuildConfig();
	}

	/**
	 * Handles the map slab's action events.
	 * 
	 * @param ev The action that triggered this call.
	 */
	@FXML
	protected void mapSlabPressed(MouseEvent ev) {
//		populatePatternList();
	}

	/**
	 * Displays a message in the message console.
	 * 
	 * @param message The message to display
	 */
	private synchronized void addMessage(String message) {
		messageDisplayer.setText(messageDisplayer.getText() + "\n" + message);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		router.registerListener(this, new MapUpdate(null));
		router.registerListener(this, new StatusMessage(null));
		router.registerListener(this, new AlgorithmDone(null, null, null));
		router.registerListener(this, new RequestRedraw());
		messageDisplayer.setText("Awaiting commands");
	}

	/**
	 * Draws a matrix on the canvas.
	 * 
	 * @param matrix A rectangular matrix of integers. Each integer corresponds
	 * 		to some predefined colour.
	 */
	public synchronized void drawMatrix(int[][] matrix) {
		int m = matrix.length;
		int n = matrix[0].length;
		int pWidth = (int) Math.floor(mapCanvas.getWidth() / Math.max(m, n));
		GraphicsContext gc = mapCanvas.getGraphicsContext2D();
		Image image = null;

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				if (render) {
					image = getTileImage(matrix[i][j]);
					gc.drawImage(image, i * pWidth, j * pWidth, pWidth, pWidth);
				} else {
					gc.setFill(getColour(matrix[i][j]));
					gc.fillRect(i * pWidth, j * pWidth, pWidth, pWidth);
				}
			}
		}
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof RequestRedraw) {
			restoreMap();
		} else if (e instanceof MapUpdate) {
			currentMap = (Map) e.getPayload();
			restoreMap();
		} else if (e instanceof StatusMessage) {
			String message = (String) e.getPayload();
			if (message != null) {
				Platform.runLater(() -> {
					addMessage(message);
				});
			}
		} else if (e instanceof AlgorithmDone) {
			micropatterns = ((AlgorithmDone) e).micropatterns;
			mesopatterns = ((AlgorithmDone) e).mesopatterns;
			macropatterns = ((AlgorithmDone) e).macropatterns;
			Platform.runLater(() -> {
				runButton.setDisable(false);
				populatePatternList();
			});
		}
	}

	/**
	 * Selects a colour based on the pixel's integer value.
	 * 
	 * @param pixel The pixel to select for.
	 * @return A selected colour code.
	 */
	private Color getColour(int pixel) {
		Color color = null;

		switch (TileTypes.toTileType(pixel)) {
		case DOOR:
			color = Color.BLACK;
			break;
		case TREASURE:
			color = Color.YELLOW;
			break;
		case ENEMY:
			color = Color.RED;
			break;
		case WALL:
			color = Color.DARKSLATEGRAY;
			break;
		case FLOOR:
			color = Color.WHITE;
			break;
		case DOORENTER:
			color = Color.MAGENTA;
			break;
		default:
			color = Color.WHITE;
		}

		return color;
	}

	/**
	 * Selects a tile image based on the pixel's integer value.
	 * 
	 * @param pixel The pixel to select for.
	 * @return A file.
	 */
	private Image getTileImage(int pixel) {
		Image image = tiles.get(pixel);

		if (image == null) {
			switch (TileTypes.toTileType(pixel)) {
			case DOOR:
				image = new Image("/" + config.getString("map.visual.tiles.door"));
				break;
			case TREASURE:
				image = new Image("/" + config.getString("map.visual.tiles.treasure"));
				break;
			case ENEMY:
				image = new Image("/" + config.getString("map.visual.tiles.enemy"));;
				break;
			case WALL:
				image = new Image("/" + config.getString("map.visual.tiles.wall"));;
				break;
			case FLOOR:
				image = new Image("/" + config.getString("map.visual.tiles.floor"));;
				break;
			case DOORENTER:
				image = new Image("/" + config.getString("map.visual.tiles.doorenter"));;
				break;
			default:
				image = null;
			}
			tiles.add(pixel, image);
		}

		return image;
	}

	/**
	 * Reads the full config tree and builds a GUI to handle it.
	 */
	private void readAndBuildConfig() {
		addToConfigPane(config.getTree(), configSlab, "");
	}
	
	/**
	 * Restores the map view to the latest map.
	 */
	private void restoreMap() {
		patternOpacity = config.getDouble("map.visual.pattern_opacity");
		if (currentMap != null) {
			Platform.runLater(() -> {
				// First, draw the raw map
				drawMatrix(currentMap.toMatrix());
				
				// Now, let's put patterns and stuff on it
				for (Entry<Pattern, Color> e : activePatterns.entrySet()) {
					Platform.runLater(() -> {
						outlinePattern(e.getKey(), e.getValue());
					});
				}
			});
		}
	}
	
	/**
	 * Outlines a pattern onto the map.
	 * 
	 * @param p The pattern.
	 * @param c The color of the pattern's outline.
	 */
	private void outlinePattern(Pattern p, Color c) {
		Geometry g = p.getGeometry();
		
		if (g instanceof Point) {
			outlinePoint((Point) g, c);
		} else if (g instanceof Bitmap) {
			for (Point point : ((finder.geometry.Polygon) g).getPoints()) {
				outlinePoint(point, c);
			}
		} else if (g instanceof finder.geometry.Rectangle) {
			outlineRectangle((finder.geometry.Rectangle) g, c);
		}
	}
	
	/**
	 * Outlines a point on the map
	 * 
	 * @param p The point to outline.
	 * @param c The colour of the point's outline.
	 */
	private void outlinePoint(Point p, Color c) {
		int[][] matrix = currentMap.toMatrix();
		int m = matrix.length;
		int n = matrix[0].length;
		int pWidth = (int) Math.floor(mapCanvas.getWidth() / Math.max(m, n));
		
		drawRectangle(
				p.getX() * pWidth,
				p.getY() * pWidth,
				pWidth - 1,
				pWidth - 1,
				c);
	}
	
	/**
	 * Outlines a rectangle on the map.
	 * 
	 * @param r The rectangle to outline.
	 * @param c The colour of the rectangle's outline.
	 */
	private void outlineRectangle(finder.geometry.Rectangle r, Color c) {
		int[][] matrix = currentMap.toMatrix();
		int m = matrix.length;
		int n = matrix[0].length;
		int pWidth = (int) Math.floor(mapCanvas.getWidth() / Math.max(m, n));
		
		drawRectangle(
				r.getTopLeft().getX() * pWidth,
				r.getTopLeft().getY() * pWidth,
				(r.getBottomRight().getX() - r.getTopLeft().getX() + 1) * pWidth + pWidth - 1,
				(r.getBottomRight().getY() - r.getTopLeft().getY() + 1) * pWidth + pWidth - 1,
				c);
	}
	
	/**
	 * Draws a rectangle on the map.
	 * 
	 * @param x The x value of the first point.
	 * @param y The y value of the first point.
	 * @param width The x value of the second point.
	 * @param height The y value of the second point.
	 * @param c The colour of the outline.
	 */
	private synchronized void drawRectangle(int x, int y, int width, int height, Color c) {
		GraphicsContext gc = mapCanvas.getGraphicsContext2D();
		
		gc.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), patternOpacity));
		gc.setStroke(c);
		gc.setLineWidth(2);
		gc.fillRect(x, y, width, height);
		gc.strokeRect(x, y, width, height);
	}
	
	/**
	 * Populates the pattern list.
	 */
	private void populatePatternList() {
		patternAccordion.getPanes().clear();
		if (micropatterns != null) {
			// TODO: This doesn't scale very well, but works for now
			List<Node> rooms = new ArrayList<Node>();
			List<Node> connectors = new ArrayList<Node>();
			List<Node> corridors = new ArrayList<Node>();
			
			Color roomColour = Color.BLUE;
			Color connectorColour = Color.YELLOW;
			Color corridorColour = Color.RED;
			
			for (Pattern p : micropatterns) {
				if (p instanceof Room) {
					PatternInstanceControl pic =
							new PatternInstanceControl(rooms.size(),
									roomColour,
									p,
									activePatterns);
					rooms.add(pic);
					if (activePatterns.containsKey(p)) {
						pic.setSelected(true);
					}
					roomColour = roomColour.darker();
				} else if (p instanceof Connector) {
					PatternInstanceControl pic =
							new PatternInstanceControl(connectors.size(),
									connectorColour,
									p,
									activePatterns);
					connectors.add(pic);
					if (activePatterns.containsKey(p)) {
						pic.setSelected(true);
					}
					connectorColour = connectorColour.darker();
				} else if (p instanceof Corridor) {
					PatternInstanceControl pic =
							new PatternInstanceControl(corridors.size(),
									corridorColour,
									p,
									activePatterns);
					corridors.add(pic);
					if (activePatterns.containsKey(p)) {
						pic.setSelected(true);
					}
					corridorColour = corridorColour.darker();
				}
			}
			
			if (rooms.size() > 0) {
				patternAccordion.getPanes().add(new TitledPane("Rooms",
						new ScrollPane(new VBox(rooms.toArray(new Node[0])))));
			}
			
			if (corridors.size() > 0) {
				patternAccordion.getPanes().add(new TitledPane("Corridors",
						new ScrollPane(new VBox(corridors.toArray(new Node[0])))));
			}
			
			if (connectors.size() > 0) {
				patternAccordion.getPanes().add(new TitledPane("Connectors",
						new ScrollPane(new VBox(connectors.toArray(new Node[0])))));
			}
		}
	}

	/**
	 * Adds an object to the config pane and connects it to its corresponding
	 * config value.
	 * 
	 * @param o The object to add.
	 * @param slab The pane to add it to.
	 * @param path The parent's JSON path.
	 */
	private void addToConfigPane(JsonObject o, TitledPane slab, String path) {
		VBox vbox = new VBox();
		Accordion accordion = new Accordion();
		int aCount;
		aCount = 0;

		for (Entry<String, JsonElement> e : o.entrySet()) {
			if (e.getKey().equals("_comment")) {
				Text text = new Text(e.getValue().getAsString());
				text.setWrappingWidth(280);
				vbox.getChildren().add(text);
			} else if (e.getValue() instanceof JsonObject) {
				TitledPane title = new TitledPane(e.getKey(), null);
				title.prefHeightProperty().bind(accordion.heightProperty());
				accordion.getPanes().add(title);
				addToConfigPane(e.getValue().getAsJsonObject(), title, path + e.getKey() + ".");
				aCount++;
			} else if (e.getValue() instanceof JsonArray) {
				// TODO: Do stuff
			} else if (e.getValue() instanceof JsonPrimitive) {
				JsonPrimitive p = e.getValue().getAsJsonPrimitive();
				// TODO: Connect this value to the corresponding JSON value

				if (p.isBoolean()) {
					CheckBox cb = new CheckBox(e.getKey());
					cb.setSelected(p.getAsBoolean());
					cb.selectedProperty().addListener(new BurdenedChangeListenerer<Boolean>(path + e.getKey(), Type.BOOLEAN));
					vbox.getChildren().add(cb);
				} else {
					BorderPane bp = new BorderPane();
					Label label = new Label(e.getKey());
					TextField text = null;
					label.setLabelFor(text);
					
					if (p.isString()) {
						text = new TextField();
						text.setText(p.getAsString());
						text.textProperty().addListener(new BurdenedChangeListenerer<String>(path + e.getKey(), Type.STRING));
					} else if (p.isNumber()) {
						text = new NumberTextField();
						text.setText("" + p.getAsNumber());
						text.textProperty().addListener(new BurdenedChangeListenerer<String>(path + e.getKey(), Type.NUMBER));
					}

					bp.setLeft(label);
					bp.setRight(text);
					vbox.getChildren().add(bp);
				}
			}
		}

		if (aCount > 0) {
			accordion.prefHeightProperty().bind(vbox.heightProperty());
			vbox.getChildren().add(0, accordion);
		}
		vbox.prefHeightProperty().bind(slab.heightProperty());
		slab.setContent(vbox);
	}
	
	/**
	 * This class is used to carry important information regarding changed
	 * configuration.
	 * 
	 * @author Johan Holmberg
	 *
	 * @param <T>
	 */
	private class BurdenedChangeListenerer<T> implements ChangeListener<T> {
		
		private String path;
		private Type type;
		
		public BurdenedChangeListenerer(String path, Type type) {
			this.path = path;
			this.type = type;
		}

		@Override
		public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
			String strVal = null;
			boolean boolVal = false;
			
			switch (type) {
			case STRING:
				strVal = (String) newValue;
				config.updateValue(path, strVal);
				break;
			case NUMBER:
				strVal = (String) newValue;
				if (strVal.length() == 0) {
					strVal = "0";
				}
				if (strVal.contains(".")) {
					config.updateValue(path, Double.parseDouble(strVal));
				} else {
					config.updateValue(path, Integer.parseInt(strVal));
				}
				break;
			case BOOLEAN:
				boolVal = (boolean) newValue;
				config.updateValue(path, boolVal);
				break;
			default:
				break;
			}
		}
		
	}
}
