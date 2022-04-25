package designerModeling.archetypicalPaths;

import java.util.ArrayList;

public class SubsetPath{
    public int start_pos;

    public int steps_uncounted = 0;
    public int real_final_steps = 0;
    public int path_steps = 0;

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
        //We actually have more path than what we matched, don't know if we should return like this.
        if(path.size() > this.matching_steps)
        {
            setPathPercentage(0.0f);
            return;
        }

        setPathPercentage(this.reached_branch ? this.matching_steps/(float)this.real_final_steps :
                this.matching_steps/(float)standardStepsSize);
    }

    public void setMatchingSteps(int matching_step)
    {
        this.matching_steps = matching_step;
        this.real_final_steps = this.steps_uncounted + matching_step;
        if(this.reached_branch)
        {
            this.branch_length = matching_step - this.path_steps;
        }
        else
        {
            this.path_steps = matching_step;
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