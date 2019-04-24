package util.eventrouting.events;

import machineLearning.PreferenceModel;
import util.eventrouting.PCGEvent;

public class UpdatePreferenceModel extends PCGEvent
{
	
	protected PreferenceModel currentUserModel;
	
	public UpdatePreferenceModel(PreferenceModel currentModel)
	{
		this.currentUserModel = currentModel;
	}
	
	public PreferenceModel getCurrentUserModel()
	{
		return currentUserModel;
	}
}
