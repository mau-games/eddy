package gui.controls;

import java.io.IOException;

import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

/**
 * This control is used to display a labeled image.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class LabeledCanvas extends BorderPane {
	@FXML private Label label;
	@FXML private ResizableCanvas canvas;
	@FXML private AnchorPane canvasPane;
	@FXML private BorderPane rootPane;
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
		
		
		canvas.widthProperty().bind(canvasPane.widthProperty());
		canvas.heightProperty().bind(canvasPane.heightProperty());
		canvasPane.setPrefSize(rootPane.widthProperty().doubleValue(), 100);

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
	 * Returns the canvas's graphics context.
	 * 
	 * @return A graphics context.
	 */
	public GraphicsContext getGraphicsContext() {
		return gc.getCanvas().getGraphicsContext2D();
	}
	
	/**
	 * Gets the label's text.
	 * 
	 * @return The label's text value.
	 */
    public String getText() {
        return label.getText();
    }
	
	/**
	 * Sets the label's text.
	 * 
	 * @param label The text to display.
	 */
    public void setText(String value) {
    	label.setText(value);
    }

    /**
     * Gets the label's text property.
     * 
     * @return The label's text property.
     */
    public StringProperty textProperty() {
        return label.textProperty();
    }
}
