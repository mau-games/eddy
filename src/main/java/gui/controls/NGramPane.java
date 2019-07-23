package gui.controls;

import java.io.IOException;

import gui.utils.MapRenderer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.ApplySuggestion;
import util.eventrouting.events.MAPEGridUpdate;
import util.eventrouting.events.MAPElitesDone;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.StartGA_MAPE;
import util.eventrouting.events.SuggestedMapSelected;
import util.eventrouting.events.SuggestedMapsDone;
import util.eventrouting.events.intraview.RoomEditionStarted;

public class NGramPane extends BorderPane implements Listener {

	//FROM THE FXML
	@FXML VBox centerPane;
	@FXML StackPane generatedRoomPane;
	@FXML Button stepButton;
	@FXML Button runButton;
	@FXML Button saveButton;
	@FXML TextField widthField;
	@FXML TextField heightField;
	@FXML TextField nStepField;
	
	private MapRenderer renderer = MapRenderer.getInstance();
	private static EventRouter router = EventRouter.getInstance();
	private InteractiveMap mapView;
	
	private boolean isActive = false;
	
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
		
//		setupMAPElitesGUI();
//		saveGenBtn.setDisable(true);
		isActive = true;
		setupView();
	}
	
	private void setupView()
	{
		InitializeView();
//		router.postEvent(new StartGA_MAPE(originalRoom, currentDimensions));
//		Arrays.stream(genotype.getChromosome()).boxed().map(x -> TileTypes.toTileType(x)).toArray(TileTypes[]::new);
//		currentRoom = originalRoom;
		getMapView().updateMap(null); 
//		SetStats();
	}
	
	public void InitializeView()
	{
		generatedRoomPane.getChildren().clear();

		setMapView(new InteractiveMap());
		StackPane.setAlignment(getMapView(), Pos.CENTER);
		getMapView().setMinSize(420, 420);
		getMapView().setMaxSize(420, 420);
		getMapView().setPrefSize(420, 420);
		generatedRoomPane.getChildren().add(getMapView());
	}
	
	@Override
	public void ping(PCGEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@FXML
	private void onStepGeneration()
	{
		
	}
	
	@FXML
	private void onRunGeneration()
	{
		
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
		
	}
	
	
	public InteractiveMap getMapView() {
		return mapView;
	}

	public void setMapView(InteractiveMap roomView) {
		this.mapView = roomView;
	}

}
