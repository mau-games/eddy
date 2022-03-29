package game.narrative;
import game.Dungeon;
import game.Room;
import game.narrative.entity.Enemy;
import game.narrative.entity.Entity;
import game.narrative.entity.Item;
import game.narrative.entity.NPC;
//import narrative.entity.*;

import java.util.ArrayList;
import java.util.List;


import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.events.MapQuestUpdate;

public class NarrativeBase extends Dungeon {
    private Dungeon owner;
    private ArrayList<Entity> entities; //ändras till entity
    private ArrayList<NarrativeAttribute> narrativeAttributes;

    public ArrayList<NarrativeAttribute> getAttributeList() { return narrativeAttributes;}
    public ArrayList<Entity> getEntities() { return  entities;}

    public NarrativeBase(){
        this.entities = new ArrayList<Entity>();
        this.narrativeAttributes = new ArrayList<NarrativeAttribute>();
        CreateEntities();
    }

    public NarrativeBase(Dungeon owner) {
        this.entities = new ArrayList<Entity>();
        this.narrativeAttributes = new ArrayList<NarrativeAttribute>();
        this.owner = owner;

        //EventRouter.getInstance().registerListener(this::pings, new MapQuestUpdate());
    }

    public void CreateEntities(){
        entities.clear();

        for (Room room :  owner.getAllRooms()){
            for (Point enemy : room.getEnemies()){
                entities.add(new Enemy(enemy)); // new Enemy()
            }

            for (Point NPC : room.getNPCs()){
                entities.add(new NPC(NPC)); // new NPC()
            }

            for (Point item : room.getItems()){
                entities.add(new Item(item)); // new Item()
            }
        }

        ((Enemy)entities.get(1)).GetPoint();
        Enemy e = new Enemy(new Point(1,1));
        e.GetPoint();
    }
}
