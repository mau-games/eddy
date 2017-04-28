package gui.views;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;

public class EditView extends BorderPane {

	public EditView() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/interactive/EditView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
}
