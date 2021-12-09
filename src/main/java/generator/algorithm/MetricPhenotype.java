package generator.algorithm;

import game.Dungeon;
import game.Room;
import game.Tile;
import game.TileTypes;
import generator.algorithm.MAPElites.Dimensions.CustomGADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.GrammarMAPEliteAlgorithm;
import generator.algorithm.MAPElites.MAPEliteAlgorithm;
import generator.config.GeneratorConfig;
import util.Point;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.MAPElitesMetricDone;
import util.eventrouting.events.StartGA_MAPE;

import java.util.*;

public class MetricPhenotype implements Listener
{
    private MetricGenotype genotype;
    private MAPEliteAlgorithm map_elites; //This is the actual "phenotype"
    private MetricInterpreter interpreter;
    private CustomGADimension metricDimension;
    public UUID algorithm_unique_code;

    //Room = best room in cell, Double metric result.'
    public LinkedHashMap<Room, Double> results = new LinkedHashMap();   // use LinkedHashMap to maintain sequence

    public MetricPhenotype(MetricGenotype genotype)
    {
        this.genotype = genotype;
        EventRouter.getInstance().registerListener(this, new MAPElitesMetricDone());
    }

    public MAPEliteAlgorithm getAlgorithm(Room relative_room)
    {
        createMetric();

        map_elites = new MAPEliteAlgorithm(relative_room, relative_room.getCalculatedConfig());
        map_elites.metric_map_elites = true;
        map_elites.initPopulations(relative_room, new MAPEDimensionFXML[]{new MAPEDimensionFXML(GADimension.DimensionTypes.CUSTOM, 5, interpreter)});
        map_elites.start();
        algorithm_unique_code = map_elites.id;
        return map_elites;

        //TODO: Need to think a bit around this!
        // But maybe we want to do this one a bit in a special way
        // I mean, we want to run it and listen/hook to the result of 50 gens.
        //Also the dimension to be passed is the one from this model!
//        router.postEvent(new StartGA_MAPE(relative_room, currentDimensions));
    }

    /**
     * Create metric from the Genotype!
     */
    public MetricInterpreter createMetric()
    {
        if(interpreter != null)
            return interpreter;

        interpreter = new MetricInterpreter(this.genotype.getChromosomes());
        return interpreter;
    }

    @Override
    public void ping(PCGEvent e)
    {
        if(e instanceof MAPElitesMetricDone && ((MAPElitesMetricDone) e).getID() == algorithm_unique_code)
        {
            //Now we know this is our MAP-Elites
            for(Room fit_room : ((MAPElitesMetricDone) e).GetRooms())
            {
                if(fit_room != null)
                    results.put(fit_room, interpreter.calculateMetric(fit_room));
                else
                    results.put(null, -1.0);
            }
        }
    }
}
