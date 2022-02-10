package designerModeling.designerPersona;

import game.Room;

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

    public void setCurrentStyle(int current_style)
    {
        this.current_style = current_style;
        previous_styles.add(current_style);
    }

    public void setCurrentStyle(Room room)
    {
        int style = 0;

        /**
         *   CODE TO CALCULATE CURRENT STYLE
          */



        current_style = style;
        previous_styles.add(current_style);
    }

}
