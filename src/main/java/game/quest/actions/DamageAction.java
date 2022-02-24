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
public class DamageAction extends Action {
	private List<QuestMotives> questMotiveList;
    public DamageAction() {
        this.setType(ActionType.DAMAGE);
        AddQuestMotives();

    }

    public DamageAction(boolean precondition) {
        super(precondition);
        AddQuestMotives();
    }

    public DamageAction(ActionType type) {
        super(type);
        AddQuestMotives();
    }

    @Override
    public void checkConditions() {
        Tile tile = getRoom().getTile(getPosition().getX(),getPosition().getY());
        setPrecondition(
                tile.GetType().isItem() ||
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
        questMotiveList.add(QuestMotives.SERENITY);
        questMotiveList.add(QuestMotives.PROTECTION);
        questMotiveList.add(QuestMotives.CONQUEST);
        
    }
}

