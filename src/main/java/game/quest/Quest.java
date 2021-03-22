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
        final int hasKnight = owner.getKnights().size();
        final int hasWizard = owner.getWizards().size();
        final int hasDruid = owner.getDruids().size();
        final int hasBountyHunter = owner.getBountyHunters().size();
        final int hasBlacksmith = owner.getBlacksmiths().size();
        final int hasMerchant = owner.getMerchants().size();
        final int hasThief = owner.getThiefs().size();
        //final int hasEnemies = owner.getEnemies().size() + owner.getBossesPositions().size();
        
		if (actions.size() == 0) {
			if (hasKnight > 0) {
	        	availableActions.add(ActionType.LISTEN);
	            return;
			}
	        if (hasWizard > 0) {
	        	availableActions.add(ActionType.LISTEN);
	            return;
			}
	        if (hasDruid > 0) {
	        	availableActions.add(ActionType.LISTEN);
	            return;
			}
	        if (hasBountyHunter > 0) {
	        	availableActions.add(ActionType.LISTEN);
	            return;
			}
	        if (hasBlacksmith > 0) {
	        	availableActions.add(ActionType.LISTEN);
	            return;
			}
	        if (hasMerchant > 0) {
	        	availableActions.add(ActionType.LISTEN);
	            return;
			}
	        if (hasThief > 0) {
	        	availableActions.add(ActionType.LISTEN);
	            return;
			}
	        
	        if (hasNPC > 0){
	            availableActions.add(ActionType.LISTEN);
	            //availableActions.add(ActionType.REPORT);
	            //availableActions.add(ActionType.ESCORT);
	            return;
	        }
		}
    }
    
    public void checkForAvailableActions(Action action, TileTypes tempTileType, TileTypes activeNpc, TileTypes rememberedNpc){
        availableActions.clear();
        final int hasNpc = owner.getAllNpcs();
        if (checkIfLastActionWasReport()) {
        	if (rememberedNpc != TileTypes.NONE) {
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
				decideWhatActions(activeNpc);
			}
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
                } else if (prev.GetType().isKnight()) {
                    owner.removeKnight(new KnightTile(prev), update.getRoom());
                } else if (prev.GetType().isWizard()) {
                    owner.removeWizard(new WizardTile(prev), update.getRoom());
                } else if (prev.GetType().isDruid()) {
                    owner.removeDruid(new DruidTile(prev), update.getRoom());
                } else if (prev.GetType().isBountyhunter()) {
                    owner.removeBountyhunter(new BountyhunterTile(prev), update.getRoom());
                } else if (prev.GetType().isBlacksmith()) {
                    owner.removeBlacksmith(new BlacksmithTile(prev), update.getRoom());
                } else if (prev.GetType().isMerchant()) {
                    owner.removeMerchant(new MerchantTile(prev), update.getRoom());
                } else if (prev.GetType().isThief()) {
                    owner.removeThief(new ThiefTile(prev), update.getRoom());
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
                } else if (next.GetType().isKnight()) {
                    owner.addKnight(new KnightTile(next), update.getRoom());
				} else if (next.GetType().isWizard()) {
                    owner.addWizard(new WizardTile(next), update.getRoom());
				} else if (next.GetType().isDruid()) {
                    owner.addDruid(new DruidTile(next), update.getRoom());
				} else if (next.GetType().isBountyhunter()) {
                    owner.addBountyhunter(new BountyhunterTile(next), update.getRoom());
				} else if (next.GetType().isBlacksmith()) {
                    owner.addBlacksmith(new BlacksmithTile(next), update.getRoom());
				} else if (next.GetType().isMerchant()) {
                    owner.addMerchant(new MerchantTile(next), update.getRoom());
				} else if (next.GetType().isThief()) {
                    owner.addThief(new ThiefTile(next), update.getRoom());
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
    private boolean checkIfActionIsCivilian()
    {
    	if (actions.get(actions.size() - 1).getType() == ActionType.DEFEND) {
			return true;
		}
    	return false;
    }
    
    private void decideWhatActions(TileTypes tempTileType)
    {
        final int hasItem = owner.getItems().size();
        final int hasEnemies = owner.getEnemies().size() + owner.getBossesPositions().size();
        final int hasTreasures = owner.getTreasures().size();
        final int hasCivilians = owner.getThiefs().size() + owner.getMerchants().size() + owner.getBlacksmiths().size();
        
    	if (tempTileType == TileTypes.KNIGHT) {
			if (hasCivilians > 0){
	            availableActions.add(ActionType.DEFEND);
	        }
			if (hasEnemies > 0) {
	            availableActions.add(ActionType.DAMAGE);
	            availableActions.add(ActionType.KILL);
	            availableActions.add(ActionType.CAPTURE);
			}
		}
		else if (tempTileType == TileTypes.WIZARD) {
			if (hasItem > 0){
	            availableActions.add(ActionType.EXPERIMENT);
	            availableActions.add(ActionType.READ);
	            availableActions.add(ActionType.USE);
	        }
		}
		else if (tempTileType == TileTypes.DRUID) {
			if (hasItem > 0){
	            availableActions.add(ActionType.EXPERIMENT);
	            availableActions.add(ActionType.USE);
			}
			if (hasTreasures > 0) {
	            availableActions.add(ActionType.GATHER);
			}
				
		}
		else if (tempTileType == TileTypes.BOUNTYHUNTER) {
			if (hasEnemies > 0 || hasCivilians > 0) {
				availableActions.add(ActionType.SPY);
				availableActions.add(ActionType.STEALTH);
			}
			if (hasItem > 0 && hasCivilians > 0) {
				availableActions.add(ActionType.TAKE);
			}
			if (hasEnemies > 0) {
				availableActions.add(ActionType.KILL);
				availableActions.add(ActionType.CAPTURE);
			}
		}
		else if (tempTileType == TileTypes.BLACKSMITH) {
			if (hasItem > 0) {
				availableActions.add(ActionType.REPAIR);
			}
			if (hasTreasures > 0) {
				availableActions.add(ActionType.GATHER);
			}
			availableActions.add(ActionType.ESCORT);
		}
		else if (tempTileType == TileTypes.MERCHANT) {
			if (hasItem > 0 && hasCivilians > 0) {
				availableActions.add(ActionType.EXCHANGE);
			}
			availableActions.add(ActionType.ESCORT);
		}
		else if (tempTileType == TileTypes.THIEF) {
			if (hasItem > 0 && hasCivilians > 0) {
				availableActions.add(ActionType.TAKE);
				availableActions.add(ActionType.GIVE);
			}
		}
    }
}
