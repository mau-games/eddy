package designerModeling.archetypicalPaths;

import java.util.ArrayList;
import java.util.Arrays;

public class SplitCentralFocus extends ArchetypicalPath
{
    public static ArchetypicalPathTypes archetype = ArchetypicalPathTypes.SPLIT_CENTRAL_FOCUS;
    public static ArrayList<Integer> path = new ArrayList<Integer>(Arrays.asList(0, 8, 7));
    public static ArrayList<Integer> branches = new ArrayList<Integer>(Arrays.asList(6, 9, 11));

    public SplitCentralFocus()
    {
        archetype = ArchetypicalPathTypes.SPLIT_CENTRAL_FOCUS;

        path = new ArrayList<Integer>();
        path.add(0);
        path.add(8);
        path.add(7);

        branches = new ArrayList<Integer>();
        branches.add(6);
        branches.add(9);
        branches.add(11);
    }

    /**
     *FIX THIS
     * @param other_path
     * @return
     */
    public static float checkMatchingPath(ArrayList<Integer> other_path)
    {
        //Very bad approach
        int counter = 0;
        boolean contain_branches = false;

        for(int i = 0; i < path.size(); i++)
        {
            if(other_path.get(i).equals(path.get(i)))
            {
                counter++;
            }
        }

        if(other_path.size() > path.size())
        {
            contain_branches = true;
            for(int i = path.size(), j = 0; j < branches.size(); i++, j++)
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
