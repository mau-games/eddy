package gui.utils;

import java.util.ArrayList;
import java.util.HashMap;

import gui.controls.Popup;
import javafx.geometry.Bounds;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

public class InformativePopupManager 
{
	public enum PresentableInformation
	{
		LOCK_RESTART,
		ROOM_INFEASIBLE,
		ROOM_INFEASIBLE_LOCK,
		CHANGE_DIMENSIONS,
		NO_BOSS_YET,
		ROOMS_CONNECTED,
		PLACE_ONE_POSITION,
		PLACE_TWO_POSITIONS

	}
	
	private static InformativePopupManager instance = null;
	private ArrayList<Popup> currentPopups;
	private AnchorPane mainPane;
	
	//How many times have we actualy render the popup
	private HashMap<PresentableInformation, Integer> popupCounter;
	//Is the Popup active?
	private HashMap<PresentableInformation, Popup> popupActive;
	
	private InformativePopupManager()
	{
		currentPopups = new ArrayList<Popup>();
		popupCounter = new HashMap<PresentableInformation, Integer>();
		popupActive = new HashMap<PresentableInformation, Popup>();
	}
	
	public static InformativePopupManager getInstance()
	{
		if(instance == null)
		{
			instance = new InformativePopupManager();
		}
		
		return instance;
	}
	
	public void setMainPane(AnchorPane main)
	{
		mainPane = main;
	}
	
	public void restartPopups()
	{
		popupCounter.clear();
		popupActive.clear();
		
		popupCounter = new HashMap<PresentableInformation, Integer>();
		popupActive = new HashMap<PresentableInformation, Popup>();
	}
	
	public void requestPopup(Pane requestingPane, PresentableInformation type, String info)
	{
		if(popupActive.containsKey(type) || (popupCounter.containsKey(type) && popupCounter.get(type) > 2))
		{
			System.out.println("Cannot spawn Informative Popup!");
			return;
		}
		
		Bounds requesterBounds = requestingPane.localToScene(requestingPane.getBoundsInLocal());
		if(info == "") info = TextForPopup(type);
		
		double popupPosY = requesterBounds.getMaxY() + 5;
		double popupPosX = requesterBounds.getMinX();
		
		Popup newPopup = new Popup(popupPosX, popupPosY, type);
		newPopup.setInformation(info);
		
		mainPane.getChildren().add(newPopup);
		currentPopups.add(newPopup);
		
		popupCounter.put(type, !popupCounter.containsKey(type) ? 1 : popupCounter.get(type) + 1);
		popupActive.put(type, newPopup);
	}
	
	public void popupFinished(PresentableInformation finishedType)
	{
		if(popupActive.containsKey(finishedType))
		{
			Popup fin = popupActive.get(finishedType);
			mainPane.getChildren().remove(fin);
			currentPopups.remove(fin);
			popupActive.remove(finishedType);
		}
	}
	
	private String TextForPopup(PresentableInformation type)
	{
		String text = "no lcue";
		
		switch(type)
		{
		case CHANGE_DIMENSIONS:
			break;
		case ROOM_INFEASIBLE:
			text="Your room is infeasible due to unreachable areas. Not even my Deep Neural Network was able to play!";
			break;
		case ROOM_INFEASIBLE_LOCK:
			text="If you lock an unpassable region, it will always be infeasible!";
			break;
		case LOCK_RESTART:
			text="The locked tiles will appear after you RESTART the EA.";
			break;
		case NO_BOSS_YET:
			text="There are no bosses yet, don't you want challenge?";
			break;
		case ROOMS_CONNECTED:
			text="Rooms must be connected and all areas should be reachable, else they are infeasible!";
			break;
			case PLACE_ONE_POSITION:
				text="This action requires one position to be placed on the map!";
				break;
				case PLACE_TWO_POSITIONS:
				text= "This action requires two positions to be placed on the map!";
			default:
			break;
		}
		return text;
	}
}
