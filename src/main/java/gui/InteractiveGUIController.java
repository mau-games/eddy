package gui;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import gui.views.EditViewController;
import gui.views.StartViewController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;

public class InteractiveGUIController implements Initializable, Listener {
	
	@FXML private AnchorPane mainPane;
	
	StartViewController startView = null;
	EditViewController editView = null;
	EventHandler<MouseEvent> mouseEventHandler = null;
	
	Stage stage = null;

	@Override
	public void ping(PCGEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
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
			 // TODO: Load map and switch to edit view
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
	
	public void openPreferences() {
		System.out.println("Preferences...");
	}
	
	public void openAboutApplication() {
		System.out.println("About Eddy");
	}
	
//	public void
	
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
	}
	
	/*
	 * Event handlers
	 */
	
	private class StartViewEventHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent event) {
			System.out.println("Map: " + event.getSource());
			initEditView();
		}
		
	}
	
	private class EditViewEventHandler implements EventHandler<MouseEvent> {
		@Override
		public void handle(MouseEvent event) {
			System.out.println("Map: " + event.getSource());
			initEditView();
		}
		
	}
}
