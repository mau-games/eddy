package gui.views;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import game.*;
import game.Game.MapMutationType;
import game.tiles.*;
import generator.algorithm.Algorithm.AlgorithmTypes;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import gui.controls.*;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import org.apache.commons.io.FileUtils;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;
import util.eventrouting.events.intraview.EditedRoomToggleLocks;
import util.eventrouting.events.intraview.EditedRoomTogglePatterns;
import util.eventrouting.events.intraview.InteractiveRoomBrushUpdated;
import util.eventrouting.events.intraview.UserEditedRoom;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * This class controls the visualization of scaled maps
 * 
 * @author Johan Holmberg, Malmö University
 * @author Alberto Alvarez, Malmö University
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University
 * @author Petter Rignell, Malmö University
 */

//@Todo: NO INTERACTIVE MAP, TWO MAPS IN SCALEDROOM (LIKE SUGGESTIONROOM)
public class ScaleViewController extends BorderPane implements Listener
{
	private ScaledMapsVisualizationPane scaledMapsVisualizationPane;
	private MapRenderer renderer = MapRenderer.getInstance();
	//Brush Slider
	@FXML private Slider brushSlider;

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

	@FXML private Button worldGridBtn;
	@FXML private Button genSuggestionsBtn;
	@FXML private Button appSuggestionsBtn;
	@FXML private Button saveGenBtn;
	private ScaledRoom selectedSuggestion;

	private MapContainer map;
	private boolean isActive = false; //for having the same event listener in different views
	private static EventRouter router = EventRouter.getInstance();
//	private final static Logger logger = LoggerFactory.getLogger(RoomViewController.class);
	private ApplicationConfig config;
	private int requestedSuggestion;
	public Drawer myBrush;
	private MAPEDimensionFXML[] currentDimensions = new MAPEDimensionFXML[] {};
	private ArrayList<ScaledRoom> roomDisplays;

	//PROVISIONAL FIX!
	public enum EvoState
	{
		STOPPED,
		RUNNING
	}
	public EvoState currentState;

