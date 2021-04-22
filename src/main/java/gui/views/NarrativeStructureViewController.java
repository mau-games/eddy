package gui.views;

import game.*;
import game.narrative.GrammarGraph;
import game.narrative.GrammarNode;
import game.narrative.NarrativePane;
import game.narrative.TVTropeType;
import generator.algorithm.Algorithm;
import generator.algorithm.MAPElites.GrammarGACell;
import generator.algorithm.MAPElites.GrammarMAPEliteAlgorithm;
import generator.algorithm.MAPElites.NSEvolutionarySystemEvaluator;
import generator.algorithm.MAPElites.grammarDimensions.GADimensionGrammar;
import generator.algorithm.MAPElites.grammarDimensions.MAPEDimensionGrammarFXML;
import gui.controls.*;
import gui.utils.DungeonDrawer;
import gui.utils.InterRoomBrush;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Orientation;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class NarrativeStructureViewController extends BorderPane implements Listener
{
	private ApplicationConfig config;
	private EventRouter router = EventRouter.getInstance();
	private boolean isActive = false;

	@FXML Pane worldPane;

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
	private final ArrayList<SuggestionNarrativeStructure> narrativeStructureDisplays;
	private MAPEDimensionGrammarFXML[] currentDimensions = new MAPEDimensionGrammarFXML[] {};

	@FXML VBox elite_previews;
	@FXML NarrativePane elite_preview_1;
	@FXML NarrativePane elite_preview_2;
	Separator elite_sep;

	GrammarGraph elp_1;
	GrammarGraph elp_2;

	SuggestionNarrativeStructure hovered_suggestion = null;
	SuggestionNarrativeStructure selected_suggestion = null;

	//INFO STRUCTURES
	@FXML GrammarGraphInfoPane current_graph_info;
	@FXML GrammarGraphInfoPane hovered_graph_info;
	@FXML GrammarGraphInfoPane selected_graph_info;

	NSEvolutionarySystemEvaluator evaluator = new NSEvolutionarySystemEvaluator();
	DecimalFormat df = new DecimalFormat("#.##");

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

		/////////////// SET EVERYTHING FOR MAP-ELITES /////////////////////////

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

	/***
	 * Create example narrative grammar
	 * @return
	 */
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
		

		worldPane.addEventHandler(MouseEvent.MOUSE_PRESSED, new MouseEventWorldPane());

		//Added all context menu for fast interaction
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
//		TVTropeMenuItem conflictChild2 = new TVTropeMenuItem("CONA", TVTropeType.CONA);
//		TVTropeMenuItem conflictChild3 = new TVTropeMenuItem("COSO", TVTropeType.COSO);
//		conflictMenu.getItems().addAll(conflictChild1, conflictChild2, conflictChild3);
		conflictMenu.getItems().addAll(conflictChild1);

		Menu enemyMenu = new Menu("Enemy");
		TVTropeMenuItem EnemyChild1 = new TVTropeMenuItem("Enemy", TVTropeType.ENEMY);
		TVTropeMenuItem EnemyChild2 = new TVTropeMenuItem("EMP", TVTropeType.EMP);
		TVTropeMenuItem EnemyChild3 = new TVTropeMenuItem("BAD", TVTropeType.BAD);
		TVTropeMenuItem EnemyChild4 = new TVTropeMenuItem("DRAKE", TVTropeType.DRA);
		enemyMenu.getItems().addAll(EnemyChild1, EnemyChild2, EnemyChild3, EnemyChild4);

		Menu modifierMenu = new Menu("Modifier");
		TVTropeMenuItem modifierChild1 = new TVTropeMenuItem("PlotDevice", TVTropeType.PLOT_DEVICE);
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

		/// CONTEXT MENU ////////////


		//Don't allow children to pass over the pane!
		clipChildren(worldPane, 12);
	}

	public void initNarrative(GrammarGraph grammar, Room currentRoom)
	{
		//important distinction so you can use the graph for MAP-Elites and edit at the same time
		this.editedGraph = new GrammarGraph(grammar);
		this.renderedGraph = grammar;

		double[] fitness_values = evaluator.testEvaluation(editedGraph, editedGraph);

		current_graph_info.intFitnessText.setText(df.format(fitness_values[0]));
		current_graph_info.cohFitnessText.setText(df.format(fitness_values[1]));
		current_graph_info.fitnessText.setText(df.format(fitness_values[2]));

		worldPane.getChildren().clear();

		//Not really necessary
		worldPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));

		renderedGraph.nPane.renderAll();
		worldPane.getChildren().add(renderedGraph.nPane);

		elite_preview_1.layoutGraph();
		elite_preview_2.layoutGraph();

