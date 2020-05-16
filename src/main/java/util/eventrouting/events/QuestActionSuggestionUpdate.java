package util.eventrouting.events;

import game.quest.Action;
import util.eventrouting.PCGEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestActionSuggestionUpdate extends PCGEvent {
    private List<Action> actions = new ArrayList<>();

    public void add(Action... actions){
        this.actions.addAll(Arrays.asList(actions));
    }

    public List<Action> getActions() {
        return actions;
    }
}
