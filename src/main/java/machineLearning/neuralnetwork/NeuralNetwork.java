package machineLearning.neuralnetwork;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Random;

import collectors.DataSaverLoader;
import machineLearning.neuralnetwork.Neuron.NeuronTypes;
import machineLearning.neuralnetwork.activationFunction.ActivationFunction;
import machineLearning.neuralnetwork.activationFunction.LogisticSigmoid;
import machineLearning.neuralnetwork.activationFunction.ReLU;

public class NeuralNetwork <T extends DataTuple>
{
	/*
	public double[][] XOR_DATASET = { 	{ 1.0, 0.0, 1.0, 1.0 }, 
										{ 0.0, 0.0, 0.0, 0.0 },
										{ 0.0, 1.0, 1.0, 1.0 }, 
										{ 1.0, 1.0, 0.0, 0.0 } };*/
	
	//public ArrayList<PacmanInfo> dataset = new ArrayList<PacmanInfo>();
	Random rnd = new Random();
	public ArrayList<ModelInformation> modelInfoEachStep = new ArrayList<ModelInformation>();
	
	public ArrayList<T> dataset = new ArrayList<T>();
	public ArrayList<T> trainingSet = new ArrayList<T>();
	public ArrayList<T> testSet = new ArrayList<T>();
	public ArrayList<T> validationSet = new ArrayList<T>();
	
	public float[] setDivision = {0.7f, 0.1f, 0.2f}; //Training, test, validation3
	public double batchSize = 0.2;
	public int max_epochs = 1000;
	
	public int input_neurons_quantity = 9;
	public int output_neurons_quantity = 2;
	public ArrayList<Layer> neuralLayers = new ArrayList<Layer>();
	
	//For Learning RATE - the lower-upper bounds are calculated through LRRange-test
	public double stepSize = 0.0;
//	public double lowerBoundLR = 0.015385;
//	public double upperBoundLR = 0.1;
//	public double lowerBoundLR = 0.01;
//	public double upperBoundLR = 0.05;
//	public double lowerBoundLR = 0.015385;
//	public double upperBoundLR = 0.03154;
//	public double lowerBoundLR = 0.00263;
//	public double upperBoundLR = 0.00371;
//	public double lowerBoundLR = 0.001;
//	public double upperBoundLR = 0.002;
//	public double lowerBoundLR = 0.001;
//	public double upperBoundLR = 0.01;
	public double lowerBoundLR = 0.5;
	public double upperBoundLR = 0.01;
	
	String networkName = "";
	
	public static void main(String[] args)
	{
//		System.out.println("STARTING NN WITH attribute values");
//		System.out.println();
//		NeuralNetwork<PreferenceModelDataTuple> attributeValues = new NeuralNetwork<PreferenceModelDataTuple>(new int[] {200, 100}, 
//				DataTupleManager.LoadPreferenceModelDataList("PreferenceModels", "newmap"), "VALUE_NETWORK_TES", new ReLU());	
//		
//		System.out.println("___________________________________");

		System.out.println("STARTING NN WITH VALUE MAP");
		System.out.println();
		NeuralNetwork<MapPreferenceModelTuple> mapValues = new NeuralNetwork<MapPreferenceModelTuple>(new int[] {256,100}, 
				DataTupleManager.LoadValueMapDataList("PreferenceModels", "newmap_map"), "MAP_NETWORK_TES", new ReLU());
		System.out.println("___________________________________");
		
		System.out.println("STARTING NN WITH VALUE MAP");
		System.out.println();
		NeuralNetwork<MapPreferenceModelTuple> mapValues2 = new NeuralNetwork<MapPreferenceModelTuple>(new int[] {256,256}, 
				DataTupleManager.LoadValueMapDataList("PreferenceModels", "newmap_map"), "MAP_NETWORK_TEST_2", new ReLU());
		System.out.println("___________________________________");

	}
	
	private void fillSetRND(ArrayList<T> set, int amount, ArrayList<T> fromDataSet)
	{
		for(int tuple = 0; tuple < amount; tuple++)
		{
			set.add(fromDataSet.remove(rnd.nextInt(fromDataSet.size())));
		}
	}
	
	private void fillSet(ArrayList<T> set, int amount, ArrayList<T> fromDataSet)
	{
		for(int tuple = 0; tuple < amount; tuple++)
		{
			set.add(fromDataSet.remove(0));
		}
	}
	
