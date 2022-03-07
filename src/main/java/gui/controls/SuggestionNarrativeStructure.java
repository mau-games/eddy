package gui.controls;

import game.Room;
import game.narrative.GrammarGraph;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import util.eventrouting.EventRouter;
import util.eventrouting.events.SuggestedMapSelected;
import util.eventrouting.events.SuggestedNarrativeHovered;
import util.eventrouting.events.SuggestedNarrativeSelected;

import java.util.Stack;

/***
 * This class will hold the canvas and all relevant info about the grammars that are suggested
 * to encapsulate the use of it and enhance it! :D 
 * @author Alberto Alvarez, Malmö University
 *
 */
public class SuggestionNarrativeStructure
{
	private StackPane graphicNode;
	private Rectangle shape;
	private Color cell_color;
	private GrammarGraph elite;
	private GrammarGraph selected_elite;
	private GrammarGraph original;

	private Node source;
	private boolean selected = false;

	//Super workaround which i have to do unless someone knows how can i do this thing
	private SuggestionNarrativeStructure self;

	public SuggestionNarrativeStructure()
	{
		graphicNode = new StackPane();
		cell_color = Color.rgb(246, 168, 166);
		shape = new Rectangle();
		shape.setFill(cell_color);
		shape.setHeight(50);
		shape.setWidth(50);
		graphicNode.getChildren().add(shape);
		graphicNode.addEventFilter(MouseEvent.MOUSE_ENTERED, new MouseEventH());
//
//		roomViewNode = new LabeledCanvas();
//		roomViewNode.setPrefSize(140, 140);
//		roomViewNode.addEventFilter(MouseEvent.MOUSE_ENTERED, new MouseEventH());
		self = this;
		selected = false;
	}
	
	public class MouseEventH implements EventHandler<MouseEvent>
	{
		@Override
		public void handle(MouseEvent event) 
		{
			source = (Node)event.getSource();
			
			//1) Mouse enters the canvas of the room --> I can fire the event here, no?
			source.setOnMouseEntered(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	if(elite != null)
					{
						highlight(true);
						EventRouter.getInstance().postEvent(new SuggestedNarrativeHovered(self));
						System.out.println("MOUSE ENTERED CANVAS");
					}

	            }

	        });
			
			//2) mouse is moved around the map
			source.setOnMouseMoved(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            }
	        });

			source.setOnMousePressed(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
					EventRouter.getInstance().postEvent(new SuggestedNarrativeSelected(self));
	            	selected = !selected;
	            	highlight(true);
	            	
	            }
	        });
			
			source.setOnMouseReleased(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            }
	        });
			
			source.setOnMouseExited(new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) 
	            {
	            	highlight(false);
	            }

	        });
		}
	}
	
	//TODO: Change magic numbers
	//Force me! 
	public void resizeCanvasForRoom() //ReINIT
	{
//		selected = false;
//		this.originalRoom = original;
//		roomViewNode.setPrefSize(140,140);
//
//		float proportion = (float)(Math.min(original.getColCount(), original.getRowCount()))/(float)(Math.max(original.getColCount(), original.getRowCount()));
//
//		if(original.getColCount() > 10)
//		{
//			roomViewNode.setPrefWidth(14.0 * original.getColCount());
//		}
//
//		if(original.getRowCount() > 10)
//		{
//			roomViewNode.setPrefHeight(14.0 * original.getRowCount());
//		}
//
//		if(original.getRowCount() > original.getColCount())
//		{
//			roomViewNode.setPrefWidth(roomViewNode.getPrefHeight() * proportion);
//		}
//		else if(original.getColCount() > original.getRowCount())
//		{
//			roomViewNode.setPrefHeight(roomViewNode.getPrefWidth() * proportion);
//		}

	}
	
	public Pane getNarrativeGraphicNode()
	{
		return graphicNode;
	}
	
	public String getStats() //This should be in room not here
	{
		return "THIS METHOD SHOULD NOT BE IN SUGGESTION ROOM";
	}

	public GrammarGraph getElite()
	{
		return elite;
	}

	public GrammarGraph getSelectedElite()
	{
		return selected_elite;
	}

	public void setElite(GrammarGraph elite)
	{
		if(elite == null)
		{
			this.elite = null;
			this.selected_elite = null;
			return;
		}

		this.elite = elite;
		this.selected_elite = new GrammarGraph(elite);
	}

	public void setCellFitness(double cell_fitness)
	{
		shape.setFill(cell_color.interpolate(Color.web("55C671FF"), cell_fitness));
	}

//
//	public Room getSuggestedRoom()
//	{
//		return this.suggestedRoom;
//	}
//
//	public Room getOriginalRoom()
//	{
//		return this.originalRoom;
//	}
//
//	public void setSuggestedRoom(Room suggestedRoom)
//	{
//		this.suggestedRoom = suggestedRoom;
//	}
//
//	public void setOriginalRoom(Room originalRoom)
//	{
//		this.originalRoom = originalRoom;
//	}
	
	public void setSelected(Boolean value)
	{
		selected = value;
		highlight(value);
	}

    /**
     * Highlights the control.
     * 
     * @param state True if highlighted, otherwise false.
     */
    private void highlight(boolean state)
    {
    	if(selected)
    	{
			graphicNode.setStyle("-fx-border-color: #fcdf3c;");
    	}
    	else
    	{
    		if (state) {
				graphicNode.setStyle("-fx-border-color: #6b87f9;");
        	} else {
//				graphicNode.setStyle("-fx-border-width: 0px; -fx-background-color:#2c2f33;");
				graphicNode.setStyle("-fx-background-color:#2c2f33;");

        	}
    	}
    }
}
