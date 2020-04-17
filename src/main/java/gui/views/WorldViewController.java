package gui.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import game.ApplicationConfig;
import game.Dungeon;
import game.DungeonPane;
import game.MapContainer;
import game.PathInformation;
import game.Room;
import gui.controls.LabeledCanvas;
import gui.controls.Popup;
import gui.utils.AnimatedGif;
import gui.utils.Animation;
import gui.utils.DungeonDrawer;
import gui.utils.InformativePopupManager;
import gui.utils.DungeonDrawer.DungeonBrushes;
import gui.utils.InformativePopupManager.PresentableInformation;
import gui.utils.InterRoomBrush;
import gui.utils.MapRenderer;
import gui.utils.MoveElementBrush;
import gui.utils.RoomConnectorBrush;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;
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
	
	private Button suggestionsBtn = new Button();
	private Button createNewRoomBtn = new Button();
	private Button removeRoomBtn = new Button();
	private Button pickInitBtn = new Button();
	//feature-quest
	private Button questViewBtn = new Button();
	
	private ArrayList<Button> brushBtns = new ArrayList<Button>(); //TODO: This can be improved to be dependant on how many brushes and have maybe its own class?
	private ComboBox<PathInformation.PathType> pathTypeComboBox = new ComboBox<>();
	
	private Label widthLabel = new Label("Width =");
	private Label heightLabel = new Label("Height =");
	private IntField widthField = new IntField(3, 20, 13);
	private IntField heightField = new IntField(3, 20, 7);

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
		//CHECK THE PROPERTY
		if(widthField == null)
			widthField = new IntField(1, 20, dungeon.defaultWidth);
		
		if(heightField == null)
			heightField = new IntField(1, 20, dungeon.defaultHeight);
		
		this.dungeon = dungeon;
		worldPane.getChildren().clear();
		worldPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		
		dungeon.dPane.renderAll();
		worldPane.getChildren().add(dungeon.dPane);
