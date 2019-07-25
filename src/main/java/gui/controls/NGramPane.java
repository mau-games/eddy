package gui.controls;

import java.io.IOException;

import game.Room;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import machineLearning.ngrams.Gram.GramTypes;
import machineLearning.ngrams.NGramLoader;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.ApplySuggestion;
import util.eventrouting.events.MAPEGridUpdate;
import util.eventrouting.events.MAPElitesDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestWorldView;
import util.eventrouting.events.StartGA_MAPE;
import util.eventrouting.events.Stop;
import util.eventrouting.events.SuggestedMapSelected;
import util.eventrouting.events.SuggestedMapsDone;
import util.eventrouting.events.intraview.RoomEditionStarted;

public class NGramPane extends BorderPane implements Listener {

	//FROM THE FXML
//	@FXML VBox centerPane;
	@FXML StackPane centerPane;
	@FXML Button stepButton;
	@FXML Button runButton;
	@FXML Button saveButton;
	@FXML TextField widthField;
	@FXML TextField heightField;
	@FXML TextField nStepsField;
	@FXML private Button worldGridBtn; //ok
	
	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	private InteractiveMap mapView;
	
	private boolean isActive = false;
	
	private NGramLoader gramCreator = null;
	private SuggestionRoom nGramRoom;
	private SimpleObjectProperty<Room> currentEditedRoom = new SimpleObjectProperty<>();
	private String[] textGramRoom = null;
	private String currentFormedRoom = "";
	
	
	//To be called from the fxml
	public NGramPane()
	{
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/controls/NGramView.fxml"));
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
		
		nGramRoom = new SuggestionRoom();
		gramCreator = new NGramLoader(GramTypes.COLUMN_BY_COLUMN);
		
//		nGramRoom.resizeCanvasForRoom(13.0f, 7.0f);
//		nGramRoom.getRoomCanvas().draw(null);
//		nGramRoom.getRoomCanvas().setText("Waiting for map...");
//		
//		centerPane.getChildren().clear();
//		centerPane.getChildren().add(nGramRoom.getRoomCanvas());
		
//		setupMAPElitesGUI();
//		saveGenBtn.setDisable(true);
		isActive = false;
//		setupView();
	}
	
	private void disablePane()
	{
//		handleEvolutionPressed();
	}
	
	private void enablePane()
	{
		String[] toCreate = {"0101","0000","3333","0123","0441"};
//		currentEditedRoom.set(Room.createRoomFromStringColumn(toCreate));
		setupView();
	}
	
	private void setupView()
	{
		InitializeView();
	}
	
	public void InitializeView()
	{
		centerPane.getChildren().clear();

//		nGramRoom.resizeCanvasForRoom(currentEditedRoom.get());
		double mapHeight = 30.0;
		double mapWidth = 30.0;
		
		if(nGramRoom.getOriginalRoom() != null)
		{
			mapHeight = (int)(30.0 * (float)((float)nGramRoom.getOriginalRoom().getRowCount())); //Recalculate map size
			mapWidth = (int)(30.0 * (float)((float)nGramRoom.getOriginalRoom().getColCount()));//Recalculate map size
		}
		
		
		StackPane.setMargin(centerPane, new Insets(8,8,8,8));
		
		nGramRoom.getRoomCanvas().setMinSize(mapWidth, mapHeight);
		nGramRoom.getRoomCanvas().setMaxSize(mapWidth, mapHeight);
		nGramRoom.getRoomCanvas().setPrefSize(mapWidth, mapHeight);
		
		nGramRoom.getRoomCanvas().draw(null);
		nGramRoom.getRoomCanvas().setText("Waiting for map...");
		
//		setMapView(new InteractiveMap());
//		StackPane.setAlignment(getMapView(), Pos.CENTER);
//		getMapView().setMinSize(420, 420);
//		getMapView().setMaxSize(420, 420);
//		getMapView().setPrefSize(420, 420);
		centerPane.getChildren().add(nGramRoom.getRoomCanvas());
		
		Platform.runLater(() -> {
			
			if(nGramRoom.getOriginalRoom() != null)
				nGramRoom.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(nGramRoom.getOriginalRoom(), 0));
			else
				nGramRoom.getRoomCanvas().draw(null);
		});
	}
	
	@Override
	public void ping(PCGEvent e) {
		if(e instanceof RoomEditionStarted)
		{
			currentEditedRoom.set((Room) e.getPayload());
			setupView();
		}
		
	}
	
	@FXML
	private void onStepGeneration()
	{
		nGramRoom.setOriginalRoom(null);

		String prevWord = "";

		prevWord = gramCreator.getNGram(currentFormedRoom, Integer.parseInt(nStepsField.getText()));
		currentFormedRoom += prevWord + " ";
		
		System.out.format("Generated Room using %s-gram: ", nStepsField.getText());
		System.out.println();
		System.out.println(currentFormedRoom);
		
		//Form a string array, create the room and add paint it!
		textGramRoom = currentFormedRoom.split(" ");
		nGramRoom.setOriginalRoom(Room.createRoomFromStringColumn(textGramRoom));
		InitializeView();
	}
	
	@FXML
	private void onRunGeneration()
	{	
		nGramRoom.setOriginalRoom(null);
		
		currentFormedRoom = "";
		String prevWord = "";
		for(int words = 0; words < 13; words++)
		{
			prevWord = gramCreator.getNGram(currentFormedRoom, Integer.parseInt(nStepsField.getText()));
			currentFormedRoom += prevWord + " ";
		}
		
		System.out.format("Generated Room using %s-gram: ", nStepsField.getText());
		System.out.println();
		System.out.println(currentFormedRoom);
		
		//Form a string array, create the room and add paint it!
		textGramRoom = currentFormedRoom.split(" ");
		nGramRoom.setOriginalRoom(Room.createRoomFromStringColumn(textGramRoom));
		InitializeView();
	}
	
	@FXML
	private void onWidthChanged()
	{
		
	}
	
	@FXML
	private void onHeightChanged()
	{
		
	}
	
	@FXML
	private void onNChanged()
	{
		
	}
	
	@FXML
	private void onSaveRoom()
	{
		gramCreator.addGrams(currentEditedRoom.get().owner.getAllRooms());
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
	
	public InteractiveMap getMapView() {
		return mapView;
	}

	public void setMapView(InteractiveMap roomView) {
		this.mapView = roomView;
	}
	
	public Button getWorldGridBtn() {
		return worldGridBtn;
	}

	public void setWorldGridBtn(Button worldGridBtn) {
		this.worldGridBtn = worldGridBtn;
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

}
