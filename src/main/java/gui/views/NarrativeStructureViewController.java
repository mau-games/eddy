package gui.views;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import game.*;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;
import game.narrative.NarrativePane;
import game.narrative.TVTropeType;
import generator.algorithm.Algorithm;
import generator.algorithm.MAPElites.GrammarGACell;
import generator.algorithm.MAPElites.GrammarMAPEliteAlgorithm;
import generator.algorithm.MAPElites.grammarDimensions.GADimensionGrammar;
import generator.algorithm.MAPElites.grammarDimensions.MAPEDimensionGrammarFXML;
import gui.controls.*;
import gui.utils.DungeonDrawer;
import gui.utils.DungeonDrawer.DungeonBrushes;
import gui.utils.InterRoomBrush;
import gui.utils.MapRenderer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import util.IntField;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NarrativeStructureViewController extends BorderPane implements Listener
{
	private ApplicationConfig config;
	private EventRouter router = EventRouter.getInstance();
	private boolean isActive = false;
	private boolean isToggled = false;

	private Button suggestionsBtn = new Button();
	private Button createNewRoomBtn = new Button();
	private Button removeRoomBtn = new Button();
	private Button pickInitBtn = new Button();
	private Button toggleFilterButton = new Button();

	private ArrayList<Button> brushBtns = new ArrayList<Button>(); //TODO: This can be improved to be dependant on how many brushes and have maybe its own class?
	private ComboBox<PathInformation.PathType> pathTypeComboBox = new ComboBox<>();

	private Label widthLabel = new Label("Width =");
	private Label heightLabel = new Label("Height =");
	private IntField widthField = new IntField(3, 20, 13);
	private IntField heightField = new IntField(3, 20, 7);

	private Canvas buttonCanvas;
	private MapRenderer renderer = MapRenderer.getInstance();

	@FXML private StackPane buttonPane;
	@FXML Pane worldPane;
	@FXML private List<LabeledCanvas> mapDisplays;

	private Node source;

	private GrammarGraph editedGraph;
	private GrammarGraph renderedGraph;

	double anchorX;
	double anchorY;

	//Line to appear when we try to draw connections between rooms (just visual feedback)
	private Line auxLine;

	//MAP-ELITES
	@FXML private NarrativeDimensionsTable MainTable;
	@FXML private NarrativeDimensionsTable secondaryTable;

	//RIGHT SIDE!
	@FXML public VBox rightSidePane;

	//Suggestions
//	@FXML private GridPane suggestionsPane;
	@FXML private MAPEGrammarVisualizationPane MAPElitesPane;
	private ArrayList<SuggestionNarrativeStructure> narrativeStructureDisplays;
	private MAPEDimensionGrammarFXML[] currentDimensions = new MAPEDimensionGrammarFXML[] {};

	@FXML VBox elite_previews;
	@FXML NarrativePane elite_preview_1;
	@FXML NarrativePane elite_preview_2;
	Separator elite_sep;

	GrammarGraph elp_1;
	GrammarGraph elp_2;

	SuggestionNarrativeStructure hovered_suggestion = null;
	SuggestionNarrativeStructure selected_suggestion = null;

	GrammarGraph core_graph;


	public NarrativeStructureViewController() {
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/interactive/NarrativeStructureView.fxml"));
		loader.setRoot(this);
		loader.setController(this);



		try {
			loader.load();
			config = ApplicationConfig.getInstance();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		router.registerListener(this, new MapUpdate(null));
		router.registerListener(this, new MAPENarrativeGridUpdate(null));
		router.registerListener(this, new NarrativeStructMAPElitesDone());
		router.registerListener(this, new SuggestedNarrativeHovered(null));
		router.registerListener(this, new SuggestedNarrativeSelected(null));
		router.registerListener(this, new NarrativeStructEdited(null));

		//Create the graphs!
		Scale newScale = new Scale();
		newScale.setPivotX(0);
		newScale.setPivotY(0);
		newScale.setX(0.6);
		newScale.setY(0.6);


		createAxiom();

		elite_previews.getChildren().clear();
		elp_1 = createGrammar();
		elite_preview_1 = elp_1.nPane;
		elite_preview_1.forceScale(newScale);

//		elite_preview_1.renderAll();

		elp_2 = createGrammar();
		elite_preview_2 = elp_2.nPane;

//		elite_preview_2.renderAll();
		elite_preview_2.forceScale(newScale);

		elite_sep= new Separator();
		elite_sep.setOrientation(Orientation.HORIZONTAL);
//		elite_sep.setValignment(VPos.CENTER);
		elite_sep.setValignment(VPos.CENTER);
		elite_previews.getChildren().addAll(elite_preview_1, elite_sep, elite_preview_2);



//		elite_previews.setStyle("-fx-padding: 10;" + "-fx-border-style: solid inside;"
//				+ "-fx-border-width: 2;" + "-fx-border-insets: 5;"
//				+ "-fx-border-radius: 5;" + "-fx-border-color: blue;");


		narrativeStructureDisplays = new ArrayList<SuggestionNarrativeStructure>();
		for(int i = 0; i < 100; i++)
		{
			SuggestionNarrativeStructure sug = new SuggestionNarrativeStructure();
			narrativeStructureDisplays.add(sug);
		}



//		suggestionsPane.setVisible(false);

		MAPElitesPane.init(narrativeStructureDisplays, "","",0,0);

		MainTable.setup(2);
		MainTable.InitMainTable(MAPElitesPane);
		MainTable.setEventListeners();

		secondaryTable.setup(GADimensionGrammar.GrammarDimensionTypes.values().length);

		for(GADimensionGrammar.GrammarDimensionTypes dimension : GADimensionGrammar.GrammarDimensionTypes.values())
		{
			if(dimension != GADimensionGrammar.GrammarDimensionTypes.STEP && dimension != GADimensionGrammar.GrammarDimensionTypes.DIVERSITY)
			{
				secondaryTable.getItems().add(new MAPEDimensionGrammarFXML(dimension, 5));
			}
		}

		secondaryTable.setEventListeners();


		initNarrativeView();
	}

	private void createAxiom()
	{
		core_graph = new GrammarGraph();
		GrammarNode a1 = core_graph.addNode(TVTropeType.ANY);
		GrammarNode b1 = core_graph.addNode(TVTropeType.ANY);
		a1.addConnection(b1, 1);
	}

	private GrammarGraph createGrammar()
	{
		GrammarGraph graph = new GrammarGraph();

		GrammarNode hero = new GrammarNode(0, TVTropeType.HERO);
		GrammarNode conflict = new GrammarNode(1, TVTropeType.CONFLICT);
		GrammarNode enemy = new GrammarNode(2, TVTropeType.ENEMY);

		hero.addConnection(conflict, 1);
		conflict.addConnection(enemy, 1);

		graph.nodes.add(hero);
		graph.nodes.add(conflict);
		graph.nodes.add(enemy);

		return graph;
	}


	private void relocateGrammarGraph()
	{

	}

	public void setActive(boolean state) {
		isActive = state;
	}

	private void initNarrativeView()
	{
		//setup visual line
		auxLine = new Line();
		auxLine.setStrokeWidth(2.0f);
		auxLine.setStroke(Color.PINK);
		auxLine.setMouseTransparent(true);
		
		buttonPane = new StackPane();

		//setting the parts of the border pane
//		setCenter(worldPane);
//		setRight(buttonPane);
		worldPane.addEventHandler(MouseEvent.MOUSE_PRESSED, new MouseEventWorldPane());

		ContextMenu contextMenu = new ContextMenu();



		MenuItem anyMenu = new MenuItem("Any");

		Menu heroMenu = new Menu("Hero");
		TVTropeMenuItem heroChild1 = new TVTropeMenuItem("Hero", TVTropeType.HERO);
		TVTropeMenuItem heroChild2 = new TVTropeMenuItem("5MA", TVTropeType.FIVE_MA);
		TVTropeMenuItem heroChild3 = new TVTropeMenuItem("NEO", TVTropeType.NEO);
		TVTropeMenuItem heroChild4 = new TVTropeMenuItem("SH", TVTropeType.SH);
		heroMenu.getItems().addAll(heroChild1, heroChild2, heroChild3, heroChild4);

		Menu conflictMenu = new Menu("Conflict");
		TVTropeMenuItem conflictChild1 = new TVTropeMenuItem("Conflict", TVTropeType.CONFLICT);
		TVTropeMenuItem conflictChild2 = new TVTropeMenuItem("CONA", TVTropeType.CONA);
		TVTropeMenuItem conflictChild3 = new TVTropeMenuItem("COSO", TVTropeType.COSO);
		conflictMenu.getItems().addAll(conflictChild1, conflictChild2, conflictChild3);

		Menu enemyMenu = new Menu("Enemy");
		TVTropeMenuItem EnemyChild1 = new TVTropeMenuItem("Enemy", TVTropeType.ENEMY);
		TVTropeMenuItem EnemyChild2 = new TVTropeMenuItem("EMP", TVTropeType.EMP);
		TVTropeMenuItem EnemyChild3 = new TVTropeMenuItem("BAD", TVTropeType.BAD);
		TVTropeMenuItem EnemyChild4 = new TVTropeMenuItem("DRAKE", TVTropeType.DRA);
		enemyMenu.getItems().addAll(EnemyChild1, EnemyChild2, EnemyChild3, EnemyChild4);

		Menu modifierMenu = new Menu("Modifier");
		TVTropeMenuItem modifierChild1 = new TVTropeMenuItem("Modifier", TVTropeType.MODIFIER);
		TVTropeMenuItem modifierChild2 = new TVTropeMenuItem("CHK", TVTropeType.CHK);
		TVTropeMenuItem modifierChild3 = new TVTropeMenuItem("MCG", TVTropeType.MCG);
		TVTropeMenuItem modifierChild4 = new TVTropeMenuItem("MHQ", TVTropeType.MHQ);
		modifierMenu.getItems().addAll(modifierChild1, modifierChild2, modifierChild3, modifierChild4);

		SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();

		MenuItem redrawConnections = new MenuItem("ReDraw Connections");

		// Set accelerator to menuItem.
		redrawConnections.setAccelerator(KeyCombination.keyCombination("Ctrl+o"));

		redrawConnections.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				renderedGraph.nPane.renderAll();
			}
		});

		MenuItem reorganizeGraph = new MenuItem("ReOrganize Graph");

		// Set accelerator to menuItem.
		reorganizeGraph.setAccelerator(KeyCombination.keyCombination("Ctrl+o"));

		reorganizeGraph.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				renderedGraph.nPane.layoutGraph();
			}
		});




