package nettest;

import feedforward.*;
import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import rbf.*;

/**
 * Created by joshua on 9/22/15.
 */

public class Trainer
{
    static final Random rand = new Random();
    
    /***********************Test Values*****************************
     * 
     *                      * = default
     *
     * Default values are set if the value's respective array is either null or
     * empty (length = 0). 
     * 
     *  Sizes: 1000, 50000, 100000*, 500000, 1000000
     *
     *  Repeats:
     *  {100,5000}, {50000,100}, {5000000,1}, {1,1}*
     *
     *  Dimensions:
     *  2, 3, 4*, 5, 6
     *
     *  RBF basis function: gaussian*, cubed, multi-quad
     *
     *  FF activation function: 
     *  linear, log*
     *
     *  Learning rate:
     *  0.1, 0.05, 0.01*, 0.005, 0.001
     *
     *  Clusters {hidden num, number of clusters}:
     *  {50,500}, {100,10000}*, {500,10000}
     *
     *  momentums
     *  .1, .01*, .001
     *
     *  hidden (hidden size)
     *  0, 1*, 2
     * 
     *************************************************************
     */
    public static void main(String[] args) throws IOException {
        
        // Dataset sizes
        int[] sizes = new int[] 
        {
            10000
        };
        
        // Numbers of inputs to the Rosenbrock function
        int[] dimensions = new int[] 
        {
            4
        };
        
        // Numbers of times to repeat training on the same data
        int[][] repeats = new int[][] 
        {
            {1,1}
        };
        
        // Basis functions for the RBF neural network
        int[] rbfBasisFunction = new int[] 
        {
            0
        };
        
        // For the Feed Forward net
        ActivationFunction[] activationFunction = new ActivationFunction[] 
        {
            ActivationFunction.LOGISTIC
        };
        
        // Learning rates
        double[] learningRate = new double[]
        {
            0.01
        };
        
        // { Numbers of nodes in each hidden layer, number of clusters }
        int[][] clusters = new int[][]
        {
            {100,100}
        };
        
        // Momentums for the Feed Forward neural net
        double[] momentum = new double[]
        {
            0.01
        };
        
        // Numbers of hidden layers.
        int[] hiddenNum = new int[]
        {
            1
        };
        
        runExperiment(sizes,dimensions,repeats,rbfBasisFunction,activationFunction,learningRate,clusters,momentum,hiddenNum);
    }
    
    public static void setFeedforward() {
        Scanner in = new Scanner(System.in);
        System.out.println("-----Set the parameters for the feed forward neural net-----");
        System.out.print("Training set size: ");
        int trainingSetSize = in.nextInt();
        System.out.println();
        System.out.print("Building set size: ");
        int hiddenLayers = in.nextInt();
        System.out.println();
        System.out.print("Number of inputs: ");
        int[] sizesLayers = new int[3];
        sizesLayers[0] = in.nextInt();
        System.out.println();
        System.out.print("Number of examples: ");
        sizesLayers[1] = in.nextInt();
        System.out.println();
        System.out.print("Number of outputs: ");
        sizesLayers[2] = in.nextInt();
        System.out.println();
        System.out.print("Learning rate: ");
        double learningRate = in.nextDouble();
        System.out.println();
        System.out.print("Momentum: ");
        double momentum = in.nextDouble();
        System.out.println();
        System.out.print("Activation function ('linear' or 'logistic'): ");
        String selection = in.nextLine().toLowerCase();
        System.out.println();
        ActivationFunction activationFunction;
        if (selection.charAt(1) == 'o' || selection.charAt(2) == 'g') {
            activationFunction = ActivationFunction.LOGISTIC;
            selection = "Logistic";
        } else {
            activationFunction = ActivationFunction.LINEAR;
            selection = "Linear";
        }
        
        System.out.format("Hidden layers: %d, Inputs: %d, Examples: %d, Outputs: %d,%nLearning rate: %f, Momentum: %f, Activation function: %s%n",
                hiddenLayers, sizesLayers[0], sizesLayers[1], sizesLayers[2], learningRate, momentum, selection);
        
    }
    
