package gui.views;

import java.io.IOException;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
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
		createWorldBtn.setMinSize(400, 100);
		
	}
	
	@FXML
	private void createWorld(ActionEvent event) throws IOException {
		ActionLogger.getInstance().storeAction(ActionType.CLICK, 
												View.LAUNCH, 
												TargetPane.BUTTON_PANE, 
												false, 
												createWorldBtn.getText());
		router.postEvent(new StartWorld(1));
	}

	

	
}
