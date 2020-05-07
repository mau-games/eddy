package gui.views;

import game.ApplicationConfig;
import game.Dungeon;
import game.TileTypes;
import game.quest.Action;
import game.quest.ActionType;
import game.quest.ActionWithSecondPosition;
import game.quest.Quest;
import game.quest.actions.*;
import generator.algorithm.grammar.QuestGrammar;
import gui.utils.DungeonDrawer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import gui.utils.InformativePopupManager;
import gui.utils.InformativePopupManager.PresentableInformation;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
    private boolean firstTime = true;

    private QuestGrammar questGrammar;
    private List<Quest> suggestedQuests;
    private int globalQuestIndex;

    @FXML
    private ScrollPane mapScrollPane;
    @FXML
    private BorderPane buttonPane;
    @FXML
    private FlowPane questPane;
    @FXML
    private FlowPane generatorPane;
    @FXML
    private ToolBar tbQuestTools;
    @FXML
    private ToggleGroup questActions;
    @FXML
    private ToggleGroup questActionsTools;
    @FXML
    private ToggleGroup generatorActions;
    @FXML
    private CheckBox togglePath;
    @FXML
    private CheckBox toggleHelp;
    @FXML
    private Button regenerateButton;

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
        router.registerListener(this, new QuestPositionUpdate(null, null, false));
        router.registerListener(this, new QuestPositionInvalid());

        initQuestView();
        initActionToolbar();

    }

    private void initQuestView() {
        questPane.getChildren().stream()
                .filter(node -> node.getId().equals("questPlaceholder"))
                .forEach(node -> {
                    node.addEventHandler(MouseEvent.MOUSE_CLICKED,
                            event -> {
                                int paneCount = questPane.getChildren().size();
                                tbQuestTools.getItems().stream()
                                        .filter(action -> ((ToggleButton) action).isSelected())
                                        .forEach(selected -> {
                                            ToggleButton toggleButton = (ToggleButton) selected;
                                            int questCount = dungeon.getQuest().getActions().size();
                                            if (!doublePosition && updatedPosition != null) {
                                                Action action = addQuestAction(toggleButton, questCount);
                                                addVisualQuestPaneAction(action, paneCount - 1);
                                                toggleButton.setSelected(false);
                                                globalQuestIndex++;
                                                reRenderGeneratedAction();
                                            }
                                        });
                                generatorPane.getChildren().stream()
                                        .filter(action -> ((ToggleButton) action).isSelected())
                                        .forEach(selected -> Platform.runLater(() -> {
                                            ToggleButton toggleButton = (ToggleButton) selected;
                                            int questCount = dungeon.getQuest().getActions().size();
                                            if (!doublePosition && updatedPosition != null) {
                                                Action action = addQuestAction(toggleButton, questCount);
                                                addVisualQuestPaneAction(action, paneCount - 1);
                                                toggleButton.setSelected(false);
                                                globalQuestIndex++;
                                                reRenderGeneratedAction();
                                            }
                                        }));

                            });
                });

        this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.DELETE)) {
                questPane.getChildren().stream()
                        .filter(questAction -> questAction instanceof ToggleButton)
                        .filter(questAction -> ((ToggleButton) questAction).isSelected())
                        .forEach(questAction -> {
                            Platform.runLater(() -> {
                                removeQuestAction((ToggleButton) questAction);
                                globalQuestIndex = dungeon.getQuest().getActions().size();
                                reRenderGeneratedAction();
                            });
                        });
            }
        });
    }

    private void initActionToolbar() {
        tbQuestTools.getItems()
                .forEach(toolbarAction -> {
                    toolbarAction.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                        if (((ToggleButton) toolbarAction).isSelected()) {
                            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.QUEST_POS);
                            selectedActionType = ActionType.valueOf(((ToggleButton) toolbarAction).getId());
                            List<TileTypes> types = findTileTypeByAction();
                            EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesSelection(types));
                            if (toggleHelp.isSelected()) {
                                InformativePopupManager.getInstance().restartPopups();
                                InformativePopupManager.getInstance()
                                        .requestPopup(dungeon.dPane, PresentableInformation.PLACE_ONE_POSITION, "");
                            }
                        } else {
                            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
                            EventRouter.getInstance().postEvent(
                                    new RequestDisplayQuestTilesUnselection(false));
                            selectedActionType = ActionType.NONE;
                        }
                    });
                });
