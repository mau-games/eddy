package gui.views;

import java.io.IOException;

import collectors.DataSaverLoader;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import game.ApplicationConfig;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.StartTask2;
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
	
	@FXML public CheckBox academicBtn;
	@FXML public CheckBox industryBtn;
	//@FXML public CheckBox otherBtn;

	@FXML public TextField subject_id;

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
		createWorldBtn.setDisable(true);
	}
	
	@FXML
	private void createWorld(ActionEvent event) throws IOException {
		
		if(academicBtn.isSelected() || industryBtn.isSelected())
		{
			DataSaverLoader.SUBJECT_ID = subject_id.getText();

			if(academicBtn.isSelected())
			{
				ActionLogger.getInstance().storeAction(ActionType.CLICK, 
						View.LAUNCH, 
						TargetPane.BUTTON_PANE, 
						false, 
						"ACADEMIC",
						academicBtn.isSelected() );

				router.postEvent(new StartTask2());
			}
			else if(industryBtn.isSelected())
			{
				ActionLogger.getInstance().storeAction(ActionType.CLICK, 
						View.LAUNCH, 
						TargetPane.BUTTON_PANE, 
						false, 
						"INDUSTRY",
						industryBtn.isSelected() );

				router.postEvent(new StartWorld(1));
			}
			
			ActionLogger.getInstance().storeAction(ActionType.CLICK, 
					View.LAUNCH, 
					TargetPane.BUTTON_PANE, 
					false, 
					createWorldBtn.getText());
			
			

		}		
	}
	
	@FXML
	public void toggleIndustry()
	{
		createWorldBtn.setDisable(false);
//		if(academicBtn.isSelected())
		if(!industryBtn.isSelected())
		{
			createWorldBtn.setDisable(true);
		}
		academicBtn.setSelected(false);
		//otherBtn.setSelected(false);

	}
	
	@FXML
	public void toggleAcademic()
	{
		createWorldBtn.setDisable(false);
		if(!academicBtn.isSelected())
		{
			createWorldBtn.setDisable(true);
		}
		
		if(industryBtn != null)
		{
			industryBtn.setSelected(false);
		}

//		if(academicBtn.isSelected())
//		workBtn.setSelected(false);
//		otherBtn.setSelected(false);
	}
	
	@FXML
	public void toggleOther()
	{
		createWorldBtn.setDisable(false);
		
		
		
		if(academicBtn != null)
		{
			academicBtn.setSelected(false);
		}
		
		if(industryBtn != null)
		{
			industryBtn.setSelected(false);
		}
//		if(academicBtn.isSelected())
//		academicBtn.setSelected(false);
//		workBtn.setSelected(false);
	}

}
