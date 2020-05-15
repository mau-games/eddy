package game.quest;

import finder.patterns.micro.Treasure;
import game.Dungeon;
import game.Tile;
import game.TileTypes;
import game.tiles.*;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MapQuestUpdate;
import util.eventrouting.events.MapUpdate;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class Quest {
    private List<Action> actions;
    private Dungeon owner;
    private boolean feasible;
    private List<ActionType> availableActions = new ArrayList<>();

    public Quest() {
        this.actions = new ArrayList<Action>();
//        EventRouter.getInstance().registerListener(this::pings, new MapQuestUpdate());
    }

    public Quest(Dungeon owner) {
        this.actions = new ArrayList<Action>();
        this.owner = owner;
        EventRouter.getInstance().registerListener(this::pings, new MapQuestUpdate());
    }

    private Quest(Quest quest) {
        this.actions = quest.getActions();
        this.owner = quest.owner;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void addActions(Action... actions){
        Collections.addAll(this.actions, actions);
    }

    public void addActionsAt(int index, Action... actions) {
        for (Action a : actions) {
            this.actions.add(index++, a);
        }
    }

    public void removeAction(Action action){
        this.actions.remove(action);
    }

    public int indexOf(Action action){
        return actions.indexOf(action);
    }

    public Action getAction(int index){
        return this.actions.get(index);
    }

    public Action getAction(UUID id){
        return this.actions.stream()
                .filter(action -> action.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    /**
     * Compares the action of current and given quest.
     * @param o
     * @return if they are not Equals
     */
    public boolean notEquals(Object o){
        if (!(o instanceof Quest)){
            return false;
        }
        int size = ((Quest) o).actions.size();
        if (actions.size() != size){
            return true;
        }
        for (int i = 0; i < size; i++) {
            if (actions.get(i).getType().getValue() != ((Quest) o).actions.get(i).getType().getValue()){
                return true;
            }
        }
        return false;
    }

    public boolean isFeasible() {
        return feasible;
    }

    public List<ActionType> getAvailableActions() {
        return availableActions;
    }

    public void checkForAvailableActions(){
        availableActions.clear();
        final int hasItem = owner.getItems().size();
        final int hasNPC = owner.getNpcs().size();
        final int hasEnemies = owner.getEnemies().size() + owner.getBossesPositions().size();

        if (hasItem > 0){
            availableActions.add(ActionType.EXPERIMENT);
            availableActions.add(ActionType.GATHER);
            availableActions.add(ActionType.READ);
            availableActions.add(ActionType.REPAIR);
            availableActions.add(ActionType.USE);
        }
        if (hasNPC > 0){
            availableActions.add(ActionType.LISTEN);
            availableActions.add(ActionType.REPORT);
            availableActions.add(ActionType.ESCORT);
        }
        if (hasEnemies > 0){
            availableActions.add(ActionType.KILL);
        }
        if (hasNPC > 0 || hasEnemies > 0){
            availableActions.add(ActionType.CAPTURE);
            availableActions.add(ActionType.STEALTH);
            availableActions.add(ActionType.SPY);
        }
        if (hasNPC > 0 || hasItem > 0){
            availableActions.add(ActionType.DAMAGE);
            availableActions.add(ActionType.DEFEND);
        }
        if (hasItem > 0 && hasNPC > 0){
            availableActions.add(ActionType.EXCHANGE);
            availableActions.add(ActionType.GIVE);
            availableActions.add(ActionType.TAKE);
        }
        availableActions.add(ActionType.EXPLORE);
        availableActions.add(ActionType.GO_TO);
    }

    public Quest copy(){
        return new Quest(this);
    }

    public void pings(PCGEvent e) {
        //TODO: get any update from dungeon that might affect any quest artifact
        if (e instanceof MapQuestUpdate){
            System.out.println(this.getClass().getName() + " : " + e.getClass().getName());
            MapQuestUpdate update = (MapQuestUpdate)e;
            if (update.hasPayload()){
                Tile prev = update.getPrev();
                if(prev.GetType().isEnemy()){
                    owner.removeEnemy(new EnemyTile(prev),update.getRoom());
                } else if (prev.GetType().isNPC()){
                    owner.removeNpc(new NpcTile(prev),update.getRoom());
                } else if (prev.GetType().isItem()){
                    owner.removeItem(new ItemTile(prev),update.getRoom());
                } else if (prev.GetType().isTreasure()){
                    owner.removeTreasure(new TreasureTile(prev),update.getRoom());
                } else if (prev.GetType().isEnemyBoss()) {
                    owner.removeBoss(new BossEnemyTile(prev), update.getRoom());
                }

                Tile next = update.getNext();
                if(next.GetType().isEnemy()){
                    owner.addEnemy(new EnemyTile(next),update.getRoom());
                } else if (next.GetType().isNPC()){
                    owner.addNpc(new NpcTile(next),update.getRoom());
                } else if (next.GetType().isItem()){
                    owner.addItem(new ItemTile(next),update.getRoom());
                } else if (next.GetType().isTreasure()){
                    owner.addTreasure(new TreasureTile(next),update.getRoom());
                } else if (next.GetType().isEnemyBoss()) {
                    owner.addBoss(new BossEnemyTile(prev), update.getRoom());
                }
            }
            checkForAvailableActions();
        }
    }
    
    public int[] toIntArray(){
        int[] arr = new int[actions.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = actions.get(i).getType().getValue();
        }
        return arr;
    }

    public void clearAction() {
        actions.clear();
    }

    /**
     * Match is true until proven wrong
     * @param quest
     * @return
     */
    public boolean startsWith(Quest quest) {
        for (int i = 0; i < quest.getActions().size(); i++){
            if ( i < actions.size()) {
//                System.out.println(actions.get(i).getType() + " == " + quest.getActions().get(i).getType());
                if (!(actions.get(i).getType().getValue() == quest.getActions().get(i).getType().getValue())){
                    return false;
                }
            }
        }
        return true;
    }
}
