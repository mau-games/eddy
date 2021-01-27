package game.narrative;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import org.checkerframework.checker.units.qual.A;
import util.Util;

import java.util.*;

public class GrammarPattern {

    public GrammarGraph pattern;
    public ArrayList<GrammarGraph> productionRules;

    public GrammarPattern()
    {
        pattern = new GrammarGraph();
        productionRules = new ArrayList<GrammarGraph>();
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
        ArrayList<GrammarGraph> permutations = currentGraph.getPermutations(this.pattern.nodes.size());
        GrammarGraph selectedSubgraph = new GrammarGraph();

        for(GrammarGraph perm : permutations)
        {
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

        //break connections within the subgraph
        selectedSubgraph.removeInterestedConnections();

        GrammarGraph selectedPattern = this.productionRules.get(Util.getNextInt(0, this.productionRules.size()));

        //Add nodes if needed!
        for (int i = selectedSubgraph.nodes.size(); i < selectedPattern.nodes.size(); i++)
        {
            GrammarNode newNode = new GrammarNode(currentGraph.nodes.size(), selectedPattern.nodes.get(i).grammarNodeType);
            currentGraph.nodes.add(newNode);
            selectedSubgraph.nodes.add(newNode);
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

                //Check this!"

                selectedSubgraph.nodes.get(i).addConnection(selectedSubgraph.nodes.get(index), patternChild.getValue());
                if(patternChild.getValue() == 0 || patternChild.getValue() == 2)
                {
                    selectedSubgraph.nodes.get(index).addConnection(selectedSubgraph.nodes.get(i), patternChild.getValue());
                }
            }
        }

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