    public static void setRBF() {
        Scanner in = new Scanner(System.in);
        System.out.println("-----Set the parameters for the RBF neural net-----");
        System.out.print("Training set size: ");
        int trainingSetSize = in.nextInt();
        System.out.println();
        System.out.print("Building set size: ");
        int buildingSetSize = in.nextInt();
        System.out.println();
        System.out.print("Number of inputs: ");
        int numInputs = in.nextInt();
        System.out.println();
        System.out.print("Number of clusters: ");
        int numbOfClusters = in.nextInt();
        System.out.println();
        System.out.print("Learning rate: ");
        double learningRate = in.nextDouble();
        System.out.println();
        
        System.out.format("Training set size: %d, Building set size: %d, Inputs: %d, Number of clusters: %d, Learning rate: %f%n",
                trainingSetSize, buildingSetSize, numInputs, numbOfClusters, learningRate);
        
    }
    
    public static double[][] trainFF(
                int size, 
                int dimensions, 
                int[] repeats, 
                int rbfBasisFunction, 
                ActivationFunction activationFunction,
                double learningRate,
                int[] clusters,
                double momentums,
                int hidden,
                double[][] dataset) 
    {
        // Create the sizes array used by the feed forward neural net
        int[] sizesArray = new int[hidden + 2];
        sizesArray[0] = dimensions + 1;
        for(int i = 1; i < sizesArray.length - 1; i++) {
            sizesArray[i] = clusters[0];
        }
        sizesArray[sizesArray.length-1] = 2;
        
        // Initialize the storage for the output of this test.
        double[][] output = new double[5][10];
        
        // For 5 repetitions of 2-fold cross validation
        for (int i = 0; i < 5; i++) {
            double[][][] datasets = partitionData(dataset);
            
            // First: train on datasets[0], test on datasets[1]
            FeedForwardNeuralNetwork ff = RunFeedForward.run(datasets[0], hidden, sizesArray, learningRate, momentums, activationFunction, repeats[1]);
            
            double confidenceSum = 0.0;
            double aboveRight = 0.0;
            double aboveWrong = 0.0;
            double belowRight = 0.0;
            double belowWrong = 0.0;
            
            for(int j = 0; j < datasets[1].length; j++) {
                int index = datasets[1][j].length - 1;
                double actualValue = datasets[1][j][index];
                double offsetValue = plusOrMinus10(actualValue);
                datasets[1][j][index] = offsetValue;
                double[] prediction = ff.compute(datasets[1][j]);
                datasets[1][j][index] = actualValue;
                //System.out.println("confidence correct: " + prediction[1] + "\nconfidence incorrect: " + prediction[0]);

                boolean abovePredicted =  prediction[1] < prediction[0];
                boolean aboveActual = actualValue < offsetValue;
                double confidence;
                if (abovePredicted && aboveActual) {
                    confidence = prediction[0];
                    aboveRight += 1.0;
                } else if (abovePredicted) {
                    confidence = prediction[0];
                    aboveWrong += 1.0;
                } else if (aboveActual) {
                    confidence = prediction[1];
                    belowWrong += 1.0;
                } else {
                    confidence = prediction[1];
                    belowRight += 1.0;
                }
                
                confidenceSum += confidence;
            }
        
            confidenceSum /= datasets[1].length;
            
            output[i][0] = aboveRight;
            output[i][1] = aboveWrong;
            output[i][2] = belowRight;
            output[i][3] = belowWrong;
            output[i][4] = confidenceSum;
            
            
            // Second: train on datasets[1], test on datasets[0]
            ff = RunFeedForward.run(datasets[1], hidden, sizesArray, learningRate, momentums, activationFunction, repeats[1]);
        
            confidenceSum = 0.0;
            aboveRight = 0.0;
            aboveWrong = 0.0;
            belowRight = 0.0;
            belowWrong = 0.0;
            
            for(int j = 0; j < datasets[0].length; j++) {
                int index = datasets[0][j].length - 1;
                double actualValue = datasets[0][j][index];
                double offsetValue = plusOrMinus10(actualValue);
                datasets[0][j][index] = offsetValue;
                double[] prediction = ff.compute(datasets[0][j]);
                datasets[0][j][index] = actualValue;
                
                boolean abovePredicted =  prediction[1] < prediction[0];
                boolean aboveActual = actualValue < offsetValue;
                double confidence;
                if (abovePredicted && aboveActual) {
                    confidence = prediction[0];
                    aboveRight += 1.0;
                } else if (abovePredicted) {
                    confidence = prediction[0];
                    aboveWrong += 1.0;
                } else if (aboveActual) {
                    confidence = prediction[1];
                    belowWrong += 1.0;
                } else {
                    confidence = prediction[1];
                    belowRight += 1.0;
                }
                
                confidenceSum += confidence;
            }
        
            confidenceSum /= datasets[0].length;
            
            output[i][5] = aboveRight;
            output[i][6] = aboveWrong;
            output[i][7] = belowRight;
            output[i][8] = belowWrong;
            output[i][9] = confidenceSum;
        }
        
        return output;
    }
    