//        togglePath.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
        //TODO ELIN:show quest path
        //find best path from hero to first action
        //loop through all actions {
        // if action with second position
        // then find best path from first to second position
        // find best path second position to next
        // else
        // find best path current to next
        // }
        //     });
        if (toggleHelp != null) {
            toggleHelp.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                InformativePopupManager.getInstance().restartPopups();
                if (toggleHelp.isSelected()) {
                    Platform.runLater(() -> InformativePopupManager.getInstance().requestPopup(dungeon.dPane, PresentableInformation.HELP_MODE, "Help mode is activated!"));
                } else {
                    Platform.runLater(() -> InformativePopupManager.getInstance().requestPopup(dungeon.dPane, PresentableInformation.NO_HELP_MODE, "OK no help!"));
                }
            });
        }
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

    private void initGeneratorPane() {
        questGrammar = new QuestGrammar(dungeon);
        suggestedQuests = new ArrayList<Quest>();
        globalQuestIndex = 0;

        regenerateButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> generateSuggestions());

        generateSuggestions();
    }

    private void generateSuggestions() {
        //remove all old suggestions
        generatorPane.getChildren().clear();
        suggestedQuests.clear();

        //start generating new suggestions
        while (suggestedQuests.isEmpty()) {
            int limit = dungeon.getAllRooms().size() * 5;
            for (int i = 0; i < QuestGrammar.Motives.length; i++) {
                Quest quest = new Quest();
                while (quest.getActions().isEmpty()) {
                    questGrammar.expand(quest, QuestGrammar.START_VALUE, dungeon.getQuest().getAvailableActions(), 0, limit);
                    quest.getActions().forEach(action -> System.out.print(action.getType() + "-"));
                }
                suggestedQuests.add(quest);
                System.out.println();
            }
            //TODO: filter the quests to match current
            suggestedQuests = suggestedQuests.stream().filter(quest -> quest.startsWith(dungeon.getQuest())).collect(Collectors.toList());
            // else pre-add the current and generate new
        }

        for (int i = 0; i < suggestedQuests.size(); i++) {
            int index = i;
            if (globalQuestIndex < suggestedQuests.get(i).getActions().size()) {
                //merge duplicates
                if (generatorPane.getChildren()
                        .stream()
                        .noneMatch(node ->
                                ((ToggleButton) node)
                                        .getText()
                                        .equals(suggestedQuests
                                                .get(index)
                                                .getAction(globalQuestIndex)
                                                .getName()))) {
                    //add generated suggestions buttons
                    addVisualGeneratorPaneAction(suggestedQuests.get(index).getAction(globalQuestIndex), generatorPane.getChildren().size(), index);
                }
            }
        }
    }

    private void reRenderGeneratedAction() {
        generatorPane.getChildren().clear();
        for (int i = 0; i < suggestedQuests.size(); i++) {
            int index = i;
            if (globalQuestIndex < suggestedQuests.get(i).getActions().size()) {
                //merge duplicates
                if (generatorPane.getChildren()
                        .stream()
                        .noneMatch(node ->
                                ((ToggleButton) node)
                                        .getText()
                                        .equals(suggestedQuests
                                                .get(index)
                                                .getAction(globalQuestIndex)
                                                .getName()))) {
                    //add generated suggestions buttons
                    addVisualGeneratorPaneAction(suggestedQuests.get(index).getAction(globalQuestIndex), generatorPane.getChildren().size(), index);
                }
            }
        }
    }

    @Override
    public void ping(PCGEvent e) {
        if (e instanceof RequestQuestView) {
            if (firstTime & toggleHelp.isSelected()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Welcome to the quest builder!\n"
                        + "To the left you have the available actions\n"
                        + "To the right you the autogenerated actions!\n"
                        + "The map shows your dungeon\n"
                        + "The plus button is where you add your actions!",
                        ButtonType.OK);
                alert.setTitle("Quest editor");
                Image image = new Image("graphics/mesopatterns/ambush.png");
                ImageView imageview = new ImageView(image);
                imageview.setFitWidth(100);
                imageview.setFitHeight(100);
                alert.setGraphic(imageview);
                alert.setHeaderText("");
                alert.showAndWait();
            }
            firstTime = false;
            //disable current dungeon brush so accidents wont happen :)
            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
            final boolean[] IsAnyDisabled = {false};
            //refresh toolbarActionToggleButton
            tbQuestTools.getItems().forEach(node -> {
                String buttonID = ((ToggleButton) node).getTooltip().getText();
                boolean disable = dungeon.getQuest().getAvailableActions().stream()
                        .noneMatch(actionType -> actionType.toString().equals(buttonID));
                node.setDisable(disable);
                if (disable) {
                    IsAnyDisabled[0] = true;
                }
            });
            if (IsAnyDisabled[0] && toggleHelp.isSelected()) {
                InformativePopupManager.getInstance()
                        .requestPopup(dungeon.dPane, PresentableInformation.ACTION_NOT_AVAILABLE, "");
            }
            initGeneratorPane();
        } else if (e instanceof QuestPositionUpdate) {
            doublePosition = ((QuestPositionUpdate) e).isSecondPosition();
            AtomicBoolean replace = new AtomicBoolean(false);
            if (doublePosition) {
                secondUpdatedPosition = (QuestPositionUpdate) e;
                questPane.getChildren()
                        .filtered(questAction -> questAction instanceof ToggleButton)
                        .filtered(questAction -> ((ToggleButton) questAction).isSelected())
                        .forEach(questAction -> {
                            replace.set(true);
                            Platform.runLater(() -> replaceQuestAction((ToggleButton) questAction, (ToggleButton) questActionsTools.getSelectedToggle()));
                            globalQuestIndex = dungeon.getQuest().getActions().size();
                            reRenderGeneratedAction();
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
                        .filtered(questAction -> ((ToggleButton) questAction).isSelected())
                        .forEach(questAction -> {
                            replace.set(true);
                            Platform.runLater(() -> replaceQuestAction((ToggleButton) questAction, (ToggleButton) questActionsTools.getSelectedToggle()));
                            globalQuestIndex = dungeon.getQuest().getActions().size();
                            reRenderGeneratedAction();
                        });
            } else {
                if (toggleHelp.isSelected()) {
                    InformativePopupManager.getInstance().requestPopup(dungeon.dPane, PresentableInformation.PLACE_TWO_POSITIONS, "");
                }
            }
            if (!replace.get() && toggleHelp.isSelected()) {
                InformativePopupManager.getInstance().requestPopup(
                        dungeon.dPane, PresentableInformation.ADD_ACTION,
                        "The action can be added!\n " +
                                "Click the plus button below");
            }
            EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesUnselection(doublePosition));
        } else if (e instanceof QuestPositionInvalid) {
            if (toggleHelp.isSelected()) {
                if (dungeon != null) {
                    InformativePopupManager.getInstance()
                            .requestPopup(dungeon.dPane, PresentableInformation.INVALID_QUEST_POSITION, "");
                }
            }
        }
    }

    /**
     * Set this Pane as active
     *
     * @param active
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }

    public void addVisualQuestPaneAction(Action action, int index) {
        //add toggle button representation
        ToggleButton toAdd = new ToggleButton();
        toAdd.setText(action.getName());
        toAdd.setId(action.getId().toString());
        toAdd.setToggleGroup(questActions);
        toAdd.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton().equals(MouseButton.PRIMARY)) {
                if (toAdd.isSelected()) {
                    Action tileAction = dungeon.getQuest().getAction(UUID.fromString(toAdd.getId()));
                    globalQuestIndex = dungeon.getQuest().indexOf(tileAction);
                    reRenderGeneratedAction();
                    if (!doublePosition && updatedPosition != null) {
                        tbQuestTools.getItems().stream()
                                .filter(a -> ((ToggleButton) a).isSelected())
                                .forEach(s -> {
                                    Platform.runLater(() -> replaceQuestAction(toAdd, (ToggleButton) s));
                                    globalQuestIndex = dungeon.getQuest().getActions().size();
                                    reRenderGeneratedAction();
                                });
                        generatorPane.getChildren().stream()
                                .filter(a -> ((ToggleButton) a).isSelected())
                                .forEach(s -> {
                                    Platform.runLater(() -> replaceQuestAction(toAdd, (ToggleButton) s));
                                    globalQuestIndex = dungeon.getQuest().getActions().size();
                                    reRenderGeneratedAction();
                                });
                    }
                } else {
                    globalQuestIndex = dungeon.getQuest().getActions().size();
                    reRenderGeneratedAction();
                }
            } else if (e.getButton().equals(MouseButton.SECONDARY)) {
                //for future use if a pop-up menu is needed.
            }
        });
        toAdd.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            Action tileAction = dungeon.getQuest().getAction(UUID.fromString(toAdd.getId()));
            QuestPositionUpdate firstPosition = new QuestPositionUpdate(
                    tileAction.getPosition(), tileAction.getRoom(), false);
            QuestPositionUpdate secondPosition = null;
            if (tileAction instanceof ActionWithSecondPosition) {
                secondPosition = new QuestPositionUpdate(
                        ((ActionWithSecondPosition) tileAction).getSecondPosition(), tileAction.getRoom(), false);
            }
            EventRouter.getInstance().postEvent(new RequestDisplayQuestTilePosition(firstPosition, secondPosition));
        });
        toAdd.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            if (selectedActionType.isNone()) { //this means that the user is not in the process of picking a position
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

    public void addVisualGeneratorPaneAction(Action action, int paneIndex, int questIndex) {
        //add toggle button representation
        ToggleButton toAdd = new ToggleButton();
        toAdd.setText(action.getName());
        toAdd.setId(action.getId().toString());
        toAdd.setTooltip(new Tooltip(action.getName()));
        toAdd.setToggleGroup(questActionsTools);
        toAdd.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {

            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
            EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesUnselection(false));

            Action tileAction = suggestedQuests.get(questIndex).getAction(UUID.fromString(toAdd.getId()));
            updatedPosition = new QuestPositionUpdate(tileAction.getPosition(), tileAction.getRoom(), false);
            if (tileAction instanceof ActionWithSecondPosition) {
                secondUpdatedPosition = new QuestPositionUpdate(
                        ((ActionWithSecondPosition) tileAction).getSecondPosition(), tileAction.getRoom(), false);
            }
            AtomicBoolean replace = new AtomicBoolean(false);
            if (!doublePosition && updatedPosition != null) {
                questPane.getChildren()
                        .filtered(questAction -> questAction instanceof ToggleButton)
                        .filtered(a -> ((ToggleButton) a).isSelected())
                        .forEach(s -> {
                            replace.set(true);
                            Platform.runLater(() -> replaceQuestAction((ToggleButton) s, toAdd));
                            globalQuestIndex = dungeon.getQuest().getActions().size();
                            reRenderGeneratedAction();
                        });
            }
            if (!replace.get() && toggleHelp.isSelected()) {
                InformativePopupManager.getInstance().
                        requestPopup(dungeon.dPane, PresentableInformation.ADD_ACTION,
                                "The action can be added!\n " +
                                        "Click the plus button below");
            }

        });
        toAdd.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            Action tileAction = suggestedQuests.get(questIndex).getAction(UUID.fromString(toAdd.getId()));
            QuestPositionUpdate firstPosition = new QuestPositionUpdate(
                    tileAction.getPosition(), tileAction.getRoom(), false);
            QuestPositionUpdate secondPosition = null;
            if (tileAction instanceof ActionWithSecondPosition) {
                secondPosition = new QuestPositionUpdate(
                        ((ActionWithSecondPosition) tileAction).getSecondPosition(), tileAction.getRoom(), false);
            }
            EventRouter.getInstance().postEvent(new RequestDisplayQuestTilePosition(firstPosition, secondPosition));
        });
        toAdd.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            if (selectedActionType.isNone()) { //this means that the user is not in the process of picking a position
                EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesUnselection(false));
            }
        });
        generatorPane.getChildren().add(paneIndex, toAdd);
    }

    /**
     * adds action to quest and gives it random available position  for now.
     *
     * @param toolbarActionToggleButton
     * @return
     */
    private Action addQuestAction(ToggleButton toolbarActionToggleButton, int index) {
        Random random = new Random();
        ActionType type = ActionType.valueOf(toolbarActionToggleButton.getTooltip().getText().toUpperCase());
        Action action = null;
        switch (type) {
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
        if (action != null) {
            action.setId(UUID.randomUUID());
            action.setType(type);
            action.setPosition(updatedPosition.getPoint());
            action.setRoom(updatedPosition.getRoom());
            updatedPosition = null;
            if (action instanceof ActionWithSecondPosition) {
                if (secondUpdatedPosition != null) {
                    ((ActionWithSecondPosition) action).setSecondPosition(secondUpdatedPosition.getPoint());
                    ((ActionWithSecondPosition) action).setSecondRoom(secondUpdatedPosition.getRoom());
                    secondUpdatedPosition = null;
                }
            }
        }
        dungeon.getQuest().addActionsAt(index, action);

        return action;
    }

    private void removeQuestAction(ToggleButton questActionToggleButton) {
        //remove the QuestAction from Quest
        dungeon.getQuest().removeAction(dungeon.getQuest().getAction(UUID.fromString(questActionToggleButton.getId())));
        //remove the arrow label after QuestActionToggleButton
        questPane.getChildren().remove(questPane.getChildren().indexOf(questActionToggleButton) + 1);
        //remove the QuestActionToggleButton
        questPane.getChildren().remove(questActionToggleButton);
    }

    private void replaceQuestAction(ToggleButton questActionToggleButton, ToggleButton toolbarActionToggleButton) {
        int questIndex = dungeon.getQuest().indexOf(dungeon.getQuest()
                .getAction(UUID.fromString(questActionToggleButton.getId())));
        int paneIndex = questPane.getChildren().indexOf(questActionToggleButton);

        removeQuestAction(questActionToggleButton);
        Action action = addQuestAction(toolbarActionToggleButton, questIndex);
        addVisualQuestPaneAction(action, paneIndex);

        toolbarActionToggleButton.setSelected(false);
        questActionToggleButton.setSelected(false);
    }

    @FXML
    private void backWorldView(ActionEvent event) throws IOException {
        EventRouter.getInstance().postEvent(new RequestDisplayQuestTilesUnselection(false));
        DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.MOVEMENT);
        dungeon.dPane.setDisable(false);
        router.postEvent(new RequestWorldView());
    }


}
