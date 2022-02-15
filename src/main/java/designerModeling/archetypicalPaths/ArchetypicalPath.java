package designerModeling.archetypicalPaths;

import generator.algorithm.MAPElites.Dimensions.*;

import java.util.ArrayList;

class SubsetPath{
    public int start_pos;
    public int length;
    public int real_length;
    public ArrayList<Integer> path;
    public float path_percentage;

    public SubsetPath(int start_pos, int length, int real_length, ArrayList<Integer> subsetpath)
    {
        this.start_pos = start_pos;
        this.length = length;
        this.real_length = real_length;
        this.path = subsetpath;
    }

    public void setPathPercentage(float perc)
    {
        this.path_percentage = perc;
    }
}

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
                return GoalOriented.checkMatchingPath(path_to_test);
            case SPLIT_CENTRAL_FOCUS:
                return SplitCentralFocus.checkMatchingPath(path_to_test);
            case COMPLEX_BALANCE:
                return ComplexBalance.checkMatchingPath(path_to_test);
            case NULL:
                return ArchitecturalFocus.checkMatchingPath(path_to_test);
            default:
                return -1.0f;
        }
    }

}
