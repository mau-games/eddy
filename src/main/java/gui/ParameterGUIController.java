package gui;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
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

import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import finder.patterns.micro.Connector;
import finder.patterns.micro.Corridor;
import finder.patterns.micro.Chamber;
import game.ApplicationConfig;
import game.Room;
import gui.controls.LabeledTextField;
import gui.controls.NumberTextField;
import gui.controls.PatternInstanceControl;
import gui.utils.MapRenderer;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseEvent;
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
public class ParameterGUIController implements Initializable, Listener {
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

	final static Logger logger = LoggerFactory.getLogger(ParameterGUIController.class);
	private static EventRouter router = EventRouter.getInstance();
	private ApplicationConfig config;
	
	private Room currentMap;
	private GraphicsContext ctx;
	private MapRenderer renderer = MapRenderer.getInstance();
	
	private List<Pattern> micropatterns;
	private List<CompositePattern> mesopatterns;
	private List<CompositePattern> macropatterns;
	private IdentityHashMap<Pattern, Color> activePatterns = new IdentityHashMap<Pattern, Color>();
	
	private boolean render = false;
	private boolean renderPatterns = false;

	/**
	 * Creates an instance of GUIController. This method is implicitly called
	 * when the GUI is created.
	 */
	public ParameterGUIController() {
		try {
			config = config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
			logger.error("Couldn't read config: " + e.getMessage());
		}
		Platform.runLater(() -> {
			ctx = mapCanvas.getGraphicsContext2D();
		});
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
		renderPatterns = true;
		selectAllPatterns();
	}
	
	protected void selectAllPatterns(){
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
		renderPatterns = false;
		deselectAllPatterns();
	}
	
	protected void deselectAllPatterns(){
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
				if(renderPatterns){
					selectAllPatterns();
				} else {
					deselectAllPatterns();
				}
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
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		router.registerListener(this, new MapUpdate(null));
		router.registerListener(this, new StatusMessage(null));
		router.registerListener(this, new AlgorithmDone(null));
		router.registerListener(this, new RequestRedraw());
		messageDisplayer.setText("Awaiting commands");
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof RequestRedraw) {
			restoreMap();
		} else if (e instanceof MapUpdate) {
			currentMap = (Room) e.getPayload();
			restoreMap();
		} else if (e instanceof StatusMessage) {
			String message = (String) e.getPayload();
			if (message != null) {
				Platform.runLater(() -> {
					addMessage(message);
				});
			}
		} else if (e instanceof AlgorithmDone) {
			HashMap<String, Object> result = (HashMap<String, Object>) ((AlgorithmDone) e).getPayload();
			micropatterns = (List<Pattern>) result.get("micropatterns");
			mesopatterns = (List<CompositePattern>) result.get("mesopatterns");
			macropatterns = (List<CompositePattern>) result.get("macropatterns");
			Platform.runLater(() -> {
				runButton.setDisable(false);
				cancelButton.setDisable(true);
				populatePatternList();
			});
		}
	}

	/**
	 * Reads the full config tree and builds a GUI to handle it.
	 */
	private void readAndBuildConfig() {
		addToConfigPane(config.getInternalConfig().getTree(), configSlab, "");
	}

	/**
	 * Displays a message in the message console.
	 * 
	 * @param message The message to display
	 */
	private synchronized void addMessage(String message) {
		messageDisplayer.setText(messageDisplayer.getText() + "\n" + message);
	}

	/**
	 * Draws a matrix on the canvas.
	 * 
	 * @param matrix A rectangular matrix of integers. Each integer corresponds
	 * 		to some predefined colour.
	 */
	private synchronized void drawMatrix(int[][] matrix) {
		Platform.runLater(() -> {
			if (render) {
				renderer.renderMap(ctx, matrix);
			} else {
				renderer.sketchMap(ctx, matrix);
			}
		});
	}
	
	/**
	 * Restores the map view to the latest map.
	 */
	private void restoreMap() {
		if (currentMap != null) {
			Platform.runLater(() -> {
				int[][] matrix = currentMap.toMatrix();
				if (render) {
					renderer.renderMap(ctx, matrix);
				} else {
					renderer.sketchMap(ctx, matrix);
				}
				renderer.drawPatterns(ctx, matrix, activePatterns);
				renderer.drawGraph(ctx, matrix, currentMap.getPatternFinder().getPatternGraph());				renderer.drawMesoPatterns(ctx, matrix, currentMap.getPatternFinder().findMesoPatterns());
				renderer.drawMesoPatterns(ctx, matrix, currentMap.getPatternFinder().findMesoPatterns());
			});
		}
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
				if (p instanceof Chamber) {
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
					TextField text = null;
					
					if (p.isString()) {
						text = new TextField();
						text.setText(p.getAsString());
						text.textProperty().addListener(new BurdenedChangeListenerer<String>(path + e.getKey(), Type.STRING));
					} else if (p.isNumber()) {
						text = new NumberTextField();
						text.setText("" + p.getAsNumber());
						text.textProperty().addListener(new BurdenedChangeListenerer<String>(path + e.getKey(), Type.NUMBER));
					}

					vbox.getChildren().add(new LabeledTextField(e.getKey(), text));
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
				config.getInternalConfig().updateValue(path, strVal);
				break;
			case NUMBER:
				strVal = (String) newValue;
				if (strVal.length() == 0) {
					strVal = "0";
				}
				if (strVal.contains(".")) {
					config.getInternalConfig().updateValue(path, Double.parseDouble(strVal));
				} else {
					config.getInternalConfig().updateValue(path, Integer.parseInt(strVal));
				}
				break;
			case BOOLEAN:
				boolVal = (Boolean) newValue;
				config.getInternalConfig().updateValue(path, boolVal);
				break;
			default:
				break;
			}
		}
		
	}
}
