package util.algorithms;

import util.Point;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//Nearest-neighbour interpolation in the context of scaling maps
public class NearestNeighbour extends ScaleMatrix{
    private int[][] matrix;
    private int[][] scaledMatrix;
    private int scaleFactor;
    private static int doorVal = 4;
    private static int floorVal = 0;
    private static int charachterVal = 6;

    public NearestNeighbour(int[][] matrix, int scaleFactor){
        this.matrix = matrix;
        this.scaleFactor = scaleFactor;

        System.out.println("Original Matrix: " + Arrays.deepToString(matrix));
    }

    //To upscale, have to be integer
    @Override
    public int[][] Upscale()
    {
        int newRows = matrix.length * scaleFactor;
        int newCols = matrix[0].length * scaleFactor;
        scaledMatrix = new int[newRows][newCols];

        int[][] doorCoords = new int[newRows][newCols];

        for(int r = 0; r < newRows; r++)
        {
            for(int c = 0; c < newCols; c++)
            {
                if(matrix[r/scaleFactor][c/scaleFactor] == doorVal){
                    doorCoords[r][c] = doorVal;
                    scaledMatrix[r][c] = floorVal;
                }else if(matrix[r/scaleFactor][c/scaleFactor] == charachterVal){
                    scaledMatrix[r][c] = floorVal;
                } else{
                    scaledMatrix[r][c] = matrix[r/scaleFactor][c/scaleFactor];
                }
            }
        }

        for(int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                if(doorCoords[r][c] == doorVal && (r == 0 || c == 0 || r == scaledMatrix.length - 1 || c == scaledMatrix[r].length - 1)){
                    try{
                        if(doorCoords[r-1][c] != doorVal||doorCoords[r][c-1] != doorVal){
                            scaledMatrix[r][c] = doorVal;
                        }
                    }catch (ArrayIndexOutOfBoundsException exception){
                        System.out.println(exception + " OK");
                    }
                }
            }
        }

        return scaledMatrix;
    }

    //Downscale and use the most common out of four adjacent elements when representing
    @Override
    public int[][] Downscale() {
        int newRows = matrix.length / scaleFactor;
        int newCols = matrix[0].length / scaleFactor;
        int [][] doorCoords = new int[matrix.length][matrix[0].length];
        int xDoor = 0;
        int yDoor = 0;
        scaledMatrix = new int[newRows][newCols];

        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                Map<Integer, Integer> freqMap = new HashMap<>();
                for (int x = r * scaleFactor; x < (r + 1) * scaleFactor; x++) {
                    for (int y = c * scaleFactor; y < (c + 1) * scaleFactor; y++) {
                        int value = matrix[x][y];
                        if(matrix[x][y]== doorVal){
                            doorCoords[x/scaleFactor][y/scaleFactor] = doorVal;
                            xDoor = x/scaleFactor;
                            yDoor = y/scaleFactor;
                        }
                        if (freqMap.containsKey(value)) {
                            freqMap.put(value, freqMap.get(value) + 1);
                        } else {
                            freqMap.put(value, 1);
                        }
                    }
                }
                int maxFreq = 0;
                int mostFreqValue = 0;
                for (Map.Entry<Integer, Integer> entry : freqMap.entrySet()) {
                    if (entry.getValue() > maxFreq) {
                        maxFreq = entry.getValue();
                        mostFreqValue = entry.getKey();
                    }
                }
                scaledMatrix[r][c] = mostFreqValue;
            }
        }

        scaledMatrix[xDoor][yDoor] = doorVal;

        return scaledMatrix;
    }

    public int[][] getScaledMatrix(){
        return scaledMatrix;
    }

}
