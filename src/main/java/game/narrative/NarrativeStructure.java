package game.narrative;

import finder.geometry.Bitmap;
import util.Point;
import util.Util;
import util.algorithms.Node;

import java.util.*;

public class NarrativeStructure {

    //Basic test
    HashMap<String, String[]> productionRules = new HashMap<String, String[]>();
    String axiom = "basic";
    String delimiter = "_";

    public NarrativeStructure()
    {
        //CORE RULES
        productionRules.put("hero", new String[]{"5ma","neo","sh"});
        productionRules.put("conflict", new String[]{"covs","cose","cona","coso"});
        productionRules.put("enemy", new String[]{"emp","emp_enemy_hero","bad","dra"});
        productionRules.put("modifier", new String[]{"chk","mcg","mhq"});
        productionRules.put("basic", new String[]{"hero_conflict_enemy"});

    }

    public void expand(int depth, String axiom)
    {
        ArrayList<String> result = new ArrayList<String>();
        String[] div_ax = axiom.split(this.delimiter);

        Queue<String> queue = new LinkedList<String>();
        queue.addAll(Arrays.asList(div_ax));

        System.out.println(queue);

        //Go one by one to apply the production rules!
        while(!queue.isEmpty())
        {
            //Get the step and expand with production rules
            String current = queue.remove();
            div_ax = this.productionRules.get(current);

            if(this.productionRules.containsKey(current))
            {
                div_ax = this.productionRules.get(current);
//                System.out.println(Arrays.asList(div_ax));
                div_ax = div_ax[Util.getNextInt(0, div_ax.length)].split(this.delimiter);
//                System.out.println(Arrays.asList(div_ax));
                queue.addAll(Arrays.asList(div_ax));
            }
            else
            {
                result.add(current);
            }

        }

        System.out.println(result);

    }


    public static void main(String args[])
    {
        NarrativeStructure ns = new NarrativeStructure();
        ns.expand(0, ns.axiom);


    }

}
