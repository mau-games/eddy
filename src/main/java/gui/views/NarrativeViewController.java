package gui.views;

import finder.geometry.Point;
import game.ApplicationConfig;
import game.Dungeon;
import game.Room;
import game.TileTypes;
import game.narrative.NarrativeBase;
import game.narrative.entity.Entity;
import game.quest.Action;
import game.quest.ActionType;
import game.quest.actions.*;
import game.tiles.*;
import gui.utils.DungeonDrawer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import game.narrative.Defines;
import game.narrative.entity.NPC;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;

import java.io.IOException;
import java.util.*;
import java.util.List;
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
    @FXML
    private ScrollPane questScrollPane;
    @FXML
    private BorderPane borderPane;
    @FXML
    private Label entityLabel;

    private Defines.AttributeTypes attributeType;
    private int questPaneH_AddedPanelsCounter;
    private String raceStr;
    //Tillägg
    private EntityPositionUpdate updatedPosition = null;
    private EntityPositionUpdate secondUpdatedPosition = null;

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
        router.registerListener(this, new RequestNarrativeView());
        router.registerListener(this, new EntityPositionUpdate(null, null, false));
        router.registerListener(this, new EntityPositionInvalid());

        initNarrativeView();
        initAttributeToolbar();

        stackNpc = new Stack<NPC>();
        npcPosition = new Stack<finder.geometry.Point>();
        roomsNpc = new Stack<Room>();

        questPaneH.setPrefWidth(1300); // got easier with hard coding....

        for (Defines.AttributeTypes at : Defines.AttributeTypes.values()) {
            hiddenVBOXes.add(new VBox());   // create a dummy vbox for the specifiq attribute to use when disabled
            hiddenVBOXes.get(hiddenVBOXes.size() - 1).setPrefSize(0,0);

            CreateNarrativeGUI(at); // create the main GUI interaction

            attributeGUIArray.add(hiddenVBOXes.get(hiddenVBOXes.size() - 1)); // add the corresponding hidden vbox to the right place (attribute) in the attributeGUIArray
            questPaneH.getChildren().add(attributeGUIArray.get(attributeGUIArray.size() - 1)); //add the attribute to the panel (currently the hidden vbox)
        }

    }

    //displayar alla entities
    public void DrawSelectableEntities(){
        List<TileTypes> types = findTileTypes();
        router.postEvent(new RequestDisplayQuestTilesSelection(types));
    }

    //kallas när en togglebutton toolbaren klickas på
    private void initNarrativeView() {

        tbNarrativeTools.getItems()
                .forEach(toolbarAction -> {
                    toolbarAction.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

                        if (((ToggleButton) toolbarAction).isSelected()) {
                            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
                            //selectedActionType = ActionType.valueOf(((ToggleButton) toolbarAction).getId());
                            List<TileTypes> types = findTileTypes();
                            attributeType = Defines.AttributeTypes.valueOf(((ToggleButton)toolbarAction).getId());

                            //router.postEvent(new RequestDisplayQuestTilesSelection(types));

                            for (Defines.AttributeTypes at : Defines.AttributeTypes.values()) {
                                if(at == attributeType){
                                    //questPaneH.getChildren().add(questPaneH.getChildren().get(at.ordinal()));
                                    //questPaneH.getChildren().add(narrativeAttributeGUI.get(at.ordinal()));
                                    questPaneH.getChildren().set(at.ordinal(),narrativeAttributeGUI.get(at.ordinal())); // set the attribute to show the corresponding GUI in narrativeAttributeGUI
                                    //attributeGUIArray.set(at.ordinal(), narrativeAttributeGUI.get(at.ordinal()));
                                    break;
                                }
                            }
                        }
                        else {
                            attributeType = Defines.AttributeTypes.valueOf(((ToggleButton)toolbarAction).getId());

                            for (Defines.AttributeTypes at : Defines.AttributeTypes.values()) {
                                if(at == attributeType){
                                    //attributeGUIArray.set(at.ordinal(), hiddenVBOX);
                                    questPaneH.getChildren().set(at.ordinal(), hiddenVBOXes.get(at.ordinal())); // set the right attribute to use the corresponding hiddenVbox in the hiddenVBox array
                                    break;
                                }
                            }

                            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
                            router.postEvent(
                                    new RequestDisplayQuestTilesUnselection(false));
                        }
                    });
                });


        tbNarrativeLocks.getItems()
                .forEach(toolbarAction -> {
                    toolbarAction.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

                        if (((ToggleButton) toolbarAction).isSelected()) {
                            ((ToggleButton) toolbarAction).setText("LOCKED");
                            ((ToggleButton) toolbarAction).setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
                        }
                        else {
                            ((ToggleButton) toolbarAction).setText("LOCK");
                            ((ToggleButton) toolbarAction).setStyle("-fx-background-color: black; -fx-text-fill: white;");
                            ((ToggleButton) toolbarAction).setSelected(false);
                            ((ToggleButton) toolbarAction).setFocusTraversable(false);

                        }
                    });
                });
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

    //NARRATIVE
    @Override
    public void ping(PCGEvent e) {

        if (e instanceof RequestNarrativeView) {
            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NarrativeEntity_POS);
            List<TileTypes> types = findTileTypes();
            router.postEvent(new RequestDisplayQuestTilesSelection(types));

            if (firstTime) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Welcome to the Narrative-Creation!\n"
                        + "To the left you have the available attributes\n"
                        + "To the right you the generated narrative!\n"
                        + "The map shows your dungeon\n"
                        + "Click on a entity in the dungeon you wish to create a narrative for\n"
                        + "Add attributes for the entity and you can start designing a narrative with the AI \n"
                        + "Lock the attributes to not generate towards that attribute \n",
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
            //DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);

            //RefreshPanel();

            //check the validity of each action
/*            questPaneH.getChildren().filtered(node -> node instanceof ToggleButton).forEach(node -> {
                if (dungeon.getQuest().getAction(UUID.fromString(node.getId())).isPreconditionMet()){
                    node.getStyleClass().remove("danger");
                } else {
                    node.getStyleClass().add("danger");
                }
            });*/

        }

        else if (e instanceof EntityPositionUpdate) {
            doublePosition = ((EntityPositionUpdate) e).isSecondPosition();
            AtomicBoolean replace = new AtomicBoolean(false);
            if (doublePosition) {
                secondUpdatedPosition = (EntityPositionUpdate) e;

                entityLabel.setText(e.getPayload().toString());
                doublePosition = false;
            } else {
                updatedPosition = (EntityPositionUpdate) e;
                doublePosition = false;
            }
            if (!doublePosition) {
                DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
            }

            router.postEvent(new RequestDisplayQuestTilesUnselection(doublePosition));
        }

    }

    private void FindEntity(){

    }

    private List<Point> findEntityTilePositions() {
        List<finder.geometry.Point> narrativeEntities = new LinkedList<finder.geometry.Point>();

        for (QuestPositionUpdate enemyEntity: dungeon.getEnemies()) {
            narrativeEntities.add(enemyEntity.getPoint());
        }
        for (QuestPositionUpdate itemEntity: dungeon.getItems()) {
            narrativeEntities.add(itemEntity.getPoint());
        }
        for (BossEnemyTile bossEntity: dungeon.getBosses()) { //
            for (Point p : bossEntity.GetPositions()){
                narrativeEntities.add(p);
            }
        }
        for (QuestPositionUpdate npc :   dungeon.getNpcs()){
            narrativeEntities.add(npc.getPoint());
        }

        return narrativeEntities;
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






    //-------------------------------------------------------------------------

    private List<VBox> narrativeAttributeGUI = new ArrayList<VBox>();
    private List<VBox> attributeGUIArray = new ArrayList<VBox>();
    private List<VBox> hiddenVBOXes = new ArrayList<VBox>();

    private void CreateNarrativeGUI(Defines.AttributeTypes atr){

        narrativeAttributeGUI.add(new VBox());

        VBox attributeGUI = narrativeAttributeGUI.get(narrativeAttributeGUI.size() - 1);
        attributeGUI.setPrefWidth(140);
        attributeGUI.setMaxWidth(200);
        attributeGUI.setMaxHeight(80);

        //label
        Label atrLbl = new Label(atr.toString());
        atrLbl.setFont(Font.font(18.0));
        atrLbl.setTextFill(Color.WHITE);
        atrLbl.setStyle("-fx-background-color: transparent;");
        if(atr == Defines.AttributeTypes.Relationship){
            atrLbl.setText("Relation to other entities");
        }
        attributeGUI.getChildren().add(atrLbl);

        switch(atr){
            case Name:
                //textField
                TextField tf = new TextField();
                tf.setPrefWidth(80);
                tf.setMaxWidth(90);
                tf.setStyle("-fx-text-inner-color: white;");
                attributeGUI.getChildren().add( tf);
                break;
            case Age:
                //textfield
                TextField tf2 = new TextField();
                tf2.setPrefWidth(80);
                tf2.setMaxWidth(90);
                tf2.setStyle("-fx-text-inner-color: white;");
                attributeGUI.getChildren().add(tf2);
                break;
            case Gender:
                attributeGUI.getChildren().add(CreateGenderMenuGUI());
                break;
            case Race:
                attributeGUI.getChildren().add(CreateRaceMenuGUI());
                break;
            case Class:
                attributeGUI.getChildren().add(CreateClassMenuGUI());
                break;
            case Relationship:

                CreateRelationGUI(attributeGUI);
                break;
            default:
                break;
        }

        attributeGUI.setStyle("-fx-border-radius: 10; -fx-border-color: #666;");
        attributeGUI.setAlignment(Pos.TOP_CENTER);
    }

    private MenuButton CreateGenderMenuGUI(){
        MenuButton mb = new MenuButton();
        mb.setPrefWidth(100);
        mb.setMaxWidth(120);

        mb.setText("gender");
        mb.setStyle("-fx-background-color: white;");

        for (Defines.Gender g : Defines.Gender.values()){
            if(g.toString() == "LOCKED")
                continue;

            MenuItem mi = new MenuItem();
            mi.setText(g.toString());

            mb.getItems().add(mi);
        }

        return mb;
    }

    private MenuButton CreateRaceMenuGUI(){
        MenuButton mb = new MenuButton();
        mb.setPrefWidth(100);
        mb.setMaxWidth(120);

        mb.setText("race");
        mb.setStyle("-fx-background-color: white;");


        for (Defines.Race r : Defines.Race.values()){
            if(r.toString() == "LOCKED")
                continue;

            MenuItem mi = new MenuItem();
            mi.setText(r.toString());
            mb.getItems().add(mi);
        }

        return mb;
    }

    private MenuButton CreateClassMenuGUI(){
        MenuButton mb = new MenuButton();
        mb.setPrefWidth(100);
        mb.setMaxWidth(120);

        mb.setText("class");
        mb.setStyle("-fx-background-color: white;");

        for (Defines.Class c : Defines.Class.values()){
            if(c.toString() == "LOCKED")
                continue;

            MenuItem mi = new MenuItem();
            mi.setText(c.toString());
            mb.getItems().add(mi);
        }

        return mb;
    }

    private HBox CreateHBoxInRelationGUI(){
        HBox hbox = new HBox();
        hbox.setSpacing(6);
        MenuButton mb = new MenuButton();
        mb.setPrefWidth(100);
        mb.setMaxWidth(120);


        for (Defines.RelationshipType r : Defines.RelationshipType.values()){
            if(r.toString() == "LOCKED")
                continue;

            MenuItem mi = new MenuItem();
            mi.setText(r.toString());
            mi.setId(r.toString());

            mi.setOnAction(event ->
            {
                if (hbox.getChildren().size() >= 3) {
                    return;
                }
                Label arrow = new Label("=>");
                arrow.setTextFill(Color.WHITE);
                arrow.setFont(Font.font(14.0));
                arrow.setStyle("-fx-background-color: transparent;");
                hbox.getChildren().add(hbox.getChildren().size() -1, arrow);

                if (mi.getId() == "Phobia") {
                    MenuButton mb2 = new MenuButton();
                    for (Defines.Element e : Defines.Element.values()) {
                        if (e.toString() == "LOCKED" || e.toString() == "NONE")
                            continue;

                        MenuItem mi2 = new MenuItem();
                        mi2.setText(e.toString());
                        mi2.setId(e.toString());

                        mb2.getItems().add(mi2);
                    }
                    mb2.setPrefWidth(70);
                    hbox.getChildren().add(hbox.getChildren().size() -1, mb2);
                }
                else {
                    ToggleButton entityBtn = new ToggleButton("select entity");
                    entityBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, event2 -> {
                        if(entityBtn.isSelected()){
                            List<TileTypes> alltypes = findTileTypes();
                            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NarrativeEntity_POS);
                            router.postEvent(new RequestDisplayQuestTilesSelection(alltypes));
                        }
                        else {
                            router.postEvent(new RequestDisplayQuestTilesUnselection(true));
                        }
                    });

                    hbox.getChildren().add(hbox.getChildren().size() -1, entityBtn);

/*                    Label lbl = new Label('"' + "insert Enitity" + '"');
                    hbox.getChildren().add(hbox.getChildren().size() -1, lbl);*/
                }
            });
            mb.getItems().add(mi);
        }

        hbox.getChildren().add(mb);

        Button btn = new Button("-");
        btn.prefWidth(30);
        btn.prefHeight(30);
        btn.setStyle("-fx-background-color: red;");

        btn.setOnAction(event -> narrativeAttributeGUI.get(Defines.AttributeTypes.Relationship.ordinal()).getChildren().remove(hbox));
        hbox.getChildren().add(btn);

        return hbox;
    }

    private void CreateRelationGUI(VBox vbox){
        vbox.setAlignment(Pos.CENTER_LEFT);
        vbox.setMaxHeight(500);
        vbox.setPrefWidth(300);
        vbox.setPrefHeight(100);
        vbox.setMaxWidth(350);

        vbox.setSpacing(15);
        //Image image = new Image()
        //ImageView iv = new ImageView();
        HBox hbox = CreateHBoxInRelationGUI();

        vbox.getChildren().add(hbox);

        //javafx.scene.control.Button btn = new javafx.scene.control.Button("Add Relation");
        javafx.scene.control.Button btn = new javafx.scene.control.Button("+");
        btn.setStyle("-fx-background-color: green;");
        btn.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            vbox.getChildren().add(vbox.getChildren().size() - 1,   CreateHBoxInRelationGUI());
        });

        vbox.getChildren().add( btn);
    }
}