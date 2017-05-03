package gui.views;

import java.io.IOException;
import java.util.List;

import gui.controls.LabeledCanvas;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

/**
 * his class controls the interactive application's edit view.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class EditViewController extends BorderPane {
	
	@FXML private Canvas centralCanvas;
	@FXML private List<LabeledCanvas> maps;
	
	boolean isActive = false;

	/**
	 * Creates an instance of this class.
	 */
	public EditViewController() {
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
	
	public void setActive(boolean state) {
		isActive = state;
	}

	/**
	 * Draws stuff on the canvas. Useful only for testing at the moment...
	 */
	private void draw() {
		GraphicsContext ctx = centralCanvas.getGraphicsContext2D();
		
		ctx.setFill(Color.RED);
		ctx.fillRect(0, 0, 500, 500);
	}
}
