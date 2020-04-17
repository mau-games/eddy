package gui.views;

import finder.geometry.Point;
import game.ApplicationConfig;
import game.Dungeon;
import game.Tile;
import game.quest.Action;
import game.quest.ActionType;
import game.quest.actions.*;
import gui.utils.DungeonDrawer;
import gui.utils.InformativePopupManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;


import java.io.IOException;
import java.util.*;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class QuestViewController extends BorderPane implements Listener {
    private final EventRouter router = EventRouter.getInstance();
    private ApplicationConfig config;
    private boolean isActive = false;
    private Dungeon dungeon;
    private Point selectedPosition;
    private QuestPositionUpdate updatedPosition = null;

    @FXML
    private ScrollPane mapScrollPane;
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
        router.registerListener(this, new RequestQuestView());
        router.registerListener(this, new QuestPositionUpdate(null,null));

        initQuestView();
        initActionToolbar();
    }

    private void initQuestView() {
        questPane.getChildren().stream()
                .filter(node -> node.getId().equals("questPlaceholder"))
                .forEach(node -> {
                    node.addEventHandler(MouseEvent.MOUSE_CLICKED,
                            event -> {
                                int actionCount = questPane.getChildren().size();
                                tbQuestTools.getItems().stream()
                                        .filter(action -> ((ToggleButton) action).isSelected())
                                        .forEach(selected -> {
                                            ToggleButton toggleButton = (ToggleButton) selected;
                                            int questCount = dungeon.getQuest().getActions().size();
                                            Action action = addQuestAction(toggleButton, questCount);
                                            addQuestPaneAction(action, actionCount - 1);
                                            toggleButton.setSelected(false);

                                        });
                            });
                });

        this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if(event.getCode().equals(KeyCode.DELETE)){
                questPane.getChildren().stream()
                        .filter(questAction -> questAction instanceof ToggleButton)
                        .filter(questAction -> ((ToggleButton)questAction).isSelected())
                        .forEach(questAction -> {
                            Platform.runLater(() -> {
                                removeQuestAction((ToggleButton) questAction);
                            });
                        });
            }
        });
    }

    private void initActionToolbar(){
        tbQuestTools.getItems()
                .forEach(toolbarAction -> {
                    toolbarAction.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                        if (((ToggleButton)toolbarAction).isSelected()){
                            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.QUEST_POS);
                            System.out.println("request DisplayQuestTiles");
                            EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesSelection());
                            questPane.getChildren()
                                    .filtered(questAction -> questAction instanceof ToggleButton)
                                    .filtered(questAction -> ((ToggleButton)questAction).isSelected())
                                    .forEach(questAction -> {
                                        //TODO: needs to account for position and room
//                                    Platform.runLater(() -> replaceQuestAction((ToggleButton)questAction, (ToggleButton)toolbarAction));
                                    });
                        } else {
                            System.out.println("request UnDisplayQuestTiles");
                            EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesUnselection());
                        }
                    });
                });
    }

    public void initWorldMap(Dungeon dungeon) {
        this.dungeon = dungeon;

        dungeon.dPane.renderAll();
        mapScrollPane.setContent(dungeon.dPane);

		if(this.dungeon.getAllRooms().size() > 3 && this.dungeon.getBosses().isEmpty())
		{
			InformativePopupManager.getInstance().requestPopup(dungeon.dPane, InformativePopupManager.PresentableInformation.NO_BOSS_YET, "");
		}
    }

    @Override
    public void ping(PCGEvent e) {
        if (e instanceof RequestQuestView){
            //disable current dungeon brush so accidents wont happen :)
            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);

            //refresh toolbarActionToggleButton
            tbQuestTools.getItems().forEach(node -> {
                String buttonText = ((ToggleButton)node).getId();
                boolean disable = dungeon.getQuest().getAvailableActions().stream().noneMatch(actionType -> actionType.toString().equals(buttonText));
                node.setDisable(disable);
            });
            //TODO: refresh/reset the QuestActionToggleButtons
        } else if (e instanceof QuestPositionUpdate){
            updatedPosition = (QuestPositionUpdate) e;
            System.out.println(String.format("%s => { %d : %d }",
                    updatedPosition.getRoom(),
                    updatedPosition.getPoint().getX(),
                    updatedPosition.getPoint().getY()));
        }
    }

    /**
     * Set this Pane as active
     * @param active
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void addQuestPaneAction(Action action, int index){
        //add toggle button representation
        ToggleButton toAdd = new ToggleButton();
        toAdd.setText(action.getName());
        toAdd.setId(action.getId().toString());
        toAdd.setToggleGroup(questActions);
        toAdd.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if(e.getButton().equals(MouseButton.PRIMARY)){
                tbQuestTools.getItems().stream()
                        .filter(a -> ((ToggleButton)a).isSelected())
                        .forEach(s -> {
                            //TODO: needs to account for position and room
//                            replaceQuestAction(toAdd, (ToggleButton) s)
                        });
            } else if (e.getButton().equals(MouseButton.SECONDARY)){
                System.out.println("secondary");
                //todo: add a popup option menu
            }
        });
        questPane.getChildren().add(index, toAdd);

        //add arrow label
        Label arrow = new Label("=>");
        arrow.setTextFill(Color.WHITE);
        arrow.setFont(Font.font(14.0));
        arrow.setStyle("-fx-background-color: transparent;");

        questPane.getChildren().add(index + 1, arrow);
    }

    /**
     * adds action to quest and gives it random available position  for now.
     * TODO: let the user choose a available position on the map
     * @param toolbarActionToggleButton
     * @return
     */
    private Action addQuestAction(ToggleButton toolbarActionToggleButton, int index) {
        Random random = new Random();

        //add QuestAction in Quest
        ActionType type = ActionType.valueOf(toolbarActionToggleButton.getId().toUpperCase());
        Action action = null;
        List<Tile> tiles = new ArrayList<Tile>();
        switch (type){
            case CAPTURE:
                action = new CaptureAction();
                tiles.addAll(dungeon.getEnemies());
                tiles.addAll(dungeon.getBosses());
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case DAMAGE:
                action = new DamageAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case DEFEND:
                action = new DefendAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case ESCORT:
                action = new EscortAction();
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case EXCHANGE: //needs 2 positions
                action = new ExchangeAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                tiles.clear();
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    ((ExchangeAction)action).setSecondPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case EXPERIMENT:
                action = new ExperimentAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case EXPLORE:
                action = new ExploreAction();
                dungeon.getAllRooms().forEach(room -> Collections.addAll(tiles, room.getTileBasedMap()));
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case GATHER:
                action = new GatherAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case GIVE:
                action = new GiveAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                tiles.clear();
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    ((GiveAction)action).setSecondPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case GO_TO:
                action = new GotoAction();
                dungeon.getAllRooms().forEach(room -> Collections.addAll(tiles, room.getTileBasedMap()));
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case KILL:
                action = new KillAction();
                tiles.addAll(dungeon.getEnemies());
                tiles.addAll(dungeon.getBosses());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case LISTEN:
                action = new ListenAction();
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case READ:
                action = new ReadAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case REPAIR:
                action = new RepairAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case REPORT:
                action = new ReportAction();
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case SPY:
                action = new SpyAction();
                tiles.addAll(dungeon.getEnemies());
                tiles.addAll(dungeon.getBosses());
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case STEALTH:
                action = new StealthAction();
                tiles.addAll(dungeon.getEnemies());
                tiles.addAll(dungeon.getBosses());
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case TAKE:
                action = new TakeAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());

                tiles.clear();
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 0) //TODO: temporary until other method for finding user-picked position
                    ((TakeAction)action).setSecondPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case USE:
                action = new UseAction();
                tiles.addAll(dungeon.getItems());
                break;
        }
        if (action != null){
            action.setId(UUID.randomUUID());
            action.setType(type);
        }
        dungeon.getQuest().addActionsAt(index,action);

        return action;
    }

    private void removeQuestAction(ToggleButton questActionToggleButton){
        //remove the QuestAction from Quest
        dungeon.getQuest().removeAction(dungeon.getQuest().getAction(UUID.fromString(questActionToggleButton.getId())));
        //remove the arrow label after QuestActionToggleButton
        questPane.getChildren().remove(questPane.getChildren().indexOf(questActionToggleButton) + 1);
        //remove the QuestActionToggleButton
        questPane.getChildren().remove(questActionToggleButton);
    }

    private void replaceQuestAction(ToggleButton questActionToggleButton, ToggleButton toolbarActionToggleButton){
        int questIndex = dungeon.getQuest().indexOf(dungeon.getQuest().getAction(UUID.fromString(questActionToggleButton.getId())));
        int paneIndex = questPane.getChildren().indexOf(questActionToggleButton);

        removeQuestAction(questActionToggleButton);
        Action action = addQuestAction(toolbarActionToggleButton, questIndex);
        addQuestPaneAction(action, paneIndex);

        toolbarActionToggleButton.setSelected(false);
        questActionToggleButton.setSelected(false);
    }

    public void selectBrush(){

    }

    @FXML
    private void backWorldView(ActionEvent event) throws IOException
    {
        EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesUnselection());
        DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.MOVEMENT);
        dungeon.dPane.setDisable(false);
        router.postEvent(new RequestWorldView());
    }


}
