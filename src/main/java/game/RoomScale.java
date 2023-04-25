package game;

import generator.algorithm.MAPElites.Dimensions.CharacteristicSimilarityGADimension;
import generator.algorithm.MAPElites.Dimensions.GADimension;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.Dimensions.GADimension.DimensionTypes;
import generator.algorithm.MAPElites.Dimensions.SimilarityGADimension;
import javafx.application.Platform;
import util.Point;
import util.algorithms.NearestNeighbour;
import util.algorithms.ScaleFibonacci;
import util.algorithms.ScaleMatrix;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class RoomScale implements Listener{
    private Room origRoom;
    private Room scaledRoom;
    private Room scaledEaRoom;
    private int[][] eaMatrix;
    private static int granularity = 5;
    private static EventRouter router = EventRouter.getInstance();

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
    public enum RoomType{
        Original,
        Scaled,
        EaScaled
    }

    private ScaleType scaleType;
    private SizeAdjustType sizeAdjustType;
    private double scaleFactor;
    private HashMap<DimensionTypes, Double> preserveDimValues;

    public RoomScale(Room origRoom, String sizeAdjustType, String strScaleType, double scaleFactor, String[] preserveDimArr){

        router.registerListener(this, new MAPElitesDone());

        this.origRoom = origRoom;
        this.sizeAdjustType = SizeAdjustType.valueOf(sizeAdjustType);
        this.scaleType = ScaleType.valueOf(strScaleType);
        this.scaleFactor = scaleFactor;
        preserveDimValues = new HashMap<DimensionTypes, Double>();
        PreserveDimType(preserveDimArr);
    }

    private void PreserveDimType(String[] preserveDimArr){
        origRoom.calculateAllDimensionalValues();
        int counter = 0;
        for(int i = 0; i<preserveDimArr.length; i++){
            switch (preserveDimArr[i]){
                case "LENIENCY":
                    preserveDimValues.put(DimensionTypes.LENIENCY, origRoom.getDimensionValue(DimensionTypes.LENIENCY));
                    break;
                case "INNER-SIMILARITY":
                    preserveDimValues.put(DimensionTypes.INNER_SIMILARITY, origRoom.getDimensionValue(DimensionTypes.INNER_SIMILARITY));
                    break;
                case "SYMMETRY":
                    preserveDimValues.put(DimensionTypes.SYMMETRY, origRoom.getDimensionValue(DimensionTypes.SYMMETRY));
                    break;
                case "SIMILARITY":
                    break;
                case "NUMBER_MESO_PATTERN":
                    preserveDimValues.put(DimensionTypes.NUMBER_MESO_PATTERN, origRoom.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN));
                    break;
                case "NUMBER_PATTERNS":
                    preserveDimValues.put(DimensionTypes.NUMBER_PATTERNS, origRoom.getDimensionValue(DimensionTypes.NUMBER_PATTERNS));
                    break;
                case "LINEARITY":
                    preserveDimValues.put(DimensionTypes.LINEARITY, origRoom.getDimensionValue(DimensionTypes.LINEARITY));
                    break;
                default:
                    counter++;
                    System.out.println("array-length: " + preserveDimArr.length + " counter: " + counter);
                    if(preserveDimArr.length == counter){
                        setAllDimTypes();
                        System.out.println("ALL DIMTYPES SET");
                    }
                    break;
            }
            preserveDimValues.put(DimensionTypes.SIMILARITY, origRoom.getDimensionValue(DimensionTypes.SIMILARITY));
        }
    }

    private void setAllDimTypes(){
        preserveDimValues.put(DimensionTypes.LINEARITY, origRoom.getDimensionValue(DimensionTypes.LINEARITY));
        preserveDimValues.put(DimensionTypes.NUMBER_PATTERNS, origRoom.getDimensionValue(DimensionTypes.NUMBER_PATTERNS));
        preserveDimValues.put(DimensionTypes.NUMBER_MESO_PATTERN, origRoom.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN));
        preserveDimValues.put(DimensionTypes.SYMMETRY, origRoom.getDimensionValue(DimensionTypes.SYMMETRY));
        preserveDimValues.put(DimensionTypes.INNER_SIMILARITY, origRoom.getDimensionValue(DimensionTypes.INNER_SIMILARITY));
        preserveDimValues.put(DimensionTypes.LENIENCY, origRoom.getDimensionValue(DimensionTypes.LENIENCY));
    }

    public int[][] calculateScaledMatrix(){
        int[][] matrix;
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
                matrix = scaleMatrix.Upscale();
                System.out.println(Arrays.deepToString(matrix));
                return matrix;
            case Downscale:
                matrix = scaleMatrix.Downscale();
                System.out.println(Arrays.deepToString(matrix));
                return matrix;
            case NONE:
                System.out.println("No scaling");
                break;
            default:
                System.out.println("Invalid: sizeAdjustment");
                break;
        }

        return null;
    }

    public synchronized void ping(PCGEvent e){
        if(e instanceof MAPElitesDone){

            List<Room> generatedRooms = ((MAPElitesDone) e).GetRooms();
            double topDimsDiff = Double.MAX_VALUE;
            Room currTopRoom = null;

            for (Room room : generatedRooms) {
                if (room != null) {
                    double dimsDiff = 0.0;
                    double simDiff = 0.0;
                    for (DimensionTypes dimType : preserveDimValues.keySet()) {
                        if(dimType == DimensionTypes.SIMILARITY){
                            try{
                                room.setSpeficidDimensionValue(GADimension.DimensionTypes.SIMILARITY, SimilarityGADimension.calculateValueIndependently(room, origRoom));
                            }catch (ArrayIndexOutOfBoundsException exception){
                                System.out.println(exception);
                            }
                            simDiff = room.getDimensionValue(DimensionTypes.SIMILARITY) -2;
                        }
                        else if(dimType == DimensionTypes.INNER_SIMILARITY){
                            try{
                                room.setSpeficidDimensionValue(DimensionTypes.INNER_SIMILARITY, CharacteristicSimilarityGADimension.calculateValueIndependently(room, origRoom));
                            }catch (ArrayIndexOutOfBoundsException exception){
                                System.out.println(exception);
                            }
                            simDiff = room.getDimensionValue(DimensionTypes.INNER_SIMILARITY) -2;
                        }
                        double dimValue = room.getDimensionValue(dimType);
                        double targetValue = preserveDimValues.get(dimType);
                        dimsDiff += Math.abs(dimValue - targetValue) + simDiff;
                    }
                    if (dimsDiff < topDimsDiff) {
                        topDimsDiff = dimsDiff;
                        currTopRoom = room;
                    }
                }
            }

            for (DimensionTypes dimType: preserveDimValues.keySet()) {
                System.out.println("ACTUAL DIMTYPE AND VALUE - " + dimType.toString() + ": " + preserveDimValues.get(dimType));
                System.out.println("MOST SUITABLE VALUE FOR - " + dimType.toString() + ": " + currTopRoom.getDimensionValue(dimType) + '\n');
            }

            setScaleEaMatrix(currTopRoom.toMatrix());
            Platform.runLater(()->{
                router.postEvent(new RequestMatrixGeneratedRoom(getScaledEaMatrix(), this, true));
                //createConnection(scaledRoom, scaledEaRoom, RoomType.EaScaled);
                router.postEvent(new Stop());
                ArrayList<Room> rooms = new ArrayList<Room>();
                rooms.add(scaledRoom);
                rooms.add(scaledEaRoom);

                router.postEvent(new RequestScaleView(rooms));
                router.unregisterListener(this, new MAPElitesDone());
            });
        }
    }

    public void createConnection(Room srcRoom, Room destRoom, RoomType roomType){
        RoomType roomTypeSrc = RoomType.Original;
        RoomType roomTypeDest = RoomType.Scaled;

        if(roomType != RoomType.Scaled){
            roomTypeSrc = RoomType.Original;
            roomTypeDest = RoomType.EaScaled;
        }
        Point pointSrc = calculateConnCoords(srcRoom.toMatrix(), roomTypeSrc);
        Point pointDest = calculateConnCoords(destRoom.toMatrix(), roomTypeDest);

        router.postEvent(new RequestConnection(null, -1, srcRoom, destRoom, pointSrc, pointDest));
    }

    private Point calculateConnCoords(int mat[][], RoomType roomType){
        Point point = null;

        //Create point at the first suitable location
        switch(roomType) {
            case Original:
                for (int row = mat.length - 1; row >= 0; row--) {
                    for (int col = mat[row].length - 1; col >= 0; col--) {
                        if (mat[row][col] == 0 && (row == 0 || col == 0 || row == mat.length - 1 || col == mat[row].length - 1)){
                            point = new Point(col, row);
                            break;
                        }
                    }
                    if (point != null) {
                        break;
                    }
                }
                break;
            case Scaled:
                for (int row = 0; row < mat.length; row++) {
                    for (int col = 0; col < mat[row].length; col++) {
                        if (mat[row][col] == 0 && (row == 0 || col == 0 || row == mat.length - 1 || col == mat[row].length - 1)) {
                            point = new Point(col, row);
                            break;
                        }
                    }
                    if (point != null) {
                        break;
                    }
                }
                break;
            case EaScaled:
                for (int col = 0; col < mat[0].length; col++) {
                    for (int row = 0; row < mat.length; row++) {
                        if (mat[row][col] == 4) {
                            point = new Point(col, row);
                            break;
                        }
                    }
                    if (point != null) {
                        break;
                    }
                }
                break;
            default:
                break;
        }

        return point;
    }

    public HashMap<GADimension.DimensionTypes, Double> getPreservedDimValues(){
        return preserveDimValues;
    }

    public MAPEDimensionFXML[] getMAPEDimensions(){
        MAPEDimensionFXML[] mapeDimensionFXMLS = new MAPEDimensionFXML[preserveDimValues.size()];
        DimensionTypes[] dimensionTypes = preserveDimValues.keySet().toArray(new DimensionTypes[preserveDimValues.size()]);
        MAPEDimensionFXML mapeDimensionFXML = null;
        int counter = 0;

        for (DimensionTypes dimType: dimensionTypes) {
            mapeDimensionFXML = new MAPEDimensionFXML(dimType, granularity);
            mapeDimensionFXMLS[counter] = mapeDimensionFXML;
            counter++;
        }
        System.out.println("Dimlength: " + mapeDimensionFXMLS.length);
        return mapeDimensionFXMLS;
    }

    public int[][] getScaledEaMatrix(){
        return eaMatrix;
    }

    public void setScaleEaMatrix(int[][] eaMatrix){
        this.eaMatrix = eaMatrix;
    }

    public Room getScaledRoom(){
        return scaledRoom;
    }

    public void setScaledRoom(Room scaledRoom){
        this.scaledRoom = scaledRoom;
    }

    public Room getEaScaledRoom(){
        return scaledEaRoom;
    }

    public void setScaledEaRoom(Room scaledEaRoom){
        this.scaledEaRoom = scaledEaRoom;
    }
}