//		RunMAPElites(core_graph);
		//Fixme: This should happen as an event to be received by the class Game
		RunMAPElites(editedGraph);
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
			//Actually we have the cells and they have the "ind" so it shouldn't be a problem to simply iterate fixme:
			int ind = (int) (indices[1] * dimensionSizes[0] + indices[0]);

//			if(filled_cells.get(0) != null && filled_cells.get(0).GetFeasiblePopulation().size() >= ind)
//			{
//				narrativeStructureDisplays.get(ind).setElite(
//						filled_cells.get(0).GetFeasiblePopulation().get(ind).getPhenotype().getGrammarGraphOutputBest(editedGraph, 1));
//				narrativeStructureDisplays.get(ind).setCellFitness(
//						filled_cells.get(0).getEliteFitness());
//			}

			if(filled_cells.get(ind) != null)
			{
				narrativeStructureDisplays.get(ind).setElite(
						filled_cells.get(ind).GetFeasiblePopulation().get(0).getPhenotype().getGrammarGraphOutputBest(editedGraph, 1));
				narrativeStructureDisplays.get(ind).setCellFitness(
						filled_cells.get(ind).getEliteFitness());
			}
			else
			{
				narrativeStructureDisplays.get(ind).setElite(null);
				narrativeStructureDisplays.get(ind).setCellFitness(0.0);
			}

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
		else if(e instanceof SuggestedNarrativeHovered) //We are hovering so just show
		{
			hovered_suggestion = (SuggestionNarrativeStructure) ((SuggestedNarrativeHovered) e).getPayload();
			Scale newScale = new Scale();
			newScale.setPivotX(0);
			newScale.setPivotY(0);
			newScale.setX(0.6);
			newScale.setY(0.6);

//			elite_previews.getChildren().clear();
			elite_preview_1.setNewOwner(hovered_suggestion.getElite());
//			elite_preview_1 = hovered_suggestion.getElite().nPane;
//			elite_preview_1.forceScale(newScale);


//			elite_previews.getChildren().addAll(elite_preview_1, elite_sep, elite_preview_2);

			elite_preview_1.layoutGraph();
			elite_preview_1.rePositionGraph();
//			elite_preview_1.setMaxHeight(300);
//			elite_preview_1.setPrefHeight(300);

			double[] fitness_values = evaluator.testEvaluation(hovered_suggestion.getElite(), editedGraph);

			hovered_graph_info.intFitnessText.setText(df.format(fitness_values[0]));
			hovered_graph_info.cohFitnessText.setText(df.format(fitness_values[1]));
			hovered_graph_info.fitnessText.setText(df.format(fitness_values[2]));

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
			elite_preview_2.rePositionGraph();
//			elite_preview_2.setMaxHeight(300);
//			elite_preview_1.setPrefHeight(300);

			double[] fitness_values = evaluator.testEvaluation(selected_suggestion.getSelectedElite(), editedGraph);

			selected_graph_info.intFitnessText.setText(df.format(fitness_values[0]));
			selected_graph_info.cohFitnessText.setText(df.format(fitness_values[1]));
			selected_graph_info.fitnessText.setText(df.format(fitness_values[2]));

		}
		else if(e instanceof NarrativeStructEdited)
		{
			editedGraph = new GrammarGraph((GrammarGraph) e.getPayload());

			double[] fitness_values = evaluator.testEvaluation(editedGraph, editedGraph);

			current_graph_info.intFitnessText.setText(df.format(fitness_values[0]));
			current_graph_info.cohFitnessText.setText(df.format(fitness_values[1]));
			current_graph_info.fitnessText.setText(df.format(fitness_values[2]));

			if(hovered_suggestion != null && hovered_suggestion.getElite() != null)
			{
				fitness_values = evaluator.testEvaluation(hovered_suggestion.getElite(), editedGraph);

				hovered_graph_info.intFitnessText.setText(df.format(fitness_values[0]));
				hovered_graph_info.cohFitnessText.setText(df.format(fitness_values[1]));
				hovered_graph_info.fitnessText.setText(df.format(fitness_values[2]));
			}

			if(selected_suggestion != null && selected_suggestion.getSelectedElite() != null)
			{
				fitness_values = evaluator.testEvaluation(selected_suggestion.getSelectedElite(), editedGraph);

				selected_graph_info.intFitnessText.setText(df.format(fitness_values[0]));
				selected_graph_info.cohFitnessText.setText(df.format(fitness_values[1]));
				selected_graph_info.fitnessText.setText(df.format(fitness_values[2]));
			}

		}
	}

}

