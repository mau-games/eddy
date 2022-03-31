package game.narrative;
import game.Dungeon;
import game.Room;
import game.TileTypes;
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
import util.eventrouting.events.QuestPositionUpdate;

public class NarrativeBase extends Dungeon {
    private Dungeon owner;

    private Dungeon dungeon;
    private ArrayList<Entity> entities; //ändras till entity
    private ArrayList<NarrativeAttribute> narrativeAttributes;

    public ArrayList<NarrativeAttribute> getAttributeList() { return narrativeAttributes;}
    public ArrayList<Entity> getEntities() { return  entities;}

    private Entity selectedEntity;
    public Entity GetSelectedEntity(){ return  selectedEntity;}

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

    public void SetSelectedEntityFromPoint(finder.geometry.Point _selectedTilePoint){
        Point selectedTilePointer = new Point(_selectedTilePoint.getX(), _selectedTilePoint.getY());

        for (Entity e : entities) {
            if(e.GetPoint().equals(selectedTilePointer)){
                selectedEntity = e;
                break;
            }
        }
    }

    public Entity GetEntityFromPoint(finder.geometry.Point _selectedTilePoint){
        Point selectedTilePointer = new Point(_selectedTilePoint.getX(), _selectedTilePoint.getY());

        for (Entity e : entities) {
            if(e.GetPoint().equals(selectedTilePointer)){
                return e;
            }
        }

        return null;
    }

    public void CreateEntities(){
        entities.clear();

        for (QuestPositionUpdate enemyEntity: owner.getEnemies()) {
            entities.add(new Enemy(new Point(enemyEntity.getPoint().getX(),enemyEntity.getPoint().getY())));
        }
        for (QuestPositionUpdate itemEntity: owner.getItems()) {
            entities.add(new Item(new Point(itemEntity.getPoint().getX(),itemEntity.getPoint().getY())));
        }

        //NPCS...--------------
        for (QuestPositionUpdate npcBountyHunter: owner.getBountyHunters()) {
            entities.add(new NPC(new Point(npcBountyHunter.getPoint().getX(),npcBountyHunter.getPoint().getY()), TileTypes.BOUNTYHUNTER));
        }
        for (QuestPositionUpdate npcCivilian: owner.getCivilians()) {
            entities.add(new NPC(new Point(npcCivilian.getPoint().getX(),npcCivilian.getPoint().getY()), TileTypes.CIVILIAN));
        }
        for (QuestPositionUpdate npcSoldier: owner.getSoldiers()) {
            entities.add(new NPC(new Point(npcSoldier.getPoint().getX(),npcSoldier.getPoint().getY()), TileTypes.SOLDIER));
        }
        for (QuestPositionUpdate npcMage: owner.getMages()) {
            entities.add(new NPC(new Point(npcMage.getPoint().getX(),npcMage.getPoint().getY()), TileTypes.MAGE));
        }

        for (QuestPositionUpdate npc: owner.getNpcs()) {
            //if(owner.getNpcs().)
                entities.add(new NPC(new Point(npc.getPoint().getX(),npc.getPoint().getY()), TileTypes.NPC));
        }

    }

    public void SetEntityName(String nameInput){
        if(selectedEntity != null){
            selectedEntity.SetName(nameInput);
        }
    }
}
