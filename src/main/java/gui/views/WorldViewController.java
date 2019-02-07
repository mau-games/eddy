package gui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import game.ApplicationConfig;
import game.Dungeon;
import game.DungeonPane;
import game.MapContainer;
import gui.controls.LabeledCanvas;
import gui.utils.DungeonDrawer;
import gui.utils.DungeonDrawer.DungeonBrushes;
import gui.utils.InterRoomBrush;
import gui.utils.MapRenderer;
import gui.utils.MoveElementBrush;
import gui.utils.RoomConnectorBrush;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestEmptyRoom;
import util.eventrouting.events.RequestNewRoom;
import util.eventrouting.events.RequestRoomRemoval;
import util.eventrouting.events.RequestRoomView;
import util.eventrouting.events.RequestSuggestionsView;
import util.IntField;
import util.Point;

/*  
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University*/

public class WorldViewController extends BorderPane implements Listener
{

	private ApplicationConfig config;
	private EventRouter router = EventRouter.getInstance();
	private boolean isActive = false;
	
	private ToggleButton interFeasibilityToggle = new ToggleButton();
	private Button suggestionsBtn = new Button();
	private Button createNewRoomBtn = new Button();
	private Button removeRoomBtn = new Button();
	private Button changeBrushBtn = new Button();
	
	private ArrayList<Button> brushBtns = new ArrayList<Button>(); //TEST
	
	private Label widthLabel = new Label("W =");
	private Label heightLabel = new Label("H =");
	private IntField widthField = new IntField(1, 20, 11);
	private IntField heightField = new IntField(1, 20, 11);

	private Canvas buttonCanvas;
	private MapRenderer renderer = MapRenderer.getInstance();

	@FXML private StackPane buttonPane;
	@FXML Pane worldPane;
	@FXML private List<LabeledCanvas> mapDisplays;

	private Node source;
	
	private Dungeon dungeon;
	
	double anchorX;
	double anchorY;

	//Line to appear when we try to draw conenctions between rooms (just visual feedback)
	private Line auxLine;
	
	public WorldViewController() {
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/interactive/WorldView.fxml"));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
			config = ApplicationConfig.getInstance();
		} catch (IOException ex) {

		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		router.registerListener(this, new MapUpdate(null));

		initWorldView();
	}

	public void setActive(boolean state) {
		isActive = state;
	}

	private void initWorldView() 
	{
		//setup visual line
		auxLine = new Line();
		auxLine.setStrokeWidth(2.0f);
		auxLine.setStroke(Color.PINK);
		auxLine.setMouseTransparent(true);
		
		buttonPane = new StackPane();

		//setting the parts of the border pane
		setCenter(worldPane);
		setRight(buttonPane);
		worldPane.addEventHandler(MouseEvent.MOUSE_PRESSED, new MouseEventWorldPane());

		//Don't allow children to pass over the pane!
		clipChildren(worldPane, 12);
		worldButtonEvents();
		initOptions();	
	}
	
	public void initWorldMap(Dungeon dungeon) 
	{
		
		if(widthField == null)
			widthField = new IntField(1, 20, dungeon.defaultWidth);
		
		if(heightField == null)
			heightField = new IntField(1, 20, dungeon.defaultHeight);
		
		this.dungeon = dungeon;
		worldPane.getChildren().clear();
		worldPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		
		dungeon.dPane.renderAll();
		worldPane.getChildren().add(dungeon.dPane);
	}
	
	public class MouseEventWorldPane implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
			
			source.setOnScroll(new EventHandler<ScrollEvent>()
			{

				@Override
				public void handle(ScrollEvent event) {
        			Scale newScale = new Scale();
        	        newScale.setPivotX(event.getX());
        	        newScale.setPivotY(event.getY());
        	        
        	        for(Node child : worldPane.getChildren()) 
            		{
            			newScale.setX( child.getScaleX() + (0.001 * event.getDeltaY()) );
            	        newScale.setY( child.getScaleY() + (0.001 * event.getDeltaY()) );
            	        ((DungeonPane)child).tryScale(newScale);
            		}

        			event.consume();
				}

			});
			
			source.setOnMouseDragged(new EventHandler<MouseEvent>() 
			{

	            @Override
	            public void handle(MouseEvent event)
	            {
	            	if(DungeonDrawer.getInstance().getBrush() instanceof InterRoomBrush)
	            	{
		    			auxLine.setEndX(event.getX());
		    			auxLine.setEndY(event.getY());
	            	}
	            	else if(event.getTarget() == worldPane && event.isMiddleButtonDown()) //TODO: WORK IN PROGRESS
	            	{
	            		for(Node child : worldPane.getChildren())
	            		{
	            			child.setLayoutX(child.getLayoutX() + (event.getX() - anchorX));
	            			child.setLayoutY(child.getLayoutY() + event.getY() - anchorY); 
	            		}
	            		
            			anchorX = event.getX();
            			anchorY = event.getY();
            			
            			event.consume();
	            	}
	            }
	            
	        });
			
