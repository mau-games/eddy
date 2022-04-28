package designerModeling;

import designerModeling.archetypicalPaths.*;
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
    SubsetPath[] persona_percentages = new SubsetPath[5];

    SubsetPath current_persona = new SubsetPath(-1, -1, -1, null);

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

        System.out.println("architectural_focus: " + persona_percentages[0].path_percentage);
        System.out.println("goal oriented: " + persona_percentages[1].path_percentage);
        System.out.println("split central focus: " + persona_percentages[2].path_percentage);
        System.out.println("complex balance: " + persona_percentages[3].path_percentage);
        System.out.println("null: " + persona_percentages[4].path_percentage);


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
        persona_percentages[4] = null;

        float null_sum = 0.0f;
//        boolean not_null = false;

        for(SubsetPath persona_percentage : persona_percentages)
        {
            if(persona_percentage == null)
                continue;


//            if(persona_percentage == 1.0f)
//            {
//                not_null = true;
//                break;
//            }

            null_sum += persona_percentage.path_percentage;
        }

        persona_percentages[4] = new SubsetPath(-1, -1, -1, null);
        persona_percentages[4].path_percentage = (1.0f - null_sum/4.0f);


        //Here now we decide which persona is the current one!
        //all the following code is to know which one we are!
        int index_persona = -1;
        SubsetPath sp_persona = null;
        float max_perc = -10000f;
        int index = 0;

        for(int i = 0; i < 4; i++) //4 because we dont care about the null one! that is for us!
        {
            if(persona_percentages[i].path_percentage > max_perc)
            {
                sp_persona = persona_percentages[i];
                max_perc = persona_percentages[i].path_percentage;
                index_persona = i;
            }
        }

        current_persona = sp_persona;

        switch(index_persona)
        {
            case 0:
                persona = ArchetypicalPath.ArchetypicalPathTypes.ARCHITECTURAL_FOCUS;
                break;
            case 1:
                persona = ArchetypicalPath.ArchetypicalPathTypes.GOAL_ORIENTED;
                break;
            case 2:
                persona = ArchetypicalPath.ArchetypicalPathTypes.SPLIT_CENTRAL_FOCUS;
                break;
            case 3:
                persona = ArchetypicalPath.ArchetypicalPathTypes.COMPLEX_BALANCE;
                break;
            default:
                persona = ArchetypicalPath.ArchetypicalPathTypes.ARCHITECTURAL_FOCUS;
                break;
        }
    }

//    public void checkPersona()
//    {
//        persona_percentages[0] = ArchetypicalPath.calculatePath(ArchetypicalPath.ArchetypicalPathTypes.ARCHITECTURAL_FOCUS, key_path);
//        persona_percentages[1] = ArchetypicalPath.calculatePath(ArchetypicalPath.ArchetypicalPathTypes.GOAL_ORIENTED, key_path);
//        persona_percentages[2] = ArchetypicalPath.calculatePath(ArchetypicalPath.ArchetypicalPathTypes.SPLIT_CENTRAL_FOCUS, key_path);
//        persona_percentages[3] = ArchetypicalPath.calculatePath(ArchetypicalPath.ArchetypicalPathTypes.COMPLEX_BALANCE, key_path);
//
//        float null_sum = 0.0f;
////        boolean not_null = false;
//
//        for(float persona_percentage : persona_percentages)
//        {
////            if(persona_percentage == 1.0f)
////            {
////                not_null = true;
////                break;
////            }
//
//            null_sum += persona_percentage;
//        }
//
//        persona_percentages[4] = null_sum/4.0f;
//
//
////        if(!not_null)
////        {
////            //This is the null, it needs to be in the case another persona is not
////            persona_percentages[4] = null_sum/4.0f;
////        }
////        else
//
//
////        persona_percentages[4] =
//    }

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

    public RoomDesignerPersona getRoomsDesignerPersona(Room currentRoom)
    {
        for(RoomDesignerPersona rdp : roomPersonas)
        {
            if(rdp.room.specificID.equals(currentRoom.specificID))
            {
                return rdp;
            }
        }

        return null;
    }

    /***
     * Return the subset path related to the persona that scored the highests!
     * @param currentRoom
     * @return
     */
    public SubsetPath currentPersona(Room currentRoom)
    {
        for(RoomDesignerPersona rdp : roomPersonas)
        {
            if(rdp.room.specificID.equals(currentRoom.specificID))
            {
                return rdp.current_persona;
            }
        }

        return null;
    }

    /***
     * Return all the persona calculations.
     * @param currentRoom
     * @return
     */
    public SubsetPath[] getAllPersonaCalculations(Room currentRoom)
    {
        for(RoomDesignerPersona rdp : roomPersonas)
        {
            if(rdp.room.specificID.equals(currentRoom.specificID))
            {
                return rdp.persona_percentages;
            }
        }

        return null;
    }

    /***
     *  Get is the final step that has been taken by the most correct persona!
     * @param currentRoom
     * @return
     */
    public int currentPersonaSpecificCluster(Room currentRoom)
    {
        ArchetypicalPath.ArchetypicalPathTypes current_persona = ArchetypicalPath.ArchetypicalPathTypes.NULL;

        SubsetPath currentPersonaPath = null;

        for(RoomDesignerPersona rdp : roomPersonas)
        {
            if(rdp.room.specificID.equals(currentRoom.specificID))
            {
                currentPersonaPath = rdp.current_persona;
            }
        }

        return currentPersonaPath.path.get(currentPersonaPath.path.size() - 1);
    }


    /***
     * Get the soecific archetypical path connected to the closest subsetpath
     * @param currentRoom
     * @return
     */
    public ArchetypicalPath.ArchetypicalPathTypes specificArchetypicalPath(Room currentRoom)
    {
        for(RoomDesignerPersona rdp : roomPersonas)
        {
            if(rdp.room.specificID.equals(currentRoom.specificID))
            {
                return rdp.persona;
            }
        }

        return null;
    }

    /***
     * Get the cluster associated with the current archetypical path!
     * The issue I see here, is that there are 3 that are simply 7! How to differentiate??
     * @param currentRoom
     * @return
     */
    public int specificArchetypicalPathCluster(Room currentRoom)
    {
        ArchetypicalPath.ArchetypicalPathTypes current_persona = ArchetypicalPath.ArchetypicalPathTypes.NULL;

        for(RoomDesignerPersona rdp : roomPersonas)
        {
            if(rdp.room.specificID.equals(currentRoom.specificID))
            {
                current_persona = rdp.persona;
            }
        }

        switch(current_persona) {
            case ARCHITECTURAL_FOCUS:
                return 3;
            case GOAL_ORIENTED:
                return 7;
            case SPLIT_CENTRAL_FOCUS:
                return 7;
            case COMPLEX_BALANCE:
                return 7;
            case NULL:
                return 7;
            default:
                return -1;
        }
    }

    public void transformPath()
    {
        //Add code to transform "Path" in to "reduced Path"
    }


}
