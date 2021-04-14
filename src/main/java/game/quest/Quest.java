package game.quest;

import finder.patterns.micro.Treasure;
import game.Dungeon;
import game.Tile;
import game.TileTypes;
import game.tiles.*;
import gui.utils.InformativePopupManager;
import gui.utils.InformativePopupManager.PresentableInformation;
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
        	if (actions.get(i).getType() == ActionType.REPORT && actions.get(i).getPosition() != ((Quest) o).actions.get(i).getPosition()) {
				return true;
			}
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
        final int hasNpc = owner.getAllNpcs();
        //final int hasEnemies = owner.getEnemies().size() + owner.getBossesPositions().size();
        
		if (actions.size() == 0) {
	        if (hasNpc > 0){
	            availableActions.add(ActionType.LISTEN);
	            return;
	        }
		}
    }
    public void checkForAvailableActions(Action action, TileTypes tempTileType, Stack<TileTypes> stack, Dungeon dungeon){
        availableActions.clear();
        final int hasNpc = owner.getAllNpcs();
        if (checkIfLastActionWasReport()) {
        	if (stack.size() != 0) {
				availableActions.add(ActionType.REPORT);
			}
        	else if (hasNpc > 0) {
	        	availableActions.add(ActionType.LISTEN);
	            return;
			}
        }
        
		if (checkIfListenWasLastAction()) {
			decideWhatActions(tempTileType);
		}
		else if (!checkIfLastActionWasReport()){
			if (checkIfActionIsCivilian()) {
				availableActions.add(ActionType.LISTEN);
				availableActions.add(ActionType.REPORT);
			}
			else {
				availableActions.add(ActionType.REPORT);
				decideWhatActions(stack.peek());
			}
		}
		if (availableActions.size() == 0) {
			InformativePopupManager.getInstance().restartPopups();
			InformativePopupManager.getInstance().requestPopup(dungeon.dPane, PresentableInformation.OUT_OF_ACTIONS, "");
		}
		
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
                } else if (prev.GetType().isSoldier()) {
                    owner.removeSoldier(new SoldierTile(prev), update.getRoom());
                } else if (prev.GetType().isMage()) {
                    owner.removeMage(new MageTile(prev), update.getRoom());
                } else if (prev.GetType().isBountyhunter()) {
                    owner.removeBountyhunter(new BountyhunterTile(prev), update.getRoom());
                } else if (prev.GetType().isCivilian()) {
                    owner.removeCivilian(new CivilianTile(prev), update.getRoom());
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
                } else if (next.GetType().isSoldier()) {
                    owner.addSoldier(new SoldierTile(next), update.getRoom());
				} else if (next.GetType().isMage()) {
                    owner.addMage(new MageTile(next), update.getRoom());
				} else if (next.GetType().isBountyhunter()) {
                    owner.addBountyhunter(new BountyhunterTile(next), update.getRoom());
				} else if (next.GetType().isCivilian()) {
                    owner.addCivilian(new CivilianTile(next), update.getRoom());
				}
            }
            checkCurrentActions();
            checkForAvailableActions();
        }
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
    
    public boolean checkIfLastActionWasReport()
    {
    	if (actions.get(actions.size() - 1).getType() == ActionType.REPORT) {
			return true;
		}
    	return false;
    }
    public boolean checkIfActionIsCivilian()
    {
    	if (actions.get(actions.size() - 1).getType() == ActionType.DEFEND || actions.get(actions.size() - 1).getType() == ActionType.ESCORT) {
			return true;
		}
    	return false;
    }
    
    private void decideWhatActions(TileTypes tempTileType)
    {
        final int hasItem = owner.getItems().size();
        final int hasEnemies = owner.getEnemies().size() + owner.getBossesPositions().size();
        final int hasTreasures = owner.getTreasures().size();
        final int hasCivilians = owner.getCivilians().size();
        
    	if (tempTileType == TileTypes.SOLDIER) {
			if (hasCivilians > 0){
	            availableActions.add(ActionType.DEFEND);
	            availableActions.add(ActionType.ESCORT);
	        }
			if (hasEnemies > 0) {
	            availableActions.add(ActionType.DAMAGE);
	            availableActions.add(ActionType.CAPTURE);
			}
		}
		else if (tempTileType == TileTypes.MAGE) {
			if (hasItem > 0){
	            availableActions.add(ActionType.EXPERIMENT);
	            availableActions.add(ActionType.USE);
	            availableActions.add(ActionType.READ);
	            availableActions.add(ActionType.DAMAGE);
			}
			if (hasEnemies > 0) {
	            availableActions.add(ActionType.DAMAGE);
			}
				
		}
		else if (tempTileType == TileTypes.BOUNTYHUNTER) {
			if (hasEnemies > 0) {
				availableActions.add(ActionType.SPY);
			}
			if (hasEnemies > 0) {
				availableActions.add(ActionType.KILL);
			}
			if (hasTreasures > 0) {
				availableActions.add(ActionType.GATHER);
			}
		}
		else if (tempTileType == TileTypes.CIVILIAN) {
			if (hasTreasures > 0) {
				availableActions.add(ActionType.GATHER);
			}
			if (hasItem > 0) {
				availableActions.add(ActionType.REPAIR);
			}
		}
    }
}
