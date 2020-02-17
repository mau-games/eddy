package gui.controls;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import collectors.XMLHandler;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
//import machineLearning.ngrams.Gram.GramTypes;
//import machineLearning.ngrams.NGramLoader;
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
import util.eventrouting.events.intraview.DungeonPreviewSelected;
import util.eventrouting.events.intraview.RoomEditionStarted;
import util.eventrouting.events.intraview.SequencePreviewSelected;

public class RoomSequenceVisualizer extends BorderPane implements Listener {

	//FROM THE FXML
//	@FXML VBox centerPane;
	@FXML StackPane centerPane;
	@FXML Button useCurrentButton;
	@FXML Button loadRoomsButton;
	@FXML Button saveButton;
	@FXML TextField widthField;
	@FXML TextField heightField;
	@FXML TextField nStepsField;
	@FXML private Button worldGridBtn; //ok
	
	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	private InteractiveMap mapView;
	
	private boolean isActive = false;
	private SimpleObjectProperty<Room> currentEditedRoom = new SimpleObjectProperty<>();
	
	@FXML public VBox sequenceRoomPane;
	@FXML public HBox loadedRoomsPane;
	
	
	//To be called from the fxml
	public RoomSequenceVisualizer()
	{
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/controls/RoomSequenceView.fxml"));
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
		router.registerListener(this, new SequencePreviewSelected(null));
		
		
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
//		InitializeView();
	}

	
	private void visualizeRoomSequence(LinkedList<Room> roomSequence)
	{
		int counter = 0;
		int maxColumns = 4;
		HBox row = new HBox();
		row.setSpacing(20.0);
		
		sequenceRoomPane.getChildren().clear();
		sequenceRoomPane.getChildren().add(row);
		
		for(int i = 0; i < roomSequence.size(); i++) 
		{
			if(counter >= maxColumns)
			{
				counter=0;
				row = new HBox();
				row.setSpacing(20.0);
				sequenceRoomPane.getChildren().add(row);
			}
			
			RoomPreview<SequencePreviewSelected> roomPreview = new RoomPreview<SequencePreviewSelected>(roomSequence.get(i), SequencePreviewSelected.class);
			row.getChildren().add(roomPreview.getRoomCanvas());
			
			double mapHeight = 15.0;
			double mapWidth = 15.0;
			
			mapHeight = (int)(20.0 * (float)((float)roomSequence.get(i).getRowCount())); //Recalculate map size
			mapWidth = (int)(20.0 * (float)((float)roomSequence.get(i).getColCount()));//Recalculate map size
			
//			StackPane.setMargin(centerPane, new Insets(8,8,8,8));
			
			roomPreview.getRoomCanvas().setMinSize(mapWidth, mapHeight);
			roomPreview.getRoomCanvas().setMaxSize(mapWidth, mapHeight);
			roomPreview.getRoomCanvas().setPrefSize(mapWidth, mapHeight);
			
			roomPreview.getRoomCanvas().draw(null);
			roomPreview.getRoomCanvas().setText("Waiting for map...");
			counter++;
			
			Platform.runLater(() -> {
				roomPreview.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(roomPreview.getPreviewRoom(), -1));
			});
		}	
	}
	
	private void addLoadedRooms(LinkedList<Room> roomsInFile)
	{
		loadedRoomsPane.getChildren().clear();
		
		for(int i = 0; i < roomsInFile.size(); i++) 
		{
			
			RoomPreview<SequencePreviewSelected> roomPreview = new RoomPreview<SequencePreviewSelected>(roomsInFile.get(i),SequencePreviewSelected.class);
			loadedRoomsPane.getChildren().add(roomPreview.getRoomCanvas());
			
			double mapHeight = 15.0;
			double mapWidth = 15.0;
			
			mapHeight = (int)(20.0 * (float)((float)roomsInFile.get(i).getRowCount())); //Recalculate map size
			mapWidth = (int)(20.0 * (float)((float)roomsInFile.get(i).getColCount()));//Recalculate map size
			
//			StackPane.setMargin(centerPane, new Insets(8,8,8,8));
			
			roomPreview.getRoomCanvas().setMinSize(mapWidth, mapHeight);
			roomPreview.getRoomCanvas().setMaxSize(mapWidth, mapHeight);
			roomPreview.getRoomCanvas().setPrefSize(mapWidth, mapHeight);
			
			roomPreview.getRoomCanvas().draw(null);
			roomPreview.getRoomCanvas().setText("Waiting for map...");
			
			Platform.runLater(() -> {
				roomPreview.getRoomCanvas().draw(renderer.renderMiniSuggestedRoom(roomPreview.getPreviewRoom(), -1));
			});
		}
	}
	
	@Override
	public void ping(PCGEvent e) {
		if(e instanceof RoomEditionStarted)
		{
			currentEditedRoom.set((Room) e.getPayload());
//			setupView();
		}
		else if(e instanceof SequencePreviewSelected)
		{
			visualizeRoomSequence(((Room)e.getPayload()).getEditionSequence());
		}
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
	public void onLoadRooms()
	{
		XMLHandler.getInstance().clearLoaded();
		XMLHandler.getInstance().loadRooms(XMLHandler.projectPath + "testReader\\", false);
		XMLHandler.getInstance().sortRoomsToLoad();
		XMLHandler.getInstance().createRooms();
		
		LinkedList<Room> xmlRooms = XMLHandler.getInstance().roomsInFile;
		
		addLoadedRooms(xmlRooms);
		visualizeRoomSequence(xmlRooms.getFirst().getEditionSequence());
	}
	
	@FXML
	public void onUseCurrent()
	{
		LinkedList<Room> xmlRooms = new LinkedList<>();
		xmlRooms.add(currentEditedRoom.get());
		addLoadedRooms(xmlRooms);
		visualizeRoomSequence(xmlRooms.getFirst().getEditionSequence());
	}
	
	@FXML
	private void onSaveRoom()
	{

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
