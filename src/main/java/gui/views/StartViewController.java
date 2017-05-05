package gui.views;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.Map;
import game.MapContainer;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestViewSwitch;

/**
 * This class controls the interactive application's start view.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class StartViewController extends GridPane implements Listener {

	@FXML private List<LabeledCanvas> mapDisplays;
	
	private boolean isActive = false;
	private HashMap<Integer, MapContainer> maps = new HashMap<Integer, MapContainer>();
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
		if (e instanceof AlgorithmDone) {
			if (isActive) {
				MapContainer container = (MapContainer) ((AlgorithmDone) e).getPayload(); 
				UUID uuid = ((AlgorithmDone) e).getID();
				LabeledCanvas canvas = mapDisplays.get(nextMap);
				canvas.setText("Got map:\n" + uuid);
				maps.put(nextMap, container);
				
				Platform.runLater(() -> {
					int[][] matrix = container.getMap().toMatrix();
					canvas.draw(renderer.renderMap(matrix));
//					renderer.renderMap(mapDisplays.get(nextMap++).getGraphicsContext(), matrix);
//					renderer.drawPatterns(ctx, matrix, activePatterns);
//					renderer.drawGraph(ctx, matrix, currentMap.getPatternFinder().getPatternGraph());				renderer.drawMesoPatterns(ctx, matrix, currentMap.getPatternFinder().findMesoPatterns());
//					renderer.drawMesoPatterns(ctx, matrix, currentMap.getPatternFinder().findMesoPatterns());
				});
				
				canvas.addEventFilter(MouseEvent.MOUSE_CLICKED,
						new MouseEventHandler(maps.get(nextMap)));
				nextMap++;
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
	
	private class MouseEventHandler implements EventHandler<MouseEvent> {
		
		private MapContainer map;
		
		public MouseEventHandler(MapContainer map) {
			this.map = map;
		}
		
		@Override
		public void handle(MouseEvent event) {
			router.postEvent(new RequestViewSwitch(map));
		}
		
	}
}
