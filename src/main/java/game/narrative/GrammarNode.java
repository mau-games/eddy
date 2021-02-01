package game.narrative;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import game.narrative.TVTropeType;
import util.Util;

import java.util.*;


public class GrammarNode {

    int id;

    //int is connection type! -- I don't know if all of this will be necessary
    //0 = no direction --> connection is on both
    //1 = unidirectional
    //2 = bidirectional
    HashMap<GrammarNode, Integer> connections;

    TVTropeType grammarNodeType;

    public GrammarNode(int id, TVTropeType nodeType)
    {
        this.connections = new HashMap<GrammarNode, Integer>();
        this.id = id;
        this.grammarNodeType = nodeType;
    }

    public boolean addConnection(GrammarNode otherNode, int connectionType)
    {
        if(connections.containsKey(otherNode)) //perhaps no if we are interested in having this type of connection
            return false;

        connections.put(otherNode, connectionType);
        return true;
    }

    public void removeAllConnection()
    {
        //First remove all connections to this node
        for(GrammarNode connection : connections.keySet())
        {
            connection.removeConnection(this);
        }

        //Then remove all connections from this node.
        // For now, this is okay, but in reality i should call some type of destructor
        connections = new HashMap<GrammarNode, Integer>();

    }

    public boolean removeRndConnection()
    {
        if(connections.isEmpty())
            return false;

        List<GrammarNode> keysAsArray = new ArrayList<GrammarNode>(connections.keySet());
        GrammarNode connection = keysAsArray.get(Util.getNextInt(0, keysAsArray.size()));

        connection.removeConnection(this);
        connections.remove(connection);

        return true;
    }

    public int removeConnection(GrammarNode otherNode)
    {
        int connectionType = -1; // -1 = no connection

        if(connections.containsKey(otherNode))
        {
            connectionType = connections.get(otherNode);
            connections.remove(otherNode);
        }

        return connectionType;
    }

    public boolean checkConnectionExists(GrammarNode otherNode)
    {
        return connections.containsKey(otherNode);
    }

    public int checkConnection()
    {
        return -1;
    }

    public boolean checkNode(GrammarNode testedNode)
    {
        if(testedNode.grammarNodeType == TVTropeType.ANY || grammarNodeType == TVTropeType.ANY ||
                testedNode.grammarNodeType == grammarNodeType)
            return true;

        return false;
    }

    public HashMap<GrammarNode, Integer> getChildrenClone()
    {
        HashMap<GrammarNode, Integer> result = new HashMap<GrammarNode, Integer>();

        for(Map.Entry<GrammarNode, Integer> keyValue : connections.entrySet())
        {
            result.put(keyValue.getKey(), keyValue.getValue());
        }
        return result;
    }

    /// <summary>
    /// get a subset of the children that are in a certain graph
    /// </summary>
    /// <param name="graph">the mission graph that need to be checked</param>
    /// <returns>a subset of the children that are in the input graph</returns>
    public HashMap<GrammarNode, Integer> getFilteredChildren(GrammarGraph graph)
    {
//        HashMap<GrammarNode, Integer> children = this.getChildrenClone();
        HashMap<GrammarNode, Integer> children = new HashMap<GrammarNode, Integer>();
//        HashMap<GrammarNode, Integer> result = new HashMap<GrammarNode, Integer>();

        for(Map.Entry<GrammarNode, Integer> connection : connections.entrySet())
        {
            int childIndex = graph.getNodeIndex(connection.getKey());
            if(childIndex != -1)
            {
                children.put(connection.getKey(), connection.getValue());
            }
        }

        return children;
    }

    /// <summary>
    /// get a subset of the children that are in a certain graph
    /// </summary>
    /// <param name="graph">the mission graph that need to be checked</param>
    /// <returns>a subset of the children that are in the input graph</returns>
    public ArrayList<GrammarNode> getFilteredChildrenNodes(GrammarGraph graph)
    {
//        HashMap<GrammarNode, Integer> children = this.getChildrenClone();
//        ArrayList<GrammarNode> childrenClone = new ArrayList<>(children.keySet());
//        HashMap<GrammarNode, Integer> result = new HashMap<GrammarNode, Integer>();

        ArrayList<GrammarNode> children = new ArrayList<GrammarNode>();

        for(Map.Entry<GrammarNode, Integer> connection : connections.entrySet())
        {
            int childIndex = graph.getNodeIndex(connection.getKey());
            if(childIndex != -1)
            {
                children.add(connection.getKey());
            }
        }

        return children;
    }

    @Override
    public String toString() {
        String result = grammarNodeType + "_Node_" + id + " - Connections: ";

        for(Map.Entry<GrammarNode, Integer> keyValue : connections.entrySet())
        {
            //0 = no direction --> connection is on both
            //1 = unidirectional
            //2 = bidirectional

            String connection_type = "";

            switch(keyValue.getValue())
            {
                case 0: connection_type = "No Direction"; break;
                case 1: connection_type = "Unidirectional"; break;
                case 2: connection_type = "Bidirectional"; break;
            }

//            result += "(" + keyValue.getKey().id + "," + keyValue.getValue() + ") ";
            result += "(" + keyValue.getKey().id + "," + connection_type + ") ";

        }

        return result;
    }

    public void setID(int id) {this.id = id;}
    public int getID() {return id;}

    public TVTropeType getGrammarNodeType() {return grammarNodeType;}
    public void setGrammarNodeType(TVTropeType type){grammarNodeType = type;}

}
