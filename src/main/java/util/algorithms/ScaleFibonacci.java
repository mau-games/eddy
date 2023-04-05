package util.algorithms;

//Fibonacci sequence in the context of scaling maps
public class ScaleFibonacci {
    private int[][] matrix;
    private double scaleFactor;
    private boolean isUpscale;

    public ScaleFibonacci(int[][] matrix, double scaleFactor, boolean isUpscale){
        this.matrix = matrix;
        this.scaleFactor = scaleFactor;
        this.isUpscale = isUpscale;
    }
}