//		CheckMenuItem checkMenuItem = new CheckMenuItem("Check Menu Item");
//		checkMenuItem.setSelected(true);
//
//		SeparatorMenuItem separatorMenuItem = new SeparatorMenuItem();
//
//		RadioMenuItem radioMenuItem1 = new RadioMenuItem("Radio - Option 1");
//		RadioMenuItem radioMenuItem2 = new RadioMenuItem("Radio - Option 2");
//		ToggleGroup group = new ToggleGroup();
//
//		radioMenuItem1.setToggleGroup(group);
//		radioMenuItem2.setToggleGroup(group);

		// Add MenuItem to ContextMenu
		contextMenu.getItems().addAll(anyMenu, heroMenu, //
				conflictMenu, enemyMenu, modifierMenu,
				separatorMenuItem, redrawConnections, reorganizeGraph);

		worldPane.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

			@Override
			public void handle(ContextMenuEvent event) {

				contextMenu.show(worldPane, event.getScreenX(), event.getScreenY());
			}
		});

		//Don't allow children to pass over the pane!
		clipChildren(worldPane, 12);
		worldButtonEvents();
		initOptions();	
	}

	public void initNarrative(GrammarGraph grammar, Room currentRoom)
	{
		//CHECK THE PROPERTY
		if(widthField == null)
			widthField = new IntField(1, 20, 10);

		if(heightField == null)
			heightField = new IntField(1, 20, 10);

		this.editedGraph = new GrammarGraph(grammar);
		this.renderedGraph = grammar;

		worldPane.getChildren().clear();
		worldPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));


		renderedGraph.nPane.renderAll();
		worldPane.getChildren().add(renderedGraph.nPane);

		elite_preview_1.layoutGraph();
		elite_preview_2.layoutGraph();

