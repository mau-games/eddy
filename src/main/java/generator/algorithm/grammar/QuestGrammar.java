package generator.algorithm.grammar;

import com.google.gson.*;
import game.Dungeon;
import game.quest.Action;
import game.quest.ActionType;
import game.quest.Quest;
import game.quest.actions.*;
import util.eventrouting.events.QuestPositionUpdate;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
        EQUIPMENT
    }

    private final static String defaultConfig = "config/quest_rules.json";
    private static Map<String, List<List<String>>> rules = new LinkedHashMap<>();
    private Dungeon owner;
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
//                tiles.addAll(owner.getEnemies());
//                tiles.addAll(owner.getBossesPositions());
//                tiles.addAll(owner.getNpcs());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case DAMAGE:
                action = new DamageAction();
//                tiles.addAll(owner.getItems());
//                tiles.addAll(owner.getNpcs());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case DEFEND:
                action = new DefendAction();
//                tiles.addAll(owner.getItems());
//                tiles.addAll(owner.getNpcs());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case ESCORT:
                action = new EscortAction();
//                tiles.addAll(owner.getNpcs());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case EXCHANGE: //needs 2 positions
                action = new ExchangeAction();
//                tiles.addAll(owner.getItems());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
//                tiles.clear();
//                tiles.addAll(owner.getNpcs());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    ((ActionWithSecondPosition)action).setSecondPosition(position.getPoint());
//                    ((ActionWithSecondPosition)action).setSecondRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    ((ActionWithSecondPosition)action).setSecondPosition(tiles.get(0).getPoint());
//                    ((ActionWithSecondPosition)action).setSecondRoom(tiles.get(0).getRoom());
//                }
                break;
            case EXPERIMENT:
                action = new ExperimentAction();
//                tiles.addAll(owner.getItems());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case EXPLORE:
                action = new ExploreAction();
//                owner.getAllRooms().forEach(room -> tiles.addAll(room.getWalkablePositions()));
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case GATHER:
                action = new GatherAction();
//                tiles.addAll(owner.getItems());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case GIVE:
                action = new GiveAction();
//                tiles.addAll(owner.getItems());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
//                tiles.clear();
//                tiles.addAll(owner.getNpcs());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    ((ActionWithSecondPosition)action).setSecondPosition(position.getPoint());
//                    ((ActionWithSecondPosition)action).setSecondRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    ((ActionWithSecondPosition)action).setSecondPosition(tiles.get(0).getPoint());
//                    ((ActionWithSecondPosition)action).setSecondRoom(tiles.get(0).getRoom());
//                }
                break;
            case GO_TO:
                action = new GotoAction();
//                owner.getAllRooms().forEach(room -> tiles.addAll(room.getWalkablePositions()));
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case KILL:
                action = new KillAction();
//                tiles.addAll(owner.getEnemies());
//                tiles.addAll(owner.getBossesPositions());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case LISTEN:
                action = new ListenAction();
//                tiles.addAll(owner.getNpcs());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case READ:
                action = new ReadAction();
//                tiles.addAll(owner.getItems());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case REPAIR:
                action = new RepairAction();
//                tiles.addAll(owner.getItems());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case REPORT:
                action = new ReportAction();
//                tiles.addAll(owner.getNpcs());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case SPY:
                action = new SpyAction();
//                tiles.addAll(owner.getEnemies());
//                tiles.addAll(owner.getBossesPositions());
//                tiles.addAll(owner.getNpcs());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case STEALTH:
                action = new StealthAction();
//                tiles.addAll(owner.getEnemies());
//                tiles.addAll(owner.getBossesPositions());
//                tiles.addAll(owner.getNpcs());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
                break;
            case TAKE:
                action = new TakeAction();
//                tiles.addAll(owner.getItems());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
//                tiles.clear();
//                tiles.addAll(owner.getNpcs());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    ((ActionWithSecondPosition)action).setSecondPosition(position.getPoint());
//                    ((ActionWithSecondPosition)action).setSecondRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    ((ActionWithSecondPosition)action).setSecondPosition(tiles.get(0).getPoint());
//                    ((ActionWithSecondPosition)action).setSecondRoom(tiles.get(0).getRoom());
//                }
                break;
            case USE:
                action = new UseAction();
//                tiles.addAll(owner.getItems());
//                if (tiles.size() > 1) {
//                    QuestPositionUpdate position = tiles.get(random.nextInt(tiles.size()-1));
//                    action.setPosition(position.getPoint());
//                    action.setRoom(position.getRoom());
//                    //needs room
//                } else {
//                    //get only one
//                    action.setPosition(tiles.get(0).getPoint());
//                    action.setRoom(tiles.get(0).getRoom());
//                }
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

        int limitLength = 146;//averageLimit: 146
        int questResolution = 60000000; //5000000
//        int averageTOBElimit = 0;
//        for (int i = 0; i < 1000; i++) {
        Quest quest;
        int progressPercentage = 0;
        while (maps.size() < questResolution) {
            try{
                quest = new Quest();
                grammar.expand(quest, QuestGrammar.START_VALUE, availableOptions, 0 , limitLength);
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
