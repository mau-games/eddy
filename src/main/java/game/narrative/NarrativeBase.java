package game.narrative;
import game.Dungeon;
import game.Tile;
import game.TileTypes;
import game.narrative.entity.*;
//import narrative.entity.*;

import java.util.ArrayList;
import java.util.Random;


import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapQuestUpdate;

public class NarrativeBase extends Dungeon {
    private Dungeon owner;

    private ArrayList<Entity> entities; //ändras till entity
    private ArrayList<NarrativeAttribute> narrativeAttributes;
    private ArrayList<Entity> generatedEntities;

    public ArrayList<NarrativeAttribute> getAttributeList() { return narrativeAttributes;}
    public ArrayList<Entity> getEntities() { return  entities;}

    private Entity selectedEntity;
    public Entity GetSelectedEntity(){ return  selectedEntity;}
    public Entity GetGeneratedCharacter(int at){ return generatedEntities.get(at);}
    public void ClearGeneratedEntities(){
        generatedEntities.clear();
    }
    public Entity GetEntityAt(int at){ return entities.get(at);}

    public NarrativeBase(Dungeon owner) {
        this.entities = new ArrayList<Entity>();
        this.narrativeAttributes = new ArrayList<NarrativeAttribute>();
        this.owner = owner;
        this.generatedEntities = new ArrayList<Entity>();

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

    public void CopyGeneratedEntity(Entity entity){

    }

    public void AddGeneratedEntity(Entity _generateEntity){
        generatedEntities.add(_generateEntity);
    }

    public void ResetGeneratedEntityList(){
        generatedEntities.clear();
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

    public void CreateEntityRelationship(Entity entity){
        if(entities.size() <= 1)
            return;

        Random rnd = new Random();
        double random = 0 + rnd.nextDouble() * (1 - 0);

        float relationChance = 0.08f * entities.size();

        int maxPossibleRelatoins = 4;

        for (int i = 0; i < maxPossibleRelatoins; i++){
            //chance for a relationship
            if(0 + rnd.nextDouble() * (1 - 0) < relationChance/maxPossibleRelatoins){
                //chance for relation type, Love,Hate,Phobia,Family
                double relationType = 0 + rnd.nextDouble() * (1 - 0);
                int randomEntity = rnd.nextInt((entities.size() - 0));

                while(entities.get(randomEntity) == entity){
                    randomEntity = rnd.nextInt((entities.size() - 0));
                }

                if(relationType <= 0.33f){
                    entity.AddRelation(new Defines().new Relationship(Defines.RelationshipType.Family, entities.get(randomEntity)));
                }
                else if(relationType <= 0.66f){
                    entity.AddRelation(new Defines().new Relationship(Defines.RelationshipType.Hate, entities.get(randomEntity)));
                }
                else if(relationType <= 1f){
                    entity.AddRelation(new Defines().new Relationship(Defines.RelationshipType.Love, entities.get(randomEntity)));
                }
            }
        }
    }
}