//		RunMAPElites(core_graph);
		RunMAPElites(editedGraph);
	}



	/**
	 * Resets the mini suggestions for a new run of map generation
	 */
	private void resetSuggestedRooms()
	{
		router.postEvent(new Stop());

//		for(SuggestionRoom sr : roomDisplays)
//		{
////			sr.getRoomCanvas().resizeRotatingThingie();
//			sr.getRoomCanvas().draw(null);
//			sr.getRoomCanvas().setText("Waiting for map...");
//		}
	}
	
	public class MouseEventWorldPane implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
			
			source.setOnScroll(new EventHandler<ScrollEvent>()
			{

				@Override
				public void handle(ScrollEvent event) {
        			Scale newScale = new Scale();
        	        newScale.setPivotX(event.getX());
        	        newScale.setPivotY(event.getY());
        	        
        	        for(Node child : worldPane.getChildren()) 
            		{
            			newScale.setX( child.getScaleX() + (0.001 * event.getDeltaY()) );
            	        newScale.setY( child.getScaleY() + (0.001 * event.getDeltaY()) );
            	        ((NarrativePane)child).tryScale(newScale);
            		}

        			event.consume();
				}

			});
			
			source.setOnMouseDragged(new EventHandler<MouseEvent>() 
			{

	            @Override
	            public void handle(MouseEvent event)
	            {
	            	if(DungeonDrawer.getInstance().getBrush() instanceof InterRoomBrush)
	            	{
		    			auxLine.setEndX(event.getX());
		    			auxLine.setEndY(event.getY());
	            	}
	            	else if(event.getTarget() == worldPane && event.isMiddleButtonDown()) //TODO: WORK IN PROGRESS
	            	{
	            		for(Node child : worldPane.getChildren())
	            		{
	            			child.setLayoutX(child.getLayoutX() + (event.getX() - anchorX));
	            			child.setLayoutY(child.getLayoutY() + event.getY() - anchorY); 
	            		}
	            		
            			anchorX = event.getX();
            			anchorY = event.getY();
            			
            			event.consume();
	            	}
	            }
	            
	        });
			
			source.setOnMouseReleased(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	worldPane.getChildren().remove(auxLine);
	            }

	        });
			
			source.setOnMousePressed(new EventHandler<MouseEvent>() 
			{

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	anchorX = event.getX();
	    			anchorY = event.getY();
	    			
	            	if(DungeonDrawer.getInstance().getBrush() instanceof InterRoomBrush)
	            	{
		    			if(!worldPane.getChildren().contains(auxLine))
		    				worldPane.getChildren().add(auxLine);
		    			
		    			auxLine.setStartX(event.getX());
		    			auxLine.setStartY(event.getY());
		    			auxLine.setEndX(event.getX());
		    			auxLine.setEndY(event.getY());
	            	}
	            	else //Another brush
	            	{
	            		
	            	}
	            	
	            }
	        });
			
		}
	}
	
	private void clipChildren(Region region, double arc)
	{
		final Rectangle outputClip = new Rectangle();
	    outputClip.setArcWidth(arc);
	    outputClip.setArcHeight(arc);
	    region.setClip(outputClip);

	    region.layoutBoundsProperty().addListener((ov, oldValue, newValue) -> {
	        outputClip.setWidth(newValue.getWidth());
	        outputClip.setHeight(newValue.getHeight());
	    });
	}

	private void initOptions() 
	{				
		buttonCanvas = new Canvas(500, 1000); //TODO This will stay like this but it shoudl be responsive!!
		StackPane.setAlignment(buttonCanvas, Pos.CENTER);
		buttonPane.setAlignment(Pos.CENTER);
		buttonPane.getChildren().add(buttonCanvas);
		buttonCanvas.setVisible(true);
		buttonCanvas.setMouseTransparent(true);
		
//		pathTypeComboBox.getItems().setAll(PathInformation.PathType.values());
//		pathTypeComboBox.setValue(PathInformation.getInstance().getPathType());
		
		
		//some calculations for the brushes
		double maxWidth = 250;
		double maxHeight = 50;
		double initXPos = -100;
		double yPos = 0;
		double widthPadding = 30;
		double btnWidthSize = (maxWidth / brushBtns.size()) - (10 * brushBtns.size());
		double xStep = btnWidthSize + (btnWidthSize / 2.0) + widthPadding;
		
//		ImageView mView = new ImageView(new Image(this.getClass().getResource("/graphics/player.gif").toExternalForm()));
//		 Animation ani = new AnimatedGif(getClass().getResource("/graphics/player.gif").toExternalForm(), 1000);
//	        ani.setCycleCount(Timeline.INDEFINITE);
//	        ani.play();
			
//			ani.getView().setX(0);
//			ani.getView().setY(200);
//			ani.getView().setFitWidth(50);
//			ani.getView().setFitHeight(50);
		//Arrange the controls
		
		arrangeControls(widthLabel, 90, -310, 50, 50);
		arrangeControls(heightLabel, 90, -260, 50, 50);
		arrangeControls(widthField, 140, -310, 50, 30);
		arrangeControls(heightField, 140, -260, 50, 30);
		arrangeControls(createNewRoomBtn, -50, -300, -1, -1);
		arrangeControls(removeRoomBtn, 0, -150, -1, -1);
		arrangeControls(getSuggestionsBtn(), -30, 50, 200, 50);
		arrangeControls(toggleFilterButton, 120, 50, 50, 50);
//		arrangeControls(getPickInitBtn(), 0, 200, 300, 50);
//		arrangeControls(mView, 0, 200, 50, 50);

		Popup restartProgram = new Popup("If you are a new user, please restart the workflow by \"File/New Workflow\" at the top menu", 50, 100, false);
		restartProgram.setMaxHeight(100);
		restartProgram.setMinHeight(100);
		restartProgram.setPrefHeight(100);
		restartProgram.setTranslateX(0);
		restartProgram.setTranslateY(150);
//		restartProgram.setPosition(0, 250);
		
//		arrangeControls(pathTypeComboBox, 0, 300, 300, 50);
		
//		String[] btnsText = {"M", "C", "P"};
		for(int i = 0; i < brushBtns.size(); i++, initXPos += xStep)
		{
//			arrangeControls(brushBtns.get(i), initXPos, 0, btnWidthSize, maxHeight);
			arrangeControls(brushBtns.get(i), initXPos, -25, -1, -1);
			buttonPane.getChildren().add(brushBtns.get(i));
//			brushBtns.get(i).setText(btnsText[i]);
		}
		
		try {
			config = ApplicationConfig.getInstance();
		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//all of this is so ugly!! :( :( 
		ConfigurationUtility c = config.getInternalConfig();
		
		//ADD ROOM
		ImageView brushBtnImage = new ImageView(new Image(c.getString("map.dungeonbrushes.addRoom"), 200, 200, true, true));
//		BorderPane imageViewWrapper = new BorderPane(brushBtnImage);
		createNewRoomBtn.setGraphic(brushBtnImage);
		createNewRoomBtn.setStyle("-fx-background-color: transparent; -fx-padding: 5;\r\n" + 
				"    -fx-border-style: none;\r\n" + 
				"    -fx-border-width: 0;\r\n" + 
				"    -fx-border-insets: 0;");
//		
//		imageViewWrapper.setOnMouseEntered(new EventHandler<MouseEvent>() {
//
//            @Override
//            public void handle(MouseEvent event) 
//            {
////            	ImageView iv = (ImageView)createNewRoomBtn.getGraphic();
//            	imageViewWrapper.setStyle("-fx-border-width: 2px; -fx-border-color: #6b87f9;");
////            	iv.setcolorfi
////            	createNewRoomBtn.setStyle("-fx-background-color: transparent; -fx-padding: 6 4 4 6;");
//            }
//
//        });
		
		//REMOVE ROOM
		brushBtnImage = new ImageView(new Image(c.getString("map.dungeonbrushes.removeRoom"), 200, 200, true, true));
		removeRoomBtn.setGraphic(brushBtnImage);
		removeRoomBtn.setStyle("-fx-background-color: transparent; -fx-padding: 5;\r\n" + 
				"    -fx-border-style: none;\r\n" + 
				"    -fx-border-width: 0;\r\n" + 
				"    -fx-border-insets: 0;");
		//MOVE
		brushBtnImage = new ImageView(new Image(c.getString("map.dungeonbrushes.move"), 40, 40, true, true));
		brushBtns.get(0).setGraphic(brushBtnImage);
		//CONNECTOR
		brushBtnImage = new ImageView(new Image(c.getString("map.dungeonbrushes.connector"), 80, 80, true, true));
//		brushBtnImage.setStyle("-fx-background-color: transparent");
		brushBtns.get(1).setGraphic(brushBtnImage);
		
		brushBtnImage = new ImageView(new Image(c.getString("map.dungeonbrushes.init"), 40, 40, true, true));
//		brushBtnImage.setStyle("-fx-background-color: transparent");
		brushBtns.get(2).setGraphic(brushBtnImage);
	
		//change color of the input fields!
		heightField.setStyle("-fx-text-inner-color: white;");		
		widthField.setStyle("-fx-text-inner-color: white;");
		widthLabel.setTextFill(Color.WHITE);
		heightLabel.setTextFill(Color.WHITE);
//		widthLabel.setTextFill(Paint);

		//Add everything to the button pane!
		buttonPane.getChildren().add(getSuggestionsBtn());
//		buttonPane.getChildren().add(getPickInitBtn());
		buttonPane.getChildren().add(createNewRoomBtn);
		buttonPane.getChildren().add(removeRoomBtn);
		buttonPane.getChildren().add(heightField);
		buttonPane.getChildren().add(widthField);
		buttonPane.getChildren().add(widthLabel);
		buttonPane.getChildren().add(heightLabel);
		buttonPane.getChildren().add(restartProgram);
		buttonPane.getChildren().add(toggleFilterButton);
		
//		buttonPane.getChildren().add(pathTypeComboBox);
//		buttonPane.getChildren().add(ani.getView());
		
//		createNewRoomBtn.setOnAction( e -> ani.pause());
//		removeRoomBtn.setOnAction( e -> ani.play());
		
		//Change the text of the buttons!
		pickInitBtn.setText("Pick Init Room and Pos");
//		createNewRoomBtn.setText("NEW ROOM");
//		removeRoomBtn.setText("REMOVE ROOM");
		getSuggestionsBtn().setText("Start with our suggestions");
		toggleFilterButton.setText("OBJ");
		getSuggestionsBtn().setTooltip(new Tooltip("Start with our suggested designs as generated by genetic algorithms"));
		
		//Tooltips!
		createNewRoomBtn.setTooltip(new Tooltip("Create and Add new room to dungeon with specified Width and Height"));
		removeRoomBtn.setTooltip(new Tooltip("Remove currently selected room"));
		brushBtns.get(0).setTooltip(new Tooltip("Press and Hold over a room to move"));
		brushBtns.get(1).setTooltip(new Tooltip("Connect 2 rooms with a door. Drag with your mouse from one room's border to another room's border"));
		brushBtns.get(2).setTooltip(new Tooltip("Place the hero in a room and position as starting point!"));
		toggleFilterButton.setTooltip(new Tooltip("Show the objectives of the dungeon"));

	}
	
	private void arrangeControls(Control obj, double xPos, double yPos, double sizeWidth, double sizeHeight)
	{
		obj.setTranslateX(xPos);
		obj.setTranslateY(yPos);
		if(sizeWidth > 0 && sizeHeight > 0)
		{
			obj.setMinSize(sizeWidth, sizeHeight);
			obj.setMaxSize(sizeWidth, sizeHeight);
		}
	}

	private void RunMAPElites(GrammarGraph ax)
	{
		Algorithm ga = new GrammarMAPEliteAlgorithm(ax);
		((GrammarMAPEliteAlgorithm)ga).initPopulations(currentDimensions);
		ga.start();
	}

	public void renderCell(List<GrammarGACell> filled_cells, int dimension, float [] dimensionSizes, int[] indices)
	{
		if(dimension < 0)
		{
//			MAPElitesPane.GetGridCell(, row);
//			this.cells.add(new GACell(MAPElitesDimensions, indices));
			int ind = (int) (indices[1] * dimensionSizes[0] + indices[0]);

//			core_graph
//			ind.getPhenotype().getGrammarGraphOutput(axiom, 1);

			if(filled_cells.get(ind) != null)
			{
				narrativeStructureDisplays.get(ind).setElite(
						filled_cells.get(ind).GetFeasiblePopulation().get(0).getPhenotype().getGrammarGraphOutput(editedGraph, 1));
				narrativeStructureDisplays.get(ind).setCellFitness(
						filled_cells.get(ind).getEliteFitness());
			}

//			narrativeStructureDisplays.get((int) (indices[1] * dimensionSizes[0] + indices[0])).setElite(filled_cells.get((int) (indices[1] * dimensionSizes[0] + indices[0])));
//			narrativeStructureDisplays.get((int) (indices[1] * dimensionSizes[0] + indices[0])).setOriginalRoom(getMapView().getMap());
//			roomDisplays.get(nextRoom).setOriginalRoom(); //Maybe this does not make sense? Idk
			return;
		}

		for(int i = 0; i < dimensionSizes[dimension]; i++)
		{
			indices[dimension] = i;
			renderCell(filled_cells, dimension-1, dimensionSizes, indices);
		}
	}

	@Override
	public void ping(PCGEvent e) {
		if(e instanceof MAPENarrativeGridUpdate)
		{

			MAPElitesPane.dimensionsUpdated(narrativeStructureDisplays, ((MAPENarrativeGridUpdate) e).getDimensions());
			currentDimensions = ((MAPENarrativeGridUpdate) e).getDimensions();
//			OnChangeTab();
		}
		else if(e instanceof NarrativeStructMAPElitesDone)
		{
			if (isActive) {
				//THIS NEED TO BE IMPROVED!
				List<GrammarGACell> filled_cells = ((NarrativeStructMAPElitesDone) e).getCells();
//				Room room = (Room) ((MapUpdate) e).getPayload();
//				UUID uuid = ((MapUpdate) e).getID();
				LabeledCanvas canvas;
				synchronized (narrativeStructureDisplays) {

					renderCell(filled_cells, currentDimensions.length - 1,
							new float [] {currentDimensions[0].getGranularity(), currentDimensions[1].getGranularity()}, new int[] {0,0});
				}


			}
		}
		else if(e instanceof SuggestedNarrativeHovered)
		{
			hovered_suggestion = (SuggestionNarrativeStructure) ((SuggestedNarrativeHovered) e).getPayload();
			Scale newScale = new Scale();
			newScale.setPivotX(0);
			newScale.setPivotY(0);
			newScale.setX(0.6);
			newScale.setY(0.6);

			elite_previews.getChildren().clear();
			elite_preview_1 = hovered_suggestion.getElite().nPane;
			elite_preview_1.forceScale(newScale);


			elite_previews.getChildren().addAll(elite_preview_1, elite_sep, elite_preview_2);

			elite_preview_1.layoutGraph();
			elite_preview_1.setMaxHeight(300);
//			elite_preview_1.setPrefHeight(300);

		}
		else if(e instanceof SuggestedNarrativeSelected)
		{
			if(selected_suggestion != null)
				selected_suggestion.setSelected(false);

			selected_suggestion = (SuggestionNarrativeStructure) ((SuggestedNarrativeSelected) e).getPayload();
			Scale newScale = new Scale();
			newScale.setPivotX(0);
			newScale.setPivotY(0);
			newScale.setX(0.6);
			newScale.setY(0.6);

//			elite_previews.getChildren().clear();
			elite_previews.getChildren().remove(elite_preview_2);
			elite_preview_2 = selected_suggestion.getSelectedElite().nPane;
			elite_preview_2.forceScale(newScale);

			elite_previews.getChildren().add(elite_preview_2);

			elite_preview_2.layoutGraph();
			elite_preview_2.setMaxHeight(300);
//			elite_preview_1.setPrefHeight(300);

		}
		else if(e instanceof NarrativeStructEdited)
		{
			editedGraph = new GrammarGraph((GrammarGraph) e.getPayload());
		}
	}

	public Button getPickInitBtn() {
		return pickInitBtn;
	}

	public void setPickInitBtn(Button pickInitBtn) {
		this.pickInitBtn = pickInitBtn;
	}

	public Button getSuggestionsBtn() {
		return suggestionsBtn;
	}

	public void setSuggestionsBtn(Button suggestionsBtn) {
		this.suggestionsBtn = suggestionsBtn;
	}
	
	//TODO: This should check which brush was before and go back to that one!!!!
	public void restoreBrush() //If you call this method is because you have change the initial room!!
	{
		DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.MOVEMENT);
		getPickInitBtn().setText("Pick Init Room and Pos: DONE");
	}

	private void worldButtonEvents() 
	{
		//How many Brushes do we have? --> This should be automatic but it will be coded for the 3 dif brushes 
		brushBtns = new ArrayList<Button>();
		
		brushBtns.add(new Button()); //Move
		brushBtns.add(new Button()); //Connection
		brushBtns.add(new Button()); //Path
		
		brushBtns.get(0).setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				ActionLogger.getInstance().storeAction(ActionType.CLICK,
														View.WORLD, 
														TargetPane.BUTTON_PANE, 
														false,
														DungeonBrushes.MOVEMENT);
				
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.MOVEMENT);
			}

		}); 
		
		brushBtns.get(1).setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				ActionLogger.getInstance().storeAction(ActionType.CLICK,
														View.WORLD, 
														TargetPane.BUTTON_PANE, 
														false,
														DungeonBrushes.ROOM_CONNECTOR);
				
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.ROOM_CONNECTOR);
			}

		}); 
		
		brushBtns.get(2).setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) 
			{
				ActionLogger.getInstance().storeAction(ActionType.CLICK,
														View.WORLD, 
														TargetPane.BUTTON_PANE, 
														false,
														DungeonBrushes.INITIAL_ROOM);
				
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.INITIAL_ROOM);
			}

		}); 
		
