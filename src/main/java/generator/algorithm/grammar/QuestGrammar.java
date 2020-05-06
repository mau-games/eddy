package generator.algorithm.grammar;

import com.google.gson.*;
import game.Dungeon;
import game.Tile;
import game.quest.Action;
import game.quest.ActionType;
import game.quest.ActionWithSecondPosition;
import game.quest.Quest;
import game.quest.actions.*;
import util.eventrouting.events.QuestPositionUpdate;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class QuestGrammar {
    public enum QuestMotives {
        KNOWLEDGE,
        COMFORT,
        REPUTATION,
        PROTECTION,
        CONQUEST,
        WEALTH,
        ABILITY,
        EQUIPMENT
    }

    private final static String defaultConfig = "config/quest_rules.json";
    private static Map<String, List<List<String>>> rules = new LinkedHashMap<>();
    private Dungeon owner;
    public final static String START_VALUE = "<QUEST>";
    public final static String[] Motives = {"<KNOWLEDGE>","<COMFORT>","<REPUTATION>","<PROTECTION>","<CONQUEST>","<WEALTH>","<ABILITY>","<EQUIPMENT>"};
    private final Random random = new Random();
    public QuestGrammar(Dungeon dungeon) {
        this.owner = dungeon;
        init();
    }

    private void init() {
        String toParse = readFile(defaultConfig);
        parseJSONAndPopulateRules(toParse);
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
            boolean available = availableOptions.stream()
                    .map(Enum::toString)
                    .anyMatch(s -> s.equals(value.toUpperCase())) ||
                    value.startsWith("<");
            if (rules.containsKey(value.toUpperCase())
                    && available){
                List<String> nextValues = pickRandom(rules.get(value));
                int finalStart = start;
                nextValues.forEach(s -> expand(toExpand, s, availableOptions, finalStart, limit));
            } else if (available){
                Action action = createActionOnType(value);
                toExpand.addActions(action);
            }
        }
        //check limit and save or scrap

        //check
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
            case DAMAGE:
                action = new DamageAction();
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
            case DEFEND:
                action = new DefendAction();
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
            case ESCORT:
                action = new EscortAction();
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
                tiles.addAll(owner.getNpcs());
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
                tiles.addAll(owner.getNpcs());
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
            case SPY:
                action = new SpyAction();
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
                tiles.addAll(owner.getNpcs());
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
//        else System.out.println(value);
        return action;
    }

    private String readFile(String fileName){
        String toReturn = "";
        try {
            ClassLoader loader = QuestGrammar.class.getClassLoader();
            BufferedReader br = new BufferedReader(new FileReader(loader.getResource(fileName).getFile()));
            StringBuilder builder = new StringBuilder();
            String line = br.readLine();
            while (line != null){
                builder.append(line);
                line = br.readLine();
            }
            toReturn = builder.toString();
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

    public static void main(String[] args) throws IOException {
        Dungeon dungeon = new Dungeon();
        QuestGrammar grammar = new QuestGrammar(dungeon);

        List<Map<String, Float>> maps = new LinkedList<>();
        List<ActionType> availableOptions = new LinkedList<>();
        availableOptions.add(ActionType.CAPTURE);
        availableOptions.add(ActionType.DAMAGE);
        availableOptions.add(ActionType.DEFEND);
        availableOptions.add(ActionType.ESCORT);
        availableOptions.add(ActionType.EXCHANGE);
        availableOptions.add(ActionType.EXPERIMENT);
        availableOptions.add(ActionType.EXPLORE);
        availableOptions.add(ActionType.GATHER);
        availableOptions.add(ActionType.GIVE);
        availableOptions.add(ActionType.GO_TO);
        availableOptions.add(ActionType.KILL);
        availableOptions.add(ActionType.LISTEN);
        availableOptions.add(ActionType.READ);
        availableOptions.add(ActionType.REPAIR);
        availableOptions.add(ActionType.REPORT);
        availableOptions.add(ActionType.SPY);
        availableOptions.add(ActionType.TAKE);
        availableOptions.add(ActionType.STEALTH);
        availableOptions.add(ActionType.USE);

        int limitLength = 150;//750
        Quest quest;
        while (maps.size() < 500000) {
            try{
                quest = new Quest();
                grammar.expand(quest, QuestGrammar.START_VALUE, availableOptions, 0 , limitLength);
                maps.add(QuestAnalyser.mapQuestActions(quest));
            } catch (StackOverflowError e){
                System.out.println("StackOverFlow");
            }
        }

        //analyse all quests with heat map
        Map<Integer, List<Map<String, Float>>> mapQuestActionMaps = QuestAnalyser.mapQuestActionMaps(maps, limitLength + 1);
        QuestAnalyser.analyseMapQuestActionMaps(mapQuestActionMaps).forEach((integer, map) -> {
            System.out.println(integer);
            map.forEach((s, integer1) -> System.out.println(s + " : " + integer1));
            System.out.println();
        });

//        for (int i = 0; i < 1000; i++) {
//            Quest quest = new Quest();
//            try {
//                grammar.expand(quest, QuestGrammar.START_VALUE);
////                quest.getActions().forEach(action -> System.out.print(action.getType() + "-"));
//                System.out.println();
//                //mapping the quest
//            } catch (StackOverflowError e){
//                System.out.println("StackOverFlow");
//            }
//            System.out.println();
//            QuestAnalyser.mapQuestActions(quest).forEach((s, integer) -> System.out.println(s + " : " + integer));
//            //analyse all quests
//        }
    }
}
