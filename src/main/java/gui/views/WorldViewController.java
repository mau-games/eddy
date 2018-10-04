package gui.views;

import java.io.IOException;
import java.util.List;

import game.ApplicationConfig;
import game.Dungeon;
import game.Room;
import game.MapContainer;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import gui.views.SuggestionsViewController.MouseEventHandler;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestEmptyRoom;
import util.eventrouting.events.RequestNewRoom;
import util.eventrouting.events.RequestNullRoom;
import util.eventrouting.events.RequestRoomView;
import util.eventrouting.events.RequestSuggestionsView;
import util.IntField;

/*  
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University*/

public class WorldViewController extends GridPane implements Listener
{

	private ApplicationConfig config;
	private EventRouter router = EventRouter.getInstance();
	private boolean isActive = false;

	private Button startEmptyBtn = new Button();
	private Button roomNullBtn = new Button ();
	private Button suggestionsBtn = new Button();
	private Button testBtn = new Button();
	private IntField widthField = new IntField(1, 20, 11);
	private IntField heightField = new IntField(1, 20, 11);

	private Canvas buttonCanvas;
	private MapRenderer renderer = MapRenderer.getInstance();


	@FXML private StackPane worldPane;
	@FXML private StackPane buttonPane;
//	@FXML GridPane gridPane;
	@FXML StackPane stackPane;
	@FXML private List<LabeledCanvas> mapDisplays;

	private int row = 0;
	private int col = 0;
	private MapContainer[][] matrix;
	private int size;
	private int viewSize;
	private Node source;
	private Node oldNode;
	private LabeledCanvas canvas;
	
	Dungeon dungeon;
	
	double anchorX;
	double anchorY;

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

	private void initWorldView() {
		worldButtonEvents();
		initOptions();	
		

	}
	
