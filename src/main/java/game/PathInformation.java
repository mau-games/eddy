package game;

import java.util.HashMap;

/***
 * This class contains information related to the different types of paths, including:
 * The per tileType value per Pathtype
 * the Current pathType
 * A way of changing the pathType
 * TODO: The TileValue per pathtype should be loaded from a config file! 
 * @author Alberto Alvarez, Malmö University
 *
 */
public class PathInformation 
{
	private static PathInformation instance = null;
	
	public enum PathType {
		FASTEST,
		REWARDING,
		LESS_DANGER,
		MORE_DANGER
	}
	
	public PathType pathType;
	private HashMap<PathType, TileInformation> tilesInPathInfo = new HashMap<PathType, TileInformation>();
	
	private PathInformation()
	{
		pathType = PathType.FASTEST;
		
		//this should be loaded from a config file!!!!
		tilesInPathInfo.put(PathType.FASTEST, new TileInformation(1.0f, 1.0f, 1.0f, 1.0f));
		tilesInPathInfo.put(PathType.REWARDING, new TileInformation(1.0f, 1.0f, 0.01f, 10.0f));
		tilesInPathInfo.put(PathType.LESS_DANGER, new TileInformation(1.0f, 1.0f, 1.0f, 20.0f));
		tilesInPathInfo.put(PathType.MORE_DANGER, new TileInformation(1.0f, 1.0f, 1.0f, 0.1f));
	}

	public static PathInformation getInstance()
	{
		if(instance == null)
		{
			instance = new PathInformation();
		}
		
		return instance;
	}
	
	public void changePathType(PathType nextPathType)
	{
		pathType = nextPathType;
	}
	
	public PathType getPathType()
	{
		return pathType;
	}
	
	public TileInformation getTileInformation()
	{
		return tilesInPathInfo.get(pathType);
	}
	
}
