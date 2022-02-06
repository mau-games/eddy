package designerModeling;

import designerModeling.archetypicalPaths.ArchetypicalPath;
import designerModeling.archetypicalPaths.ArchitecturalFocus;
import game.Room;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.RoomEdited;
import util.eventrouting.events.despers.DesPersEvaluation;

import java.util.ArrayList;

class RoomDesignerPersona
{
    //Basic information we need
    public Room room;
    public ArrayList<Integer> path;
    public ArrayList<Integer> reduced_path;
    public ArrayList<Integer> key_path;

    public ArchetypicalPath.ArchetypicalPathTypes persona;
    float[] persona_percentages = new float[5];

    public RoomDesignerPersona(Room room)
    {
        this.room = room;
        persona = ArchetypicalPath.ArchetypicalPathTypes.NULL;
        path = new ArrayList<Integer>();
        reduced_path = new ArrayList<Integer>();
    }

    public void addToPath(int step)
    {
        path.add(step);
    }

    public void transformPath()
    {
        //Add code to transform "Path" in to "reduced Path"
        reduced_path.clear();
        key_path = new ArrayList<Integer>();

        //We add the first step (makes sense).
        reduced_path.add(path.get(0));
        key_path.add(path.get(0));

        int threshold = 4; //indicated threshold in the paper
        int step_counter = 1; //how many steps have we gone
        int cl_size = 0; //cluster size
        boolean initial = true;

        for(int i = 1; i < path.size(); i++)
        {
            if(!path.get(i).equals(path.get(i - 1)))
            {
                reduced_path.add(path.get(i));
                cl_size = key_path.size();

                if(!initial && (step_counter < threshold || (cl_size > 1 && key_path.get(cl_size - 1).equals(key_path.get(cl_size - 2)))))
                    key_path.remove(key_path.size() - 1);
                else
                    initial = false;

                step_counter = 1;
                reduced_path.add(path.get(i));
            }
            else
            {
                step_counter += 1;
            }
        }

        cl_size = key_path.size();
        if (cl_size > 1 && key_path.get(cl_size - 1).equals(key_path.get(cl_size - 2)))
            key_path.remove(key_path.size() - 1);
    }

    public void checkPersona()
    {
        persona_percentages[0] = ArchetypicalPath.calculatePath(ArchetypicalPath.ArchetypicalPathTypes.ARCHITECTURAL_FOCUS, reduced_path);
        persona_percentages[1] = ArchetypicalPath.calculatePath(ArchetypicalPath.ArchetypicalPathTypes.GOAL_ORIENTED, reduced_path);
        persona_percentages[2] = ArchetypicalPath.calculatePath(ArchetypicalPath.ArchetypicalPathTypes.SPLIT_CENTRAL_FOCUS, reduced_path);
        persona_percentages[3] = ArchetypicalPath.calculatePath(ArchetypicalPath.ArchetypicalPathTypes.COMPLEX_BALANCE, reduced_path);

        float null_sum = 0.0f;
//        boolean not_null = false;

        for(float persona_percentage : persona_percentages)
        {
//            if(persona_percentage == 1.0f)
//            {
//                not_null = true;
//                break;
//            }

            null_sum += persona_percentage;
        }

        persona_percentages[4] = null_sum/4.0f;


//        if(!not_null)
//        {
//            //This is the null, it needs to be in the case another persona is not
//            persona_percentages[4] = null_sum/4.0f;
//        }
//        else


//        persona_percentages[4] =
    }
}

/***
 * I think designer persona, could be a static class? Where I could query for...
 *
 * Maybe actually, it is better that Designer Persona is a class to be instantiated by a class "Designer Model".
 * That class would then contain other models such as the preference model as well! Then this class (despers)
 * could be set to a specific target (Some persona to achieve) or "calculating" online.
 *
 * This class contains a set of rooms
 */
public class DesignerPersona implements Listener
{
    //In case we would like to have a specific type of persona as target!
    public ArchetypicalPath.ArchetypicalPathTypes target_persona;

    public ArchetypicalPath.ArchetypicalPathTypes active_persona;
    public ArrayList<Integer> activate_path;
    public ArrayList<Integer> active_reduced_path;

    /**
     * This list contains all the rooms in a session, and here we calculate their
     */
    public ArrayList<RoomDesignerPersona> roomPersonas;

    public DesignerPersona()
    {
        target_persona = ArchetypicalPath.ArchetypicalPathTypes.NULL;

        roomPersonas = new ArrayList<RoomDesignerPersona>();
        active_persona = ArchetypicalPath.ArchetypicalPathTypes.NULL;
        activate_path = new ArrayList<Integer>();
        active_reduced_path = new ArrayList<Integer>();

    }

    @Override
    public void ping(PCGEvent e)
    {
        if(e instanceof RoomEdited)
        {

        }
        else if(e instanceof DesPersEvaluation)
        {

        }
    }

    public void transformPath()
    {
        //Add code to transform "Path" in to "reduced Path"
    }


}
