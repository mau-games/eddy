package util.algorithms;

import java.util.HashMap;
import java.util.Map;

//Fibonacci sequence in the context of scaling maps
public class ScaleFibonacci extends ScaleMatrix{
    private int[][] matrix;
    private int[][] scaledMatrix;
    private double scaleFactor;
    //Replace doors with floor when upscaling
    private static int doorVal = 4;
    private static int floorVal = 0;
    private static int charachterVal = 6;

    public ScaleFibonacci(int[][] matrix, double scaleFactor){
        this.matrix = matrix;
        this.scaleFactor = scaleFactor;
    }

    @Override
    public int[][] Upscale()
    {
        int newRows = (int)(matrix.length * scaleFactor);
        int newCols = (int)(matrix[0].length * scaleFactor);
        scaledMatrix = new int[newRows][newCols];

        int[][] doorCoords = new int[newRows][newCols];

        for(int r = 0; r < newRows; r++)
        {
            for(int c = 0; c < newCols; c++)
            {
                if(matrix[(int)(r/scaleFactor)][(int)(c/scaleFactor)] == doorVal){
                    doorCoords[r][c] = doorVal;
                    scaledMatrix[r][c] = floorVal;
                } else if (matrix[(int)(r/scaleFactor)][(int)(c/scaleFactor)] == charachterVal) {
                    scaledMatrix[r][c] = floorVal;
                } else{
                    scaledMatrix[r][c] = matrix[(int)(r/scaleFactor)][(int)(c/scaleFactor)];
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

    @Override
    public int[][] Downscale(){
        int newRows = (int)(matrix.length / scaleFactor);
        int newCols = (int)(matrix[0].length / scaleFactor);
        scaledMatrix = new int[newRows][newCols];
        int[][] doorCoords = new int[newRows][newCols];
        boolean ok = true;

        for (int r = 0; r < newRows; r++) {
            for (int c = 0; c < newCols; c++) {
                Map<Integer, Integer> freqMap = new HashMap<>();
                for (int x = (int)(r * scaleFactor); x < (r + 1) * scaleFactor; x++) {
                    for (int y = (int)(c * scaleFactor); y < (c + 1) * scaleFactor; y++) {
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
                    if ((entry.getValue() > maxFreq || (entry.getKey() == doorVal && ok))) {
                        maxFreq = entry.getValue();
                        mostFreqValue = entry.getKey();

                        if(entry.getKey() == doorVal){
                            ok = false;
                        }
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
