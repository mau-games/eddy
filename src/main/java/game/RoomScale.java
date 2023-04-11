package game;

import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.LeniencyGADimension;
import util.algorithms.NearestNeighbour;
import util.algorithms.ScaleFibonacci;
import util.algorithms.ScaleMatrix;
import util.eventrouting.Listener;
import util.eventrouting.events.RequestScaleSettings;

import java.util.Arrays;

public class RoomScale{

    private Room scaleRoom;

    public enum ScaleType{
        None,
        NearestNeighbour,
        Fibonacci
    }
    public enum SizeAdjustType{
        None,
        Upscale,
        Downscale
    }
    
    private ScaleType scaleType;
    private SizeAdjustType sizeAdjustType;
    private double scaleFactor;
    private String[] preserveDimArr;

    public RoomScale(Room scaleRoom, String sizeAdjustType, String strScaleType, double scaleFactor, String[] preserveDimArr){
        this.scaleRoom = scaleRoom;
        this.sizeAdjustType = SizeAdjustType.valueOf(sizeAdjustType);
        this.scaleType = ScaleType.valueOf(strScaleType);
        this.scaleFactor = scaleFactor;
        this.preserveDimArr = preserveDimArr;
        PreserveDimType();
    }

    private void PreserveDimType(){
        double value = 0;
        for (String dimType: preserveDimArr) {
            switch (dimType){
                case "None":
                case "":
                    break;
                case "Difficulty":
                    value = scaleRoom.getDimensionValue(GADimension.DimensionTypes.LENIENCY);
                    System.out.println("");
                    System.out.println("Difficulty: " + value + '\n' + " ");
                    break;
                case "Similarity":
                    value = scaleRoom.getDimensionValue(GADimension.DimensionTypes.SIMILARITY);
                    System.out.println("Similarity: " + value + '\n' + " ");
                    break;
                case "Symmetry":
                    value = scaleRoom.getDimensionValue(GADimension.DimensionTypes.SYMMETRY);
                    System.out.println("Symmetry: " + value + '\n' + " ");
                    break;
                case "Number of Meso-Patterns":
                    value = scaleRoom.getDimensionValue(GADimension.DimensionTypes.NUMBER_MESO_PATTERN);
                    System.out.println("Number of Meso-patterns: " + value + '\n' + " ");
                    break;
            }
        }
    }

    public int[][] calcScaledMatrix(){
        ScaleMatrix scaleMatrix = null;
        int[][] matrix;

        switch (scaleType){
            case NearestNeighbour:
                scaleMatrix = new NearestNeighbour(scaleRoom.toMatrix(), (int)scaleFactor);
                break;
            case Fibonacci:
                scaleMatrix = new ScaleFibonacci(scaleRoom.toMatrix(), scaleFactor);
                break;
            case None:
                System.out.println("No scaletype");
                break;
            default:
                System.out.println("Invalid: scaletype");
                break;
        }
        System.out.println(sizeAdjustType.toString());

        switch (sizeAdjustType){
            case Upscale:
                matrix = scaleMatrix.Upscale();
                System.out.println(Arrays.deepToString(matrix));
                return matrix;
            case Downscale:
                matrix = scaleMatrix.Downscale();
                System.out.println(Arrays.deepToString(matrix));
                return matrix;
            case None:
                System.out.println("No scaling");
                break;
            default:
                System.out.println("Invalid: sizeAdjustment");
                break;
        }
        return null;
    }
}
