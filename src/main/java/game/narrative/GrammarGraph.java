package game.narrative;

import game.narrative.NarrativeFinder.NarrativeStructPatternFinder;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.grammarDimensions.GADimensionGrammar;

import java.util.*;

/***
 * Code adapted from: https://github.com/amidos2006/GraphDungeonGenerator
 */
public class GrammarGraph
{
    public int ID_counter = 0; //Actually this can literally just be the size of nodes.
    public ArrayList<GrammarNode> nodes;

    //TODO: WIP
    public NarrativePane nPane;
    public NarrativeStructPatternFinder pattern_finder;

    //Might be interesting to know the dimension of the room?
    protected HashMap<GADimensionGrammar.GrammarDimensionTypes, Double> dimensionValues;

    public GrammarGraph()
    {
        nodes = new ArrayList<GrammarNode>();
        nPane = new NarrativePane(this);
        pattern_finder = new NarrativeStructPatternFinder(this);
    }

    //Copy constructor.
    public GrammarGraph(GrammarGraph other)
    {
        nodes = new ArrayList<GrammarNode>();

        for(GrammarNode other_nodes : other.nodes)
        {
            addNode(other_nodes.getGrammarNodeType());
        }

        for(int i = 0; i < other.nodes.size(); i++)
        {
            for(Map.Entry<GrammarNode, Integer> keyValue : other.nodes.get(i).connections.entrySet())
            {
                if(other.getNodeIndex(keyValue.getKey()) == -1)
                    System.out.println("This shouldn't happen");

                nodes.get(i).addConnection(nodes.get(other.getNodeIndex(keyValue.getKey())), keyValue.getValue());
            }
        }

        nPane = new NarrativePane(this);
        pattern_finder = new NarrativeStructPatternFinder(this);
    }

    public GrammarNode addNode(TVTropeType nodeType)
    {
        GrammarNode n = new GrammarNode(ID_counter, nodeType);
        nodes.add(n);
        ID_counter++;

        return n;
    }

    public void addNode(GrammarNode node, boolean setID)
    {
        if(setID)
        {
            node.setID(ID_counter);
        }

        nodes.add(node);
        ID_counter++;
    }

    public void removeNode(GrammarNode node)
    {
        int node_id = getNodeIndex(node);

        if(node_id != -1)
        {
            removeAllConnectionsToNode(node);
            node.removeAllConnection();
            nodes.remove(node);

            for(int i = node_id; i < nodes.size(); i++)
            {
                nodes.get(i).setID(i);
            }

            ID_counter--;
        }
    }

    public int removeNodeByPosition(int node_position)
    {
        if(node_position != -1)
        {
            GrammarNode n_pos = nodes.remove(node_position);
            removeAllConnectionsToNode(n_pos);
            n_pos.removeAllConnection();
            n_pos.removeAllMyConnections();
            int actual_id = n_pos.id;
            ID_counter--;
            return actual_id;

        }

        return -1;
    }

    public void removeNode(int node_id)
    {
        GrammarNode toRemove = getNodeByID(node_id);

        if(toRemove != null)
        {
//            GrammarNode node = nodes.get(node_id);
            removeAllConnectionsToNode(toRemove);
            toRemove.removeAllConnection();
            int actual_id = toRemove.id;
            nodes.remove(toRemove);

            for(GrammarNode n : nodes)
            {
                if(n.id > actual_id)
                    n.setID(n.getID() - 1);
            }
//
//            //FIXME: HERE IS ONE OF THE PROBLEMS!
//            for(int i = actual_id; i < nodes.size(); i++)
//            {
//                nodes.get(i).setID(i);
//            }

            ID_counter--;
        }
    }

    //Damn boi
    private void removeAllConnectionsToNode(GrammarNode toBeRemoved)
    {
        for(GrammarNode other : nodes)
        {
            if(other == toBeRemoved)
                continue;

            other.removeConnection(toBeRemoved, true);
        }
    }