	public NeuralNetwork(int[] hiddenLayers, ArrayList<T> data, String networkName, ActivationFunction neuralActivation)
	{
		this.networkName = networkName;
//		dataset = DataManager.LoadNeuralNetworkDataset("average_player.csv");
		
		////////////////////// LOAD AND DIVIDE DATASET /////////////
		dataset = data;	
		
		int trainingTuples = (int) (dataset.size() * setDivision[0]);
		int testingTuples = (int) (dataset.size() * setDivision[1]);
		int validationTuples = (int) (dataset.size() * setDivision[2]);
		
		input_neurons_quantity = data.get(0).numericalData.size();
		
		fillSet(validationSet, validationTuples, dataset);
		fillSet(testSet, testingTuples, dataset);
		fillSet(trainingSet, trainingTuples, dataset);
		
//		fillSet(validationSet, validationTuples, dataset);
//		fillSet(testSet, testingTuples, dataset);
//		fillSet(trainingSet, trainingTuples, dataset);

		////////////// GENERATE ANN ////////////////////

		//Fill the input neurons
		this.neuralLayers.add(new Layer(input_neurons_quantity, NeuronTypes.INPUT, 0.0));
		this.neuralLayers.get(0).populateLayer(neuralActivation);
		
		//Fill the hidden neurons
		for(int hiddenLayer : hiddenLayers)
		{
			Layer layer = new Layer(hiddenLayer, NeuronTypes.HIDDEN, 0.5);
			layer.populateLayer(neuralActivation);
			this.neuralLayers.add(layer);
		}
		
		//Fill the output neurons
		this.neuralLayers.add(new Layer(output_neurons_quantity, NeuronTypes.OUTPUT, 0.0));
		this.neuralLayers.get(this.neuralLayers.size() - 1).populateLayer(new LogisticSigmoid());
		
		//Go through the layers (from the second one) and connect each neuron in that layer to all the neurons in the prev layer
		//As bonus, add the Bias too!
		//Disclaimmer! this only works for Multi layered perceptron fully connected :D --> but it will work without any hidden layer too
		for(int layerCount = 1; layerCount < this.neuralLayers.size(); layerCount++)
		{
			for(int currentNeuron = 0; currentNeuron < this.neuralLayers.get(layerCount).size; currentNeuron++)
			{
				Neuron neuron = this.neuralLayers.get(layerCount).getNeuron(currentNeuron, DatasetUses.VALIDATION);
				neuron.AddConnection(new Neuron(NeuronTypes.BIAS, neuralActivation, 0.1f));
				
				for(int prevLayerNeuron = 0; prevLayerNeuron < this.neuralLayers.get(layerCount - 1).size; prevLayerNeuron++)
				{
					neuron.AddConnection(this.neuralLayers.get(layerCount - 1).getNeuron(prevLayerNeuron, DatasetUses.VALIDATION));
				}
			}
		}

//		lrRangeTest();
		NonBatchBackPropagation(1.0f);
	}

	public NeuralNetwork(boolean a ) //This method was for testing the XOR GATE
	{
//		//Generate neural network fully connected
//		for(int i = 0; i < input_neurons_quantity; i++)
//		{
//			input_neurons.add(new Neuron(NeuronTypes.INPUT));
//		}
//		
//		hidden_neurons.add(new Neuron(NeuronTypes.HIDDEN));
//		hidden_neurons.get(0).AddConnection(input_neurons.get(0), 0.2);
//		hidden_neurons.get(0).AddConnection(input_neurons.get(1), 0.4);
//		hidden_neurons.get(0).AddConnection(input_neurons.get(2), -0.5);
//		hidden_neurons.get(0).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0), -0.4);
//		
//		hidden_neurons.add(new Neuron(NeuronTypes.HIDDEN));
//		hidden_neurons.get(1).AddConnection(input_neurons.get(0), -0.3);
//		hidden_neurons.get(1).AddConnection(input_neurons.get(1), 0.1);
//		hidden_neurons.get(1).AddConnection(input_neurons.get(2), 0.2);
//		hidden_neurons.get(1).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0), 0.2);
//		
//		output_neurons.add(new Neuron(NeuronTypes.OUTPUT));
//		output_neurons.get(0).AddConnection(hidden_neurons.get(0), -0.3);
//		output_neurons.get(0).AddConnection(hidden_neurons.get(1), -0.2);
//		output_neurons.get(0).AddConnection(new Neuron(NeuronTypes.BIAS, 1.0), 0.1);
//		
//		NonBatchBackPropagation(0.1f);
	}
	
