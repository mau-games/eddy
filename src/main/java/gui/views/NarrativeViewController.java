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
import javafx.scene.Node;
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
 * @author Adam Ovilius
 * @author Oskar Kylvåg
 */

public class NarrativeViewController extends BorderPane implements Listener {
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

    public NarrativeViewController() {
        super();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/interactive/NarrativeView.fxml"));
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


    @Override
    public void ping(PCGEvent e) {

    }

    /* Ping method from QuestViewController
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
    }*/

    /**
     * Set this Pane as active
     *
     * @param active
     */
    public void setActive(boolean active) {
        this.isActive = active;
    }
}