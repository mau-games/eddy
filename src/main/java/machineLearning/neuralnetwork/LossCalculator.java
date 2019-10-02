package machineLearning.neuralnetwork;

import java.util.ArrayList;

public class LossCalculator {
	public enum lossFunctions
	{
		MSE,
		MAE,
		SIMPLIFIED_CROSSENTROPY,
		CROSSENTROPY,
		MULTI_CROSSENTROPY,
		HINGE
	}
	
	public static <T extends DataTuple> double lossValue(NeuralNetwork<T> network, int expectedIndex, lossFunctions lossFunction, ArrayList<T> set, int tuple)
	{
		switch(lossFunction)
		{
		case CROSSENTROPY:
			return binaryCrossEntropy(network, expectedIndex, set, tuple);
		case SIMPLIFIED_CROSSENTROPY:
			return simplifiedCrossEntropy(network, expectedIndex, set, tuple);
		case MULTI_CROSSENTROPY:
			return multiCrossEntropy(network, expectedIndex, set, tuple);
		case HINGE:
			return Hinge(network, expectedIndex, set, tuple);
		case MSE:
			return MSE(network, expectedIndex, set, tuple);
		case MAE:
			return MAE(network, expectedIndex, set, tuple);
		default:
			break;
		
		}
		
		return 0.0;
	}
	
	/***
	 * Cross-entropy loss function to be called when we have more than 2 classes (M > 2)
	 * @param network
	 * @param expectedIndex
	 * @return
	 */
	private static <T extends DataTuple> double multiCrossEntropy(NeuralNetwork<T> network, int expectedIndex, ArrayList<T> set, int tuple)
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
	
	/***
	 * Cross-entropy loss function to be called when we have more than 2 classes (M > 2)
	 * But a bit special, I should double check this method
	 * @param network The network
	 * @param expectedIndex Correct label index
	 * @return
	 */
	private static <T extends DataTuple> double simplifiedCrossEntropy(NeuralNetwork<T> network, int expectedIndex, ArrayList<T> set, int tuple) //These ints have to disappear for classes
	{
		Neuron outputN =  network.neuralLayers.get(network.neuralLayers.size() - 1).getNeuron(expectedIndex, DatasetUses.TEST);
		double result = 0.0;
		Layer outputLayer = network.neuralLayers.get(network.neuralLayers.size() - 1);
		
		for(int i = 0; i < outputLayer.getNeurons().size(); i++)
		{
			double expectedOutput = network.getClassificationOutput(outputN.activation, i == expectedIndex);
			double neuralOutput =  outputLayer.getNeuron(i, DatasetUses.TEST).output;
			if(neuralOutput == 0.0) neuralOutput = 0.000000001; //Avoid LOG(0)
			
			result -= expectedOutput * Math.log(neuralOutput);
		}
		
		return result/(double)outputLayer.getNeurons().size();
	}
	
	
	private static <T extends DataTuple> double binaryCrossEntropy(NeuralNetwork<T> network, int expectedIndex, ArrayList<T> set, int tuple)
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
	
	/**
	 * Mean Absolute Error - L1 Loss method
	 * @param network
	 * @param expectedIndex
	 * @return
	 */
	private static <T extends DataTuple> double MAE(NeuralNetwork<T> network, int expectedIndex, ArrayList<T> set, int tuple)
	{
		double absoluteError = 0.0;
		ArrayList<Neuron> outputNeurons = network.neuralLayers.get(network.neuralLayers.size() - 1).getNeurons();
		for(int currentNeuron = 0; currentNeuron < outputNeurons.size(); currentNeuron++)
		{
			absoluteError += Math.abs(network.getClassificationOutput(outputNeurons.get(currentNeuron).activation, expectedIndex == currentNeuron)
										- outputNeurons.get(currentNeuron).output);
		}
		
		return absoluteError/(double)outputNeurons.size();
	}
	
	/**
	 * Mean Squared Error - L2 Loss method
	 * @param network
	 * @param expectedIndex
	 * @return
	 */
	private static <T extends DataTuple> double MSE(NeuralNetwork<T> network, int expectedIndex, ArrayList<T> set, int tuple)
	{
		double squaredError = 0.0;
		ArrayList<Neuron> outputNeurons = network.neuralLayers.get(network.neuralLayers.size() - 1).getNeurons();
		for(int currentNeuron = 0; currentNeuron < outputNeurons.size(); currentNeuron++)
		{
			squaredError += Math.pow(network.getClassificationOutput(outputNeurons.get(currentNeuron).activation, currentNeuron == 1, set.get(tuple) )
					- outputNeurons.get(currentNeuron).output, 2.0);
		}
		
		return squaredError/(double)outputNeurons.size();
	}
	
	private static <T extends DataTuple> double Hinge(NeuralNetwork<T> network, int expectedIndex, ArrayList<T> set, int tuple)
	{
		Neuron outputN =  network.neuralLayers.get(network.neuralLayers.size() - 1).getNeuron(expectedIndex, DatasetUses.TEST);
		
		double expectedOutput = network.getClassificationOutput(outputN.activation, true);
		double neuralOutput = outputN.output;

		return Math.max(0.0, 1.0 - neuralOutput * expectedOutput);
	}
}
