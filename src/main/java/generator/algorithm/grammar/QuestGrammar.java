package generator.algorithm.grammar;

import com.google.gson.*;
import game.Dungeon;
import game.Room;
import game.Tile;
import game.TileTypes;
import game.quest.Action;
import game.quest.ActionType;
import game.quest.ActionWithSecondPosition;
import game.quest.Quest;
import game.quest.actions.*;
import game.tiles.BountyhunterTile;
import game.tiles.CivilianTile;
import game.tiles.MageTile;
import game.tiles.NpcTile;
import game.tiles.SoldierTile;
import generator.algorithm.grammar.QuestGrammar.QuestMotives;
import util.eventrouting.events.QuestPositionUpdate;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sql.rowset.spi.SyncResolver;

import org.apache.commons.io.IOUtils;

public class QuestGrammar {
    public enum QuestMotives {
        KNOWLEDGE,
        COMFORT,
        REPUTATION,
        SERENITY,
        PROTECTION,
        CONQUEST,
        WEALTH,
        ABILITY,
        EQUIPMENT,
        NONE
    }
    public enum NPCTypes{
    	SOLDIER,
    	MAGE,
    	BOUNTYHUNTER,
    	CIVILIAN
    }

    private final static String defaultConfig = "config/npc_rules.json";
    private static Map<String, List<List<String>>> rules = new LinkedHashMap<>();
    private Dungeon owner;
    
    private Stack<QuestPositionUpdate> npcStack;
    private Stack<QuestPositionUpdate> civilianStack;
    
    private float[] perfectMotiveBalance;
    private Quest quest;
    
    public final static String START_VALUE = "<QUEST>";
    public final static String[] Motives = {"<KNOWLEDGE>","<COMFORT>","<REPUTATION>","<SERENITY>","<PROTECTION>","<CONQUEST>","<WEALTH>","<ABILITY>","<EQUIPMENT>"};
    private final Random random = new Random();
    public QuestGrammar(Dungeon dungeon) {
        this.owner = dungeon;
        init();
    }

    private void init() {
        String toParse = readFile(defaultConfig);
        parseJSONAndPopulateRules(toParse);
        perfectMotiveBalance = new float[9];
        SetPerfectMotiveBalance();
        
        npcStack = new Stack<QuestPositionUpdate>();
        civilianStack = new Stack<QuestPositionUpdate>();
    }

    public Quest expand(Quest toExpand, String value){
        if (rules.containsKey(value.toUpperCase())){
            List<String> nextValues = pickRandom(rules.get(value));
            nextValues.forEach(s -> expand(toExpand, s));
        } else {
            Action action = createActionOnType(value);
            toExpand.addActions(action);
        }
        return toExpand;
    }

    public void expand(final Quest toExpand, final String value, final List<ActionType> availableOptions, int start, int limit){
//        System.out.println(value);
//        System.out.println(start);
//        System.out.println(toExpand.size());
    	
        if (start++ <= limit && toExpand.getActions().size() < limit) {
        	synchronized (availableOptions) {
                boolean available = Available(availableOptions, value);
                if (rules.containsKey(value.toUpperCase())
                        && available){
                    List<String> nextValues = pickRandom(rules.get(value));
                    int finalStart = start;
                    synchronized (availableOptions) {
                        nextValues.forEach(s -> expand(toExpand, s, availableOptions, finalStart, limit));
    				}
                } else if (available){
                    Action action = createActionOnType(value);
                    toExpand.addActions(action);
                }
			}
            
        }
        //check limit and save or scrap

        //check
    }
    
    private boolean Available(final List<ActionType> availableOptions, final String value)
    {
    	synchronized (availableOptions) {
    		if (availableOptions.isEmpty()) {
				System.out.println("availableOptions was null");
			}
    		else {
    			return availableOptions.stream()
    		            .map(Enum::toString)
    		            .anyMatch(s -> s.equals(value.toUpperCase())) ||
    		            value.startsWith("<");
			}
    		return false;
		}
    }

