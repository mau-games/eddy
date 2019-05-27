package gui.controls;

import java.io.IOException;

import game.ApplicationConfig;
import gui.utils.InformativePopupManager;
import gui.utils.InformativePopupManager.PresentableInformation;
import javafx.animation.FadeTransition;
import javafx.beans.NamedArg;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import util.config.MissingConfigurationException;

public class Popup extends BorderPane
{
	@FXML public ImageView teller;
	@FXML public Label information;
	
	FadeTransition fadeOut;
	protected PresentableInformation informationType;
	
	//To be called from the fxml
	public Popup(@NamedArg("text") String text, @NamedArg("width") double width,  @NamedArg("height") double height, @NamedArg("image") boolean image)
	{
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/controls/PopupInfo.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		
		try {
			loader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		
		if(!image)
		{
			setLeft(null);
		}
		
		setInformation(text);
		setWidth(width);
		setHeight(height);
	}
	
	public Popup(@NamedArg("text") String text)
	{
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/controls/PopupInfo.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		
		try {
			loader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		
		setInformation(text);
	}
	
	public Popup(PresentableInformation type)
	{
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/controls/PopupInfo.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		
		try {
			loader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		show();
		informationType = type;
	}
	
	public Popup(double x, double y, PresentableInformation type)
	{
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/controls/PopupInfo.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		
		try {
			loader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
		
		show();
		
		setLayoutX(x);
		setLayoutY(y);
		informationType = type;
	}
	
	public void setInformation(String text)
	{
		information.setText(text);
	}
	
	public void setPosition(double x, double y)
	{
		setLayoutX(x);
		setLayoutY(y);
	}
	
	public void detach()
	{
//		this.getParent().
	}
	
	public boolean isBeingRendered()
	{
		return getParent() != null;
	}
	
	public void show()
	{
		fadeOut = new FadeTransition(Duration.millis(1500), this);
		fadeOut.setFromValue(1.0);
		fadeOut.setToValue(0.0);
		fadeOut.setDelay(Duration.millis(3000));
		fadeOut.play();
		
		fadeOut.onFinishedProperty().set(new EventHandler<ActionEvent>() {
	        @Override 
	        public void handle(ActionEvent actionEvent) {
	            InformativePopupManager.getInstance().popupFinished(informationType);
	        }
	    });
	}
}
