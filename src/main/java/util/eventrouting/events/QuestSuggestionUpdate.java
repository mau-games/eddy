package util.eventrouting.events;

import game.quest.Quest;
import util.eventrouting.PCGEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QuestSuggestionUpdate extends PCGEvent {
    private List<Quest> quests = new ArrayList<>();

    public void add(Quest... quests){
        this.quests.addAll(Arrays.asList(quests));
    }

    public List<Quest> getQuests() {
        return quests;
    }
}