	public void lrRangeTest()
	{
		double learningRate = 0.005;
		int iterations = (int)(trainingSet.size() * batchSize);
		int currentBatch = 0;
		int batchGrow = (int)(trainingSet.size()/iterations);
		max_epochs = 50;
		stepSize = max_epochs;
		lowerBoundLR = 0.001;
		upperBoundLR = 1.0;
		
		
		for(int epoch = 0; epoch < max_epochs; epoch++)
		{
//			System.out.println("EPOCH " + epoch);
			double currDelta = 0.0f;
			double currentAccuracy = 0.0;
			learningRate = findLearningRate(epoch);
			
//			learningRate = 0.1;
			
			for(int iter = 0; iter < iterations; iter++ )
			{
				currDelta = 0.0f;          
				currentAccuracy = 0.0;
				currentBatch = iter * batchGrow;
				
				
				for(int tuple = currentBatch; tuple < currentBatch + batchGrow; tuple++)
				{
					//step 1: feedforward
					FeedForward(tuple, trainingSet, DatasetUses.TRAINING);
					int expectedOutput = trainingSet.get(tuple).label == true ? 1 : 0;
					
					//This work unless you have connections within the same layer --> Then you need to handle it in a different way
					for(int layerCount = this.neuralLayers.size() - 1; layerCount > 1; layerCount--)
					{
						for(int currentNeuron = 0; currentNeuron < this.neuralLayers.get(layerCount).size; currentNeuron++)
						{
							Neuron neuron = this.neuralLayers.get(layerCount).getNeuron(currentNeuron, DatasetUses.TEST);
							
							if(neuron == null)
								continue;
							
							//Step 2: Calculate accumulated error (only important for hidden neurons)
							if(layerCount !=  this.neuralLayers.size() - 1)
								neuron.GetAccumulatedError(this.neuralLayers.get(layerCount + 1).getNeurons());
							
							//Step 3: Calculate error! --> IS THIS CORRECT OR A BUG??
							if(currentNeuron == expectedOutput)
							{
								neuron.CalculateError(0.9);
							}
							else
							{
								neuron.CalculateError(0.1);
							}
							
							//Step 4: Accumulate the delta weights
							currDelta = neuron.AccumulateWeights(learningRate, 0.9);
						}
					}
					
					//Step 6: Correct all the weights (This is Online)
					for(int layerCount = this.neuralLayers.size() - 1; layerCount > 1; layerCount--)
					{
						for(int currentNeuron = 0; currentNeuron < this.neuralLayers.get(layerCount).size; currentNeuron++)
						{
							this.neuralLayers.get(layerCount).getNeuron(currentNeuron, DatasetUses.TEST).ChangeWeights(learningRate);;
						}
					}
				}
			}

			
			///////////////// DO FINAL TEST OF ONLY THE EPOCH //////////////////
			ModelInformation currentModel = new ModelInformation(learningRate, epoch, -1, batchSize, iterations, max_epochs);
			modelInfoEachStep.add(currentModel);
			
			TestNetwork(epoch, -1, trainingSet, DatasetUses.TRAINING, true, false, currentModel, DebugMode.NONE);
			TestNetwork(epoch, -1, testSet, DatasetUses.TEST, true, false, currentModel, DebugMode.NONE);
			System.out.println("----------------------------------");		
		}
		
		saveModelInformation("Execution-" + LocalDateTime.now().getNano());
		System.out.println("IS OVER!");
	}

	
	public double decreaseLR(int epoch, double current_lr)
	{
		return Math.max(0.05,current_lr/(1.0+(double)(epoch)/45.0)); //45 is max epoch
	}
	
	
	/***
	 * We update connection weights after each tuple
	 * @param learning_ratee
	 */
	public void NonBatchBackPropagation(float learning_rate)
	{
		double deltaThreshold = 0.0001f;
		double accuracyThreshold = 0.85;
		double learningRate = 0.2;
		int iterations = (int)(trainingSet.size() * batchSize);
		int currentBatch = 0;
		int batchGrow = (int)(trainingSet.size()/iterations);
		stepSize = iterations;
		int counter = 0;
		for(int epoch = 0; epoch < max_epochs; epoch++)
		{
//			System.out.println("EPOCH " + epoch);
			double currentHighestDelta = 0.0;
			double currDelta = 0.0f;
			double currentAccuracy = 0.0;
			learningRate = findLearningRate(epoch);
//			
//			counter++;
//			if(counter > stepSize * 5)
//			{
//				counter = 0;
//				lowerBoundLR = lowerBoundLR/2.0;
//			}
			lowerBoundLR = decreaseLR(epoch, 0.5); 
			
//			learningRate = 0.01;
//			learningRate = 0.1;
			
			System.out.println(learningRate);
			
			for(int iter = 0; iter < iterations; iter++ )
			{
				currentHighestDelta = 0.0; 
				currDelta = 0.0f;          
				currentAccuracy = 0.0;
				currentBatch = iter * batchGrow;
				
				//Step 0: create the dropoutlayers
				for(int layerCount = 0; layerCount < this.neuralLayers.size(); layerCount++)
				{
					this.neuralLayers.get(layerCount).createDropoutLayer();
				}
				
				for(int tuple = currentBatch; tuple < currentBatch + batchGrow; tuple++)
				{

					//step 1: feedforward
					FeedForward(tuple, trainingSet, DatasetUses.TRAINING);
					int expectedOutput = trainingSet.get(tuple).label == true ? 1 : 0;
					
					//This work unless you have connections within the same layer --> Then you need to handle it in a different way
					for(int layerCount = this.neuralLayers.size() - 1; layerCount > 1; layerCount--)
					{
						for(int currentNeuron = 0; currentNeuron < this.neuralLayers.get(layerCount).size; currentNeuron++)
						{
							Neuron neuron = this.neuralLayers.get(layerCount).getNeuron(currentNeuron, DatasetUses.TEST);
							
							if(neuron == null)
								continue;
							
							//Step 2: Calculate accumulated error (only important for hidden neurons)
							if(layerCount !=  this.neuralLayers.size() - 1)
								neuron.GetAccumulatedError(this.neuralLayers.get(layerCount + 1).getNeurons());
							
							//Step 3: Calculate error! --> IS THIS CORRECT OR A BUG??
							if(currentNeuron == expectedOutput)
							{
								neuron.CalculateError(0.9);
							}
							else
							{
								neuron.CalculateError(0.1);
							}
							
							//Step 4: Accumulate the delta weights
							currDelta = neuron.AccumulateWeights(learningRate, 0.9);
							if(currDelta > currentHighestDelta)
								currentHighestDelta = currDelta;
						}
					}
//					//Step 5: Correct all the weights (This is Online)
//					for(int layerCount = this.neuralLayers.size() - 1; layerCount > 1; layerCount--)
//					{
//						for(int currentNeuron = 0; currentNeuron < this.neuralLayers.get(layerCount).size; currentNeuron++)
//						{
//							this.neuralLayers.get(layerCount).getNeuron(currentNeuron).ChangeWeights(learningRate);;
//						}
//					}
					
					for(int layerCount = 0; layerCount < this.neuralLayers.size(); layerCount++)
					{
						for(Neuron n : this.neuralLayers.get(layerCount).getNeurons())
						{
							n.dropoutMultiplier = 1.0;
						}
					}
					
				}
				
				//Step 5: Correct all the weights (This is offline)
				for(int layerCount = this.neuralLayers.size() - 1; layerCount > 1; layerCount--)
				{
					for(int currentNeuron = 0; currentNeuron < this.neuralLayers.get(layerCount).size; currentNeuron++)
					{
						this.neuralLayers.get(layerCount).getNeuron(currentNeuron, DatasetUses.TEST).ChangeWeights(learningRate);;
					}
				}
				
//				if(currentHighestDelta <= deltaThreshold)
//				{
//					System.out.println("IS OVER, WEIGHT IS NOT MODIFIED ENOUGH");
//					return;
//				}
				
//				if(currentAccuracy >= accuracyThreshold)
//				{
//					System.out.println("IS OVER, ACCURATE ENOUGH");
//					return;
//				}
				
				
			}

			
			///////////////// DO FINAL TEST OF ONLY THE EPOCH //////////////////
			ModelInformation currentModel = new ModelInformation(learningRate, epoch, -1, batchSize, iterations, max_epochs);
			modelInfoEachStep.add(currentModel);
			
			TestNetwork(epoch, -1, validationSet, DatasetUses.VALIDATION, true, false, currentModel, DebugMode.EVERYTHING);
			TestNetwork(epoch, -1, trainingSet, DatasetUses.TRAINING, true, false, currentModel, DebugMode.EVERYTHING);
			System.out.println("----------------------------------");

			if(currentModel.getAccuracy(DatasetUses.VALIDATION) >= accuracyThreshold)
			{
				System.out.println("IS OVER, ACCURATE ENOUGH for Validadation, accuracy threshold: " + accuracyThreshold);
				TestNetwork(epoch, -1, testSet, DatasetUses.TEST, true, false, currentModel, DebugMode.EVERYTHING);
				saveModelInformation("Execution-" + LocalDateTime.now().getNano());
				return;
			}
		
		}
		ModelInformation currentModel = new ModelInformation(learningRate, 0, -1, batchSize, iterations, max_epochs);
		TestNetwork(0, -1, testSet, DatasetUses.TEST, true, false, currentModel, DebugMode.EVERYTHING);
		saveModelInformation("Execution-" + LocalDateTime.now().getNano());
		System.out.println("IS OVER!");
	}
	
