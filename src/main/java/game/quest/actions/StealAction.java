package game.quest.actions;

import java.util.ArrayList;
import java.util.List;

import game.Tile;
import game.quest.Action;
import game.quest.ActionType;
import generator.algorithm.grammar.QuestGrammar.QuestMotives;

public class StealAction extends Action {

	List<QuestMotives> questMotiveList;
    public StealAction() {
        this.setType(ActionType.STEAL);
        AddQuestMotives();
    }

    public StealAction(boolean precondition) {
        super(precondition);
        AddQuestMotives();
    }

    public StealAction(ActionType type) {
        super(type);
        AddQuestMotives();
    }

    @Override
    public void checkConditions() {
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(
                tile.GetType().isEnemyBoss() ||
                        tile.GetType().isEnemy() ||
                        tile.GetType().isNPC()
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
        questMotiveList.add(QuestMotives.CONQUEST);
        questMotiveList.add(QuestMotives.EQUIPMENT);
    }

}
