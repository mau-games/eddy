package game;

import java.util.HashMap;

import util.Point;

public class PathFindingTile implements Comparable<PathFindingTile>
{
	PathFindingTile parent;
	Point position;
	TileTypes type;
	
	public float gScore = 0.0f;
	public float hScore = 0.0f;
	public float fScore = 0.0f;
	
	private float roomSize;
	
	public PathFindingTile(Point position, Point endPosition, TileTypes type, float roomWidth, float roomHeight)
	{	
		this.position = position;
		this.type = type;
		this.roomSize = roomWidth + roomHeight;
		
		calculateG();
		calculateH(endPosition);
		fScore = gScore + hScore;
	}
	
	public PathFindingTile(PathFindingTile parent, Point position, Point endPosition, TileTypes type, float roomWidth, float roomHeight)
	{
		this.parent = parent;
		this.position = position;
		this.type = type;
		this.roomSize = roomWidth + roomHeight;
		
		calculateG();
		calculateH(endPosition);
		fScore = gScore + hScore;
	}
	
	//Quite simple at the moment
	private void calculateG()
	{
		//PUTTING MORE WEIGHT TO ENEMIES
//		if(parent != null)
//		{
//			gScore = parent.gScore;
//			gScore += type.isEnemy() ? 5 : 1;
//		}
//		else
//		{
//			gScore = type.isEnemy() ? 5 : 1;
//		}
		
		if(parent != null)
		{
			gScore = parent.gScore + PathInformation.getInstance().getTileInformation().getStepToTileValue(this.type);
		}
		else
		{
			gScore = PathInformation.getInstance().getTileInformation().getStepToTileValue(this.type);
		}
	}
	
	//We normalize the value
	private void calculateH(Point endPosition)
	{
		hScore = ((Math.abs(endPosition.getX() - position.getX()) + Math.abs(endPosition.getY() - position.getY()))/roomSize)*PathInformation.getInstance().getTileInformation().getStepToTileValue(this.type);
	}
	
	public void updateTile(PathFindingTile newParent)
	{
		this.parent = newParent;
		calculateG();
		fScore = gScore + hScore;
	}
	
	public boolean testFScore(PathFindingTile newcomer)
	{
		float prevFScore = gScore + hScore;
//		float newFScore = (next.gScore + 1) + hScore; //TODO: Why is it a 1 here?
		float newFScore = (newcomer.gScore +  PathInformation.getInstance().getTileInformation().getStepToTileValue(this.type)) + hScore; //TODO: Why is it a 1 here?
		
		return newFScore < prevFScore;
		
	}
	
	public boolean equals(PathFindingTile p){
		return this.position.equals(p);
	}

	@Override
	public int compareTo(PathFindingTile o) 
	{
		return (int) (this.fScore - o.fScore);
	}
	
}