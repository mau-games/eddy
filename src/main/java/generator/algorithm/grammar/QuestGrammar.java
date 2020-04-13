package generator.algorithm.grammar;

import collectors.ActionLogger;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import game.Dungeon;
import game.Tile;
import game.quest.Action;
import game.quest.ActionType;
import game.quest.Quest;
import game.quest.actions.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class QuestGrammar {
    private final static String defaultConfig = "quest_rules.json";
    private final static Map<String, List<List<String>>> rules = new LinkedHashMap<>();
    public final static String START_VALUE = "<QUEST>";
    private final Random random = new Random();
    private final Dungeon dungeon;
    public QuestGrammar(Dungeon dungeon) {
        this.dungeon = dungeon;
        init();
    }

    private void init() {
        String toParse = readFile(defaultConfig);
        parseJSONAndPopulateRules(toParse);
    }

    public Quest expand(Quest toExpand, String value){
        if (rules.containsKey(value.toUpperCase())){
            List<String> nextValues = pickRandom(rules.get(value));
            pickRandom(rules.get(value)).forEach(s -> expand(toExpand, s));
            //expand more
        } else {
            Action action = createActionOnType(value);
            toExpand.addActions(action);
        }
        return toExpand;
    }

    public Quest expand(final Quest toExpand, final String value, final List<ActionType> availableOptions){
        boolean available = availableOptions.stream()
                .map(Enum::toString)
                .anyMatch(s -> s.equals(value.toUpperCase()));
        if (rules.containsKey(value.toUpperCase())
                && available){
            pickRandom(rules.get(value)).forEach(s -> expand(toExpand, s, availableOptions));
        } else if (available){
            Action action = createActionOnType(value);
            toExpand.addActions(action);
        }
        return toExpand;
    }

    /**
     * To create context for generated actions
     * @param value
     * @return
     */
    private Action createActionOnType(String value) {
        Action action = null;
        List<Tile> tiles = new ArrayList<Tile>();
        switch (ActionType.valueOf(value.toUpperCase())){
            case CAPTURE:
                action = new CaptureAction();
                tiles.addAll(dungeon.getEnemies());
                tiles.addAll(dungeon.getBosses());
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case DAMAGE:
                action = new DamageAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }                break;
            case DEFEND:
                action = new DefendAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }                break;
            case ESCORT:
                action = new EscortAction();
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }                break;
            case EXCHANGE: //needs 2 positions
                action = new ExchangeAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                tiles.clear();
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 1) { //TODO: copy to other options
                    ((ExchangeAction)action).setSecondPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case EXPERIMENT:
                action = new ExperimentAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case EXPLORE:
                action = new ExploreAction();
                dungeon.getAllRooms().forEach(room -> Collections.addAll(tiles, room.getTileBasedMap()));
                action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                break;
            case GATHER:
                action = new GatherAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case GIVE:
                action = new GiveAction();tiles.addAll(dungeon.getItems());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                tiles.clear();
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 1) { //TODO: copy to other options
                    ((GiveAction)action).setSecondPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case GO_TO:
                action = new GotoAction();
                dungeon.getAllRooms().forEach(room -> Collections.addAll(tiles, room.getTileBasedMap()));
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case KILL:
                action = new KillAction();
                tiles.addAll(dungeon.getEnemies());
                tiles.addAll(dungeon.getBosses());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case LISTEN:
                action = new ListenAction();
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case READ:
                action = new ReadAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case REPAIR:
                action = new RepairAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case REPORT:
                action = new ReportAction();
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case SPY:
                action = new SpyAction();
                tiles.addAll(dungeon.getEnemies());
                tiles.addAll(dungeon.getBosses());
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case STEALTH:
                action = new StealthAction();
                tiles.addAll(dungeon.getEnemies());
                tiles.addAll(dungeon.getBosses());
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case TAKE:
                action = new TakeAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                tiles.clear();
                tiles.addAll(dungeon.getNpcs());
                if (tiles.size() > 1) { //TODO: copy to other options
                    ((TakeAction)action).setSecondPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
            case USE:
                action = new UseAction();
                tiles.addAll(dungeon.getItems());
                if (tiles.size() > 1) { //TODO: copy to other options
                    action.setPosition(tiles.get(random.nextInt(tiles.size()-1)).GetCenterPosition());
                } else {
                    //get only one
                    action.setPosition(tiles.get(0).GetCenterPosition());
                }
                break;
        }
        if (action != null) action.setId(UUID.randomUUID());
        return action;
    }


    private String readFile(String fileName){
        String toReturn = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
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
        JsonObject jsonObject = new JsonParser().parse(toParse).getAsJsonObject();
        jsonObject.entrySet().stream().forEach(stringJsonElementEntry -> {
            String key = stringJsonElementEntry.getKey().toUpperCase();
            JsonArray jsonArray = stringJsonElementEntry.getValue().getAsJsonArray();
            List<List<String>> values = new ArrayList<>();
            int i = 0;
            jsonArray.forEach(jsonElement -> {
                List<String> sequence = new ArrayList<>();
                jsonElement.getAsJsonArray().forEach(stringElement -> {
                    sequence.add(stringElement.getAsString().toUpperCase());
                });
                values.add(sequence);
            });
            rules.put(key, values);
        });
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
}