//		
//		if(this.dungeon.getAllRooms().size() > 3 && this.dungeon.getBosses().isEmpty())
//		{
//			InformativePopupManager.getInstance().requestPopup(dungeon.dPane, PresentableInformation.NO_BOSS_YET, "");
//		}
	}
	
	/**
	 * Hard coded but necessary as it is initial setup!
	 */
	public void initialSetup()
	{
		ArrayList<Room> initRooms = dungeon.getAllRooms();
		
		initRooms.get(1).localConfig.getWorldCanvas().getCanvas().setTranslateX(
				initRooms.get(1).localConfig.getWorldCanvas().viewSizeWidth + 
				initRooms.get(1).localConfig.getWorldCanvas().viewSizeWidth / 2.0);
		
		EventRouter.getInstance().postEvent(new RequestConnection(null, 
				-1, 
				initRooms.get(0), initRooms.get(1), 
				new Point(initRooms.get(0).getColCount() -1, initRooms.get(0).getRowCount()/2), 
				new Point(0, initRooms.get(1).getRowCount()/2)));	
		
		for(Node child : worldPane.getChildren())
		{
			child.setLayoutX(child.getLayoutX() + (200));
			child.setLayoutY(child.getLayoutY() + 200); 
		}
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
		
//		pathTypeComboBox.getItems().setAll(PathInformation.PathType.values());
//		pathTypeComboBox.setValue(PathInformation.getInstance().getPathType());
		
		
		//some calculations for the brushes
		double maxWidth = 250;
		double maxHeight = 50;
		double initXPos = -100;
		double yPos = 0;
		double widthPadding = 30;
		double btnWidthSize = (maxWidth / brushBtns.size()) - (10 * brushBtns.size());
		double xStep = btnWidthSize + (btnWidthSize / 2.0) + widthPadding;
		
//		ImageView mView = new ImageView(new Image(this.getClass().getResource("/graphics/player.gif").toExternalForm()));
//		 Animation ani = new AnimatedGif(getClass().getResource("/graphics/player.gif").toExternalForm(), 1000);
//	        ani.setCycleCount(Timeline.INDEFINITE);
//	        ani.play();
			
//			ani.getView().setX(0);
//			ani.getView().setY(200);
//			ani.getView().setFitWidth(50);
//			ani.getView().setFitHeight(50);

		//Arrange the controls
		arrangeControls(widthLabel, 90, -310, 50, 50);
		arrangeControls(heightLabel, 90, -260, 50, 50);
		arrangeControls(widthField, 140, -310, 50, 30);
		arrangeControls(heightField, 140, -260, 50, 30);
		arrangeControls(createNewRoomBtn, -50, -300, -1, -1);
		arrangeControls(removeRoomBtn, 0, -150, -1, -1);
		arrangeControls(getSuggestionsBtn(), 0, 50, 200, 50);
//		arrangeControls(getPickInitBtn(), 0, 200, 300, 50);
//		arrangeControls(mView, 0, 200, 50, 50);
		arrangeControls(questViewBtn, 0,250 , 100,50); //feature-quest


		Popup restartProgram = new Popup("If you are a new user, please restart the workflow by \"File/New Workflow\" at the top menu", 50, 100, false);
		restartProgram.setMaxHeight(100);
		restartProgram.setMinHeight(100);
		restartProgram.setPrefHeight(100);
		restartProgram.setTranslateX(0);
		restartProgram.setTranslateY(150);
//		restartProgram.setPosition(0, 250);
		
//		arrangeControls(pathTypeComboBox, 0, 300, 300, 50);
		
//		String[] btnsText = {"M", "C", "P"};
		for(int i = 0; i < brushBtns.size(); i++, initXPos += xStep)
		{
//			arrangeControls(brushBtns.get(i), initXPos, 0, btnWidthSize, maxHeight);
			arrangeControls(brushBtns.get(i), initXPos, -25, -1, -1);
			buttonPane.getChildren().add(brushBtns.get(i));
//			brushBtns.get(i).setText(btnsText[i]);
		}
		
		try {
			config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//all of this is so ugly!! :( :( 
		ConfigurationUtility c = config.getInternalConfig();
		
		//ADD ROOM
		ImageView brushBtnImage = new ImageView(new Image(c.getString("map.dungeonbrushes.addRoom"), 200, 200, true, true));
//		BorderPane imageViewWrapper = new BorderPane(brushBtnImage);
		createNewRoomBtn.setGraphic(brushBtnImage);
		createNewRoomBtn.setStyle("-fx-background-color: transparent; -fx-padding: 5;\r\n" + 
				"    -fx-border-style: none;\r\n" + 
				"    -fx-border-width: 0;\r\n" + 
				"    -fx-border-insets: 0;");
//		
//		imageViewWrapper.setOnMouseEntered(new EventHandler<MouseEvent>() {
//
//            @Override
//            public void handle(MouseEvent event) 
//            {
////            	ImageView iv = (ImageView)createNewRoomBtn.getGraphic();
//            	imageViewWrapper.setStyle("-fx-border-width: 2px; -fx-border-color: #6b87f9;");
////            	iv.setcolorfi
////            	createNewRoomBtn.setStyle("-fx-background-color: transparent; -fx-padding: 6 4 4 6;");
//            }
//
//        });
		
		//REMOVE ROOM
		brushBtnImage = new ImageView(new Image(c.getString("map.dungeonbrushes.removeRoom"), 200, 200, true, true));
		removeRoomBtn.setGraphic(brushBtnImage);
		removeRoomBtn.setStyle("-fx-background-color: transparent; -fx-padding: 5;\r\n" + 
				"    -fx-border-style: none;\r\n" + 
				"    -fx-border-width: 0;\r\n" + 
				"    -fx-border-insets: 0;");
		//MOVE
		brushBtnImage = new ImageView(new Image(c.getString("map.dungeonbrushes.move"), 40, 40, true, true));
		brushBtns.get(0).setGraphic(brushBtnImage);
		//CONNECTOR
		brushBtnImage = new ImageView(new Image(c.getString("map.dungeonbrushes.connector"), 80, 80, true, true));
//		brushBtnImage.setStyle("-fx-background-color: transparent");
		brushBtns.get(1).setGraphic(brushBtnImage);
		
		brushBtnImage = new ImageView(new Image(c.getString("map.dungeonbrushes.init"), 40, 40, true, true));
//		brushBtnImage.setStyle("-fx-background-color: transparent");
		brushBtns.get(2).setGraphic(brushBtnImage);
	
		//change color of the input fields!
		heightField.setStyle("-fx-text-inner-color: white;");		
		widthField.setStyle("-fx-text-inner-color: white;");
		widthLabel.setTextFill(Color.WHITE);
		heightLabel.setTextFill(Color.WHITE);
//		widthLabel.setTextFill(Paint);

		//Add everything to the button pane!
		buttonPane.getChildren().add(getSuggestionsBtn());
//		buttonPane.getChildren().add(getPickInitBtn());
		buttonPane.getChildren().add(createNewRoomBtn);
		buttonPane.getChildren().add(removeRoomBtn);
		buttonPane.getChildren().add(heightField);
		buttonPane.getChildren().add(widthField);
		buttonPane.getChildren().add(widthLabel);
		buttonPane.getChildren().add(heightLabel);
		buttonPane.getChildren().add(restartProgram);
		//feature-quest
		buttonPane.getChildren().add(questViewBtn);

//		buttonPane.getChildren().add(pathTypeComboBox);
//		buttonPane.getChildren().add(ani.getView());
		
//		createNewRoomBtn.setOnAction( e -> ani.pause());
//		removeRoomBtn.setOnAction( e -> ani.play());
		
		//Change the text of the buttons!
		pickInitBtn.setText("Pick Init Room and Pos");
//		createNewRoomBtn.setText("NEW ROOM");
//		removeRoomBtn.setText("REMOVE ROOM");
		getSuggestionsBtn().setText("Start with our suggestions");
		getSuggestionsBtn().setTooltip(new Tooltip("Start with our suggested designs as generated by genetic algorithms"));
		questViewBtn.setText("Add Quests");
		questViewBtn.setTooltip(new Tooltip(("Open the QuestEditor")));
		
		//Tooltips!
		createNewRoomBtn.setTooltip(new Tooltip("Create and Add new room to dungeon with specified Width and Height"));
		removeRoomBtn.setTooltip(new Tooltip("Remove currently selected room"));
		brushBtns.get(0).setTooltip(new Tooltip("Press and Hold over a room to move"));
		brushBtns.get(1).setTooltip(new Tooltip("Connect 2 rooms with a door. Drag with your mouse from one room's border to another room's border"));
		brushBtns.get(2).setTooltip(new Tooltip("Place the hero in a room and position as starting point!"));

	}
	
	private void arrangeControls(Control obj, double xPos, double yPos, double sizeWidth, double sizeHeight)
	{
		obj.setTranslateX(xPos);
		obj.setTranslateY(yPos);
		if(sizeWidth > 0 && sizeHeight > 0)
		{
			obj.setMinSize(sizeWidth, sizeHeight);
			obj.setMaxSize(sizeWidth, sizeHeight);
		}
	}

	@Override
	public void ping(PCGEvent e) {


	}

	public Button getPickInitBtn() {
		return pickInitBtn;
	}

	public void setPickInitBtn(Button pickInitBtn) {
		this.pickInitBtn = pickInitBtn;
	}

	public Button getSuggestionsBtn() {
		return suggestionsBtn;
	}

	public void setSuggestionsBtn(Button suggestionsBtn) {
		this.suggestionsBtn = suggestionsBtn;
	}
	
	//TODO: This should check which brush was before and go back to that one!!!!
	public void restoreBrush() //If you call this method is because you have change the initial room!!
	{
		DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.MOVEMENT);
		getPickInitBtn().setText("Pick Init Room and Pos: DONE");
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
				ActionLogger.getInstance().storeAction(ActionType.CLICK,
														View.WORLD, 
														TargetPane.BUTTON_PANE, 
														false,
														DungeonBrushes.MOVEMENT);
				
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.MOVEMENT);
			}

		}); 
		
		brushBtns.get(1).setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				ActionLogger.getInstance().storeAction(ActionType.CLICK,
														View.WORLD, 
														TargetPane.BUTTON_PANE, 
														false,
														DungeonBrushes.ROOM_CONNECTOR);
				
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.ROOM_CONNECTOR);
			}

		}); 
		
		brushBtns.get(2).setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				ActionLogger.getInstance().storeAction(ActionType.CLICK,
														View.WORLD, 
														TargetPane.BUTTON_PANE, 
														false,
														DungeonBrushes.INITIAL_ROOM);
				
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.INITIAL_ROOM);
			}

		}); 
		
