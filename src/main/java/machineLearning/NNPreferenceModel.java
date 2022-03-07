package machineLearning;

import java.io.File;
import java.util.Stack;

import game.Room;
import machineLearning.neuralnetwork.activationFunction.ActivationFunction;
import machineLearning.neuralnetwork.activationFunction.LeakyReLU;
import machineLearning.neuralnetwork.activationFunction.SoftMax;
import machineLearning.neuralnetwork.DataTupleManager;
import machineLearning.neuralnetwork.DatasetUses;
import machineLearning.neuralnetwork.MapPreferenceModelTuple;
import machineLearning.neuralnetwork.NeuralNetwork;
import machineLearning.neuralnetwork.PreferenceModelDataTuple;

public class NNPreferenceModel extends PreferenceModel 
{
	
	protected NeuralNetwork<MapPreferenceModelTuple> mapValues;
	NeuralNetwork<PreferenceModelDataTuple> attributeValues;
	public NNPreferenceModel()
	{
		prevStates = new Stack<PreferenceModel>();
		projectPath = System.getProperty("user.dir") + File.separator + File.separator + "my-data" + File.separator + File.separator +
				"PreferenceModels";
		
		mapValues = new NeuralNetwork<MapPreferenceModelTuple>(new int[] {256, 200, 100}, 
				DataTupleManager.LoadValueMapDataList("PreferenceModels", "newmap_map"), "MAP_NETWORK_256_100_RELU",
				new ActivationFunction[] {new ActivationFunction(), new LeakyReLU(), new LeakyReLU(), new LeakyReLU(), new SoftMax()});
//		attributeValues = new NeuralNetwork<PreferenceModelDataTuple>(new int[] {100,100, 100, 100}, 
//				DataTupleManager.LoadPreferenceModelDataList("PreferenceModels", "newmap"), "MAP_NETWORK_100_100_RELU", 
//				new ActivationFunction[] {new ActivationFunction(), new ReLU(), new ReLU(),  new ReLU(), new ReLU(), new SoftMax()});
		
	}
	
	@Override
	public void UpdateModel(boolean like, Room usedRoom)
	{
		//I WOULD LIKE TO TEST EACH OF THE NETWORKS HERE
		
		
		prevStates.push(new PreferenceModel(preferences, new Room(usedRoom), like));
		tuples.add(new PreferenceModelDataTuple(prevStates.peek(), like));
		mapTuples.add(new MapPreferenceModelTuple(usedRoom, like));
		
		mapValues.FeedForward(mapTuples.get(mapTuples.size() - 1), DatasetUses.TEST);
		System.out.println(mapValues.Classify(mapTuples.get(mapTuples.size() - 1)));
		
//		attributeValues.FeedForward(tuples.get(tuples.size() - 1), DatasetUses.TEST);
//		System.out.println(attributeValues.Classify(tuples.get(tuples.size() - 1)));
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
		
		PreferenceModelDataTuple pdt = new PreferenceModelDataTuple(room, false);
		MapPreferenceModelTuple mpdt = new MapPreferenceModelTuple(room, false);
		
		mapValues.FeedForward(mpdt, DatasetUses.TEST);
		preferenceValue = mapValues.neuralLayers.get(mapValues.neuralLayers.size() - 1).getNeurons().get(1).output;
//		secondPreferenceValue = mapValues.neuralLayers.get(mapValues.neuralLayers.size() - 1).getNeurons().get(0).output;
//		attributeValues.FeedForward(pdt, DatasetUses.TEST);
//		secondPreferenceValue = attributeValues.neuralLayers.get(attributeValues.neuralLayers.size() - 1).getNeurons().get(1).output;
//		System.out.println(attributeValues.Classify(pdt));
		
		
//		if(preferenceValue > 0.99)
//		{
//			System.out.println("PREF!");
//		}
		
		return preferenceValue;
		
	}
}
