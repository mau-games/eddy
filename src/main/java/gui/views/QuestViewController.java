package gui.views;

import game.ApplicationConfig;
import game.Dungeon;
import game.DungeonPane;
import game.Room;
import game.Tile;
import game.TileTypes;
import game.quest.Action;
import game.quest.ActionType;
import game.quest.ActionWithSecondPosition;
import game.quest.Quest;
import game.quest.actions.*;
import game.tiles.CivilianTile;
import game.tiles.EnemyTile;
import game.tiles.ItemTile;
import game.tiles.SoldierTile;
import generator.algorithm.grammar.QuestGrammar;
import generator.algorithm.grammar.QuestGrammar.QuestMotives;
import gui.controls.LabeledCanvas;
import gui.utils.DungeonDrawer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
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
import java.math.RoundingMode;
import java.text.DecimalFormat;
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
    float[] motiveArray;
    
    private Tile tempTile;
    private Action tempAction;
    
    private Stack<TileTypes> stackNpc;
    private Stack<finder.geometry.Point> npcPosition;
    private Stack<TileTypes> stackCivilian;
    private Stack<finder.geometry.Point> civilianPosition;
    private Stack<Room> roomsNpc;
    private Stack<Room> roomsCivilian;

    private QuestGrammar questGrammar;
    //    private List<Quest> suggestedQuests;
    private List<Action> suggestedActions;
    private int globalQuestIndex;

    @FXML
    private StackPane mapPane;
    @FXML
    private BorderPane buttonPane;
    @FXML
    private VBox questPaneV;
    @FXML
    private HBox questPaneH;
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
    @FXML
    private Button questPlaceholder;
    @FXML
    private FlowPane motivePane;
    @FXML
    private Label knowledgeText;
    @FXML
    private Label comfortText;
    @FXML
    private Label reputationText;
    @FXML
    private Label serenityText;
    @FXML
    private Label protectionText;
    @FXML
    private Label conquestText;
    @FXML
    private Label wealthText;
    @FXML
    private Label abilityText;
    @FXML
    private Label equipmentText;
    
    
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
        
        stackNpc = new Stack<TileTypes>();
        npcPosition = new Stack<finder.geometry.Point>();
        stackCivilian = new Stack<TileTypes>();
        civilianPosition = new Stack<finder.geometry.Point>();
        roomsNpc = new Stack<Room>();
        roomsCivilian = new Stack<Room>();

    }
    //trycker på plus + ändring av JA
    private void initQuestView() {
        questPaneH.getChildren().stream().filter(node -> node.getId().equals("questPlaceholder")).forEach(node -> {
                    node.addEventHandler(MouseEvent.MOUSE_CLICKED,
                            event -> {
                                AtomicBoolean added = new AtomicBoolean(false);
                                int paneCount = questPaneH.getChildren().size();
                                tbQuestTools.getItems().stream()
                                        .filter(action -> ((ToggleButton) action).isSelected())
                                        .forEach(selected -> {
                                            ToggleButton toggleButton = (ToggleButton) selected;
                                            int questCount = dungeon.getQuest().getActions().size();
                                            
                                            if (!doublePosition && updatedPosition != null) {
                                                Action action = addQuestAction(toggleButton, questCount);Tile tile = action.getRoom().getTile(action.getPosition().getX(), action.getPosition().getY());
                                                CheckUsedTile(tile, action);
                                                tempTile = tile;
                                                tempAction = action;
                                                StackNpc(tile.GetType(), action);
                                                addVisualQuestPaneAction(action, paneCount - 1);
                                                toggleButton.setSelected(false);
                                                questGrammar.setStacks(stackNpc, stackCivilian, npcPosition, civilianPosition, roomsNpc, roomsCivilian, dungeon.getQuest());
                                                dungeon.getQuest().checkForAvailableActions(action, tile.GetType(), stackNpc,dungeon);
                                                RefreshPanel();
                                                GetMotiveBalance();
                                                UpdateMotives();
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
                                                Tile tile = action.getRoom().getTile(action.getPosition().getX(), action.getPosition().getY());
                                                CheckUsedTile(tile, action);
                                                tempTile = tile;
                                                tempAction = action;
                                                StackNpc(tile.GetType(), action);
                                                addVisualQuestPaneAction(action, paneCount - 1);
                                                toggleButton.setSelected(false);
                                                dungeon.getQuest().checkForAvailableActions(action, tile.GetType(), stackNpc, dungeon);
                                                questGrammar.setStacks(stackNpc, stackCivilian, npcPosition, civilianPosition, roomsNpc, roomsCivilian, dungeon.getQuest());
                                                RefreshPanel();
                                                GetMotiveBalance();
                                                UpdateMotives();
                                                added.set(true);
                                            }
                                        });
                                if (added.get()) {
                                    globalQuestIndex++;
                                    updateQuestAndIndexToGenerator();
                                    if (toggleHelp.isSelected()) {
                                        //InformativePopupManager.getInstance().restartPopups();
                                        //InformativePopupManager.getInstance().requestPopup(dungeon.dPane, PresentableInformation.ADDED_ACTION, "");
                                    }
                                    if (togglePath.isSelected()){
                                        calculateAndPaintQuestPath();
                                    }
                                }
                            });
                });

        this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode().equals(KeyCode.DELETE)) {
                questPaneH.getChildren().stream()
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
            int end = questPaneH.getChildren().size() - 1;
            questPaneH.getChildren().remove(0, end);
            updateQuestAndIndexToGenerator();
            if (togglePath.isSelected()){
                calculateAndPaintQuestPath();
            }
        });
    }
    //trycker på panelen till vänster
    private void initActionToolbar() {
        tbQuestTools.getItems()
                .forEach(toolbarAction -> {
                    toolbarAction.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                        if (((ToggleButton) toolbarAction).isSelected()) {
                            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.QUEST_POS);
                            selectedActionType = ActionType.valueOf(((ToggleButton) toolbarAction).getId());
                            List<TileTypes> types;
                            if (dungeon.getQuest().toIntArray().length != 0) {
                            	types = findTileTypeByAction(dungeon.getQuest().checkIfActionIsCivilian());
							}
                            else {
                            	types = findTileTypeByAction();
							}
                            
                            if (stackCivilian.size() != 0) {
                            	if (dungeon.getQuest().checkIfActionIsCivilian() && selectedActionType != ActionType.REPORT) {
                                	router.postEvent(new RequestDisplayQuestTilesSelection2(types,civilianPosition.peek()));
								}
                            	else {
                            		router.postEvent(new RequestDisplayQuestTilesSelection2(types,npcPosition.peek()));
								}
							}
                            else if (stackNpc.size() != 0 && (selectedActionType == ActionType.LISTEN || selectedActionType == ActionType.REPORT)) {
								router.postEvent(new RequestDisplayQuestTilesSelection2(types,npcPosition.peek()));
							}
                            else {
                                router.postEvent(new RequestDisplayQuestTilesSelection(types));
							}
							//}
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

    private List<TileTypes> findTileTypeByAction(boolean temp) {
        List<TileTypes> typesList = new LinkedList<TileTypes>();
        switch (selectedActionType) {
            case ESCORT:
                typesList.add(TileTypes.CIVILIAN);
                break;
            case GATHER:
            	typesList.add(TileTypes.TREASURE);
            	typesList.add(TileTypes.ITEM);
            	break;
            case STEAL:
            	typesList.add(TileTypes.TREASURE);
            case EXPERIMENT:
            case READ:
            case REPAIR:
            case USE:
                typesList.add(TileTypes.ITEM);
                break;
            case LISTEN:
            	if (temp) {
                	typesList.add(TileTypes.CIVILIAN);
				}
            	typesList.add(TileTypes.SOLDIER);
            	typesList.add(TileTypes.MAGE);
            	typesList.add(TileTypes.BOUNTYHUNTER);
            	typesList.add(TileTypes.CIVILIAN);
            	break;
            case REPORT:
            	if (stackNpc.size() != 0) {
					typesList.add(stackNpc.peek());
				}
            	break;
            case KILL:
            case CAPTURE:
            case SPY:
                typesList.add(TileTypes.ENEMY);
                typesList.add(TileTypes.ENEMY_BOSS);
                break;
            case DAMAGE:
                typesList.add(TileTypes.ENEMY);
                typesList.add(TileTypes.ENEMY_BOSS);
                typesList.add(TileTypes.ITEM);
                break;
            case DEFEND:
                typesList.add(TileTypes.CIVILIAN);
                break;
        }
        return typesList;
    }
    
    private List<TileTypes> findTileTypeByAction() {
        List<TileTypes> typesList = new LinkedList<TileTypes>();
        switch (selectedActionType) {
            case LISTEN:
            	typesList.add(TileTypes.SOLDIER);
            	typesList.add(TileTypes.MAGE);
            	typesList.add(TileTypes.BOUNTYHUNTER);
            	typesList.add(TileTypes.CIVILIAN);
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
                        + "The plus button is where you add your actions!\n"
                        + "If you run out of available actions \n"
                        + "return to the room view and add objects",
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
            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
            if (tempTile != null) {
            	dungeon.getQuest().checkForAvailableActions(tempAction, tempTile.GetType(), stackNpc, dungeon);
			}
            RefreshPanel();
            //check the validity of each action
            questPaneH.getChildren().filtered(node -> node instanceof ToggleButton).forEach(node -> {
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
                questPaneH.getChildren()
                        .filtered(questAction -> questAction instanceof ToggleButton)
                        .filtered(questAction -> ((ToggleButton) questAction).isSelected())
                        .forEach(questAction -> {
                            replace.set(true);
                            Platform.runLater(() -> replaceQuestAction((ToggleButton) questAction, (ToggleButton) questActionsTools.getSelectedToggle()));
                            globalQuestIndex = dungeon.getQuest().getActions().size();
                            updateIndexToGenerator();
                        });
                doublePosition = false;
            } else {
                updatedPosition = (QuestPositionUpdate) e;
                doublePosition = (selectedActionType.isExchange() || selectedActionType.isGive() || selectedActionType.isTake());
                selectedActionType = ActionType.NONE;
            }
            if (!doublePosition) {
                DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
                questPaneH.getChildren()
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
            if (actions.size() != 0) {
            	if (actions.get(0).getType() == ActionType.REPORT) {
                	suggestedActions.forEach(action -> unique.set(actions.stream()
                            .noneMatch(action1 ->
                                    action.getPosition() == action1.getPosition())));
    			}
			}
            
            unique.set((unique.get() || suggestedActions.size() != actions.size()));

            if (unique.get()) {
                suggestedActions.clear();
                suggestedActions.addAll(actions);
                System.out.println("SuggestedActions to render: " + suggestedActions.size());

                Platform.runLater(this::reRenderGeneratedAction);
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

        questPaneH.getChildren().add(index, toAdd);

        //add arrow label
        if (action.getType() == ActionType.REPORT && stackNpc.size() == 0) {
        	questPaneH = new HBox(2);
            questPaneH.setPadding(new Insets(10, 10, 10, 10));

            questPaneH.getChildren().add(questPlaceholder);
        	questPaneV.getChildren().add(questPaneH);
		}
        else {
            Label arrow = new Label("=>");
            arrow.setTextFill(Color.WHITE);
            arrow.setFont(Font.font(14.0));
            arrow.setStyle("-fx-background-color: transparent;");
            questPaneH.getChildren().add(index + 1, arrow);
		}
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
                questPaneH.getChildren()
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
            case STEAL:
            	action = new StealAction();
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
        questPaneH.getChildren().remove(questPaneH.getChildren().indexOf(questActionToggleButton) + 1);
        //remove the QuestActionToggleButton
        questPaneH.getChildren().remove(questActionToggleButton);
    }

    private void replaceQuestAction(ToggleButton questActionToggleButton, ToggleButton toolbarActionToggleButton) {
        int questIndex = dungeon.getQuest().indexOf(dungeon.getQuest()
                .getAction(UUID.fromString(questActionToggleButton.getId())));
        int paneIndex = questPaneH.getChildren().indexOf(questActionToggleButton);

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
    private void RefreshPanel()
    {
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
            //InformativePopupManager.getInstance()
                    //.requestPopup(dungeon.dPane, PresentableInformation.ACTION_NOT_AVAILABLE, "");
        }
    }
    
    private void UpdateMotives()
    {
    	String[] tempText = new String[9];
    	
    	float[] tempFloat = new float[9];
    	for (int i = 0; i < tempFloat.length; i++) {
			tempFloat[i] = (float)Math.round(motiveArray[i] * 10f) /10f;
		}
    	
    	tempText[0] = "Knowledge = " + Float.toString(tempFloat[0]) + "/18.3%";
    	tempText[1] = "Comfort = " + Float.toString(tempFloat[1]) + "/1.6%";
    	tempText[2] = "Reputation = " + Float.toString(tempFloat[2]) + "/6.5%";
		tempText[3] = "Serenity = " + Float.toString(tempFloat[3]) + "/13.7%";
		tempText[4] = "Protection = " + Float.toString(tempFloat[4]) + "/18.2%";
		tempText[5] = "Conquest = " + Float.toString(tempFloat[5]) + "/20.2%";
		tempText[6] = "Wealth = " + Float.toString(tempFloat[6]) + "/2.0%";
		tempText[7] = "Ability = " + Float.toString(tempFloat[7]) + "/1.1%";
		tempText[8] = "Equipment = " + Float.toString(tempFloat[8]) + "/18.5%";

    	
    	knowledgeText.setText(tempText[0]);
    	comfortText.setText(tempText[1]);
    	reputationText.setText(tempText[2]);
    	serenityText.setText(tempText[3]);
    	protectionText.setText(tempText[4]);
    	conquestText.setText(tempText[5]);
    	wealthText.setText(tempText[6]);
    	abilityText.setText(tempText[7]);
    	equipmentText.setText(tempText[8]);
    	
    }
    
    private void GetMotiveBalance()
    {
    	motiveArray = new float[9];
		if (dungeon.getQuest() != null) {
			for (int i = 0; i < dungeon.getQuest().getActions().size(); i++) {
				if (dungeon.getQuest().getActions().get(i).CheckMotives(QuestMotives.KNOWLEDGE)) {
					motiveArray[0]++;
				} if (dungeon.getQuest().getActions().get(i).CheckMotives(QuestMotives.COMFORT)) {
					motiveArray[1]++;
				} if (dungeon.getQuest().getActions().get(i).CheckMotives(QuestMotives.REPUTATION)) {
					motiveArray[2]++;
				} if (dungeon.getQuest().getActions().get(i).CheckMotives(QuestMotives.SERENITY)) {
					motiveArray[3]++;
				} if (dungeon.getQuest().getActions().get(i).CheckMotives(QuestMotives.PROTECTION)) {
					motiveArray[4]++;
				} if (dungeon.getQuest().getActions().get(i).CheckMotives(QuestMotives.CONQUEST)) {
					motiveArray[5]++;
				} if (dungeon.getQuest().getActions().get(i).CheckMotives(QuestMotives.WEALTH)) {
					motiveArray[6]++;
				} if (dungeon.getQuest().getActions().get(i).CheckMotives(QuestMotives.ABILITY)) {
					motiveArray[7]++;
				} if (dungeon.getQuest().getActions().get(i).CheckMotives(QuestMotives.EQUIPMENT)) {
					motiveArray[8]++;
				}
			}
		}
		
		float amountOfMotives = 0;
		
		for (int i = 0; i < motiveArray.length; i++) {
			amountOfMotives += motiveArray[i];
		}
		
		for (int i = 0; i < motiveArray.length; i++) {
			if (amountOfMotives != 0) {
				motiveArray[i] = motiveArray[i] / amountOfMotives;
				motiveArray[i] *= 100;
			}
		}
    }
    
    
    private void CheckUsedTile(Tile tile, Action action)
    {
        if (tile.GetType() == TileTypes.ENEMY) {
            dungeon.removeEnemy(tile, action.getRoom());
		}
        else if (tile.GetType() == TileTypes.ITEM) {
			dungeon.removeItem(tile, action.getRoom());
		}
        else if (tile.GetType() == TileTypes.ENEMY_BOSS) {
			dungeon.removeBoss(tile, action.getRoom());
		}
        else if (tile.GetType() == TileTypes.TREASURE) {
        	dungeon.removeTreasure(tile, action.getRoom());
        }
        if (dungeon.getCivilians().size() + dungeon.getEnemies().size() + dungeon.getItems().size() == 0) {
        	if (stackNpc.size() != 0) {
            	if (stackNpc.get(0) == TileTypes.SOLDIER) {
            		if (action.getType() == ActionType.REPORT) {
            			dungeon.removeSoldier(tile, action.getRoom());
					}
            		else {
            			dungeon.removeSoldiers(npcPosition.get(0), action.getRoom());
					}
    			}
            	else {
            		dungeon.removeSoldiers(tile.GetCenterPosition(), action.getRoom());
    			}
			}
        	else {
        		dungeon.removeSoldiers(tile.GetCenterPosition(), action.getRoom());
			}
		}
        if (dungeon.getEnemies().size() + dungeon.getItems().size() == 0) {
        	if (stackNpc.size() != 0) {
        		if (stackNpc.get(0) == TileTypes.MAGE) {
        			if (action.getType() == ActionType.REPORT) {
        				dungeon.removeMage(tile, action.getRoom());
					}
        			else {
        				dungeon.removeMages(npcPosition.get(0), action.getRoom());
					}
    			}
            	else {
            		dungeon.removeMages(tile.GetCenterPosition(), action.getRoom());
    			}
			}
        	else {
        		dungeon.removeMages(tile.GetCenterPosition(), action.getRoom());
			}
    	}
        if (dungeon.getEnemies().size() + dungeon.getTreasures().size() == 0) {
        	if (stackNpc.size() != 0) {
        		if (stackNpc.get(0) == TileTypes.BOUNTYHUNTER) {
        			if (action.getType() == ActionType.REPORT) {
        				dungeon.removeBountyhunter(tile, action.getRoom());
					}
        			else {
        				dungeon.removeBountyHunters(npcPosition.get(0), action.getRoom());
					}
    			}
        		else {
            		dungeon.removeBountyHunters(tile.GetCenterPosition(), action.getRoom());
    			}
			}
        	else {
        		dungeon.removeBountyHunters(tile.GetCenterPosition(), action.getRoom());
			}
		}
        if (dungeon.getTreasures().size() + dungeon.getItems().size() == 0) {
        	if (stackCivilian.size() != 0) {
        		if (stackCivilian.get(0) == TileTypes.CIVILIAN) {
        			if (action.getType() == ActionType.REPORT) {
        				dungeon.removeCivilian(tile, action.getRoom());
					}
        			else {
        				dungeon.removeCivilians(civilianPosition.get(0), action.getRoom());
					}
    			}
        		else {
            		dungeon.removeCivilians(tile.GetCenterPosition(), action.getRoom());
    			}
			}
        	else {
        		dungeon.removeCivilians(tile.GetCenterPosition(), action.getRoom());
			}
		}
        
    }
    
    private void StackNpc(TileTypes temptype, Action action)
    {
    	if (action.getType() == ActionType.LISTEN) {
			stackNpc.push(temptype);
			npcPosition.push(action.getPosition());
			roomsNpc.push(action.getRoom());
		}
    	else if (action.getType() == ActionType.REPORT) {
			stackNpc.pop();
			npcPosition.pop();
			roomsNpc.pop();
			if (stackCivilian.size() != 0) {
				stackCivilian.pop();
				civilianPosition.pop();
				roomsCivilian.pop();
			}
		}
    	else if (action.getType() == ActionType.DEFEND || action.getType() == ActionType.ESCORT) {
			stackCivilian.push(temptype);
			civilianPosition.push(action.getPosition());
			roomsCivilian.push(action.getRoom());
		}
    }
}