    public ArrayList<GrammarNode> getAllConnectionsToNode(GrammarNode nodeToCheck)
    {
        ArrayList<GrammarNode> others = new ArrayList<GrammarNode>();

        for(GrammarNode other : nodes)
        {
            if(other == nodeToCheck)
                continue;
            else if(other.checkConnectionExists(nodeToCheck))
                others.add(other);
        }

        return others;
    }

    public ArrayList<GrammarNode> getAllConnectionsToNode(GrammarNode nodeToCheck, boolean directional)
    {
        ArrayList<GrammarNode> others = new ArrayList<GrammarNode>();

        for(GrammarNode other : nodes)
        {
            if(other == nodeToCheck)
                continue;
            else if(other.checkConnectionExists(nodeToCheck))
            {
                if(directional && other.connections.get(nodeToCheck) == 0)
                    continue;

                others.add(other);
            }
        }

        return others;
    }

    //TODO: Check this method.
    public HashMap<GrammarNode, Integer> getAllConnectionsTypesToNode(GrammarNode nodeToCheck, boolean directional)
    {
        HashMap<GrammarNode, Integer> others = new HashMap<GrammarNode, Integer>();

        for(GrammarNode other : nodes)
        {
            if(other == nodeToCheck)
                continue;

            else if(other.checkConnectionExists(nodeToCheck))
            {
                if(directional && other.connections.get(nodeToCheck) == 0)
                    continue;

                others.put(other, other.connections.get(nodeToCheck));
            }
        }

        return others;
    }

//    public List<GrammarNode> getNodesByType()

    /// <summary>
    /// get the index of the node in the node array in the graph
    /// </summary>
    /// <param name="n">the node needed to find its index</param>
    /// <returns>index of the node in the node array</returns>
    public int getNodeIndex(GrammarNode n){
        return this.nodes.indexOf(n);
    }

    /// <summary>
    /// get the index of the node in the node array in the graph
    /// </summary>
    /// <param name="n">the node needed to find its index</param>
    /// <returns>index of the node in the node array</returns>
    public GrammarNode getNodeByID(int node_id){

        GrammarNode result = null;

        for(GrammarNode node : nodes)
        {
            if(node.id == node_id)
            {
                result = node;
                break;
            }

        }

        return result;
    }

    public void removeInterestedConnections()
    {
        for (GrammarNode n : nodes)
        {
            HashMap<GrammarNode, Integer> nChildren = n.getFilteredChildren(this);

            for(Map.Entry<GrammarNode, Integer> nChild : nChildren.entrySet())
            {
                n.removeConnection(nChild.getKey(), true);
                nChild.getKey().removeConnection(n, true); //if the connection exist it will be removed.
            }
        }
    }

    public void removeGhostConnections()
    {
        for (GrammarNode n : nodes)
        {
//            HashMap<GrammarNode, Integer> nChildren = n.getChildrenClone();
            ArrayList<GrammarNode> childrenToBeRemoved = new ArrayList<GrammarNode>();

            //More javatonic way of doing this (but I also need to do an operation for the node to be removed)
//            n.connections.entrySet().removeIf(b -> this.getNodeIndex(b.getKey()) == -1);

            for(Map.Entry<GrammarNode, Integer> nChild :  n.connections.entrySet())
            {
                if(this.getNodeIndex(nChild.getKey()) == -1)
                {
                    childrenToBeRemoved.add(nChild.getKey());
                    //The node does not exist anymore!
                }
            }

            //TODO: Maybe this is not necessary (the double true)
            for(GrammarNode childToRemove : childrenToBeRemoved)
            {
                n.removeConnection(childToRemove, true);
                childToRemove.removeConnection(n, true);
            }


        }
    }

    /**
     * Get all connections in the graph (size)
     * @param filtered if filtered, we get only the connections that actually exist within the graph
     *                 This is possible because of graph matching. This means that the node might have more connections
     *                 since it is a subgraph of another graph, but filtering give us only connections within the specific subgraph
     * @return
     */
    public int getAllConnections(boolean filtered)
    {
        int count = 0;
        for(GrammarNode gn : nodes)
        {
            count += filtered ? gn.getFilteredChildrenNodes(this).size() : gn.connections.size();
        }

        return count;
    }

