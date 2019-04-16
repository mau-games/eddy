package machineLearning.neuralnetwork;

import java.util.ArrayList;

import machineLearning.neuralnetwork.Neuron.NeuronTypes;
import machineLearning.neuralnetwork.activationFunction.ActivationFunction;

//Methods that can be implemented:
// (1) The neuron with the highest value in the layer
// (2) Something with the input neurons
public class Layer 
{
	public int id;
	public int size;
	public NeuronTypes layerType;
	public ArrayList<Neuron> neurons;
	public ArrayList<Integer> dropoutLayer;
	public double dropoutRate;
	public ActivationFunction layerActivationFunction;
	
	public Layer(int size, NeuronTypes layerType, double dropoutRate)
	{
		this.size = size;
		neurons = new ArrayList<Neuron>();
		this.layerType = layerType;
		this.dropoutRate = dropoutRate;
	}
	
	/***
	 * Call this method to fully populate the layer with neurons of the layer type
	 */
	public void populateLayer(ActivationFunction neuralActivation)
	{
		this.layerActivationFunction= neuralActivation;
		for(int i = 0; i < size; i++)
		{
			addNeuron(new Neuron(layerType, neuralActivation));
		}
	}
	
	public void createDropoutLayer()
	{
		dropoutLayer = new ArrayList<Integer>();
		
		for(int i = 0; i < neurons.size(); i++)
		{
			dropoutLayer.add(Math.random() >= dropoutRate ? 1 : 0);
		}
	}
	
	public void addNeuron(Neuron neuron)
	{
		neurons.add(neuron);
	}
	
	public Neuron getNeuron(int index, DatasetUses dataset)
	{

		if(dataset.equals(DatasetUses.TRAINING))
		{
			if(dropoutRate == 0.0)
				neurons.get(index).dropoutMultiplier = 1.0;
			else
				neurons.get(index).dropoutMultiplier = dropoutLayer.get(index)/dropoutRate;
			
			return neurons.get(index);
		}
		else if(dataset.equals(DatasetUses.TEST))
		{
			if(dropoutRate == 0.0)
				neurons.get(index).dropoutMultiplier = 1.0;
			else
				neurons.get(index).dropoutMultiplier = 1.0;
			
			return neurons.get(index);
		}
		
		return neurons.get(index);
	}
	
	public ArrayList<Neuron> getNeurons()
	{
		return neurons;
	}
}