	private void TestNetwork(int epoch, int iteration, ArrayList<T> set, 
							DatasetUses setName, boolean getLoss, boolean saveValues, 
							ModelInformation modelInfo, DebugMode debug)
	{
		float success = 0.0f;
		float failure = 0.0f;
		float setSize = set.size();
		double loss = 0.0;
		
		for(int k = 0; k < setSize; k++)
		{

			//step 1: feedforward
			FeedForward(k, set, DatasetUses.TEST);
			
			//step 2: check if it was good classifications!
			if(Classify(k, set, false))
			{
				success++;
			}
			else
			{
				failure++;
			}
			
			if(getLoss)
				loss += defaultLossFunction((set.get(k).label == true ? 1 : 0), 0.9);
			
		}
		
		loss /= setSize;
		
		//////////////////////////////////////////////////////
		
		switch(debug)
		{
		case NONE:
			break;
		case ACCURACY:
			System.out.println("in " + setName + ", SUCCESS: " + (success * 100.0f)/setSize + "%; FAILURE: " + (failure * 100.0f)/setSize  + "%; TOTAL: " + setSize);
			break;
		case LOSS:
			System.out.println("Loss in " + setName + " at epoch.iteration " + epoch + "." + iteration + ": " + 
					loss + ", " + success/setSize);
			break;
		case EVERYTHING:
			System.out.println("Loss and accuracy in " + setName + " at epoch.iteration " + epoch + "." + iteration + ": " + 
								loss + ", " + success/setSize);
			break;
		default:
			break;
		}

		if(saveValues)
		{
			String inf = epoch + ";" + loss + ";" + success/setSize;
//			DataTupleManager.sa("loss-value" + setName + ".csv", inf, true);
		}
		
		modelInfo.setAccuracy(setName, success/setSize);
		modelInfo.setLoss(setName, loss);
	}
	