	//TODO: THIS NEEDS TO BE CHECK BECAUSE OF HOW WE RENDER!!!!!!
	//TODO: this need to be check IDK do we really need to clear the children and create them again? probably there is a better way :D 
	public void initWorldMap(Dungeon dungeon) 
	{
		
		if(widthField == null)
			widthField = new IntField(1, 20, dungeon.defaultWidth);
		
		if(heightField == null)
			heightField = new IntField(1, 20, dungeon.defaultHeight);
		
		stackPane.getChildren().clear();
		this.dungeon = dungeon;
		size = this.dungeon.size;
		viewSize = 500;
		int viewSizeHeight = 500;
		int viewSizeWidth = 500;
		
		stackPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
		stackPane.setAlignment(Pos.CENTER);
		
		for (int index = 0; index < size; index++) 
		{
			//TODO: the 50 should be scaled --> And the 20... is because of the padding
			viewSizeHeight = (50 * (dungeon.getRoomByIndex(index).getRowCount())) + 20; //THIS IS PART OF THE SOLUTION
			viewSizeWidth = (50 * (dungeon.getRoomByIndex(index).getColCount())) + 20; //THIS IS PART OF THE SOLUTION
			canvas = new LabeledCanvas();
			canvas.setText("");
			canvas.setMinSize(viewSizeWidth, viewSizeHeight);
			canvas.setMaxSize(viewSizeWidth, viewSizeHeight); //THIS IS PART OF THE SOLUTION
			canvas.setPrefSize(viewSizeWidth, viewSizeHeight);
			canvas.draw(renderer.renderMap(dungeon.getRoomByIndex(index))); //TODO: HERE!!!
//				canvas.relocate(25, 25);
//			
//			canvas.setMinSize(viewSize, viewSize);
//			canvas.setMaxSize(viewSize, viewSize);
			stackPane.getChildren().add(canvas);

//				canvas.addEventFilter(MouseEvent.MOUSE_CLICKED,
//						new MouseEventHandler());

//				
//				canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, new MouseEventH());
			canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, new MouseEventH());
		}
		
//		boolean voidRoom = matrix[row][col].getMap().getNull();
//		getSuggestionsBtn().setDisable(voidRoom);
//		getStartEmptyBtn().setDisable(voidRoom);

	}
	
	private void zoom()
	{
		int s = stackPane.getChildren().size();
		viewSize += 10;
		for(int i = 0; i < s; i++)
		{
			canvas = (LabeledCanvas)stackPane.getChildren().get(i); //DIRTy
			canvas.setMaxSize(viewSize, viewSize);
		}
		
		
	}

	private void initOptions() {				
		buttonCanvas = new Canvas(1000, 1000);
		StackPane.setAlignment(buttonCanvas, Pos.CENTER);
		buttonPane.getChildren().add(buttonCanvas);
		buttonCanvas.setVisible(false);
		buttonCanvas.setMouseTransparent(true);

		getStartEmptyBtn().setTranslateX(800);
		getStartEmptyBtn().setTranslateY(-200);
		getRoomNullBtn().setTranslateX(800);
		getRoomNullBtn().setTranslateY(0);
		getSuggestionsBtn().setTranslateX(800);
		getSuggestionsBtn().setTranslateY(200);
		
		testBtn.setTranslateX(800);
		testBtn.setTranslateY(400);
		
		heightField.setStyle("-fx-text-inner-color: white;");
		heightField.setPromptText("HEIGHT");
		heightField.setTranslateX(700);
		heightField.setTranslateY(-300);
		
		widthField.setStyle("-fx-text-inner-color: white;");
		widthField.setPromptText("WIDTH");
		widthField.setTranslateX(810);
		widthField.setTranslateY(-300);
		
		getStartEmptyBtn().setMinSize(500, 100);
		getRoomNullBtn().setMinSize(500, 100);
		getSuggestionsBtn().setMinSize(500, 100);
		testBtn.setMinSize(500, 100);
		widthField.setMinSize(100, 50);
		widthField.setMaxSize(100, 50);
		heightField.setMinSize(100, 50);
		heightField.setMaxSize(100, 50);

		buttonPane.getChildren().add(getStartEmptyBtn());
		buttonPane.getChildren().add(getRoomNullBtn());
		buttonPane.getChildren().add(getSuggestionsBtn());
		buttonPane.getChildren().add(testBtn);
		buttonPane.getChildren().add(heightField);
		buttonPane.getChildren().add(widthField);
		
		testBtn.setText("ZOOM IN");
		testBtn.setDisable(false);
		
		getStartEmptyBtn().setText("Edit room");
		getStartEmptyBtn().setTooltip(new Tooltip("Go to room view and start designing"));
		getRoomNullBtn().setText("Enable/Disable room");
		getRoomNullBtn().setTooltip(new Tooltip("Makes the room inaccessible for more complex designs"));
		getSuggestionsBtn().setText("Start with our suggestions");
		getSuggestionsBtn().setTooltip(new Tooltip("Start with our suggested designs as generated by genetic algorithms"));

	}



	@Override
	public void ping(PCGEvent e) {
		// TODO Auto-generated method stub

	}

	public Button getStartEmptyBtn() {
		return startEmptyBtn;
	}

	public void setStartEmptyBtn(Button startEmptyBtn) {
		this.startEmptyBtn = startEmptyBtn;
	}

	public Button getRoomNullBtn() {
		return roomNullBtn;
	}

	public void setRoomNullBtn(Button roomNullBtn) {
		this.roomNullBtn = roomNullBtn;
	}

	public Button getSuggestionsBtn() {
		return suggestionsBtn;
	}

	public void setSuggestionsBtn(Button suggestionsBtn) {
		this.suggestionsBtn = suggestionsBtn;
	}
	
	public class MouseEventH implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
			
			source.setOnMouseDragged(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {

	            	
//	            	System.out.println(source.getLayoutBounds());
//	            	System.out.println("parent: " + source.getParent().getLayoutBounds());
//	            	System.out.println("EVENT X: " +  (event.getX()));
//	            	System.out.println("getTranslateX: " +  (source.getTranslateX()));
//	            	System.out.println("SCENE X: " + event.getSceneX());

	            	
//	            	if(outSideParentBounds(source.getLayoutBounds(),source.getTranslateX() + 10, source.getTranslateY() + 10))
//	            	{
//	            		event.consume();
//	            		return;
//	            	}
//	            		
	            	
	    			source.setTranslateX(event.getX() + source.getTranslateX() - anchorX);
	    			source.setTranslateY(event.getY() + source.getTranslateY() - anchorY); 

	            }

	            //TODO: Trying to limit if the canvas is outside the pane --> need to give it a bettter try
	            private boolean outSideParentBounds( Bounds childBounds, double newX, double newY) {

	                Bounds parentBounds = source.getParent().getLayoutBounds();
	                
	                newX += parentBounds.getMaxX() / 2;
	                newY += parentBounds.getMaxY() / 2;

	                if((childBounds.getMaxX() / 2.0) + newX > parentBounds.getMaxX())
	                {
//	                	source.setTranslateX(source.getTranslateX() - 10);
	                	return true;
	                }
	                	
	                
//	                //check if too left
//	                if( parentBounds.getMaxX() <= (newX + childBounds.getMaxX()) ) {
//	                    return true ;
//	                }
//
//	                //check if too right
//	                if( parentBounds.getMinX() >= (newX + childBounds.getMinX()) ) {
//	                    return true ;
//	                }
//
//	                //check if too down
//	                if( parentBounds.getMaxY() <= (newY + childBounds.getMaxY()) ) {
//	                    return true ;
//	                }
//
//	                //check if too up
//	                if( parentBounds.getMinY() >= (newY + childBounds.getMinY()) ) {
//	                    return true ;
//	                }

	                return false;

	            }
	        });
			
			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {

	            	anchorX = event.getX();
	    			anchorY = event.getY();
	    		
	            }
	        });
			
		}
	}
	
	
	public class MouseEventHandler implements EventHandler<MouseEvent> {


		@Override
		public void handle(MouseEvent event) {
			source = (Node)event.getSource() ;
			Integer colIndex = GridPane.getColumnIndex(source);
			Integer rowIndex = GridPane.getRowIndex(source);
			row = rowIndex;
			col = colIndex;
			getRoomNullBtn().setDisable(false);
			if (matrix[row][col].getMap().getNull()) {
				//disable
				getSuggestionsBtn().setDisable(true);
				getStartEmptyBtn().setDisable(true);
			}
			else {
				//enable
				getSuggestionsBtn().setDisable(false);
				getStartEmptyBtn().setDisable(false);	
			}
			

			source.setOnMouseClicked(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {
	    			source.setStyle("-fx-background-color:#fcdf3c;");
	    			if (oldNode != null) {
	    				oldNode.setStyle("-fx-background-color:#2c2f33;");
	    			}	 
	    			oldNode = source;
	    			

	            }
	        });
			
			source.setOnMouseExited(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {
	    			source.setStyle("-fx-background-color:#fcdf3c;");
	    			
	            }
	        });
			
			source.setOnMouseEntered(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {
	    			source.setStyle("-fx-background-color:#fcdf3c;");
	    			
	            }
	        });

		}

	}

	private void worldButtonEvents() 
	{
		
		getStartEmptyBtn().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				
				router.postEvent(new RequestEmptyRoom(matrix[row][col], row, col, matrix));
			}

		}); 
		getSuggestionsBtn().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				router.postEvent(new RequestSuggestionsView(matrix[row][col], row, col, matrix, 6));
			}

		}); 


		getRoomNullBtn().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				router.postEvent(new RequestNullRoom(matrix[row][col], row, col, matrix));
			}

		}); 
		
		testBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				System.out.println("Pressed test btn");
//				zoom();
				router.postEvent(new RequestNewRoom(dungeon, -1, heightField.getValue(), widthField.getValue()));
			}

		}); 

	}
	
	
	
	private String matrixToString() {
		//create large string
		String largeString = "";
		int j = 1;

		for (MapContainer[] outer : matrix) {

			for (int k = 0; k < outer[0].getMap().toString().length(); k++) {

				if (outer[0].getMap().toString().charAt(k) != '\n') {
					largeString += outer[0].getMap().toString().charAt(k);

				}
				if (outer[0].getMap().toString().charAt(k) == '\n') {
					while (j < 3) {

						for (int i = (k - 11); i < k; i++) {
							largeString += outer[j].getMap().toString().charAt(i);

						}
						j++;
					}
					j = 1;
					largeString += outer[0].getMap().toString().charAt(k);
				}

			}

		}
		return largeString;
	}
}

