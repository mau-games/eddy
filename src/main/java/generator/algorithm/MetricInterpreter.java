package generator.algorithm;

import finder.patterns.CompositePattern;
import finder.patterns.Pattern;
import finder.patterns.meso.*;
import finder.patterns.micro.*;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import game.Room;

import java.util.ArrayList;
import java.util.List;

public class MetricInterpreter
{
    public double final_metric_value = 0.0f;
    public ArrayList<MetricChromosome> individualChromosomes;

    public MetricInterpreter(ArrayList<MetricChromosome> chromosomes) {
        final_metric_value = 0.0f;
        individualChromosomes = chromosomes;
    }

    public double calculateMetric( Room room)
    {
        final_metric_value = 0.0f;
        float chokepoint_quality = 0.0f;
        float de_quality = 0.0f;
        float ambush_quality = 0.0f;
        float gr_qualitY = 0.0f;
        float tr_quality = 0.0f;
        float gt_quality = 0.0f;


        List<CompositePattern> mesos = room.getPatternFinder().getMesoPatterns();
        for(CompositePattern meso : mesos)
        {
            if(meso instanceof  DeadEnd)
            {
                de_quality += meso.getQuality();
            }
            else if(meso instanceof ChokePoint)
            {
                chokepoint_quality += meso.getQuality();
            }
            else if(meso instanceof Ambush)
            {
                ambush_quality += meso.getQuality();
            }
            else if(meso instanceof GuardRoom)
            {
                gr_qualitY += meso.getQuality();
            }
            else if(meso instanceof TreasureRoom)
            {
                tr_quality += meso.getQuality();
            }
            else if(meso instanceof GuardedTreasure)
            {
                gt_quality += meso.getQuality();
            }
        }

        float corridors_q = 0.0f;
        float chambers_q = 0.0f;
        float treasures_q = 0.0f;
        float enemies_q = 0.0f;
        float bosses_q = 0.0f;
        float doors_q = 0.0f;
        float nothing_q = 0.0f;

        List<Pattern> micros = room.getPatternFinder().findMicroPatterns();

        for(Pattern micro : micros)
        {
            if(micro instanceof Corridor)
            {
                corridors_q += micro.getQuality();
            }
            else if(micro instanceof Chamber)
            {
                chambers_q += micro.getQuality();
            }
             else if(micro instanceof Treasure)
            {
                treasures_q += micro.getQuality();
            }
              else if(micro instanceof Enemy)
            {
                enemies_q += micro.getQuality();
            }
               else if(micro instanceof Boss)
            {
                bosses_q += micro.getQuality();
            }
                else if(micro instanceof Door)
            {
                doors_q += micro.getQuality();
            }
                else if(micro instanceof Nothing)
            {
                nothing_q += micro.getQuality();
            }
        }


        for(MetricChromosome chromosome : individualChromosomes)
        {
            chromosome.weight = 1.0f;

            if(chromosome.functionType == 0) //Behavioral Repertoire function
            {
                switch (chromosome.function)
                {
                    case 0: //Symmetry
                        final_metric_value += chromosome.weight *
                                GADimension.calculateIndividualValue(GADimension.DimensionTypes.SYMMETRY, room);
                        break;
                    case 1: //Leniency
                        final_metric_value += chromosome.weight *
                                GADimension.calculateIndividualValue(GADimension.DimensionTypes.LENIENCY, room);
                        break;
                    case 2: //SpatialPatterns
                        final_metric_value += chromosome.weight *
                                GADimension.calculateIndividualValue(GADimension.DimensionTypes.NUMBER_PATTERNS, room);
                        break;
                    case 3: //MESO
                        final_metric_value += chromosome.weight *
                                GADimension.calculateIndividualValue(GADimension.DimensionTypes.NUMBER_MESO_PATTERN, room);
                        break;
                    case 4: //linearity
                        final_metric_value += chromosome.weight *
                                GADimension.calculateIndividualValue(GADimension.DimensionTypes.LINEARITY, room);
                        break;
                    case 5: //INNER_SIM
                        final_metric_value += chromosome.weight *
                                GADimension.calculateIndividualValue(GADimension.DimensionTypes.INNER_SIMILARITY, room);
                        break;
                    case 6: //SIM
                        final_metric_value += chromosome.weight *
                                GADimension.calculateIndividualValue(GADimension.DimensionTypes.SIMILARITY, room);
                        break;
                    default: //linearity
                        final_metric_value += chromosome.weight *
                                GADimension.calculateIndividualValue(GADimension.DimensionTypes.LINEARITY, room);
                        break;
                }
            }
            else //Specific Room functions
            {
                switch (chromosome.function)
                {
                    case 0: //Treasure sparsity
                        final_metric_value += chromosome.weight * room.calculateTreasureSparsity();
                        break;
                    case 1: //Treasure density
                        final_metric_value += chromosome.weight * room.calculateTreasureDensity();
                        break;
                    case 2: //treasure count
                        final_metric_value += chromosome.weight * room.getTreasurePercentage();
                        break;
                    case 3: //Treasure sparsity
                        final_metric_value += chromosome.weight * room.calculateEnemySparsity();
                        break;
                    case 4: //Treasure density
                        final_metric_value += chromosome.weight * room.calculateEnemyDensity();
                        break;
                    case 5: //treasure count
                        final_metric_value += chromosome.weight * room.getEnemyPercentage();
                        break;
                    case 6: //Treasure sparsity
                        final_metric_value += chromosome.weight * room.calculateWallSparsity();
                        break;
                    case 7: //Treasure density
                        final_metric_value += chromosome.weight * room.calculateWallDensity();
                        break;
                    case 8: //treasure count
                        final_metric_value += chromosome.weight * ((float)room.getWallCount()/91.0);
                        break;
                    case 9: //Treasure sparsity
                        final_metric_value += chromosome.weight * room.calculateFloorSparsity();
                        break;
                    case 10: //Treasure density
                        final_metric_value += chromosome.weight * room.calculateFloorDensity();
                        break;
                    case 11: //treasure count
                        final_metric_value += chromosome.weight * (room.emptySpacesRate());
                        break;
                    case 12: //door greedness
                        room.calculateDoorGreedness();
                        final_metric_value += chromosome.weight * room.getDoorGreedness();
                        break;
                    case 13: //door safeness
                        room.calculateDoorSafeness();
                        final_metric_value += chromosome.weight * room.getDoorSafeness();
                        break;
                    case 14: //treasure safeness
                        final_metric_value += chromosome.weight * room.calculateTreasureSafeness();
                        break;
                    case 15: //treasure safeness //Missing patterns quality!
                        final_metric_value += chromosome.weight * corridors_q;
                        break;
                    case 16: //treasure safeness
                        final_metric_value += chromosome.weight * chambers_q;
                        break;
                    case 17: //treasure safeness
                        final_metric_value += chromosome.weight * treasures_q;
                        break;
                    case 18: //treasure safeness
                        final_metric_value += chromosome.weight * enemies_q;
                        break;
                    case 19: //treasure safeness
                        final_metric_value += chromosome.weight * bosses_q;
                        break;
                    case 20: //treasure safeness
                        final_metric_value += chromosome.weight * doors_q;
                        break;
                    case 21: //treasure safeness
                        final_metric_value += chromosome.weight * nothing_q;
                        break;
                    case 22: //treasure safeness
                        final_metric_value += chromosome.weight * chokepoint_quality;
                        break;
                    case 23: //treasure safeness
                        final_metric_value += chromosome.weight * de_quality;
                        break;
                    case 24: //treasure safeness
                        final_metric_value += chromosome.weight * ambush_quality;
                        break;
                    case 25: //treasure safeness
                        final_metric_value += chromosome.weight * gr_qualitY;
                        break;
                    case 26: //treasure safeness
                        final_metric_value += chromosome.weight * tr_quality;
                        break;
                    case 27: //treasure safeness
                        final_metric_value += chromosome.weight * gt_quality;
                        break;
                }
            }
        }

        final_metric_value /= individualChromosomes.size(); // normalize
        return final_metric_value;
    }
}
