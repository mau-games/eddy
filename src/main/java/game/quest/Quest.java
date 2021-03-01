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
    private boolean feasible = true;
    private List<ActionType> availableActions = new ArrayList<>();
    private int usedFriend;
    private int usedVillian;

    public Quest() {
        this.actions = new ArrayList<Action>();
        usedVillian = 0;
        usedFriend = 0;
//        EventRouter.getInstance().registerListener(this::pings, new MapQuestUpdate());
    }

    public Quest(Dungeon owner) {
        this.actions = new ArrayList<Action>();
        this.owner = owner;
        usedVillian = 0;
        usedFriend = 0;
        EventRouter.getInstance().registerListener(this::pings, new MapQuestUpdate());
    }

    private Quest(Quest quest) {
        this.actions = quest.getActions();
        this.owner = quest.owner;
        usedVillian = 0;
        usedFriend = 0;
    }

    public List<Action> getActions() {
        return actions;
    }

    public void addActions(Action... actions){
        Collections.addAll(this.actions, actions);
        feasible = this.actions.stream().allMatch(Action::isPreconditionMet);
    }

    public void addActionsAt(int index, Action... actions) {
        for (Action a : actions) {
            this.actions.add(index++, a);
        }
        feasible = this.actions.stream().allMatch(Action::isPreconditionMet);
    }

    public void removeAction(Action action){
        this.actions.remove(action);
        feasible = this.actions.stream().allMatch(Action::isPreconditionMet);
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
        //final int hasItem = owner.getItems().size();
        final int hasNPC = owner.getNpcs().size();
        final int hasVillian = owner.getVillians().size();
        final int hasFriend = owner.getFriends().size();
        //final int hasEnemies = owner.getEnemies().size() + owner.getBossesPositions().size();
        
		if (actions.size() == 0) {
			if (hasVillian > 0) {
	        	availableActions.add(ActionType.LISTEN);
	        	usedVillian += 1;
			}
	        if (hasFriend > 0) {
	        	availableActions.add(ActionType.LISTEN);
	        	usedFriend += 1;
			}
	        if (hasNPC > 0){
	            availableActions.add(ActionType.LISTEN);
	            //availableActions.add(ActionType.REPORT);
	            //availableActions.add(ActionType.ESCORT);
	        }
            return;
		}
        
        /*if (hasItem > 0){
            availableActions.add(ActionType.EXPERIMENT);
            availableActions.add(ActionType.GATHER);
            availableActions.add(ActionType.READ);
            availableActions.add(ActionType.REPAIR);
            availableActions.add(ActionType.USE);
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
        availableActions.add(ActionType.GO_TO);*/
    }
    
    public void checkForAvailableActions(Action action){
        availableActions.clear();
        final int hasItem = owner.getItems().size();
        final int hasNPC = owner.getNpcs().size();
        final int hasVillian = owner.getVillians().size();
        final int hasFriend = owner.getFriends().size();
        final int hasEnemies = owner.getEnemies().size() + owner.getBossesPositions().size();
        
        if (checkIfLastActionWasReport()) {
			if (hasVillian > 0) {
	        	availableActions.add(ActionType.LISTEN);
	        	usedVillian += 1;
	            return;
			}
	        if (hasFriend > 0) {
	        	availableActions.add(ActionType.LISTEN);
	        	usedFriend += 1;
	            return;
			}
	        if (hasNPC > 0){
	            availableActions.add(ActionType.LISTEN);
	            //availableActions.add(ActionType.REPORT);
	            //availableActions.add(ActionType.ESCORT);
	            return;
	        }
        }
        
		if (checkIfListenWasLastAction()) {
			if (usedFriend > 0) {
				if (hasItem > 0){
		            availableActions.add(ActionType.EXPERIMENT);
		            availableActions.add(ActionType.GATHER);
		            availableActions.add(ActionType.READ);
		            availableActions.add(ActionType.REPAIR);
		            availableActions.add(ActionType.USE);
		            usedFriend--;
		        }
			}
			else if (usedVillian > 0) {
				if (hasEnemies > 0){
		            availableActions.add(ActionType.KILL);
		            usedVillian--;
		        }
			}
		}
		else if (checkIfLastActionWasFriendActions()) {
			usedFriend = 0;
			availableActions.add(ActionType.REPORT);
			if (hasItem > 0){
	            availableActions.add(ActionType.EXPERIMENT);
	            availableActions.add(ActionType.GATHER);
	            availableActions.add(ActionType.READ);
	            availableActions.add(ActionType.REPAIR);
	            availableActions.add(ActionType.USE);
	        }
		}
		else if (checkIfLastActionWasVillianActions()) {
			usedVillian = 0;
			availableActions.add(ActionType.REPORT);
			if (hasEnemies > 0){
	            availableActions.add(ActionType.KILL);
	        }
			
		}
        
        /*if (hasNPC > 0 || hasEnemies > 0){
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
        availableActions.add(ActionType.GO_TO);*/
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
                } 
                else if (next.GetType().isItem()){
                    owner.addItem(new ItemTile(next),update.getRoom());
                } else if (next.GetType().isTreasure()){
                    owner.addTreasure(new TreasureTile(next),update.getRoom());
                } else if (next.GetType().isEnemyBoss()) {
                    owner.addBoss(new BossEnemyTile(prev), update.getRoom());
                } else if (next.GetType().isVillian()) {
                    owner.addVillian(new VillianTile(next), update.getRoom());
                } else if (next.GetType().isFriend()) {
                    owner.addFriend(new FriendTile(next), update.getRoom());
				}
            }
        }
        checkCurrentActions();
        checkForAvailableActions();
    }

    private void checkCurrentActions() {
        actions.forEach(Action::checkConditions);
        feasible = actions.stream().allMatch(Action::isPreconditionMet);
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
    
    private boolean checkIfListenWasLastAction()
    {
    	if (actions.get(actions.size() - 1).getType() == ActionType.LISTEN) {
			return true;
		}
    	return false;
    }
    public boolean checkIfLastActionWasFriendActions()
    {
    	int temp = 0;
    	switch (actions.get(actions.size() - 1).getType()) {
		case EXPERIMENT:
		case GATHER:
		case READ:
		case REPAIR:
		case USE:
			temp = 1;
			break;
			

		default:
			break;
		}
    	if (temp == 1) {
			return true;
		}
    	return false;
    }
    
    public boolean checkIfLastActionWasVillianActions()
    {
    	if (actions.get(actions.size() - 1).getType() == ActionType.KILL) {
			return true;
		}
    	return false;
    }
    
    private boolean checkIfLastActionWasReport()
    {
    	if (actions.get(actions.size() - 1).getType() == ActionType.REPORT) {
			return true;
		}
    	return false;
    }
}
