package gui.views;

import finder.geometry.Point;
import game.ApplicationConfig;
import game.Dungeon;
import game.Room;
import game.TileTypes;
import game.narrative.CreateEntityFileXML;
import game.narrative.Defines;
import game.narrative.entity.Entity;
import game.narrative.entity.ExtractedGeneratedEntity;
import game.narrative.entity.NPC;
import game.quest.Action;
import game.quest.ActionType;
import game.quest.actions.DamageAction;
import game.tiles.BossEnemyTile;
import gui.utils.DungeonDrawer;
import gui.utils.MapRenderer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

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
    private boolean isSelectNewNarrativeEntity;
    private MapRenderer renderer = MapRenderer.getInstance();

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
    @FXML
    private Button generateButton;
    @FXML
    private TextField maxLengthTextField;
    @FXML
    private Label generateStatusLbl;


    @FXML
    private HBox NarrativeDesignerBOX;

    //Generated Character GUI
    @FXML
    private Label genName;
    @FXML
    private Label genAge;
    @FXML
    private Label genGender;
    @FXML
    private Label genRace;
    @FXML
    private Label genClass;
    @FXML
    private Label genLikes;
    @FXML
    private Label genDislikes;
    @FXML
    private Label genAppearance;
    @FXML
    private TextArea generatedNarrativeTA;

    @FXML
    private Label genRelation;

    @FXML
    private Label EntityInforCounterLbl;
    //_____________________________________

    //Entity INFO GUI
    @FXML
    private Label EntityNameLbl;
    @FXML
    private Label EntityAgeLbl;
    @FXML
    private Label EntityGenderLbl;
    @FXML
    private Label EntityRaceLbl;
    @FXML
    private Label EntityClassLbl;

    @FXML
    private TextArea EntityLikesTA;
    @FXML
    private TextArea EntityDislikesTA;
    @FXML
    private TextArea EntityAppearanceTA;
    @FXML
    private TextArea EntityNarrativeTA;

