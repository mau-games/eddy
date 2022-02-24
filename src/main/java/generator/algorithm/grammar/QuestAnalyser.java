package generator.algorithm.grammar;

import game.quest.Quest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuestAnalyser {

    public static Map<String,Float> mapQuestActions(Quest quest){
        Map<String, Float> map = new HashMap<>();
        map.put("TOTAL", (float) quest.getActions().size());
        quest.getActions().forEach(action -> {
            if (map.containsKey(action.getName())){
              float value = map.get(action.getName());
              map.put(action.getName(),++value);
            } else {
                map.put(action.getName(),1f);
            }
        });
        return map;
    }

    public static Map<String,Float> mapPercentQuestAction(Map<String,Float> map){
        Map<String, Float> percentages = new HashMap<>();
        map.forEach((s, integer) -> {
            if (!s.equals("TOTAL")){
                float percent = (integer / map.get("TOTAL")) * 10;
                percentages.put(s,percent);
            }
        });
        return percentages;
    }

    public static Map<Integer, List<Map<String, Float>>> mapQuestActionMaps(List<Map<String, Float>> maps, int max) {
        Map<Integer, List<Map<String, Float>>> mapped = new HashMap<>();
        for (int size = 0; size < max; size++){
            List<Map<String, Float>> sizeMaps = new ArrayList<>();
            for (Map<String, Float> map : maps) {
                if (map.get("TOTAL") == size){
                    sizeMaps.add(map);
                }
            }
            mapped.put(size, sizeMaps);
        }
        return mapped;
    }

    public static Map<Integer, Map<String,Float>> analyseMapQuestActionMaps(Map<Integer, List<Map<String, Float>>> mapQuestActionMaps) {
        Map<Integer, Map<String,Float>> analysedAveragePercentage = new HashMap<>();
        mapQuestActionMaps.forEach((size, mapList) -> {
            Map<String, Float> sizeMap = new HashMap<>();
            sizeMap.put("TOTAL", (float) mapList.size());

            mapList.forEach(map -> {
                Map<String, Float> percentage = mapPercentQuestAction(map);
                percentage.forEach((s, integer) -> {
                    if (!s.equals("TOTAL")){
                        if (sizeMap.containsKey(s)) {
                            //there is an existing value, just add to it
                            sizeMap.put(s, sizeMap.get(s) + integer);
                        } else {
                            //new value
                            sizeMap.put(s, integer);
                        }
                    }
                });
            });
            //average out the current sizeMap
            analysedAveragePercentage.put(size, mapPercentQuestAction(sizeMap));
        });

        return analysedAveragePercentage;
    }

    public static Map <String,String[]> mapToHeatMap(Map<Integer, Map<String, Float>> map) {
        Map <String,String[]> heatMap = new HashMap<>();
        int size = map.size();
        String[] header = new String[size];
        String[] capture = new String[size];
        String[] damage = new String[size];
        String[] defend = new String[size];
        String[] escort = new String[size];
        String[] exchange = new String[size];
        String[] experiment = new String[size];
        String[] explore = new String[size];
        String[] gather = new String[size];
        String[] give = new String[size];
        String[] go_to = new String[size];
        String[] kill = new String[size];
        String[] listen = new String[size];
        String[] read = new String[size];
        String[] repair = new String[size];
        String[] report = new String[size];
        String[] spy = new String[size];
        String[] take = new String[size];
        String[] stealth = new String[size];
        String[] use = new String[size];
        for (int i = 0; i < size; i++) {
            header[i] = "" + i;
            capture[i] = map.get(i).containsKey("CAPTURE") ? "" + map.get(i).get("CAPTURE") : "0";
            damage[i] = map.get(i).containsKey("DAMAGE") ? "" + map.get(i).get("DAMAGE") : "0";
            defend[i] = map.get(i).containsKey("DEFEND") ? "" + map.get(i).get("DEFEND") : "0";
            escort[i] = map.get(i).containsKey("ESCORT") ? "" + map.get(i).get("ESCORT") : "0";
            exchange[i] = map.get(i).containsKey("EXCHANGE") ? "" + map.get(i).get("EXCHANGE") : "0";
            experiment[i] = map.get(i).containsKey("EXPERIMENT") ? "" + map.get(i).get("EXPERIMENT") : "0";
            explore[i] = map.get(i).containsKey("EXPLORE") ? "" + map.get(i).get("EXPLORE") : "0";
            gather[i] = map.get(i).containsKey("GATHER") ? "" + map.get(i).get("GATHER") : "0";
            give[i] = map.get(i).containsKey("GIVE") ? "" + map.get(i).get("GIVE") : "0";
            go_to[i] = map.get(i).containsKey("GO_TO") ? "" + map.get(i).get("GO_TO") : "0";
            kill[i] = map.get(i).containsKey("KILL") ? "" + map.get(i).get("KILL") : "0";
            listen[i] = map.get(i).containsKey("LISTEN") ? "" + map.get(i).get("LISTEN") : "0";
            read[i] = map.get(i).containsKey("READ") ? "" + map.get(i).get("READ") : "0";
            repair[i] = map.get(i).containsKey("REPAIR") ? "" + map.get(i).get("REPAIR") : "0";
            report[i] = map.get(i).containsKey("REPORT") ? "" + map.get(i).get("REPORT") : "0";
            spy[i] = map.get(i).containsKey("SPY") ? "" + map.get(i).get("SPY") : "0";
            take[i] = map.get(i).containsKey("TAKE") ? "" + map.get(i).get("TAKE") : "0";
            stealth[i] = map.get(i).containsKey("STEALTH") ? "" + map.get(i).get("STEALTH") : "0";
            use[i] = map.get(i).containsKey("USE") ? "" + map.get(i).get("USE") : "0";
        }
        heatMap.put("NAME", header);
        heatMap.put("CAPTURE", capture);
        heatMap.put("DAMAGE", damage);
        heatMap.put("DEFEND", defend);
        heatMap.put("ESCORT", escort);
        heatMap.put("EXCHANGE", exchange);
        heatMap.put("EXPERIMENT", experiment);
        heatMap.put("EXPLORE", explore);
        heatMap.put("GATHER", gather);
        heatMap.put("GIVE", give);
        heatMap.put("GO_TO", go_to);
        heatMap.put("KILL", kill);
        heatMap.put("LISTEN", listen);
        heatMap.put("READ", read);
        heatMap.put("REPAIR", repair);
        heatMap.put("REPORT", report);
        heatMap.put("SPY", spy);
        heatMap.put("TAKE", take);
        heatMap.put("STEALTH", stealth);
        heatMap.put("USE", use);

        return heatMap;
    }
}
