package machineLearning;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import java.util.Stack;

import game.Room;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import gui.views.TinderViewController;
import machineLearning.neuralnetwork.DataTupleManager;
import machineLearning.neuralnetwork.MapPreferenceModelTuple;
import machineLearning.neuralnetwork.PreferenceModelDataTuple;
import util.eventrouting.EventRouter;
import util.eventrouting.events.UpdatePreferenceModel;

/****
 * This model is a very big simplification!
 * @author Alberto Alvarez, Malmö University
 *
 */
public class PreferenceModel 
{
	public enum PreferenceAttributes
	{
		SYMMETRY,
		WALL_DENSITY,
		WALL_SPARSITY,
		ENEMY_DENSITY,
		ENEMY_SPARSITY,
		TREASURE_DENSITY,
		TREASURE_SPARSITY,
		CHALLENGE
	}
	
	public HashMap<PreferenceAttributes, Double> preferences = new HashMap<PreferenceAttributes, Double>();
	
	protected double learningRate = 0.2;
	public Room room;
	public boolean like;
	
	Stack<PreferenceModel> prevStates;
	protected static DecimalFormat df3 = new DecimalFormat("#.###");
	protected String projectPath;
	
	protected ArrayList<PreferenceModelDataTuple> tuples = new ArrayList<PreferenceModelDataTuple>();
	protected ArrayList<MapPreferenceModelTuple> mapTuples = new ArrayList<MapPreferenceModelTuple>();
	
	protected HashMap<Integer, ArrayList<MapPreferenceModelTuple>> separateMapDataset = new HashMap<Integer, ArrayList<MapPreferenceModelTuple>>();
	
	private boolean broadcasted = false;
	
	public PreferenceModel(HashMap<PreferenceAttributes, Double> otherPreferences, Room room, boolean like)
	{
		this.preferences = new HashMap<PreferenceAttributes, Double>(otherPreferences);
		this.room = room;
		this.like = like;
	}
	
	public PreferenceModel()
	{
		for(PreferenceAttributes attribute : PreferenceAttributes.values())
		{
			if(attribute.equals(PreferenceAttributes.SYMMETRY))
			{
				preferences.put(attribute, 0.5);
			}
			else
			{
				preferences.put(attribute, 0.2);
			}
		}
		
		prevStates = new Stack<PreferenceModel>();
		projectPath = System.getProperty("user.dir") + "\\my-data\\PreferenceModels";
//		symmetry = 0.5;
//		density = 0.5;
//		challenge = 0.5;
	}
	
	public void UpdateModel(boolean like, Room usedRoom, int step)
	{
		//This should be for each vaLUE
//		double newSym = (usedRoom.getDimensionValue(DimensionTypes.SYMMETRY) - symmetry) * learningRate;
//		symmetry = dislike ? symmetry + (newSym * -1.0) : symmetry + newSym;
//		symmetry = Math.max(Math.min(symmetry, 1.0), 0.0);
		
		for (Entry<PreferenceAttributes, Double> entry : preferences.entrySet())
		{
			double auxAtt = entry.getValue(), auxAttPrime = 0.0;
			
			switch(entry.getKey())
			{
			case CHALLENGE:
				break;
			case ENEMY_DENSITY:
				auxAttPrime = (usedRoom.calculateEnemyDensity() - auxAtt) * learningRate;
				auxAtt = !like ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			case ENEMY_SPARSITY:
				auxAttPrime = (usedRoom.calculateEnemySparsity() - auxAtt) * learningRate;
				auxAtt = !like ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			case SYMMETRY:
				auxAttPrime = (usedRoom.getDimensionValue(DimensionTypes.SYMMETRY) - auxAtt) * learningRate;
				auxAtt = !like ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			case TREASURE_DENSITY:
				auxAttPrime = (usedRoom.calculateTreasureDensity() - auxAtt) * learningRate;
				auxAtt = !like ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			case TREASURE_SPARSITY:
				auxAttPrime = (usedRoom.calculateTreasureSparsity() - auxAtt) * learningRate;
				auxAtt = !like ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			case WALL_DENSITY:
				auxAttPrime = (usedRoom.calculateWallDensity() - auxAtt) * learningRate;
				auxAtt = !like ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			case WALL_SPARSITY:
				auxAttPrime = (usedRoom.calculateWallSparsity() - auxAtt) * learningRate;
				auxAtt = !like ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			default:
				break;
			
			}
			
			preferences.put(entry.getKey(), auxAtt);
		}
		
		prevStates.push(new PreferenceModel(preferences, new Room(usedRoom), like));
		tuples.add(new PreferenceModelDataTuple(prevStates.peek(), like));
		mapTuples.add(new MapPreferenceModelTuple(usedRoom, like));
	}
	
