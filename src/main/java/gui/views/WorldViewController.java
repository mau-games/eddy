package gui.views;

import java.io.IOException;
import java.util.List;

import game.ApplicationConfig;
import game.Map;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestEmptyRoom;
import util.eventrouting.events.RequestNullRoom;
import util.eventrouting.events.RequestRoomView;
import util.eventrouting.events.RequestSuggestionsView;

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
	@FXML GridPane gridPane;
	@FXML private List<LabeledCanvas> mapDisplays;

	private int row = 0;
	private int col = 0;
	private MapContainer[][] matrix;
	private int size;
	private int viewSize;
	private Node source;
	private Node oldNode;
	private LabeledCanvas canvas;
	
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
	
	

	public void initWorldMap(MapContainer[][] matrix) {
		gridPane.getChildren().clear();
		this.matrix = matrix;	
		size = matrix.length;
		viewSize = 750/size;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				
				for (int o = 0; o < matrix[i][j].getMap().toMatrix().length; o++) {
		        	for (int p = 0; p < matrix[i][j].getMap().toMatrix().length; p++) {
		        	}
		        }
				
				canvas = new LabeledCanvas();
				canvas.setText("");
				canvas.setPrefSize(viewSize, viewSize);
				canvas.draw(renderer.renderMap(matrix[j][i].getMap().toMatrix()));
				for (int outer = 0; outer < matrix[i][j].getMap().toMatrix().length; outer++) {
					for (int inner = 0; inner < matrix[i][j].getMap().toMatrix().length; inner++) {
					}
				}
				gridPane.add(canvas, i, j);

				canvas.addEventFilter(MouseEvent.MOUSE_CLICKED,
						new MouseEventHandler());
				
				//gridPane.add(new Button(), i, j);
			}
		}
		if (matrix[row][col].getMap().getNull()) {
			//disable
			System.out.println("disable");
			getSuggestionsBtn().setDisable(true);
			getStartEmptyBtn().setDisable(true);
		}
		else {
			//enable
			System.out.println("enable");
			getSuggestionsBtn().setDisable(false);
			getStartEmptyBtn().setDisable(false);	
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
				System.out.println("disable");
				getSuggestionsBtn().setDisable(true);
				getStartEmptyBtn().setDisable(true);
			}
			else {
				//enable
				System.out.println("enable");
				getSuggestionsBtn().setDisable(false);
				getStartEmptyBtn().setDisable(false);	
			}
			
			System.out.printf("Mouse entered cell [%d, %d]%n", colIndex.intValue(), rowIndex.intValue());

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
				router.postEvent(new RequestSuggestionsView(matrix[row][col], row, col, matrix, 3));
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

