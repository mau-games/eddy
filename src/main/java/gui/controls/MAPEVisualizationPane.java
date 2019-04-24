package gui.controls;

import java.util.ArrayList;

import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import gui.utils.MapRenderer;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapRendered;
import util.eventrouting.events.SaveDisplayedCells;
import util.eventrouting.events.SuggestedMapsDone;

public class MAPEVisualizationPane extends BorderPane implements Listener
{
	private Label xLabel;
	private Label yLabel;
	private GridPane innerSuggestions;
	private int xResolution;
	private int yResolution;
	private ScrollPane mapeScroll;
	private HBox sp;
	
	public MAPEVisualizationPane()
	{
		super();
		EventRouter.getInstance().registerListener(this, new SaveDisplayedCells());
	}
	
	public void init(ArrayList<SuggestionRoom> roomDisplays, String xLabelText, String yLabelText, int width, int height)
	{
		
		//THIS IS SETTING UP FOR MAPELITES

		//CENTER SCROLL PANE
		mapeScroll = new ScrollPane();
		mapeScroll.setHbarPolicy(ScrollBarPolicy.ALWAYS);
		mapeScroll.setVbarPolicy(ScrollBarPolicy.ALWAYS);

//		mapeScroll.setPrefWidth(50);
		mapeScroll.setMinWidth(10);
		mapeScroll.setMaxWidth(599);
		mapeScroll.setMinHeight(150);
		mapeScroll.setMaxHeight(600);
		
		//Basic setup of inner grid
		innerSuggestions = new GridPane();		
		innerSuggestions.setStyle("-fx-background-color: transparent;");
		innerSuggestions.setHgap(5.0);
		innerSuggestions.setVgap(5.0);
		innerSuggestions.setAlignment(Pos.CENTER);
		SetupInnerGrid(roomDisplays, width, height);
		sp = new HBox(innerSuggestions);


		mapeScroll.setContent(sp);
//		mapeScroll.setFitToWidth(true);
		mapeScroll.setStyle("-fx-background-color: transparent;");
		BorderPane.setAlignment(mapeScroll, Pos.CENTER);
//		mapeScroll.setVvalue(0.5);
//		mapeScroll.setHvalue(0.5);
		
		//Adjust the X label to be added to the bottom
		xLabel = new Label(xLabelText);
//		xLabel.setStyle("-fx-font-weight: bold;");
//		xLabel.setFont(new Font(30));
		xLabel.getStyleClass().add("dimensionLabel");
//		xLabel.setTextFill(Color.WHITE);
		xLabel.setAlignment(Pos.CENTER);
		BorderPane.setAlignment(xLabel, Pos.CENTER);
		
		//ADJUST THE Y LABEL
		yLabel = new Label(yLabelText);
		yLabel.getStyleClass().add("dimensionLabel");
		
		//Adjust the label to be added in the left side rotated
//		yLabel.setFont(new Font(30));
//		yLabel.setStyle("-fx-font-weight: bold");
//		yLabel.setTextFill(Color.WHITE);
		yLabel.setRotate(-90);
		yLabel.setWrapText(true);
		yLabel.setAlignment(Pos.CENTER);

		//This needs to be done because of the rotaiton of the label
		StackPane borderSidePane = new StackPane();
		borderSidePane.setPrefWidth(50);
		
		Group labelHolder = new Group(yLabel);
		borderSidePane.getChildren().add(labelHolder);
		StackPane.setAlignment(labelHolder, Pos.CENTER);

//		yLabel.setPrefWidth(500);
		BorderPane.setAlignment(borderSidePane, Pos.CENTER);

		this.setCenter(mapeScroll);
		this.setLeft(borderSidePane);
		this.setBottom(xLabel);
//		this.setVisible(false);
		sp.setAlignment(Pos.CENTER);
		

		
//		BorderPane.setAlignment(innerSuggestions, Pos.CENTER_RIGHT);
	}
	
	//This method maybe can be called internally? meaning this class receive receive the event!
	public void dimensionsUpdated(ArrayList<SuggestionRoom> roomDisplays, MAPEDimensionFXML[] dimensions)
	{
		//This should be extended for more dimensions but at the moment we only care about this two!
		SetXLabel(dimensions[0].getDimension().toString());
		SetYLabel(dimensions[1].getDimension().toString());
		
		SetupInnerGrid(roomDisplays, dimensions[0].getGranularity(),  dimensions[1].getGranularity());
	}
	
	public void SetXLabel(String labelTitle)
	{
		xLabel.setText(labelTitle);
	}
	
	public void SetYLabel(String labelTitle)
	{
		yLabel.setText(labelTitle);
	}
	
	public void SetupInnerGrid(ArrayList<SuggestionRoom> roomDisplays, int xResolution, int yResolution)
	{
		this.xResolution = xResolution;
		this.yResolution = yResolution;
		
		innerSuggestions.getChildren().clear();
		
		
		for(int j = 0, red = yResolution; j < yResolution; j++, red--)
		{
			Label boxLabel = new Label(String.valueOf((float)red/yResolution));
			boxLabel.setTextFill(Color.WHITE);
			GridPane.clearConstraints(boxLabel);
			GridPane.setConstraints(boxLabel,0, j);
			innerSuggestions.getChildren().add(boxLabel);
			
			for(int i = 1; i < xResolution + 1; i++) 
			{
				GridPane.clearConstraints(roomDisplays.get((i - 1) + j * xResolution).getRoomCanvas());
				GridPane.setConstraints(roomDisplays.get((i - 1) + j * xResolution).getRoomCanvas(), i, (red - 1));
				innerSuggestions.getChildren().add(roomDisplays.get((i - 1) + j * xResolution).getRoomCanvas());
			}
		}
		
		for(int i = 1; i < xResolution + 1; i++) 
		{
			Label boxLabel = new Label(String.valueOf((float)(i)/xResolution));
			boxLabel.setTextFill(Color.WHITE);
			GridPane.clearConstraints(boxLabel);
			GridPane.setConstraints(boxLabel, i, yResolution);
			GridPane.setHalignment(boxLabel, HPos.CENTER);
			innerSuggestions.getChildren().add(boxLabel);
		}
		
		mapeScroll.setMinWidth(10);
		mapeScroll.setMaxWidth(599);
		mapeScroll.setMinHeight(150);
		mapeScroll.setMaxHeight(600);
		
		this.autosize();
		
	}
	
	//What a ridiculous thing to do...
	public Node GetGridCell(int col, int row)
	{
		 Node result = null;

	    for (Node node : innerSuggestions.getChildren())
	    {
	        if(GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col) 
	        {
	            result = node;
	            break;
	        }
	    }

	    return result;
	}
	
	public void SaveDimensionalGrid()
	{
		MapRenderer.getInstance().saveMAPE(sp); //Needs to be fixed in next iterations
	}

	@Override
	public void ping(PCGEvent e) 
	{
		// TODO Auto-generated method stub
		if(e instanceof SaveDisplayedCells)
		{
			Platform.runLater(() -> {
				MapRenderer.getInstance().saveMAPE(sp);
			});
//			SaveDimensionalGrid();
		}
	}
	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