    //TODO: NOW WE START HERE!
    public short distanceBetweenGraphs(GrammarGraph other)
    {
        int size = this.nodes.size();
        int other_size = other.nodes.size();
        int padding_size = Math.abs(size - other_size);
        int max_size = 0;
        int min_size = 0;
        short dist = 0;
        short dist_connection = 0;
        short dist_nNodes = (short)padding_size;
        short dist_typeNodes = 0;

        byte[][] self_adjacency_matrix = null;
//        byte[] flatten_self = new byte[this.nodes.size() * this.nodes.size()];
        byte[][] other_adjacency_matrix = null;
//        byte[] flatten_other = new byte[this.nodes.size() * this.nodes.size()];

        if(size > other_size)
        {
            self_adjacency_matrix = this.computeAdjacencyMatrix(0);
            other_adjacency_matrix = other.computeAdjacencyMatrix(padding_size);
            max_size = size;
            min_size = other_size;
        }
        else
        {
            self_adjacency_matrix = this.computeAdjacencyMatrix(padding_size);
            other_adjacency_matrix = other.computeAdjacencyMatrix(0);
            max_size = other_size;
            min_size = size;
        }



        //1. Calculate first the hamming distance based on self length.
        //Faktist, I think it will be better to calculate based on longest
        //This part only evaluates connection distance
        for (int j = 0; j < max_size; j++)
        {
            for (int i = 0; i < max_size; i++)
            {
//                int step = j * size + i;
                dist_connection += Math.abs(self_adjacency_matrix[j][i] ^ other_adjacency_matrix[j][i]);

//                if(j < other_adjacency_matrix.length && i < other_adjacency_matrix[0].length)
//                {
//                    dist += Math.abs(self_adjacency_matrix[j][i] ^ other_adjacency_matrix[j][i]);
//                }

//                flatten_self[step] = self_adjacency_matrix[j][i];
//
//                if(step > )
            }
        }

        //2. Now calculate distance on the type of node
        for(int i = 0; i < min_size; i++)
        {
            if(!this.nodes.get(i).checkNode(other.nodes.get(i)))
                dist_typeNodes++;
        }

//        System.out.println(dist);

        dist = (short)(dist_connection + dist_typeNodes + dist_nNodes);

        return dist;

    }

    public byte[][] computeAdjacencyMatrix(int padding_size)
    {
        int size = this.nodes.size();
        byte[][] adjacency_matrix = new byte[this.nodes.size() + padding_size][this.nodes.size() + padding_size];

        for(int i = 0; i < this.nodes.size(); i++)
        {
            for(Map.Entry<GrammarNode, Integer> keyValue : this.nodes.get(i).connections.entrySet())
            {
                adjacency_matrix[i][keyValue.getKey().id] = 1;
//                nodes.get(i).addConnection(nodes.get(other.getNodeIndex(keyValue.getKey())), keyValue.getValue());
            }
        }

//        for (int j = size; j < size + padding_size; j++)
//        {
//            for (int i = size; i < size + padding_size; i++)
//            {
//
////                adjacency_matrix[j][i] = -1;
//
//            }
//        }



//        StringBuilder map = new StringBuilder();
//
//        for (int j = 0; j < size; j++)
//        {
//            for (int i = 0; i < size; i++)
//            {
//                {
//                    map.append(adjacency_matrix[j][i]);
//                }
//
//            }
//            map.append("\n");
//        }
//
//        System.out.println(map.toString());

        return adjacency_matrix;
    }

