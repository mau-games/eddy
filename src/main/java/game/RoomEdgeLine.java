package game;

import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class RoomEdgeLine extends Line
{
	public RoomEdgeLine(DoubleProperty startX, DoubleProperty startY, DoubleProperty endX, DoubleProperty endY)
	{
		startXProperty().bind(startX);
		startYProperty().bind(startY);
		endXProperty().bind(endX);
		endYProperty().bind(endY);
		
		setStrokeWidth(2);
		setStroke(Color.BLACK);
//		setStrokeLineCap(StrokeLineCap.BUTT);
//		getStrokeDashArray().setAll(10.0, 5.0);
		setMouseTransparent(true); //--> If you dont want to be able to "touch"
	}
}
