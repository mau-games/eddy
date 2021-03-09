package generator.algorithm.MAPElites;

/***
 * This class is the one in charge of evaluating where should the evolutionary system focus on now
 * Basically, it will act as the "focalization" part of the system, as well as deciding when are we at
 * "Engagement" and when are we at "Reflection"
 *
 *  - Focalization: adapted from gervas, is where the system should focus on. For us, 1) it will drive the evolution to
 *  expand, add, or remove narrative "perspectives" (related to constant elements such as 5MA subdividing into specific hero types,
 *  Enemies subdividng, conflcits subdividng, or structure (3SA) subdividng into specific acts). 2) It will also be used top denote
 *  what areas of the graph to focus on.
 *
 *  But how will this be evaluateD? The idea  *  thus far is based on where the designer is focusing, where the MAP-Elites
 *  have focused thus far, and finally and possibly, manually   defined by the designer.
 *
 *  I feel and think that Focalization should be something like a direction vector to where to focus on the graph.
 *
 *  - Engagement is the generation of narratives from the system (perhaps
 */
public class NSEvolutionarySystemEvaluator {
}
