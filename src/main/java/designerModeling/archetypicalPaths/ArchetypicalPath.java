package designerModeling.archetypicalPaths;

import generator.algorithm.MAPElites.Dimensions.*;

import java.util.ArrayList;
import java.util.HashMap;

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

    public boolean isThisPoint(int test_style)
    {
        return step == test_style;
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

    //FIXME: We need the full subset path to know the path that has been traversed thus far!
//    public static float calculatePath(ArchetypicalPathTypes archetypical_path, ArrayList<Integer> path_to_test)
//    {
//        switch(archetypical_path) {
//            case ARCHITECTURAL_FOCUS:
//                ArchitecturalFocus.createTree();
//                return ArchitecturalFocus.checkMatchingPath(path_to_test).path_percentage;
//            case GOAL_ORIENTED:
//                GoalOriented.createTree();
//                return GoalOriented.checkMatchingPath(path_to_test).path_percentage;
//            case SPLIT_CENTRAL_FOCUS:
//                SplitCentralFocus.createTree();
//                return SplitCentralFocus.checkMatchingPath(path_to_test).path_percentage;
//            case COMPLEX_BALANCE:
//                ComplexBalance.createTree();
//                return ComplexBalance.checkMatchingPath(path_to_test).path_percentage;
//            case NULL:
//                ArchitecturalFocus.createTree();
//                return ArchitecturalFocus.checkMatchingPath(path_to_test).path_percentage;
//            default:
//                return -1.0f;
//        }
//    }

    public static SubsetPath calculatePath(ArchetypicalPathTypes archetypical_path, ArrayList<Integer> path_to_test)
    {
        switch(archetypical_path) {
            case ARCHITECTURAL_FOCUS:
                ArchitecturalFocus.createTree();
                return ArchitecturalFocus.checkMatchingPath(path_to_test);
            case GOAL_ORIENTED:
                GoalOriented.createTree();
                return GoalOriented.checkMatchingPath(path_to_test);
            case SPLIT_CENTRAL_FOCUS:
                SplitCentralFocus.createTree();
                return SplitCentralFocus.checkMatchingPath(path_to_test);
            case COMPLEX_BALANCE:
                ComplexBalance.createTree();
                return ComplexBalance.checkMatchingPath(path_to_test);
            case NULL:
                ArchitecturalFocus.createTree();
                return ArchitecturalFocus.checkMatchingPath(path_to_test);
            default:
                return new SubsetPath(-1, -1, -1, null);
        }
    }

    public synchronized static float distanceToFinalPath(ArchetypicalPathTypes archetypical_path, int test_style)
    {
        switch(archetypical_path) {
            case ARCHITECTURAL_FOCUS:
                ArchitecturalFocus.createTree();
                return ArchitecturalFocus.checkDistanceToPath(test_style);
            case GOAL_ORIENTED:
                GoalOriented.createTree();
                return GoalOriented.checkDistanceToPath(test_style);
            case SPLIT_CENTRAL_FOCUS:
                SplitCentralFocus.createTree();
                return SplitCentralFocus.checkDistanceToPath(test_style);
            case COMPLEX_BALANCE:
                ComplexBalance.createTree();
                return ComplexBalance.checkDistanceToPath(test_style);
            case NULL:
                ArchitecturalFocus.createTree();
                return ArchitecturalFocus.checkDistanceToPath(test_style);
            default:
                return 0.0f;
        }
    }

    /**
     * Lets try this first!
     * @param resulting_subsets
     * @return
     */
    public static SubsetPath bestPath(ArrayList<SubsetPath> resulting_subsets)
    {
        float min = -1.0f;
        int starting_pos = -10;
        SubsetPath final_subset_path = null;
        ArrayList<SubsetPath> best_subsets = new ArrayList<SubsetPath>();

        for(SubsetPath subsetPath : resulting_subsets)
        {
            if(subsetPath.path_percentage >= min )
            {
                min = subsetPath.path_percentage;
                final_subset_path = subsetPath;
            }
        }

        //I am not convinced here.
        for(SubsetPath subsetPath : resulting_subsets)
        {
            if(subsetPath.path_percentage >= min)
            {
                best_subsets.add(subsetPath);
            }
        }

        for(SubsetPath subsetPath : best_subsets)
        {
            if(subsetPath.start_pos >= starting_pos)
            {
                starting_pos = subsetPath.start_pos;
                final_subset_path = subsetPath;
            }
        }

        return final_subset_path;
    }

    public static SubsetPath traverseTreePath(SpecialPath s_path, int index, int counter, SubsetPath subsetPath )
    {
        if(index >= subsetPath.path.size())
            return subsetPath;

        SpecialPath next_step = s_path.traverse(subsetPath.path.get(index));
        if(next_step != null)
        {
            counter++;
            subsetPath.reached_branch = next_step.branch;
            subsetPath.setMatchingSteps(counter);
//            subsetPath.matching_steps = counter;

            subsetPath = traverseTreePath(next_step, ++index, counter, subsetPath);
        }

//        subsetPath.matching_steps = counter;

        return subsetPath;
    }


    /**
     * Create subsets of a path divided by different lengths and starting positions
     * @param other_path
     * @return
     */
    public static ArrayList<SubsetPath> ProduceSubsets(ArrayList<Integer> other_path)
    {
        // A bit of work for this
        HashMap<Integer, ArrayList<Integer>> path_indexes = new HashMap<Integer, ArrayList<Integer>>();

        for(int index = 0; index < other_path.size(); index++)
        {
            if(path_indexes.containsKey(other_path.get(index)))
            {
                path_indexes.get(other_path.get(index)).add(index);
            }
            else
            {
                path_indexes.put(other_path.get(index), new ArrayList<Integer>());
                path_indexes.get(other_path.get(index)).add(index);
            }
        }

        ArrayList<SubsetPath>  resulting_subsets= new ArrayList<SubsetPath> ();
        int max_length = other_path.size();
        ArrayList<Integer> subset_path = new ArrayList<Integer>();

        for(int containing = 1; containing < max_length + 1; containing++)
        {
            for(int starting_pos = 0; starting_pos < max_length; starting_pos++)
            {
                if(starting_pos + containing <= max_length)
                {
                    subset_path = new ArrayList<Integer>();
                    subset_path.addAll(other_path.subList(starting_pos, starting_pos + containing));
                    resulting_subsets.add(new SubsetPath(starting_pos, containing, max_length, subset_path));
                }
            }
        }

        //Should we do this?
        for(int i = other_path.size() - 1; i > -1; i--)
        {
            ArrayList<Integer> possible_steps = path_indexes.get(other_path.get(i));

            if(possible_steps.size() > 1)
            {
                for(int j = 0; j < possible_steps.size(); j++)
                {
                    for(int  z = j + 1; z < possible_steps.size(); z++)
                    {
                        ArrayList<Integer> ppp = new ArrayList<Integer>();
                        ppp.addAll(other_path.subList(0, possible_steps.get(j) + 1));
                        ppp.addAll(other_path.subList(possible_steps.get(z) + 1, other_path.size()));

                        max_length = ppp.size();
                        subset_path = new ArrayList<Integer>();

                        for(int containing = 1; containing < max_length + 1; containing++)
                        {
                            for(int starting_pos = 0; starting_pos < max_length; starting_pos++)
                            {
                                if(starting_pos + containing <= max_length)
                                {
                                    subset_path = new ArrayList<Integer>();
                                    subset_path.addAll(ppp.subList(starting_pos, starting_pos + containing));
                                    resulting_subsets.add(new SubsetPath(starting_pos, containing, max_length, subset_path));
                                }
                            }
                        }
                    }
                }
            }
        }


        return resulting_subsets;
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
