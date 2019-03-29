package ml;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Stack;

import game.Room;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import gui.views.TinderViewController;

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
	
	double symmetry;
	double wallDensity;
	double wallSparsity;
	double enemyDensity;
	double enemySparsity;
	double treasureDensity;
	double treasureSparsity;
	double challenge;
	
	double learningRate = 0.2;
	Room room;
	boolean dislike;
	
	Stack<PreferenceModel> prevStates;
	private static DecimalFormat df3 = new DecimalFormat("#.###");
	
	public PreferenceModel(double sym, double den, double challenge, Room room, boolean dislike)
	{
		this.symmetry =sym;
		this.wallDensity = den;
		this.challenge = challenge;
		this.room = room;
		this.dislike = dislike;
	}
	
	public PreferenceModel(HashMap<PreferenceAttributes, Double> otherPreferences, Room room, boolean dislike)
	{
		this.preferences = new HashMap<PreferenceAttributes, Double>(otherPreferences);
		this.room = room;
		this.dislike = dislike;
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
//		symmetry = 0.5;
//		density = 0.5;
//		challenge = 0.5;
	}
	
	public void UpdateModel(boolean dislike, Room usedRoom)
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
				auxAtt = dislike ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			case ENEMY_SPARSITY:
				auxAttPrime = (usedRoom.calculateEnemySparsity() - auxAtt) * learningRate;
				auxAtt = dislike ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			case SYMMETRY:
				auxAttPrime = (usedRoom.getDimensionValue(DimensionTypes.SYMMETRY) - auxAtt) * learningRate;
				auxAtt = dislike ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			case TREASURE_DENSITY:
				auxAttPrime = (usedRoom.calculateTreasureDensity() - auxAtt) * learningRate;
				auxAtt = dislike ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			case TREASURE_SPARSITY:
				auxAttPrime = (usedRoom.calculateTreasureSparsity() - auxAtt) * learningRate;
				auxAtt = dislike ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			case WALL_DENSITY:
				auxAttPrime = (usedRoom.calculateWallDensity() - auxAtt) * learningRate;
				auxAtt = dislike ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			case WALL_SPARSITY:
				auxAttPrime = (usedRoom.calculateWallSparsity() - auxAtt) * learningRate;
				auxAtt = dislike ? auxAtt + (auxAttPrime * -1.0) : auxAtt + auxAttPrime;
				auxAtt = Math.max(Math.min(auxAtt, 1.0), 0.0);
				break;
			default:
				break;
			
			}
			
			preferences.put(entry.getKey(), auxAtt);
		}
		
		
//		prevStates.push(new PreferenceModel(symmetry, density, challenge, new Room(usedRoom), dislike));
		prevStates.push(new PreferenceModel(preferences, new Room(usedRoom), dislike));
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
		
		System.out.println(PreferenceAttributes.values().length);
		
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
		
//		while (iter.hasNext())
//		{
//		    System.out.println("Sym: " + iter.next().symmetry + ", Challenge: " + iter.next().challenge + ", density: " + iter.next().density );
//		    System.out.println();
//		    System.out.println(iter.next().room.toString());
//		    System.out.println("_______________________________");
//		}
	}
	
	public void SaveDataset()
	{
		System.out.println("to be implemented");
	}
}