//		brushBtns.get(2).setOnAction(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent e) 
//			{
//				ActionLogger.getInstance().storeAction(ActionType.CLICK,
//														View.WORLD, 
//														TargetPane.BUTTON_PANE, 
//														false,
//														DungeonBrushes.PATH_FINDING);
//				
//				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.PATH_FINDING);
//			}
//
//		}); 
		
		getSuggestionsBtn().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				if(dungeon.getSelectedRoom().getDoorCount() > 0)
				{
					ActionLogger.getInstance().storeAction(ActionType.CLICK,
															View.WORLD, 
															TargetPane.BUTTON_PANE, 
															false,
															"Suggestions");
					DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.MOVEMENT);
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
		
//		DungeonDrawer.getInstance().nextBrush();
		
		pickInitBtn.setOnAction(new EventHandler<ActionEvent>() { //TODO: This must change!
			@Override
			public void handle(ActionEvent e) 
			{
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.INITIAL_ROOM);
				
			}

		}); 
		
		createNewRoomBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				System.out.println("Creating a new room");
				ActionLogger.getInstance().storeAction(ActionType.CLICK, 
														View.WORLD, 
														TargetPane.BUTTON_PANE, 
														false,
														"Create Room");
				
				router.postEvent(new RequestNewRoom(dungeon, -1, widthField.getValue(), heightField.getValue()));
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.MOVEMENT);
			}
		}); 
		
		createNewRoomBtn.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) 
            {
            	createNewRoomBtn.setStyle("-fx-border-width: 2px; -fx-border-color: #6b87f9;");
            }

        });
