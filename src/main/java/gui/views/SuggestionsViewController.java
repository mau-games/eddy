package gui.views;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import game.MapContainer;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestRoomView;
import util.eventrouting.events.RequestSuggestionsView;

/**
 * This class controls the interactive application's start view.
 * 
 * @author Johan Holmberg, Malmö University
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 */
public class SuggestionsViewController extends GridPane implements Listener {

	@FXML private List<LabeledCanvas> mapDisplays;

	private boolean isActive = false;
	private HashMap<Integer, MapContainer> maps = new HashMap<Integer, MapContainer>();
	private int nextMap = 0;

	private Button worldViewButton = new Button();

	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	private MapContainer[][] worldMapMatrix;
	private int row;
	private int col;
	
	
	/**
	 * Creates an instance of this class.
	 */
	public SuggestionsViewController() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/interactive/SuggestionsView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		router.registerListener(this, new MapUpdate(null));
		router.registerListener(this, new AlgorithmDone(null));
		router.registerListener(this, new RequestSuggestionsView());
	}

	/**
	 * Initialises the controller for a new run.
	 */
	public void initialise() {
		nextMap = 0;
		getMapDisplay(0).draw(null);
		getMapDisplay(0).setText("Waiting for map...");

		getMapDisplay(1).draw(null);
		getMapDisplay(1).setText("Waiting for map...");

		getMapDisplay(2).draw(null);
		getMapDisplay(2).setText("Waiting for map...");

		getMapDisplay(3).draw(null);
		getMapDisplay(3).setText("Waiting for map...");

		getMapDisplay(4).draw(null);
		getMapDisplay(4).setText("Waiting for map...");

		getMapDisplay(5).draw(null);
		getMapDisplay(5).setText("Waiting for map...");

	}

	@Override
	public synchronized void ping(PCGEvent e) {

		if (e instanceof AlgorithmDone ) {
			if (isActive) {
				MapContainer container = (MapContainer) ((AlgorithmDone) e).getPayload(); 
				UUID uuid = ((AlgorithmDone) e).getID();
				LabeledCanvas canvas = mapDisplays.get(nextMap);
				//				canvas.setText("Got map:\n" + uuid);
				canvas.setText("");
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
		else if (e instanceof RequestSuggestionsView) {
			worldMapMatrix = ((RequestSuggestionsView) e).getMatrix();
			row = ((RequestSuggestionsView) e).getRow();
			col = ((RequestSuggestionsView) e).getCol();
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

	public Button getWorldViewButton() {
		return worldViewButton;
	}

	public void setWorldViewButton(Button worldViewButton) {
		this.worldViewButton = worldViewButton;
	}

	public class MouseEventHandler implements EventHandler<MouseEvent> {

		private MapContainer map;

		public MouseEventHandler(MapContainer map) {
			this.map = map;
		}

		@Override
		public void handle(MouseEvent event) {
			nextMap = 0;
			worldMapMatrix[row][col] = map;
			router.postEvent(new RequestRoomView(map, row, col, worldMapMatrix));
		}

	}
}