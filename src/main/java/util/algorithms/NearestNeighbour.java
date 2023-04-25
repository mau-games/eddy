package util.algorithms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//Nearest-neighbour interpolation in the context of scaling maps
public class NearestNeighbour extends ScaleMatrix{
    private int[][] matrix;
    private int[][] scaledMatrix;
    private int scaleFactor;
    //Replace doors and charachters with floor when upscaling


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
        int doorVal = 4;
        int floorVal = 0;
        int charachterVal = 6;
        int[][] doorCoords = new int[newRows][newCols];

        for(int r = 0; r < newRows; r++)
        {
            for(int c = 0; c < newCols; c++)
            {
                if(matrix[r/scaleFactor][c/scaleFactor] == doorVal){
                    doorCoords[r/scaleFactor][c/scaleFactor] = doorVal;
                    scaledMatrix[r][c] = floorVal;
                } else if (matrix[r/scaleFactor][c/scaleFactor] == charachterVal) {
                    scaledMatrix[r][c] = floorVal;
                } else{
                    scaledMatrix[r][c] = matrix[r/scaleFactor][c/scaleFactor];

                }
            }
        }

        /*for(int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                if(doorCoords[r][c]==doorVal){
                    scaledMatrix[r*scaleFactor][c*scaleFactor] = doorVal;
                }
            }
        }*/

        return scaledMatrix;
    }

    //Downscale and use the most common out of four adjacent elements when representing
    @Override
    public int[][] Downscale() {
        int newRows = matrix.length / scaleFactor;
        int newCols = matrix[0].length / scaleFactor;
        scaledMatrix = new int[newRows][newCols];

        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                Map<Integer, Integer> freqMap = new HashMap<>();
                for (int x = r * scaleFactor; x < (r + 1) * scaleFactor; x++) {
                    for (int y = c * scaleFactor; y < (c + 1) * scaleFactor; y++) {
                        int value = matrix[x][y];
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

        return scaledMatrix;
    }

    public int[][] getScaledMatrix(){
        return scaledMatrix;
    }

}
