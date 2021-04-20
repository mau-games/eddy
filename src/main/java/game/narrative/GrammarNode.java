package game.narrative;

import com.sun.org.apache.xerces.internal.xni.grammars.Grammar;
import game.narrative.TVTropeType;
import gui.controls.NarrativeShape;
import util.Point;
import util.Util;

import java.util.*;


public class GrammarNode {

    int id;

    //int is connection type! -- I don't know if all of this will be necessary
    //0 = no direction --> connection is on both
    //1 = unidirectional
    //2 = bidirectional
    public HashMap<GrammarNode, Integer> connections;
    TVTropeType grammarNodeType;

    //Graphical stuff!
    NarrativeShape graphic_element;
    //To which node i am connected, the graphic element.
    HashMap<GrammarNode, NarrativeShapeEdge> graphic_connection;

    public GrammarNode(int id, TVTropeType nodeType)
    {
        this.connections = new HashMap<GrammarNode, Integer>();
        this.id = id;
        this.grammarNodeType = nodeType;
    }

    /**
     * For now, we are not allowing more than 1 connection between nodes!
     * Maybe if the connection is bidir but you cannot have bidir it should change to directional??
     * @param otherNode
     * @param connectionType
     * @return
     */
    public boolean addConnection(GrammarNode otherNode, int connectionType)
    {
        if(connections.containsKey(otherNode)) //perhaps no if we are interested in having this type of connection
            return false;

        //NOT ALLOWING MORE THAN ONE CONNECTION (FOR NOW!)
        if(otherNode.connections.containsKey(this))
            return false;



        //So bidirection or no direction
//        if(connectionType != 1)
//            otherNode.addConnection(this, connectionType);

        //So bidirection
        if(connectionType > 1)
        {
            if(otherNode.addConnection_bidir(this, connectionType))
            {
                connections.put(otherNode, connectionType);
            }
            else {
                connections.put(otherNode, 1); //We do a unidir connection!"
            }
        }
        else
        {
            connections.put(otherNode, connectionType);
        }

        return true;
    }

    /**
     * If it is bidir
     * @param otherNode
     * @param connectionType
     * @return
     */
    protected boolean addConnection_bidir(GrammarNode otherNode, int connectionType)
    {
        if(connections.containsKey(otherNode)) //perhaps no if we are interested in having this type of connection
            return false;

        connections.put(otherNode, connectionType);

        return true;
    }

    public void removeAllMyConnections()
    {
        connections = new HashMap<GrammarNode, Integer>();
    }

