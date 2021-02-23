package game.narrative;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class NarrativeShapeEdgeLine extends Line
{
	
	public NarrativeShapeEdgeLine(DoubleProperty startX, DoubleProperty startY, DoubleProperty endX, DoubleProperty endY)
	{
		startXProperty().bind(startX);
		startYProperty().bind(startY);
		endXProperty().bind(endX);
		endYProperty().bind(endY);
		
		setStrokeWidth(2);
		setStroke(Color.PINK);
//		setStrokeLineCap(StrokeLineCap.BUTT);
//		getStrokeDashArray().setAll(10.0, 5.0);
		setMouseTransparent(false); //--> If you dont want to be able to "touch"
		setCursor(Cursor.CROSSHAIR);
		
		
	}

}