	private double defaultLossFunction(int expectedIndex, double expectedOutcome)
	{
		double squaredError = 0.0;
		ArrayList<Neuron> outputNeurons = neuralLayers.get(neuralLayers.size() - 1).getNeurons();
		for(int currentNeuron = 0; currentNeuron < outputNeurons.size(); currentNeuron++)
		{
			//Step 2: Calculate error! 
			if(currentNeuron == expectedIndex)
			{
				squaredError += Math.pow(0.9 - outputNeurons.get(currentNeuron).output, 2.0);
			}
			else
			{
				squaredError += Math.pow(0.1 - outputNeurons.get(currentNeuron).output, 2.0);
			}
		}
		
		return squaredError/(double)outputNeurons.size();
	}
	
	public double findLearningRate(double currentEpoch)
	{
		double localLR = 0.0;
		
		double localCycle = Math.floor(1.0 + currentEpoch/(2.0*stepSize));
		double localX = Math.abs(currentEpoch/stepSize - 2.0 * localCycle + 1.0);
		localLR = lowerBoundLR + (upperBoundLR - lowerBoundLR) * Math.max(0,  (1- localX));
		
		return localLR;
	}
	
	public void saveModelInformation(String filename)
	{
		String luAcc = "Epoch;Iteration;Learning Rate;Training-loss;Test-loss;Validation-loss;Training-accuracy;Test-accuracy;Validation-Accuracy";
		DataSaverLoader.saveFile("PreferenceModels",filename + networkName + ".csv", luAcc, true);
		
		for(ModelInformation mi : modelInfoEachStep)
		{
			luAcc = mi.epoch + ";";
			luAcc += mi.iteration + ";";
			luAcc += mi.learningRate + ";";
			luAcc += mi.getLoss(DatasetUses.TRAINING) + ";";
			luAcc += mi.getLoss(DatasetUses.TEST) + ";";
			luAcc += mi.getLoss(DatasetUses.VALIDATION) + ";";
			luAcc += mi.getAccuracy(DatasetUses.TRAINING) + ";";
			luAcc += mi.getAccuracy(DatasetUses.TEST) + ";";
			luAcc += mi.getAccuracy(DatasetUses.VALIDATION);
			 
			DataSaverLoader.saveFile("PreferenceModels",filename + networkName + ".csv", luAcc, true);
		}
	}
	
