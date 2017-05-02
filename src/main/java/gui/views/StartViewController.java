package gui.views;

import java.io.IOException;
import java.util.List;

import gui.controls.LabeledCanvas;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;

/**
 * This class controls the interactive application's start view.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class StartViewController extends GridPane {

	@FXML private List<LabeledCanvas> maps;

	/**
	 * Creates an instance of this class.
	 */
	public StartViewController() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/interactive/StartView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
	
	/**
	 * Gets one of the maps (i.e. a labeled view displaying a map) being under
	 * this object's control.
	 * 
	 * @param index An index of a map.
	 * @return A map if it exists, otherwise null.
	 */
	public LabeledCanvas getMap(int index) {
		return maps.get(index);
	}
}
