package gui.controls;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import collectors.ActionLogger;
import collectors.XMLHandler;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import game.Room;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.ApplySuggestion;
import util.eventrouting.events.MAPEGridUpdate;
import util.eventrouting.events.MAPElitesDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestWorldView;
import util.eventrouting.events.RoomEdited;
import util.eventrouting.events.SaveCurrentGeneration;
import util.eventrouting.events.SaveDisplayedCells;
import util.eventrouting.events.StartGA_MAPE;
import util.eventrouting.events.Stop;
import util.eventrouting.events.SuggestedMapSelected;
import util.eventrouting.events.SuggestedMapsDone;
import util.eventrouting.events.SuggestionApplied;
import util.eventrouting.events.intraview.RestartDimensionsExperiment;
import util.eventrouting.events.intraview.RoomEditionStarted;

public class EvolutionMAPEPane extends AnchorPane implements Listener 
{
	@FXML private DimensionsTable MainTable;
	@FXML private DimensionsTable secondaryTable;
	
	//Suggestions
//	@FXML private GridPane suggestionsPane;
	@FXML private MAPEVisualizationPane MAPElitesPane;
	private ArrayList<SuggestionRoom> roomDisplays;
	
	//Abusive amount of labels for info
	@FXML private Label enemyNumbr;
	@FXML private Label enemyNumbr2;
	@FXML private Label treasureNmbr;
	@FXML private Label treasureNmbr2;
	@FXML private Label treasurePercent;
	@FXML private Label treasurePercent2;
	@FXML private Label enemyPercent;
	@FXML private Label enemyPercent2;
	@FXML private Label entranceSafety;
	@FXML private Label entranceSafety2;
	@FXML private Label treasureSafety;
	@FXML private Label treasureSafety2;

	@FXML private Button worldGridBtn; //ok
	@FXML private Button genSuggestionsBtn; //bra
	@FXML private Button appSuggestionsBtn; //bra
	@FXML private Button flowControlBtn; //bra
	@FXML private Button stopEABtn; //bra
	@FXML private Button saveGenBtn;

	private SuggestionRoom selectedSuggestion;

	private boolean isActive = false; //for having the same event listener in different views 
	public HashMap<Integer, Room> suggestedRooms = new HashMap<Integer, Room>();

	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	
	private int suggestionAmount = 101; //TODO: Probably this value should be from the application config!!
	private MAPEDimensionFXML[] currentDimensions = new MAPEDimensionFXML[] {};
	
	//PROVISIONAL FIX!
	public enum EvoState
	{
		STOPPED,
		RUNNING
	}
	
	public EvoState currentState;
	
	public SimpleObjectProperty<Room> currentEditedRoom = new SimpleObjectProperty<>();;
	
	
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
		
		//Setup the different Events we will be listening:
		router.registerListener(this, new MAPEGridUpdate(null));
		router.registerListener(this, new MAPElitesDone());
		router.registerListener(this, new MapUpdate(null));
		router.registerListener(this, new ApplySuggestion(0));
		router.registerListener(this, new SuggestedMapsDone());
		router.registerListener(this, new SuggestedMapSelected(null));
		router.registerListener(this, new RoomEditionStarted(null));
		router.registerListener(this, new RoomEdited(null));
		router.registerListener(this,  new RestartDimensionsExperiment(null));
		