    public boolean testGraphMatchPattern(GrammarGraph testedGraph)
    {
        for(int i = 0; i < this.nodes.size(); ++i)
        {
//            if(this.nodes.size() > testedGraph.nodes.size())
//                System.out.println("Something strange");

            // Check first if the node is the same as in the pattern
            if(!this.nodes.get(i).checkNode(testedGraph.nodes.get(i)))
                return false;

            //Check children
            HashMap<GrammarNode, Integer>  testGraphChildren = testedGraph.nodes.get(i).getFilteredChildren(testedGraph);
            HashMap<GrammarNode, Integer>  thisChildren = this.nodes.get(i).getFilteredChildren(this);

            if(testGraphChildren.size() != thisChildren.size())
                return false;

            //Then check if the children are actually connected to the ones that should!
            ArrayList<Integer> testing = new ArrayList<Integer>();
            for(GrammarNode thisChild : thisChildren.keySet())
            {
                int pIndex = this.getNodeIndex(thisChild);
                testing.add(pIndex);
            }

//            ArrayList<Integer> testing2 = new ArrayList<Integer>();
//            for(GrammarNode otherChild : testGraphChildren.keySet())
//            {
//                int pIndex = testedGraph.getNodeIndex(otherChild);
//                testing2.add(pIndex);
//            }


//            ArrayList<GrammarNode> testChildren = new ArrayList<>(testGraphChildren.keySet());
//            ArrayList<GrammarNode> mainChildren = new ArrayList<>(thisChildren.keySet());

//            //Sizes might be right but still can be connected to the wrong ones, check that children in each matches!
//            for(int j = 0; j < testChildren.size(); j++)
//            {
//                Integer gIndex = this.getNodeIndex(mainChildren.get(j));
//                if (!testing.remove(gIndex)) {
//                    return false;
//                }
//            }

            //Fixed!
            //Sizes might be right but still can be connected to the wrong ones, check that children in each matches!
            for(GrammarNode otherChild : testGraphChildren.keySet())
            {
                int gIndex = testedGraph.getNodeIndex(otherChild);
                if (!testing.remove(Integer.valueOf(gIndex))) {
                    return false;
                }
            }

            if (testing.size() > 0)
            {
                return false;
            }

        }

        return true;
    }

    private ArrayList<Integer> makeDeepCopyInteger(ArrayList<Integer> old){
        ArrayList<Integer> copy = new ArrayList<Integer>(old.size());
        for(Integer i : old){
            copy.add(new Integer(i));
        }
        return copy;
    }

    public ArrayList<GrammarGraph> getPermutationsSmart(int size, GrammarGraph pat)
    {
        ArrayList<GrammarGraph> nodePermutations = new ArrayList<GrammarGraph>();
//        Stack<>
//
//        for(GrammarNode gn_self : this.nodes)
//        {
//
//        }
//
//        for(GrammarNode gn_pat : pat.nodes)
//        {
//
//        }
        return nodePermutations;
    }

    public ArrayList<GrammarGraph> getPermutations(int size, GrammarGraph pattern_graph)
    {
        ArrayList<Integer> indices = new ArrayList<Integer>();
        for (int i = 0; i < this.nodes.size(); i++)
        {
            indices.add(i);
        }
        ArrayList<ArrayList<Integer>> integerPermutations = getPermutations(indices, size);
        ArrayList<GrammarGraph> nodePermutations = new ArrayList<GrammarGraph>();

        for (ArrayList<Integer> integerPermutationsInd : integerPermutations)
        {
            GrammarGraph temp = new GrammarGraph();
            for (int i = 0; i < integerPermutationsInd.size(); i++)
            {
//                temp.nodes.add(nodes.get(integerPermutationsInd.get(i)));

                if(!pattern_graph.nodes.get(i).checkNode(nodes.get(integerPermutationsInd.get(i))))
                    break;

                temp.addNode(nodes.get(integerPermutationsInd.get(i)), false);
            }
            nodePermutations.add(temp);
        }
        return nodePermutations;
    }

    private ArrayList<ArrayList<Integer>> getPermutations(ArrayList<Integer> values, int size)
    {
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
        if(size == 0){
            return result;
        }

        for(int i=0; i < values.size(); i++){
            ArrayList<Integer> clone = makeDeepCopyInteger(values);
            clone.remove(i);

            ArrayList<ArrayList<Integer>> tempResult = getPermutations(clone, size - 1);

            if(tempResult.size() == 0)
            {
                result.add(new ArrayList<Integer>());
                result.get(result.size() - 1).add(values.get(i));
            }

            for(ArrayList<Integer> tempResultIndividual : tempResult)
            {
                tempResultIndividual.add(0, values.get(i));
                result.add(tempResultIndividual);
            }
        }

        return result;
    }

    public int checkGraphSize()
    {
        return nodes.size();
    }

