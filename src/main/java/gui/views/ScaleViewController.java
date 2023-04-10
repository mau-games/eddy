package gui.views;

import game.Dungeon;
import game.Room;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import util.algorithms.ScaleFibonacci;
import util.algorithms.NearestNeighbour;
import util.algorithms.ScaleMatrix;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.RequestScaleSettings;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;

import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

import java.awt.*;

import java.util.Arrays;

public class ScaleViewController implements Listener{
    private Dungeon dungeon;
    private WorldViewController worldView;
    private Room scaleRoom;
    private static EventRouter router = EventRouter.getInstance();
    private Stage window = new Stage();
    private Label scalingLb,listLb ;
    private ComboBox<String> updownCb, propertiesCb1, propertiesCb2;
    private ChoiceBox<String> scalingCb;
    private ObservableList<String> propertyList;
    private Button scaleBtn;
    private VBox topLayout;
    private Scene scene;

    public enum ScaleType{
        None,
        NearestNeighbour,
        Fibonacci
    }
    public enum SizeAdjustType{
        Upscale,
        Downscale
    }
    public ScaleViewController(WorldViewController worldView, Dungeon dungeon, Room scaleRoom){
        this.dungeon = dungeon;
        this.worldView = worldView;
        this.scaleRoom = scaleRoom;
        router.registerListener(this, new RequestScaleSettings(null, null, -1, null, null));
        //router.postEvent(new RequestScaleSettings("Downscale","NearestNeighbour", 2));

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("ScalingUI DEMO");
        window.setMinWidth(250);
        window.setMinHeight(400);


        scalingLb = new Label("Select a scaling function.");
        scalingLb.setStyle("-fx-text-fill: linear-gradient(#b9bbc9,#afafaf); -fx-font-size: 16px;");
        scalingCb = new ChoiceBox<>();

        // opptions do not show
        // make sure that the max map width is not creating problems when scaling
        scalingCb.getItems().addAll("None","Fibonacci","3:4","1:2","1:3","1:4","2:1","3:2","4:1");
        scalingCb.setStyle("-fx-background-color: linear-gradient(#b9bbc9,#3d3d3d); -fx-padding: 5;\r\n" +
                "    -fx-text-fill: linear-gradient (#b9bbc9,#afafaf); -fx-font-size: 16px; \r\n" +
                "    -fx-border-style: none;\r\n" +
                "    -fx-border-width: 0;\r\n" +
                "    -fx-border-insets: 0;");

        updownCb = new ComboBox<>();
        updownCb.getItems().addAll("Upscale","Downscale");
        updownCb.setPromptText("Select upscaling or downscaling");
        updownCb.setStyle("-fx-background-color: linear-gradient(#b9bbc9,#3d3d3d); -fx-padding: 5;\r\n" +
                "    -fx-text-fill: linear-gradient (#b9bbc9,#afafaf); -fx-font-size: 16px; \r\n" +
                "    -fx-border-style: none;\r\n" +
                "    -fx-border-width: 0;\r\n" +
                "    -fx-border-insets: 0;");

        listLb = new Label("Select the desirable properties to be used in EA");
        listLb.setStyle("-fx-text-fill: linear-gradient(#b9bbc9,#afafaf); -fx-font-size: 16px;");


        propertyList = FXCollections.observableArrayList("None","Leniency","Similarity","Symmetry","Inner_Similarity","Number_Pattern","Number_Meso_Pattern","Linearity","Custom");

        propertiesCb1 = new ComboBox<>(propertyList);
        propertiesCb1.setValue("None");
        propertiesCb1.setStyle("-fx-background-color: linear-gradient(#b9bbc9,#3d3d3d); -fx-padding: 5;\r\n" +
                "    -fx-text-fill: linear-gradient (#b9bbc9,#afafaf); -fx-font-size: 16px; \r\n" +
                "    -fx-border-style: none;\r\n" +
                "    -fx-border-width: 0;\r\n" +
                "    -fx-border-insets: 0;");

        propertiesCb2 = new ComboBox<>(propertyList);
        propertiesCb2.setValue("None");
        propertiesCb2.setStyle("-fx-background-color: linear-gradient(#b9bbc9,#3d3d3d); -fx-padding: 5;\r\n" +
                "    -fx-text-fill: linear-gradient (#b9bbc9,#afafaf); -fx-font-size: 16px; \r\n" +
                "    -fx-border-style: none;\r\n" +
                "    -fx-border-width: 0;\r\n" +
                "    -fx-border-insets: 0;");

        scaleBtn = new Button("Scale");
        scaleBtn.setStyle("-fx-background-color: linear-gradient(#dc9656,#ab4642); -fx-padding: 5;\r\n" +
        "    -fx-border-style: none;\r\n" +
                "    -fx-border-width: 0;\r\n" +
                "    -fx-border-insets: 0;\r\n"+
                "-fx-body-color: #d3d3d3;\r\n"+
                "-fx-body-insert: 0; \r\n" +
                "-fx-text-fill: linear-gradient(#d6e1e1,#ffffff); -fx-font-size: 20px; \r\n"
        );
        scaleBtn.setMinSize(250,50);
        scaleBtn.setMaxSize(250,50);





        scalingCb.getSelectionModel().selectedItemProperty().addListener( (v, oldValue, newValue) -> { if( newValue != "Fibonacci") {
            updownCb.setVisible(false);
        }else {
            updownCb.setVisible(true);
        }
        });

        /*

        scaleBtn.addEventHandler(ActiveEvent e -> {router.postEvent(new RequestScaleSettings(String strSizeAdjType, String strScaleType, double scaleFactor, String firstDimType, String secDimType))
        window.close();
        });
       // actionEvent till Btn router.postevent(new RequestScaleSettings(String strSizeAdjType, String strScaleType, double scaleFactor, String firstDimType, String secDimType));

        */


        scaleBtn.setOnAction(e -> {
            scaleAndSend();
            window.close();
        });

        topLayout = new VBox(10);
        topLayout.setPadding(new Insets(20,20,20,20));
        topLayout.getChildren().addAll(scalingLb,scalingCb,updownCb,listLb,propertiesCb1,propertiesCb2,scaleBtn);
        topLayout.setAlignment(Pos.TOP_CENTER);
        //text color wrong
        topLayout.setStyle("-fx-background-color: #2c2f33;"+"-fx-padding: 5;\r\n" +
        "    -fx-border-style: none;\r\n" +
                "-fx-font-color: #D3D3D3;\r\n"+
                "    -fx-border-width: 0;\r\n" +
                "    -fx-border-insets: 0; ") ;



        // main window



        scene = new Scene(topLayout);
        scene.getStylesheets().add(this.getClass().getResource("/gui/bootstrap3.css").toExternalForm());
        window.setScene(scene);
        window.showAndWait();

        System.out.println("ScaleViewController: " + scaleRoom.getRoomHeight() + " height. " + scaleRoom.getRoomWidth() + " width.");
    }

