package gui;

import java.net.URL;
import java.util.ResourceBundle;

import gui.views.EditViewController;
import gui.views.StartViewController;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;

public class InteractiveGUIController implements Initializable, Listener {
	
	@FXML private AnchorPane mainPane;
	
	StartViewController sv = null;
	EditViewController ev = null;
	EventHandler<MouseEvent> eh = null;

	@Override
	public void ping(PCGEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		sv = new StartViewController();
		ev = new EditViewController();
		eh = new StartViewEventHandler();
		
		AnchorPane.setTopAnchor(sv, 0.0);
		AnchorPane.setRightAnchor(sv, 0.0);
		AnchorPane.setBottomAnchor(sv, 0.0);
		AnchorPane.setLeftAnchor(sv, 0.0);
		mainPane.getChildren().add(sv);

		sv.getMap(0).addEventFilter(MouseEvent.MOUSE_CLICKED, eh);
		sv.getMap(0).setText("Label for map 0\nSome properties for map 0");
		
		sv.getMap(1).addEventFilter(MouseEvent.MOUSE_CLICKED, eh);
		sv.getMap(1).setText("Label for map 1\nSome properties for map 1");
		
		sv.getMap(2).addEventFilter(MouseEvent.MOUSE_CLICKED, eh);
		sv.getMap(2).setText("Label for map 2\nSome properties for map 2");
		
		sv.getMap(3).addEventFilter(MouseEvent.MOUSE_CLICKED, eh);
		sv.getMap(3).setText("Label for map 3\nSome properties for map 3");
		
		sv.getMap(4).addEventFilter(MouseEvent.MOUSE_CLICKED, eh);
		sv.getMap(4).setText("Label for map 4\nSome properties for map 4");
		
		sv.getMap(5).addEventFilter(MouseEvent.MOUSE_CLICKED, eh);
		sv.getMap(5).setText("Label for map 5\nSome properties for map 5");
	}

	private void initEditView() {
		mainPane.getChildren().clear();
		AnchorPane.setTopAnchor(ev, 0.0);
		AnchorPane.setRightAnchor(ev, 0.0);
		AnchorPane.setBottomAnchor(ev, 0.0);
		AnchorPane.setLeftAnchor(ev, 0.0);
		mainPane.getChildren().add(ev);
	}
	
	private class StartViewEventHandler implements EventHandler<MouseEvent> {

		@Override
		public void handle(MouseEvent event) {
			System.out.println("Map: " + event.getSource());
			initEditView();
		}
		
	}
}