	public boolean Classify(int index, ArrayList<T> set, boolean debug)
	{
		int selected_index = 0;
		double best_value = -99999.9;
		
		ArrayList<Neuron> outputNeurons = neuralLayers.get(neuralLayers.size() - 1).getNeurons();
		for(int currentNeuron = 0; currentNeuron < outputNeurons.size(); currentNeuron++)
		{
			if(outputNeurons.get(currentNeuron).output > best_value)
			{
				best_value = outputNeurons.get(currentNeuron).output;
				selected_index = currentNeuron;
			}
		}
		
		if(debug)
		{
			System.out.println("NEURAL OUTPUT: " + selected_index);
			System.out.println("EXPECTED OUTPUT: " + (set.get(index).label == true ? 1 : 0));
		}
		
		return (set.get(index).label == true ? 1 : 0) == selected_index;
	}
	
	public boolean Classify(T tuple)
	{
		int selected_index = 0;
		double best_value = -99999.9;
		
		ArrayList<Neuron> outputNeurons = neuralLayers.get(neuralLayers.size() - 1).getNeurons();
		for(int currentNeuron = 0; currentNeuron < outputNeurons.size(); currentNeuron++)
		{
			if(outputNeurons.get(currentNeuron).output > best_value)
			{
				best_value = outputNeurons.get(currentNeuron).output;
				selected_index = currentNeuron;
			}
		}
		
		return (tuple.label == true ? 1 : 0) == selected_index;
	}
	
	public void FeedForward(int index, ArrayList<T> set, DatasetUses dataset)
	{
		for(int i = 0; i < neuralLayers.get(0).neurons.size(); ++i)
		{
			Neuron n = neuralLayers.get(0).getNeuron(i, dataset);
			
			n.CalculateOutput(set.get(index).numericalData.get(i));
		}
		
		//Calculate the output of hidden and output neurons
		for(int layerCount = 1; layerCount < this.neuralLayers.size(); layerCount++)
		{
			for(int i = 0; i < neuralLayers.get(layerCount).neurons.size(); ++i)
			{
				Neuron n = neuralLayers.get(layerCount).getNeuron(i, dataset);
				n.CalculateOutput(0.0);
			}
		}
	}
	
	public void FeedForward(/*ArrayList<Double> tuple*/ T tuple, DatasetUses dataset)
	{
		for(int i = 0; i < neuralLayers.get(0).neurons.size(); ++i)
		{
			Neuron n = neuralLayers.get(0).getNeuron(i, dataset);
			n.CalculateOutput(tuple.numericalData.get(i));
		}
		
		//Calculate the output of hidden and output neurons
		for(int layerCount = 1; layerCount < this.neuralLayers.size(); layerCount++)
		{
			for(int i = 0; i < neuralLayers.get(layerCount).neurons.size(); ++i)
			{
				Neuron n = neuralLayers.get(layerCount).getNeuron(i, dataset);
				n.CalculateOutput(0.0);
			}
		}
	}
	
	/***
	 * We update connectiong weights after each epoch
	 * @param learning_ratee
	 */
	public void BatchBackPropagation(float learning_rate)
	{
		//TODO: Make this a batch back propagation -- updating the weights after each batch rather than after each delta calculation
	}
	
	
}
