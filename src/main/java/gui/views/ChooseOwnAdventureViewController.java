package gui.views;

import collectors.ActionLogger;
import collectors.ActionLogger.ActionType;
import collectors.ActionLogger.TargetPane;
import collectors.ActionLogger.View;
import game.*;
import gui.controls.LabeledCanvas;
import gui.controls.Popup;
import gui.utils.DungeonDrawer;
import gui.utils.DungeonDrawer.DungeonBrushes;
import gui.utils.InterRoomBrush;
import gui.utils.MapRenderer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import util.IntField;
import util.Point;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/*  
 * @author Chelsi Nolasco, Malmö University
 * @author Axel Österman, Malmö University*/

public class ChooseOwnAdventureViewController extends VBox implements Listener
{

	private ApplicationConfig config;
	private EventRouter router = EventRouter.getInstance();


	public ChooseOwnAdventureViewController() {
		super();
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/interactive/ChooseOwnAdventure.fxml"));
		loader.setRoot(this);
		loader.setController(this);

		try {
			loader.load();
			config = ApplicationConfig.getInstance();
		} catch (IOException ex) {

		} catch (MissingConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//initWorldView();

		System.out.println("THIS HAPPENED?");
	}

	@Override
	public void ping(PCGEvent e) {

	}
}