    public float checkAmountNodes(TVTropeType specific, boolean normalize)
    {
        float cumulative = 0.0f;

        for(GrammarNode node : nodes)
        {
            if(node.getGrammarNodeType() == specific)
                cumulative++;
        }

        if(normalize)
            return cumulative/(float) nodes.size();
        else
            return cumulative;
    }

    /**
     * This method returns the amount of nodes generic to the type, e.g., Hero, Enemy, Plot Device, Conflicts, etc.
     * If I ask for hero, the method will return all the nodes that are of type Hero and derivates (5ma, gunslinger, etc.)
     * @param generic_type I should check this, but please don't pass anything than the generics!
     * @param normalize
     * @return
     */
    public float checkGenericAmountNodes(TVTropeType generic_type, boolean normalize)
    {
        float cumulative = 0.0f;

        for(GrammarNode node : nodes)
        {
            if(node.getGrammarNodeType().getValue() >= generic_type.getValue()
            && node.getGrammarNodeType().getValue() < generic_type.getValue() + 10)
                cumulative++;
        }

        if(normalize)
            return cumulative/(float) nodes.size();
        else
            return cumulative;
    }

    public int checkUnconnectedNodes()
    {
        int unconnected = 0;
        boolean breakOut = false;

        //Check node by node that they are connected
        for(GrammarNode core : nodes)
        {
            breakOut = false;

            //Check if node has connections
            if(!core.connections.isEmpty())
                continue;

            //else lets check if any other has this node as a connection
            for(GrammarNode other : nodes)
            {
                if(breakOut)
                    break;

                if(core == other)
                    continue;

                if(other.checkConnectionExists(core))
                    breakOut = true;

            }

            if(breakOut)
                continue;
            else
                unconnected++;
        }

        return unconnected;
    }

    public int fullyConnectedGraph()
    {
        ArrayList<String> visitedConnections = new ArrayList<String>();
        ArrayList<GrammarNode> visitedNodes = new ArrayList<GrammarNode>();

        Queue<String> toCheckConnections = new LinkedList<String>();
        Queue<GrammarNode> toCheckNodes = new LinkedList<GrammarNode>();

        toCheckNodes.add(nodes.get(0));

        while(!toCheckNodes.isEmpty())
        {
            GrammarNode curNode = toCheckNodes.remove();
            String curConnection = "";

            if(!toCheckConnections.isEmpty())
                curConnection = toCheckConnections.remove();

            if(!curConnection.equals("") && !visitedConnections.contains(curConnection))
            {
                visitedConnections.add(curConnection);
            }

            //If we have already been here there is no need to check again
            if(visitedNodes.contains(curNode))
                continue;

            visitedNodes.add(curNode);

            //first add my connections
            //TODO: this works for every type of connection
            for(Map.Entry<GrammarNode, Integer> keyValue : curNode.connections.entrySet())
            {
                toCheckNodes.add(keyValue.getKey());
                toCheckConnections.add(Integer.toString(curNode.id)+Integer.toString(keyValue.getKey().id));
            }

            for(GrammarNode other : nodes)
            {
                if(other == curNode)
                    continue;

                if(other.checkConnectionExists(curNode))
                {
                    toCheckNodes.add(other);
                    toCheckConnections.add(Integer.toString(other.id)+Integer.toString(curNode.id));
                }
            }
        }

        //if we haven't visited all nodes we know this is not fully connected
//        return nodes.size() == visitedNodes.size();
        return nodes.size() - visitedNodes.size();

//        return true;
    }

