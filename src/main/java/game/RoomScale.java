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
    private Room initRoom;
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
    public enum ConnectionType{
        UpAtFloor,
        DownAtFloor,
        DoorToDoor
    }

    private ScaleType scaleType;
    private SizeAdjustType sizeAdjustType;
    private double scaleFactor;
    private HashMap<DimensionTypes, Double> preserveDimValues;
    private MAPEDimensionFXML[] mapeDimensionFXMLS;

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

        for(int i = 0; i<preserveDimArr.length; i++){
            switch (preserveDimArr[i]){
                case "Difficulty":
                    preserveDimValues.put(DimensionTypes.LENIENCY, origRoom.getDimensionValue(DimensionTypes.LENIENCY));
                    break;
                case "Inner-similarity":
                    preserveDimValues.put(DimensionTypes.INNER_SIMILARITY, origRoom.getDimensionValue(DimensionTypes.INNER_SIMILARITY));
                    break;
                case "Symmetry":
                    preserveDimValues.put(DimensionTypes.SYMMETRY, origRoom.getDimensionValue(DimensionTypes.SYMMETRY));
                    break;
                case "Number of meso-patterns":
                    preserveDimValues.put(DimensionTypes.NUMBER_MESO_PATTERN, origRoom.getDimensionValue(DimensionTypes.NUMBER_MESO_PATTERN));
                    break;
                case "Number of patterns":
                    preserveDimValues.put(DimensionTypes.NUMBER_PATTERNS, origRoom.getDimensionValue(DimensionTypes.NUMBER_PATTERNS));
                    break;
                case "Linearity":
                    preserveDimValues.put(DimensionTypes.LINEARITY, origRoom.getDimensionValue(DimensionTypes.LINEARITY));
                    break;
                case "All":
                    setAllDimTypes();
                    System.out.println("ALL DIMTYPES SET");
                    return;
                default:
                    break;
            }
        }
        if(preserveDimValues.isEmpty()){
            router.unregisterListener(this, new MAPElitesDone());
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

    public int[][] calculateScaledMatrix(Room room){
        int[][] matrix;
        ScaleMatrix scaleMatrix = null;
        switch (scaleType){
            case NearestNeighbour:
                scaleMatrix = new NearestNeighbour(room.toMatrix(), (int)scaleFactor);
                break;
            case Fibonacci:
                scaleMatrix = new ScaleFibonacci(room.toMatrix(), scaleFactor);
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
            Room currTopRoom = null;
            double topDimsDiff = Double.MAX_VALUE;
            int counter = 0;

            for (Room room : generatedRooms) {
                if (room != null) {
                    double dimsDiff = 0.0;
                    double simDiff;
                    counter++;
                    room.calculateAllDimensionalValues();

                    try{
                        if(sizeAdjustType == SizeAdjustType.Upscale){
                            room.setSpeficidDimensionValue(DimensionTypes.SIMILARITY, SimilarityGADimension.calculateValueIndependently(origRoom, room));
                        }
                        else{
                            room.setSpeficidDimensionValue(DimensionTypes.SIMILARITY, SimilarityGADimension.calculateValueIndependently(room, origRoom));
                        }
                        simDiff = -0.5 * room.getDimensionValue(DimensionTypes.SIMILARITY);
                        dimsDiff += simDiff;
                        simDiff = 0.0;
                    }
                    catch (ArrayIndexOutOfBoundsException exception){
                        System.out.println(exception);
                    }

                    for (DimensionTypes dimType : preserveDimValues.keySet()) {
                        if(dimType == DimensionTypes.INNER_SIMILARITY){
                            try{
                                if(sizeAdjustType == SizeAdjustType.Upscale){
                                    room.setSpeficidDimensionValue(DimensionTypes.INNER_SIMILARITY, CharacteristicSimilarityGADimension.calculateValueIndependently(origRoom, room));
                                }
                                else{
                                    room.setSpeficidDimensionValue(DimensionTypes.INNER_SIMILARITY, CharacteristicSimilarityGADimension.calculateValueIndependently(room, origRoom));
                                }
                                simDiff = -0.5 * room.getDimensionValue(DimensionTypes.INNER_SIMILARITY);
                                dimsDiff += simDiff;
                                simDiff = 0.0;
                            }
                            catch (ArrayIndexOutOfBoundsException exception){
                                System.out.println(exception);
                            }
                        }
                        else{
                            double dimValue = room.getDimensionValue(dimType);
                            double targetValue = preserveDimValues.get(dimType);

                            dimsDiff += Math.abs(dimValue - targetValue);
                        }

                        System.out.println("Room " + counter + " - " + dimType + ": " + room.getDimensionValue(dimType));
                    }

                    System.out.println("Room " + counter + " - Similarity: " + room.getDimensionValue(DimensionTypes.SIMILARITY) + '\n' + "Room " + counter + "- dimDiff: " + dimsDiff + '\n');
                    if (dimsDiff < topDimsDiff) {
                        topDimsDiff = dimsDiff;
                        currTopRoom = room;
                    }
                }
            }

            scaledRoom.calculateAllDimensionalValues();

            if(sizeAdjustType == SizeAdjustType.Upscale){
                scaledRoom.setSpeficidDimensionValue(DimensionTypes.SIMILARITY, CharacteristicSimilarityGADimension.calculateValueIndependently(origRoom, scaledRoom));
            }
            else{
                scaledRoom.setSpeficidDimensionValue(DimensionTypes.SIMILARITY, CharacteristicSimilarityGADimension.calculateValueIndependently(scaledRoom, origRoom));
            }

            for (DimensionTypes dimType: preserveDimValues.keySet()) {
                System.out.println("VALUE FROM EVOLUTION ALGO - " + dimType.toString() + ": " + preserveDimValues.get(dimType));
                System.out.println("ORIGINAL VALUE - " + dimType.toString() + ": " + currTopRoom.getDimensionValue(dimType));
                System.out.println("SCALED VALUE - " + dimType.toString() + ": " + scaledRoom.getDimensionValue(dimType) + '\n');
            }
            System.out.println("TOP ROOM DIMDIFF: " + topDimsDiff + '\n' + "SIMILARITY BETWEEN ORIGINAL AND EA-SCALED: " + currTopRoom.getDimensionValue(DimensionTypes.SIMILARITY));
            System.out.println("SIMILARITY BETWEEN ORIGINAL AND SCALED: " + scaledRoom.getDimensionValue(DimensionTypes.SIMILARITY) + '\n');

            //To upscale after EA
            //int[][] eaScaledMat = calculateScaledMatrix(currTopRoom);

            //The EA-generated already upscaled matrix
            int[][] eaScaledMat = currTopRoom.toMatrix();

            Platform.runLater(()->{
                router.postEvent(new RequestMatrixScaledRoom(eaScaledMat, this, true));
                createConnection(initRoom, scaledEaRoom);
                router.postEvent(new Stop());

                ArrayList<Room> rooms = new ArrayList<Room>();
                rooms.add(scaledRoom);
                rooms.add(scaledEaRoom);
                router.postEvent(new RequestScaleView(rooms));
                router.unregisterListener(this, new MAPElitesDone());
            });
        }
    }

    //Bottom floor of a room to a door of another room
    public void createConnection(Room srcRoom, Room destRoom){
        Point srcPoint = calculateConnCoords(srcRoom.toMatrix(), ConnectionType.DownAtFloor);
        Point destPoint = calculateConnCoords(destRoom.toMatrix(), ConnectionType.DoorToDoor);
        router.postEvent(new RequestConnection(null, -1, srcRoom, destRoom, srcPoint, destPoint));
    }

    //Upper-left corner of a room to bottom-right corner of another
    public void createConnection(Room srcRoom, Room destRoom, ConnectionType roomType){
        ConnectionType roomTypeSrc = ConnectionType.UpAtFloor;
        ConnectionType roomTypeDest = ConnectionType.DownAtFloor;

        if(roomType != ConnectionType.DownAtFloor){
            roomTypeSrc = ConnectionType.UpAtFloor;
            roomTypeDest = ConnectionType.DoorToDoor;
        }
        Point pointSrc = calculateConnCoords(srcRoom.toMatrix(), roomTypeSrc);
        Point pointDest = calculateConnCoords(destRoom.toMatrix(), roomTypeDest);

        router.postEvent(new RequestConnection(null, -1, srcRoom, destRoom, pointSrc, pointDest));
    }

    private Point calculateConnCoords(int mat[][], ConnectionType roomType){
        Point point = null;

        //Create point at the first suitable location
        switch(roomType) {
            case DownAtFloor:
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
            case UpAtFloor:
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
            case DoorToDoor:
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

    public MAPEDimensionFXML[] calculateMAPEDimensions(){
        mapeDimensionFXMLS = new MAPEDimensionFXML[preserveDimValues.size()];
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

    public MAPEDimensionFXML[] calculateAllMAPEDimensions(){
        mapeDimensionFXMLS = new MAPEDimensionFXML[7];
        mapeDimensionFXMLS[0] = new MAPEDimensionFXML(DimensionTypes.SYMMETRY, granularity);
        mapeDimensionFXMLS[1] = new MAPEDimensionFXML(DimensionTypes.LENIENCY, granularity);
        mapeDimensionFXMLS[2] = new MAPEDimensionFXML(DimensionTypes.LINEARITY, granularity);
        mapeDimensionFXMLS[3] = new MAPEDimensionFXML(DimensionTypes.SIMILARITY, granularity);
        mapeDimensionFXMLS[4] = new MAPEDimensionFXML(DimensionTypes.INNER_SIMILARITY, granularity);
        mapeDimensionFXMLS[5] = new MAPEDimensionFXML(DimensionTypes.NUMBER_PATTERNS, granularity);
        mapeDimensionFXMLS[6] = new MAPEDimensionFXML(DimensionTypes.NUMBER_MESO_PATTERN, granularity);

        return mapeDimensionFXMLS;
    }

    public MAPEDimensionFXML[] getMAPEDimensions(){
        return mapeDimensionFXMLS;
    }

    public Room getOrigRoom(){
        return origRoom;
    }

    public void setOrigRoom(Room origRoom){
        this.origRoom = origRoom;
    }

    public Room getInitRoom(){
        return initRoom;
    }

    public void setInitRoom(Room initRoom) {
        this.initRoom = initRoom;
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
