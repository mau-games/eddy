package util.algorithms;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

//Fibonacci sequence in the context of scaling maps
public class ScaleFibonacci extends ScaleMatrix{
    private int[][] matrix;
    private int[][] scaledMatrix;
    private int[][] doorCoords;
    private double scaleFactor;
    private static int doorVal = 4;

    public ScaleFibonacci(int[][] matrix, double scaleFactor){
        this.matrix = matrix;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public int[][] Upscale()
    {
        int newRows = (int)Math.round(matrix.length * scaleFactor);
        int newCols = (int)Math.round(matrix[0].length * scaleFactor);
        scaledMatrix = new int[newRows][newCols];
        doorCoords = new int[newRows][newCols];

        for(int r = 0; r < newRows; r++)
        {
            for(int c = 0; c < newCols; c++)
            {
                if(matrix[(int)Math.round(r/scaleFactor)][(int)Math.round(c/scaleFactor)] == doorVal){
                    doorCoords[r][c] = doorVal;
                    //scaledMatrix[r][c] = floorVal;
                } else{
                    scaledMatrix[r][c] = matrix[(int)Math.round(r/scaleFactor)][(int)Math.round(c/scaleFactor)];
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

    @Override
    public int[][] Downscale(){
        int newRows = (int)Math.round(matrix.length / scaleFactor);
        int newCols = (int)Math.round(matrix[0].length / scaleFactor);
        doorCoords = new int[newRows][newCols];
        int xDoor = 0;
        int yDoor = 0;
        scaledMatrix = new int[newRows][newCols];

        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                Map<Integer, Integer> freqMap = new HashMap<>();
                for (int x = (int)Math.round(r * scaleFactor); x < Math.round((r + 1) * scaleFactor); x++) {
                    for (int y = (int)Math.round(c * scaleFactor); y < Math.round((c + 1) * scaleFactor); y++) {
                        int value = matrix[x][y];
                        if(matrix[x][y]== doorVal){
                            xDoor = (int)Math.round(x/scaleFactor);
                            yDoor = (int)Math.round(y/scaleFactor);
                            doorCoords[xDoor][yDoor] = doorVal;
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
