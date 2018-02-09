package gui.views;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javax.management.RuntimeErrorException;

import game.ApplicationConfig;
import gui.controls.InteractiveMap;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;

public class WorldMapController implements Initializable, Listener{
	
	private ApplicationConfig config;
	private EventRouter router = EventRouter.getInstance();
	
	private InteractiveMap mapView;
	private StackPane mapPane;
	
	public WorldMapController() {
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/WorldMap.fxml"));
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
		
		initWorldMap();
	}
	

	
	private void initWorldMap() {
		int width = 420;
		int height = 420;

		Pane root = new Pane();

		mapView = new InteractiveMap();
		StackPane.setAlignment(mapView, Pos.CENTER);
		mapView.setMinSize(width, height);
		mapView.setMaxSize(width, height);
		mapPane.getChildren().add(mapView);

		
	}

	@Override
	public void ping(PCGEvent e) {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
}
