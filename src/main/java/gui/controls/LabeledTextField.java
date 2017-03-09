package gui.controls;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

/**
 * This control is used to display information regarding a pattern instance.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class LabeledTextField extends HBox {
	private Label label;
	private TextField textField;
	
	/**
	 * Creates an instance of this class.
	 * 
	 * @param label The control's label.
	 * @param textField The control's text field.
	 */
	public LabeledTextField(String label, TextField textField) {
		super();
		
		this.label = new Label(label);
		this.textField = textField;
		
		this.label.setLabelFor(this.textField);
		
		getChildren().addAll(this.label, this.textField);
	}
	
	/**
	 * Sets the text content of this control.
	 * 
	 * @param text The text content.
	 */
	public void setText(String text) {
		textField.setText(text);
	}
	
	/**
	 * Gets the text content of this control.
	 * 
	 * @return The text content.
	 */
	public String getText() {
		return textField.getText();
	}
	
	/**
	 * Gets a subset of this control's content. 
	 * 
	 * @param start The first position.
	 * @param end The last position.
	 * @return The text chosen content.
	 */
	public String getText(int start, int end) {
		return textField.getText(start, end);
	}
}
