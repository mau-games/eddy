package util.algorithms;

import util.Point;

public abstract class ScaleMatrix {
    public abstract int[][] Upscale();
    public abstract int[][] Downscale();
    public abstract int[][] getScaledMatrix();
}
