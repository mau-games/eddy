package generator.algorithm.grammar;

import game.quest.Action;
import game.quest.ActionType;
import game.quest.Quest;
import game.quest.actions.CaptureAction;
import game.quest.actions.DamageAction;
import game.quest.actions.DefendAction;
import game.quest.actions.EscortAction;
import game.quest.actions.ExperimentAction;
import game.quest.actions.GatherAction;
import game.quest.actions.KillAction;
import game.quest.actions.ListenAction;
import game.quest.actions.ReadAction;
import game.quest.actions.RepairAction;
import game.quest.actions.ReportAction;
import game.quest.actions.SpyAction;
import game.quest.actions.UseAction;
import game.tiles.BountyhunterTile;
import game.tiles.CivilianTile;
import game.tiles.MageTile;
import game.tiles.NpcTile;
import game.tiles.SoldierTile;
import generator.algorithm.grammar.QuestGrammar.QuestMotives;
import javafx.application.Platform;
import util.eventrouting.EventRouter;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.QuestActionSuggestionUpdate;
import util.eventrouting.events.QuestGenerationConfigUpdate;
import util.eventrouting.events.QuestSuggestionUpdate;
import util.eventrouting.events.QuestSuggestionsDone;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.Iterator;
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
    private float[] perfectMotiveBalance;

    public QuestGenerator(Quest quest, QuestGrammar grammar, int questLimit) {
        this.quest = quest;
        this.grammar = grammar;
        this.limit = questLimit;
        this.availableActions = quest.getAvailableActions();
        suggestedQuests = new ArrayList<>();
        suggestedActions = new ArrayList<>();
        perfectMotiveBalance = new float[9];
		SetPerfectMotiveBalance();
        EventRouter.getInstance().registerListener(this::ping, new QuestGenerationConfigUpdate());
    }
    
    private void SetPerfectMotiveBalance()
	{
		perfectMotiveBalance[0] = 0.183f; //Knowledge
		perfectMotiveBalance[1] = 0.016f; //Comfort
		perfectMotiveBalance[2] = 0.065f; //Reputation
		perfectMotiveBalance[3] = 0.137f; //Serenity
		perfectMotiveBalance[4] = 0.182f; //Protection
		perfectMotiveBalance[5] = 0.202f; //Conquest
		perfectMotiveBalance[6] = 0.02f;  //Wealth
		perfectMotiveBalance[7] = 0.011f; //Ability
		perfectMotiveBalance[8] = 0.185f; //Equipment
		
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
        } else {
            //TODO: generate a new quest at the end of the current one
        }
    }

    public synchronized void updateQuest(Quest quest) {
        synchronized (this.quest) {
            this.quest = quest;
            suggestedQuests.clear();
            suggestedActions.clear();
        }
    }

    public synchronized void updateGlobalQuestIndex(int index){
        this.globalQuestIndex = index;
        suggestedActions.clear();
        extractAndCompressActions(suggestedQuests);
        broadcastQuestActionSuggestionUpdate();
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

    protected synchronized void broadcastQuestActionSuggestionUpdate(){
//        System.out.println("Broadcast QuestSuggestionUpdate");
//        long start = System.currentTimeMillis();
        QuestActionSuggestionUpdate update = new QuestActionSuggestionUpdate();
        suggestedActions.forEach(update::add);
        EventRouter.getInstance().postEvent(update);
//        System.out.println("broadcastQuestActionSuggestionUpdate: " + (System.currentTimeMillis() - start) + "ms");
    }

    /**
     * Starts the algorithm. Called when the thread starts.
     */
    public void run() {
        try {
            System.out.println(this + " running ... ");
            List<Quest> temporaryList = new ArrayList<Quest>();
            while (!stop) {
                temporaryList.clear();
                //start generating new suggestions for each motive
                for (int i = 0; i < QuestGrammar.Motives.length; i++) {
                    Quest quest = new Quest();
//                    while (quest.getActions().isEmpty()) {
                        grammar.expand(quest, QuestGrammar.Motives[i], availableActions, 0, limit);
//                    }
                    temporaryList.add(quest);
                }

                //Filter
                synchronized (quest) {
                    temporaryList = temporaryList.stream().filter(quest -> quest.startsWith(quest)).collect(Collectors.toList());
                    quest.notifyAll();
                }

                //Save if unique
                AtomicBoolean unique = new AtomicBoolean(false);
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
                    if (!temporaryList.isEmpty()){
                        extractAndCompressActions(temporaryList);
                        if (suggestedActions.size() != 0) {
                            trimSuggestedActions();
                        }
                        broadcastQuestActionSuggestionUpdate();
                        Thread.sleep(BROADCAST_TIMEOUT);
                    }
                }
            }
            EventRouter.getInstance().postEvent(new QuestSuggestionsDone(this));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void extractAndCompressActions(List<Quest> quests) {
        for (int i = 0; i < quests.size(); i++) {
            int index = i;
            //merge duplicates
            for (int j = 0; j < quests.get(index).getActions().size(); j++) {
            	int indexj = j;
            	boolean noneMatch = suggestedActions.stream()
                        .noneMatch(action ->
                                action.getType().getValue() ==
                                        quests.get(index).getAction(indexj).getType().getValue());
            	if (quests.get(i).getActions().get(j).getType() == ActionType.REPORT) {
            		noneMatch = suggestedActions.stream()
                            .noneMatch(action -> 
                            action.getPosition() == quests.get(index).getAction(indexj).getPosition());
				}
            	if (noneMatch){
                    //add generated suggested action
                    suggestedActions.add(quests.get(index).getAction(indexj));
                }
			}
        }
    }
    private void trimSuggestedActions()
    {
    	ActionType recommendedType = DecideRecommended();
    	
    	if (recommendedType != null) {
    		List<Action> tempSuggestList = new ArrayList<Action>();
        	for (int i = 0; i < suggestedActions.size(); i++) {
    			if (suggestedActions.get(i).getType() == recommendedType) {
    				tempSuggestList.add(suggestedActions.get(i));
    			}
    		}
        	if (tempSuggestList.size() == 2 && tempSuggestList.get(0).getType() == ActionType.REPORT && tempSuggestList.get(1).getType() == ActionType.REPORT) {
    			tempSuggestList.remove(0);
    		}
        	suggestedActions = tempSuggestList;
		}
    }
    private ActionType DecideRecommended()
	{
		float[] motiveArray = new float[9];
		for (int i = 0; i < quest.getActions().size(); i++) {
			if (quest.getActions().get(i).CheckMotives(QuestMotives.KNOWLEDGE)) {
				motiveArray[0]++;
			} if (quest.getActions().get(i).CheckMotives(QuestMotives.COMFORT)) {
				motiveArray[1]++;
			} if (quest.getActions().get(i).CheckMotives(QuestMotives.REPUTATION)) {
				motiveArray[2]++;
			} if (quest.getActions().get(i).CheckMotives(QuestMotives.SERENITY)) {
				motiveArray[3]++;
			} if (quest.getActions().get(i).CheckMotives(QuestMotives.PROTECTION)) {
				motiveArray[4]++;
			} if (quest.getActions().get(i).CheckMotives(QuestMotives.CONQUEST)) {
				motiveArray[5]++;
			} if (quest.getActions().get(i).CheckMotives(QuestMotives.WEALTH)) {
				motiveArray[6]++;
			} if (quest.getActions().get(i).CheckMotives(QuestMotives.ABILITY)) {
				motiveArray[7]++;
			} if (quest.getActions().get(i).CheckMotives(QuestMotives.EQUIPMENT)) {
				motiveArray[8]++;
			}
		}
		
		float amountOfMotives = 0;
		
		for (int i = 0; i < motiveArray.length; i++) {
			amountOfMotives += motiveArray[i];
		}
		
		for (int i = 0; i < motiveArray.length; i++) {
			if (amountOfMotives != 0) {
				motiveArray[i] = motiveArray[i] / amountOfMotives;
			}
		}
		
		List<Action> tempActions = SuggestedActions();
		
		if (tempActions.size() != 0) {
			float[] actionMotivesWeight = new float[tempActions.size()];
			for (int i = 0; i < actionMotivesWeight.length; i++) {
				actionMotivesWeight[i] = 0;
			}
			
			for (int i = 0; i < tempActions.size(); i++) {
				List<QuestMotives> actionQuestMotives = new ArrayList<QuestMotives>();
				actionQuestMotives = tempActions.get(i).ReturnMotives();
				
				for (int j = 0; j < actionQuestMotives.size(); j++) {
					float currentWeight = blast(actionQuestMotives.get(j), motiveArray);
					if (tempActions.get(i).getType() == ActionType.LISTEN) {
						currentWeight += 0.00001;
					}
					
					actionMotivesWeight[i] += currentWeight;
				}
			}
			
			
			
			int recommendedActionIndex = 0;
			float currentChoice = 0;
			if (actionMotivesWeight.length != 0) {
				currentChoice = actionMotivesWeight[0];
			}
			for (int i = 1; i < actionMotivesWeight.length; i++) {
				if (actionMotivesWeight[i] >= currentChoice) {
					currentChoice = actionMotivesWeight[i];
					recommendedActionIndex = i;
				}
			}
			
			return tempActions.get(recommendedActionIndex).getType();
		}
		return null;
	}
    private float blast(QuestMotives temp, float[] motiveArray)
	{
		float startValue = 0;
		switch (temp) {
		case KNOWLEDGE:
			startValue = perfectMotiveBalance[0] - motiveArray[0];
			break;
		case COMFORT:
			startValue = perfectMotiveBalance[1] - motiveArray[1];
			break;
		case REPUTATION:
			startValue = perfectMotiveBalance[2] - motiveArray[2];
			break;
		case SERENITY:
			startValue = perfectMotiveBalance[3] - motiveArray[3];
			break;
		case PROTECTION:
			startValue = perfectMotiveBalance[4] - motiveArray[4];
			break;
		case CONQUEST:
			startValue = perfectMotiveBalance[5] - motiveArray[5];
			break;
		case WEALTH:
			startValue = perfectMotiveBalance[6] - motiveArray[6];
			break;
		case ABILITY:
			startValue = perfectMotiveBalance[7] - motiveArray[7];
			break;
		case EQUIPMENT:
			startValue = perfectMotiveBalance[8] - motiveArray[8];
			break;
		case NONE:
		default:
			break;
		}
		return startValue;
	}
    private List<Action> SuggestedActions()
    {
    	List<Action> tempSuggestedActionList = new ArrayList<Action>();
    	
    	for (int i = 0; i < suggestedActions.size(); i++) {
    		if (suggestedActions.get(i).getType() == ActionType.CAPTURE) {
    			CaptureAction tempCapture = new CaptureAction();
    			tempSuggestedActionList.add(tempCapture);
			} else if (suggestedActions.get(i).getType() == ActionType.DAMAGE) {
				DamageAction tempDamage = new DamageAction();
				tempSuggestedActionList.add(tempDamage);
			} else if (suggestedActions.get(i).getType() == ActionType.DEFEND) {
				DefendAction tempDefend = new DefendAction();
				tempSuggestedActionList.add(tempDefend);
			} else if (suggestedActions.get(i).getType() == ActionType.ESCORT) {
				EscortAction tempEscort = new EscortAction();
				tempSuggestedActionList.add(tempEscort);
			} else if (suggestedActions.get(i).getType() == ActionType.EXPERIMENT) {
				ExperimentAction tempExperiment = new ExperimentAction();
				tempSuggestedActionList.add(tempExperiment);
			} else if (suggestedActions.get(i).getType() == ActionType.GATHER) {
				GatherAction tempGather = new GatherAction();
				tempSuggestedActionList.add(tempGather);
			} else if (suggestedActions.get(i).getType() == ActionType.KILL) {
				KillAction tempKill = new KillAction();
				tempSuggestedActionList.add(tempKill);
			} else if (suggestedActions.get(i).getType() == ActionType.LISTEN) {
				
				int listen = 0;
				
				for (int j = 0; j < availableActions.size(); j++) {
					if (availableActions.get(j) == ActionType.LISTEN) {
						listen++;
					}
				}
				
				if (listen != 0) {
					ListenAction tempListen = new ListenAction();
					tempSuggestedActionList.add(tempListen);
				}
			}else if (suggestedActions.get(i).getType() == ActionType.READ) {
				ReadAction tempRead = new ReadAction();
				tempSuggestedActionList.add(tempRead);
			} else if (suggestedActions.get(i).getType() == ActionType.REPAIR) {
				RepairAction tempRepair = new RepairAction();
				tempSuggestedActionList.add(tempRepair);
			} else if (suggestedActions.get(i).getType() == ActionType.REPORT) {
				ReportAction tempReport = new ReportAction();
				tempSuggestedActionList.add(tempReport);
			} else if (suggestedActions.get(i).getType() == ActionType.SPY) {
				SpyAction tempSpy = new SpyAction();
				tempSuggestedActionList.add(tempSpy);
			} else if (suggestedActions.get(i).getType() == ActionType.USE) {
				UseAction tempUse = new UseAction();
				tempSuggestedActionList.add(tempUse);
			}
		}
    	return tempSuggestedActionList;
    }
}