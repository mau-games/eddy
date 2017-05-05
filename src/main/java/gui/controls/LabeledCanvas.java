package gui.controls;

import java.io.IOException;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
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
	@FXML private AnchorPane labelPane;
	@FXML private BorderPane rootPane;
	
	private GraphicsContext gc;
	private boolean selectable = false;
	
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
		
		rootPane.setMinSize(0, 0);
		canvasPane.setMinSize(0, 0);
		labelPane.setMinSize(0, 0);
		
		canvas.widthProperty().bind(canvasPane.widthProperty());
		canvas.heightProperty().bind(canvasPane.heightProperty());
		canvasPane.setPrefSize(rootPane.widthProperty().doubleValue(), 100);

		getStyleClass().add("labeled-canvas");
		this.label.setLabelFor(this.canvas);
		this.label.setText(label);
		gc = canvas.getGraphicsContext2D();
		
		addEventFilter(MouseEvent.MOUSE_ENTERED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				highlight(true);
			}
			
		});
		addEventFilter(MouseEvent.MOUSE_EXITED, new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				highlight(false);
			}
			
		});
	}
	
	/**
	 * Denotes whether the control is selectable or not.
	 * 
	 * @param newState The new state
	 */
	public void setSelectable(boolean newState) {
		selectable = newState;
	}
	
	/**
	 * Displays an image on the canvas.
	 * 
	 * @param image An image.
	 */
	public void draw(Image image) {
		if (image == null) {
			selectable = false;
		} else {
			selectable = true;
		}
		canvas.draw(image);
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
    	Platform.runLater(() -> {
    		label.setText(value);
    	});
    }

    /**
     * Gets the label's text property.
     * 
     * @return The label's text property.
     */
    public StringProperty textProperty() {
        return label.textProperty();
    }
    
    /**
     * Highlights the control.
     * 
     * @param state True if highlighted, otherwise false.
     */
    private void highlight(boolean state) {
    	if (state && selectable) {
    		setStyle("-fx-border-width: 2px; -fx-border-color: #6b87f9");
    	} else {
    		setStyle("-fx-border-width: 0px");
    	}
    }
}
