package designerModeling.archetypicalPaths;

import generator.algorithm.MAPElites.Dimensions.*;

import java.util.ArrayList;

public abstract class ArchetypicalPath
{
    public enum ArchetypicalPathTypes {
        ARCHITECTURAL_FOCUS,
        GOAL_ORIENTED,
        SPLIT_CENTRAL_FOCUS,
        COMPLEX_BALANCE,
        NULL
    }

    public ArchetypicalPathTypes archetype;
    public ArrayList<Integer> path;
    public ArrayList<Integer> branches;

    public ArchetypicalPath()
    {

    }

    public static float calculatePath(ArchetypicalPathTypes archetypical_path, ArrayList<Integer> path_to_test)
    {
        switch(archetypical_path) {
            case ARCHITECTURAL_FOCUS:
                return ArchitecturalFocus.checkMatchingPath(path_to_test);
            case GOAL_ORIENTED:
                return ArchitecturalFocus.checkMatchingPath(path_to_test);
            case SPLIT_CENTRAL_FOCUS:
                return ArchitecturalFocus.checkMatchingPath(path_to_test);
            case COMPLEX_BALANCE:
                return ArchitecturalFocus.checkMatchingPath(path_to_test);
            case NULL:
                return ArchitecturalFocus.checkMatchingPath(path_to_test);
            default:
                return -1.0f;
        }
    }

}
