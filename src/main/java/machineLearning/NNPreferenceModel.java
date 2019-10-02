package machineLearning;

import java.util.Stack;
import java.util.ArrayList;
import java.util.Map.Entry;

import game.Room;
import machineLearning.neuralnetwork.DataTupleManager;
import machineLearning.neuralnetwork.DatasetUses;
import machineLearning.neuralnetwork.MapPreferenceModelTuple;
import machineLearning.neuralnetwork.NeuralNetwork;
import machineLearning.neuralnetwork.PreferenceModelDataTuple;
import machineLearning.neuralnetwork.activationFunction.ActivationFunction;
import machineLearning.neuralnetwork.activationFunction.LeakyReLU;
import machineLearning.neuralnetwork.activationFunction.LogisticSigmoid;
import machineLearning.neuralnetwork.activationFunction.ReLU;
import machineLearning.neuralnetwork.activationFunction.SoftMax;

public class NNPreferenceModel extends PreferenceModel 
{
	
	protected NeuralNetwork<MapPreferenceModelTuple> mapValues;
//	NeuralNetwork<PreferenceModelDataTuple> attributeValues;
	public NNPreferenceModel()
	{
		prevStates = new Stack<PreferenceModel>();
		projectPath = System.getProperty("user.dir") + "\\my-data\\PreferenceModels";
		
		mapValues = new NeuralNetwork<MapPreferenceModelTuple>(new int[] {256, 100}, 
				DataTupleManager.LoadValueMapDataList("PreferenceModels", "newmap_map"), "MAP_NETWORK_256_100_RELU", 
				new ActivationFunction[] {new ActivationFunction(), new LeakyReLU(), new LeakyReLU(), new LogisticSigmoid()});
		
//		mapValues = new NeuralNetwork<MapPreferenceModelTuple>(new int[] {256, 200, 100}, 
//				DataTupleManager.LoadValueMapDataList("PreferenceModels", "newmap_map"), "MAP_NETWORK_256_100_RELU", 
//				new ActivationFunction[] {new ActivationFunction(), new LeakyReLU(), new LeakyReLU(), new LeakyReLU(), new SoftMax()});
		
//		attributeValues = new NeuralNetwork<PreferenceModelDataTuple>(new int[] {100,100, 100, 100}, 
//				DataTupleManager.LoadPreferenceModelDataList("PreferenceModels", "newmap"), "MAP_NETWORK_100_100_RELU", 
//				new ActivationFunction[] {new ActivationFunction(), new ReLU(), new ReLU(),  new ReLU(), new ReLU(), new SoftMax()});
//		
	}
	
	public NNPreferenceModel(boolean realUse)
	{
		prevStates = new Stack<PreferenceModel>();
		projectPath = System.getProperty("user.dir") + "\\my-data\\PreferenceModels";
		
//		mapValues = new NeuralNetwork<MapPreferenceModelTuple>(new int[] {200, 100}, "MAP_NETWORK_256_100_RELU", 
//		new ActivationFunction[] {new ActivationFunction(), new LeakyReLU(), new LeakyReLU(), new LogisticSigmoid()},
//		20);
		
		mapValues = new NeuralNetwork<MapPreferenceModelTuple>(new int[] {256, 200, 100}, "MAP_NETWORK_256_100_RELU", 
				new ActivationFunction[] {new ActivationFunction(), new LeakyReLU(), new LeakyReLU(), new LeakyReLU(), new SoftMax()},
				20);
		
//		mapValues = new NeuralNetwork<MapPreferenceModelTuple>(new int[] {256, 200, 100}, 
//				DataTupleManager.LoadValueMapDataList("PreferenceModels", "newmap_map"), "MAP_NETWORK_256_100_RELU", 
//				new ActivationFunction[] {new ActivationFunction(), new LeakyReLU(), new LeakyReLU(), new LeakyReLU(), new SoftMax()});
		
//		attributeValues = new NeuralNetwork<PreferenceModelDataTuple>(new int[] {100,100, 100, 100}, 
//				DataTupleManager.LoadPreferenceModelDataList("PreferenceModels", "newmap"), "MAP_NETWORK_100_100_RELU", 
//				new ActivationFunction[] {new ActivationFunction(), new ReLU(), new ReLU(),  new ReLU(), new ReLU(), new SoftMax()});
//		
	}
	