	public void broadcastPreferences()
	{
		if(!broadcasted)
		{
			EventRouter.getInstance().postEvent(new UpdatePreferenceModel(this));
			broadcasted = true;
		}
		else
		{
			EventRouter.getInstance().postEvent(new UpdatePreferenceModel(null));
			broadcasted = false;
		}

	}
	
	public double testWithPreference(Room room)
	{
		double preferenceValue = 0.0;
		
		for (Entry<PreferenceAttributes, Double> entry : preferences.entrySet())
		{			
			switch(entry.getKey())
			{
			case CHALLENGE:
				break;
			case ENEMY_DENSITY:
				preferenceValue += 1 - Math.abs(room.calculateEnemyDensity() - entry.getValue()); 
				break;
			case ENEMY_SPARSITY:
				preferenceValue += 1 - Math.abs(room.calculateEnemySparsity() - entry.getValue()); 
				break;
			case SYMMETRY:
				preferenceValue += 1- Math.abs(room.getDimensionValue(DimensionTypes.SYMMETRY) - entry.getValue()); 
				break;
			case TREASURE_DENSITY:
				preferenceValue += 1 - Math.abs(room.calculateTreasureDensity() - entry.getValue()); 
				break;
			case TREASURE_SPARSITY:
				preferenceValue += 1 - Math.abs(room.calculateTreasureSparsity() - entry.getValue()); 
				break;
			case WALL_DENSITY:
				preferenceValue += 1 - Math.abs(room.calculateWallDensity() - entry.getValue()); 
				break;
			case WALL_SPARSITY:
				preferenceValue += 1 - Math.abs(room.calculateWallSparsity() - entry.getValue()); 
				break;
			default:
				break;
			
			}
		}
		
//		System.out.println(PreferenceAttributes.values().length);
		
		if(preferenceValue/(PreferenceAttributes.values().length - 1) > 0.99)
		{
			System.out.println("PREF!");
		}
		
		return preferenceValue/(PreferenceAttributes.values().length - 1);
		
	}
	
	public void printAllStates()
	{
//		Iterator<PreferenceModel> iter = prevStates.iterator();
		System.out.println("_______________________________");
		for(PreferenceModel obj : prevStates)
		{
			System.out.println("          ROOM               INTERNAL");
			System.out.println("__________________________________________");
			for (Entry<PreferenceAttributes, Double> entry : obj.preferences.entrySet())
			{
				switch(entry.getKey())
				{
				case CHALLENGE:
					break;
				case ENEMY_DENSITY:
					System.out.println("Enemy Density: " + df3.format(obj.room.calculateEnemyDensity()) + " | " + df3.format(entry.getValue()));
					break;
				case ENEMY_SPARSITY:
					System.out.println("Enemy Sparsity: " + df3.format(obj.room.calculateEnemySparsity()) + " | " + df3.format(entry.getValue()));
					break;
				case SYMMETRY:
					System.out.println("Symmetry: " + df3.format(obj.room.getDimensionValue(DimensionTypes.SYMMETRY) ) + " | " + df3.format(entry.getValue()));
					break;
				case TREASURE_DENSITY:
					System.out.println("Treasure Density: " + df3.format(obj.room.calculateTreasureDensity()) + " | " + df3.format(entry.getValue()));
					break;
				case TREASURE_SPARSITY:
					System.out.println("Treasure Sparsity: " + df3.format(obj.room.calculateTreasureSparsity()) + " | " + df3.format(entry.getValue()));
					break;
				case WALL_DENSITY:
					System.out.println("Wall Density: " + df3.format(obj.room.calculateWallDensity()) + " | " + df3.format(entry.getValue()));
					break;
				case WALL_SPARSITY:
					System.out.println("Wall Sparsity: " + df3.format(obj.room.calculateWallSparsity()) + " | " + df3.format(entry.getValue()));
					break;
				default:
					break;
				
				}
			}
//			System.out.println(obj.room.getDimensionValue(DimensionTypes.SYMMETRY) + ", Dislike? "+  obj.dislike);
//			System.out.println("Sym: " + obj.symmetry + ", Challenge: " + obj.challenge + ", density: " + obj.wallDensity );
//			System.out.println();
//			System.out.println(obj.room.toString());
			System.out.println("_______________________________");
			System.out.println();
		}

	}
	
