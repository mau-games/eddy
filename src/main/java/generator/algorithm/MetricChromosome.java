package generator.algorithm;

public class MetricChromosome
{
    float weight;
    int functionType;
    int function;

    public MetricChromosome(float w, int funcT, int function)
    {
        this.weight = w;
        this.functionType = funcT;
        this.function = function;
    }
}

