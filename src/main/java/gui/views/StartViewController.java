package gui.views;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.Map;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapUpdate;

/**
 * This class controls the interactive application's start view.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class StartViewController extends GridPane implements Listener {

	@FXML private List<LabeledCanvas> mapDisplays;
	
	private boolean isActive = false;
	private HashMap<UUID, Integer> uuidToDisplay = new HashMap<UUID, Integer>();
	private HashMap<UUID, Map> uuidToMap = new HashMap<UUID, Map>();
	private int nextMap = 0;
	
	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	private final static Logger logger = LoggerFactory.getLogger(StartViewController.class);

	/**
	 * Creates an instance of this class.
	 */
	public StartViewController() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/interactive/StartView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		router.registerListener(this, new MapUpdate(null));
		router.registerListener(this, new AlgorithmDone(null));
	}

	@Override
	public synchronized void ping(PCGEvent e) {
		if (e instanceof MapUpdate) {
			if (isActive) {
				Map map = (Map) ((MapUpdate) e).getPayload();
				UUID uuid = ((MapUpdate) e).getID();
				LabeledCanvas canvas = mapDisplays.get(nextMap);
				System.out.println("Got map: " + uuid);
				canvas.setText("Got map:\n" + uuid);
				uuidToDisplay.put(uuid, nextMap++);
				uuidToMap.put(uuid, map);
				
				Platform.runLater(() -> {
					int[][] matrix = map.toMatrix();
					canvas.draw(renderer.renderMap(matrix));
//					renderer.renderMap(mapDisplays.get(nextMap++).getGraphicsContext(), matrix);
//					renderer.drawPatterns(ctx, matrix, activePatterns);
//					renderer.drawGraph(ctx, matrix, currentMap.getPatternFinder().getPatternGraph());				renderer.drawMesoPatterns(ctx, matrix, currentMap.getPatternFinder().findMesoPatterns());
//					renderer.drawMesoPatterns(ctx, matrix, currentMap.getPatternFinder().findMesoPatterns());
				});
				
				// TODO: Attach event to display
			}
		}
	}
	
	public void setActive(boolean state) {
		isActive = state;
	}
	
	/**
	 * Gets one of the maps (i.e. a labeled view displaying a map) being under
	 * this object's control.
	 * 
	 * @param index An index of a map.
	 * @return A map if it exists, otherwise null.
	 */
	public LabeledCanvas getMapDisplay(int index) {
		return mapDisplays.get(index);
	}
}
