package game.quest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Eric Grevillius
 * @author Elin Olsson
 */
public class Quest {
    private List<Action> actions;

    public Quest() {
        this.actions = new ArrayList<Action>();
    }

    private Quest(Quest quest) {
        this.actions = quest.getActions();
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

    public Quest copy(){
        return new Quest(this);
    }

}
