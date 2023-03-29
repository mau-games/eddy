package gui.views;

import game.Room;
import javafx.fxml.FXML;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import util.eventrouting.events.RequestScaleRoom;

public class ScaleViewController {

    public ScaleViewController(RequestScaleRoom scaleRoom){
        System.out.println("ScaleViewController: " + scaleRoom.getHeight() + " height. " + scaleRoom.getWidth() + " width.");
    }
}
