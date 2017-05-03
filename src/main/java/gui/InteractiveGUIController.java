package gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import game.ApplicationConfig;
import game.Map;
import gui.views.EditViewController;
import gui.views.StartViewController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestRedraw;
import util.eventrouting.events.StatusMessage;

public class InteractiveGUIController implements Initializable, Listener {
	
	@FXML private AnchorPane mainPane;
	@FXML private MenuItem newItem;
	@FXML private MenuItem openItem;
	@FXML private MenuItem saveItem;
	@FXML private MenuItem saveAsItem;
	@FXML private MenuItem exportItem;
	@FXML private MenuItem prefsItem;
	@FXML private MenuItem exitItem;
	@FXML private MenuItem aboutItem;
	
	Stage stage = null;
	
	StartViewController startView = null;
	EditViewController editView = null;
	EventHandler<MouseEvent> mouseEventHandler = null;
	
	final static Logger logger = LoggerFactory.getLogger(InteractiveGUIController.class);
	private static EventRouter router = EventRouter.getInstance();
	private ApplicationConfig config;

	@Override
	public void ping(PCGEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		router.registerListener(this, new StatusMessage(null));
		router.registerListener(this, new AlgorithmDone(null));
		router.registerListener(this, new RequestRedraw());
		
		startView = new StartViewController();
		editView = new EditViewController();
		
		mainPane.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
			if (newScene != null) {
				stage = (Stage) newScene.getWindow();
			}
		});
		
		initStartView();
	}
	
	/*
	 * Event stuff
	 */
	
	public void startNewFlow() {
		initStartView();
	}
	
	public void exitApplication() {
		// TODO: Maybe be a bit more graceful than this...
		
		Platform.exit();
		System.exit(0);
	}
	
	public void openMap() {
		System.out.println("Open map");
		
		 FileChooser fileChooser = new FileChooser();
		 fileChooser.setTitle("Open Map");
		 fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("Map Files", "*.map"),
		         new ExtensionFilter("All Files", "*.*"));
		 File selectedFile = fileChooser.showOpenDialog(stage);
		 if (selectedFile != null) {
			 System.out.println("Selected file: " + selectedFile);
			 initEditView();
			 // TODO: Load map
			 try {
				Map.LoadMap(selectedFile);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 }
	}
	
	public void saveMap() {
		System.out.println("Save map");
		
		 FileChooser fileChooser = new FileChooser();
		 fileChooser.setTitle("Open Map");
		 fileChooser.getExtensionFilters().addAll(
		         new ExtensionFilter("Map Files", "*.map"),
		         new ExtensionFilter("All Files", "*.*"));
		 File selectedFile = fileChooser.showSaveDialog(stage);
		 if (selectedFile != null) {
			 System.out.println("Selected file: " + selectedFile);
		 }
	}
	
	public void exportImage() {
		System.out.println("Exporting image");
	}
	
	public void openPreferences() {
		System.out.println("Preferences...");
	}
	
	public void openAboutApplication() {
		System.out.println("About Eddy");
	}
	
	public void generateNewMap() {
		System.out.println("Generate map");
	}
	
	/*
	 * Initialisation methods
	 */
	
	private void initStartView() {
		System.out.println("init start view");
		mainPane.getChildren().clear();
		mouseEventHandler = new StartViewEventHandler();
		
		AnchorPane.setTopAnchor(startView, 0.0);
		AnchorPane.setRightAnchor(startView, 0.0);
		AnchorPane.setBottomAnchor(startView, 0.0);
		AnchorPane.setLeftAnchor(startView, 0.0);
		mainPane.getChildren().add(startView);
		
		saveItem.setDisable(true);
		saveAsItem.setDisable(true);
		exportItem.setDisable(true);

		startView.setActive(true);
		editView.setActive(false);

		startView.getMap(0).addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventHandler);
		startView.getMap(0).setText("Label for map 0\nSome properties for map 0");
		
		startView.getMap(1).addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventHandler);
		startView.getMap(1).setText("Label for map 1\nSome properties for map 1");
		
		startView.getMap(2).addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventHandler);
		startView.getMap(2).setText("Label for map 2\nSome properties for map 2");
		
		startView.getMap(3).addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventHandler);
		startView.getMap(3).setText("Label for map 3\nSome properties for map 3");
		
		startView.getMap(4).addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventHandler);
		startView.getMap(4).setText("Label for map 4\nSome properties for map 4");
		
		startView.getMap(5).addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventHandler);
		startView.getMap(5).setText("Label for map 5\nSome properties for map 5");
	}

	private void initEditView() {
		mouseEventHandler = new EditViewEventHandler();
		
		mainPane.getChildren().clear();
		AnchorPane.setTopAnchor(editView, 0.0);
		AnchorPane.setRightAnchor(editView, 0.0);
		AnchorPane.setBottomAnchor(editView, 0.0);
		AnchorPane.setLeftAnchor(editView, 0.0);
		mainPane.getChildren().add(editView);
		
		saveItem.setDisable(false);
		saveAsItem.setDisable(false);
		exportItem.setDisable(false);

		startView.setActive(false);
		editView.setActive(true);
		
		editView.getMap(0).addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventHandler);
		editView.getMap(0).setText("Label for map 0\nSome properties for map 0");
		
		editView.getMap(1).addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventHandler);
		editView.getMap(1).setText("Label for map 1\nSome properties for map 1");
		
		editView.getMap(2).addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventHandler);
		editView.getMap(2).setText("Label for map 2\nSome properties for map 2");
		
		editView.getMap(3).addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventHandler);
		editView.getMap(3).setText("Label for map 3\nSome properties for map 3");
	}
	
	/*
	 * Event handlers
	 */
	
	private class StartViewEventHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent event) {
			System.out.println("Map: " + event.getSource());
			initEditView();
			// TODO: Populate edit view with new maps based on selected map
		}
		
	}
	
	private class EditViewEventHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			System.out.println("Map: " + event.getSource());
			// TODO: Load map in main box
		}
		
	}
}
