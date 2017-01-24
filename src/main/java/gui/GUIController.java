package gui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * This class controls our fantastic GUI.
 * 
 * @author Johan Holmberg, Malmö University
 */
public class GUIController implements Initializable {
	@FXML private Text messageDisplayer;
	@FXML private Canvas mapCanvas;
	@FXML private Button runButton;
	
	/**
	 * Handles the run button's action events.
	 * 
	 * @param ev The action event that triggered this call. 
	 */
	@FXML
	protected void runButtonPressed(ActionEvent ev) {
		// TODO: Do something else with this later on...
		addMessage("New message");
		int[][] matrix = new int[3][3];
		matrix[0][0] = 0;
		matrix[0][1] = 1;
		matrix[0][2] = 2;
		matrix[1][0] = 3;
		matrix[1][1] = 4;
		matrix[1][2] = 5;
		matrix[2][0] = 6;
		matrix[2][1] = 7;
		matrix[2][2] = 8;
		drawMatrix(matrix);
	}
	
	/**
	 * Displays a message in the message console.
	 * 
	 * @param message The message to display
	 */
	private void addMessage(String message) {
		messageDisplayer.setText(messageDisplayer.getText() + "\n" + message);
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		messageDisplayer.setText("Awaiting commands");
	}
	
	/**
	 * Draws a matrix on the canvas.
	 * 
	 * @param matrix A quadratic matrix of integers. Each integer corresponds
	 * 		to some predefined colour.
	 */
	public void drawMatrix(int[][] matrix) {
		int m = matrix.length;
		int n = matrix[0].length;
		int pWidth = (int) Math.floor(mapCanvas.getWidth() / Math.max(m, n));
		GraphicsContext gc = mapCanvas.getGraphicsContext2D();
		
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				gc.setFill(getColour(matrix[i][j]));
				gc.fillRect(i * pWidth, j * pWidth, pWidth, pWidth);
			}
		}
	}
	
	/**
	 * Selects a colour based on the pixel's integer value.
	 * 
	 * @param pixel The pixel to select for.
	 * @return A selected colour code.
	 */
	private Color getColour(int pixel) {
		Color color = null;
		
		// TODO: Choose appropriate colours and stuff
		switch (pixel) {
		case 0:
			color = Color.ALICEBLUE;
			break;
		case 1:
			color = Color.ANTIQUEWHITE;
			break;
		case 2:
			color = Color.AQUA;
			break;
		case 3:
			color = Color.AQUAMARINE;
			break;
		case 4:
			color = Color.AZURE;
			break;
		case 5:
			color = Color.BEIGE;
			break;
		case 6:
			color = Color.BISQUE;
			break;
		case 7:
			color = Color.BLANCHEDALMOND;
			break;
		default:
			color = Color.BLACK;
		}
		
		return color;
	}
}
