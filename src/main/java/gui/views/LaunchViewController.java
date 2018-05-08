package gui.views;

import java.io.IOException;

import game.ApplicationConfig;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.StartWorld;

/*  
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 */

public class LaunchViewController extends BorderPane implements Listener{

	private ApplicationConfig config;
	private boolean isActive = false;
	private EventRouter router = EventRouter.getInstance();
	
	@FXML private BorderPane buttonPane2;
	@FXML private Button createWorldBtn;
	@FXML private ComboBox<String> worldSizeBox;
	
	private int selectedSize;

	@Override
	public void ping(PCGEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public LaunchViewController() {
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/interactive/LaunchView.fxml"));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
			config = ApplicationConfig.getInstance();
		} catch (IOException ex) {

		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		initGui();
	}

	public void setActive(boolean state) {
		isActive = state;
	}
	
	public void initGui() {
		worldSizeBox.getItems().clear();
		createWorldBtn.setMinSize(400, 100);
		
		ObservableList<String> options = 
			    FXCollections.observableArrayList(
			        "2x2",
			        "3x3",
			        "4x4",
			        "5x5",
			        "6x6",
			        "7x7"
			    );
		worldSizeBox.getItems().addAll(options);
		worldSizeBox.setValue("World size...");
		
	}
	
	@FXML
	private void createWorld(ActionEvent event) throws IOException {
		router.postEvent(new StartWorld(getWorldSize()));
	}
	
	public int getWorldSize() {
      
        if (worldSizeBox.getValue().equals("2x2")) {
        	selectedSize = 2;
        } else if (worldSizeBox.getValue().equals("3x3")) {
        	selectedSize = 3;
        } else if (worldSizeBox.getValue().equals("4x4")) {
        	selectedSize = 4;
        } else if (worldSizeBox.getValue().equals("5x5")) {
        	selectedSize = 5;
        } else if (worldSizeBox.getValue().equals("6x6")) {
        	selectedSize = 6;
        } else if (worldSizeBox.getValue().equals("7x7")) {
        	selectedSize = 7;
        }
        return selectedSize;
    }

	
}
