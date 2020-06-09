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
import gui.controls.LabeledCanvas;
import gui.utils.DungeonDrawer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
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
import javafx.scene.text.TextAlignment;
import org.checkerframework.checker.units.qual.A;
import util.Point;
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

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class QuestViewController extends BorderPane implements Listener {
    private final EventRouter router = EventRouter.getInstance();
    private static final int GENERATOR_ATTEMPT_LIMIT = 100;
    private ApplicationConfig config;
    private boolean isActive = false;
    private Dungeon dungeon;
    private ActionType selectedActionType = ActionType.NONE;
    private QuestPositionUpdate updatedPosition = null;
    private QuestPositionUpdate secondUpdatedPosition = null;
    private boolean doublePosition = false;
    private boolean firstTime = true;

    private QuestGrammar questGrammar;
    //    private List<Quest> suggestedQuests;
    private List<Action> suggestedActions;
    private int globalQuestIndex;

    @FXML
    private StackPane mapPane;
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
    @FXML
    private Button clearQuestButton;
    LabeledCanvas canvas;


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
        router.registerListener(this, new QuestActionSuggestionUpdate());

        initQuestView();
        initActionToolbar();

    }

    private void initQuestView() {
        questPane.getChildren().stream()
                .filter(node -> node.getId().equals("questPlaceholder"))
                .forEach(node -> {
                    node.addEventHandler(MouseEvent.MOUSE_CLICKED,
                            event -> {
                                AtomicBoolean added = new AtomicBoolean(false);
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
                                                added.set(true);
                                            }
                                        });
                                generatorPane.getChildren().stream()
                                        .filter(action -> action instanceof ToggleButton)
                                        .filter(action -> ((ToggleButton) action).isSelected())
                                        .forEach(selected -> {
                                            ToggleButton toggleButton = (ToggleButton) selected;
                                            int questCount = dungeon.getQuest().getActions().size();
                                            if (!doublePosition && updatedPosition != null) {
                                                Action action = addQuestAction(toggleButton, questCount);
                                                addVisualQuestPaneAction(action, paneCount - 1);
                                                toggleButton.setSelected(false);
                                                added.set(true);
                                            }
                                        });
                                if (added.get()) {
                                    globalQuestIndex++;
                                    updateQuestAndIndexToGenerator();
                                    if (toggleHelp.isSelected()) {
                                        InformativePopupManager.getInstance().restartPopups();
                                        InformativePopupManager.getInstance().requestPopup(dungeon.dPane, PresentableInformation.ADDED_ACTION, "");
                                    }
                                    if (togglePath.isSelected()){
                                        calculateAndPaintQuestPath();
                                    }
                                }
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
                                updateQuestAndIndexToGenerator();
                                if (toggleHelp.isSelected()) {
                                    InformativePopupManager.getInstance().restartPopups();
                                    InformativePopupManager.getInstance().requestPopup(dungeon.dPane, PresentableInformation.DELETE_ACTION, "");
                                }
                                if (togglePath.isSelected()){
                                    calculateAndPaintQuestPath();
                                }
                            });
                        });
            }
        });

        clearQuestButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            dungeon.getQuest().clearAction();
            int end = questPane.getChildren().size() - 1;
            questPane.getChildren().remove(0, end);
            updateQuestAndIndexToGenerator();
            if (togglePath.isSelected()){
                calculateAndPaintQuestPath();
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
                            router.postEvent(new RequestDisplayQuestTilesSelection(types));
                            if (toggleHelp.isSelected()) {
                                InformativePopupManager.getInstance().restartPopups();
                                InformativePopupManager.getInstance()
                                        .requestPopup(dungeon.dPane, PresentableInformation.PLACE_ONE_POSITION, "");
                            }
                        } else {
                            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
                            router.postEvent(
                                    new RequestDisplayQuestTilesUnselection(false));
                            selectedActionType = ActionType.NONE;
                        }
                    });
                });
        togglePath.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            // find best path from hero to first action
            if (togglePath.isSelected()) {
                calculateAndPaintQuestPath();
            } else {
                unpaintPath();
            }
        });
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

    private void unpaintPath() {
        dungeon.getAllRooms().forEach(room -> {
            room.paintPath(false);
            room.clearPath();
        });

    }

    private void calculateAndPaintQuestPath() {
        unpaintPath();
        List<Action> actions = dungeon.getQuest().getActions();
        //      find best path and paint initial position to first
        if (!actions.isEmpty()){
            dungeon.calculateAndPaintBestPath(
                    dungeon.getInitialRoom(),
                    actions.get(0).getRoom(),
                    dungeon.getInitialPosition(),
                    new Point(actions.get(0).getPosition().getX(), actions.get(0).getPosition().getY()));
        }
        for (int i = 0; i < actions.size(); i++) {
            if (actions.get(i) instanceof ActionWithSecondPosition) {
                ActionWithSecondPosition action = (ActionWithSecondPosition) actions.get(i);
                //      then find best path from first to second position
                dungeon.calculateAndPaintBestPath(action.getRoom(), action.getSecondRoom(),
                        new Point(action.getPosition().getX(), action.getPosition().getY()),
                        new Point(action.getSecondPosition().getX(), action.getSecondPosition().getY()));
                //      find best path second position to next
                if (i + 1 < actions.size()) {
                    dungeon.calculateAndPaintBestPath(action.getSecondRoom(), actions.get(i + 1).getRoom(),
                            new Point(action.getSecondPosition().getX(), action.getSecondPosition().getY()),
                            new Point(actions.get(i + 1).getPosition().getX(), actions.get(i + 1).getPosition().getY()));
                }
            } else {
                if (i + 1 < actions.size()) {
                    //      find best path current to next
                    dungeon.calculateAndPaintBestPath(actions.get(i).getRoom(), actions.get(i + 1).getRoom(),
                            new Point(actions.get(i).getPosition().getX(), actions.get(i).getPosition().getY()),
                            new Point(actions.get(i + 1).getPosition().getX(), actions.get(i + 1).getPosition().getY()));
                }
            }
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
        mapPane.getChildren().add(dungeon.dPane);
    }

    private void initGeneratorPane() {
        canvas = new LabeledCanvas();
        questGrammar = new QuestGrammar(dungeon);
        suggestedActions = new ArrayList<Action>();

//        regenerateButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> router.postEvent(new ));

//        generateSuggestions();
        router.postEvent(new StartQuestGeneration(dungeon.getQuest(), questGrammar, dungeon.getAllRooms().size() * 5));
        globalQuestIndex = 0;
        updateIndexToGenerator();
    }

    private void reRenderGeneratedAction() {
        generatorPane.getChildren().clear();
        if (suggestedActions.isEmpty()) {
            Label title = new Label("Oops!");
            title.textFillProperty().setValue(Color.WHITE);
            title.textAlignmentProperty().setValue(TextAlignment.CENTER);
            title.setFont(new Font(24));
            Label label = new Label("The current quest\n" +
                    "is not compatible\n" +
                    "to the generator.\n\n" +
                    "Try changing your\n" +
                    "quest or hit \n" +
                    "re-generate to add\n" +
                    "a new quest @ the end \n" +
                    "of your current quest.\n\n" +
                    "Tip! Usually it's the\n" +
                    "most recent action that\n" +
                    "needs to be either\n" +
                    "GOTO or EXPLORE.");
            label.textFillProperty().setValue(Color.WHITE);
            label.textAlignmentProperty().setValue(TextAlignment.CENTER);
            label.wrapTextProperty().setValue(true);
            label.setFont(new Font(14));
            generatorPane.getChildren().add(title);
            generatorPane.getChildren().add(label);
        } else {
            for (int i = 0; i < suggestedActions.size(); i++) {
                addVisualGeneratorPaneAction(suggestedActions.get(i), i);
            }
            canvas.draw(null);
            generatorPane.getChildren().add(canvas);
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
            //refresh toolbarActionToggleButton
            final boolean[] IsAnyDisabled = {false};
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
            //check the validity of each action
            questPane.getChildren().filtered(node -> node instanceof ToggleButton).forEach(node -> {
                if (dungeon.getQuest().getAction(UUID.fromString(node.getId())).isPreconditionMet()){
                    node.getStyleClass().remove("danger");
                } else {
                    node.getStyleClass().add("danger");
                }
            });
            if (togglePath.isSelected()){
                calculateAndPaintQuestPath();
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
                            updateIndexToGenerator();
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
                            updateIndexToGenerator();
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
            router.postEvent(new RequestDisplayQuestTilesUnselection(doublePosition));
        } else if (e instanceof QuestPositionInvalid) {
            if (toggleHelp.isSelected()) {
                if (dungeon != null) {
                    InformativePopupManager.getInstance()
                            .requestPopup(dungeon.dPane, PresentableInformation.INVALID_QUEST_POSITION, "");
                }
            }
        } else if (e instanceof QuestActionSuggestionUpdate) {
            AtomicBoolean unique = new AtomicBoolean(true);
            List<Action> actions = ((QuestActionSuggestionUpdate) e).getActions();
            suggestedActions.forEach(action -> unique.set(actions.stream()
                    .noneMatch(action1 ->
                            action.getType().getValue() == action1.getType().getValue())));

            unique.set((unique.get() || suggestedActions.size() != actions.size()));

            if (unique.get()) {
                suggestedActions.clear();
                suggestedActions.addAll(actions);
                System.out.println("SuggestedActions to render: " + suggestedActions.size());

                Platform.runLater(this::reRenderGeneratedAction);
            }
//            reRenderGeneratedAction();
//            AtomicBoolean added = new AtomicBoolean(false);
//            ((QuestSuggestionUpdate) e).getQuests().forEach(quest -> {
//                if (suggestedQuests.isEmpty()){
//                    added.set(suggestedQuests.add(quest));
//                } else {
//                    boolean noneMatch = suggestedQuests.stream().anyMatch(quest::notEquals);
//                    if (noneMatch){
//                        added.set(suggestedQuests.add(quest));
//                    }
//                }
//            });
//            if (added.get()) {
//                System.out.println("Quest suggestion added");
//                Platform.runLater(this::reRenderGeneratedAction);
//            }
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
                    if (toggleHelp.isSelected()) {
                        InformativePopupManager.getInstance().requestPopup(dungeon.dPane, PresentableInformation.DELETE_REPLACE_ACTION, "");
                    }
                    Action tileAction = dungeon.getQuest().getAction(UUID.fromString(toAdd.getId()));
                    globalQuestIndex = dungeon.getQuest().indexOf(tileAction);
                    updateIndexToGenerator();
                    if (!doublePosition && updatedPosition != null) {
                        tbQuestTools.getItems().stream()
                                .filter(a -> ((ToggleButton) a).isSelected())
                                .forEach(s -> {
                                    Platform.runLater(() -> replaceQuestAction(toAdd, (ToggleButton) s));
                                    globalQuestIndex = dungeon.getQuest().getActions().size();
                                    QuestGenerationConfigUpdate configUpdate = new QuestGenerationConfigUpdate();
                                    configUpdate.setPayload(globalQuestIndex);
                                    router.postEvent(configUpdate);
                                    configUpdate = new QuestGenerationConfigUpdate();
                                    configUpdate.setPayload(dungeon.getQuest());
                                    router.postEvent(configUpdate);
                                });
                        generatorPane.getChildren().stream()
                                .filter(node -> node instanceof ToggleButton)
                                .filter(a -> ((ToggleButton) a).isSelected())
                                .forEach(s -> {
                                    Platform.runLater(() -> replaceQuestAction(toAdd, (ToggleButton) s));
                                    globalQuestIndex = dungeon.getQuest().getActions().size();
                                    QuestGenerationConfigUpdate configUpdate = new QuestGenerationConfigUpdate();
                                    configUpdate.setPayload(globalQuestIndex);
                                    router.postEvent(configUpdate);
                                    configUpdate = new QuestGenerationConfigUpdate();
                                    configUpdate.setPayload(dungeon.getQuest());
                                    router.postEvent(configUpdate);
                                });
                    }
                } else {
                    globalQuestIndex = dungeon.getQuest().getActions().size();
                    updateIndexToGenerator();
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
            router.postEvent(new RequestDisplayQuestTilePosition(firstPosition, secondPosition));
        });
        toAdd.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            if (selectedActionType.isNone()) { //this means that the user is not in the process of picking a position
                router.postEvent(new RequestDisplayQuestTilesUnselection(false));
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

    public void addVisualGeneratorPaneAction(Action action, int paneIndex) {
        //add toggle button representation
        ToggleButton toAdd = new ToggleButton();
        toAdd.setText(action.getName());
        toAdd.setId(action.getId().toString());
        toAdd.setTooltip(new Tooltip(action.getName()));
        toAdd.setToggleGroup(questActionsTools);
        toAdd.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {

            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
            router.postEvent(new RequestDisplayQuestTilesUnselection(false));

            Action tileAction = suggestedActions.get(paneIndex); // TODO: keep an eye on this so it's right
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
                            updateIndexToGenerator();
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
            Action tileAction = suggestedActions.get(paneIndex); // TODO: keep an eye on this so it's right
            QuestPositionUpdate firstPosition = new QuestPositionUpdate(
                    tileAction.getPosition(), tileAction.getRoom(), false);
            QuestPositionUpdate secondPosition = null;
            if (tileAction instanceof ActionWithSecondPosition) {
                secondPosition = new QuestPositionUpdate(
                        ((ActionWithSecondPosition) tileAction).getSecondPosition(), tileAction.getRoom(), false);
            }
            router.postEvent(new RequestDisplayQuestTilePosition(firstPosition, secondPosition));
        });
        toAdd.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            if (selectedActionType.isNone()) { //this means that the user is not in the process of picking a position
                router.postEvent(new RequestDisplayQuestTilesUnselection(false));
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

        if (toggleHelp.isSelected()) {
            InformativePopupManager.getInstance().restartPopups();
            InformativePopupManager.getInstance().requestPopup(dungeon.dPane, PresentableInformation.REPLACE_ACTION, "");
        }
        if (togglePath.isSelected()){
            calculateAndPaintQuestPath();
        }
    }

    private void updateQuestAndIndexToGenerator() {
        updateIndexToGenerator();
        QuestGenerationConfigUpdate questGenerationConfigUpdate = new QuestGenerationConfigUpdate();
        questGenerationConfigUpdate.setPayload(dungeon.getQuest());
        router.postEvent(questGenerationConfigUpdate);
    }

    private void updateIndexToGenerator() {
        QuestGenerationConfigUpdate questGenerationConfigUpdate = new QuestGenerationConfigUpdate();
        questGenerationConfigUpdate.setPayload(globalQuestIndex);
        router.postEvent(questGenerationConfigUpdate);
    }

    @FXML
    private void backWorldView(ActionEvent event) throws IOException {
        unpaintPath();
        router.postEvent(new StopQuestGeneration());
        router.postEvent(new RequestDisplayQuestTilesUnselection(false));
        DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.MOVEMENT);
        dungeon.dPane.setDisable(false);
        router.postEvent(new RequestWorldView());
    }


}
