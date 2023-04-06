package gui.views;

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

public class ScaleViewController implements Listener{
    private Room scaleRoom;
    private NearestNeighbour nn;
    private ScaleFibonacci fib;
    private static EventRouter router = EventRouter.getInstance();

    public enum ScaleType{
        None,
        NearestNeighbour,
        Fibonacci
    }
    public enum SizeAdjustType{
        Upscale,
        Downscale
    }
    public ScaleViewController(Room scaleRoom){
        this.scaleRoom = scaleRoom;
        router.registerListener(this, new RequestScaleSettings(null, null, -1, null, null));

        Stage window = new Stage();

        window.initModality(Modality.APPLICATION_MODAL);
        window.setTitle("ScalingUI DEMO");
        window.setMinWidth(250);
        window.setMinHeight(400);


        Label scalingLb = new Label("Please select a scaling function.");
        ChoiceBox<String> scalingCb = new ChoiceBox<>();
        TextField testTf = new TextField();

        // opptions do not show
        scalingCb.getItems().add("test");
        // make sure that the max map width is not creating problems when scaling
        scalingCb.getItems().addAll("Fibonacci","3:4","1:2","1:3","1:4","2:1","3:2","4:1");

        ComboBox<String> updownCb = new ComboBox<>();
        updownCb.getItems().addAll("Upscale","Downscale");
        updownCb.setPromptText("Please Select upscaling or downscaling");

        Label listLb = new Label("Select the desirable properties to be used in EA");

        ObservableList<String> propertyList = FXCollections.observableArrayList("None","Leniency","Similarity","Symmetry","Inner_Similarity","Number_Pattern","Number_Meso_Pattern","Linearity","Custom");

        ComboBox<String> propertiesCb1 = new ComboBox<>(propertyList);
        propertiesCb1.setValue("None");

        ComboBox<String>propertiesCb2 = new ComboBox<>(propertyList);
        propertiesCb2.setValue("None");

        Button scaleBtn = new Button("Scale");
        scaleBtn.setStyle("-fx-background-color: linear-gradient(#dc9656,#ab4642); -fx-padding: 5;\r\n" +
        "    -fx-border-style: none;\r\n" +
                "    -fx-border-width: 0;\r\n" +
                "    -fx-border-insets: 0;");


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
        VBox topLayout = new VBox(10);
        topLayout.setPadding(new Insets(20,20,20,20));
        topLayout.getChildren().addAll(scalingLb,scalingCb,testTf,updownCb,listLb,propertiesCb1,propertiesCb2,scaleBtn);
        topLayout.setAlignment(Pos.TOP_CENTER);
        //font color wrong
        topLayout.setStyle("-fx-background-color: #2c2f33;"+"-fx-padding: 5;\r\n" +
        "    -fx-border-style: none;\r\n" +
                "-fx-font-color: #D3D3D3;\r\n"+
                "    -fx-border-width: 0;\r\n" +
                "    -fx-border-insets: 0;") ;


        // main window



        Scene scene = new Scene(topLayout);
        window.setScene(scene);
        window.showAndWait();

        System.out.println("ScaleViewController: " + scaleRoom.getRoomHeight() + " height. " + scaleRoom.getRoomWidth() + " width.");
    }
    @Override
    public synchronized void ping(PCGEvent e){
        if(e instanceof RequestScaleSettings){
            RequestScaleSettings rSS = (RequestScaleSettings)e;
            ScaleType scaleType = ScaleType.valueOf(rSS.getStrScaleType());
            SizeAdjustType sizeAdjustType = SizeAdjustType.valueOf(rSS.getStrSizeAdjType());
            boolean isUpscale = false;

            if(sizeAdjustType == SizeAdjustType.Upscale)
                isUpscale = true;

            switch (scaleType){
                case NearestNeighbour:
                    nn = new NearestNeighbour(scaleRoom.toMatrix(), (int)rSS.getScaleFactor(), isUpscale);
                    break;
                case Fibonacci:
                    fib = new ScaleFibonacci(scaleRoom.toMatrix(), rSS.getScaleFactor(), isUpscale);
                    break;
                case None:
                    System.out.println("None: scaletype");
                    break;
                default:
                    System.out.println("Invalid: scaletype");
                    break;

            }
        }
    }
}
