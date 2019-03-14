package gui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import game.MapContainer;
import game.Room;
import gui.controls.LabeledCanvas;
import gui.controls.SuggestedNode;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapUpdate;

/**
 * This class controls the interactive application's start view. NO
 * 
 * @author Johan Holmberg, Malmö University
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 */
public class SuggestionsViewController extends AnchorPane implements Listener {

	@FXML private List<LabeledCanvas> mapDisplays;
	private ArrayList<SuggestedNode> suggestedRooms = new ArrayList<SuggestedNode>();

	private boolean isActive = false;
	private int nextMap = 0;

	private Button worldViewButton = new Button();

	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	
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
		router.registerListener(this, new AlgorithmDone(null, null));
		
		this.setPrefSize(1920, 1080);
		
		//Everything is loaded!
		for(LabeledCanvas canvas : mapDisplays) //limitations....
		{
			suggestedRooms.add(new SuggestedNode(canvas));
			System.out.println(this.getWidth());
			System.out.println(this.getPrefWidth());
			System.out.println(this.getMinWidth());
			System.out.println(this.getMaxWidth());
		}
	}

	/**
	 * Initialises the controller for a new run.
	 */
	public void initialise(Room original) {
		nextMap = 0;

		for(SuggestedNode node : suggestedRooms)
		{
			node.setReadiness(false);
			node.setOriginalRoom(original);
			node.getGraphicNode().draw(null);
			node.getGraphicNode().setText("Waiting for map...");
			node.resizeCanvasForRoom(original);
			LabeledCanvas.setAlignment(node.getGraphicNode(), Pos.CENTER);
		}
		
	}

	@Override
	public synchronized void ping(PCGEvent e) {

		if (e instanceof AlgorithmDone ) {
			if (isActive) {
				MapContainer container = (MapContainer) ((AlgorithmDone) e).getPayload(); 
				UUID uuid = ((AlgorithmDone) e).getID();
				SuggestedNode suggestion = getSuggestionsNode(nextMap);
				//				canvas.setText("Got map:\n" + uuid);
				suggestion.getGraphicNode().setText("");
				
				Platform.runLater(() -> {
					
					int[][] matrix = container.getMap().toMatrix();

					suggestion.getGraphicNode().draw(renderer.renderMiniSuggestedRoom(container.getMap(), nextMap));
					//					renderer.renderMap(mapDisplays.get(nextMap++).getGraphicsContext(), matrix);
					//					renderer.drawPatterns(ctx, matrix, activePatterns);
					//					renderer.drawGraph(ctx, matrix, currentMap.getPatternFinder().getPatternGraph());				renderer.drawMesoPatterns(ctx, matrix, currentMap.getPatternFinder().findMesoPatterns());
					//					renderer.drawMesoPatterns(ctx, matrix, currentMap.getPatternFinder().findMesoPatterns());
				});
				
				suggestion.setSuggestedRoomContainer(container);
				suggestion.setReadiness(true);
				nextMap++;
			}
		}
	}


	public void setActive(boolean state) {
		isActive = state;
	}
	
	/**
	 * Gets one of the suggestions node (i.e. a labeled view displaying a map and extra functionalities) being under
	 * this object's control.
	 * 
	 * @param index An index of a map.
	 * @return A map if it exists, otherwise null.
	 */
	public SuggestedNode getSuggestionsNode(int index){
		return suggestedRooms.get(index);
	}

	public Button getWorldViewButton() {
		return worldViewButton;
	}

	public void setWorldViewButton(Button worldViewButton) {
		this.worldViewButton = worldViewButton;
	}
}
