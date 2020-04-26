package gui.views;

import finder.geometry.Point;
import game.ApplicationConfig;
import game.Dungeon;
import game.TileTypes;
import game.quest.Action;
import game.quest.ActionType;
import game.quest.ActionWithSecondPosition;
import game.quest.actions.*;
import gui.utils.DungeonDrawer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
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
    private ActionType selectedActionType = ActionType.NONE;
    private QuestPositionUpdate updatedPosition = null;
    private QuestPositionUpdate secondUpdatedPosition = null;
    private boolean doublePosition = false;

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
    @FXML
    private ToggleGroup questActionsTools;


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
        router.registerListener(this, new QuestPositionUpdate(null,null, false));

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
                                            if (!doublePosition && updatedPosition != null){
                                                Action action = addQuestAction(toggleButton, questCount);
                                                addQuestPaneAction(action, actionCount - 1);
                                                toggleButton.setSelected(false);
                                            }
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
                            selectedActionType = ActionType.valueOf(((ToggleButton) toolbarAction).getId());
                            List<TileTypes> types = findTileTypeByAction();
                            EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesSelection(types));
                        } else {
                            EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesUnselection(false));
                            selectedActionType = ActionType.NONE;
                        }
                    });
                });
    }

    private List<TileTypes> findTileTypeByAction() {
        List<TileTypes> typesList = new LinkedList<TileTypes>();
        switch (selectedActionType) {
            case EXPLORE:
            case GO_TO:
                typesList.add(TileTypes.FLOOR);
                break;
            case EXPERIMENT:
            case GATHER:
            case READ:
            case REPAIR:
            case USE:
            case EXCHANGE:
            case GIVE:
            case TAKE:
                typesList.add(TileTypes.ITEM);
                break;
            case LISTEN:
            case REPORT:
            case ESCORT:
                typesList.add(TileTypes.NPC);
                break;
            case KILL:
                typesList.add(TileTypes.ENEMY);
                typesList.add(TileTypes.ENEMY_BOSS);
                break;
            case CAPTURE:
            case STEALTH:
            case SPY:
                typesList.add(TileTypes.NPC);
                typesList.add(TileTypes.ENEMY);
                typesList.add(TileTypes.ENEMY_BOSS);
                break;
            case DAMAGE:
                typesList.add(TileTypes.ENEMY);
                typesList.add(TileTypes.ENEMY_BOSS);
            case DEFEND:
                typesList.add(TileTypes.ITEM);
                typesList.add(TileTypes.NPC);
                break;
        }
        return typesList;
    }

    public void initWorldMap(Dungeon dungeon) {
        this.dungeon = dungeon;

        dungeon.dPane.renderAll();
        StackPane pane = new StackPane(dungeon.dPane);
        pane.setAlignment(Pos.CENTER);
        mapScrollPane.setContent(null);
        mapScrollPane.setContent(pane);
    }

    @Override
    public void ping(PCGEvent e) {
        if (e instanceof RequestQuestView){
            //disable current dungeon brush so accidents wont happen :)
            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);

            //refresh toolbarActionToggleButton
            tbQuestTools.getItems().forEach(node -> {
                String buttonID = ((ToggleButton)node).getId();
                boolean disable = dungeon.getQuest().getAvailableActions().stream().noneMatch(actionType -> actionType.toString().equals(buttonID));
                node.setDisable(disable);
            });
        } else if (e instanceof QuestPositionUpdate){
            doublePosition = ((QuestPositionUpdate) e).isSecondPosition();
            if (doublePosition) {
                secondUpdatedPosition = (QuestPositionUpdate) e;
                questPane.getChildren()
                        .filtered(questAction -> questAction instanceof ToggleButton)
                        .filtered(questAction -> ((ToggleButton)questAction).isSelected())
                        .forEach(questAction -> {
                            Platform.runLater(() -> replaceQuestAction((ToggleButton)questAction, (ToggleButton) questActionsTools.getSelectedToggle()));
                        });
                //do stuff to select the second position and make sure it doesn't loop around
                doublePosition = false;
            } else {
                updatedPosition = (QuestPositionUpdate) e;
                doublePosition = (selectedActionType.isExchange() || selectedActionType.isGive() || selectedActionType.isTake());
                selectedActionType = ActionType.NONE;
            }
            if (!doublePosition) {
                DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
                questPane.getChildren()
                        .filtered(questAction -> questAction instanceof ToggleButton)
                        .filtered(questAction -> ((ToggleButton)questAction).isSelected())
                        .forEach(questAction -> {
                                    Platform.runLater(() -> replaceQuestAction((ToggleButton)questAction, (ToggleButton) questActionsTools.getSelectedToggle()));
                        });
            }
            EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesUnselection(doublePosition));
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
                if (!doublePosition && updatedPosition != null){
                    tbQuestTools.getItems().stream()
                            .filter(a -> ((ToggleButton)a).isSelected())
                            .forEach(s -> {
                                Platform.runLater(() -> replaceQuestAction(toAdd, (ToggleButton) s));
                            });
                }
            } else if (e.getButton().equals(MouseButton.SECONDARY)){
                //for future use if a pop-up menu is needed.
            }
        });
        toAdd.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            Action tileAction = dungeon.getQuest().getAction(UUID.fromString(toAdd.getId()));
            QuestPositionUpdate firstPosition = new QuestPositionUpdate(tileAction.getPosition(),tileAction.getRoom(),false);
            QuestPositionUpdate secondPosition = null;
            if (tileAction instanceof ActionWithSecondPosition) {
                secondPosition = new QuestPositionUpdate(((ActionWithSecondPosition)tileAction).getSecondPosition(),((ActionWithSecondPosition)tileAction).getRoom(),false);
            }
            EventRouter.getInstance().postEvent(new RequestDisplayQuestTilePosition(firstPosition,secondPosition));
        });
        toAdd.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            if (selectedActionType.isNone()){ //this means that the user is not in the process of picking a position
                EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesUnselection(false));
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
     * @param toolbarActionToggleButton
     * @return
     */
    private Action addQuestAction(ToggleButton toolbarActionToggleButton, int index) {
        Random random = new Random();
        ActionType type = ActionType.valueOf(toolbarActionToggleButton.getId().toUpperCase());
        Action action = null;
        switch (type){
            case CAPTURE:
                action = new CaptureAction();
                break;
            case DAMAGE:
                action = new DamageAction();
                break;
            case DEFEND:
                action = new DefendAction();
                break;
            case ESCORT:
                action = new EscortAction();
                break;
            case EXCHANGE:
                action = new ExchangeAction();
                break;
            case EXPERIMENT:
                action = new ExperimentAction();
                break;
            case EXPLORE:
                action = new ExploreAction();
                break;
            case GATHER:
                action = new GatherAction();
                break;
            case GIVE:
                action = new GiveAction();
                break;
            case GO_TO:
                action = new GotoAction();
                break;
            case KILL:
                action = new KillAction();
                break;
            case LISTEN:
                action = new ListenAction();
                break;
            case READ:
                action = new ReadAction();
                break;
            case REPAIR:
                action = new RepairAction();
                break;
            case REPORT:
                action = new ReportAction();
                break;
            case SPY:
                action = new SpyAction();
                break;
            case STEALTH:
                action = new StealthAction();
                break;
            case TAKE:
                action = new TakeAction();
                break;
            case USE:
                action = new UseAction();
                break;
        }
        if (action != null){
            action.setId(UUID.randomUUID());
            action.setType(type);
            action.setPosition(updatedPosition.getPoint());
            action.setRoom(updatedPosition.getRoom());
            updatedPosition = null;
            if (secondUpdatedPosition != null){
                if (action instanceof ActionWithSecondPosition){
                    ((ActionWithSecondPosition)action).setSecondPosition(secondUpdatedPosition.getPoint());
                    ((ActionWithSecondPosition)action).setSecondRoom(secondUpdatedPosition.getRoom());
                    secondUpdatedPosition = null;
                }
            }
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

    @FXML
    private void backWorldView(ActionEvent event) throws IOException
    {
        EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesUnselection(false));
        DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.MOVEMENT);
        dungeon.dPane.setDisable(false);
        router.postEvent(new RequestWorldView());
    }


}
