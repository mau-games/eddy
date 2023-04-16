package game;

import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
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

public class RoomScale{
    private Room origRoom;
    private int[][] origMatrix;
    private static int granularity = 5;

    public enum ScaleType{
        NONE,
        NearestNeighbour,
        Fibonacci
    }
    public enum SizeAdjustType{
        NONE,
        Upscale,
        Downscale
    }
    private ScaleType scaleType;
    private SizeAdjustType sizeAdjustType;
    private double scaleFactor;
    private HashMap<DimensionTypes, Double> preserveDimValues;

    public RoomScale(Room origRoom, String sizeAdjustType, String strScaleType, double scaleFactor, String[] preserveDimArr){
        this.origRoom = origRoom;
        this.sizeAdjustType = SizeAdjustType.valueOf(sizeAdjustType);
        this.scaleType = ScaleType.valueOf(strScaleType);
        this.scaleFactor = scaleFactor;
        this.preserveDimValues = new HashMap<DimensionTypes, Double>();
        PreserveDimType(preserveDimArr);
    }

    private void PreserveDimType(String[] preserveDimArr){
        origRoom.calculateAllDimensionalValues();
        for(int i = 0; i<preserveDimArr.length; i++){
            switch (preserveDimArr[i]){
                case "NONE":
                case "":
                    break;
                case "LENIENCY":
                    preserveDimValues.put(DimensionTypes.LENIENCY, origRoom.getDimensionValue(DimensionTypes.LENIENCY));
                    break;
                case "SIMILARITY":
                    preserveDimValues.put(DimensionTypes.SIMILARITY, origRoom.getDimensionValue(DimensionTypes.SIMILARITY));
                    break;
                case "INNER-SIMILARITY":
                    preserveDimValues.put(DimensionTypes.INNER_SIMILARITY, origRoom.getDimensionValue(DimensionTypes.INNER_SIMILARITY));
                    break;
                case "SYMMETRY":
                    preserveDimValues.put(DimensionTypes.SYMMETRY, origRoom.getDimensionValue(DimensionTypes.SYMMETRY));
                    break;
                case "NUMBER_MESO_PATTERN":
                    preserveDimValues.put(DimensionTypes.NUMBER_MESO_PATTERN, origRoom.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN));
                    break;
                case "NUMBER_PATTERNS":
                    preserveDimValues.put(DimensionTypes.NUMBER_MESO_PATTERN, origRoom.getDimensionValue(DimensionTypes.NUMBER_PATTERNS));
                    break;
                case "LINEARITY":
                    preserveDimValues.put(DimensionTypes.LINEARITY, origRoom.getDimensionValue(DimensionTypes.LINEARITY));
                    break;
            }

            //System.out.println(preserveDimValues.keySet() + " " + preserveDimValues.get(DimensionTypes.valueOf(preserveDimArr[i])) + '\n' + " ");
        }
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
            case NONE:
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
            case NONE:
                System.out.println("No scaling");
                break;
            default:
                System.out.println("Invalid: sizeAdjustment");
                break;
        }
        return null;
    }

    public int[][] calcMatrixEa(){
        System.out.println("PreserveDimensions");
        //1. Start EA but narrow it down according to dimension types

        //2. Receive generated matrix from EA

        //3. Create a new room from that matrix

        //int[][] eaMatrix = calcMatrixEA;
        //router.postEvent(new RequestMatrixGeneratedRoom(eaMatrix));

        return null;
    }

    public HashMap<GADimension.DimensionTypes, Double> getPreservedDimValues(){
        return preserveDimValues;
    }

    public MAPEDimensionFXML[] getMAPEDimensions(){
        MAPEDimensionFXML[] mapeDimensionFXMLS = new MAPEDimensionFXML[preserveDimValues.size()];
        int counter = 0;
        DimensionTypes[] dimensionTypes = preserveDimValues.keySet().toArray(new DimensionTypes[preserveDimValues.size()]);

        for (DimensionTypes dimType: dimensionTypes) {
            MAPEDimensionFXML mapeDimensionFXML = new MAPEDimensionFXML(dimType, granularity);
            mapeDimensionFXMLS[counter] = mapeDimensionFXML;
            counter++;
        }
        return mapeDimensionFXMLS;
    }
}
