package gui.controls;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;

/**
 * This control is used to display a labeled image.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class LabeledCanvas extends VBox {
	@FXML private Label label;
	@FXML private Canvas canvas;
	private GraphicsContext gc;
	
	/**
	 * Creates an instance of this class.
	 */
	public LabeledCanvas() {
		super();
		init("default");
	}
	
	/**
	 * Creates an instance of this class.
	 * 
	 * @param label The control's label.
	 */
	public LabeledCanvas(String label) {
		super();
		init(label);
	}
	
	private void init(String label) {
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/controls/LabeledCanvas.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}

		getStyleClass().add("labeled-canvas");
		this.label.setLabelFor(this.canvas);
		this.label.setText(label);
		gc = canvas.getGraphicsContext2D();
	}
	
	/**
	 * Displays an image on the canvas.
	 * 
	 * @param image An image.
	 */
	public void drawImage(Image image) {
		double width = gc.getCanvas().getWidth();
		double height = gc.getCanvas().getHeight();
		
		gc.drawImage(image, 0, 0, width, height);
	}
	
	/**
	 * Updates the label.
	 * 
	 * @param label The new text to display.
	 */
	public void updateLabel(String label) {
		this.label.setText(label);
	}
}