    //TODO: This is not correct
    public void removeAllConnection()
    {
        //First remove all connections to this node
        for(GrammarNode connection : connections.keySet())
        {
            connection.removeConnection(this, false);

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

        connection.removeConnection(this, true);
        connections.remove(connection);

        return true;
    }

    public int removeConnection(GrammarNode otherNode, boolean removeOtherDir)
    {
        int connectionType = -1; // -1 = no connection

        if(connections.containsKey(otherNode))
        {
            connectionType = connections.get(otherNode);
            connections.remove(otherNode);

            //So bidirection or no direction
            if(removeOtherDir && connectionType == 2)
                otherNode.removeConnection(this, true);
//            if(removeOtherDir && connectionType != 1) //todo: problem here!
//                otherNode.removeConnection(this, true);
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


    /***
     * This will only be required when wanting to paint the graph.
     * But until then we try to avoid doing anything with graphics!
     * @return the graphics shape of this node!
     */
    public NarrativeShape getNarrativeShape()
    {
        if(graphic_element == null)
        {
            graphic_element = new NarrativeShape(grammarNodeType, this);
        }

        return graphic_element;
    }

    /***
     * This will only be required when wanting to paint the graph.
     * But until then we try to avoid doing anything with graphics!
     * @return the graphics shape of this node!
     */
    public List<NarrativeShapeEdgeLine> getNarrativeShapeConnections()
    {
        ArrayList<NarrativeShapeEdgeLine> return_edges = new ArrayList<NarrativeShapeEdgeLine>();

        if(graphic_connection == null)
            graphic_connection = new HashMap<GrammarNode, NarrativeShapeEdge>();

        for(Map.Entry<GrammarNode, Integer> keyValue : connections.entrySet())
        {
            //We don't have a graphic representation of the connection
            if(!graphic_connection.containsKey(keyValue.getKey()))
            {
                NarrativeShapeEdge nse = new NarrativeShapeEdge(this.getNarrativeShape(),
                        keyValue.getKey().getNarrativeShape(),
                        this.getNarrativeShape().getRNDPositionShapeBorder(),
                        keyValue.getKey().getNarrativeShape().getRNDPositionShapeBorder(),
                        keyValue.getValue());

                graphic_connection.put(keyValue.getKey(), nse);
                return_edges.add(nse.graphicElement);
            }
            else
                return_edges.add(graphic_connection.get(keyValue.getKey()).graphicElement);

        }

        return return_edges;
    }

    /***
     * This will only be required when wanting to paint the graph.
     * But until then we try to avoid doing anything with graphics!
     * @return the graphics shape of this node!
     */
    public List<NarrativeShapeEdgeLine> recreateConnectionsBasedPosition()
    {
        ArrayList<NarrativeShapeEdgeLine> return_edges = new ArrayList<NarrativeShapeEdgeLine>();
        graphic_connection = new HashMap<GrammarNode, NarrativeShapeEdge>();

        for(Map.Entry<GrammarNode, Integer> keyValue : connections.entrySet())
        {
            //We don't have a graphic representation of the connection
            if(!graphic_connection.containsKey(keyValue.getKey()))
            {
                Point from;
                Point to;

                double dx = Math.abs(this.getNarrativeShape().xPosition.get() - keyValue.getKey().getNarrativeShape().xPosition.get());
                double dy = Math.abs(this.getNarrativeShape().yPosition.get() - keyValue.getKey().getNarrativeShape().yPosition.get());

                if(dx > dy)
                {
                    if(this.getNarrativeShape().xPosition.get() > keyValue.getKey().getNarrativeShape().xPosition.get()) //From is greater than To in X
                    {
                        from = this.getNarrativeShape().getPositionShapeBorder(3);
                        to = keyValue.getKey().getNarrativeShape().getPositionShapeBorder(1);
                    }
                    else
                    {
                        from = this.getNarrativeShape().getPositionShapeBorder(1);
                        to = keyValue.getKey().getNarrativeShape().getPositionShapeBorder(3);
                    }
                }
                else
                {
                    if(this.getNarrativeShape().yPosition.get() > keyValue.getKey().getNarrativeShape().yPosition.get())
                    {
                        from = this.getNarrativeShape().getPositionShapeBorder(0);
                        to = keyValue.getKey().getNarrativeShape().getPositionShapeBorder(2);
                    }
                    else
                    {
                        from = this.getNarrativeShape().getPositionShapeBorder(2);
                        to = keyValue.getKey().getNarrativeShape().getPositionShapeBorder(0);
                    }
                }

//                if(this.getNarrativeShape().xPosition.get() > keyValue.getKey().getNarrativeShape().xPosition.get()) //From is greater than To in X
//                {
//                    double dx = this.getNarrativeShape().xPosition.get() - keyValue.getKey().getNarrativeShape().xPosition.get();
//
//                    if(this.getNarrativeShape().yPosition.get() > keyValue.getKey().getNarrativeShape().yPosition.get())
//                    {
//                        double dy = this.getNarrativeShape().yPosition.get() - keyValue.getKey().getNarrativeShape().yPosition.get();
//
//                        if(dx > dy)
//                        from = this.getNarrativeShape().getPositionShapeBorder(0);
//                        to = keyValue.getKey().getNarrativeShape().getPositionShapeBorder(2);
//                    }
//                    else
//                    {
//                        from = this.getNarrativeShape().getPositionShapeBorder(2);
//                        to = keyValue.getKey().getNarrativeShape().getPositionShapeBorder(0);
//                    }
//                }
//                else{ //From is lesser than To in X
//
//                    if(this.getNarrativeShape().yPosition.get() > keyValue.getKey().getNarrativeShape().yPosition.get())
//                    {
//                        from = this.getNarrativeShape().getPositionShapeBorder(0);
//                        to = keyValue.getKey().getNarrativeShape().getPositionShapeBorder(2);
//                    }
//                    else
//                    {
//                        from = this.getNarrativeShape().getPositionShapeBorder(2);
//                        to = keyValue.getKey().getNarrativeShape().getPositionShapeBorder(0);
//                    }
//                }

                NarrativeShapeEdge nse = new NarrativeShapeEdge(this.getNarrativeShape(),
                        keyValue.getKey().getNarrativeShape(),
                        from,
                        to,
                        keyValue.getValue());

                graphic_connection.put(keyValue.getKey(), nse);
                return_edges.add(nse.graphicElement);
            }
            else
                return_edges.add(graphic_connection.get(keyValue.getKey()).graphicElement);

        }

        return return_edges;
    }

}
