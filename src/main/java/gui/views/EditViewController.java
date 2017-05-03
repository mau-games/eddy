package gui.views;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.Map;
import gui.InteractiveGUIController;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;

/**
 * his class controls the interactive application's edit view.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class EditViewController extends BorderPane implements Listener {
	
	@FXML private Canvas centralCanvas;
	@FXML private List<LabeledCanvas> maps;
	
	private boolean isActive = false;
	private Map currentMap = null;
	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	private final static Logger logger = LoggerFactory.getLogger(EditViewController.class);

	/**
	 * Creates an instance of this class.
	 */
	public EditViewController() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/interactive/EditView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		
		draw();
		router.registerListener(this, new MapUpdate(null));
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof MapUpdate) {
			if (isActive) {
				currentMap = (Map) e.getPayload();
				draw();
			}
		}
	}
	
	/**
	 * Gets one of the maps (i.e. a labeled view displaying a map) being under
	 * this object's control.
	 * 
	 * @param index An index of a map.
	 * @return A map if it exists, otherwise null.
	 */
	public LabeledCanvas getMap(int index) {
		return maps.get(index);
	}
	
	public void setActive(boolean state) {
		isActive = state;
	}
	
	public void updateMap(Map map) {
		currentMap = map;
	}
	
	public Map getCurrentMap() {
		return currentMap;
	}
	
	public Image getRenderedMap() {
		return centralCanvas.snapshot(null, new WritableImage((int) centralCanvas.getWidth(), (int) centralCanvas.getHeight()));
	}

	/**
	 * Draws stuff on the canvas. Useful only for testing at the moment...
	 */
	private void draw() {
		GraphicsContext ctx = centralCanvas.getGraphicsContext2D();
		
		if (currentMap == null) {
			ctx.setFill(Color.RED);
			ctx.fillRect(0, 0, 500, 500);
		} else {
			if (currentMap != null) {
				Platform.runLater(() -> {
					int[][] matrix = currentMap.toMatrix();
					renderer.renderMap(ctx, matrix);
//					renderer.drawPatterns(ctx, matrix, activePatterns);
//					renderer.drawGraph(ctx, matrix, currentMap.getPatternFinder().getPatternGraph());				renderer.drawMesoPatterns(ctx, matrix, currentMap.getPatternFinder().findMesoPatterns());
//					renderer.drawMesoPatterns(ctx, matrix, currentMap.getPatternFinder().findMesoPatterns());
				});
			}
		}
	}
}
