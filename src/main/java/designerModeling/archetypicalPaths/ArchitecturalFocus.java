package designerModeling.archetypicalPaths;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

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

        this.parent.addChild(this);
    }

    public void setParent(SpecialPath parent, boolean auto_child)
    {
        this.parent = parent;
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

public class ArchitecturalFocus extends ArchetypicalPath
{
    public static ArchetypicalPathTypes archetype = ArchetypicalPathTypes.ARCHITECTURAL_FOCUS;
    public static ArrayList<Integer> path = new ArrayList<Integer>(Arrays.asList(0,1,2,3));
    public static ArrayList<Integer> branches = new ArrayList<Integer>(Arrays.asList(5,4));

    public static ArrayList<SpecialPath> tree_path = new ArrayList<SpecialPath>();

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

        //creating the tree structure!
        SpecialPath s0 = new SpecialPath(null, 0, false, false);
        SpecialPath s1 = new SpecialPath(s0, 1, false, true);
        SpecialPath s2 = new SpecialPath(s1, 2, false, true);
        SpecialPath s3 = new SpecialPath(s2, 3, false, true);
        SpecialPath b1 = new SpecialPath(s3, 5, true, true);
        SpecialPath b2 = new SpecialPath(s3, 4, true, true);

        tree_path.addAll(Arrays.asList(s0, s1, s2, s3, b1, b2));

//        s0.addChild(s1);
//        s1.addChild(s2);

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

    public static SubsetPath checkPath(ArrayList<Integer> other_path)
    {
        //Get all subsets from the path
        ArrayList<SubsetPath> resulting_subsets = ProduceSubsets(other_path);
        int counter = 0;

        for(SubsetPath subsetPath : resulting_subsets)
        {
            int path_index = -1;

            //First lets get which position fits the initial step in the subset
            for(int i = 0; i < path.size(); i++)
            {
                if(subsetPath.path.get(0).equals(path.get(i)))
                {
                    path_index = i;
                    break;
                }
            }

            //If the first step is within the path, then we check the subsequent from that point on.
            if(path_index != -1)
            {
                int longest_path = Math.min(subsetPath.path.size(), path.size());

                for(int i = path_index; i < longest_path; i++)
                {
                    if(subsetPath.path.get(i).equals(path.get(i)))
                    {
                        counter++;
                    }
                }

//                if(subsetPath.path.size() > path.size() - path_index)
//                {
//                    longest_path = Math.min(branches.size(), subsetPath.path.size() - (path.size() - path_index));
//
//                    contain_branches = true;
//                    for(int i = path.size(), j = 0; j < longest_path; i++, j++)
//                    {
//                        if(other_path.get(i).equals(branches.get(j)))
//                        {
//                            counter++;
//                        }
//                    }
//                }
//
//                return contain_branches ? counter/(float)(path.size() + branches.size()) : counter/(float)path.size();
            }

        }

        //FIXME½
        return resulting_subsets.get(0);

    }

    /**
     * Not very satisfied with this approach!!
     * @param other_path
     * @return
     */
    public static float checkMatchingPath2(ArrayList<Integer> other_path)
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

    /**
     * Create subsets of a path divided by different lengths and starting positions
     * @param other_path
     * @return
     */
    public static ArrayList<SubsetPath> ProduceSubsets(ArrayList<Integer> other_path)
    {
        ArrayList<SubsetPath>  resulting_subsets= new ArrayList<SubsetPath> ();
        int max_length = other_path.size();
        ArrayList<Integer> subset_path = new ArrayList<Integer>();

        for(int containing = 0; containing < max_length; containing++)
        {
            for(int starting_pos=0; starting_pos<max_length; starting_pos++)
            {
                if(starting_pos + containing < max_length)
                {
                    subset_path.clear();
                    subset_path.addAll(other_path.subList(starting_pos, starting_pos + containing));
                    resulting_subsets.add(new SubsetPath(starting_pos, containing, max_length, subset_path));
                }

            }

        }

        return resulting_subsets;
    }
}