    public static double[][] trainRBF(
                int size, 
                int dimensions, 
                int[] repeats, 
                int rbfBasisFunction, 
                ActivationFunction activationFunction,
                double learningRate,
                int[] clusters,
                double momentums,
                int hidden,
                double[][] dataset) 
        {
        
        double[][] output = new double[5][10];
                
        for (int i = 0; i < 5; i++) {
            double[][][] datasets = partitionData(dataset);
            
            RBFNeuralNetwork rbf = RunRBF.testRBF(datasets[0], datasets[0], learningRate, clusters[1], rbfBasisFunction, repeats[1]);
        
            double aboveRight = 0.0;
            double aboveWrong = 0.0;
            double belowRight = 0.0;
            double belowWrong = 0.0;
            double varianceSum = 0.0;
        
            for(int j = 0; j < datasets[1].length; j++) {
                int index = datasets[1][j].length - 1;
                double actualValue = datasets[1][j][index];
                double offsetValue = plusOrMinus10(actualValue);
                double predictedValue = rbf.getResult(datasets[1][j]);
                
                varianceSum += Math.abs(predictedValue - actualValue);
                
                boolean abovePredicted = rbf.aboveValue(datasets[1][j], offsetValue);

                boolean aboveActual = actualValue < offsetValue;

                if (abovePredicted && aboveActual) {
                    aboveWrong += 1.0;
                } else if (abovePredicted) {
                    aboveRight += 1.0;
                } else if (aboveActual) {
                    belowRight += 1.0;
                } else {
                    belowWrong += 1.0;
                }
            }
        
            varianceSum /= datasets[1].length;
            output[i][0] = aboveRight;
            output[i][1] = aboveWrong;
            output[i][2] = belowRight;
            output[i][3] = belowWrong;
            output[i][4] = varianceSum;
            
            
            rbf = RunRBF.testRBF(datasets[1], datasets[1], learningRate, clusters[1], rbfBasisFunction, repeats[1]);
        
            aboveRight = 0.0;
            aboveWrong = 0.0;
            belowRight = 0.0;
            belowWrong = 0.0;
            varianceSum = 0.0;
        
            for(int j = 0; j < datasets[0].length; j++) {
                int index = datasets[0][j].length - 1;
                double actualValue = datasets[0][j][index];
                double offsetValue = plusOrMinus10(actualValue);
                double predictedValue = rbf.getResult(datasets[0][j]);
                
                varianceSum += Math.abs(predictedValue - actualValue);
                
                boolean abovePredicted = rbf.aboveValue(datasets[0][j], offsetValue);

                boolean aboveActual = actualValue < offsetValue;
                
                if (abovePredicted && aboveActual) {
                    aboveWrong += 1.0;
                } else if (abovePredicted) {
                    aboveRight += 1.0;
                } else if (aboveActual) {
                    belowRight += 1.0;
                } else {
                    belowWrong += 1.0;
                }
            }
        
            varianceSum /= datasets[0].length;
            output[i][5] = aboveRight;
            output[i][6] = aboveWrong;
            output[i][7] = belowRight;
            output[i][8] = belowWrong;
            output[i][9] = varianceSum;
        }
        
        return output;
    }
    
    private static double plusOrMinus10(double output) {
        double newVal = output + output * 0.1 * Math.pow(-1, (double)(rand.nextInt(2) + 1)); //Math.pow(-1, (double)(rand.nextInt(2) + 1)) * 2 * output * Math.random() + output;
        return newVal;
    }
    
