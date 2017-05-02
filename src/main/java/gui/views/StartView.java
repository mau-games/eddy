package gui.views;

import java.io.IOException;

import gui.controls.LabeledCanvas;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.GridPane;

public class StartView extends GridPane {

	@FXML private LabeledCanvas map1;
	@FXML private LabeledCanvas map2;
	@FXML private LabeledCanvas map3;
	@FXML private LabeledCanvas map4;
	@FXML private LabeledCanvas map5;
	@FXML private LabeledCanvas map6;

	public StartView() {
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
}