	/**
	 * Creates an instance of this class.
	 */
	@SuppressWarnings("unchecked")
	public ScaleViewController() {
		super();
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
				"/gui/interactive/ScaleView.fxml"));
		fxmlLoader.setRoot(this);
		fxmlLoader.setController(this);

		try {
			fxmlLoader.load();
			config = ApplicationConfig.getInstance();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		} catch (MissingConfigurationException e) {
//			logger.error("Couldn't read config file.");
		}

		router.registerListener(this, new ApplySuggestion(0));
		router.registerListener(this, new SuggestedMapsDone());
		router.registerListener(this, new SuggestedMapSelected(null, -1));

		myBrush = new Drawer();

		brushSlider.valueProperty().addListener((obs, oldval, newVal) -> { 
//			redrawPatterns(mapView.getMap());
			myBrush.SetBrushSize((int)(brushSlider.getValue()));
			ActionLogger.getInstance().storeAction(ActionType.CHANGE_VALUE, 
													View.ROOM, 
													TargetPane.BRUSH_PANE,
													false,
													oldval,
													newVal); //Point 
			});
		
		currentState = EvoState.RUNNING;
		saveGenBtn.setDisable(false);
	}
	
	/**
	 * Initialises the edit view.
	 */
	public void init(ArrayList<Room> rooms) {

		roomDisplays = new ArrayList<ScaledRoom>();
		int i = 0;

		for(Room room: rooms)
		{
			ScaledRoom scaledRoom = new ScaledRoom();
			roomDisplays.add(scaledRoom);
			roomDisplays.get(i).setScaledRoom(room);
			i++;
		}

		scaledMapsVisualizationPane.init(roomDisplays, 0, 0);
	}

	public void renderMaps(){
		Platform.runLater(() -> {
			int i = 0;
			for (ScaledRoom scaledRoom : roomDisplays) {
				if (scaledRoom.getScaledRoom() != null) {
					scaledRoom.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(scaledRoom.getScaledRoom(), i));
				} else {
					scaledRoom.getRoomCanvas().draw(null);
				}
				i++;
			}
		});
	}

	public void setContainer(MapContainer map) {
		map = this.map;
	}

	@Override
	public void ping(PCGEvent e) {

		if (e instanceof ApplySuggestion )
		{
			requestedSuggestion = (int) ((ApplySuggestion) e).getPayload();
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

			selectedSuggestion = (ScaledRoom) ((SuggestedMapSelected) e).getPayload();
			clearStats();

			if(selectedSuggestion == null || selectedSuggestion.getScaledRoom() == null)
			{
				getAppSuggestionsBtn().setDisable(true);
				return;
			}

			ActionLogger.getInstance().storeAction(ActionType.CLICK,
					View.ROOM,
					TargetPane.SUGGESTION_PANE,
					false,
					currentDimensions[0].getDimension(),
					currentDimensions[0].getGranularity(),
					selectedSuggestion.getScaledRoom().getDimensionValue(currentDimensions[0].getDimension()),
					currentDimensions[1].getDimension(),
					currentDimensions[1].getGranularity(),
					selectedSuggestion.getScaledRoom().getDimensionValue(currentDimensions[1].getDimension()),
					selectedSuggestion.getScaledRoom());

			selectedSuggestion.getScaledRoom().getRoomXML("clicked-suggestion" + File.separator + File.separator);

			displayStats();
			getAppSuggestionsBtn().setDisable(false);
		}
	}


	/**
	 * Marks this control as being in an active or inactive state.
	 * 
	 * @param state The new state.
	 */
	public void setActive(boolean state) {
		isActive = state;
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

	@FXML
	public void displayStats() 
	{
		Room origRoom = roomDisplays.get(1).getScaledRoom();
		Room selectedRoom = selectedSuggestion.getScaledRoom();
		
		int originalEnemies = 0;
		int compareEnemies = 0;
		
		origRoom.createLists();
		selectedRoom.createLists();
		
		StringBuilder str = new StringBuilder();
		str.append("Number of enemies: ");
		
		str.append(origRoom.getEnemyCount());
		str.append(" ➤  ");
		enemyNumbr.setText(str.toString());	
		str = new StringBuilder();

		str.append(selectedRoom.getEnemyCount());
		if (origRoom.getEnemyCount() > selectedRoom.getEnemyCount()) {
			str.append(" ▼");
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: red");
		} else if (origRoom.getEnemyCount() < selectedRoom.getEnemyCount()) {
			str.append(" ▲");
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: green");
		} else {
			enemyNumbr2.setText(str.toString());
			enemyNumbr2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Number of treasures: ");

		str.append(origRoom.getTreasureCount());
		str.append(" ➤  ");
		treasureNmbr.setText(str.toString());	
		str = new StringBuilder();

		str.append(selectedRoom.getTreasureCount());
		if (origRoom.getTreasureCount() > selectedRoom.getTreasureCount()) {
			str.append(" ▼");
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: red");
		} else if (origRoom.getTreasureCount() < selectedRoom.getTreasureCount()) {
			str.append(" ▲");
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: green");
		} else {
			treasureNmbr2.setText(str.toString());
			treasureNmbr2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Treasure percentage: ");

		str.append(round(origRoom.getTreasurePercentage()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		treasurePercent.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(selectedRoom.getTreasurePercentage()* 100, 2 ));
		str.append("%");

		if (origRoom.getTreasurePercentage() > selectedRoom.getTreasurePercentage()) {
			str.append(" ▼");
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: red");
		} else if (origRoom.getTreasurePercentage() < selectedRoom.getTreasurePercentage()) {
			str.append(" ▲");
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: green");
		} else {
			treasurePercent2.setText(str.toString());
			treasurePercent2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Enemy percentage: ");

		str.append(round(origRoom.getEnemyPercentage()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		enemyPercent.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(selectedRoom.getEnemyPercentage()* 100, 2 ));
		str.append("%");

		if (origRoom.getEnemyPercentage() > selectedRoom.getEnemyPercentage()) {
			str.append(" ▼");
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: red");
		} else if (origRoom.getEnemyPercentage() < selectedRoom.getEnemyPercentage()) {
			str.append(" ▲");
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: green");
		} else {
			enemyPercent2.setText(str.toString());
			enemyPercent2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Entrance safety: ");

		str.append(round(origRoom.getDoorSafeness()* 100, 2 ));
		str.append("%");

		str.append(" ➤  ");
		entranceSafety.setText(str.toString());	
		str = new StringBuilder();

		str.append(round(selectedRoom.getDoorSafeness()* 100, 2 ));
		str.append("%");

		if (origRoom.getDoorSafeness() > selectedRoom.getDoorSafeness()) {
			str.append(" ▼");
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: red");
		} else if (origRoom.getDoorSafeness() < selectedRoom.getDoorSafeness()) {
			str.append(" ▲");
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: green");
		} else {
			entranceSafety2.setText(str.toString());
			entranceSafety2.setStyle("-fx-text-fill: white");
		}

		str = new StringBuilder();


		str.append("Treasure safety: ");

		Double[] safeties = origRoom.getAllTreasureSafeties();

		double totalSafety = 0;

		for (double d : safeties) {
			totalSafety += d;
		}

		if (safeties.length != 0) {
			totalSafety = totalSafety/safeties.length;
		}
		safeties = selectedRoom.getAllTreasureSafeties();

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
