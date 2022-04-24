package gui.views;

import java.io.IOException;

import game.CoCreativity.AICoCreator;
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
import javafx.scene.text.Font;
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
	
	@FXML public CheckBox lowCheckBox;
	@FXML public CheckBox mediumCheckBox;
	@FXML public CheckBox highCheckBox;

	@FXML public Label titleLabel;

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

		AICoCreator.getInstance().initAiCoCreator(null);
		
		initGui();

	}

	public void setActive(boolean state) {
		isActive = state;
	}
	
	public void initGui() {
		createWorldBtn.setMinSize(400, 100);
		createWorldBtn.setDisable(true);

		titleLabel.setStyle("-fx-font-weight: bold");
		titleLabel.setStyle("-fx-text-fill: white;");
		titleLabel.setFont(Font.font("Arial", 20));
	}
	
	@FXML
	private void createWorld(ActionEvent event) throws IOException {
		
		//if(academicBtn.isSelected() || industryBtn.isSelected() || otherBtn.isSelected())
		//{
		//	if(academicBtn.isSelected())
		//	{
		//		ActionLogger.getInstance().storeAction(ActionType.CLICK,
		//				View.LAUNCH,
		//				TargetPane.BUTTON_PANE,
		//				false,
		//				"ACADEMIC",
		//				academicBtn.isSelected() );
		//	}
			//else if(industryBtn.isSelected())
			//{
			//	ActionLogger.getInstance().storeAction(ActionType.CLICK,
			//			View.LAUNCH,
			//			TargetPane.BUTTON_PANE,
			//			false,
			//			"INDUSTRY",
			//			industryBtn.isSelected() );
			//}
			//else
			//{
			//	ActionLogger.getInstance().storeAction(ActionType.CLICK,
			//			View.LAUNCH,
			//			TargetPane.BUTTON_PANE,
			//			false,
			//			"OTHER",
			//			otherBtn.isSelected() );
			//}
		//}

		if(lowCheckBox.isSelected() || mediumCheckBox.isSelected() || highCheckBox.isSelected()) {
			ActionLogger.getInstance().storeAction(ActionType.CLICK,
					View.LAUNCH,
					TargetPane.BUTTON_PANE,
					false,
					"ACADEMIC",
					lowCheckBox.isSelected());
		}

		ActionLogger.getInstance().storeAction(ActionType.CLICK,
				View.LAUNCH,
				TargetPane.BUTTON_PANE,
				false,
				createWorldBtn.getText());


		router.postEvent(new StartWorld(1));
	}
	
	//@FXML
	//public void toggleIndustry()
	//{
	//	createWorldBtn.setDisable(false);
//	//	if(academicBtn.isSelected())
	//	if(!industryBtn.isSelected())
	//	{
	//		createWorldBtn.setDisable(true);
	//	}
	//	academicBtn.setSelected(false);
	//	otherBtn.setSelected(false);
//
	//}
	
	//@FXML
	//public void toggleAcademic()
	//{
	//	createWorldBtn.setDisable(false);
	//	if(!academicBtn.isSelected())
	//	{
	//		createWorldBtn.setDisable(true);
	//	}
	//
	//	if(industryBtn != null)
	//	{
	//		industryBtn.setSelected(false);
	//	}
	//
	//	if(otherBtn != null)
	//	{
	//		otherBtn.setSelected(false);
	//	}
//	//	if(academicBtn.isSelected())
//	//	workBtn.setSelected(false);
//	//	otherBtn.setSelected(false);
	//}
	
	//@FXML
	//public void toggleOther()
	//{
	//	createWorldBtn.setDisable(false);
	//
	//	if(!otherBtn.isSelected())
	//	{
	//		createWorldBtn.setDisable(true);
	//	}
//
	//	if(academicBtn != null)
	//	{
	//		academicBtn.setSelected(false);
	//	}
	//
	//	if(industryBtn != null)
	//	{
	//		industryBtn.setSelected(false);
	//	}
//	//	if(academicBtn.isSelected())
//	//	academicBtn.setSelected(false);
//	//	workBtn.setSelected(false);
	//}

	@FXML public void toggleLowAIControl()
	{
		AICoCreator.getInstance().setControlLevel(AICoCreator.ControlLevel.LOW);
		mediumCheckBox.setSelected(false);
		highCheckBox.setSelected(false);
		createWorldBtn.setDisable(false);

		if(!lowCheckBox.isSelected())
		{
			createWorldBtn.setDisable(true);
		}
	}

	@FXML public void toggleMediumAIControl()
	{
		AICoCreator.getInstance().setControlLevel(AICoCreator.ControlLevel.MEDIUM);
		lowCheckBox.setSelected(false);
		highCheckBox.setSelected(false);
		createWorldBtn.setDisable(false);

		if(!mediumCheckBox.isSelected())
		{
			createWorldBtn.setDisable(true);
		}
	}

	@FXML public void toggleHighAIControl()
	{
		AICoCreator.getInstance().setControlLevel(AICoCreator.ControlLevel.HIGH);
		lowCheckBox.setSelected(false);
		mediumCheckBox.setSelected(false);
		createWorldBtn.setDisable(false);

		if(!highCheckBox.isSelected())
		{
			createWorldBtn.setDisable(true);
		}
	}

}