	public void SaveMapTuples(String userName)
	{
		DataTupleManager.SaveHeader(mapTuples.get(0), "\\PreferenceModels", userName + "_map");
		
		for(int i = 0; i < mapTuples.size() ;i++)
		{
			DataTupleManager.SaveData(mapTuples.get(i), "\\PreferenceModels", userName + "_map");
		}
	}
	
	public void SaveMapDataset(String userName)
	{
		for (Map.Entry<Integer, ArrayList<MapPreferenceModelTuple>> dataset : separateMapDataset.entrySet())
		{
			DataTupleManager.SaveHeader(dataset.getValue().get(0), "\\PreferenceModels", userName + "_map_" + dataset.getKey());
			
			for(int i = 0; i < dataset.getValue().size() ;i++)
			{
				DataTupleManager.SaveData(dataset.getValue().get(i), "\\PreferenceModels", userName + "_map_" + dataset.getKey());
			}
		}
	}
	
	
	public void SaveDataset(String userName)
	{
		DataTupleManager.SaveHeader(tuples.get(0), "\\PreferenceModels", userName);
		DataTupleManager.SaveHeader(mapTuples.get(0), "\\PreferenceModels", userName + "_map");
		
		for(int i = 0; i < tuples.size() ;i++)
		{
			DataTupleManager.SaveData(tuples.get(i), "\\PreferenceModels", userName);
			DataTupleManager.SaveData(mapTuples.get(i), "\\PreferenceModels", userName + "_map");
		}
//		
//		for(PreferenceModelDataTuple tuple : tuples)
//		{
//			DataTupleManager.SaveData(tuple, "\\PreferenceModels", userName);
//		}
		
//		File file = new File(projectPath + "\\" + userName +".csv");
//		
//		try {
//			
//			
//			StringBuilder prefTuple = new StringBuilder();
//			prefTuple.append("symmetry;enemyDensity;enemySparsity;treasureDensity;treasureSparsity;wallDensity;wallSparsity;numberSpatialPat;numberMesoPat;class" + System.lineSeparator());
//			FileUtils.write(file, prefTuple.toString(), true);
//			
//			for(PreferenceModel obj : prevStates)
//			{
//				prefTuple = new StringBuilder();
//				
//				prefTuple.append(obj.room.getDimensionValue(DimensionTypes.SYMMETRY) + ";");
//				prefTuple.append(obj.room.calculateEnemyDensity()+ ";");
//				prefTuple.append(obj.room.calculateEnemySparsity()+ ";");
//				prefTuple.append(obj.room.calculateTreasureDensity()+ ";");
//				prefTuple.append(obj.room.calculateTreasureSparsity()+ ";");
//				prefTuple.append(obj.room.calculateWallDensity()+ ";");
//				prefTuple.append(obj.room.calculateWallSparsity()+ ";");
//				prefTuple.append(obj.room.getDimensionValue(DimensionTypes.NUMBER_PATTERNS)+ ";");
//				prefTuple.append(obj.room.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN)+ ";");
//				prefTuple.append(obj.like + System.lineSeparator());
//				
//				FileUtils.write(file, prefTuple.toString(), true);
//			}
//
//		} catch (IOException e1) {
//			
//		}
	}
}
