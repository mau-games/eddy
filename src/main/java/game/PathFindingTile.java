package game;

import util.Point;

public class PathFindingTile implements Comparable<PathFindingTile>
{
	PathFindingTile parent;
	Point position;
	TileTypes type;
	
	public float gScore = 0.0f;
	public float hScore = 0.0f;
	public float fScore = 0.0f;
	
	public PathFindingTile(Point position, Point endPosition, TileTypes type)
	{	
		this.position = position;
		this.type = type;
		
		calculateG();
		calculateH(endPosition);
		fScore = gScore + hScore;
	}
	
	public PathFindingTile(PathFindingTile parent, Point position, Point endPosition, TileTypes type)
	{
		this.parent = parent;
		this.position = position;
		this.type = type;
		
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
			gScore = parent.gScore + 1;
		}
		else
		{
			gScore = 1;
		}
	}
	
	private void calculateH(Point endPosition)
	{
		hScore = Math.abs(endPosition.getX() - position.getX()) + Math.abs(endPosition.getY() - position.getY());
	}
	
	public void updateTile(PathFindingTile newParent)
	{
		this.parent = newParent;
		calculateG();
		fScore = gScore + hScore;
	}
	
	public boolean testFScore(PathFindingTile next)
	{
		float prevFScore = gScore + hScore;
		float newFScore = (next.gScore + 1) + hScore;
		
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