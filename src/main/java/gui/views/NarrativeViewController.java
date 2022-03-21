package gui.views;

import finder.geometry.Point;
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
import game.tiles.*;
import generator.algorithm.grammar.QuestGrammar;
import generator.algorithm.grammar.QuestGrammar.QuestMotives;
import gui.controls.LabeledCanvas;
import gui.utils.DungeonDrawer;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import narrative.Defines;
import narrative.NarrativeBase;
import narrative.entity.NPC;
import org.checkerframework.checker.units.qual.A;
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
    private boolean doublePosition = false;
    private boolean firstTime = true;

    private Stack<NPC> stackNpc;
    private Stack<finder.geometry.Point> npcPosition;
    private Stack<Room> roomsNpc;
    @FXML
    private StackPane mapPane;
    @FXML
    private HBox questPaneH;
    @FXML
    private ToolBar tbNarrativeTools;
    @FXML
    private ToolBar tbNarrativeLocks;
    @FXML
    private HBox narrativePaneH;
    @FXML
    private VBox questPaneV;
    @FXML
    private Button questPlaceholder;
    @FXML
    private VBox parameterVBox;

    private NarrativeBase narrativeBase;
    private Defines.AttributeType attributeType;

    private String raceStr;
    //Tillägg
    private QuestPositionUpdate updatedPosition = null;
    private QuestPositionUpdate secondUpdatedPosition = null;

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

        initNarrativeView();
        initAttributeToolbar();

        stackNpc = new Stack<NPC>();
        npcPosition = new Stack<finder.geometry.Point>();
        roomsNpc = new Stack<Room>();
    }

    //displayar alla entities
    public void DrawSelectableEntities(){
        List<TileTypes> types = findTileTypes();
        router.postEvent(new RequestDisplayQuestTilesSelection(types));
    }

    //kallas när en togglebutton toolbaren klickas på.. men hur??
    private void initNarrativeView() {
        tbNarrativeTools.getItems()
                .forEach(toolbarAction -> {
                    toolbarAction.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

                        if (((ToggleButton) toolbarAction).isSelected()) {
                            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
                            //selectedActionType = ActionType.valueOf(((ToggleButton) toolbarAction).getId());
                            List<TileTypes> types = findTileTypes();
                            attributeType = Defines.AttributeType.valueOf(((ToggleButton)toolbarAction).getId());

                            router.postEvent(new RequestDisplayQuestTilesSelection(types));
                            //Test3(); // skapar labels på rätt ställen
                            CreateAttributePaneAttribute(attributeType.toString());

                            int paneCount = narrativePaneH.getChildren().size();
                            //AddPaneNarrativeAttribute(paneCount + 1);
                        }
                        else {
                            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
                            router.postEvent(
                                    new RequestDisplayQuestTilesUnselection(false));
                            //selectedActionType = ActionType.NONE;
                        }
                    });
                });


        tbNarrativeLocks.getItems()
                .forEach(toolbarAction -> {
                    toolbarAction.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

                        if (((ToggleButton) toolbarAction).isSelected()) {
                            ((ToggleButton) toolbarAction).setText("LOCKED");

                            ((ToggleButton) toolbarAction).setStyle("-fx-background-color: red; -fx-text-fill: black;");
                        }
                        else {
                            ((ToggleButton) toolbarAction).setText("LOCK");
                            ((ToggleButton) toolbarAction).setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
                            ((ToggleButton) toolbarAction).setSelected(false);
                            ((ToggleButton) toolbarAction).setFocusTraversable(false);

                        }
                    });
                });
    }

    //Borde skapa en Vbox i questPaneH som attribut med labels och textinput
    private void Test3(){
        Label arrow = new Label("=>");
        arrow.setTextFill(Color.WHITE);
        arrow.setFont(Font.font(22.0));
        arrow.setStyle("-fx-background-color: transparent;");
        int temp = questPaneH.getChildren().size();
        questPaneH.getChildren().add(temp, arrow);
    }

    private void CreateAttributePaneAttribute(String labelText){
        //vBox
        parameterVBox = new VBox();
        parameterVBox.setStyle("-fx-border-radius: 10; -fx-border-color: #666;");
        parameterVBox.setAlignment(Pos.TOP_CENTER);
        parameterVBox.setPrefWidth(75);

        //label
        Label atrLbl = new Label(labelText);
        atrLbl.setFont(Font.font(18.0));
        atrLbl.setTextFill(Color.WHITE);
        atrLbl.setStyle("-fx-background-color: transparent;");

        //textField
        //TextField tf = new TextField();
        //tf.setStyle("-fx-text-inner-color: white;");

        //if the button click was Race -> create menu bar instead of textField
        Label raceLbl = new Label("");
        raceLbl.setFont(Font.font(14.0));
        raceLbl.setTextFill(Color.WHITE);
        raceLbl.setStyle("-fx-background-color: transparent;");
        raceLbl.setId("raceID");


        parameterVBox.getChildren().add(parameterVBox.getChildren().size(), atrLbl);
        parameterVBox.getChildren().add(parameterVBox.getChildren().size(), raceLbl);


        MenuButton mb = new MenuButton();
        for (int i = 0; i < 3; i++){
            MenuItem mi = new MenuItem();
            String temp = "";
            if(i == 0) // temporärt
                temp = "Orc";
            if(i == 1)
                temp = "Elf";
            if(i == 2)
                temp = "Dwarf";
            mi.setText(temp);

            raceLbl.setText("ORC");
            mb.getItems().add(mi);
        }

        parameterVBox.getChildren().add(parameterVBox.getChildren().size(), mb);
        parameterVBox.setMaxWidth(140.0);
        questPaneH.getChildren().add(questPaneH.getChildren().size(), parameterVBox);







        //seperation arrow, added after the attribute pane
        Label arrow = new Label("=>");
        arrow.setTextFill(Color.WHITE);
        arrow.setFont(Font.font(22.0));
        arrow.setStyle("-fx-background-color: transparent;");
        int temp = questPaneH.getChildren().size();
        questPaneH.getChildren().add(temp, arrow);

    }

    private void Test2(){

        questPaneH.getChildren().stream().filter(node -> node.getId().equals("questPlaceholder")).forEach(node -> {
            node.addEventHandler(MouseEvent.MOUSE_CLICKED,
                    event -> {
                        AtomicBoolean added = new AtomicBoolean(false);
                        int paneCount = questPaneH.getChildren().size();
                        tbNarrativeTools.getItems().stream()
                                .filter(action -> ((ToggleButton) action).isSelected())
                                .forEach(selected -> {
                                    ToggleButton toggleButton = (ToggleButton) selected;
                                    int questCount = dungeon.getQuest().getActions().size();

                                    if (!doublePosition && updatedPosition != null) {
                                        Action action = AddAction(toggleButton, questCount);
                                        Tile tile = action.getRoom().getTile(action.getPosition().getX(), action.getPosition().getY());
                                        AddPaneNarrativeAttribute(action, paneCount - 1);
                                        toggleButton.setSelected(false);
                                        RefreshPanel();
                                    }
                                });
                    });
        });

    }


    @FXML
    public void Test(){
        int paneCount = questPaneH.getChildren().size();
        //AddPaneNarrativeAttribute(paneCount + 1);
    }

    //Borde skapa en Vbox i questPaneH som attribut med labels och textinput
    public void AddPaneNarrativeAttribute(Action action, int index){
        ToggleButton toAdd = new ToggleButton();
        toAdd.setText(action.getName());
        toAdd.setId(action.getId().toString());
        questPaneH.getChildren().add(index, toAdd);

            Label arrow = new Label("=>");
            arrow.setTextFill(Color.WHITE);
            arrow.setFont(Font.font(14.0));
            arrow.setStyle("-fx-background-color: transparent;");
            questPaneH.getChildren().add(index + 1, arrow);

    }

    private void initAttributeToolbar() {

    }

    public void initWorldMap(Dungeon dungeon) {
        this.dungeon = dungeon;
        dungeon.dPane.renderAll();
        mapPane.getChildren().add(dungeon.dPane);
    }

    @FXML
    private void backWorldView(ActionEvent event) throws IOException {
        DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.MOVEMENT);
        router.postEvent(new RequestWorldView());
    }

    @Override
    public void ping(PCGEvent e) {

    }

    public void setActive(boolean active) {
        this.isActive = active;
    }


    private void initNarrativeToolbar(){

        List<TileTypes> types = findTileTypes();

        router.postEvent(new RequestDisplayQuestTilesSelection(types));
    }

    //on a certain action create one of this
    @FXML
    public void ToggleLock(ActionEvent event) throws IOException {
        Button btn = (Button)event.getSource();

        String id = btn.getId();

        switch (id){
            case "nameLock":
                break;
            case "nameLock2":
                break;
            case "nameLock3":
                break;
            case "nameLock4":
                break;
            case "nameLock5":
                break;
            case "nameLock6":
                break;

            default:
                    break;
        }
        if(btn.isFocused()){
            btn.setStyle("-fx-background-color: blue; -fx-text-fill: yellow;");
        }
        else{
            btn.setStyle("-fx-background-color: blue; -fx-text-fill: red;");
        }
    }

    private List<TileTypes> findTileTypes() {
        List<TileTypes> narrativeEntities = new LinkedList<TileTypes>();

        for (QuestPositionUpdate enemyEntity: dungeon.getEnemies()) {
            narrativeEntities.add(TileTypes.ENEMY);
        }
        for (QuestPositionUpdate itemEntity: dungeon.getItems()) {
            narrativeEntities.add(TileTypes.ITEM);
        }
        for (BossEnemyTile bossEntity: dungeon.getBosses()) {
            narrativeEntities.add(TileTypes.ENEMY_BOSS);
        }
        for (int npcEntity = 0; npcEntity < dungeon.getAllNpcs(); npcEntity++) {
            narrativeEntities.add(TileTypes.NPC);
        }

        return narrativeEntities;
    }

    //create a list of all the entities(the right tiletypes) in the dungeon
    private List<TileTypes> findTileTypes(boolean temp) {
        List<TileTypes> narrativeEntities = new LinkedList<TileTypes>();

        for (QuestPositionUpdate enemyEntity: dungeon.getEnemies()) {
            narrativeEntities.add(TileTypes.ENEMY);
        }
        for (QuestPositionUpdate itemEntity: dungeon.getItems()) {
            narrativeEntities.add(TileTypes.ITEM);
        }
        for (BossEnemyTile bossEntity: dungeon.getBosses()) {
            narrativeEntities.add(TileTypes.ENEMY_BOSS);
        }
        for (int npcEntity = 0; npcEntity < dungeon.getAllNpcs(); npcEntity++) {
            narrativeEntities.add(TileTypes.NPC);
        }

        return narrativeEntities;
    }

    //?? dunno
    private void RefreshPanel()
    {
        final boolean[] IsAnyDisabled = {false};
        tbNarrativeTools.getItems().forEach(node -> {
            String buttonID = ((ToggleButton) node).getTooltip().getText();
            boolean disable = dungeon.getQuest().getAvailableActions().stream()
                    .noneMatch(actionType -> actionType.toString().equals(buttonID));
            node.setDisable(disable);
            if (disable) {
                IsAnyDisabled[0] = true;
            }
        });
    }

    //create a action dummy to get the position of the entity which is clicked on
    private Action AddAction(ToggleButton toolbarActionToggleButton, int index) {
        Random random = new Random();
        ActionType type = ActionType.NONE;
        Action action = new DamageAction();

        if (action != null) {
            action.setId(UUID.randomUUID());
            action.setType(type);
            action.setPosition(updatedPosition.getPoint());
            action.setRoom(updatedPosition.getRoom());
            updatedPosition = null;
        }

        return action;
    }
}