		setupMAPElitesGUI();
		saveGenBtn.setDisable(true);
		isActive = true;
	}
	
	private void setupMAPElitesGUI()
	{
		roomDisplays = new ArrayList<SuggestionRoom>();
		
		for(int i = 0; i < suggestionAmount; i++) 
		{
			SuggestionRoom suggestion = new SuggestionRoom();
			roomDisplays.add(suggestion);
		}
			
//		suggestionsPane.setVisible(false);
		
		MAPElitesPane.init(roomDisplays, "","",0,0);
		
		MainTable.setup(2);
		MainTable.InitMainTable(MAPElitesPane);
		MainTable.setEventListeners();
		
		secondaryTable.setup(DimensionTypes.values().length);
		
		for(DimensionTypes dimension : DimensionTypes.values())
        {
        	if(dimension != DimensionTypes.SIMILARITY && dimension != DimensionTypes.SYMMETRY
        			&& dimension != DimensionTypes.DIFFICULTY
        			&& dimension != DimensionTypes.GEOM_COMPLEXITY && dimension != DimensionTypes.REWARD)
        	{
        		secondaryTable.getItems().add(new MAPEDimensionFXML(dimension, 5));
        	}
        }
		
		secondaryTable.setEventListeners();
		
		currentState = EvoState.STOPPED;
	}
	
	public void setupPane()
	{
		for(SuggestionRoom sr : roomDisplays)
		{
			sr.resizeCanvasForRoom(currentEditedRoom.get());
		}
		
		resetSuggestedRooms();
		
		genSuggestionsBtn.setText("Stopped");
		currentState = EvoState.STOPPED;
	}
	
	private void disablePane()
	{
		handleEvolutionPressed();
	}
	
	private void enablePane()
	{
		setupPane();
	}
	
	/**
	 * Edited Room
	 * @param generatedRooms
	 * @param dimension
	 * @param dimensionSizes
	 * @param indices
	 */
	public void renderCell(List<Room> generatedRooms, int dimension, float [] dimensionSizes, int[] indices)
	{
		if(dimension < 0)
		{
//			MAPElitesPane.GetGridCell(, row);
//			this.cells.add(new GACell(MAPElitesDimensions, indices));
			
			roomDisplays.get((int) (indices[1] * dimensionSizes[0] + indices[0])).setSuggestedRoom(generatedRooms.get((int) (indices[1] * dimensionSizes[0] + indices[0])));
			roomDisplays.get((int) (indices[1] * dimensionSizes[0] + indices[0])).setOriginalRoom(currentEditedRoom.get());
//			roomDisplays.get(nextRoom).setOriginalRoom(); //Maybe this does not make sense? Idk
			return;
		}
		
		for(int i = 0; i < dimensionSizes[dimension]; i++)
		{
			indices[dimension] = i;
			renderCell(generatedRooms, dimension-1, dimensionSizes, indices);
		}
	}
	

	@Override
	public void ping(PCGEvent e) {
		// TODO Auto-generated method stub
		if(e instanceof RestartDimensionsExperiment)
		{
			//Restart the pane
			setupPane();
			
			//Update the dimension list
			MAPElitesPane.dimensionsUpdated(roomDisplays, ((RestartDimensionsExperiment) e).getDimensions());
			currentDimensions = ((RestartDimensionsExperiment) e).getDimensions(); 
			
			//run the Evo again!
			handleEvolutionPressed();
		}
		else if(e instanceof RoomEditionStarted)
		{
			currentEditedRoom.set((Room) e.getPayload());
			setupPane();
		}
		else if(e instanceof RoomEdited)
		{
			currentEditedRoom.set((Room) e.getPayload());
		}
		else if(e instanceof MAPEGridUpdate)
		{
			if(currentDimensions != null && currentDimensions.length > 0)
			{
				ActionLogger.getInstance().storeAction(ActionType.CHANGE_VALUE,
														View.ROOM, 
														TargetPane.SUGGESTION_PANE, 
														false,
														currentDimensions[0].getDimension(),
														currentDimensions[0].getGranularity(),
														currentDimensions[1].getDimension(),
														currentDimensions[1].getGranularity(),
														((MAPEGridUpdate) e).getDimensions()[0].getDimension(),
														((MAPEGridUpdate) e).getDimensions()[0].getGranularity(),
														((MAPEGridUpdate) e).getDimensions()[1].getDimension(),
														((MAPEGridUpdate) e).getDimensions()[1].getGranularity()
														);
			}
			
			MAPElitesPane.dimensionsUpdated(roomDisplays, ((MAPEGridUpdate) e).getDimensions());
			currentDimensions = ((MAPEGridUpdate) e).getDimensions(); 
//			OnChangeTab();
		}
		if(e instanceof MAPElitesDone)
		{
			if (isActive) {
				//THIS NEED TO BE IMPROVED!
				List<Room> generatedRooms = ((MAPElitesDone) e).GetRooms();
//				Room room = (Room) ((MapUpdate) e).getPayload();
//				UUID uuid = ((MapUpdate) e).getID();
				LabeledCanvas canvas;
				synchronized (roomDisplays) {
					
					renderCell(generatedRooms, currentDimensions.length - 1, 
							new float [] {currentDimensions[0].getGranularity(), currentDimensions[1].getGranularity()}, new int[] {0,0});
				}
				
				Platform.runLater(() -> {
					int i = 0;
					for(SuggestionRoom sugRoom : roomDisplays)
					{
						if(sugRoom.getSuggestedRoom() != null)
						{
							sugRoom.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(sugRoom.getSuggestedRoom(), i));
						}
						else
						{
							sugRoom.getRoomCanvas().draw(null);
						}
						i++;
					}
				});
				
			}
		}
		else if(e instanceof SuggestedMapsDone) //All the evolutionary algorithms have finish their run and returned the best rooms!
		{
			getWorldGridBtn().setDisable(false);
			getGenSuggestionsBtn().setDisable(false);	
		}
		else if(e instanceof SuggestedMapSelected)
		{
			if(selectedSuggestion != null)
				selectedSuggestion.setSelected(false);
			
			selectedSuggestion = (SuggestionRoom) ((SuggestedMapSelected) e).getPayload();
			clearStats();
			
			if(selectedSuggestion == null || selectedSuggestion.getSuggestedRoom() == null)
			{
//				getAppSuggestionsBtn().setDisable(true);
				return;
			}
			
			ActionLogger.getInstance().storeAction(ActionType.CLICK,
													View.ROOM, 
													TargetPane.SUGGESTION_PANE, 
													false,
													currentDimensions[0].getDimension(),
													currentDimensions[0].getGranularity(),
													selectedSuggestion.getSuggestedRoom().getDimensionValue(currentDimensions[0].getDimension()),
													currentDimensions[1].getDimension(),
													currentDimensions[1].getGranularity(),
													selectedSuggestion.getSuggestedRoom().getDimensionValue(currentDimensions[1].getDimension()),
													selectedSuggestion.getSuggestedRoom());
			
			XMLHandler.getInstance().saveIndividualRoomXML(selectedSuggestion.getSuggestedRoom(), "clicked-suggestion\\");

			
			displayStats();
//			getAppSuggestionsBtn().setDisable(false);
		}
	}
	

	/**
	 * Initialises the map view and creates canvases for pattern drawing and
	 * infeasibility notifications.
	 */
	private void setupButtons() {
		
	
		getWorldGridBtn().setTooltip(new Tooltip("View your world map"));
		getGenSuggestionsBtn().setTooltip(new Tooltip("Generate new maps according to the current map view"));
		getAppSuggestionsBtn().setTooltip(new Tooltip("Change the current map view with your selected generated map"));

	}
	
	/***
	 * Send event that is captured in the InteractiveGUIController and returns to the world view
	 * @param event
	 * @throws IOException
	 */
	@FXML
	private void backWorldView(ActionEvent event) throws IOException 
	{
		router.postEvent(new Stop());
		router.postEvent(new RequestWorldView());	
	}
	
	@FXML
	private void handleEvolutionPressed()
	{
		switch(currentState)
		{
		case RUNNING:
			router.postEvent(new Stop());
			genSuggestionsBtn.setText("Stopped");
			currentState = EvoState.STOPPED;
			break;
		case STOPPED:
			generateNewMaps();
			genSuggestionsBtn.setText("running");
			currentState = EvoState.RUNNING;
			break;
		default:
			break;
			
		}
	}
	
	@FXML
	public void clearStats() {
		enemyNumbr.setText("");
		enemyNumbr2.setText("");
		treasureNmbr.setText("");
		treasureNmbr2.setText("");
		treasurePercent.setText("");
		treasurePercent2.setText("");
		enemyPercent.setText("");
		enemyPercent2.setText("");
		entranceSafety.setText("");
		entranceSafety2.setText("");
		treasureSafety.setText("");
		treasureSafety2.setText("");	

	}
	

	/***
	 * Applies the selected suggestion!
	 * @param event
	 * @throws IOException
	 */
	@FXML
	private void selectSuggestion(ActionEvent event) throws IOException
	{
		if(selectedSuggestion != null)
		{
			ActionLogger.getInstance().storeAction(ActionType.CHANGE_VALUE,
					View.ROOM, 
					TargetPane.SUGGESTION_PANE, 
					false,
					currentDimensions[0].getDimension(),
					currentDimensions[0].getGranularity(),
					selectedSuggestion.getSuggestedRoom().getDimensionValue(currentDimensions[0].getDimension()),
					currentDimensions[1].getDimension(),
					currentDimensions[1].getGranularity(),
					selectedSuggestion.getSuggestedRoom().getDimensionValue(currentDimensions[1].getDimension()),
					selectedSuggestion.getSuggestedRoom());

	
			XMLHandler.getInstance().saveIndividualRoomXML(selectedSuggestion.getSuggestedRoom(), "picked-room\\");
			
			router.postEvent(new SaveCurrentGeneration());
			router.postEvent(new SuggestionApplied(selectedSuggestion.getSuggestedRoom()));
			selectedSuggestion = null;
		}

	}

	/**
	 * Generates as many suggested rooms as specified
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	@FXML
	private void saveCurrentGeneration() //TODO: some changes here!
	{	
		switch(currentState)
		{
		case STOPPED:
			//Nothing happens here :D 
			break;
		case RUNNING:
			
			router.postEvent(new SaveDisplayedCells());
//			MAPElitesPane.SaveDimensionalGrid();
//			MapRenderer.getInstance().saveCurrentEditedRoom(getMapView());
			
			break;
		}

	}
	
	/**
	 * Resets the mini suggestions for a new run of room generation
	 */
	private void resetSuggestedRooms() 
	{
		router.postEvent(new Stop());
		
		if(selectedSuggestion != null)
			selectedSuggestion.setSelected(false);
		
		selectedSuggestion = null;
		clearStats();
		
		for(SuggestionRoom sr : roomDisplays)
		{
//			sr.getRoomCanvas().resizeRotatingThingie();
			sr.getRoomCanvas().draw(null);
			sr.getRoomCanvas().setText("Waiting for map...");
		}
	}
	
	/**
	 * Generates four new mini maps.
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	public void generateNewMaps(Room room) 
	{
		
		if(currentDimensions.length > 1)
		{
			router.postEvent(new StartGA_MAPE(room, currentDimensions)); 
		}
	}

	/**
	 * Generates as many suggested rooms as specified
	 * 
	 * "Why is this public?",  you ask. Because of FXML's method binding.
	 */
	@FXML
	private void generateNewMaps() //TODO: some changes here!
	{	
		resetSuggestedRooms();
		generateNewMaps(currentEditedRoom.get());
	}
	
	//TODO: MY opportunity to change this!
	@FXML
	public void displayStats() 
	{
		Room original = currentEditedRoom.get();
		Room toCompare = selectedSuggestion.getSuggestedRoom();
		
		int originalEnemies = 0;
		int compareEnemies = 0;
		
		currentEditedRoom.get().createLists();
		toCompare.createLists();
		
		StringBuilder str = new StringBuilder();
		str.append("Number of enemies: ");
		
		str.append(currentEditedRoom.get().getEnemyCount());
		str.append(" ➤  ");
		enemyNumbr.setText(str.toString());	
		str = new StringBuilder();

		str.append(toCompare.getEnemyCount());
		if (currentEditedRoom.get().getEnemyCount() > toCompare.getEnemyCount()) {
			str.append(" ▼");
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: red");
		} else if (currentEditedRoom.get().getEnemyCount() < toCompare.getEnemyCount()) {			
			str.append(" ▲");
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: green");
		} else {
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Number of treasures: ");

		str.append(currentEditedRoom.get().getTreasureCount());
		str.append(" ➤  ");
		treasureNmbr.setText(str.toString());	
		str = new StringBuilder();

		str.append(toCompare.getTreasureCount());
		if (currentEditedRoom.get().getTreasureCount() > toCompare.getTreasureCount()) {
			str.append(" ▼");
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: red");
		} else if (currentEditedRoom.get().getTreasureCount() < toCompare.getTreasureCount()) {			
			str.append(" ▲");
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: green");
		} else {
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Treasure percentage: ");

		str.append(round(currentEditedRoom.get().getTreasurePercentage()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		treasurePercent.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(toCompare.getTreasurePercentage()* 100, 2 ));
		str.append("%");

		if (currentEditedRoom.get().getTreasurePercentage() > toCompare.getTreasurePercentage()) {
			str.append(" ▼");
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: red");
		} else if (currentEditedRoom.get().getTreasurePercentage() < toCompare.getTreasurePercentage()) {			
			str.append(" ▲");
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: green");
		} else {
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Enemy percentage: ");

		str.append(round(currentEditedRoom.get().getEnemyPercentage()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		enemyPercent.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(toCompare.getEnemyPercentage()* 100, 2 ));
		str.append("%");

		if (currentEditedRoom.get().getEnemyPercentage() > toCompare.getEnemyPercentage()) {
			str.append(" ▼");
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: red");
		} else if (currentEditedRoom.get().getEnemyPercentage() < toCompare.getEnemyPercentage()) {			
			str.append(" ▲");
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: green");
		} else {
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Entrance safety: ");

		str.append(round(currentEditedRoom.get().getDoorSafeness()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		entranceSafety.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(toCompare.getDoorSafeness()* 100, 2 ));
		str.append("%");

		if (currentEditedRoom.get().getDoorSafeness() > toCompare.getDoorSafeness()) {
			str.append(" ▼");
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: red");
		} else if (currentEditedRoom.get().getDoorSafeness() < toCompare.getDoorSafeness()) {			
			str.append(" ▲");
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: green");
		} else {
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Treasure safety: ");

		Double[] safeties = currentEditedRoom.get().getAllTreasureSafeties();

		double totalSafety = 0;

		for (double d : safeties) {
			totalSafety += d;
		}

		if (safeties.length != 0) {
			totalSafety = totalSafety/safeties.length;
		}
		safeties = toCompare.getAllTreasureSafeties();

		double totalSafety2 = 0;

		for (double d : safeties) {
			totalSafety2 += d;
		}
		
		if (safeties.length != 0) {
			totalSafety2 = totalSafety2/safeties.length;
		}

		str.append(round(totalSafety * 100, 2));
		str.append("%");

		str.append(" ➤  ");
		treasureSafety.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(totalSafety2 * 100, 2));
		str.append("%");
		if (totalSafety > totalSafety2) {
			str.append(" ▼");
			treasureSafety2.setText(str.toString());
			treasureSafety2.setStyle("-fx-text-fill: red");
		} else if (totalSafety < totalSafety2) {			
			str.append(" ▲");
			treasureSafety2.setText(str.toString());
			treasureSafety2.setStyle("-fx-text-fill: green");
		} else {
			treasureSafety2.setText(str.toString());
			treasureSafety2.setStyle("-fx-text-fill: white");
		}
	}

	public static double round(double value, int places) {
		if (places < 0) {
			throw new IllegalArgumentException();
		} else if (value == 0) {
			return 0;
		}

		BigDecimal bd = new BigDecimal(value);
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}
	
	/**
	 * Marks this control as being in an active or inactive state.
	 * 
	 * @param state The new state.
	 */
	public void setActive(boolean state)
	{
		isActive = state;
		
		if(!isActive)
			disablePane();
		else
			enablePane();
		
	}
	
	public Room getSelectedMiniMap() {
		return selectedSuggestion.getSuggestedRoom();
	}

	public void setSelectedMiniMap(Room selectedMiniMap) {
		this.selectedSuggestion.setSuggestedRoom(selectedMiniMap);
	}
	

	public Button getWorldGridBtn() {
		return worldGridBtn;
	}

	public void setWorldGridBtn(Button worldGridBtn) {
		this.worldGridBtn = worldGridBtn;
	}

	public Button getGenSuggestionsBtn() {
		return genSuggestionsBtn;
	}

	public void setGenSuggestionsBtn(Button genSuggestionsBtn) {
		this.genSuggestionsBtn = genSuggestionsBtn;
	}

	public Button getAppSuggestionsBtn() {
		return appSuggestionsBtn;
	}

	public void setAppSuggestionsBtn(Button appSuggestionsBtn) {
		this.appSuggestionsBtn = appSuggestionsBtn;
	}
	
}
