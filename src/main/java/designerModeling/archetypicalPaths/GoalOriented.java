package designerModeling.archetypicalPaths;

import java.util.ArrayList;
import java.util.Arrays;

public class GoalOriented extends ArchetypicalPath
{
    public static ArchetypicalPathTypes archetype = ArchetypicalPathTypes.GOAL_ORIENTED;
    public static ArrayList<Integer> path = new ArrayList<Integer>(Arrays.asList(0, 1, 7));
    public static ArrayList<Integer> branches = new ArrayList<Integer>(Arrays.asList(6, 9, 11));

    public GoalOriented()
    {
        archetype = ArchetypicalPathTypes.GOAL_ORIENTED;

        path = new ArrayList<Integer>();
        path.add(0);
        path.add(1);
        path.add(7);

        branches = new ArrayList<Integer>();
        branches.add(6);
        branches.add(9);
        branches.add(11);
    }

    /**
     * Not very satisfied with this approach!!
     * @param other_path
     * @return
     */
    public static float checkMatchingPath(ArrayList<Integer> other_path)
    {
        //Very bad approach
        int counter = 0;
        boolean contain_branches = false;
        int longest_path = Math.min(other_path.size(), path.size());

        for(int i = 0; i < longest_path; i++)
        {
            if(other_path.get(i).equals(path.get(i)))
            {
                counter++;
            }
        }

        if(other_path.size() > path.size())
        {
            longest_path = Math.min(branches.size(), other_path.size() - path.size());

            contain_branches = true;
            for(int i = path.size(), j = 0; j < longest_path; i++, j++)
            {
                if(other_path.get(i).equals(branches.get(j)))
                {
                    counter++;
                }
            }
        }

        return contain_branches ? counter/(float)(path.size() + branches.size()) : counter/(float)path.size();

    }
}
