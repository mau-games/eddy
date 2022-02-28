package narrative;
import game.Dungeon;
import game.Room;
import game.Tile;
import narrative.entity.Actor;
import narrative.entity.Entity;
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

    public void getallEntities(){
        for (Room room :  getAllRooms()){
            for (Point enemy : room.getEnemies()){
                entities.add(new Actor()); // new actor()
            }

            for (Point NPC : room.getNPCs()){
                entities.add(new Actor()); // new actor()
            }

            for (Point item : room.getItems()){
                entities.add(new Item()); // new item()
            }
        }
    }


}
