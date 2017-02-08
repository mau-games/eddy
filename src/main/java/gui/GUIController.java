package gui;

import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.Map;
import game.TileTypes;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import util.config.ConfigurationReader;
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
	@FXML private Text messageDisplayer;
	@FXML private Canvas mapCanvas;
	@FXML private Button runButton;
	
	final static Logger logger = LoggerFactory.getLogger(GUIController.class);
	private static EventRouter router = EventRouter.getInstance();
	private ConfigurationReader config;
	
	/**
	 * Creates an instance of GUIController. This method is implicitly called
	 * when the GUI is created.
	 */
	public GUIController() {
		try {
			config = ConfigurationReader.getInstance();
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
	
	@FXML
	protected void configSlabPressed(MouseEvent ev) {
		// TODO: Read and set up the config in here
	}
	
	/**
	 * Displays a message in the message console.
	 * 
	 * @param message The message to display
	 */
	private synchronized void addMessage(String message) {
			messageDisplayer.setText(messageDisplayer.getText() + "\n" + message);
//		try {
//			messageDisplayer.setText(messageDisplayer.getText() + "\n" + message);
//		} catch (NegativeArraySizeException | NullPointerException e) {
//			// Gracefully ignore this, it doesn't really have any real effects
//			System.out.println("OOPS");
//		}
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
		case COIN:
		case COIN2:
		case COFFER:
		case COFFER2:
			color = Color.YELLOW;
			break;
		case ENEMY:
		case ENEMY2:
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
}