			source.setOnMouseReleased(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	worldPane.getChildren().remove(auxLine);
	            }

	        });
			
			source.setOnMousePressed(new EventHandler<MouseEvent>() 
			{

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	anchorX = event.getX();
	    			anchorY = event.getY();
	    			
	            	if(DungeonDrawer.getInstance().getBrush() instanceof InterRoomBrush)
	            	{
		    			if(!worldPane.getChildren().contains(auxLine))
		    				worldPane.getChildren().add(auxLine);
		    			
		    			auxLine.setStartX(event.getX());
		    			auxLine.setStartY(event.getY());
		    			auxLine.setEndX(event.getX());
		    			auxLine.setEndY(event.getY());
	            	}
	            	else //Another brush
	            	{
	            		
	            	}
	            	
	            }
	        });
			
		}
	}
	
	private void clipChildren(Region region, double arc)
	{
		final Rectangle outputClip = new Rectangle();
	    outputClip.setArcWidth(arc);
	    outputClip.setArcHeight(arc);
	    region.setClip(outputClip);

	    region.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
	        outputClip.setWidth(newValue.getWidth());
	        outputClip.setHeight(newValue.getHeight());
	    });
	}

	private void initOptions() 
	{				
		buttonCanvas = new Canvas(500, 1000); //TODO This will stay like this but it shoudl be responsive!!
		StackPane.setAlignment(buttonCanvas, Pos.CENTER);
		buttonPane.setAlignment(Pos.CENTER);
		buttonPane.getChildren().add(buttonCanvas);
		buttonCanvas.setVisible(false);
		buttonCanvas.setMouseTransparent(true);
		
		//some calculations for the brushes
		double maxWidth = 250;
		double maxHeight = 50;
		double initXPos = -100;
		double yPos = 0;
		double widthPadding = 10;
		double btnWidthSize = (maxWidth / brushBtns.size()) - (widthPadding * brushBtns.size());
		double xStep = btnWidthSize + (btnWidthSize / 2.0) + widthPadding;
		
		
		//Arrange the controls
		
		arrangeControls(widthLabel, -90, -300, 30, 50);
		arrangeControls(heightLabel, 10, -300, 30, 50);
		arrangeControls(widthField, -55, -300, 50, 50);
		arrangeControls(heightField, 50, -300, 50, 50);
		arrangeControls(createNewRoomBtn, -100, -200, 120, 100);
		arrangeControls(removeRoomBtn, 100, -200, 120, 100);
		arrangeControls(getInterFeasibilityBtn(), 0, -100, 100, 50);
		arrangeControls(getSuggestionsBtn(), 0, 0, 300, 100);
//		arrangeControls(getChangeBrushBtn(), 0, 200, 300, 100);
		
		String[] btnsText = {"M", "C", "P"};
		for(int i = 0; i < brushBtns.size(); i++, initXPos += xStep)
		{
			arrangeControls(brushBtns.get(i), initXPos, 100, btnWidthSize, maxHeight);
			buttonPane.getChildren().add(brushBtns.get(i));
			brushBtns.get(i).setText(btnsText[i]);
		}
	
		//change color of the input fields!
		heightField.setStyle("-fx-text-inner-color: white;");		
		widthField.setStyle("-fx-text-inner-color: white;");
		widthLabel.setTextFill(Color.WHITE);
		heightLabel.setTextFill(Color.WHITE);
//		widthLabel.setTextFill(Paint);

		//Add everything to the button pane!
		buttonPane.getChildren().add(getInterFeasibilityBtn());
		buttonPane.getChildren().add(getSuggestionsBtn());
//		buttonPane.getChildren().add(changeBrushBtn);
		buttonPane.getChildren().add(createNewRoomBtn);
		buttonPane.getChildren().add(removeRoomBtn);
		buttonPane.getChildren().add(heightField);
		buttonPane.getChildren().add(widthField);
		buttonPane.getChildren().add(widthLabel);
		buttonPane.getChildren().add(heightLabel);
		
		//Change the text of the buttons!
		changeBrushBtn.setText("Change brush");
		createNewRoomBtn.setText("NEW ROOM");
		removeRoomBtn.setText("REMOVE ROOM");
		getInterFeasibilityBtn().setText("Inter Feasibility");
		getInterFeasibilityBtn().setTooltip(new Tooltip("Makes the room inaccessible for more complex designs")); //LOL change this
		getSuggestionsBtn().setText("Start with our suggestions");
		getSuggestionsBtn().setTooltip(new Tooltip("Start with our suggested designs as generated by genetic algorithms"));

	}
	
	private void arrangeControls(Control obj, double xPos, double yPos, double sizeWidth, double sizeHeight)
	{
		obj.setTranslateX(xPos);
		obj.setTranslateY(yPos);
		obj.setMinSize(sizeWidth, sizeHeight);
		obj.setMaxSize(sizeWidth, sizeHeight);
	}

	@Override
	public void ping(PCGEvent e) {


	}

	public Button getChangeBrushBtn() {
		return changeBrushBtn;
	}

	public void setChangeBrushBtn(Button changeBrushBtn) {
		this.changeBrushBtn = changeBrushBtn;
	}

	public ToggleButton getInterFeasibilityBtn() {
		return interFeasibilityToggle;
	}

	public void setInterFeasibilityBtn(ToggleButton interFeasibilityToggle) {
		this.interFeasibilityToggle = interFeasibilityToggle;
	}

	public Button getSuggestionsBtn() {
		return suggestionsBtn;
	}

	public void setSuggestionsBtn(Button suggestionsBtn) {
		this.suggestionsBtn = suggestionsBtn;
	}

	private void worldButtonEvents() 
	{
		//How many Brushes do we have? --> This should be automatic but it will be coded for the 3 dif brushes 
		brushBtns = new ArrayList<Button>();
		
		brushBtns.add(new Button()); //Move
		brushBtns.add(new Button()); //Connection
		brushBtns.add(new Button()); //Path
		
		brushBtns.get(0).setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.MOVEMENT);
			}

		}); 
		
		brushBtns.get(1).setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.ROOM_CONNECTOR);
			}

		}); 
		
		brushBtns.get(2).setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.PATH_FINDING);
			}

		}); 
		
		getSuggestionsBtn().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				if(dungeon.getSelectedRoom().getDoorCount(true) > 0)
				{
					MapContainer mc = new MapContainer();
					mc.setMap(dungeon.getSelectedRoom());
					router.postEvent(new RequestSuggestionsView(mc, 6));
				}

				
				//uncomment to reset scale
