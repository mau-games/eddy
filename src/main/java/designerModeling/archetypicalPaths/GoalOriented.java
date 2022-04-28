package designerModeling.archetypicalPaths;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GoalOriented extends ArchetypicalPath
{
    public static ArchetypicalPathTypes archetype = ArchetypicalPathTypes.GOAL_ORIENTED;
    public static ArrayList<Integer> path = new ArrayList<Integer>(Arrays.asList(0, 1, 7));
    public static ArrayList<Integer> branches = new ArrayList<Integer>(Arrays.asList(6, 9, 11));

    public static ArrayList<SpecialPath> tree_path = new ArrayList<SpecialPath>();

    public static HashMap<Integer, Integer> path_distances = new HashMap<>();

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

        //creating the tree structure!
        SpecialPath s0 = new SpecialPath(null, 0, false, false);
        SpecialPath s1 = new SpecialPath(s0, 1, false, true);
        SpecialPath s2 = new SpecialPath(s1, 7, false, true);

        SpecialPath b1 = new SpecialPath(s2, 6, true, true);
        SpecialPath b2 = new SpecialPath(s2, 9, true, true);
        SpecialPath b3 = new SpecialPath(b2, 11, true, true);


        tree_path.addAll(Arrays.asList(s0, s1, s2, b1, b2, b3));
    }

    public static void createTree()
    {
        tree_path.clear();

        //creating the tree structure!
        SpecialPath s0 = new SpecialPath(null, 0, false, false);
        SpecialPath s1 = new SpecialPath(s0, 1, false, true);
        SpecialPath s2 = new SpecialPath(s1, 7, false, true);

        SpecialPath b1 = new SpecialPath(s2, 6, true, true);
        SpecialPath b2 = new SpecialPath(s2, 9, true, true);
        SpecialPath b3 = new SpecialPath(b2, 11, true, true);

        tree_path.addAll(Arrays.asList(s0, s1, s2, b1, b2, b3));

        path_distances.clear();

        path_distances.put(0, 2);
        path_distances.put(1, 1);
        path_distances.put(7, 0);

        //Don't know about this.
        path_distances.put(6, 2);
        path_distances.put(9, 2);
    }

    public static SubsetPath checkMatchingPath(ArrayList<Integer> other_path)
    {
        //Get all subsets from the path
        ArrayList<SubsetPath> resulting_subsets = ProduceSubsets(other_path);

        for(SubsetPath subsetPath : resulting_subsets)
        {
            int counter = 0;

            int path_index = -1;

            //First lets get which position fits the initial step in the subset
            for(int i = 0; i < tree_path.size(); i++)
            {
                //If the node is a branch, we don't want to calculate this (only care if it is path + branch)
                if(!tree_path.get(i).branch && subsetPath.path.get(0).equals(tree_path.get(i).step))
                {
                    path_index = i;
                    break;
                }
            }

            //If the first step is within the path, then we check the subsequent from that point on.
            if(path_index != -1)
            {
                counter = 1;

                //I think so
//                subsetPath.matching_steps = counter;
                subsetPath.steps_uncounted = path_index;
                subsetPath.setMatchingSteps(counter);
//                subsetPath.reached_branch = tree_path.get(path_index).branch;

                subsetPath = traverseTreePath(tree_path.get(path_index), 1, counter, subsetPath);
//                counter = traverseTreePath(tree_path.get(path_index), path_index, counter, subsetPath);
            }

            subsetPath.calculatePathPercentage(path.size());

//            subsetPath.setPathPercentage(subsetPath.reached_branch ?
//                    subsetPath.matching_steps/(float)(path.size() + branches.size()):
//                    subsetPath.matching_steps/(float)path.size());

//            return contain_branches ? counter/(float)(path.size() + branches.size()) : counter/(float)path.size();


//            subsetPath.matching_steps = counter;
//            if(subsetPath.m)

        }

        //FIXME½
        return bestPath(resulting_subsets);

    }

    public static float checkDistanceToPath(int test_style)
    {
        float dist = 0.0f;
        SpecialPath style_step = null;
        int style_step_index = 0;
        for(SpecialPath sp :  tree_path)
        {
            if(sp.isThisPoint(test_style))
            {
                style_step = sp;
                break;
            }
            style_step_index++;
        }

        if(style_step == null)
            return 0.0f;

        if(style_step.branch ) //special one
        {
            return 0.2f;
        }

        return 1.0f - (path_distances.get(test_style)/3.0f);
    }
//
//    /**
//     * Not very satisfied with this approach!!
//     * @param other_path
//     * @return
//     */
//    public static float checkMatchingPath(ArrayList<Integer> other_path)
//    {
//        //Very bad approach
//        int counter = 0;
//        boolean contain_branches = false;
//        int longest_path = Math.min(other_path.size(), path.size());
//
//        for(int i = 0; i < longest_path; i++)
//        {
//            if(other_path.get(i).equals(path.get(i)))
//            {
//                counter++;
//            }
//        }
//
//        if(other_path.size() > path.size())
//        {
//            longest_path = Math.min(branches.size(), other_path.size() - path.size());
//
//            contain_branches = true;
//            for(int i = path.size(), j = 0; j < longest_path; i++, j++)
//            {
//                if(other_path.get(i).equals(branches.get(j)))
//                {
//                    counter++;
//                }
//            }
//        }
//
//        return contain_branches ? counter/(float)(path.size() + branches.size()) : counter/(float)path.size();
//
//    }
}
