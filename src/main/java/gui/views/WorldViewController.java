package gui.views;

import java.io.IOException;
import java.util.List;

import game.ApplicationConfig;
import game.MapContainer;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;

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
		initOptions();	
	}
	
	public void initWorldMap(MapContainer[][] matrix) {
			
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix.length; j++) {
				LabeledCanvas canvas = new LabeledCanvas();
				canvas.setText("");
				canvas.setPrefSize(250, 250);
				canvas.draw(renderer.renderMap(matrix[i][j].getMap().toMatrix()));
				for (int outer = 0; outer < matrix[i][j].getMap().toMatrix().length; outer++) {
					for (int inner = 0; inner < matrix[i][j].getMap().toMatrix().length; inner++) {
						System.out.print(matrix[i][j].getMap().toMatrix()[outer][inner]);
					}
					System.out.println();
				}
				gridPane.add(canvas, i, j);
				gridPane.setHgap(20);

				//gridPane.add(new Button(), i, j);
			}
		}	
	}
	
	private void initOptions() {				
		buttonCanvas = new Canvas(1000, 1000);
		StackPane.setAlignment(buttonCanvas, Pos.CENTER);
		buttonPane.getChildren().add(buttonCanvas);
		buttonCanvas.setVisible(false);
		buttonCanvas.setMouseTransparent(true);
					
		getStartEmptyBtn().setTranslateX(1500);
		getStartEmptyBtn().setTranslateY(500);
		getRoomNullBtn().setTranslateX(1500);
		getRoomNullBtn().setTranslateY(350);
		getSuggestionsBtn().setTranslateX(1500);
		getSuggestionsBtn().setTranslateY(200);
		
		getStartEmptyBtn().setMinSize(500, 100);
		getRoomNullBtn().setMinSize(500, 100);
		getSuggestionsBtn().setMinSize(500, 100);
		
		buttonPane.getChildren().add(getStartEmptyBtn());
		buttonPane.getChildren().add(getRoomNullBtn());
		buttonPane.getChildren().add(getSuggestionsBtn());
		
		getStartEmptyBtn().setText("Start with empty room");
		getRoomNullBtn().setText("Make room null");
		getSuggestionsBtn().setText("Start with our suggestions");
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




}
