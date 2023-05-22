package gui.views;

import game.Room;
import game.RoomScale;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;

import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;

public class ScaleOptionsViewController{
    private Room room;
    private Room initRoom;
    private RoomScale scale;
    private static EventRouter router = EventRouter.getInstance();
    private Stage window = new Stage();
    private Label scalingLb,listLb ;
    private ComboBox<String> updownCb, propertiesCb1, propertiesCb2;
    private ChoiceBox<String> scalingCb;
    private ObservableList<String> propertyList;
    private Button scaleBtn;
    private VBox topLayout;
    private Scene scene;
    private static int lowIterationVal = 50;
    private static int normalIterationVal = 150;
    private static int highIterationVal = 1000;
    private static int insanityIterationVal = 2000;
    private static int dimLimit = 3;

    public ScaleOptionsViewController(Room room, Room initRoom){
        this.room = room;
        this.initRoom = initRoom;
        displayUI();
    }

    public void displayUI(){
        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("ScalingUI DEMO");
        window.setMinWidth(250);
        window.setMinHeight(400);

        scalingLb = new Label("Select a scaling function.");
        scalingLb.setStyle("-fx-text-fill: linear-gradient(#b9bbc9,#afafaf); -fx-font-size: 16px;");
        scalingCb = new ChoiceBox<>();

        // opptions do not show
        // make sure that the max map width is not creating problems when scaling
        scalingCb.getItems().addAll("-","Fibonacci","1:2","1:3","1:4","2:1","3:1","4:1");
        scalingCb.setStyle("-fx-background-color: linear-gradient(#b9bbc9,#3d3d3d); -fx-padding: 5;\r\n" +
                "    -fx-text-fill: linear-gradient (#b9bbc9,#afafaf); -fx-font-size: 16px; \r\n" +
                "    -fx-border-style: none;\r\n" +
                "    -fx-border-width: 0;\r\n" +
                "    -fx-border-insets: 0;");

        updownCb = new ComboBox<>();
        updownCb.getItems().addAll("Upscale","Downscale");
        updownCb.setPromptText("Downscale or Upscale");
        updownCb.setStyle("-fx-background-color: linear-gradient(#b9bbc9,#3d3d3d); -fx-padding: 5;\r\n" +
                "    -fx-text-fill: linear-gradient (#b9bbc9,#afafaf); -fx-font-size: 16px; \r\n" +
                "    -fx-border-style: none;\r\n" +
                "    -fx-border-width: 0;\r\n" +
                "    -fx-border-insets: 0;");

        listLb = new Label("Select the desirable properties to be used in EA");
        listLb.setStyle("-fx-text-fill: linear-gradient(#b9bbc9,#afafaf); -fx-font-size: 16px;");

        propertyList = FXCollections.observableArrayList("-","Difficulty","Symmetry","Inner-similarity","Number of patterns","Number of meso-patterns","Linearity","All");

        propertiesCb1 = new ComboBox<>(propertyList);
        propertiesCb1.setValue("-");
        propertiesCb1.setStyle("-fx-background-color: linear-gradient(#b9bbc9,#3d3d3d); -fx-padding: 5;\r\n" +
                "    -fx-text-fill: linear-gradient (#b9bbc9,#afafaf); -fx-font-size: 16px; \r\n" +
                "    -fx-border-style: none;\r\n" +
                "    -fx-border-width: 0;\r\n" +
                "    -fx-border-insets: 0;");

        propertiesCb2 = new ComboBox<>(propertyList);
        propertiesCb2.setValue("-");
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
            sendScaleSettings();
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
    }

    /***
     * This method is called in the ActionEvent handler for the button, it gets the users inputs and send it to the RequestScaleSettings()
     *
     */
    private void sendScaleSettings(){
        String scalingType, updownType;
        double scalefactor;
        String[] preserveDimArr = new String[2]; //Change later?

        switch (scalingCb.getValue()){
            case "Fibonacci":
                scalefactor = 1.618;
                updownType = updownCb.getValue();
                scalingType = "Fibonacci";
                break;
            case "NONE":
                scalefactor = 1;
                updownType = "NONE";
                scalingType = "NONE";
                break;
            default:
                //scalefactor = (Double.parseDouble(scalingCb.getValue().substring(0, 1).replace(':',' ')) / Double.parseDouble(scalingCb.getValue().substring(2)));
                int first = Integer.parseInt(scalingCb.getValue().substring(0, 1).replace(':',' '));
                int secound = Integer.parseInt(scalingCb.getValue().substring(2));
                scalefactor = (first  < secound)?secound:first;
                updownType = (first>=secound)?"Upscale":"Downscale";
                scalingType = "NearestNeighbour";
        }

        preserveDimArr[0] = propertiesCb1.getValue();
        preserveDimArr[1] = propertiesCb2.getValue();

        scale = new RoomScale(room, updownType, scalingType, scalefactor, preserveDimArr);
        scale.setInitRoom(initRoom);
        int[][] matrix = scale.calculateScaledMatrix(room);
        router.postEvent(new RequestMatrixScaledRoom(matrix, scale, false));

        if(scale.getOrigRoom() != scale.getInitRoom()){

            System.out.println("Doors before: " + scale.getScaledRoom().getDoorCount());
            int nmbrOfDoors = scale.getScaledRoom().getDoorCount();
            for (int i = 0; i<nmbrOfDoors; i++){
                scale.createConnection(initRoom, scale.getScaledRoom(), RoomScale.ConnectionType.DownAtFloor, RoomScale.ConnectionType.DoorToDoor);
            }

            System.out.println("Doors after: " + scale.getScaledRoom().getDoorCount() + " Wallcount: " + scale.getScaledRoom().getWallCount() + " Enemycount: " + scale.getScaledRoom().getEnemyCount());
            scale.setNewDoors();

            if(!scale.getPreservedDimValues().isEmpty()) {
                Platform.runLater(() -> {

                    //router.postEvent(new StartGA_MAPE(scale.getScaledRoom(), scale.calculateAllMAPEDimensions(), lowIterationVal));
                    router.postEvent(new StartGA_MAPE(scale.getScaledRoom(), scale.calculateAllMAPEDimensions(), normalIterationVal));
                    //router.postEvent(new StartGA_MAPE(scale.getScaledRoom(), scale.calculateMAPEDimensions(), highIterationVal));
                });
            }
        }
    }
}
