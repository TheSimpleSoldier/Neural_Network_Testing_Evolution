package training;

import feedforward.FeedForwardNeuralNetwork;

public class MuLambdaEvolution extends GeneticAlgorithm
{
    double[] parameters;
    public MuLambdaEvolution(double[] parameters)
    {
        this.parameters = parameters;
    }

    /**
     * This method initializes the GA population using random values
     */
    public double[][] initialize(int populationSize)
    {
        double[][] population = new double[populationSize][];

        for (int i = 0; i < populationSize; i++)
        {
            this.net.generateRandomWeights();
            population[i] = this.net.getWeights();
        }

        return population;
    }

    /**
     * This method selects individuals from the population to
     * reproduce
     */
    public double[][] select(int populationSize, double[][] currentPop, double[][] examples)
    {
        double[] values = new double[currentPop.length];
        double[][] children = new double[populationSize][currentPop[0].length];

        for (int i = 0; i < currentPop.length; i++)
        {
            values[i] = fitnessFunction(currentPop[i], examples);
        }

        for (int i = 0; i < currentPop.length; i++)
        {
            for (int j = i; j < currentPop.length; j++)
            {
                if (values[j] > values[i])
                {
                    double tempValue = values[i];
                    values[i] = values[j];
                    values[i] = tempValue;

                    double[] temp = currentPop[i];
                    currentPop[i] = children[j];
                    currentPop[j] = temp;
                }
            }
        }

        for (int i = 0; i < children.length; i++)
        {
            children[i] = currentPop[i % currentPop.length];
        }

        return children;
    }

    /**
     * This method uses the parents and children to create a new pool
     * eliminating less fit individuals
     */
    public double[][] Replace(double[][] population, int size, double[][] examples)
    {
        double[][] newPop = new double[size][population[0].length];
        double[] scores = new double[population.length];

        for (int i = 0; i < population.length; i++)
        {
            scores[i] = fitnessFunction(population[i], examples);
        }

        for (int i = 0; i < population.length; i++)
        {
            for (int j = i; j < population.length; j++)
            {
                if (scores[j] > scores[i])
                {
                    double tempValue = scores[i];
                    scores[i] = scores[j];
                    scores[i] = tempValue;

                    double[] temp = population[i];
                    population[i] = population[j];
                    population[j] = temp;
                }
            }
        }

        for (int i = 0; i < size; i++)
        {
            newPop[i] = population[i];
        }

        return newPop;
    }

    /**
     * This function will use GA to train a neural net
     *
     * @param net
     * @param examples
     * [0] => Mu
     * [1] => Lamda
     * [2] => # of generations
     * [3] => mutation rate
     * [4] => cross over rate
     * [5] => crossover Type
     *
     * @return
     */
    @Override
    public FeedForwardNeuralNetwork run(FeedForwardNeuralNetwork net, double[][] examples)
    {
        this.net = net;
        int mu = (int) parameters[0];
        int lamda = (int) parameters[1];
        int generations = (int) parameters[2];
        double mutationRate = parameters[3];
        double crossoverRate = parameters[4];
        int crossoverType = (int) parameters[5];
        double[][][] paritionedExamples = new double[10][examples.length / 10][];

        for (int i = 0; i < 10; i++)
        {
            for (int j = 0; j < examples.length / 10; j++)
            {
                paritionedExamples[i][j] = examples[(i*10 + j) % examples.length];
            }
        }

        double[][] population = initialize(mu);

        for (int i = 0; i < generations; i++)
        {
            int crossoverParents = (int) (Math.random() * 5) + 1;
            //System.out.println("Running Generationg: " + i);
            double[][] children = select(lamda, population, paritionedExamples[i%10]);
            //System.out.println("Children have been selected");
            children = operate(children, mutationRate, crossoverRate, crossoverParents, crossoverType);
            //System.out.println("Children have been operated on");
            double[][] totalPop = combine(population, children);
            //System.out.println("Populations have been combined");
            population = Replace(totalPop, mu, paritionedExamples[i%10]);
            //System.out.println("Replacement has occured");
        }

        //System.out.println("Picking best GA chromosome");
        net.setWeights(getBestWeights(population, examples));

        double[] weights = net.getWeights();
        double averageWeight = 0.0;
//        for(int i = 0; i < weights.length; i++)
//        {
//            System.out.print(weights[i] + ", ");
//
//            averageWeight += Math.abs(weights[i]) / weights.length;
//        }
//        System.out.println();
//        System.out.println("Average weight: " + averageWeight);

        return net;
    }

}
