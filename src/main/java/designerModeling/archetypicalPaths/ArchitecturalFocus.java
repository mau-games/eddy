package designerModeling.archetypicalPaths;

import java.util.ArrayList;
import java.util.Arrays;

public class ArchitecturalFocus extends ArchetypicalPath
{
    public static ArchetypicalPathTypes archetype = ArchetypicalPathTypes.ARCHITECTURAL_FOCUS;
    public static ArrayList<Integer> path = new ArrayList<Integer>(Arrays.asList(0,1,2,3));
    public static ArrayList<Integer> branches = new ArrayList<Integer>(Arrays.asList(5,4));

    public ArchitecturalFocus()
    {
        archetype = ArchetypicalPathTypes.ARCHITECTURAL_FOCUS;

        path = new ArrayList<Integer>();
        path.add(0);
        path.add(1);
        path.add(2);
        path.add(3);

        branches = new ArrayList<Integer>();
        branches.add(5);
        branches.add(4);
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
