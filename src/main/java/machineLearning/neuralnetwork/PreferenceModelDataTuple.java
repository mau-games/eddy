package machineLearning.neuralnetwork;

import java.util.ArrayList;

import game.Room;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import machineLearning.PreferenceModel;

public class PreferenceModelDataTuple extends DataTuple 
{
	private PreferenceModel modelInstance = null;
	
	protected double roomSymmetry;
	protected double roomMesoPatterns;
	protected double roomSpatialPatterns;
	protected double enemyDensity;
	protected double enemySparsity;
	protected double treasureDensity;
	protected double treasureSparsity;
	protected double wallDensity;
	protected double wallSparsity;
	
	protected boolean like;

	public PreferenceModelDataTuple(PreferenceModel model, boolean like)
	{
		this.roomSymmetry = model.room.getDimensionValue(DimensionTypes.SYMMETRY); 
		this.roomMesoPatterns = model.room.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN); 
		this.roomSpatialPatterns = model.room.getDimensionValue(DimensionTypes.NUMBER_PATTERNS); 
		this.enemyDensity = model.room.calculateEnemyDensity();
		this.enemySparsity = model.room.calculateEnemySparsity();
		this.treasureDensity  = model.room.calculateTreasureDensity();
		this.treasureSparsity  = model.room.calculateTreasureSparsity();
		this.wallDensity = model.room.calculateWallDensity();
		this.wallSparsity = model.room.calculateWallSparsity();
		
		this.like = like;
		
		this.numericalData = new ArrayList<Double>();
		this.numericalData.add(this.roomSymmetry);
		this.numericalData.add(this.roomMesoPatterns);
		this.numericalData.add(this.roomSpatialPatterns);
		this.numericalData.add(this.enemyDensity);
		this.numericalData.add(this.enemySparsity);
		this.numericalData.add(this.treasureDensity);
		this.numericalData.add(this.treasureSparsity);
		this.numericalData.add(this.wallDensity);
		this.numericalData.add(this.wallSparsity);
		this.label = like;
		
	}
	
	public PreferenceModelDataTuple(Room room, boolean like)
	{
		this.roomSymmetry = room.getDimensionValue(DimensionTypes.SYMMETRY); 
		this.roomMesoPatterns = room.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN); 
		this.roomSpatialPatterns = room.getDimensionValue(DimensionTypes.NUMBER_PATTERNS); 
		this.enemyDensity = room.calculateEnemyDensity();
		this.enemySparsity = room.calculateEnemySparsity();
		this.treasureDensity  = room.calculateTreasureDensity();
		this.treasureSparsity  = room.calculateTreasureSparsity();
		this.wallDensity = room.calculateWallDensity();
		this.wallSparsity = room.calculateWallSparsity();
		
		this.like = like;
		
		this.numericalData = new ArrayList<Double>();
		this.numericalData.add(this.roomSymmetry);
		this.numericalData.add(this.roomMesoPatterns);
		this.numericalData.add(this.roomSpatialPatterns);
		this.numericalData.add(this.enemyDensity);
		this.numericalData.add(this.enemySparsity);
		this.numericalData.add(this.treasureDensity);
		this.numericalData.add(this.treasureSparsity);
		this.numericalData.add(this.wallDensity);
		this.numericalData.add(this.wallSparsity);
		this.label = like;
		
	}
	
	public PreferenceModelDataTuple(String data)
	{
		String[] dataSplit = data.split(";");
		
		this.like = Boolean.parseBoolean(dataSplit[0]); //class
		
		this.roomSymmetry = Double.parseDouble(dataSplit[1]);   
		this.roomMesoPatterns = Double.parseDouble(dataSplit[2]);
		this.roomSpatialPatterns = Double.parseDouble(dataSplit[3]); 
		this.enemyDensity = Double.parseDouble(dataSplit[4]);                                  
		this.enemySparsity = Double.parseDouble(dataSplit[5]);                                
		this.treasureDensity  = Double.parseDouble(dataSplit[6]);                           
		this.treasureSparsity  =Double.parseDouble(dataSplit[7]);                        
		this.wallDensity = Double.parseDouble(dataSplit[8]);                                    
		this.wallSparsity =Double.parseDouble(dataSplit[9]);     
		
		this.numericalData = new ArrayList<Double>();
		this.numericalData.add(this.roomSymmetry);
		this.numericalData.add(this.roomMesoPatterns);
		this.numericalData.add(this.roomSpatialPatterns);
		this.numericalData.add(this.enemyDensity);
		this.numericalData.add(this.enemySparsity);
		this.numericalData.add(this.treasureDensity);
		this.numericalData.add(this.treasureSparsity);
		this.numericalData.add(this.wallDensity);
		this.numericalData.add(this.wallSparsity);
		this.label = like;
	}
	
	@Override
	public String getSaveString()
	{
		StringBuilder prefTuple = new StringBuilder();
		
		prefTuple.append(this.like + ";");
		prefTuple.append(this.roomSymmetry + ";");
		prefTuple.append(this.roomMesoPatterns+ ";");
		prefTuple.append(this.roomSpatialPatterns+ ";");
		prefTuple.append(this.enemyDensity + ";");
		prefTuple.append(this.enemySparsity + ";");
		prefTuple.append(this.treasureDensity + ";");
		prefTuple.append(this.treasureSparsity + ";");
		prefTuple.append(this.wallDensity + ";");
		prefTuple.append(this.wallSparsity + System.lineSeparator());

		return prefTuple.toString();
	}
	
	@Override
	public String getHeader() 
	{
		return "class;symmetry;numberMesoPat;numberSpatialPat;enemyDensity;enemySparsity;treasureDensity;treasureSparsity;wallDensity;wallSparsity" + System.lineSeparator();
	}
}
