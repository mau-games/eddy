package generator.algorithm;

import game.Room;
import generator.algorithm.MAPElites.GADimensionsGranularity;

public class MetricExampleRooms
{
    public double metric_value;
    public GADimensionsGranularity granularity_value;
    public Room room;
    public boolean positive = true;

    public MetricExampleRooms(Room room, GADimensionsGranularity granularity, double metric_value)
    {
        this.room = room;
        this.metric_value = metric_value;
        this.granularity_value = granularity;
    }

    public MetricExampleRooms(Room room, GADimensionsGranularity granularity, double metric_value, boolean positive)
    {
        this.room = room;
        this.metric_value = metric_value;
        this.granularity_value = granularity;
        this.positive  = positive;
    }

    public void setPositive(Boolean positive){this.positive = positive;}

}

