package game;

import finder.graph.Node;
import gui.controls.LabeledCanvas;
import gui.utils.MapRenderer;

public class RoomConfig
{
	private WorldViewCanvas worldGraphicNode;
	private Node<Room> node;
	private Room owner;
	
	//We should add a few values to make our life easier
	private int renderSizeWidth;
	private int renderSizeHeight;
	
	public RoomConfig(Room owner)
	{
		this.owner = owner;
		node = new Node<Room>(owner);
		worldGraphicNode = new WorldViewCanvas();
		worldGraphicNode.setViewSize((30 * this.owner.getColCount()) + 20, (30 * this.owner.getRowCount()) + 20);
		//This is a bit .......
		renderSizeHeight = (int)((float)MapRenderer.getInstance().getApplicationConfig().getMapRenderHeight() * (float)((float)owner.getRowCount() / 10.0f));
		renderSizeWidth = (int)((float)MapRenderer.getInstance().getApplicationConfig().getMapRenderWidth() * (float)((float)owner.getColCount() / 10.0f));
	}
	
	public WorldViewCanvas getWorldCanvas()
	{
		return worldGraphicNode;
	}
	
	public int getRenderSizeWidth() { return renderSizeWidth; }
	public int getRenderSizeHeight() { return renderSizeHeight; }
	
	public void setRenderSizeWidth(float value)
	{
		
	}
	
	public void setRenderSizeHeight(float value)
	{
		
	}
}
