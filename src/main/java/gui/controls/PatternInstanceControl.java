package gui.controls;

import java.util.IdentityHashMap;

import finder.patterns.Pattern;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import util.eventrouting.EventRouter;
import util.eventrouting.events.RequestRedraw;

/**
 * This control is used to display information regarding a pattern instance.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class PatternInstanceControl extends HBox {
	private CheckBox active = null;
	private ColorPicker picker;
	private Pane pane = new Pane();
	
	private Pattern pattern;
	private IdentityHashMap<Pattern, Color> activePatterns;
	
	/**
	 * Creates an instance of this class.
	 * 
	 * @param index The index of the found pattern instance. This will be used
	 * 		as the control's label.
	 * @param colour The colour that will be used to identify the pattern instance.
	 * @param pattern A reference to the pattern.
	 * @param activePatterns A reference to a list of active pattern instances.
	 */
	public PatternInstanceControl(int index,
			Color colour,
			Pattern pattern,
			IdentityHashMap<Pattern, Color> activePatterns) {
		super();
		
		this.pattern = pattern;
		this.activePatterns = activePatterns;
		
		active = new CheckBox("" + index);
		picker = new ColorPicker(colour);
		
		getChildren().addAll(active, pane, picker);
		HBox.setHgrow(pane, Priority.ALWAYS);
		
		active.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				PatternInstanceControl pic = PatternInstanceControl.this;
				if (pic.active.isSelected()) {
					pic.activePatterns.put(pattern, pic.picker.getValue());
				} else {
					pic.activePatterns.remove(pic.pattern);
				}
				EventRouter.getInstance().postEvent(new RequestRedraw());
			}
		});
		
		picker.setOnAction(new EventHandler<ActionEvent>() {
			PatternInstanceControl pic = PatternInstanceControl.this;
			
			@Override
			public void handle(ActionEvent e) {
				if (pic.activePatterns.containsKey(pic.pattern)) {
					pic.activePatterns.put(pic.pattern, pic.picker.getValue());
					EventRouter.getInstance().postEvent(new RequestRedraw());
				}
			}
		});
	}
	
	/**
	 * Returns the pattern associated with this control.
	 * 
	 * @return The associated pattern.
	 */
	public Pattern getPattern() {
		return pattern;
	}
	
	/**
	 * Returns the chosen colour.
	 * 
	 * @return The chosen colour.
	 */
	public Color getColour() {
		return picker.getValue();
	}
	
	/**
	 * Sets the selected state of this pattern instance.
	 * 
	 * @param state The desired state.
	 */
	public void setSelected(boolean state) {
		active.setSelected(state);
	}
	
	/**
	 * Returns true if this pattern instance is selected, otherwise false.
	 * 
	 * @return True if selected, otherwise false.
	 */
	public boolean isSelected() {
		return active.isSelected();
	}
}
