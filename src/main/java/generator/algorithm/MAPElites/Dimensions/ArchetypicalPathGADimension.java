package generator.algorithm.MAPElites.Dimensions;


import designerModeling.DesignerModel;
import designerModeling.archetypicalPaths.ArchetypicalPath;
import game.AlgorithmSetup;
import game.Room;
import game.Tile;
import game.tiles.BossEnemyTile;
import generator.algorithm.ZoneIndividual;

public class ArchetypicalPathGADimension extends GADimension {

	double patternMultiplier = 4.0;

	public ArchetypicalPathGADimension(float granularity)
	{
		super();
		dimension = DimensionTypes.ARCHETYPICAL_PATH;
		this.granularity = granularity;
	}

	@Override
	public double CalculateValue(ZoneIndividual individual, Room target) 
	{
		Room individualRoom = individual.getPhenotype().getMap(-1, -1, null, null, null);

		float persona_weight = ArchetypicalPath.distanceToFinalPath(
				DesignerModel.getInstance().designer_persona.specificArchetypicalPath(target),
				individualRoom.room_style.current_style);

		return persona_weight;

	}

	@Override
	public double CalculateValue(Room individualRoom, Room target) {

		float persona_weight = ArchetypicalPath.distanceToFinalPath(
				DesignerModel.getInstance().designer_persona.specificArchetypicalPath(target),
				individualRoom.room_style.current_style);

		return persona_weight;
	}
	
	public static double getValue(Room individualRoom)
	{
		return -1.0;
	}
}
