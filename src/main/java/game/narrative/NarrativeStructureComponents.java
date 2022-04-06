package game.narrative;

import game.Room;
import game.Tile;
import game.narrative.NarrativeFinder.*;
import util.Point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//This simply defines where!
class NarrativeComponent
{
    Room dungeon_room;
    Point room_pos;
    Tile pos_tile;
    NarrativePattern event;
    ArrayList<NarrativePattern> conditionals = new ArrayList<NarrativePattern>();
    ArrayList<NarrativeComponent> conditional_components = new ArrayList<NarrativeComponent>();

    public NarrativeComponent(Room room, Point pos, Tile tile, NarrativePattern event)
    {
        dungeon_room = room;
        room_pos = pos;
        pos_tile = tile;
        this.event = event;
    }


    public void addConditional(NarrativePattern conditional)
    {
        conditionals.add(conditional);
    }

    public void addConditional(NarrativePattern conditional, NarrativeComponent conditional_component)
    {
        conditionals.add(conditional);
        conditional_components.add(conditional_component);
    }

    public void addConditional(NarrativeComponent conditional_component)
    {
        conditional_components.add(conditional_component);
    }

    @Override
    public boolean equals(Object v) {
        boolean retVal = false;

        if (v instanceof NarrativeComponent){
            NarrativeComponent ptr = (NarrativeComponent) v;
            retVal = ptr.event.equals(this.event);
        }

        return retVal;
    }
}

class OrderedPlotEvent extends NarrativeComponent
{
    int seq_order; //In case there are a sequence of plot events (for instance,
    int order; //This might be -1 if event is not in order!


    public OrderedPlotEvent(int seq, int order, NarrativePattern np, Room room, Point pos, Tile tile)
    {
        super(room, pos, tile, np);
        this.seq_order = seq;
        this.order = order;
//        this.event = np;
    }

}

/***
 * Thinking if this should be a class, or if it should be a pattern to know things like Factions, and roles?
 * Also knowing the conflicts might give a good idea of what the narrative creates?
 */
public class NarrativeStructureComponents
{
//    public int factions

    public int factions;
    public int roles;
    public int plot_points;
    public int plot_twists;

    public List<NarrativeComponent> components;
//    public HashMap<NarrativePattern, NarrativeComponent> components;

    public GrammarGraph owner;

    public NarrativeStructureComponents(GrammarGraph owner)
    {
        this.owner = owner;
        this.components = new ArrayList<NarrativeComponent>();
//        this.components = new HashMap<NarrativePattern, NarrativeComponent>();
    }

    //Of course active plot devices will also play a role here, in the sense that if they have output they require those to be after
    //likewise with the derivatives!
    public void evaluate_collect_components()
    {
        int seq = 0;

        //This method gives for granted that the narrative patterns have already been found in the graph.
        //lets start with the hard stuff! :D Plot points!
        ArrayList<PlotPoint> contained_plotpoints = this.owner.pattern_finder.getAllPatternsByType(PlotPoint.class);
//        ArrayList<DerivativePattern> derivatives = this.owner.pattern_finder.getAllPatternsByType(PlotPoint.class);
        ArrayList<DerivativePattern> graph_derivatives = this.owner.pattern_finder.getAllPatternsByType(DerivativePattern.class);

        for(DerivativePattern derivative_pattern : graph_derivatives)
        {
            int order = 0;

            //This already have the ordered list!
            for(NarrativePattern np : derivative_pattern.derivatives)
            {
                OrderedPlotEvent ope = new OrderedPlotEvent(seq, order, np, null, null, null); 

//                if(components.containsKey(np))
//                    ope = (OrderedPlotEvent) components.get(np);
//                else
//                    ope = new OrderedPlotEvent(seq, order, np, null, null, null);

                //If we already have this component we want to add conditionals, not adding the object again! 
                if(components.contains(ope))
                    ope = (OrderedPlotEvent) components.get(components.indexOf(ope));
//                else
//                    ope = new OrderedPlotEvent(seq, order, np, null, null, null);

                if(order != 0)
                {
                    ope.addConditional(derivative_pattern.derivatives.get(order - 1), components.get(components.size() - 1));
                }
                else
                {
                    ope.addConditional(derivative_pattern.source);
                }

                components.add(ope);
                order++;

            }
            seq++;
        }

        for(PlotPoint contained_plotpoint : contained_plotpoints)
        {
            boolean found =  false;


            for(NarrativeComponent assigned_component : components)
            {
                if(assigned_component.event.equals(contained_plotpoint.core_pattern))
                    found = true;
            }

            if(found)
                continue;

            //ESTOY AQUI!
            if(contained_plotpoint.core_pattern instanceof ActivePlotDevice) //This one is also relevant!
            {
                //OrderedPlotEvent ope = new OrderedPlotEvent(seq, order, np, null, null, null);

            }
            else if(contained_plotpoint.core_pattern instanceof RevealPattern)
            {

            }
            else //then we are checking a derivative pattern!
            {
                //Not relevant because we already got these values!

//                int order = 0;
//                //This has the ordered list!
//                for(NarrativePattern ordered_pp : contained_plotpoint.patterns)
//                {
//                    OrderedPlotEvent ope = new OrderedPlotEvent(seq, order, ordered_pp, null, null, null);
//
//                    if(!components.contains(ope))
//                    {
//                        components.add(ope);
//                        order++;
//                    }
//
////                    components.add(new OrderedPlotEvent(seq, order++, ordered_pp, null, null, null));
//                }
//
//                seq++;
            }


//            ArrayList<NarrativePattern> plot_point_specialists = this.owner.pattern_finder.getAllInstances(contained_plotpoint.connected_node);
//
//            //Now lets see what is this plot point for real!
//            for(NarrativePattern plot_point_specialist : plot_point_specialists)
//            {
//                if(plot_point_specialist instanceof BasicNarrativePattern) //Here we get the micropattern of this pp
//                {
//
//                }
//                else if(plot_point_specialist instanceof DerivativePattern)
//            }

        }
    }

}
