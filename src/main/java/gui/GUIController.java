package gui;

import java.net.URL;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import game.Map;
import game.TileTypes;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
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
import util.eventrouting.events.Start;
import util.eventrouting.events.StatusMessage;

/**
 * This class controls our fantastic GUI.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class GUIController implements Initializable, Listener {
	public enum Type {
		STRING, NUMBER, BOOLEAN;
	};
	
	@FXML private Text messageDisplayer;
	@FXML private Canvas mapCanvas;
	@FXML private Button runButton;
	@FXML private TitledPane messageSlab;
	@FXML private TitledPane configSlab;

	final static Logger logger = LoggerFactory.getLogger(GUIController.class);
	private static EventRouter router = EventRouter.getInstance();
	private ConfigurationUtility config;

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
	}

	/**
	 * Handles the run button's action events.
	 * 
	 * @param ev The action event that triggered this call. 
	 */
	@FXML
	protected void runButtonPressed(ActionEvent ev) {
		messageDisplayer.setText("");
		router.postEvent(new Start());
		runButton.setDisable(true);
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
		router.registerListener(this, new AlgorithmDone());
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

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				gc.setFill(getColour(matrix[i][j]));
				gc.fillRect(j * pWidth, i * pWidth, pWidth, pWidth);
			}
		}
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof MapUpdate) {
			Map map = (Map) e.getPayload();
			if (map != null) {
				Platform.runLater(() -> {
					drawMatrix(map.toMatrix());
				});
			}
		} else if (e instanceof StatusMessage) {
			String message = (String) e.getPayload();
			if (message != null) {
				Platform.runLater(() -> {
					addMessage(message);
				});
			}
		} else if (e instanceof AlgorithmDone) {
			runButton.setDisable(false);
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
			color = Color.LIGHTGRAY;
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
	 * Reads the full config tree and builds a GUI to handle it.
	 */
	private void readAndBuildConfig() {
		addToConfigPane(config.getTree(), configSlab, "");
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
