package game.quest;

import game.Dungeon;
import game.TileTypes;
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
        EventRouter.getInstance().registerListener(this::pings, new MapQuestUpdate());
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

    public Action getAction(int index){
        return this.actions.get(index);
    }

    public boolean isFeasible() {
        return feasible;
    }

    public List<ActionType> getAvailableActions() {
        return availableActions;
    }

    public void checkForAvailableActions(){
        availableActions.clear();
        final int[] hasItem = {0};
        final int[] hasNPC = {0};
        final int[] hasEnemies = {0};
        owner.getAllRooms().forEach(room -> {
            for (int[] tileTypeRow : room.toMatrix()) {
                for (int tileType : tileTypeRow) {
                    switch (TileTypes.toTileType(tileType)){
                        case ENEMY:
                        case ENEMY_BOSS:
                            hasEnemies[0]++;
                            break;
                        case TREASURE:
                        case ITEM:
                            hasItem[0]++;
                            break;
                        case NPC:
                            hasNPC[0]++;
                            break;
                    }
                }
            }
        });
        if (hasItem[0] > 0){
            availableActions.add(ActionType.DAMAGE);
            availableActions.add(ActionType.DEFEND);
            availableActions.add(ActionType.EXPERIMENT);
            availableActions.add(ActionType.GATHER);
            availableActions.add(ActionType.READ);
            availableActions.add(ActionType.REPAIR);
            availableActions.add(ActionType.USE);
        }
        if (hasNPC[0] > 0){
            availableActions.add(ActionType.LISTEN);
            availableActions.add(ActionType.REPORT);
            availableActions.add(ActionType.ESCORT);        }
        if (hasEnemies[0] > 0){
            availableActions.add(ActionType.KILL);
        }
        if (hasNPC[0] > 0 || hasEnemies[0] > 0){
            availableActions.add(ActionType.CAPTURE);
            availableActions.add(ActionType.STEALTH);
            availableActions.add(ActionType.SPY);
        }
        if (hasItem[0] > 0 && hasNPC[0] > 0){
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
            checkForAvailableActions();
        }
    }
}