    /***
     * This method is called in the ActionEvent handler for the button, it gets the users inputs and send it to the RequestScaleSettings()
     *
     * rename method later?
     */
    public void scaleAndSend(){
        String scalingType, updownType;
        double scalefactor;
        if (scalingCb.getValue()=="Fibonacci") {
            scalefactor = 1.618;
            updownType = updownCb.getValue();
            scalingType = "Fibonacci";
        }else if(scalingCb.getValue()=="None"){
            scalefactor = 1;
            updownType = "Upscale";
            scalingType = "None";
        }else {
            //scalefactor = (Double.parseDouble(scalingCb.getValue().substring(0, 1).replace(':',' ')) / Double.parseDouble(scalingCb.getValue().substring(2)));
            int first = Integer.parseInt(scalingCb.getValue().substring(0, 1).replace(':',' '));
            int secound = Integer.parseInt(scalingCb.getValue().substring(2));
            scalefactor = (first  < secound)?secound:first;
            updownType = (first>=secound)?"Upscale":"Downscale";
            scalingType = "NearestNeighbour";
        }
        router.postEvent(new RequestScaleSettings(updownType, scalingType, scalefactor));
       /*
       This method call below will work when the implementation of RequestScaleSettings method has full functional implementation.
       router.postEvent(new RequestScaleSettings(updownType, scalingType, scalefactor,propertiesCb1.getValue(),propertiesCb2.getValue()));
        */
    }
    @Override
    public synchronized void ping(PCGEvent e){
        if(e instanceof RequestScaleSettings){
            RequestScaleSettings rSS = (RequestScaleSettings)e;
            ScaleType scaleType = ScaleType.valueOf(rSS.getStrScaleType());
            SizeAdjustType sizeAdjustType = SizeAdjustType.valueOf(rSS.getStrSizeAdjType());
            ScaleMatrix scaleMatrix = null;
            int[][] matrix;

            switch (scaleType){
                case NearestNeighbour:
                    scaleMatrix = new NearestNeighbour(scaleRoom.toMatrix(), (int)rSS.getScaleFactor());
                    break;
                case Fibonacci:
                    scaleMatrix = new ScaleFibonacci(scaleRoom.toMatrix(), rSS.getScaleFactor());
                    break;
                case None:
                    System.out.println("None: scaletype");
                    break;
                default:
                    System.out.println("Invalid: scaletype");
                    break;
            }
            System.out.println(sizeAdjustType.toString());

            switch (sizeAdjustType){
                case Upscale:
                    matrix = scaleMatrix.Upscale();
                    dungeon.addRoom(matrix);
                    worldView.initWorldMap(dungeon);
                    System.out.println(Arrays.deepToString(matrix));
                    break;
                case Downscale:
                    matrix = scaleMatrix.Downscale();
                    dungeon.addRoom(matrix);
                    worldView.initWorldMap(dungeon);
                    System.out.println(Arrays.deepToString(matrix));
                    break;
                default:
                    System.out.println("Invalid: sizeAdjustment");
                    break;
            }

        }
    }
}