//				for(Node child : worldPane.getChildren()) 
//        		{
//        	        ((DungeonPane)child).resetScale();
//        		}
			}

		}); 

		//This button get all paths from room 0 to room 1
		getInterFeasibilityBtn().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				dungeon.checkInterFeasible(getInterFeasibilityBtn().isSelected());
				
				//TODO: Simple mini-hack to get the interfeasibility 
				if(getInterFeasibilityBtn().isSelected())
				{
					getInterFeasibilityBtn().setStyle("-fx-background-color:red");
				}
				else
				{
					getInterFeasibilityBtn().setStyle("");
				}
				
//				System.out.println("IS IT HERE?");
//				
//				
////				if(dungeon.size > 1)
////				{
////					dungeon.testTraverseNetwork(dungeon.getRoomByIndex(0),dungeon.getRoomByIndex(1));
////					dungeon.printRoomsPath();
////				}
//				
//				//Arbitrary path finding
//				if(dungeon.get)
//				{
////					dungeon.getSelectedRoom().applyPathfinding(new Point(0,0), new Point(10,0));
////					dungeon.getBestPathBetweenRooms(dungeon.getRoomByIndex(0), dungeon.getRoomByIndex(1));
//					
////					dungeon.calculateBestPath(dungeon.getRoomByIndex(0), dungeon.getRoomByIndex(1), new Point(0,0), new Point(10,0));
//					
//				}
			}

		}); 
		
		changeBrushBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				DungeonDrawer.getInstance().nextBrush();
//				if(DungeonDrawer.getInstance().getBrush() instanceof MoveElementBrush)
//				{
//					DungeonDrawer.getInstance().changeToConnector();
//				}
//				else
//				{
//					DungeonDrawer.getInstance().changeToMove();
//				}
			}

		}); 
		
		createNewRoomBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				System.out.println("Creating a new room");
				router.postEvent(new RequestNewRoom(dungeon, -1, widthField.getValue(), heightField.getValue()));
			}

		}); 
		
		removeRoomBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {

				if(dungeon.getSelectedRoom() != null)
					router.postEvent(new RequestRoomRemoval(dungeon.getSelectedRoom(), dungeon, -1));
			}

		}); 

	}
	
	
	//TODO: NEeds to be redone
	private String matrixToString() {
		//create large string
		String largeString = "";
//		int j = 1;
//
//		for (MapContainer[] outer : matrix) {
//
//			for (int k = 0; k < outer[0].getMap().toString().length(); k++) {
//
//				if (outer[0].getMap().toString().charAt(k) != '\n') {
//					largeString += outer[0].getMap().toString().charAt(k);
//
//				}
//				if (outer[0].getMap().toString().charAt(k) == '\n') {
//					while (j < 3) {
//
//						for (int i = (k - 11); i < k; i++) {
//							largeString += outer[j].getMap().toString().charAt(i);
//
//						}
//						j++;
//					}
//					j = 1;
//					largeString += outer[0].getMap().toString().charAt(k);
//				}
//
//			}
//
//		}
		return largeString;
	}
}