    /***
     * This value when local is in effect, the diversity within the graph
     * While passing a false will return global diversity based on the nodes that can be used!
     * @param local True if you want local diversity
     * @return
     */
    public float getNodeDiversityBase()
    {
        HashMap<TVTropeType, Integer> nTypes = new HashMap<>();
        HashMap<TVTropeType, Integer> baseNTypes = new HashMap<>();
        float output = 0.0f;

        //Count nodes and repetition
        for(GrammarNode node : nodes)
        {
            if(nTypes.containsKey(node.grammarNodeType))
            {
                nTypes.put(node.grammarNodeType, nTypes.get(node.grammarNodeType) + 1);
            }
            else
                nTypes.put(node.grammarNodeType,1);
        }

        //If any node is repeated more than "min_freq", and is not part of the excluded nodes (special ones) we count it!
        for(Map.Entry<TVTropeType, Integer> keyValue : nTypes.entrySet())
        {
            if(keyValue.getKey().getValue() >= 40)
            {
                if(baseNTypes.containsKey(TVTropeType.PLOT_DEVICE))
                {
                    baseNTypes.put(TVTropeType.PLOT_DEVICE, baseNTypes.get(TVTropeType.PLOT_DEVICE) + 1);
                }
                else
                    baseNTypes.put(TVTropeType.PLOT_DEVICE,1);
            }
            else if(keyValue.getKey().getValue() >= 30)
            {
                if(baseNTypes.containsKey(TVTropeType.ENEMY))
                {
                    baseNTypes.put(TVTropeType.ENEMY, baseNTypes.get(TVTropeType.ENEMY) + 1);
                }
                else
                    baseNTypes.put(TVTropeType.ENEMY,1);
            }
            else if(keyValue.getKey().getValue() >= 20)
            {
                if(baseNTypes.containsKey(TVTropeType.CONFLICT))
                {
                    baseNTypes.put(TVTropeType.CONFLICT, baseNTypes.get(TVTropeType.CONFLICT) + 1);
                }
                else
                    baseNTypes.put(TVTropeType.CONFLICT,1);
            }
            else if(keyValue.getKey().getValue() >= 10)
            {
                if(baseNTypes.containsKey(TVTropeType.HERO))
                {
                    baseNTypes.put(TVTropeType.HERO, baseNTypes.get(TVTropeType.HERO) + 1);
                }
                else
                    baseNTypes.put(TVTropeType.HERO,1);
            }
            else
            {
                if(baseNTypes.containsKey(TVTropeType.ANY))
                {
                    baseNTypes.put(TVTropeType.ANY, baseNTypes.get(TVTropeType.ANY) + 1);
                }
                else
                    baseNTypes.put(TVTropeType.ANY,1);
            }
        }

        output = baseNTypes.size();

        return output/5.0f;
    }



    /***
     * This value when local is in effect, the diversity within the graph
     * While passing a false will return global diversity based on the nodes that can be used!
     * @param local True if you want local diversity
     * @return
     */
    public float getNodeDiversity(boolean local)
    {
        HashMap<TVTropeType, Integer> nTypes = new HashMap<>();
        float output = 0.0f;

        //Count nodes and repetition
        for(GrammarNode node : nodes)
        {
            if(nTypes.containsKey(node.grammarNodeType))
            {
                nTypes.put(node.grammarNodeType, nTypes.get(node.grammarNodeType) + 1);
            }
            else
                nTypes.put(node.grammarNodeType,1);
        }

        output = nTypes.size();

        if(local)
            return output/(float)nodes.size();

        return output/(float)TVTropeType.values().length;
    }