//		
		createNewRoomBtn.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) 
            {
            	createNewRoomBtn.setStyle("-fx-border-width: 0px; -fx-background-color:#2c2f33;");
            }

        });
		
		removeRoomBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {

				if(dungeon.getSelectedRoom() != null)
				{
					ActionLogger.getInstance().storeAction(ActionType.CLICK, 
															View.WORLD, 
															TargetPane.BUTTON_PANE, 
															false,
															"Remove Room");
					router.postEvent(new RequestRoomRemoval(dungeon.getSelectedRoom(), dungeon, -1));
					DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.MOVEMENT);
				}
					
			}

		}); 
		
		removeRoomBtn.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) 
            {
            	removeRoomBtn.setStyle("-fx-border-width: 2px; -fx-border-color: #6b87f9;");
            }

        });
//		
		removeRoomBtn.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) 
            {
            	removeRoomBtn.setStyle("-fx-border-width: 0px; -fx-background-color:#2c2f33;");
            }

        });
		
		pathTypeComboBox.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {

				PathInformation.getInstance().changePathType(pathTypeComboBox.getValue());
			}

		});

		questViewBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				ActionLogger.getInstance().storeAction(ActionType.CLICK, View.WORLD, TargetPane.BUTTON_PANE, false, "Open Quest Editor");
				router.postEvent(new RequestQuestView());
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

