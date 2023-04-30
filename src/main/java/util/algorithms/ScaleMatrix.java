package util.algorithms;

//In either of the algorithms the doors are not being placed accurately in the
//scaled maps, hence, connections to the side of the map is necessary
public abstract class ScaleMatrix {
    public abstract int[][] Upscale();
    public abstract int[][] Downscale();
    public abstract int[][] getScaledMatrix();
}
