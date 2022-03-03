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
import narrative.NarrativeBase;
import narrative.entity.NPC;
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
    private boolean doublePosition = false;
    private boolean firstTime = true;

    private Stack<NPC> stackNpc;
    private Stack<finder.geometry.Point> npcPosition;
    private Stack<Room> roomsNpc;
    @FXML
    private StackPane mapPane;

    public NarrativeViewController() {
        super();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/interactive/NarrativeView.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        //dungeon.getNarrative().CreateEntities();

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

    private void initNarrativeView() {

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

}