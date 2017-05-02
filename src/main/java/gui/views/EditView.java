package gui.views;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class EditView extends BorderPane {
	
	@FXML private Canvas centralCanvas;

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
		
		draw();
	}

	private void draw() {
		GraphicsContext ctx = centralCanvas.getGraphicsContext2D();
		
		ctx.setFill(Color.RED);
		ctx.fillRect(0, 0, 500, 500);
	}
}
