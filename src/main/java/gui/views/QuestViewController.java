package gui.views;

import game.ApplicationConfig;
import game.Dungeon;
import gui.utils.InformativePopupManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
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
    private FlowPane questPane;
    @FXML
    private ToolBar tbQuestTools;
    @FXML
    private ToggleGroup questActions;


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
        questPane.getChildren().stream()
                .filter(node -> node.getId().equals("questPlaceholder"))
                .forEach(node -> {
                    node.addEventHandler(MouseEvent.MOUSE_CLICKED,
                            event -> {
                                int actionCount = questPane.getChildren().size();
                                tbQuestTools.getItems().stream()
                                        .filter(action -> ((ToggleButton) action).isSelected())
                                        .forEach(selected -> {
                                            ToggleButton tb = (ToggleButton) selected;
                                            ToggleButton toAdd = new ToggleButton();
                                            toAdd.setText(tb.getText());
                                            toAdd.setToggleGroup(questActions);
                                            //TODO: add event for click on ToggleButton in questView
                                            questPane.getChildren().add(actionCount - 1, toAdd);

                                            Label arrow = new Label("=>");
                                            arrow.setTextFill(Color.WHITE);
                                            arrow.setFont(Font.font(14.0));
                                            arrow.setStyle("-fx-background-color: transparent;");

                                            questPane.getChildren().add(actionCount, arrow);
                                            tb.setSelected(false);
                                        });
                            });
                });
    }

    public void initWorldMap(Dungeon dungeon)
    {
        this.dungeon = dungeon;
        this.dungeon.dPane.setDisable(true);
        mapPane.getChildren().clear();

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
