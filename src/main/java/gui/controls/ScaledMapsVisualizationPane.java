package gui.controls;

import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

import java.util.ArrayList;

public class ScaledMapsVisualizationPane extends BorderPane {

    private GridPane scaledMapsPane;
    private HBox sp;

    public ScaledMapsVisualizationPane(){
        super();
    }

    public void init(ArrayList<ScaledRoom> roomDisplays, int width, int height){
        scaledMapsPane = new GridPane();
        scaledMapsPane.setStyle("-fx-background-color: transparent;");
        scaledMapsPane.setHgap(15.0);
        scaledMapsPane.setVgap(10.0);
        scaledMapsPane.setAlignment(Pos.CENTER);
        setupGrid(roomDisplays, width, height);
        sp = new HBox(scaledMapsPane);
    }

    private void setupGrid(ArrayList<ScaledRoom> roomDisplays, int width, int height){

        scaledMapsPane.getChildren().clear();

        for(int j = 0, red = height; j < height; j++, red--)
        {
            Label boxLabel = new Label(String.valueOf((float)red/height));
            boxLabel.setTextFill(Color.WHITE);
            GridPane.clearConstraints(boxLabel);
            GridPane.setConstraints(boxLabel,0, j);
            scaledMapsPane.getChildren().add(boxLabel);

            for(int i = 1; i < width + 1; i++)
            {
                GridPane.clearConstraints(roomDisplays.get((i - 1) + j * width).getRoomCanvas());
                GridPane.setConstraints(roomDisplays.get((i - 1) + j * width).getRoomCanvas(), i, (red - 1));
                scaledMapsPane.getChildren().add(roomDisplays.get((i - 1) + j * width).getRoomCanvas());
            }
        }

        for(int i = 1; i < width + 1; i++)
        {
            Label boxLabel = new Label(String.valueOf((float)(i)/width));
            boxLabel.setTextFill(Color.WHITE);
            GridPane.clearConstraints(boxLabel);
            GridPane.setConstraints(boxLabel, i, height);
            GridPane.setHalignment(boxLabel, HPos.CENTER);
            scaledMapsPane.getChildren().add(boxLabel);
        }

        this.autosize();
    }



}
