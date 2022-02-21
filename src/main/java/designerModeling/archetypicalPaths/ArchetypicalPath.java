package designerModeling.archetypicalPaths;

import generator.algorithm.MAPElites.Dimensions.*;

import java.util.ArrayList;

class SpecialPath
{
    public int step;
    public SpecialPath parent;
    public ArrayList<SpecialPath> children;
    public boolean branch;

    public SpecialPath(SpecialPath parent, int step, boolean branch, boolean auto_child)
    {
        this.parent = parent;
        this.step = step;
        this.branch = branch;
        this.children = new ArrayList<SpecialPath>();

        if(auto_child)
            this.parent.addChild(this);
    }

    public void setParent(SpecialPath parent, boolean auto_child)
    {
        this.parent = parent;

        if(auto_child)
            this.parent.addChild(this);
    }

    public void addChild(SpecialPath child)
    {
        children.add(child);
    }

    public SpecialPath traverse(int next_step)
    {
        SpecialPath next_node = null;

        for(SpecialPath child : children)
        {
            if(next_step == child.step)
            {
                next_node = child;
                break;
            }
        }

        return next_node;
    }
}

class SubsetPath{
    public int start_pos;

    public int steps_uncounted = 0;
    public int length;
    public int real_length;
    public int branch_length;
    public ArrayList<Integer> path;
    public float path_percentage;
    private int matching_steps;

    public boolean reached_branch = false;

    public SubsetPath(int start_pos, int length, int real_length, ArrayList<Integer> subsetpath)
    {
        this.start_pos = start_pos;
        this.length = length;
        this.real_length = real_length;
        this.path = subsetpath;

        this.matching_steps = 0;
    }

    public void calculatePathPercentage(int standardStepsSize)
    {
        setPathPercentage(this.reached_branch ? this.matching_steps/(float)this.real_length :
                this.matching_steps/(float)standardStepsSize);
    }

    public void setMatchingSteps(int matching_step)
    {
        this.matching_steps = matching_step;
        this.real_length = this.steps_uncounted + matching_step;
        if(this.reached_branch)
        {
            this.branch_length = matching_step - this.length;
        }
        else
        {
            this.length = matching_step;
        }
    }

    public void setReachedBranch(boolean reached_branch)
    {
        this.reached_branch = reached_branch;
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
                ArchitecturalFocus.createTree();
                return ArchitecturalFocus.checkMatchingPath(path_to_test).path_percentage;
            case GOAL_ORIENTED:
                GoalOriented.createTree();
                return GoalOriented.checkMatchingPath(path_to_test).path_percentage;
            case SPLIT_CENTRAL_FOCUS:
                SplitCentralFocus.createTree();
                return SplitCentralFocus.checkMatchingPath(path_to_test).path_percentage;
            case COMPLEX_BALANCE:
                ComplexBalance.createTree();
                return ComplexBalance.checkMatchingPath(path_to_test).path_percentage;
            case NULL:
                ArchitecturalFocus.createTree();
                return ArchitecturalFocus.checkMatchingPath(path_to_test).path_percentage;
            default:
                return -1.0f;
        }
    }
//
//    public static SubsetPath calculatePath(ArchetypicalPathTypes archetypical_path, ArrayList<Integer> path_to_test)
//    {
//        switch(archetypical_path) {
//            case ARCHITECTURAL_FOCUS:
//                return ArchitecturalFocus.checkMatchingPath(path_to_test);
//            case GOAL_ORIENTED:
//                return GoalOriented.checkMatchingPath(path_to_test);
//            case SPLIT_CENTRAL_FOCUS:
//                return SplitCentralFocus.checkMatchingPath(path_to_test);
//            case COMPLEX_BALANCE:
//                return ComplexBalance.checkMatchingPath(path_to_test);
//            case NULL:
//                return ArchitecturalFocus.checkMatchingPath(path_to_test);
//            default:
//                return null;
//        }
//    }

}