	@Override
	public void UpdateModel(boolean like, Room usedRoom, int step)
	{
		if(!separatedDataset.containsKey(step))
		{
			separatedDataset.put(step, new ArrayList<MapPreferenceModelTuple>());
		}
		
		
		//I WOULD LIKE TO TEST EACH OF THE NETWORKS HERE
		
		prevStates.push(new PreferenceModel(preferences, new Room(usedRoom), like));
//		tuples.add(new PreferenceModelDataTuple(prevStates.peek(), like));
		mapTuples.add(new MapPreferenceModelTuple(usedRoom, like));
		separatedDataset.get(step).add(new MapPreferenceModelTuple(usedRoom, like));
		
		mapValues.FeedForward(mapTuples.get(mapTuples.size() - 1), DatasetUses.TEST);
		System.out.println(mapValues.Classify(mapTuples.get(mapTuples.size() - 1)));
		
//		attributeValues.FeedForward(tuples.get(tuples.size() - 1), DatasetUses.TEST);
//		System.out.println(attributeValues.Classify(tuples.get(tuples.size() - 1)));
	}
	
	public void updateContinuousModel(double pref, Room usedRoom, int step)
	{
		if(!separatedDataset.containsKey(step))
		{
			separatedDataset.put(step, new ArrayList<MapPreferenceModelTuple>());
		}	
		
		pref = Math.max(pref, 0.0);
		pref = Math.min(1.0, pref);
		
		//I WOULD LIKE TO TEST EACH OF THE NETWORKS HERE

//		tuples.add(new PreferenceModelDataTuple(prevStates.peek(), like));
		mapTuples.add(new MapPreferenceModelTuple(usedRoom, true));
		mapTuples.get(mapTuples.size() - 1).preference = pref;
		separatedDataset.get(step).add(new MapPreferenceModelTuple(usedRoom, true));
		separatedDataset.get(step).get(separatedDataset.get(step).size() - 1).preference = pref;
		
		mapValues.FeedForward(mapTuples.get(mapTuples.size() - 1), DatasetUses.TEST);
		System.out.println(mapValues.ClassifyContinuous(mapTuples.size() - 1, mapTuples, false));
		
//		attributeValues.FeedForward(tuples.get(tuples.size() - 1), DatasetUses.TEST);
//		System.out.println(attributeValues.Classify(tuples.get(tuples.size() - 1)));
	}
	
	public void printSeparatedDataset(int step)
	{
		for(MapPreferenceModelTuple mpmt : separatedDataset.get(step))
		{
			System.out.println(mpmt.preference);
		}
	}
	
	@Override
	public double testWithPreference(Room room)
	{
		//TODO: PROBLEMS HERE!!!!!
		if(this.room == null)
			this.room = room;
		
		if(this.room.equals(room))
			System.out.println("SAAAAAAAAAAAAAAMA");
		
		double preferenceValue = 0.0;
		double secondPreferenceValue = 0.0;
		
//		PreferenceModelDataTuple pdt = new PreferenceModelDataTuple(room, false);
		MapPreferenceModelTuple mpdt = new MapPreferenceModelTuple(room, false);
		
		mapValues.FeedForward(mpdt, DatasetUses.TEST);
		preferenceValue = mapValues.neuralLayers.get(mapValues.neuralLayers.size() - 1).getNeurons().get(0).output;
//		secondPreferenceValue = mapValues.neuralLayers.get(mapValues.neuralLayers.size() - 1).getNeurons().get(0).output;
//		attributeValues.FeedForward(pdt, DatasetUses.TEST);
//		secondPreferenceValue = attributeValues.neuralLayers.get(attributeValues.neuralLayers.size() - 1).getNeurons().get(1).output;
//		System.out.println(attributeValues.Classify(pdt));
		
		
//		if(preferenceValue > 0.99)
//		{
//			System.out.println("PREF!");
//		}
		
//		System.out.println("Pref Value: " + preferenceValue);
//		System.out.println("NO Pref Value: " + mapValues.neuralLayers.get(mapValues.neuralLayers.size() - 1).getNeurons().get(0).output);
//		System.out.println();
		
		return preferenceValue;
		
	}
	
	public void trainNetwork()
	{
		mapValues.incomingData(new ArrayList<MapPreferenceModelTuple>(mapTuples));
	}
	
	public void trainNetwork(int specificSet)
	{
		mapValues.incomingData(new ArrayList<MapPreferenceModelTuple>(separatedDataset.get(specificSet)));
	}
}
