package util.eventrouting.events;

import game.quest.Quest;
import generator.algorithm.grammar.QuestGenerator;
import util.eventrouting.PCGEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class QuestSuggestionsDone extends PCGEvent {

    private QuestGenerator generator;

    public QuestSuggestionsDone(QuestGenerator generator) {
        this.generator = generator;
    }

    public QuestGenerator getGenerator() {
        return generator;
    }
}
