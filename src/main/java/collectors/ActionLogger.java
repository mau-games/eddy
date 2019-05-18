package collectors;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.UUID;

import javafx.util.Duration;
import machineLearning.neuralnetwork.DataTupleManager;
import util.eventrouting.PCGEvent;

public class ActionLogger {

	private static ActionLogger instance;
	
	//I should save from time to time, every 30 secs?
	private Duration saveTime = new Duration(30000);
	protected UUID id;
	private ArrayList<ActionLog> logs;
	private File directory;
	//General information to be saved
	public enum ActionType
	{
		CLICK,
		CHANGE,
		CHANGE_VALUE,
		CHANGE_TILE,
		CHANGE_POSITION,
		CREATE,
		CREATE_CONNECTION,
		CREATE_ROOM,
		REMOVE,
		REMOVE_CONNECTION,
		REMOVE_ROOM
	}
	
	//General information to be saved
	public enum View
	{
		LAUNCH,
		WORLD,
		SUGGESTION,
		ROOM
	}
	
	public enum TargetPane
	{
		BUTTON_PANE,
		SUGGESTION_PANE,
		BRUSH_PANE,
		TILE_PANE,
		MAP_PANE,
		WORLD_MAP_PANE
	}
	
	private ActionLogger()
	{
		id = UUID.randomUUID();
		logs = new ArrayList<ActionLog>();
	}
	
	public static ActionLogger getInstance()
	{
		if(instance == null)
			instance = new ActionLogger();
		
		return instance;
	}
	
	public void storeAction(ActionType action, View currentView, TargetPane targetPane, boolean grouped, Object... event)
	{
		ActionLog log = new ActionLog(new Timestamp(System.currentTimeMillis()), action, currentView, targetPane, grouped, event);
		logs.add(log);
	}
	
	public void init()
	{
		DataSaverLoader.SaveData(getHeader(), "\\summer-school\\" + id.toString(), "actionLogs");
	}
	
	private String getHeader()
	{
		return "Time;View;Pane;Action;Grouped;Target" + System.lineSeparator();
	}
	
	public void saveNFlush()
	{
		StringBuilder inf = new StringBuilder();

		for(ActionLog log : logs)
		{
			inf.append(log.toString() + System.lineSeparator());;
		}
		
		DataSaverLoader.SaveData(inf.toString(), "\\summer-school\\" + id.toString(), "actionLogs");
		logs.clear();
	}
	
	public class ActionLog
	{
		Timestamp timeStamp;
		ActionType action;
		View currentView;
		TargetPane targetPane;
		TargetHolder event; //This wont be 
		Boolean grouped;
		
		public ActionLog(Timestamp timeStamp, ActionType action, View currentView, TargetPane targetPane, Boolean grouped, Object... information)
		{
			this.timeStamp = timeStamp;
			this.action = action;
			this.currentView = currentView;
			this.targetPane = targetPane;
			this.grouped = grouped;
			this.event = new TargetHolder(information);
		}
		
		@Override
		public String toString()
		{
			return timeStamp + ";" + currentView + ";" + targetPane + ";" + action + ";" + grouped+ ";" + event.getInformation();
		}
		
	}
	
	public class TargetHolder
	{
		Object[] information;
		
		public TargetHolder(Object... objects )
		{
			information = new Object[objects.length];
			int index = 0;
			for(Object object : objects)
			{
				information[index++] = object;
			}
		}
		
		public String getInformation()
		{
			String result = "";
			for(int i = 0; i < information.length; i++)
			{
				result += information[i].toString();
				
				if(i != information.length -1 )
				{
					result += ";";
				}
			}
			
			return result;
		}
	}
	
	
}
