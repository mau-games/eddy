package util.algorithms;

import util.Point;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//Nearest-neighbour interpolation in the context of scaling maps
public class NearestNeighbour extends ScaleMatrix{
    private int[][] matrix;
    private int[][] scaledMatrix;
    int [][] doorCoords;
    private int scaleFactor;
    private static int doorVal = 4;

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
        doorCoords = new int[newRows][newCols];

        for(int r = 0; r < newRows; r++)
        {
            for(int c = 0; c < newCols; c++)
            {
                if(matrix[r/scaleFactor][c/scaleFactor] == doorVal){
                    doorCoords[r][c] = doorVal;
                    //scaledMatrix[r][c] = floorVal;
                } else{
                    scaledMatrix[r][c] = matrix[r/scaleFactor][c/scaleFactor];
                }
            }
        }

        int doorDelayCounter = 0;

        for(int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                if(doorCoords[r][c] == doorVal && (r == 0 || c == 0 || r == scaledMatrix.length - 1 || c == scaledMatrix[r].length - 1)){
                    if(doorDelayCounter > 1 && doorDelayCounter != scaledMatrix[0].length){
                        System.out.println("Door delay counter: " + doorDelayCounter + " Scaled Matrix length: " + scaledMatrix[0].length + " Rows: " + r + " Col: " + c + '\n');
                        scaledMatrix[r][c] = doorVal;
                        doorDelayCounter = 0;
                    }
                }
                doorDelayCounter++;
            }
        }

        return scaledMatrix;
    }

    //Downscale and use the most common out of four adjacent elements when representing
    @Override
    public int[][] Downscale() {
        int newRows = matrix.length / scaleFactor;
        int newCols = matrix[0].length / scaleFactor;
        doorCoords = new int[matrix.length][matrix[0].length];
        scaledMatrix = new int[newRows][newCols];

        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                Map<Integer, Integer> freqMap = new HashMap<>();
                for (int x = r * scaleFactor; x < (r + 1) * scaleFactor; x++) {
                    for (int y = c * scaleFactor; y < (c + 1) * scaleFactor; y++) {
                        int value = matrix[x][y];
                        if(matrix[x][y] == doorVal){
                            doorCoords[x/scaleFactor][y/scaleFactor] = doorVal;
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

        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                if(doorCoords[r][c] == doorVal)
                    scaledMatrix[r][c] = doorVal;
            }
        }

        return scaledMatrix;
    }

    public int[][] getScaledMatrix(){
        return scaledMatrix;
    }

}
