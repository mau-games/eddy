package game.narrative;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Rotate;

public class NarrativeShapeEdgeLine extends Group
{
	public Line line;
	private Polygon triangle;
	private DoubleBinding dx;
	private DoubleBinding dy;
	
	public NarrativeShapeEdgeLine(DoubleProperty startX, DoubleProperty startY, DoubleProperty endX, DoubleProperty endY, int connectionType)
	{
		line = new Line();

		line.startXProperty().bind(startX);
		line.startYProperty().bind(startY);
		line.endXProperty().bind(endX);
		line.endYProperty().bind(endY);

		line.setStrokeWidth(1);
		line.setStroke(Color.PINK);
		line.setStrokeLineCap(StrokeLineCap.ROUND);
//		setStrokeLineJoin(StrokeLineJoin.BEVEL);
//		getStrokeDashArray().setAll(10.0, 5.0);
		line.setMouseTransparent(false); //--> If you dont want to be able to "touch"
		setCursor(Cursor.CROSSHAIR);
		this.getChildren().add(line);

		//Non directional!
		if(connectionType != 0)
			createArrow();
	}

	private void createArrow(){

		dx = line.endXProperty().add(line.startXProperty().negate());
		dy = line.endYProperty().add(line.startYProperty().negate());
		triangle = new Polygon(0, 0, 0 - 16, 0 + 10, 0 - 16, 0 - 10);
		Rotate rotate = new Rotate(0,0,0,1,Rotate.Z_AXIS);
		triangle.getTransforms().add(rotate);

		dx.addListener((observable, oldValue, newValue) -> {
			rotate.setAngle(getAngle(dy.doubleValue(), newValue.doubleValue()));
		});
		dy.addListener((observable, oldValue, newValue) -> {
			rotate.setAngle(getAngle(newValue.doubleValue(), dx.doubleValue()));
		});

//		rotate.setAngle(getAngle(dy.doubleValue(), dx.doubleValue()));
//		triangle.getTransforms().add(rotate);
//		triangle.relocate(line.endXProperty().get(), line.endYProperty().get() );

		triangle.layoutXProperty().bind(line.endXProperty());
		triangle.layoutYProperty().bind(line.endYProperty());

//		triangle.layoutXProperty().bind(Bindings.subtract(triangle.getLayoutBounds().getMinX(), line.endXProperty()));
//		triangle.layoutYProperty().bind(Bindings.subtract(triangle.getLayoutBounds().getMinY(), line.endYProperty()));
		this.getChildren().add(triangle);
//		this.getChildren().add(triangle);

		rotate.setAngle(getAngle(dy.doubleValue(), dx.doubleValue()));

	}

	private double getAngle(double dy ,double dx){
		return Math.toDegrees(Math.atan2(dy, dx));
	}

}
