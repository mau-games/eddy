package generator.algorithm;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetricChromosome that = (MetricChromosome) o;
        return Float.compare(that.weight, weight) == 0 && functionType == that.functionType && function == that.function;
    }

    @Override
    public int hashCode() {
        return Objects.hash(weight, functionType, function);
    }
}

