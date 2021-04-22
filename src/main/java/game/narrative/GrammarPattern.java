package game.narrative;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import org.checkerframework.checker.units.qual.A;
import util.Util;

import java.util.*;
import java.util.stream.Collectors;

public class GrammarPattern {

    public GrammarGraph pattern;
    public ArrayList<GrammarGraph> productionRules;

    public GrammarPattern()
    {
        pattern = new GrammarGraph();
        productionRules = new ArrayList<GrammarGraph>();
    }

    public GrammarPattern(GrammarPattern other)
    {
        pattern = new GrammarGraph();
        productionRules = new ArrayList<GrammarGraph>();

        setPattern(new GrammarGraph(other.pattern));

        for(GrammarGraph production_rule : other.productionRules)
        {
            addProductionRule(new GrammarGraph(production_rule));
        }
    }

    public void setPattern(GrammarGraph pattern)
    {
        this.pattern = pattern;
    }

    //For the evolutionary algorithm
    public void editPattern()
    {

    }

    public void addProductionRule(GrammarGraph rule)
    {
        productionRules.add(rule);
    }

//    public GrammarGraph

    public void match(GrammarGraph currentGraph, int maxConnections)
    {
        if(this.pattern == null)
            System.out.println("SOMETHING WENT HORRIBLE WRONG WITH THIS PATTERN!");

        ArrayList<GrammarGraph> permutations = currentGraph.getPermutations(this.pattern.nodes.size(), this.pattern);
//        Collections.shuffle(permutations);
        GrammarGraph selectedSubgraph = new GrammarGraph();
        int perm_size = permutations.size();

        int pattern_graph_size = this.pattern.getAllConnections(true);
        permutations = permutations.stream()
                .filter(p -> p.getAllConnections(true) == pattern_graph_size)
                .collect(Collectors.toCollection(ArrayList::new));

//        if(perm_size > 1000)
//        {
//            System.out.println(perm_size + "; " + permutations.size());
//        }

        for(GrammarGraph perm : permutations)
        {
//            int dist = this.pattern.distanceBetweenGraphs(perm);

            if(this.pattern.nodes.size() > perm.nodes.size())
                continue;


            if(pattern.testGraphMatchPattern(perm))
            {
                selectedSubgraph = perm;
                break;
            }
        }

        //There is no selectedsubgraph!
        if(selectedSubgraph.nodes.size() == 0){
            return;
        }

        //fixme: For testing only
//        System.out.println("SELECTED SUBGRAPH!");
//        System.out.println(selectedSubgraph.toString());

        //break connections within the subgraph
        selectedSubgraph.removeInterestedConnections();

        GrammarGraph selectedPattern = this.productionRules.get(Util.getNextInt(0, this.productionRules.size()));

        //Add nodes if needed!
        //TODO: WHAT IF WE NEED TO REMOVE BRA? we need to indicate to the bigger graph i think (for indices)
        //Got you cover fam!
        for (int i = selectedSubgraph.nodes.size(); i < selectedPattern.nodes.size(); i++)
        {
            GrammarNode newNode = new GrammarNode(currentGraph.nodes.size(), selectedPattern.nodes.get(i).grammarNodeType);
            currentGraph.addNode(newNode, false);
            selectedSubgraph.addNode(newNode, false);
//            currentGraph.nodes.add(newNode);
//            selectedSubgraph.nodes.add(newNode);
        }

        //Remove nodes if needed!
        for (int i = selectedSubgraph.nodes.size(); i > selectedPattern.nodes.size(); i--)
        {
            int idToRemove = selectedSubgraph.removeNodeByPosition(i-1);
//            selectedSubgraph.removeNode(i-1);
            currentGraph.removeNode(idToRemove);
        }


        //add connections?
        for (int i = 0; i < selectedPattern.nodes.size(); i++)
        {
            //Not interesting at the moment
//            selectedSubgraph.nodes.get(i).adjustAccessLevel(this.patternMatch.relativeAccess + selectedPattern.nodes[i].accessLevel);
            HashMap<GrammarNode, Integer> patternChildren = selectedPattern.nodes.get(i).getChildrenClone();
            for(Map.Entry<GrammarNode, Integer> patternChild : patternChildren.entrySet())
            {
                int index = selectedPattern.getNodeIndex(patternChild.getKey());

                //TODO: Remember to Check this! because of how the connection is added

                selectedSubgraph.nodes.get(i).addConnection(selectedSubgraph.nodes.get(index), patternChild.getValue());
                if(patternChild.getValue() == 2)
                {
                    selectedSubgraph.nodes.get(index).addConnection(selectedSubgraph.nodes.get(i), patternChild.getValue());
                }
//                if(patternChild.getValue() == 0 || patternChild.getValue() == 2)
//                {
//                    selectedSubgraph.nodes.get(index).addConnection(selectedSubgraph.nodes.get(i), patternChild.getValue());
//                }
            }
        }

        //Final removal of connections in case we removed a node and we have a ghost connection!
        selectedSubgraph.removeGhostConnections();

//        //Go through each of the nodes in the graph
//        for(GrammarNode graphNode : currentGraph.nodes)
//        {
//            Queue<GrammarNode> queue = new LinkedList<GrammarNode>();
//            ArrayList<GrammarNode> visited = new ArrayList<GrammarNode>();
//
//            queue.add(graphNode);
//
//            while(!queue.isEmpty())
//            {
//                //Get the step and expand with production rules
//                GrammarNode current = queue.remove();
//
//                if(visited.contains(current))
//                    continue;
//
//                visited.add(current);
//
//
//                for(Map.Entry<GrammarNode, Integer> keyValue : connections.entrySet())
//                {
//                    result += "(" + keyValue.getKey().id + "," + keyValue.getValue() + ") ";
//                }
//
//
//                div_ax = this.productionRules.get(current);
//
//                if (this.productionRules.containsKey(current)) {
//                    div_ax = this.productionRules.get(current);
////                System.out.println(Arrays.asList(div_ax));
//                    div_ax = div_ax[Util.getNextInt(0, div_ax.length)].split(this.delimiter);
////                System.out.println(Arrays.asList(div_ax));
//                    queue.addAll(Arrays.asList(div_ax));
//                } else {
//                    result.add(current);
//                }
//            }
//
//
//            for(Map.Entry<GrammarNode, Integer> keyValue : connections.entrySet())
//            {
//                result += "(" + keyValue.getKey().id + "," + keyValue.getValue() + ") ";
//            }
//            //Then go through each child
//            for(GrammarNode)
//        }
    }
}
