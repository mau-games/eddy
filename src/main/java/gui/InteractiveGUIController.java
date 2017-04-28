package gui;

import java.net.URL;
import java.util.ResourceBundle;

import gui.views.EditView;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.AnchorPane;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;

public class InteractiveGUIController implements Initializable, Listener {
	
	@FXML private AnchorPane mainPane;

	@Override
	public void ping(PCGEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		EditView ev = new EditView();
		AnchorPane.setTopAnchor(ev, 0.0);
		AnchorPane.setRightAnchor(ev, 0.0);
		AnchorPane.setBottomAnchor(ev, 0.0);
		AnchorPane.setLeftAnchor(ev, 0.0);
		mainPane.getChildren().add(ev);
	}

}
