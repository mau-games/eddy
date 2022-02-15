package designerModeling;

import designerModeling.archetypicalPaths.ArchetypicalPath;
import designerModeling.archetypicalPaths.ArchitecturalFocus;
import game.Room;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.RoomEdited;
import util.eventrouting.events.despers.DesPersEvaluation;
import util.eventrouting.events.despers.RoomStyleEvaluated;

import java.util.ArrayList;
import java.util.UUID;

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
        key_path = new ArrayList<Integer>();
    }

    private void debugRoomPersona()
    {
        System.out.println("-----------");
        System.out.println("path: " + path);
        System.out.println("reduced path: " + reduced_path);
        System.out.println("key path: " + key_path);

        System.out.println("architectural_focus: " + persona_percentages[0]);
        System.out.println("goal oriented: " + persona_percentages[1]);
        System.out.println("split central focus: " + persona_percentages[2]);
        System.out.println("complex balance: " + persona_percentages[3]);
        System.out.println("null: " + persona_percentages[4]);


        System.out.println("-----------");

    }

    public void addToPath(int step)
    {
        path.add(step);
        transformPath();
        checkPersona();
        debugRoomPersona();
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
                key_path.add(path.get(i));
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
        persona_percentages[0] = ArchetypicalPath.calculatePath(ArchetypicalPath.ArchetypicalPathTypes.ARCHITECTURAL_FOCUS, key_path);
        persona_percentages[1] = ArchetypicalPath.calculatePath(ArchetypicalPath.ArchetypicalPathTypes.GOAL_ORIENTED, key_path);
        persona_percentages[2] = ArchetypicalPath.calculatePath(ArchetypicalPath.ArchetypicalPathTypes.SPLIT_CENTRAL_FOCUS, key_path);
        persona_percentages[3] = ArchetypicalPath.calculatePath(ArchetypicalPath.ArchetypicalPathTypes.COMPLEX_BALANCE, key_path);

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

    public ArrayList<Integer> getFullSteps()
    {
        return path;
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
    private RoomDesignerPersona current_room_persona;

    public DesignerPersona()
    {
        target_persona = ArchetypicalPath.ArchetypicalPathTypes.NULL;

        roomPersonas = new ArrayList<RoomDesignerPersona>();
        active_persona = ArchetypicalPath.ArchetypicalPathTypes.NULL;
        activate_path = new ArrayList<Integer>();
        active_reduced_path = new ArrayList<Integer>();

        EventRouter.getInstance().registerListener(this, new RoomEdited(null));
        EventRouter.getInstance().registerListener(this, new RoomStyleEvaluated(0, null));
    }

    public void designStarted(Room room)
    {
        boolean exists = false;

        for(RoomDesignerPersona rdp : roomPersonas)
        {
            if(rdp.room.specificID.equals(room.specificID))
            {
                exists = true;
                current_room_persona = rdp;
            }
        }

        if(!exists)
        {
            RoomDesignerPersona rdp = new RoomDesignerPersona(room);
            roomPersonas.add(rdp);
            current_room_persona = rdp;

        }
    }

    private void styleAdded(int style, UUID id)
    {
        for(RoomDesignerPersona rdp : roomPersonas)
        {
            if(rdp.room.specificID.equals(id))
            {
                rdp.addToPath(style);
            }
        }
    }

    @Override
    public void ping(PCGEvent e)
    {
        if(e instanceof RoomEdited)
        {
            designStarted(((RoomEdited) e).editedRoom);
        }
        else if(e instanceof DesPersEvaluation)
        {

        }
        else if(e instanceof RoomStyleEvaluated)
        {
            styleAdded(((RoomStyleEvaluated) e).room_style, ((RoomStyleEvaluated) e).room_id);
        }
    }

    public void transformPath()
    {
        //Add code to transform "Path" in to "reduced Path"
    }


}
