package designerModeling.designerPersona;

import java.util.ArrayList;

/***
 * I am thinking that room style is a class that each room has.
 *
 * - It keeps track of it's current style (queried to the Python code - scikitlearn)
 * - It also keeps track of previous
 */
public class RoomStyle {

    public int current_style;
    public ArrayList<Integer> previous_styles;

    public RoomStyle()
    {
        previous_styles = new ArrayList<Integer>();
    }

    /**
     * Probably here it will need to be calculated!
     * @return
     */
    public int getCurrentStyle()
    {
        return current_style;
    }

}
