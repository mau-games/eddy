package game.quest.actions;

import java.util.ArrayList;
import java.util.List;

import game.Tile;
import game.quest.Action;
import game.quest.ActionType;
import generator.algorithm.grammar.QuestGrammar.QuestMotives;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class CaptureAction extends Action {
	List<QuestMotives> questMotiveList;
    public CaptureAction() {
        this.setType(ActionType.CAPTURE);
        AddQuestMotives();
    }
    public CaptureAction(boolean precondition) {
        super(precondition);
        AddQuestMotives();
    }

    public CaptureAction(ActionType type) {
        super(type);
        AddQuestMotives();
    }

    @Override
    public void checkConditions() {
        getRoom().isIntraFeasible();
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(
                getRoom().walkablePositions.stream().anyMatch(walkablePosition ->
                        walkablePosition.getPoint().getX() == getPosition().getX() &&
                        walkablePosition.getPoint().getY() == getPosition().getY()) &&
                        (tile.GetType().isEnemyBoss() ||
                        tile.GetType().isEnemy() ||
                        tile.GetType().isNPC())
        );
    }
    public List<QuestMotives> ReturnMotives()
    {
    	return questMotiveList;
    }
    @Override
    public boolean CheckMotives(QuestMotives temp)
    {
    	for (int i = 0; i < questMotiveList.size(); i++) {
			if (questMotiveList.get(i) == temp) {
				return true;
			}
		}
    	return false;
    }
    
    private void AddQuestMotives()
    {
        questMotiveList = new ArrayList<QuestMotives>();
        questMotiveList.add(QuestMotives.SERENITY);
    }
}
