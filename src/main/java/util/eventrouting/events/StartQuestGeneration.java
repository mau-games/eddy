package util.eventrouting.events;

import game.quest.Quest;
import generator.algorithm.grammar.QuestGrammar;
import util.eventrouting.PCGEvent;

public class StartQuestGeneration extends PCGEvent {

    private final Quest quest;
    private final QuestGrammar grammar;
    private final int limit;

    public StartQuestGeneration(Quest quest, QuestGrammar questGrammar, int limit) {
        this.quest = quest;
        this.grammar = questGrammar;
        this.limit = limit;
    }

    public Quest getQuest() {
        return quest;
    }

    public QuestGrammar getGrammar() {
        return grammar;
    }

    public int getLimit() {
        return limit;
    }
}
