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
                float percent = integer / map.get("TOTAL");
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
}