    public float SameNodes(int min_freq, TVTropeType ... excluded_nodes)
    {
        HashMap<TVTropeType, Integer> nTypes = new HashMap<>();
        float output = 0.0f;

        //Count nodes and repetition
        for(GrammarNode node : nodes)
        {
            if(nTypes.containsKey(node.grammarNodeType))
            {
                nTypes.put(node.grammarNodeType, nTypes.get(node.grammarNodeType) + 1);
            }
            else
                nTypes.put(node.grammarNodeType,1);
        }

        //If any node is repeated more than "min_freq", and is not part of the excluded nodes (special ones) we count it!
        for(Map.Entry<TVTropeType, Integer> keyValue : nTypes.entrySet())
        {
            if(keyValue.getValue() > min_freq && !Arrays.stream(excluded_nodes).anyMatch(keyValue.getKey()::equals))
            {
                output += keyValue.getValue();
            }
        }

        //Relative normalized value
        return output/(float)nodes.size();

    }

//    public List<GrammarNode

//    public boolean iterateGraph(List<GrammarNode> subgraphNodes, List<int[]> subgraphConnections, int step, GrammarNode startNode, int current_node)
//    {
//        ArrayList<ArrayList<GrammarNode>> result_nodes = new ArrayList<ArrayList<GrammarNode>>();
//        result_nodes.add(new ArrayList<GrammarNode>());
//
//        boolean correct = true;
//
//        for(int i = 0; i < subgraphConnections.size(); ++i)
//        {
//            //If the connection matches the current node number then we can use it!
//            if(subgraphConnections.get(i)[0] == current_node)
//            {
//                //index 0 = from connection
//                //index 1 = to connection
//                //index 2 = connection type
//                int[] connection = subgraphConnections.get(i);
//
//                //we return false because there is no connection (and we are expecting one)
//                if(startNode.connections.isEmpty())
//                    return false;
//
//                //Check this node's connections
//                for(Map.Entry<GrammarNode, Integer> keyValue : startNode.connections.entrySet())
//                {
//                    if(keyValue.getValue() == connection[2])
//                    {
//                        if(iterateGraph(subgraphNodes, subgraphConnections, step + 1, keyValue.getKey(), connection[1]))
//                        {
//                            result_nodes.get(0).add(startNode);
//                        }
//
//
//
//                        if(iterateGraph(subgraphNodes, subgraphConnections, step + 1, startNode, connection[1]))
//                        {
//
//                        }
//
//
//
////                    result_nodes = new ArrayList<GrammarNode>();
//                    }
//                }
//
//
//            }
//        }
//
//        return correct;
//
//
//        for(int i = 0; i < nodes.size(); ++i)
//        {
//            //index 0 = from connection
//            //index 1 = to connection
//            //index 2 = connection type
//            int[] connection = subgraphConnections.get(step);
//
//            //Check this node's connections
//            for(Map.Entry<GrammarNode, Integer> keyValue : startNode.connections.entrySet())
//            {
//                if(keyValue.getValue() == connection[2])
//                {
//                    result_nodes.add(new ArrayList<GrammarNode>());
//
//                    if(iterateGraph(subgraphNodes, subgraphConnections, step + 1, startNode, 0))
//                    {
//
//                    }
//
//
//
////                    result_nodes = new ArrayList<GrammarNode>();
//                }
//            }
//
//        }
//
//        for(GrammarNode node : nodes)
//        {
//
//
//
//        }
//
//        return true;
//    }

    public List<GrammarGraph> getSubGraphGraph(GrammarGraph subgraph)
    {
        ArrayList<GrammarGraph> subgraphs = new ArrayList<GrammarGraph>();

        return subgraphs;
    }

    public GrammarGraph getSubGraphNodes(List<GrammarNode> subgraphNodes)
    {
        GrammarGraph subgraph = new GrammarGraph();

        return subgraph;
    }

    public GrammarGraph getSubGraphIndices(List<Integer> subgraphNodesIndices)
    {
        GrammarGraph subgraph = new GrammarGraph();

        return subgraph;
    }

    public void cleanGraphics()
    {
        for(GrammarNode node : nodes)
        {
            node.clearGraphics();
        }
    }

    public void calculateAllDimensionalValues(GrammarGraph target_graph) //TODO:
    {
        dimensionValues = new HashMap<GADimensionGrammar.GrammarDimensionTypes, Double>();

        for(GADimensionGrammar.GrammarDimensionTypes dimension : GADimensionGrammar.GrammarDimensionTypes.values())
        {
            if(dimension != GADimensionGrammar.GrammarDimensionTypes.STRUCTURE && dimension != GADimensionGrammar.GrammarDimensionTypes.TENSION)
            {
                dimensionValues.put(dimension, GADimensionGrammar.calculateIndividualValue(dimension, this, target_graph));
            }
        }
    }

    public double getDimensionValue(GADimensionGrammar.GrammarDimensionTypes currentDimension)
    {
        if(dimensionValues == null || !dimensionValues.containsKey(currentDimension)) return -1.0;
        return dimensionValues.get(currentDimension);
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();

        for(GrammarNode node : nodes)
        {
            result.append(node.toString() + "\n");
        }

        return result.toString();
    }
}
