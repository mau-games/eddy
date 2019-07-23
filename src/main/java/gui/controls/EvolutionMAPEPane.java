package gui.controls;

import java.io.IOException;

import javafx.beans.NamedArg;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;

public class EvolutionMAPEPane extends AnchorPane
{
	//To be called from the fxml
	public EvolutionMAPEPane()
	{
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/controls/EvolutionMAPE.fxml"));
		loader.setRoot(this);
		loader.setController(this);
		
		try {
			loader.load();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}
}
