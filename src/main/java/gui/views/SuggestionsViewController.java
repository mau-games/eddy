package gui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import game.MapContainer;
import game.Room;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestRoomView;

/**
 * This class controls the interactive application's start view. NO
 * 
 * @author Johan Holmberg, Malmö University
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 */
public class SuggestionsViewController extends GridPane implements Listener {

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
		this.layout();
		System.out.println(this.getWidth());
		System.out.println(this.getPrefWidth());
		System.out.println(this.getMinWidth());
		System.out.println(this.getMaxWidth());
		System.out.println(this.widthProperty().get());
		System.out.println(this.getBoundsInLocal().getWidth());
		System.out.println(this.getBoundsInLocal().getMaxX());
		System.out.println(this.getLayoutBounds().getWidth());
		System.out.println(this.getLayoutBounds().getMaxX());
		System.out.println(getBoundsInParent().getWidth());
		System.out.println(getBoundsInParent().getMaxX());
		System.out.println(getBoundsInParent());
		
		Platform.runLater(() -> {
			
			
			
			
			System.out.println(getBoundsInParent());
			double width = getBoundsInParent().getWidth();
			double height = getBoundsInParent().getHeight();
			double procent = height/width;
			
			System.out.println(width + ", " + height + " , " + procent);
		});
		
		setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		
		for(SuggestedNode node : suggestedRooms)
		{
			node.setReadiness(false);
			node.setOriginalRoom(original);
//			node.getGraphicNode().setMinSize(width/3.0 * procent, height/2.0);
//			node.getGraphicNode().setMaxSize(width/3.0 * procent, height/2.0);
//			node.getGraphicNode().setPrefSize(width/3.0 * procent, height/2.0);
			node.getGraphicNode().draw(null);
			node.getGraphicNode().setText("Waiting for map...");
			System.out.println(node.getGraphicNode().getLayoutBounds());
			System.out.println(node.getGraphicNode().getBoundsInLocal());
			System.out.println(node.getGraphicNode().getBoundsInParent());
			node.getGraphicNode().setPrefSize(140, 140);
			node.getGraphicNode().setPrefHeight(100);
			node.getGraphicNode().setPrefWidth(100);
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

					suggestion.getGraphicNode().draw(renderer.renderMap(matrix));
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
	
	public class SuggestedNode
	{
		private LabeledCanvas graphicNode;
		private MapContainer suggestedRoomContainer;
		private Room originalRoom;
		private Node source;
		private boolean ready = false;
		
		public SuggestedNode(LabeledCanvas node)
		{
			this.graphicNode = node;
			this.graphicNode.addEventFilter(MouseEvent.MOUSE_ENTERED, new MouseEventH());
		}
		
		public class MouseEventH implements EventHandler<MouseEvent>
		{
			@Override
			public void handle(MouseEvent event) 
			{
				source = (Node)event.getSource();
				
				//1) Mouse enters the canvas of the room --> I can fire the event here, no?
				source.setOnMouseEntered(new EventHandler<MouseEvent>() {

		            @Override
		            public void handle(MouseEvent event) 
		            {
		            	highlight(true);
		            }

		        });
				
				//2) mouse is moved around the map
				source.setOnMouseMoved(new EventHandler<MouseEvent>() {

		            @Override
		            public void handle(MouseEvent event) 
		            {
		            }
		        });

				source.setOnMousePressed(new EventHandler<MouseEvent>() {

		            @Override
		            public void handle(MouseEvent event) 
		            {	            	
		            	if(ready)
		    			{
		    				originalRoom.applySuggestion(suggestedRoomContainer.getMap());
		    				suggestedRoomContainer.setMap(originalRoom);
		    				router.postEvent(new RequestRoomView(suggestedRoomContainer, 0, 0, null));
		    			}	
		            }
		        });
				
				source.setOnMouseReleased(new EventHandler<MouseEvent>() {

		            @Override
		            public void handle(MouseEvent event) 
		            {
		            }
		        });
				
				source.setOnMouseExited(new EventHandler<MouseEvent>() {

		            @Override
		            public void handle(MouseEvent event) 
		            {
		            	highlight(false);
		            }

		        });
			}
		}
		
		//TODO: CHANGE THIS 
		public void resizeCanvasForRoom(Room original) //ReINIT
		{
		}
		
		public LabeledCanvas getGraphicNode()
		{
			return graphicNode;
		}
	
		public MapContainer getSuggestedRoom()
		{
			return this.suggestedRoomContainer;
		}
		
		public Room getOriginalRoom()
		{
			return this.originalRoom;
		}
		
		public void setSuggestedRoomContainer(MapContainer suggestedRoomContainer)
		{
			this.suggestedRoomContainer = suggestedRoomContainer;
		}
		
		public void setOriginalRoom(Room originalRoom)
		{
			this.originalRoom = originalRoom;
		}
		
		public void setReadiness(boolean value) {ready = value;}

	    /**
	     * Highlights the control.
	     * 
	     * @param state True if highlighted, otherwise false.
	     */
	    private void highlight(boolean state)
	    {
    		if (state) {
        		graphicNode.setStyle("-fx-border-width: 2px; -fx-border-color: #6b87f9;");
        	} else {
        		graphicNode.setStyle("-fx-border-width: 0px; -fx-background-color:#2c2f33;");
        	}
	    }
	}
}
