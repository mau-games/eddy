package game;

import generator.algorithm.MAPElites.Dimensions.GADimension;
import util.algorithms.NearestNeighbour;
import util.algorithms.ScaleFibonacci;
import util.algorithms.ScaleMatrix;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.PreserveDimensions;
import util.eventrouting.events.RequestMatrixGeneratedRoom;

import java.util.Arrays;
import java.util.HashMap;

public class RoomScale implements Listener{
    private Room origRoom;
    private int[][] origMatrix;
    private static EventRouter router = EventRouter.getInstance();

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
    private HashMap<String, Double> preserveDimValues;

    public RoomScale(Room origRoom, String sizeAdjustType, String strScaleType, double scaleFactor, String[] preserveDimArr){
        router.registerListener(this, new PreserveDimensions());
        this.origRoom = origRoom;
        this.sizeAdjustType = SizeAdjustType.valueOf(sizeAdjustType);
        this.scaleType = ScaleType.valueOf(strScaleType);
        this.scaleFactor = scaleFactor;
        this.preserveDimValues = new HashMap<String, Double>();
        for(int i = 0; i< preserveDimArr.length; i++){
            preserveDimValues.put(preserveDimArr[i], 0.0);
        }
        PreserveDimType();
    }

    private void PreserveDimType(){
        origRoom.calculateAllDimensionalValues();
        preserveDimValues.forEach((strDim, dimVal) ->{
            switch (strDim){
                case "None":
                case "":
                    break;
                case "Difficulty":
                    preserveDimValues.put(strDim, origRoom.getDimensionValue(GADimension.DimensionTypes.LENIENCY));
                    break;
                case "Similarity":
                    preserveDimValues.put(strDim, origRoom.getDimensionValue(GADimension.DimensionTypes.SIMILARITY));
                    break;
                case "Inner-similarity":
                    preserveDimValues.put(strDim, origRoom.getDimensionValue(GADimension.DimensionTypes.INNER_SIMILARITY));
                    break;
                case "Symmetry":
                    preserveDimValues.put(strDim, origRoom.getDimensionValue(GADimension.DimensionTypes.SYMMETRY));
                    break;
                case "Number of Meso-Patterns":
                    preserveDimValues.put(strDim, origRoom.getDimensionValue(GADimension.DimensionTypes.NUMBER_MESO_PATTERN));
                    break;
                case "Number of Patterns":
                    preserveDimValues.put(strDim, origRoom.getDimensionValue(GADimension.DimensionTypes.NUMBER_PATTERNS));
                    break;
                case "Linearity":
                    preserveDimValues.put(strDim, origRoom.getDimensionValue(GADimension.DimensionTypes.LINEARITY));
                    break;
            }
            System.out.println(strDim + " " + preserveDimValues.get(strDim) + '\n' + " ");
        });

    }

    public int[][] calcScaledMatrix(){
        ScaleMatrix scaleMatrix = null;
        switch (scaleType){
            case NearestNeighbour:
                scaleMatrix = new NearestNeighbour(origRoom.toMatrix(), (int)scaleFactor);
                break;
            case Fibonacci:
                scaleMatrix = new ScaleFibonacci(origRoom.toMatrix(), scaleFactor);
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
                origMatrix = scaleMatrix.Upscale();
                System.out.println(Arrays.deepToString(origMatrix));
                return origMatrix;
            case Downscale:
                origMatrix = scaleMatrix.Downscale();
                System.out.println(Arrays.deepToString(origMatrix));
                return origMatrix;
            case None:
                System.out.println("No scaling");
                break;
            default:
                System.out.println("Invalid: sizeAdjustment");
                break;
        }
        return null;
    }

    @Override
    public synchronized void ping(PCGEvent e){
        if(e instanceof PreserveDimensions){
            System.out.println("PreserveDimensions");
            //1. Start EA but narrow it down according to dimension types

            //2. Receive generated matrix from EA

            //3. Create a new room from that matrix

            //int[][] eaMatrix = calcMatrixEA;
            //router.postEvent(new RequestMatrixGeneratedRoom(eaMatrix));
        }
    }
}
