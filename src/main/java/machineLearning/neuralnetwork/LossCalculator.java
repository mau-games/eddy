package machineLearning.neuralnetwork;

import java.util.ArrayList;

public class LossCalculator {
	public enum lossFunctions
	{
		MSE,
		CROSSENTROPY,
		MULTI_CROSSENTROPY,
		HINGE
	}
	
	public static <T extends DataTuple> double lossValue(NeuralNetwork<T> network, int expectedIndex, lossFunctions lossFunction)
	{
		switch(lossFunction)
		{
		case CROSSENTROPY:
			return crossEntropy(network, expectedIndex);
		case MULTI_CROSSENTROPY:
			return multiCrossEntropy(network, expectedIndex);
		case HINGE:
			return Hinge(network, expectedIndex);
		case MSE:
			return MSE(network, expectedIndex);
		default:
			break;
		
		}
		
		return 0.0;
	}
	
	private static <T extends DataTuple> double multiCrossEntropy(NeuralNetwork<T> network, int expectedIndex)
	{
		double crossEntropy = 0.0;
		ArrayList<Neuron> outputNeurons = network.neuralLayers.get(network.neuralLayers.size() - 1).getNeurons();
		for(int currentNeuron = 0; currentNeuron < outputNeurons.size(); currentNeuron++)
		{
			double expectedOutput = network.getClassificationOutput(outputNeurons.get(currentNeuron).activation, currentNeuron == expectedIndex);
			double neuralOutput = outputNeurons.get(currentNeuron).output;
			double differenceNeural = 1.0 - neuralOutput;
			if(differenceNeural == 0.0)
				differenceNeural = 0.000000001;

			crossEntropy -= expectedOutput * Math.log(neuralOutput) + (1.0-expectedOutput)*Math.log(differenceNeural);
		}
		
		return crossEntropy/ (double)outputNeurons.size(); ///divide by N
	}
	
	private static <T extends DataTuple> double crossEntropy(NeuralNetwork<T> network, int expectedIndex)
	{
		Neuron outputN =  network.neuralLayers.get(network.neuralLayers.size() - 1).getNeuron(expectedIndex, DatasetUses.TEST);
		
		double expectedOutput = network.getClassificationOutput(outputN.activation, true);
		double neuralOutput = outputN.output;
		//Avoid the LOG(0)
		double differenceNeural = 1.0 - neuralOutput;
		if(differenceNeural == 0.0)
			differenceNeural = 0.000000001;

		return -(expectedOutput * Math.log(neuralOutput) + (1.0-expectedOutput)*Math.log(differenceNeural));

	}
	
	private static <T extends DataTuple> double MSE(NeuralNetwork<T> network, int expectedIndex)
	{
		double squaredError = 0.0;
		ArrayList<Neuron> outputNeurons = network.neuralLayers.get(network.neuralLayers.size() - 1).getNeurons();
		for(int currentNeuron = 0; currentNeuron < outputNeurons.size(); currentNeuron++)
		{
			squaredError += Math.pow(network.getClassificationOutput(outputNeurons.get(currentNeuron).activation, expectedIndex == currentNeuron)
					- outputNeurons.get(currentNeuron).output, 2.0);
		}
		
		return squaredError/(double)outputNeurons.size();
	}
	
	private static <T extends DataTuple> double Hinge(NeuralNetwork<T> network, int expectedIndex)
	{
		Neuron outputN =  network.neuralLayers.get(network.neuralLayers.size() - 1).getNeuron(expectedIndex, DatasetUses.TEST);
		
		double expectedOutput = network.getClassificationOutput(outputN.activation, true);
		double neuralOutput = outputN.output;

		return Math.max(0.0, 1.0 - neuralOutput * expectedOutput);
	}
}