    /**
     * To create context for generated actions
     * @param value name of action
     * @return action
     */
    private Action createActionOnType(String value) {
        Action action = null;
        List<QuestPositionUpdate> tiles = new ArrayList<QuestPositionUpdate>();
        switch (ActionType.valueOf(value.toUpperCase())){
            case CAPTURE:
                action = new CaptureAction();
                tiles.addAll(owner.getEnemies());
                tiles.addAll(owner.getBossesPositions());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    Tile tile = position.getRoom().getTile(action.getPosition().getX(),action.getPosition().getY());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case DAMAGE:
                action = new DamageAction();
                tiles.addAll(owner.getItems());
                tiles.addAll(owner.getEnemies());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case DEFEND:
                action = new DefendAction();
                tiles.addAll(owner.getCivilians());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case ESCORT:
                action = new EscortAction();
                tiles.addAll(owner.getCivilians());
                //owner.getAllRooms().forEach(room -> tiles.addAll(room.getWalkablePositions()));
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                	QuestPositionUpdate position = tiles.get(0);
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case EXCHANGE: //needs 2 positions
                action = new ExchangeAction();
                tiles.addAll(owner.getItems());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                tiles.clear();
                tiles.addAll(owner.getCivilians());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    ((ActionWithSecondPosition)action).setSecondPosition(position.getPoint());
                    ((ActionWithSecondPosition)action).setSecondRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    ((ActionWithSecondPosition)action).setSecondPosition(tiles.get(0).getPoint());
                    ((ActionWithSecondPosition)action).setSecondRoom(tiles.get(0).getRoom());
                }
                break;
            case EXPERIMENT:
                action = new ExperimentAction();
                tiles.addAll(owner.getItems());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case EXPLORE:
                action = new ExploreAction();
                owner.getAllRooms().forEach(room -> tiles.addAll(room.getWalkablePositions()));
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case GATHER:
                action = new GatherAction();
                tiles.addAll(owner.getTreasures());
                tiles.addAll(owner.getItems());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case GIVE:
                action = new GiveAction();
                tiles.addAll(owner.getItems());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                tiles.clear();
                tiles.addAll(owner.getCivilians());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    ((ActionWithSecondPosition)action).setSecondPosition(position.getPoint());
                    ((ActionWithSecondPosition)action).setSecondRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    ((ActionWithSecondPosition)action).setSecondPosition(tiles.get(0).getPoint());
                    ((ActionWithSecondPosition)action).setSecondRoom(tiles.get(0).getRoom());
                }
                break;
            case GO_TO:
                action = new GotoAction();
                owner.getAllRooms().forEach(room -> tiles.addAll(room.getWalkablePositions()));
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case KILL:
                action = new KillAction();
                tiles.addAll(owner.getEnemies());
                tiles.addAll(owner.getBossesPositions());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case LISTEN:
                action = new ListenAction();
                if (civilianStack.size() != 0) {
					tiles.add(civilianStack.peek());
				}
                else {
                	NPCTypes temp = DecideRecommended();
                	
                	switch (temp) {
					case SOLDIER:
						tiles.addAll(owner.getSoldiers());
						break;
					case MAGE:
						tiles.addAll(owner.getMages());
						break;
					case BOUNTYHUNTER:
						tiles.addAll(owner.getBountyHunters());
						break;
					case CIVILIAN:
						tiles.addAll(owner.getCivilians());
						break;
					default:
						break;
					}
                    
				}
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                	QuestPositionUpdate position = tiles.get(0);
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case READ:
                action = new ReadAction();
                tiles.addAll(owner.getItems());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case REPAIR:
                action = new RepairAction();
                tiles.addAll(owner.getItems());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case REPORT:
                action = new ReportAction();
                if (npcStack.size() != 0) {
                    tiles.add(npcStack.peek());
				}
                if (tiles != null) {
                	if (tiles.size() != 0) {
                		QuestPositionUpdate position = tiles.get(0);
                        action.setPosition(position.getPoint());
                        action.setRoom(position.getRoom());
					}
				}
                break;
            case SPY:
                action = new SpyAction();
                tiles.addAll(owner.getEnemies());
                tiles.addAll(owner.getBossesPositions());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case STEALTH:
                action = new StealthAction();
                tiles.addAll(owner.getEnemies());
                tiles.addAll(owner.getBossesPositions());
                tiles.addAll(owner.getNpcs());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case STEAL:
                action = new StealAction();
                tiles.addAll(owner.getTreasures());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
            case TAKE:
                action = new TakeAction();
                tiles.addAll(owner.getItems());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                tiles.clear();
                //tiles.addAll(owner.getNpcs());
                tiles.addAll(owner.getCivilians());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    ((ActionWithSecondPosition)action).setSecondPosition(position.getPoint());
                    ((ActionWithSecondPosition)action).setSecondRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    ((ActionWithSecondPosition)action).setSecondPosition(tiles.get(0).getPoint());
                    ((ActionWithSecondPosition)action).setSecondRoom(tiles.get(0).getRoom());
                }
                break;
            case USE:
                action = new UseAction();
                tiles.addAll(owner.getItems());
                if (tiles.size() > 1) {
                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
                    action.setPosition(position.getPoint());
                    action.setRoom(position.getRoom());
                    //needs room
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).getPoint());
                    action.setRoom(tiles.get(0).getRoom());
                }
                break;
        }
        if (action != null) action.setId(UUID.randomUUID());
