package machineLearning;

import java.util.Stack;
import java.util.Map.Entry;

import game.Room;
import machineLearning.neuralnetwork.DataTupleManager;
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
		projectPath = System.getProperty("user.dir") + "\\my-data\\PreferenceModels";
		
		mapValues = new NeuralNetwork<MapPreferenceModelTuple>(new int[] {200, 200}, 
				DataTupleManager.LoadValueMapDataList("PreferenceModels", "newmap_map"), "mapValues");
		attributeValues = new NeuralNetwork<PreferenceModelDataTuple>(new int[] {20, 20}, 
				DataTupleManager.LoadPreferenceModelDataList("PreferenceModels", "newmap"), "AttributeValues");	
		
	}
	
	@Override
	public void UpdateModel(boolean like, Room usedRoom)
	{
		//I WOULD LIKE TO TEST EACH OF THE NETWORKS HERE
		
		
		prevStates.push(new PreferenceModel(preferences, new Room(usedRoom), like));
		tuples.add(new PreferenceModelDataTuple(prevStates.peek(), like));
		mapTuples.add(new MapPreferenceModelTuple(usedRoom, like));
		
		mapValues.FeedForward(mapTuples.get(mapTuples.size() - 1));
		System.out.println(mapValues.Classify(mapTuples.get(mapTuples.size() - 1)));
		
		attributeValues.FeedForward(tuples.get(tuples.size() - 1));
		System.out.println(attributeValues.Classify(tuples.get(tuples.size() - 1)));
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
		
		mapValues.FeedForward(mpdt);
		preferenceValue = mapValues.neuralLayers.get(mapValues.neuralLayers.size() - 1).getNeurons().get(1).output;
//		secondPreferenceValue = mapValues.neuralLayers.get(mapValues.neuralLayers.size() - 1).getNeurons().get(0).output;
		attributeValues.FeedForward(pdt);
		secondPreferenceValue = attributeValues.neuralLayers.get(attributeValues.neuralLayers.size() - 1).getNeurons().get(1).output;
//		System.out.println(attributeValues.Classify(pdt));
		
		
//		if(preferenceValue > 0.99)
//		{
//			System.out.println("PREF!");
//		}
		
		return preferenceValue;
		
	}
}
