package gui.views;

import game.ApplicationConfig;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapUpdate;


import java.io.IOException;

public class QuestViewController extends BorderPane implements Listener {
    private EventRouter router = EventRouter.getInstance();
    private ApplicationConfig config;
    private boolean isActive = false;

    @FXML
    private Pane mapPane;
    @FXML
    private StackPane buttonPane;
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

//        router.registerListener(this, new MapUpdate(null));

        initQuestView();
    }

    public void initQuestView() {
        Label title = new Label("Quest Editor");
        title.setStyle("-fx-font-weight: bold");
        title.setStyle("-fx-font-size: 40px");
        title.setStyle("-fx-text-inner-color: white;");
        mapPane.getChildren().add(title);
    }

    @Override
    public void ping(PCGEvent e) {

    }

    public void setActive(boolean active) {
        this.isActive = active;
    }
}