/*    @FXML
    private Image entityImageGUI;*/
    @FXML
    private ToggleButton newEntityButton;
    @FXML
    private ImageView entityImageViewGUI;

    private ToggleButton relationEntitySelectionButton; // the button from the relation GUI menu
    private Defines.AttributeTypes attributeType;
    private int questPaneH_AddedPanelsCounter;
    private String raceStr;
    private Defines.RelationshipType selectedRelationType;
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

        initializeGeneratedEntityButtonEvents();
    }

    //displayar alla entities
    public void DrawSelectableEntities(){
        List<TileTypes> types = findTileTypes();
        router.postEvent(new RequestDisplayQuestTilesSelection(types));
    }

    //kallas när en togglebutton toolbaren klickas på
    private void initNarrativeView() {
        generateButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(dungeon.getNarrative().GetSelectedEntity() != null)
                CreateEntityFileXML.NewEntityFile(dungeon.getNarrative().GetSelectedEntity());
        });
        tbNarrativeTools.getItems()
                .forEach(toolbarAction -> {
                    toolbarAction.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {

                        if (((ToggleButton) toolbarAction).isSelected()) {
                            attributeType = Defines.AttributeTypes.valueOf(((ToggleButton)toolbarAction).getId());

                            for (Defines.AttributeTypes at : Defines.AttributeTypes.values()) {
                                if(at == attributeType){
                                    questPaneH.getChildren().set(at.ordinal(),narrativeAttributeGUI.get(at.ordinal())); // set the attribute to show the corresponding GUI in narrativeAttributeGUI
                                    break;
                                }
                            }
                        }
                        else {
                            attributeType = Defines.AttributeTypes.valueOf(((ToggleButton)toolbarAction).getId());

                            for (Defines.AttributeTypes at : Defines.AttributeTypes.values()) {
                                if(at == attributeType){
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
                            ((ToggleButton) toolbarAction).setText("Generate");
                            ((ToggleButton) toolbarAction).setStyle("-fx-background-color: lime; -fx-text-fill: black;");
                        }
                        else {
                            ((ToggleButton) toolbarAction).setText("Locked");
                            ((ToggleButton) toolbarAction).setStyle("-fx-background-color: #E34234; -fx-text-fill: black;");
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
/*            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NarrativeEntity_POS);
            List<TileTypes> types = findTileTypes();
            router.postEvent(new RequestDisplayQuestTilesSelection(types));*/

            generateStatusLbl.setText("WAITING");
            generateStatusLbl.setTextFill(Color.web("#f6ff00")); //yellow

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
        }

        else if (e instanceof EntityPositionUpdate) {
            if(newEntityButton.isSelected()){
                dungeon.getNarrative().SetSelectedEntityFromPoint(((EntityPositionUpdate) e).getPoint());
                LoadNewEntityGUI(dungeon.getNarrative().GetSelectedEntity());
                newEntityButton.setSelected(false);
                newEntityButton.setFocusTraversable(false);
                UpdateEntityInfoGUI();

                DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
            }
            else if(relationEntitySelectionButton != null && relationEntitySelectionButton.isSelected() && dungeon.getNarrative().GetSelectedEntity() != null){

                Entity selectedEntity = dungeon.getNarrative().GetEntityFromPoint(((EntityPositionUpdate) e).getPoint());
                HBox tempBox = ((HBox)relationEntitySelectionButton.getParent()); // temp holder for the Hbox parent
                tempBox.getChildren().remove(tempBox.getChildren().size() - 2); // removes the selectEntity button
                dungeon.getNarrative().GetSelectedEntity().AddRelation(new Defines().new Relationship(selectedRelationType, selectedEntity)); //adds the relation to the entity
                selectedRelationType = null;

                //Updated entity Name GUI
                Label newLabel = new Label(selectedEntity.GetNameOrID());
                newLabel.setTextFill(Color.WHITE);
                newLabel.setStyle("-fx-border-radius: 10; -fx-border-color: white;");
                newLabel.setFont(Font.font(18.0));
                newLabel.setPrefWidth(90);
                newLabel.setPrefHeight(40);
                tempBox.getChildren().add(tempBox.getChildren().size() - 1, newLabel);
                relationEntitySelectionButton.setSelected(false);
                relationEntitySelectionButton = null;

                DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
            }
            else{
                DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
                newEntityButton.setSelected(false);
                newEntityButton.setFocusTraversable(false);
            }


/*            if (doublePosition) {
                secondUpdatedPosition = (EntityPositionUpdate) e;

                if(dungeon.getNarrative().GetSelectedEntity() != null && newEntityButton.isSelected()){
                    dungeon.getNarrative().SetSelectedEntityFromPoint(((EntityPositionUpdate) e).getPoint());
                    LoadNewEntityGUI(dungeon.getNarrative().GetSelectedEntity());
                }
                doublePosition = false;
            }
            else {
                updatedPosition = (EntityPositionUpdate) e;
                doublePosition = false;
            }
            if (!doublePosition) {
                DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
                newEntityButton.setSelected(false);
                newEntityButton.setFocusTraversable(false);
            }*/

            router.postEvent(new RequestDisplayQuestTilesUnselection(false));
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
    @FXML
    private void SelectNewEntity(){
        if(newEntityButton.isSelected()){
            List<TileTypes> alltypes = findTileTypes();
            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NarrativeEntity_POS);
            router.postEvent(new RequestDisplayQuestTilesSelection(alltypes));
        }
        else{
            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NONE);
            newEntityButton.setSelected(false);
            newEntityButton.setFocusTraversable(false);
            router.postEvent(new RequestDisplayQuestTilesUnselection(false));
        }
    }

    private List<ExtractedGeneratedEntity> lmOutputEntities = new ArrayList<ExtractedGeneratedEntity>();
    @FXML
    private void GenerateNarrative(){
        dungeon.getNarrative().ClearGeneratedEntities();

        int maxLength = 500;
        if (!Objects.equals(maxLengthTextField.getText(), "")) //Om textfältet inte är tomt
        {
            if (Integer.parseInt(maxLengthTextField.getText()) > 0) //Om textfältets värde är över noll
            {
                maxLength = Integer.parseInt(maxLengthTextField.getText());
            }
        }

        EntityInforCounterLbl.setText( String.valueOf(characterToShow + 1) + "/" + lmOutputEntities.size());

        if(dungeon.getNarrative().GetSelectedEntity() != null){
            generateStatusLbl.setText("GENERATING...");
            generateStatusLbl.setTextFill(Color.web("#00eeff")); //blue

            String output = QueryLM(dungeon.getNarrative().GetSelectedEntity().ToModellString(), maxLength, 4);
            List<ExtractedGeneratedEntity> lmOutputEntities = ParseLMOutput(output);

            for (ExtractedGeneratedEntity e: lmOutputEntities) {
                dungeon.getNarrative().AddGeneratedEntity(e);
            }

            UpdateGeneratedEntityGUI(lmOutputEntities.get(0));
            characterToShow = 0;
            EntityInforCounterLbl.setText( String.valueOf(characterToShow + 1) + "/" + lmOutputEntities.size());

            generateStatusLbl.setText("DONE");
            generateStatusLbl.setTextFill(Color.web("#00ff40")); //green
        }

        System.out.println("");
    }

    private List<VBox> narrativeAttributeGUI = new ArrayList<VBox>();
    private List<VBox> attributeGUIArray = new ArrayList<VBox>();
    private List<VBox> hiddenVBOXes = new ArrayList<VBox>();

    private void CreateNarrativeGUI(Defines.AttributeTypes atr){


        narrativeAttributeGUI.add(new VBox());

        VBox attributeGUI = narrativeAttributeGUI.get(narrativeAttributeGUI.size() - 1);
        attributeGUI.setPrefWidth(140);
        attributeGUI.setMaxWidth(200);
        attributeGUI.setMaxHeight(80);

        attributeGUI.addEventHandler(KeyEvent.KEY_PRESSED ,event -> { //osäker om denna fungerar korrekt
            if( event.getCode() == KeyCode.ENTER )
                UpdateEntityInfoGUI();
        });

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
                tf.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent ke) {
                        if (ke.getCode().equals(KeyCode.ENTER)) {
                            entityLabel.setText(tf.getText());
                            dungeon.getNarrative().SetEntityName(tf.getText());
                        }
                    }
                });


                tf.setStyle("-fx-text-inner-color: white;");
                attributeGUI.getChildren().add( tf);
                break;
            case Age:
                //textfield
                TextField tf2 = new TextField();
                tf2.setPrefWidth(80);
                tf2.setMaxWidth(90);
                tf2.setStyle("-fx-text-inner-color: white;");
                tf2.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent ke) {
                        if (ke.getCode().equals(KeyCode.ENTER)) {
                            dungeon.getNarrative().GetSelectedEntity().SetAge(Integer.parseInt(tf2.getText()));
                        }
                    }
                });
                attributeGUI.getChildren().add(tf2);
                break;
            case Gender:
                attributeGUI.getChildren().add(CreateGenderMenuGUI());
                break;
            case Race:
                attributeGUI.getChildren().add(CreateRaceMenuGUI());
                break;
            case Class:
                attributeGUI.getChildren().add(CreateClassMenuGUI(""));
                break;
            case Relationship:
                CreateRelationGUI(attributeGUI);
                break;
            case Likes:
                //CreateListAttributeGUI(atr, attributeGUI); //button version
                CreateTextFieldInputAttribute(attributeGUI, atr);
                break;
            case Dislikes:
                //CreateListAttributeGUI(atr, attributeGUI); // button version
                CreateTextFieldInputAttribute(attributeGUI, atr);
                break;
            case Appearance:
                attributeGUI.setMaxWidth(250);
                attributeGUI.setPrefWidth(200);
                attributeGUI.setMaxHeight(150);
                attributeGUI.setPrefHeight(250);


                TextArea tf3 = new TextArea();
                tf3.setPrefWidth(200);
                tf3.setWrapText(true);
                tf3.setStyle("-fx-text-inner-color: white;");
                attributeGUI.getChildren().add( tf3);

                tf3.addEventHandler(KeyEvent.KEY_PRESSED ,event -> {
                            if( event.getCode() == KeyCode.ENTER )
                                dungeon.getNarrative().GetSelectedEntity().SetAppearance(tf3.getText());
                            });
                break;
            default:

                break;
        }

        attributeGUI.setStyle("-fx-border-radius: 10; -fx-border-color: #666;");
        attributeGUI.setAlignment(Pos.TOP_CENTER);
    }

    private void CreateTextFieldInputAttribute(VBox attributeGUI, Defines.AttributeTypes atr){
        attributeGUI.setMinHeight(100);
        attributeGUI.setMaxWidth(250);
        attributeGUI.setPrefWidth(200);


        TextArea tf3 = new TextArea();
        tf3.setPrefWidth(200);
        tf3.setMinHeight(80);
        tf3.setWrapText(true);
        tf3.setStyle("-fx-text-inner-color: white;");
        attributeGUI.getChildren().add( tf3);

        tf3.addEventHandler(KeyEvent.KEY_PRESSED ,event -> {
            if( event.getCode() == KeyCode.ENTER )
                if(atr == Defines.AttributeTypes.Likes)
                    dungeon.getNarrative().GetSelectedEntity().SetLikes(tf3.getText());
                else if(atr == Defines.AttributeTypes.Dislikes)
                    dungeon.getNarrative().GetSelectedEntity().SetDislikes(tf3.getText());
        });
    }
    private void CreateListAttributeGUI(Defines.AttributeTypes atr, VBox atrGUI){
        Button addBtn = new Button("+");
        addBtn.setStyle("-fx-background-color: green;");

        addBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> { // add a object
            HBox hbox = new HBox();
            hbox.setSpacing(20);

            TextField tf = new TextField();
            tf.setPrefSize(160, 80);
            tf.setStyle("-fx-text-inner-color: white;");
            tf.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent ke) {
                    if (ke.getCode().equals(KeyCode.ENTER)) { // finalise the input to a label, the object is created for the entity

                        if(atr == Defines.AttributeTypes.Likes)
                            dungeon.getNarrative().GetSelectedEntity().SetLikes(tf.getText());
                        else if(atr == Defines.AttributeTypes.Dislikes)
                            dungeon.getNarrative().GetSelectedEntity().SetDislikes(tf.getText());

                        Label LBL = new Label(tf.getText());
                        LBL.setTextFill(Color.WHITE);
                        LBL.setFont(Font.font(14.0));
                        hbox.getChildren().add(0, LBL);
                        hbox.getChildren().remove(tf);
                    }
                }
            });

            Button btn = new Button("-");
            btn.prefWidth(30);
            btn.prefHeight(30);
            btn.setStyle("-fx-background-color: red;");

            btn.setOnAction(event2 -> { // remove the object
                int index = ((VBox)hbox.getParent()).getChildren().indexOf(hbox);
                narrativeAttributeGUI.get(atr.ordinal()).getChildren().remove(hbox);
                dungeon.getNarrative().GetSelectedEntity().RemoveRelation(index - 1);
            });

            hbox.getChildren().add(tf);
            hbox.getChildren().add(btn);

            atrGUI.getChildren().add(atrGUI.getChildren().size() - 1, hbox);
        });


        atrGUI.getChildren().add(addBtn);

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

            mi.setOnAction(event ->{
                mb.setText(mi.getText());

                if(dungeon.getNarrative().GetSelectedEntity() != null){
                    dungeon.getNarrative().GetSelectedEntity().SetGender( Defines.Gender.valueOf(mi.getText()));
                }
            });
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

            mi.setOnAction(event ->{
                mb.setText(mi.getText());

                if(dungeon.getNarrative().GetSelectedEntity() != null){
                    dungeon.getNarrative().GetSelectedEntity().SetRace( Defines.Race.valueOf(mi.getText()));
                }
            });
        }

        return mb;
    }

    private MenuButton CreateClassMenuGUI(String hasValue){

        MenuButton mb = new MenuButton();

        if(hasValue != "")
            mb.setText(hasValue);

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
            mi.setOnAction(event ->{
                mb.setText(mi.getText());

                if(dungeon.getNarrative().GetSelectedEntity() != null){
                    dungeon.getNarrative().GetSelectedEntity().SetClass( Defines.Class.valueOf(mi.getText()));
                }
            });
        }

        return mb;
    }

    private HBox LoadHBoxinRelationGUI(Defines.RelationshipType relationType, String targetName){
        HBox hbox = new HBox();
        Label relationLabel = new Label(relationType.toString());
        relationLabel.setTextFill(Color.WHITE);
        relationLabel.setStyle("-fx-border-radius: 10; -fx-border-color: white;");
        relationLabel.setFont(Font.font(18.0));
        relationLabel.setPrefWidth(90);
        relationLabel.setPrefHeight(40);

        Label arrow = new Label("=>");
        arrow.setTextFill(Color.WHITE);
        arrow.setFont(Font.font(24.0));

        Label targetLabel = new Label(targetName);
        targetLabel.setTextFill(Color.WHITE);
        targetLabel.setStyle("-fx-border-radius: 10; -fx-border-color: white;");
        targetLabel.setFont(Font.font(18.0));
        targetLabel.setPrefWidth(90);
        targetLabel.setPrefHeight(40);

        hbox.getChildren().add(relationLabel);
        hbox.getChildren().add(arrow);
        hbox.getChildren().add(targetLabel);

        Button btn = new Button("-");
        btn.prefWidth(30);
        btn.prefHeight(30);
        btn.setStyle("-fx-background-color: red;");

        btn.setOnAction(event -> {
            int index = ((VBox)hbox.getParent()).getChildren().indexOf(hbox);
            narrativeAttributeGUI.get(Defines.AttributeTypes.Relationship.ordinal()).getChildren().remove(hbox);
            dungeon.getNarrative().GetSelectedEntity().RemoveRelation(index - 1);
        });
        hbox.getChildren().add(btn);
        return hbox;
    }

    private HBox CreateHBoxInRelationGUI(){
        HBox hbox = new HBox();
        hbox.setSpacing(6);
        MenuButton mb = new MenuButton();
        mb.setPrefWidth(100);
        mb.setMaxWidth(120);
        mb.setStyle("-fx-background-color: white;");

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
                        if (e.toString() == "LOCKED" || e.toString() == "None")
                            continue;

                        MenuItem mi2 = new MenuItem();
                        mi2.setText(e.toString());
                        mi2.setId(e.toString());
                        mb2.setStyle("-fx-background-color: white;");
                        mi2.setOnAction(event2 ->{
                            mb2.setText(mi2.getText());
                            dungeon.getNarrative().GetSelectedEntity().AddRelation(new Defines().new Relationship(e));
                        });
                        mb2.getItems().add(mi2);
                    }
                    mb2.setPrefWidth(100);
                    hbox.getChildren().add(hbox.getChildren().size() -1, mb2);
                }
                else {
                    relationEntitySelectionButton = new ToggleButton("select entity");
                    relationEntitySelectionButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event2 -> {
                        if(relationEntitySelectionButton.isSelected()){
                            selectedRelationType = r;
                            List<TileTypes> alltypes = findTileTypes();
                            DungeonDrawer.getInstance().changeBrushTo(DungeonDrawer.DungeonBrushes.NarrativeEntity_POS);
                            router.postEvent(new RequestDisplayQuestTilesSelection(alltypes));
                        }
                        else {
                            //List<TileTypes> alltypes = findTileTypes();
                            //router.postEvent(new RequestDisplayQuestTilesUnselection(false));
                        }
                    });

                    hbox.getChildren().add(hbox.getChildren().size() -1, relationEntitySelectionButton);
                }

                mb.setText(mi.getText());
                mb.setDisable(true);
            });

            mb.getItems().add(mi);
        }

        hbox.getChildren().add(mb);

        Button btn = new Button("-");
        btn.prefWidth(30);
        btn.prefHeight(30);
        btn.setStyle("-fx-background-color: red;");

        btn.setOnAction(event -> {
            int index = ((VBox)hbox.getParent()).getChildren().indexOf(hbox);
            narrativeAttributeGUI.get(Defines.AttributeTypes.Relationship.ordinal()).getChildren().remove(hbox);
            dungeon.getNarrative().GetSelectedEntity().RemoveRelation(index - 1);
        });
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
        //HBox hbox = CreateHBoxInRelationGUI();

        //vbox.getChildren().add(hbox);

        //javafx.scene.control.Button btn = new javafx.scene.control.Button("Add Relation");
        javafx.scene.control.Button btn = new javafx.scene.control.Button("+");
        btn.setStyle("-fx-background-color: green;");
        btn.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            vbox.getChildren().add(vbox.getChildren().size() - 1,   CreateHBoxInRelationGUI());
        });

        vbox.getChildren().add( btn);
    }

    public void LoadNewEntityGUI(Entity entity){
        narrativeAttributeGUI.clear();
        for (Defines.AttributeTypes at : Defines.AttributeTypes.values()) {
            CreateNarrativeGUI(at); // create the main GUI interaction
        }

        //clear pane
        for (int i = 0; i < questPaneH.getChildren().size(); i++){
            questPaneH.getChildren().set(i, hiddenVBOXes.get(i));
        }
        tbNarrativeTools.getItems() // set the buttons on the buttons panel to unselected
                .forEach(toolbarAction -> {
                    ((ToggleButton)toolbarAction).setSelected(false);

                        });

        //Name
        if(entity.GetName() != ""){
            entityLabel.setText(dungeon.getNarrative().GetSelectedEntity().GetName()); // set the entity info label name
            questPaneH.getChildren().set(Defines.AttributeTypes.Name.ordinal(), narrativeAttributeGUI.get(Defines.AttributeTypes.Name.ordinal()));

            ((VBox)narrativeAttributeGUI.get(Defines.AttributeTypes.Name.ordinal())).getChildren().stream()
                    .filter(tf -> tf instanceof TextField).forEach( tf -> {
                        ((TextField) tf).setText(entity.GetName());
                    });

            ((ToggleButton)tbNarrativeTools.getItems().get(Defines.AttributeTypes.Name.ordinal())).setSelected(true);
        }
        else
            entityLabel.setText(dungeon.getNarrative().GetSelectedEntity().GetID());

        //Age
        if(!"0".equals(entity.GetAge())){
            questPaneH.getChildren().set(Defines.AttributeTypes.Age.ordinal(), narrativeAttributeGUI.get(Defines.AttributeTypes.Age.ordinal()));

            ((VBox)narrativeAttributeGUI.get(Defines.AttributeTypes.Age.ordinal())).getChildren().stream()
                    .filter(tf -> tf instanceof TextField).forEach( tf -> {
                        ((TextField) tf).setText(entity.GetAge());
                    });

            ((ToggleButton)tbNarrativeTools.getItems().get(Defines.AttributeTypes.Age.ordinal())).setSelected(true);
        }
        //Gender
        if(entity.GetGender() != null){
            questPaneH.getChildren().set(Defines.AttributeTypes.Gender.ordinal(), narrativeAttributeGUI.get(Defines.AttributeTypes.Gender.ordinal()));

            ((VBox)narrativeAttributeGUI.get(Defines.AttributeTypes.Gender.ordinal())).getChildren().stream()
                    .filter(mb -> mb instanceof MenuButton).forEach( mb -> {
                        ((MenuButton) mb).setText(entity.GetGender().toString());
                    });

            ((ToggleButton)tbNarrativeTools.getItems().get(Defines.AttributeTypes.Gender.ordinal())).setSelected(true);
        }
        //Race
        if(entity.GetRace() != null){
            questPaneH.getChildren().set(Defines.AttributeTypes.Race.ordinal(), narrativeAttributeGUI.get(Defines.AttributeTypes.Race.ordinal()));

            ((VBox)narrativeAttributeGUI.get(Defines.AttributeTypes.Race.ordinal())).getChildren().stream()
                    .filter(mb -> mb instanceof MenuButton).forEach( mb -> {
                        ((MenuButton) mb).setText(entity.GetRace().toString());
                    });

            ((ToggleButton)tbNarrativeTools.getItems().get(Defines.AttributeTypes.Race.ordinal())).setSelected(true);
        }
        //Class
        if(entity.GetClass() != null){
            questPaneH.getChildren().set(Defines.AttributeTypes.Class.ordinal(), narrativeAttributeGUI.get(Defines.AttributeTypes.Class.ordinal()));

            ((VBox)narrativeAttributeGUI.get(Defines.AttributeTypes.Class.ordinal())).getChildren().stream()
                    .filter(tf -> tf instanceof MenuButton).forEach( tf -> {
                        ((MenuButton) tf).setText(entity.GetClass().toString());
                    });

            ((ToggleButton)tbNarrativeTools.getItems().get(Defines.AttributeTypes.Class.ordinal())).setSelected(true);
        }
        //Likes
        if(entity.GetLikes() != ""){
            questPaneH.getChildren().set(Defines.AttributeTypes.Likes.ordinal(), narrativeAttributeGUI.get(Defines.AttributeTypes.Likes.ordinal()));

            ((VBox)narrativeAttributeGUI.get(Defines.AttributeTypes.Likes.ordinal())).getChildren().stream()
                    .filter(tf -> tf instanceof TextArea).forEach( tf -> {
                        ((TextArea) tf).setText(entity.GetLikes());
                    });

            ((ToggleButton)tbNarrativeTools.getItems().get(Defines.AttributeTypes.Likes.ordinal())).setSelected(true);
        }
        //Dislikes
        if(entity.GetDislikes() != ""){
            questPaneH.getChildren().set(Defines.AttributeTypes.Dislikes.ordinal(), narrativeAttributeGUI.get(Defines.AttributeTypes.Dislikes.ordinal()));

            ((VBox)narrativeAttributeGUI.get(Defines.AttributeTypes.Dislikes.ordinal())).getChildren().stream()
                    .filter(tf -> tf instanceof TextArea).forEach( tf -> {
                        ((TextArea) tf).setText(entity.GetDislikes());
                    });

            ((ToggleButton)tbNarrativeTools.getItems().get(Defines.AttributeTypes.Dislikes.ordinal())).setSelected(true);
        }
        //Appearance
        if(entity.GetAppearance() != ""){
            questPaneH.getChildren().set(Defines.AttributeTypes.Appearance.ordinal(), narrativeAttributeGUI.get(Defines.AttributeTypes.Appearance.ordinal()));

            ((VBox)narrativeAttributeGUI.get(Defines.AttributeTypes.Appearance.ordinal())).getChildren().stream()
                    .filter(tf -> tf instanceof TextArea).forEach( tf -> {
                        ((TextArea) tf).setText(entity.GetAppearance());
                    });

            ((ToggleButton)tbNarrativeTools.getItems().get(Defines.AttributeTypes.Appearance.ordinal())).setSelected(true);
        }

        //Relationships
        if(entity.GetRelations().size() != 0){
            questPaneH.getChildren().set(Defines.AttributeTypes.Relationship.ordinal(), narrativeAttributeGUI.get(Defines.AttributeTypes.Relationship.ordinal()));
            for (Defines.Relationship relation : entity.GetRelations()) {
                int index = ((VBox)questPaneH.getChildren().get(Defines.AttributeTypes.Relationship.ordinal())).getChildren().size() - 1;
                ((VBox)questPaneH.getChildren().get(Defines.AttributeTypes.Relationship.ordinal())).getChildren().add( index ,LoadHBoxinRelationGUI(relation.GetRelation(), relation.GetName()));
            }
            ((ToggleButton)tbNarrativeTools.getItems().get(Defines.AttributeTypes.Relationship.ordinal())).setSelected(true);
        }

        //Show Entity Image
        Image entityImageGUI = new Image(entity.getURL());
        entityImageViewGUI.setImage(entityImageGUI);
    }


    public synchronized String QueryLM(String aPrompt, int aMaxLength, int aNumRuns)
    {
        BufferedReader reader;
        String line;
        StringBuilder responseContent = new StringBuilder();

        try {
            if (Objects.equals(aPrompt, "HelloTest")) //Hello world-test
            {
                URL url = new URL("http://127.0.0.1:5000/hello/");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();

                //Request setup
                http.setRequestMethod("GET");
                http.setConnectTimeout(5000);
                http.setReadTimeout(5000);

                http.disconnect();
            }
            else
            {
                URL url = new URL("http://127.0.0.1:5000/generate_narrative/");

                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setDoOutput(true);
                http.setRequestMethod("POST");
                http.addRequestProperty("message", aPrompt);
                http.addRequestProperty("max_length", String.valueOf(aMaxLength));
                http.addRequestProperty("num_runs", String.valueOf(aNumRuns));

                reader = new BufferedReader(new InputStreamReader(http.getInputStream()));

                while ((line = reader.readLine()) != null)
                {
                    responseContent.append(line);
                }
                reader.close();

                http.disconnect();
            }

        }
        catch (MalformedURLException e) {
            e.printStackTrace();
        }catch (IOException e){
            e.printStackTrace();
        }

        System.out.println(responseContent.toString());
        return responseContent.toString();
    }

    public List<ExtractedGeneratedEntity> ParseLMOutput(String aOutput)
    {
        //Fixa formateringsproblem med &-tecken
        String temp = aOutput.replace("&", "&amp;");
        ExtractedGeneratedEntity retVal = null;
        List<ExtractedGeneratedEntity> entities = new ArrayList<ExtractedGeneratedEntity>();

        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            //Konvertera strängen från LMen till ett XML-dokument som kan tolkas
            Document doc = saxBuilder.build(new StringReader(temp));
            Element classElement = doc.getRootElement();
            List<Element> entries = classElement.getChildren();

            //Ignorera första entriet då det är det som skickades till modellen

            for (int i = 0; i < entries.size(); i++)
            {
                Element entry = entries.get(i);
                String name = entry.getChild("name").getText();
                int age = Integer.parseInt(entry.getChild("age").getText());
                String gender = entry.getChild("gender").getText();
                String race = entry.getChild("race").getText();
                String characterClass = entry.getChild("class").getText();
                String appearance = entry.getChild("appearance").getText();
                String loves = entry.getChild("loves").getText();
                String hates = entry.getChild("hates").getText();
                String phobias = entry.getChild("phobias").getText();
                String narrative = entry.getChild("narrative").getText();

                entities.add(new ExtractedGeneratedEntity(name, age, gender, race, characterClass, appearance, loves, hates, phobias, narrative));
            }

        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return entities;
    }

    public void UpdateEntityInfoGUI(){
        Entity temp = dungeon.getNarrative().GetSelectedEntity();

        EntityNameLbl.setText(temp.GetName());
        EntityAgeLbl.setText(temp.GetAge());
        EntityGenderLbl.setText(temp.GetGenderStr());
        EntityRaceLbl.setText(temp.GetRaceStr());
        EntityClassLbl.setText(temp.GetClassStr());

        EntityLikesTA.setText(temp.GetLikes());
        EntityDislikesTA.setText(temp.GetDislikes());
        EntityAppearanceTA.setText(temp.GetAppearance());
        EntityNarrativeTA.setText(temp.GetNarrative());
    }

    private int characterToShow = 0;
    public void UpdateGeneratedEntityGUI(Entity entity){
        genName.setText(entity.GetName());
        genAge.setText(entity.GetAge());
        genGender.setText(entity.GetGenderStr());
        genRace.setText(entity.GetRaceStr());
        genClass.setText(entity.GetClassStr());
        genLikes.setText(entity.GetLikes());
        genDislikes.setText(entity.GetDislikes());
        genAppearance.setText(entity.GetAppearance());
        generatedNarrativeTA.setText(entity.GetNarrative());

        //if(entity.GetRelationStringAt(0) != null)
        //    genRelation.setText(entity.GetRelationStringAt(0));
    }

    @FXML
    private Button nameBtn;
    @FXML
    private Button ageBtn;
    @FXML
    private Button genderBtn;
    @FXML
    private Button raceBtn;
    @FXML
    private Button classBtn;
    @FXML
    private Button likesBtn;
    @FXML
    private Button dislikesBtn;
    @FXML
    private Button appearanceBtn;
    @FXML
    private Button relationshipBtn;

    public void initializeGeneratedEntityButtonEvents() {
        nameBtn.setOnAction(e -> ApplyAttribute(Defines.AttributeTypes.Name));
        ageBtn.setOnAction(e -> ApplyAttribute(Defines.AttributeTypes.Age));
        genderBtn.setOnAction(e -> ApplyAttribute(Defines.AttributeTypes.Gender));
        raceBtn.setOnAction(e -> ApplyAttribute(Defines.AttributeTypes.Race));
        classBtn.setOnAction(e -> ApplyAttribute(Defines.AttributeTypes.Class));
        likesBtn.setOnAction(e -> ApplyAttribute(Defines.AttributeTypes.Likes));
        dislikesBtn.setOnAction(e -> ApplyAttribute(Defines.AttributeTypes.Dislikes));
        appearanceBtn.setOnAction(e -> ApplyAttribute(Defines.AttributeTypes.Appearance));

        relationshipBtn.setOnAction(e -> ApplyAttribute(Defines.AttributeTypes.Relationship));
    }

    public void ApplyAttribute(Defines.AttributeTypes atr){
        if(dungeon.getNarrative().GetSelectedEntity() == null || dungeon.getNarrative().GetGeneratedCharacter(characterToShow) == null)
            return;

        Entity selectedEntity = dungeon.getNarrative().GetSelectedEntity();
        Entity generatedEntity = dungeon.getNarrative().GetGeneratedCharacter(characterToShow);

        switch (atr){
            case Relationship:
                if(generatedEntity.GetRelations().size() != 0)
                    selectedEntity.AddRelation(generatedEntity.GetRelations().get(0));
                break;
            case Name:
                selectedEntity.SetName(generatedEntity.GetName());
                break;
            case Age:
                selectedEntity.SetAge(Integer.parseInt(generatedEntity.GetAge()));
                break;
            case Gender:
                selectedEntity.SetGender(generatedEntity.GetGender());
                break;
            case Race:
                selectedEntity.SetRace(generatedEntity.GetRace());
                break;
            case Class:
                selectedEntity.SetClass(generatedEntity.GetClass());
                break;
            case Likes:
                selectedEntity.SetLikes(generatedEntity.GetLikes());
                break;
            case Dislikes:
                selectedEntity.SetDislikes(generatedEntity.GetDislikes());
                break;
            case Appearance:
                selectedEntity.SetAppearance(generatedEntity.GetAppearance());
                break;
        }

        UpdateEntityInfoGUI();
        LoadNewEntityGUI(dungeon.getNarrative().GetSelectedEntity());
    }

    private int generatedEntitiesCount = 0;
    private int maxGenerateEntities = 10;
    @FXML
    private void OnNextGeneratedEntity(){
        if(characterToShow < lmOutputEntities.size() - 1)
            characterToShow++;
        else {
            return;
        }

        UpdateGeneratedEntityGUI(lmOutputEntities.get(characterToShow));
        EntityInforCounterLbl.setText( String.valueOf(characterToShow + 1) + "/" + lmOutputEntities.size());
    }
    @FXML
    private void OnApplyAll(){
        if(dungeon.getNarrative().GetSelectedEntity() != null && dungeon.getNarrative().GetGeneratedCharacter(characterToShow) != null){
            dungeon.getNarrative().GetSelectedEntity().Copy(dungeon.getNarrative().GetGeneratedCharacter(characterToShow));
            LoadNewEntityGUI(dungeon.getNarrative().GetSelectedEntity());
            UpdateEntityInfoGUI();
        }
    }

    @FXML
    private void ResetEntityBtn(){
        if(dungeon.getNarrative().GetSelectedEntity() != null)
            dungeon.getNarrative().GetSelectedEntity().ResetEntity();

        UpdateEntityInfoGUI();
        LoadNewEntityGUI(dungeon.getNarrative().GetSelectedEntity());
    }

    @FXML
    private void OnPrevGeneratedEntity(){
        if(characterToShow > 0 && lmOutputEntities.size() > 0)
            characterToShow--;

        UpdateGeneratedEntityGUI(lmOutputEntities.get(characterToShow));

        EntityInforCounterLbl.setText( String.valueOf(characterToShow + 1) + "/" + lmOutputEntities.size());
    }
}