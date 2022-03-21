package narrative;
import game.Dungeon;
import game.Room;
import game.Tile;
import game.quest.Action;
import narrative.entity.Actor;
import narrative.entity.Enemy;
import narrative.entity.Entity;
import narrative.entity.NPC;
import narrative.entity.Item;
import java.util.ArrayList;
import java.util.List;


import game.quest.ActionType;
import game.quest.Quest;
import generator.algorithm.Algorithm;
import generator.algorithm.Algorithm.AlgorithmTypes;
import generator.algorithm.MAPElites.MAPEliteAlgorithm;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.grammar.QuestGenerator;
import generator.algorithm.grammar.QuestGrammar;
import generator.config.GeneratorConfig;
import util.Point;
import util.Util;
import util.config.ConfigurationUtility;
import util.config.MissingConfigurationException;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;
import java.util.ArrayList;

public class NarrativeBase extends Dungeon {
    private Dungeon owner;
    private ArrayList<Entity> entities; //ändras till entity

    public NarrativeBase(){
        this.entities = new ArrayList<Entity>();
        CreateEntities();
    }

    public NarrativeBase(Dungeon owner) {
        this.entities = new ArrayList<Entity>();
        this.owner = owner;

        //EventRouter.getInstance().registerListener(this::pings, new MapQuestUpdate());

    }

    public void CreateEntities(){
        entities.clear();

        for (Room room :  owner.getAllRooms()){
            for (Point enemy : room.getEnemies()){
                entities.add(new Enemy()); // new Enemy()
            }

            for (Point NPC : room.getNPCs()){
                entities.add(new NPC()); // new NPC()
            }

            for (Point item : room.getItems()){
                entities.add(new Item()); // new Item()
            }
        }
    }
}
