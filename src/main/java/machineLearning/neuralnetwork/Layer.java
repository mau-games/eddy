package machineLearning.neuralnetwork;

import java.util.ArrayList;

import machineLearning.neuralnetwork.Neuron.NeuronTypes;

//Methods that can be implemented:
// (1) The neuron with the highest value in the layer
// (2) Something with the input neurons
public class Layer 
{
	public int id;
	public int size;
	public NeuronTypes layerType;
	public ArrayList<Neuron> neurons;
	public double dropoutRate;
	
	public Layer(int size, NeuronTypes layerType)
	{
		this.size = size;
		neurons = new ArrayList<Neuron>();
		this.layerType = layerType;
	}
	
	/***
	 * Call this method to fully populate the layer with neurons of the layer type
	 */
	public void populateLayer()
	{
		for(int i = 0; i < size; i++)
		{
			addNeuron(new Neuron(layerType));
		}
	}
	
	public void addNeuron(Neuron neuron)
	{
		neurons.add(neuron);
	}
	
	public Neuron getNeuron(int index)
	{
		return neurons.get(index);
	}
	
	public ArrayList<Neuron> getNeurons()
	{
		return neurons;
	}
}