//		brushBtns.get(2).setOnAction(new EventHandler<ActionEvent>() {
//			@Override
//			public void handle(ActionEvent e) 
//			{
//				ActionLogger.getInstance().storeAction(ActionType.CLICK,
//														View.WORLD, 
//														TargetPane.BUTTON_PANE, 
//														false,
//														DungeonBrushes.PATH_FINDING);
//				
//				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.PATH_FINDING);
//			}
//
//		});

		getSuggestionsBtn().setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e)
			{
				editedGraph.nPane.renderAll();


				//uncomment to reset scale
//				for(Node child : worldPane.getChildren())
//        		{
//        	        ((DungeonPane)child).resetScale();
//        		}
			}

		});

//		DungeonDrawer.getInstance().nextBrush();
		
		pickInitBtn.setOnAction(new EventHandler<ActionEvent>() { //TODO: This must change!
			@Override
			public void handle(ActionEvent e) 
			{
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.INITIAL_ROOM);
			}
		}); 
		
		createNewRoomBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				System.out.println("Creating a new room");
				ActionLogger.getInstance().storeAction(ActionType.CLICK, 
														View.WORLD, 
														TargetPane.BUTTON_PANE, 
														false,
														"Create Room");
				
//				router.postEvent(new RequestNewRoom(dungeon, -1, widthField.getValue(), heightField.getValue()));
				DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.MOVEMENT);
			}
		}); 
		
		createNewRoomBtn.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) 
            {
            	createNewRoomBtn.setStyle("-fx-border-width: 2px; -fx-border-color: #6b87f9;");
            }

        });