    private static double[][][] swapTrainingAndTesting(double[][][] trainingAndTesting) {
        for (int i = 0; i < trainingAndTesting[0].length; i++) {
            int lastIndex = trainingAndTesting[0][i].length - 1;
            int secondLast = lastIndex - 1;
            
            double swap = trainingAndTesting[0][i][lastIndex];
            trainingAndTesting[0][i][lastIndex] = trainingAndTesting[0][i][secondLast];
            trainingAndTesting[0][i][secondLast] = swap;
            
            swap = trainingAndTesting[1][i][lastIndex];
            trainingAndTesting[1][i][lastIndex] = trainingAndTesting[1][i][secondLast];
            trainingAndTesting[1][i][secondLast] = swap;
        }
        
        return trainingAndTesting;
    }
    
    private static double[][][] partitionData(double[][] dataset) {
        
        int numInputs = dataset[0].length;
        int size = dataset.length;
        
        double[][] trainingData = new double[size/2][numInputs];
        double[][] testingData = new double[size/2][numInputs];
        
        int trainingIndex = 0;
        int testingIndex = 0;
        int dataIndex = 0;
        int half = size/2;
        
        while(trainingIndex < half && testingIndex < half) {
            if (rand.nextBoolean()) {
                for (int i = 0; i < numInputs; i++) {
                    trainingData[trainingIndex][i] = dataset[dataIndex][i];
                }
                
                //trainingData[trainingIndex][numInputs] = plusOrMinus10(trainingData[trainingIndex][numInputs - 1]);
                
                trainingIndex++;
                dataIndex++;
            } else {
                for (int i = 0; i < numInputs; i++) {
                    testingData[testingIndex][i] = dataset[dataIndex][i];
                }
                
                //testingData[testingIndex][numInputs] = plusOrMinus10(testingData[testingIndex][numInputs - 1]);
                
                testingIndex++;
                dataIndex++;
            }
        }
        
        while(trainingIndex < half) {
            for (int i = 0; i < numInputs; i++) {
                trainingData[trainingIndex][i] = dataset[dataIndex][i];
            }
                
            //trainingData[trainingIndex][numInputs] = plusOrMinus10(trainingData[trainingIndex][numInputs - 1]);
                
            trainingIndex++;
            dataIndex++;
        }
        
        while(testingIndex < half) {
            for (int i = 0; i < numInputs; i++) {
                testingData[testingIndex][i] = dataset[dataIndex][i];
            }
            
            //testingData[testingIndex][numInputs] = plusOrMinus10(testingData[testingIndex][numInputs - 1]);
            
            testingIndex++;
            dataIndex++;
        }
        
        return new double[][][] { trainingData, testingData };
    }
    