//      System.out.println(value);
        return action;
    }

    private String readFile(String fileName){
        String toReturn = "";
        try {
            ClassLoader loader = QuestGrammar.class.getClassLoader();           
            toReturn = IOUtils.toString(this.getClass().getResourceAsStream("/" + fileName), StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    private void parseJSONAndPopulateRules(String toParse) {
//        Gson gson = new Gson();
//        Type rulesMapType = new TypeToken<Map<String,List<List<String>>>>() {}.getType();
//        rules = gson.fromJson(toParse, rulesMapType);

        JsonObject jsonObject = new JsonParser().parse(toParse).getAsJsonObject();
        Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
        for (Map.Entry<String, JsonElement> entry: entrySet) {
            String key = entry.getKey().toUpperCase();
            JsonArray jsonArray = entry.getValue().getAsJsonArray();
            List<List<String>> values = new ArrayList<>();
            jsonArray.forEach(jsonElement -> {
                List<String> sequence = new ArrayList<>();
                jsonElement.getAsJsonArray().forEach(stringElement -> {
                    sequence.add(stringElement.getAsString().toUpperCase());
                });
                values.add(sequence);
            });
            rules.put(key, values);
        }
    }

    private List<String> pickRandom(List<List<String>> listOfLists){
        int size = listOfLists.size();
        return listOfLists.get(random.nextInt(size));
    }

    public List<List<String>> getAllRuleSequences(){
        List<List<String>> listOfList = new ArrayList<>();
        rules.values().forEach(listOfList::addAll);
        return listOfList;
    }
    
    public void setStacks(Stack<TileTypes> npcStack, Stack<TileTypes> civilianStack, 
    		Stack<finder.geometry.Point> npcPosition, Stack<finder.geometry.Point> civilianPosition, 
    		Stack<Room> npcRooms, Stack<Room> civilianRooms, Quest quest)
    {
    	
    	Stack<QuestPositionUpdate> tempNpc = new Stack<QuestPositionUpdate>();
    	Stack<QuestPositionUpdate> tempCivilian = new Stack<QuestPositionUpdate>();
    	
    	for (int i = 0; i < npcPosition.size(); i++) {
			int x = npcPosition.get(i).getX();
			int y = npcPosition.get(i).getY();
			
			finder.geometry.Point point = new finder.geometry.Point(x,y);
			
			tempNpc.add(new QuestPositionUpdate(point, npcRooms.get(i), false));
		}
    	for (int i = 0; i < civilianPosition.size(); i++) {
    		int x = civilianPosition.get(i).getX();
			int y = civilianPosition.get(i).getY();
			
			finder.geometry.Point point = new finder.geometry.Point(x,y);
			
			tempCivilian.add(new QuestPositionUpdate(point, civilianRooms.get(i), false));
		}
    	
    	this.npcStack = tempNpc;
    	this.civilianStack = tempCivilian;
    	this.quest = quest;
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
    
    private NPCTypes DecideRecommended()
	{
		float[] motiveArray = new float[9];
		if (quest != null) {
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
		
		List<NpcTile> tempTiles = ExistingNpcs();
		
		float[] NPCMotivesWeight = new float[tempTiles.size()];
		for (int i = 0; i < NPCMotivesWeight.length; i++) {
			NPCMotivesWeight[i] = 0;
		}
		
		for (int i = 0; i < tempTiles.size(); i++) {
			List<QuestMotives> npcQuestMotives = new ArrayList<QuestMotives>();
			npcQuestMotives = tempTiles.get(i).ReturnMotives();
			
			for (int j = 0; j < npcQuestMotives.size(); j++) {
				float currentWeight = blast(npcQuestMotives.get(j), motiveArray);
				
				NPCMotivesWeight[i] += currentWeight;
			}
		}
		
		int recommendedNpcIndex = 0;
		float currentChoice = NPCMotivesWeight[0];
		for (int i = 1; i < NPCMotivesWeight.length; i++) {
			if (NPCMotivesWeight[i] >= currentChoice) {
				currentChoice = NPCMotivesWeight[i];
				recommendedNpcIndex = i;
			}
		}
		
		NPCTypes temp = NPCTypes.SOLDIER;
		
		if ((tempTiles.get(recommendedNpcIndex)) instanceof SoldierTile) {
			temp = NPCTypes.SOLDIER;
		} else if ((tempTiles.get(recommendedNpcIndex)) instanceof MageTile) {
			temp = NPCTypes.MAGE;
		} else if ((tempTiles.get(recommendedNpcIndex)) instanceof BountyhunterTile) {
			temp = NPCTypes.BOUNTYHUNTER;
		} else if ((tempTiles.get(recommendedNpcIndex)) instanceof CivilianTile) {
			temp = NPCTypes.CIVILIAN;
		}
		
		return temp;
	}
    
    private List<NpcTile> ExistingNpcs()
    {
    	List<NpcTile> tempTiles = new ArrayList<NpcTile>();
    	
    	if (owner.getSoldiers().size() != 0) {
    		SoldierTile tempSoldier = new SoldierTile();
    		tempTiles.add(tempSoldier);
		}
    	if (owner.getMages().size() != 0) {
    		MageTile tempMage = new MageTile();
    		tempTiles.add(tempMage);
		}
    	if (owner.getBountyHunters().size() != 0) {
    		BountyhunterTile tempBountyhunter = new BountyhunterTile();
    		tempTiles.add(tempBountyhunter);
		}
    	if (owner.getCivilians().size() != 0) {
    		CivilianTile tempCivilian = new CivilianTile();
    		tempTiles.add(tempCivilian);
    	}
		
		return tempTiles;
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
		default:
			break;
		}
		return startValue;
	}

    public static void main(String[] args) throws IOException {
        Dungeon dungeon = new Dungeon();
        QuestGrammar grammar = new QuestGrammar(dungeon);

        List<Map<String, Float>> maps = new LinkedList<>();
        List<ActionType> availableOptions = new LinkedList<>();
        availableOptions.add(ActionType.LISTEN);

        int limitLength = 146;//averageLimit: 146
        int questResolution = 60000000; //5000000
//        int averageTOBElimit = 0;
//        for (int i = 0; i < 1000; i++) {
        Quest quest;
        int progressPercentage = 0;
        while (maps.size() < questResolution) {
            try{
                quest = new Quest();
                synchronized (availableOptions) {
                    grammar.expand(quest, QuestGrammar.START_VALUE, availableOptions, 0 , limitLength);
				}
                maps.add(QuestAnalyser.mapQuestActions(quest));
            } catch (StackOverflowError e){
                System.out.println("StackOverFlow");
            }
            int currentPercentage  = (maps.size() * 100) / (questResolution);
            if (progressPercentage < currentPercentage){
                progressPercentage = currentPercentage;
                System.out.println(progressPercentage);
            }
        }
        System.out.println("Quests generated and mapped");

        //analyse all quests with heat map
        Map<Integer, List<Map<String, Float>>> mapQuestActionMaps = QuestAnalyser.mapQuestActionMaps(maps, limitLength + 1);
        Map<Integer, Map<String, Float>> analysedMapQuestActionMaps = QuestAnalyser.analyseMapQuestActionMaps(mapQuestActionMaps);
        System.out.println("Quests re-mapped and analysed");
//        AtomicInteger highest = new AtomicInteger();
//        analysedMapQuestActionMaps.forEach((integer, map) -> {
//            System.out.println(integer);
//            map.forEach((s, integer1) -> System.out.println(s + " : " + integer1));
//            System.out.println();
//            if (integer != limitLength && map.size() > 0 && integer > highest.get()){
//                highest.set(integer);
//            }
//        });
//            System.out.println(highest.get());
//            averageTOBElimit += highest.get();
//            maps.clear();
//        }
//        System.out.println(averageTOBElimit/1000);

        System.out.println("generating heat-map");
        Map<String, String[]> heatMap = QuestAnalyser.mapToHeatMap(analysedMapQuestActionMaps);
        StringBuilder builder = new StringBuilder();
        builder.append("NAME");
        builder.append("\t");
        for (String string : heatMap.get("NAME")) {
            builder.append(string);
            builder.append("\t");
        }
        builder.append("\n");
        heatMap.forEach((s, strings) -> {
            if (!s.equals("NAME")) {
                builder.append(s);
                builder.append("\t");
                for (String string : strings) {
                    builder.append(string);
                    builder.append("\t");
                }
                builder.append("\n");
            }
        });

        System.out.println(builder.toString());

        //print to file
//        BufferedWriter writer = null;
//        try {
//            Date date = new Date();
//            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
//            File file = new File("expressive_range_" +
//                    limitLength + "_limit_" + questResolution + "_resolution_" +
//                    "_quest_" + format.format(date) + ".txt");
//            file.createNewFile();
//            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("expressive_range_" +
//                    limitLength + "_limit_" + questResolution + "_resolution_" +
//                    "_quest_" + format.format(date) + ".txt"),"utf-8"));
//            writer.write(builder.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (writer != null){
//                writer.close();
//            }
//        }
//
    }
}