//		
		createNewRoomBtn.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) 
            {
            	createNewRoomBtn.setStyle("-fx-border-width: 0px; -fx-background-color:#2c2f33;");
            }

        });
		
		removeRoomBtn.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {

//				if(dungeon.getSelectedRoom() != null)
//				{
//					ActionLogger.getInstance().storeAction(ActionType.CLICK,
//															View.WORLD,
//															TargetPane.BUTTON_PANE,
//															false,
//															"Remove Room");
//					router.postEvent(new RequestRoomRemoval(dungeon.getSelectedRoom(), dungeon, -1));
//					DungeonDrawer.getInstance().changeBrushTo(DungeonBrushes.MOVEMENT);
//				}
					
			}

		}); 
		
		removeRoomBtn.setOnMouseEntered(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) 
            {
            	removeRoomBtn.setStyle("-fx-border-width: 2px; -fx-border-color: #6b87f9;");
            }

        });
//		
		removeRoomBtn.setOnMouseExited(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) 
            {
            	removeRoomBtn.setStyle("-fx-border-width: 0px; -fx-background-color:#2c2f33;");
            }

        });
		
		pathTypeComboBox.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {

				PathInformation.getInstance().changePathType(pathTypeComboBox.getValue());
			}

		});
		
		toggleFilterButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e)
			{	
				if (!isToggled)
				{
					router.postEvent(new ToggleObjectives(true));
					isToggled = true;
				}
				else if (isToggled)
				{
					router.postEvent(new ToggleObjectives(false));
					isToggled = false;
				}
			}
		});
	}
	
	
	//TODO: NEeds to be redone
	private String matrixToString() {
		//create large string
		String largeString = "";
//		int j = 1;
//
//		for (MapContainer[] outer : matrix) {
//
//			for (int k = 0; k < outer[0].getMap().toString().length(); k++) {
//
//				if (outer[0].getMap().toString().charAt(k) != '\n') {
//					largeString += outer[0].getMap().toString().charAt(k);
//
//				}
//				if (outer[0].getMap().toString().charAt(k) == '\n') {
//					while (j < 3) {
//
//						for (int i = (k - 11); i < k; i++) {
//							largeString += outer[j].getMap().toString().charAt(i);
//
//						}
//						j++;
//					}
//					j = 1;
//					largeString += outer[0].getMap().toString().charAt(k);
//				}
//
//			}
//
//		}
		return largeString;
	}
}