    public static void runExperiment(
            int[] sizes, 
            int[] dimensions, 
            int[][] repeats, 
            int[] rbfBasisFunction, 
            ActivationFunction[] activationFunction, 
            double[] learningRate, 
            int[][] clusters, 
            double[] momentum, 
            int[] hiddenNum) {
        
        // Check for empty and null-valued arguments and initialize to default values.
        if (sizes == null || sizes.length == 0) {
            sizes = new int[]{100000};
        }
        if (dimensions == null || dimensions.length == 0) {
            dimensions = new int[]{4};
        }
        if (repeats == null || repeats.length == 0) {
            repeats = new int[][]{{1,1}};
        }
        if (rbfBasisFunction == null || rbfBasisFunction.length == 0) {
            rbfBasisFunction = new int[]{0};
        }
        if (activationFunction == null || activationFunction.length == 0) {
            activationFunction = new ActivationFunction[]{ActivationFunction.LOGISTIC};
        }
        if (learningRate == null || learningRate.length == 0) {
             learningRate = new double[]{0.01};
        }
        if (clusters == null || clusters.length == 0) {
            clusters = new int[][]{{100,10000}};
        }
        if (momentum == null || momentum.length == 0) {
            momentum = new double[]{0.01};
        }
        if (hiddenNum == null || hiddenNum.length == 0) {
            hiddenNum = new int[]{1};
        }
        
        // calculate the number of experiments
        int experimentCount = 
                sizes.length * 
                repeats.length * 
                dimensions.length * 
                rbfBasisFunction.length * 
                activationFunction.length * 
                learningRate.length * 
                clusters.length * 
                momentum.length * 
                hiddenNum.length;
        
        
        double[][][][] results = new double[2][experimentCount][][];
        int experimentIndex = 0;
        
        // Loop through all possible combinations of variables (most arrays should be of size 1)
        for (int a = 0; a < sizes.length; a++) {
            for (int b = 0; b < dimensions.length; b++) {
                // Retrieve a dataset of size a with b inputs, or generate one if it does not exist
                double[][] datasets = DataTools.getDataFromFile(dimensions[b], sizes[a]);
                
                for (int c = 0; c < repeats.length; c++) {
                    for (int d = 0; d < rbfBasisFunction.length; d++) {
                        for (int e = 0; e < activationFunction.length; e++) {
                            for (int f = 0; f < learningRate.length; f++) {
                                for (int g = 0; g < clusters.length; g++) {
                                    for (int h = 0; h < momentum.length; h++) {
                                        for (int i = 0; i < hiddenNum.length; i++) {
                                            
                                            // Print information about the next experiment
                                            System.out.println("Experiment " + experimentIndex);
                                            System.out.println("# of training examples" + sizes[a]);
                                            System.out.println("Dataset size: " + dimensions[b]);
                                            System.out.println("Repeats:" + repeats[c]);
                                            System.out.println("RBF basis function: " + rbfBasisFunction[d]);
                                            System.out.println("FF activation function: " + activationFunction[e]);
                                            System.out.println("Learning rate: " + learningRate[f]);
                                            System.out.println("Clusters: " + clusters[g][1]);
                                            System.out.println("Momentum: " + momentum[h]);
                                            System.out.println("Hidden layers: " + hiddenNum[i]);
                                            System.out.println();
                                            
                                            
                                            // Train and test an RBF neural net
                                            results[0][experimentIndex] = trainRBF(
                                                    sizes[a], 
                                                    dimensions[b], 
                                                    repeats[c], 
                                                    rbfBasisFunction[d], 
                                                    activationFunction[e], 
                                                    learningRate[f], 
                                                    clusters[g],
                                                    momentum[h], 
                                                    hiddenNum[i], 
                                                    datasets);
                                            
                                            double[] statistics = new double[5];
                                            for (int j = 0; j < 5; j++) {
                                                for (int k = 0; k < 10; k++) {
                                                    statistics[k%5] += results[0][experimentIndex][j][k];
                                                }
                                            }
                                            statistics[4] /= 10;
                                            
                                            System.out.println("------------RBF Neural Network------------");
                                            System.out.println("  Correct above guesses: " + statistics[0]);
                                            System.out.println("Incorrect above guesses: " + statistics[1]);
                                            System.out.println("  Correct below guesses: " + statistics[2]);
                                            System.out.println("Incorrect below guesses: " + statistics[3]);
                                            System.out.println("       Average variance: " + statistics[4]);
                                            System.out.println();
                                            //swapTrainingAndTesting(datasets);
                                            
                                            
                                            // Train and test a Feedforward neural net
//                                            results[1][experimentIndex] =  trainFF(
//                                                    sizes[a],
//                                                    dimensions[b],
//                                                    repeats[c],
//                                                    rbfBasisFunction[d],
//                                                    activationFunction[e],
//                                                    learningRate[f],
//                                                    clusters[g],
//                                                    momentum[h],
//                                                    hiddenNum[i],
//                                                    datasets);
//
//                                            statistics = new double[5];
//                                            for (int j = 0; j < 5; j++) {
//                                                for (int k = 0; k < 10; k++) {
//                                                    statistics[k%5] += results[1][experimentIndex][j][k];
//                                                }
//                                            }
//
//                                            statistics[4] /= 10;
//
//                                            System.out.println("--------Feed Forward Neural Network--------");
//                                            System.out.println("  Correct above guesses: " + statistics[0]);
//                                            System.out.println("Incorrect above guesses: " + statistics[1]);
//                                            System.out.println("  Correct below guesses: " + statistics[2]);
//                                            System.out.println("Incorrect below guesses: " + statistics[3]);
//                                            System.out.println("       Average variance: " + statistics[4]);
//                                            System.out.println();
                                            
                                            experimentIndex++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
