package gui.controls;

import java.io.IOException;

import game.ApplicationConfig;
import javafx.animation.FadeTransition;
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
	
	public Popup()
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
	}
	
	public void setInformation(String text)
	{
		information.setText(text);
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
		fadeOut = new FadeTransition(Duration.millis(3000), this);
		fadeOut.setFromValue(1.0);
		fadeOut.setToValue(0.0);
		fadeOut.setDelay(Duration.millis(5000));
		fadeOut.play();
	}
}
