package gui.controls;

import game.Room;
import game.TileTypes;
import gui.utils.MapRenderer;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import util.Point;

import java.util.ArrayList;
import java.util.HashMap;

public class ScaledMapsVisualizationPane extends BorderPane {

    private final static HashMap<TileTypes, Image> images = new HashMap<TileTypes, Image>();
    private MapRenderer renderer = MapRenderer.getInstance();
    private GridPane scaledMapsPane;
    private GridPane tileMapsPane;
    private HBox sp;

    public ScaledMapsVisualizationPane(){
        super();
    }

    public void init(ArrayList<ScaledRoom> roomDisplays) {
        scaledMapsPane = new GridPane();
        scaledMapsPane.setPadding(new Insets(10, 10, 10, 10));
        scaledMapsPane.setStyle("-fx-background-color: transparent;");
        scaledMapsPane.setHgap(15.0);
        scaledMapsPane.setVgap(15.0);
        scaledMapsPane.setAlignment(Pos.CENTER);
        int counter = 0;

        for(ScaledRoom scaledRoom: roomDisplays){
            tileMapsPane = new GridPane();
            int cols = scaledRoom.getScaledRoom().getColCount();
            int rows = scaledRoom.getScaledRoom().getRowCount();
                    images.clear();

            autosize();
            double width = scaledMapsPane.getWidth() / cols;
            double height = scaledMapsPane.getHeight() / rows;
            double scale = Math.min(width, height);

            getChildren().clear();

            for (int j = 0; j < rows; j++) {
                for (int i = 0; i < cols; i++) {
                    ImageView iv = new ImageView(getImage(scaledRoom.getScaledRoom().getTile(i, j).GetType(), scale));
                    GridPane.setFillWidth(iv, true);
                    GridPane.setFillHeight(iv, true);
                    tileMapsPane.add(iv, i, j);
                }
            }

            scaledMapsPane.add(tileMapsPane, counter, 1);
            counter++;
        }

        sp = new HBox(scaledMapsPane);
    }

    public Image getImage(TileTypes type, double size)
    {
        Image tile = images.get(type);

        tile = renderer.renderTile(type, size, size);
        images.put(type, tile);

        return tile;
    }


}
