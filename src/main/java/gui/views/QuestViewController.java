package gui.views;

import game.ApplicationConfig;
import game.Dungeon;
import gui.utils.InformativePopupManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import util.IntField;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;
import util.eventrouting.events.RequestWorldView;
import util.eventrouting.events.Stop;


import java.io.IOException;

public class QuestViewController extends BorderPane implements Listener {
    private final EventRouter router = EventRouter.getInstance();
    private ApplicationConfig config;
    private boolean isActive = false;
    private Dungeon dungeon;

    @FXML
    private Pane mapPane;
    @FXML
    private BorderPane buttonPane;
    @FXML
    private StackPane questPane;


    public QuestViewController() {
        super();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/interactive/QuestView.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
            config = ApplicationConfig.getInstance();
        } catch (IOException | MissingConfigurationException ex) {
            ex.printStackTrace();
        }

        router.registerListener(this, new MapUpdate(null));

        initQuestView();
    }

    public void initQuestView() {
        Label title = new Label("Quest Editor");
        title.setStyle("-fx-font-weight: bold");
        title.setStyle("-fx-font-size: 40px");
        title.setStyle("-fx-text-inner-color: white;");
        //mapPane.getChildren().add(title);
    }

    public void initWorldMap(Dungeon dungeon)
    {
        //CHECK THE PROPERTY
//        if(widthField == null)
//            widthField = new IntField(1, 20, dungeon.defaultWidth);
//
//        if(heightField == null)
//            heightField = new IntField(1, 20, dungeon.defaultHeight);
//
        this.dungeon = dungeon;
        this.dungeon.dPane.setDisable(true);
        mapPane.getChildren().clear();
//        mapPane.setBorder(new Border(new BorderStroke(Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));


        dungeon.dPane.renderAll();
        mapPane.getChildren().add(dungeon.dPane);

		if(this.dungeon.getAllRooms().size() > 3 && this.dungeon.getBosses().isEmpty())
		{
			InformativePopupManager.getInstance().requestPopup(dungeon.dPane, InformativePopupManager.PresentableInformation.NO_BOSS_YET, "");
		}
    }

    @Override
    public void ping(PCGEvent e) {

    }

    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void selectBrush(){

    }

    @FXML
    private void backWorldView(ActionEvent event) throws IOException
    {
//        router.postEvent(new Stop());
        dungeon.dPane.setDisable(false);
        router.postEvent(new RequestWorldView());
    }

}
