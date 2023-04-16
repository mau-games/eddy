package util.algorithms;

//Fibonacci sequence in the context of scaling maps
public class ScaleFibonacci extends ScaleMatrix{
    private int[][] matrix;
    private int[][] scaledMatrix;
    private double scaleFactor;

    public ScaleFibonacci(int[][] matrix, double scaleFactor){
        this.matrix = matrix;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public int[][] Upscale(){
        return scaledMatrix;
    }

    @Override
    public int[][] Downscale(){
        return scaledMatrix;
    }

    public int[][] getScaledMatrix(){
        return scaledMatrix;
    }
}
