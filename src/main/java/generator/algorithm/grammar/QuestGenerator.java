package generator.algorithm.grammar;

import game.quest.Action;
import game.quest.ActionType;
import game.quest.Quest;
import javafx.application.Platform;
import util.eventrouting.EventRouter;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.QuestGenerationConfigUpdate;
import util.eventrouting.events.QuestSuggestionUpdate;
import util.eventrouting.events.QuestSuggestionsDone;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class QuestGenerator extends Thread {
    private final static int BROADCAST_TIMEOUT = 5000;
    private Quest quest;
    private final QuestGrammar grammar;
    private List<Quest> suggestedQuests;
    private List<Action> suggestedActions;
    private final int limit;
    private final List<ActionType> availableActions;
    private boolean stop = false;
    private int globalQuestIndex = 0;

    public QuestGenerator(Quest quest, QuestGrammar grammar, int questLimit) {
        this.quest = quest;
        this.grammar = grammar;
        this.limit = questLimit;
        this.availableActions = quest.getAvailableActions();
        suggestedQuests = new ArrayList<>();
        suggestedActions = new ArrayList<>();
        EventRouter.getInstance().registerListener(this::ping, new QuestGenerationConfigUpdate());
    }

    private void ping(PCGEvent e) {
        if (e instanceof QuestGenerationConfigUpdate){
            if(e.getPayload() instanceof Quest){
                System.out.println("Generator config update is quest");
                updateQuest((Quest) e.getPayload());
            } else if (e.getPayload().getClass().isInstance(1)){
                System.out.println("Generator config update is int");
                updateGlobalQuestIndex((Integer) e.getPayload());
            }
        }
    }

    public synchronized void updateQuest(Quest quest) {
        synchronized (this.quest) {
            this.quest = quest;
            suggestedQuests.clear();
        }
    }

    public synchronized void updateGlobalQuestIndex(int index){
        this.globalQuestIndex = index;
        suggestedActions.clear();
        extractAndCompressActions(suggestedQuests);
    }


    public void terminate(){
        stop = true;
    }

    protected synchronized void broadcastQuestSuggestionUpdate(){
        System.out.println("Broadcast QuestSuggestionUpdate");
        QuestSuggestionUpdate update = new QuestSuggestionUpdate();
        suggestedQuests.forEach(update::add);
        EventRouter.getInstance().postEvent(update);
    }

    /**
     * Starts the algorithm. Called when the thread starts.
     */
    public void run() {
        System.out.println(this + " running ... ");
        List<Quest> temporaryList = new ArrayList<Quest>();
        long startTime = System.currentTimeMillis(); //broadcast only once every second
        while (!stop) {
            temporaryList.clear();
            //start generating new suggestions for each motive
            for (int i = 0; i < QuestGrammar.Motives.length; i++) {
                Quest quest = new Quest();
                while (quest.getActions().isEmpty()) {
                    grammar.expand(quest, QuestGrammar.Motives[i], availableActions, 0, limit);
                }
                temporaryList.add(quest);
            }
            synchronized (quest) {
                temporaryList = temporaryList.stream().filter(quest -> quest.startsWith(quest)).collect(Collectors.toList());
            }

            AtomicBoolean unique = new AtomicBoolean(false);
//            List<Quest> finalTemporaryList = temporaryList;
            temporaryList.forEach(quest -> {
                if (suggestedQuests.isEmpty()){
                    unique.set(suggestedQuests.add(quest));
                } else {
                    boolean noneMatch = suggestedQuests.stream().anyMatch(quest::notEquals);
                    if (noneMatch){
                        unique.set(suggestedQuests.add(quest));
                    }
                }
            });
            if (unique.get()) {
                if (!suggestedQuests.isEmpty()){
                    long now = System.currentTimeMillis();
                    if ((now - startTime) > BROADCAST_TIMEOUT) { //broadcast only once every second
                        System.out.println(now - startTime);
                        extractAndCompressActions(temporaryList);
                        broadcastQuestSuggestionUpdate();
                        startTime = now;
                    }
                }
            }

        }
        EventRouter.getInstance().postEvent(new QuestSuggestionsDone(this));
    }

    private void extractAndCompressActions(List<Quest> quests) {
        for (int i = 0; i < quests.size(); i++) {
                int index = i;
                if (globalQuestIndex < quests.get(i).getActions().size()) {
                    //merge duplicates
                    if (suggestedActions.stream().noneMatch(action -> action.getName().equals(quests.get(index).getAction(globalQuestIndex).getName()))) {
                        //add generated suggestions buttons
                        suggestedActions.add(suggestedQuests.get(index).getAction(globalQuestIndex));
                    }
                }
            }
    }
}