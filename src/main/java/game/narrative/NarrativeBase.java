package game.narrative;
import game.Dungeon;
import game.Room;
import game.Tile;
import game.TileTypes;
import game.narrative.entity.Enemy;
import game.narrative.entity.Entity;
import game.narrative.entity.Item;
import game.narrative.entity.NPC;
//import narrative.entity.*;

import java.util.ArrayList;
import java.util.List;


import game.tiles.*;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapQuestUpdate;
import util.eventrouting.events.QuestPositionUpdate;

public class NarrativeBase extends Dungeon {
    private Dungeon owner;

    private ArrayList<Entity> entities; //ändras till entity
    private ArrayList<NarrativeAttribute> narrativeAttributes;

    public ArrayList<NarrativeAttribute> getAttributeList() { return narrativeAttributes;}
    public ArrayList<Entity> getEntities() { return  entities;}

    private Entity selectedEntity;
    public Entity GetSelectedEntity(){ return  selectedEntity;}

/*    public NarrativeBase(){
        this.entities = new ArrayList<Entity>();
        this.narrativeAttributes = new ArrayList<NarrativeAttribute>();
        CreateEntities();
    }*/

    public NarrativeBase(Dungeon owner) {
        this.entities = new ArrayList<Entity>();
        this.narrativeAttributes = new ArrayList<NarrativeAttribute>();
        this.owner = owner;
        EventRouter.getInstance().registerListener(this::pings, new MapQuestUpdate());
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
    public void RemoveEntity(finder.geometry.Point _point){
        Entity toRemove = GetEntityFromPoint(_point);
        if(toRemove == null)
            return;

        for (Entity e : entities) {
            if(toRemove == e){
                entities.remove(e);
            }
        }
    }

    public void AddEntity(TileTypes type, finder.geometry.Point _point) {
        Point point = new Point(_point.getX(), _point.getY());

        switch (type) {
            case NPC:
                entities.add(new NPC(point, type));
                break;
            case MAGE:
                entities.add(new NPC(point, type));
                break;
            case SOLDIER:
                entities.add(new NPC(point, type));
                break;
            case CIVILIAN:
                entities.add(new NPC(point, type));
                break;
            case BOUNTYHUNTER:
                entities.add(new NPC(point, type));
                break;
            case ITEM:
                entities.add(new Item(point));
                break;
            case ENEMY:
                entities.add(new Enemy(point));
                break;
            case ENEMY_BOSS:
                break;

        }

    }

/*    public void CreateEntities(){
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

    }*/

    public void pings(PCGEvent e) {

        //TODO: get any update from dungeon that might affect any quest artifact
        if (e instanceof MapQuestUpdate) {
            System.out.println(this.getClass().getName() + " : " + e.getClass().getName());
            MapQuestUpdate update = (MapQuestUpdate) e;
            if (update.hasPayload()) {


                Tile prev = update.getPrev();
                    RemoveEntity(prev.GetCenterPosition());

                Tile next = update.getNext();
                if(next.GetType() == TileTypes.NPC || next.GetType() == TileTypes.ENEMY || next.GetType() == TileTypes.ITEM
                || next.GetType() == TileTypes.MAGE || next.GetType() == TileTypes.SOLDIER || next.GetType() == TileTypes.CIVILIAN
                || next.GetType() == TileTypes.BOUNTYHUNTER)
                    AddEntity(next.GetType(), next.GetPositions().get(0));
            }
        }
    }
    public void SetEntityName(String nameInput){
        if(selectedEntity != null){
            selectedEntity.SetName(nameInput);
        }
    }
}
