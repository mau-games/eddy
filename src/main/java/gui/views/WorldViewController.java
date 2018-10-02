package gui.views;

import java.io.IOException;
import java.util.List;

import game.ApplicationConfig;
import game.Room;
import game.MapContainer;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import gui.views.SuggestionsViewController.MouseEventHandler;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import util.eventrouting.events.RequestNullRoom;
import util.eventrouting.events.RequestRoomView;
import util.eventrouting.events.RequestSuggestionsView;

/*  
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University*/

public class WorldViewController extends GridPane implements Listener{

	private ApplicationConfig config;
	private EventRouter router = EventRouter.getInstance();
	private boolean isActive = false;

	private Button startEmptyBtn = new Button();
	private Button roomNullBtn = new Button ();
	private Button suggestionsBtn = new Button();

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
	
	double orgSceneX;
	double orgSceneY;
	double offsetX;
	double offsetY;
	double initialTransX;
	double initialTransY;
	
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
	
	
	//TODO: this need to be check
	public void initWorldMap(MapContainer[][] matrix) {
//		gridPane.getChildren().clear();
//		this.matrix = matrix;	
//		size = matrix.length;
//		viewSize = 750/size;
//		gridPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
////		gridPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//		
//		for (int i = 0; i < matrix.length; i++) 
//		{
//			for (int j = 0; j < matrix.length; j++)
//			{
//
//				canvas = new LabeledCanvas();
//				canvas.setText("");
//				canvas.setPrefSize(viewSize, viewSize);
//				canvas.draw(renderer.renderMap(matrix[j][i].getMap().toMatrix()));
//
//				gridPane.add(canvas, i, j);
//
//				canvas.addEventFilter(MouseEvent.MOUSE_CLICKED,
//						new MouseEventHandler());
//				
//				canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, new MouseEventH());
//				
//				canvas.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//				
////				canvas.setOnDragDetected(new EventHandler <MouseEvent>() {
////			            public void handle(MouseEvent event) {
////			                /* drag was detected, start drag-and-drop gesture*/
////			                System.out.println("onDragDetected");
////			                
////			                /* allow any transfer mode */
////			                Dragboard db = canvas.startDragAndDrop(TransferMode.ANY);
////			                
////			                /* put a string on dragboard */
////			                ClipboardContent content = new ClipboardContent();
//////			                content.putString("YEAH BOI");
////			                db.setContent(content);
////			                
////			                event.consume();
////			            }
////			        });
////				
//			}
//		}

		stackPane.getChildren().clear();
		this.matrix = matrix;	
		size = matrix.length;
		viewSize = 750/size;
//		pane.setStyle("-fx-background-color: black;");
//		pane.setPrefSize(750,750);
		stackPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//		gridPane.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
//		StackPane.setAlignment(stackPane, Pos.CENTER);
		
		stackPane.setAlignment(Pos.CENTER);

		
		for (int i = 0; i < matrix.length; i++) 
		{
			for (int j = 0; j < matrix.length; j++)
			{

				canvas = new LabeledCanvas();
				canvas.setText("");
				canvas.setPrefSize(viewSize, viewSize);
				canvas.draw(renderer.renderMap(matrix[j][i].getMap().toMatrix()));
//				canvas.relocate(25, 25);
				
				canvas.setMinSize(viewSize, viewSize);
				canvas.setMaxSize(viewSize, viewSize);
				stackPane.getChildren().add(canvas);

//				canvas.addEventFilter(MouseEvent.MOUSE_CLICKED,
//						new MouseEventHandler());

//				
				canvas.addEventFilter(MouseEvent.MOUSE_DRAGGED, new MouseEventH());
				canvas.addEventFilter(MouseEvent.MOUSE_PRESSED, new MouseEventH());
//				
//				canvas.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
				
//				canvas.setOnDragDetected(new EventHandler <MouseEvent>() {
//			            public void handle(MouseEvent event) {
//			                /* drag was detected, start drag-and-drop gesture*/
//			                System.out.println("onDragDetected");
//			                
//			                /* allow any transfer mode */
//			                Dragboard db = canvas.startDragAndDrop(TransferMode.ANY);
//			                
//			                /* put a string on dragboard */
//			                ClipboardContent content = new ClipboardContent();
////			                content.putString("YEAH BOI");
//			                db.setContent(content);
//			                
//			                event.consume();
//			            }
//			        });
//				
			}
		}
		
		boolean voidRoom = matrix[row][col].getMap().getNull();
		getSuggestionsBtn().setDisable(voidRoom);
		getStartEmptyBtn().setDisable(voidRoom);

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

		getStartEmptyBtn().setMinSize(500, 100);
		getRoomNullBtn().setMinSize(500, 100);
		getSuggestionsBtn().setMinSize(500, 100);


		buttonPane.getChildren().add(getStartEmptyBtn());
		buttonPane.getChildren().add(getRoomNullBtn());
		buttonPane.getChildren().add(getSuggestionsBtn());

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
			source = (Node)event.getSource() ;
//			Integer colIndex = GridPane.getColumnIndex(source);
//			Integer rowIndex = GridPane.getRowIndex(source);
//			row = rowIndex;
//			col = colIndex;
//			orgSceneX = source.getScene().getX();
//			orgSceneY = source.getScene().getY();
			
			source.setOnMouseDragged(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {
//	    			System.out.println("DRAAAAAAAAAGED");
	            	
//	            	source.setTranslateX(
//	            			initialTransX
//	                            + event.getX()
//	                            - orgSceneX);
//	                    source.setTranslateY(
//	                    		initialTransY
//	                            + event.getY()
//	                            - orgSceneY);
	    			
//	    			offsetX = event.getScreenX() - offsetX;
//	    			offsetY = event.getScreenY() - offsetY;
	    			
//	    			source.setTranslateX( (event.getScreenX() - offsetX));
//	    			source.setTranslateY((event.getScreenY() - offsetY));

//	            	source.setTranslateX((event.getX() + source.getTranslateX()) - (source.boundsInLocalProperty().get().getWidth()/2.0f));
//	    			source.setTranslateY((event.getY() + source.getTranslateY()) - (source.boundsInLocalProperty().get().getHeight()/2.0f)); 
	    			
//	            	offsetX = event.getSceneX() - orgSceneX;
//	    			offsetY = event.getSceneY() - orgSceneY;
//	            	
	    			source.setTranslateX(event.getX() + source.getTranslateX() - orgSceneX);
	    			source.setTranslateY(event.getY() + source.getTranslateY() - orgSceneY); 
	    			
//	    			source.setTranslateX(source.getTranslateX() + offsetX);
//	    			source.setTranslateY(source.getTranslateY() + offsetY); 
	    			
	    			System.out.println("translate X = " + orgSceneX + ", translate Y = " + orgSceneY);
//	    			
//	    			source.setTranslateX(source.getTranslateX() + offsetX);
//					source.setTranslateY(source.getTranslateY() + offsetY);
//
//					orgSceneX = event.getSceneX();
//					orgSceneY = event.getSceneY();
	    			

	            }
	        });
			
			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {
//	    			System.out.println("PRESS");
	    			
//	    			orgSceneX = event.getSceneX();
//	    			orgSceneY = event.getSceneY();
	            	
	            	orgSceneX = event.getX();
	    			orgSceneY = event.getY();
	    			
	    			initialTransX = source.getTranslateX();
	    			initialTransY = source.getTranslateY();
	    			
	    			
	    			System.out.println("Initial X = " + orgSceneX + ", Initial Y = " + orgSceneY);
	    			

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

	private void worldButtonEvents() {
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

