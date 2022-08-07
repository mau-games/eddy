package game.narrative;

import game.AlgorithmSetup;
import generator.algorithm.Algorithm;
import generator.algorithm.MAPElites.Dimensions.MAPEDimensionFXML;
import generator.algorithm.MAPElites.GrammarMAPEliteAlgorithm;
import generator.algorithm.MAPElites.NSEvolutionarySystemEvaluator;
import generator.algorithm.MAPElites.grammarDimensions.GADimensionGrammar;
import generator.algorithm.MAPElites.grammarDimensions.MAPEDimensionGrammarFXML;
import util.Util;
import util.eventrouting.EventRouter;
import util.eventrouting.Listener;
import util.eventrouting.PCGEvent;
import util.eventrouting.events.AlgorithmDone;

import java.util.*;

public class NarrativeStructure implements Listener {

    //Basic test
    HashMap<String, String[]> productionRules = new HashMap<String, String[]>();
    String axiom = "basic";
    String delimiter = "_";

    GrammarGraph grammarGraph;

    GrammarGraph axiom_1;
    GrammarGraph target_graph;
    int counter = 0;

    private ArrayList<MAPEDimensionGrammarFXML[]> possibleCombinations = new ArrayList<MAPEDimensionGrammarFXML[]>();

    private void RunMAPElites(MAPEDimensionGrammarFXML[] dimensions, GrammarGraph target, GrammarGraph ax)
    {
        counter++;
        Algorithm ga = new GrammarMAPEliteAlgorithm(target, ax);
        ((GrammarMAPEliteAlgorithm)ga).initPopulations(dimensions);
        ga.start();
    }

    private void nextElite()
    {
        elite_counter++;
        AlgorithmSetup.getInstance().setSaveData(true);

//        if(elite_counter == 5)
//        {
//            counter = 0;
//            AlgorithmSetup.getInstance().setMarioConstraints();
//            AlgorithmSetup.getInstance().setConstraintNarrativeStruct(true);
//            runExperiment4();
//            return;
//        }
//        else if(elite_counter == 10)
//        {
//            possibleCombinations.clear();
//            possibleCombinations.add(new MAPEDimensionGrammarFXML[]{
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});
//
//            possibleCombinations.add(new MAPEDimensionGrammarFXML[]{
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});
//
//            possibleCombinations.add(new MAPEDimensionGrammarFXML[]{
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});
//
//            possibleCombinations.add(new MAPEDimensionGrammarFXML[]{
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});
//
//            possibleCombinations.add(new MAPEDimensionGrammarFXML[]{
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
//                    new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});
//
//            counter = 0;
//            AlgorithmSetup.getInstance().setMarioConstraints();
//            AlgorithmSetup.getInstance().setConstraintNarrativeStruct(false);
//            runExperiment4();
//            return;
//        }
//        else if(elite_counter == 15)
//        {
//            counter = 0;
//            AlgorithmSetup.getInstance().setMarioConstraints();
//            AlgorithmSetup.getInstance().setConstraintNarrativeStruct(true);
//            runExperiment4();
//            return;
//        }

        System.out.println("NEXT ELITE");
        RunMAPElites(possibleCombinations.get(counter), target_graph, target_graph);
    }

    private void nextEliteFromExperiment()
    {
        System.out.println("NEXT ELITE FROM EXPERIMENT!");
        RunMAPElites(possibleCombinations.get(counter), target_graph, target_graph);
    }

    private void runZOOTExperiment()
    {
        target_graph = new GrammarGraph();

        GrammarNode y_link = target_graph.addNode(TVTropeType.HERO);
        GrammarNode triforce = target_graph.addNode(TVTropeType.MCG);
        GrammarNode a_link = target_graph.addNode(TVTropeType.NEO);
        GrammarNode gannon = target_graph.addNode(TVTropeType.BAD);
        GrammarNode bad_conf = target_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode zelda = target_graph.addNode(TVTropeType.HERO);
        GrammarNode sheik = target_graph.addNode(TVTropeType.SH);
        GrammarNode good_conf = target_graph.addNode(TVTropeType.CONFLICT);

        y_link.addConnection(triforce, 1);
        triforce.addConnection(a_link, 1);

        gannon.addConnection(bad_conf, 1);
        bad_conf.addConnection(a_link, 1);
        bad_conf.addConnection(zelda, 1);

        zelda.addConnection(sheik, 1);

        a_link.addConnection(good_conf, 1);
        sheik.addConnection(good_conf, 1);
        good_conf.addConnection(gannon, 1);

        target_graph.pattern_finder.findNarrativePatterns(null);

        AlgorithmSetup.getInstance().setSaveData(true);
//        System.out.println(target_graph.toString());
        nextEliteFromExperiment();
    }

    private void runMarioExperiment()
    {
        target_graph = new GrammarGraph();

		GrammarNode mario = target_graph.addNode(TVTropeType.HERO);
		GrammarNode conf = target_graph.addNode(TVTropeType.CONFLICT);
		GrammarNode empire = target_graph.addNode(TVTropeType.EMP);
		GrammarNode fake_bowser = target_graph.addNode(TVTropeType.DRA);
		GrammarNode bowser = target_graph.addNode(TVTropeType.BAD);
		GrammarNode quest_item = target_graph.addNode(TVTropeType.MCG);
		GrammarNode peach = target_graph.addNode(TVTropeType.HERO);

		mario.addConnection(conf, 1);
		mario.addConnection(quest_item, 1);
		conf.addConnection(empire, 1);

		empire.addConnection(fake_bowser, 0);
		fake_bowser.addConnection(bowser, 0);
		bowser.addConnection(quest_item, 0);

		quest_item.addConnection(peach, 1);

        target_graph.pattern_finder.findNarrativePatterns(null);

        AlgorithmSetup.getInstance().setSaveData(true);
//        System.out.println(target_graph.toString());
        nextEliteFromExperiment();
    }

    private void runZTempleExperiment()
    {
        //ZELDA temple
        target_graph = new GrammarGraph();

		GrammarNode link = target_graph.addNode(TVTropeType.HERO);
		GrammarNode conf = target_graph.addNode(TVTropeType.CONFLICT);
		GrammarNode generic_en = target_graph.addNode(TVTropeType.ENEMY);
//		GrammarNode drake = graph.addNode(TVTropeType.DRA);
		GrammarNode bow = target_graph.addNode(TVTropeType.MHQ);
		GrammarNode boss = target_graph.addNode(TVTropeType.BAD);
		GrammarNode quest_item = target_graph.addNode(TVTropeType.MCG);
		GrammarNode elder = target_graph.addNode(TVTropeType.HERO);
		GrammarNode extra_item = target_graph.addNode(TVTropeType.MHQ);

		link.addConnection(conf, 1);
		link.addConnection(quest_item, 1);
		conf.addConnection(generic_en, 1);

		generic_en.addConnection(bow, 0);
//		drake.addConnection(bow, 0);
		bow.addConnection(boss, 0);
		boss.addConnection(quest_item, 0);
		quest_item.addConnection(elder, 1);
		elder.addConnection(extra_item, 0);

		extra_item.addConnection(link, 1);

        target_graph.pattern_finder.findNarrativePatterns(null);

        AlgorithmSetup.getInstance().setSaveData(true);
//        System.out.println(target_graph.toString());
        nextEliteFromExperiment();
    }

    private void runExperiment4()
    {
        //ZELDA temple
        target_graph = new GrammarGraph();

		GrammarNode hero = new GrammarNode(0, TVTropeType.HERO);
		GrammarNode conflict = new GrammarNode(1, TVTropeType.CONFLICT);
		GrammarNode enemy = new GrammarNode(2, TVTropeType.BAD);
		GrammarNode mcg = new GrammarNode(3, TVTropeType.MCG);
		GrammarNode drake = new GrammarNode(4, TVTropeType.DRA);

		hero.addConnection(conflict, 1);
		hero.addConnection(mcg, 1);
		conflict.addConnection(enemy, 1);
		conflict.addConnection(drake, 1);
		drake.addConnection(mcg, 0);

        target_graph.nodes.add(hero);
        target_graph.nodes.add(conflict);
        target_graph.nodes.add(enemy);
        target_graph.nodes.add(mcg);
        target_graph.nodes.add(drake);

        target_graph.pattern_finder.findNarrativePatterns(null);

        AlgorithmSetup.getInstance().setSaveData(true);
//        System.out.println(target_graph.toString());
        nextEliteFromExperiment();
    }

    private void runExperiment()
    {
        axiom_1 = new GrammarGraph();
        GrammarNode axiom_a = axiom_1.addNode(TVTropeType.ANY);
//        GrammarNode b1 = graph_axiom.addNode(TVTropeType.ANY);
//        a1.addConnection(b1, 1);

//        GrammarNode hero = new GrammarNode(0, TVTropeType.HERO);
//        GrammarNode conflict = new GrammarNode(1, TVTropeType.CONFLICT);
//        GrammarNode enemy = new GrammarNode(2, TVTropeType.ENEMY);
//
//        hero.addConnection(conflict, 1);
//        conflict.addConnection(enemy, 1);

        target_graph = new GrammarGraph();
        GrammarNode a1 = target_graph.addNode(TVTropeType.HERO);
        GrammarNode b1 = target_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = target_graph.addNode(TVTropeType.ENEMY);

        //Hero - Conflict
        a1.addConnection(b1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);

        AlgorithmSetup.getInstance().setSaveData(true);

        nextElite();

        //now it looks like it works.
        //Now i need to make it that you actually create the phenotype!
//        RunMAPElites(new MAPEDimensionGrammarFXML[]{
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5)
//        }, target_graph, target_graph);
    }

    private void runPatternFinderExperiment()
    {
        GrammarGraph test_graph = new GrammarGraph();
        GrammarNode a1 = test_graph.addNode(TVTropeType.HERO);
        GrammarNode b1 = test_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = test_graph.addNode(TVTropeType.ENEMY);
        GrammarNode d1 = test_graph.addNode(TVTropeType.SH);
        GrammarNode e1 = test_graph.addNode(TVTropeType.CONFLICT);

        //Hero - Conflict
        a1.addConnection(b1, 2);
        a1.addConnection(d1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);

        //SH - Conflict
        d1.addConnection(b1, 1);

        //ENEMY - Conflict_2
        c1.addConnection(e1, 1);

        //Conflict_2 - SH
        e1.addConnection(d1, 1);

        test_graph.pattern_finder.findNarrativePatterns(test_graph);
    }

    private void runQualityExperiment()
    {
        GrammarGraph test_graph = new GrammarGraph();

        GrammarNode a1 = test_graph.addNode(TVTropeType.HERO);

        //There is something wrong here? Else the fitness function has to shine! Enemy is "revealed" "to be the hero
        // But enemy do not participate in anything else, what does that mean?? -- todo: I think it should mean not interesting! let the fitness work!
        GrammarNode b1 = test_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = test_graph.addNode(TVTropeType.EMP);
        GrammarNode d1 = test_graph.addNode(TVTropeType.DRA);
        GrammarNode e1 = test_graph.addNode(TVTropeType.NEO);
        GrammarNode f1 = test_graph.addNode(TVTropeType.BAD);

        GrammarNode pd1 = test_graph.addNode(TVTropeType.MHQ);
        a1.addConnection(pd1, 1);

        //Hero - Conflict
        a1.addConnection(b1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);

        c1.addConnection(d1, 0);
        d1.addConnection(e1, 0);
        e1.addConnection(f1, 0);
//        c1.addConnection(d1, 0);

//        //SH - Conflict
//        d1.addConnection(b1, 1);
//
//        //ENEMY - Conflict_2
//        c1.addConnection(e1, 1);
//
//        //Conflict_2 - SH
//        e1.addConnection(d1, 1);

        test_graph.pattern_finder.findNarrativePatterns(test_graph);
    }


    private void runDerivativePatternExperiment()
    {
        GrammarGraph test_graph = new GrammarGraph();

        GrammarNode suba1 = test_graph.addNode(TVTropeType.HERO);
        GrammarNode a1 = test_graph.addNode(TVTropeType.HERO);

        //There is something wrong here? Else the fitness function has to shine! Enemy is "revealed" "to be the hero
        // But enemy do not participate in anything else, what does that mean?? -- todo: I think it should mean not interesting! let the fitness work!
        GrammarNode aa1 = test_graph.addNode(TVTropeType.ENEMY);
        GrammarNode b1 = test_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode b2 = test_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = test_graph.addNode(TVTropeType.EMP);
        GrammarNode d1 = test_graph.addNode(TVTropeType.DRA);
        GrammarNode e1 = test_graph.addNode(TVTropeType.NEO);
        GrammarNode f1 = test_graph.addNode(TVTropeType.BAD);

        GrammarNode pd1 = test_graph.addNode(TVTropeType.MHQ);
        a1.addConnection(pd1, 1);

        suba1.addConnection(a1, 1);
        suba1.addConnection(b1, 1);

        //Hero - Conflict
        a1.addConnection(b1, 1);
        aa1.addConnection(a1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);
        b2.addConnection(b1, 1);

        c1.addConnection(d1, 0);
        d1.addConnection(e1, 0);
        e1.addConnection(f1, 0);
//        c1.addConnection(d1, 0);

//        //SH - Conflict
//        d1.addConnection(b1, 1);
//
//        //ENEMY - Conflict_2
//        c1.addConnection(e1, 1);
//
//        //Conflict_2 - SH
//        e1.addConnection(d1, 1);

        test_graph.pattern_finder.findNarrativePatterns(test_graph);
    }

    public void runTestRemoveNode_Connections()
    {
        GrammarGraph graph_axiom = new GrammarGraph();
        GrammarNode a1 = graph_axiom.addNode(TVTropeType.ANY);
        GrammarNode b1 = graph_axiom.addNode(TVTropeType.ANY);
        a1.addConnection(b1, 1);

        //PATTERN 1

        GrammarPattern pattern_1 = new GrammarPattern();
        GrammarGraph ipatt_1 = new GrammarGraph();
        GrammarGraph opatt_11 = new GrammarGraph();

        GrammarNode a = new GrammarNode(0, TVTropeType.ANY);
        GrammarNode b = new GrammarNode(1, TVTropeType.ANY);
        GrammarNode c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(c, 1);
        b.addConnection(c, 1);
        b.addConnection(a, 1);
        ipatt_1.nodes.add(a); ipatt_1.nodes.add(b); ipatt_1.nodes.add(c);

        a = new GrammarNode(0, TVTropeType.ANY);
        opatt_11.nodes.add(a);

        pattern_1.setPattern(ipatt_1);
        pattern_1.addProductionRule(opatt_11);

        //PATTERN 2

        GrammarPattern pattern_2 = new GrammarPattern();
        GrammarGraph ipatt_2 = new GrammarGraph();
        GrammarGraph opatt_21 = new GrammarGraph();

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        a.addConnection(b, 1);
        ipatt_2.nodes.add(a); ipatt_2.nodes.add(b);

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(c, 1);
        b.addConnection(c, 1);
        opatt_21.nodes.add(a); opatt_21.nodes.add(b); opatt_21.nodes.add(c);

        pattern_2.setPattern(ipatt_2);
        pattern_2.addProductionRule(opatt_21);

        //PATTERN 2

        GrammarPattern pattern_3 = new GrammarPattern();
        GrammarGraph ipatt_3 = new GrammarGraph();
        GrammarGraph opatt_31 = new GrammarGraph();

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(c, 1);
        c.addConnection(b, 1);
        ipatt_3.nodes.add(a); ipatt_3.nodes.add(b); ipatt_3.nodes.add(c);

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
//        c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(b, 1);
//        c.addConnection(b, 1);
        opatt_31.nodes.add(a);opatt_31.nodes.add(b);
//        opatt_31.nodes.add(b); opatt_31.nodes.add(c);

        pattern_3.setPattern(ipatt_3);
        pattern_3.addProductionRule(opatt_31);

        //APPLY CHANGES

        System.out.println(graph_axiom.toString());
        pattern_1.match(graph_axiom, 4);
        System.out.println(graph_axiom.toString());
        pattern_2.match(graph_axiom, 4);
        System.out.println(graph_axiom.toString());
        pattern_3.match(graph_axiom, 4); //THIS IS THE RULE THAT GAVE PROBLEMS
        System.out.println(graph_axiom.toString());

        GrammarGraph gaxiom = new GrammarGraph();
        a1 = gaxiom.addNode(TVTropeType.ANY);
        b1 = gaxiom.addNode(TVTropeType.ANY);
        a1.addConnection(b1, 1);



//        pattern_2.match(grammarGraph, 4);
//        System.out.println(grammarGraph.toString());
    }

    public void runTestGhostConns_RepeatedID()
    {
        GrammarGraph graph_axiom = new GrammarGraph();
        GrammarNode a1 = graph_axiom.addNode(TVTropeType.ANY);
        GrammarNode b1 = graph_axiom.addNode(TVTropeType.ANY);
        a1.addConnection(b1, 1);

        //PATTERN 1

        GrammarPattern pattern_1 = new GrammarPattern();
        GrammarGraph ipatt_1 = new GrammarGraph();
        GrammarGraph opatt_11 = new GrammarGraph();

        GrammarNode a = new GrammarNode(0, TVTropeType.ANY);
//        GrammarNode b = new GrammarNode(1, TVTropeType.ANY);
//        GrammarNode c = new GrammarNode(2, TVTropeType.ANY);
//        a.addConnection(c, 1);
//        b.addConnection(c, 1);
//        b.addConnection(a, 1);
        ipatt_1.nodes.add(a);
        a = new GrammarNode(0, TVTropeType.ANY);
        GrammarNode b = new GrammarNode(1, TVTropeType.ANY);
        a.addConnection(b, 1);
        opatt_11.nodes.add(a); opatt_11.nodes.add(b);

        pattern_1.setPattern(ipatt_1);
        pattern_1.addProductionRule(opatt_11);

        //PATTERN 2

        GrammarPattern pattern_2 = new GrammarPattern();
        GrammarGraph ipatt_2 = new GrammarGraph();
        GrammarGraph opatt_21 = new GrammarGraph();

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        GrammarNode c = new GrammarNode(2, TVTropeType.ANY);
        b.addConnection(a, 1);
        ipatt_2.nodes.add(a); ipatt_2.nodes.add(b); ipatt_2.nodes.add(c);

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
//        c = new GrammarNode(2, TVTropeType.ANY);
//        a.addConnection(c, 1);
        b.addConnection(a, 1);
        opatt_21.nodes.add(a); opatt_21.nodes.add(b);

        pattern_2.setPattern(ipatt_2);
        pattern_2.addProductionRule(opatt_21);

        //PATTERN 3

        GrammarPattern pattern_3 = new GrammarPattern();
        GrammarGraph ipatt_3 = new GrammarGraph();
        GrammarGraph opatt_31 = new GrammarGraph();

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
//        c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(b, 1);
//        c.addConnection(b, 1);
        ipatt_3.nodes.add(a); ipatt_3.nodes.add(b);

        a = new GrammarNode(0, TVTropeType.ANY);
        opatt_31.nodes.add(a);

        pattern_3.setPattern(ipatt_3);
        pattern_3.addProductionRule(opatt_31);

        //APPLY CHANGES

        System.out.println(graph_axiom.toString());
        pattern_1.match(graph_axiom, 4);
        System.out.println(graph_axiom.toString());
        pattern_2.match(graph_axiom, 4);
        System.out.println(graph_axiom.toString());
        pattern_3.match(graph_axiom, 4); //THIS IS THE RULE THAT GAVE PROBLEMS
        System.out.println(graph_axiom.toString());

        GrammarGraph gaxiom = new GrammarGraph();
        a1 = gaxiom.addNode(TVTropeType.ANY);
        b1 = gaxiom.addNode(TVTropeType.ANY);
        a1.addConnection(b1, 1);

        short dist = gaxiom.distanceBetweenGraphs(graph_axiom);

//        pattern_2.match(grammarGraph, 4);
//        System.out.println(grammarGraph.toString());
    }

    private void runFitnessTest()
    {
        GrammarGraph axiom_graph = new GrammarGraph();
        GrammarNode a1 = axiom_graph.addNode(TVTropeType.HERO);

        //There is something wrong here? Else the fitness function has to shine! Enemy is "revealed" "to be the hero
        // But enemy do not participate in anything else, what does that mean?? -- todo: I think it should mean not interesting! let the fitness work!
        GrammarNode b1 = axiom_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = axiom_graph.addNode(TVTropeType.EMP);

        //Hero - Conflict
        a1.addConnection(b1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);

        GrammarGraph test_graph = new GrammarGraph();
        GrammarNode tg_a1 = test_graph.addNode(TVTropeType.HERO);
        GrammarNode tg_b1 = test_graph.addNode(TVTropeType.ENEMY);
        GrammarNode tg_c1 = test_graph.addNode(TVTropeType.CONFLICT);
        tg_a1.addConnection(tg_b1, 1);
        tg_c1.addConnection(tg_b1, 1);

        NSEvolutionarySystemEvaluator evaluator = new NSEvolutionarySystemEvaluator();
        evaluator.testEvaluation(test_graph, axiom_graph);

//        test_graph.pattern_finder.findNarrativePatterns(test_graph);
    }

    private void runCoherenceTest()
    {
        GrammarGraph axiom_graph = new GrammarGraph();
        GrammarNode a1 = axiom_graph.addNode(TVTropeType.HERO);

        //There is something wrong here? Else the fitness function has to shine! Enemy is "revealed" "to be the hero
        // But enemy do not participate in anything else, what does that mean?? -- todo: I think it should mean not interesting! let the fitness work!
        GrammarNode b1 = axiom_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = axiom_graph.addNode(TVTropeType.EMP);

        //Hero - Conflict
        a1.addConnection(b1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);

        GrammarGraph test_graph = new GrammarGraph();
        GrammarNode tg_a1 = test_graph.addNode(TVTropeType.HERO);
        GrammarNode tg_b1 = test_graph.addNode(TVTropeType.ENEMY);
        GrammarNode tg_c1 = test_graph.addNode(TVTropeType.CONFLICT);

        //Connection problem test!
//        tg_a1.addConnection(tg_b1, 0);
//        tg_b1.addConnection(tg_a1, 2);
//        tg_a1.addConnection(tg_b1, 2);

        tg_a1.addConnection(tg_b1, 0);
        tg_a1.addConnection(tg_c1, 0);
//        tg_c1.addConnection(tg_a1, 0);

        tg_c1.addConnection(tg_b1, 2);
        tg_b1.addConnection(tg_a1, 1);

        NSEvolutionarySystemEvaluator evaluator = new NSEvolutionarySystemEvaluator();
        evaluator.testEvaluation(test_graph, axiom_graph);

//        test_graph.pattern_finder.findNarrativePatterns(test_graph);
    }

    private void runSamples()
    {
        GrammarGraph axiom_graph = new GrammarGraph();
        GrammarNode a1 = axiom_graph.addNode(TVTropeType.HERO);

        //There is something wrong here? Else the fitness function has to shine! Enemy is "revealed" "to be the hero
        // But enemy do not participate in anything else, what does that mean?? -- todo: I think it should mean not interesting! let the fitness work!
        GrammarNode b1 = axiom_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = axiom_graph.addNode(TVTropeType.EMP);

        //Hero - Conflict
        a1.addConnection(b1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);

        GrammarGraph test_graph = new GrammarGraph();
        GrammarNode tg_a1 = test_graph.addNode(TVTropeType.HERO);
        GrammarNode tg_b1 = test_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode tg_c1 = test_graph.addNode(TVTropeType.ENEMY);
//        GrammarNode tg_d1 = test_graph.addNode(TVTropeType.CHK);
//        GrammarNode tg_e1 = test_graph.addNode(TVTropeType.CHK);

        tg_a1.addConnection(tg_b1, 2);
        tg_c1.addConnection(tg_a1, 0);
//        tg_b1.addConnection(tg_e1, 0);
//        tg_c1.addConnection(tg_d1, 0);
//        tg_d1.addConnection(tg_a1, 1);
//        tg_d1.addConnection(tg_a1, 1);

        NSEvolutionarySystemEvaluator evaluator = new NSEvolutionarySystemEvaluator();
        evaluator.testEvaluation(test_graph, axiom_graph);

//        test_graph.pattern_finder.findNarrativePatterns(test_graph);
    }

    private void sampleTest()
    {
        GrammarGraph axiom_graph = new GrammarGraph();
        GrammarNode a1 = axiom_graph.addNode(TVTropeType.HERO);

        //There is something wrong here? Else the fitness function has to shine! Enemy is "revealed" "to be the hero
        // But enemy do not participate in anything else, what does that mean?? -- todo: I think it should mean not interesting! let the fitness work!
        GrammarNode b1 = axiom_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = axiom_graph.addNode(TVTropeType.EMP);
        GrammarNode d1 = axiom_graph.addNode(TVTropeType.CHK);

        //Hero - Conflict
        a1.addConnection(b1, 1);
        a1.addConnection(d1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);
        c1.addConnection(d1, 0);


        // NOW ALL THE TESTS!

        // Kind of made sense after thinking about it!
//        GrammarGraph test_graph = new GrammarGraph();
//        GrammarNode tg_a1 = test_graph.addNode(TVTropeType.HERO);
//        GrammarNode tg_b1 = test_graph.addNode(TVTropeType.CONFLICT);
//        GrammarNode tg_c1 = test_graph.addNode(TVTropeType.ENEMY);
////        GrammarNode tg_d1 = test_graph.addNode(TVTropeType.CHK);
////        GrammarNode tg_e1 = test_graph.addNode(TVTropeType.CHK);
//
//        tg_a1.addConnection(tg_c1, 0);
//        tg_a1.addConnection(tg_b1, 1);
//        tg_b1.addConnection(tg_c1, 2);

        //SHOWED AN IMPORTANT PROBLEM!

//        GrammarGraph test_graph = new GrammarGraph();
//        GrammarNode tg_a1 = test_graph.addNode(TVTropeType.HERO);
//        GrammarNode tg_b1 = test_graph.addNode(TVTropeType.CONFLICT);
//        GrammarNode tg_c1 = test_graph.addNode(TVTropeType.ENEMY);
//        GrammarNode tg_d1 = test_graph.addNode(TVTropeType.PLOT_DEVICE);
//        GrammarNode tg_e1 = test_graph.addNode(TVTropeType.PLOT_DEVICE);
//        GrammarNode tg_f1 = test_graph.addNode(TVTropeType.PLOT_DEVICE);
////        GrammarNode tg_d1 = test_graph.addNode(TVTropeType.CHK);
////        GrammarNode tg_e1 = test_graph.addNode(TVTropeType.CHK);
//
//        tg_b1.addConnection(tg_a1, 0);
//        tg_c1.addConnection(tg_a1, 0);
//        tg_d1.addConnection(tg_b1, 0);
//        tg_e1.addConnection(tg_a1, 0);
//        tg_f1.addConnection(tg_e1, 0);

        //Fixed
//        GrammarGraph test_graph = new GrammarGraph();
//        GrammarNode tg_a1 = test_graph.addNode(TVTropeType.HERO);
//        GrammarNode tg_b1 = test_graph.addNode(TVTropeType.CONFLICT);
//        GrammarNode tg_c1 = test_graph.addNode(TVTropeType.ENEMY);
//        GrammarNode tg_d1 = test_graph.addNode(TVTropeType.CHK);
//        GrammarNode tg_e1 = test_graph.addNode(TVTropeType.MCG);
//        GrammarNode tg_f1 = test_graph.addNode(TVTropeType.ANY);
////        GrammarNode tg_d1 = test_graph.addNode(TVTropeType.CHK);
////        GrammarNode tg_e1 = test_graph.addNode(TVTropeType.CHK);
//
//        tg_a1.addConnection(tg_b1, 1);
//        tg_b1.addConnection(tg_c1, 1);
//        tg_d1.addConnection(tg_c1, 0);
//        tg_e1.addConnection(tg_c1, 0);
//        tg_f1.addConnection(tg_c1, 0);
//        tg_f1.addConnection(tg_e1, 2);

        //Interesting PROBLEM!

//        GrammarGraph test_graph = new GrammarGraph();
//        GrammarNode tg_a1 = test_graph.addNode(TVTropeType.CONFLICT);
//        GrammarNode tg_b1 = test_graph.addNode(TVTropeType.MCG);
//        GrammarNode tg_c1 = test_graph.addNode(TVTropeType.ENEMY);
////        GrammarNode tg_d1 = test_graph.addNode(TVTropeType.MCG);
//
//        tg_a1.addConnection(tg_b1, 1);
//        tg_c1.addConnection(tg_b1, 0);
////        tg_b1.addConnection(tg_d1, 0);

        GrammarGraph test_graph = new GrammarGraph();
        GrammarNode tg_a1 = test_graph.addNode(TVTropeType.HERO);
        GrammarNode tg_b1 = test_graph.addNode(TVTropeType.CONFLICT);
//        GrammarNode tg_d1 = test_graph.addNode(TVTropeType.MCG);

        tg_b1.addConnection(tg_a1, 1);



        NSEvolutionarySystemEvaluator evaluator = new NSEvolutionarySystemEvaluator();
        evaluator.testEvaluation(test_graph, axiom_graph);

//        test_graph.pattern_finder.findNarrativePatterns(test_graph);
    }

    public void runSameTestSameAxiom()
    {
        GrammarGraph axiom_graph = new GrammarGraph();
        GrammarNode a1 = axiom_graph.addNode(TVTropeType.HERO);

        //There is something wrong here? Else the fitness function has to shine! Enemy is "revealed" "to be the hero
        // But enemy do not participate in anything else, what does that mean?? -- todo: I think it should mean not interesting! let the fitness work!
        GrammarNode b1 = axiom_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = axiom_graph.addNode(TVTropeType.ENEMY);

        //Hero - Conflict
        a1.addConnection(b1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);

        GrammarGraph test_graph = new GrammarGraph();
        GrammarNode tg_a1 = test_graph.addNode(TVTropeType.HERO);
        GrammarNode tg_b1 = test_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode tg_c1 = test_graph.addNode(TVTropeType.ENEMY);

        tg_a1.addConnection(tg_b1, 1);
        tg_b1.addConnection(tg_c1, 1);

        NSEvolutionarySystemEvaluator evaluator = new NSEvolutionarySystemEvaluator();
        evaluator.testEvaluation(test_graph, axiom_graph);
    }

    public void runSpecialDerivativeIssue()
    {
        GrammarGraph axiom_graph = new GrammarGraph();
        GrammarNode a1 = axiom_graph.addNode(TVTropeType.HERO);

        //There is something wrong here? Else the fitness function has to shine! Enemy is "revealed" "to be the hero
        // But enemy do not participate in anything else, what does that mean?? -- todo: I think it should mean not interesting! let the fitness work!
        GrammarNode b1 = axiom_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c1 = axiom_graph.addNode(TVTropeType.ENEMY);

        //Hero - Conflict
        a1.addConnection(b1, 1);

        //Conflict - Enemy
        b1.addConnection(c1, 1);

        GrammarGraph test_graph = new GrammarGraph();
        GrammarNode tg_a1 = test_graph.addNode(TVTropeType.HERO);
        GrammarNode tg_b1 = test_graph.addNode(TVTropeType.MCG);
        GrammarNode tg_c1 = test_graph.addNode(TVTropeType.CHK);
        GrammarNode tg_d1 = test_graph.addNode(TVTropeType.MCG);
        GrammarNode tg_e1 = test_graph.addNode(TVTropeType.CHK);
        GrammarNode tg_f1 = test_graph.addNode(TVTropeType.MCG);
        GrammarNode tg_g1 = test_graph.addNode(TVTropeType.CHK);

        tg_a1.addConnection(tg_g1, 0);
        tg_b1.addConnection(tg_a1, 0);
        tg_c1.addConnection(tg_b1, 0);
        tg_d1.addConnection(tg_a1, 0);
        tg_e1.addConnection(tg_d1, 0);
        tg_f1.addConnection(tg_a1, 0);
        tg_g1.addConnection(tg_f1, 0);

        NSEvolutionarySystemEvaluator evaluator = new NSEvolutionarySystemEvaluator();
        evaluator.testEvaluation(test_graph, axiom_graph);
    }

    private void analyzeZOOTElite()
    {
        target_graph = new GrammarGraph();

        GrammarNode y_link = target_graph.addNode(TVTropeType.HERO);
        GrammarNode triforce = target_graph.addNode(TVTropeType.MCG);
        GrammarNode a_link = target_graph.addNode(TVTropeType.NEO);
        GrammarNode gannon = target_graph.addNode(TVTropeType.BAD);
        GrammarNode bad_conf = target_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode zelda = target_graph.addNode(TVTropeType.HERO);
        GrammarNode sheik = target_graph.addNode(TVTropeType.SH);
        GrammarNode good_conf = target_graph.addNode(TVTropeType.CONFLICT);

        y_link.addConnection(triforce, 1);
        triforce.addConnection(a_link, 1);

        gannon.addConnection(bad_conf, 1);
        bad_conf.addConnection(a_link, 1);
        bad_conf.addConnection(zelda, 1);

        zelda.addConnection(sheik, 1);

        a_link.addConnection(good_conf, 1);
        sheik.addConnection(good_conf, 1);
        good_conf.addConnection(gannon, 1);

        target_graph.pattern_finder.findNarrativePatterns(null);

        //NOW WE BUILD THE ELITE!

        GrammarGraph narrative_graph = new GrammarGraph();

        GrammarNode a = narrative_graph.addNode(TVTropeType.HERO);
        GrammarNode b = narrative_graph.addNode(TVTropeType.MCG);
        GrammarNode c = narrative_graph.addNode(TVTropeType.SH);
        GrammarNode d = narrative_graph.addNode(TVTropeType.NEO);
        GrammarNode e = narrative_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode f = narrative_graph.addNode(TVTropeType.BAD);
        GrammarNode g = narrative_graph.addNode(TVTropeType.HERO);

        a.addConnection(b, 1);
        c.addConnection(b, 1);
        b.addConnection(d, 0);
        f.addConnection(e, 1);
        e.addConnection(d, 1);
        e.addConnection(g, 1);

        target_graph.pattern_finder.all_narrative_patterns.clear();
        narrative_graph.pattern_finder.all_narrative_patterns.clear();

        NSEvolutionarySystemEvaluator evaluator = new NSEvolutionarySystemEvaluator();
        System.out.println("OCARINA!!");
        System.out.println("ORIGINAL");
        evaluator.testEvaluation(target_graph, null);
        target_graph.pattern_finder.all_narrative_patterns.clear();
        narrative_graph.pattern_finder.all_narrative_patterns.clear();
        System.out.println();
        System.out.println("ELITE NO TARGET");
        evaluator.testEvaluation(narrative_graph, null);
        target_graph.pattern_finder.all_narrative_patterns.clear();
        narrative_graph.pattern_finder.all_narrative_patterns.clear();
        System.out.println();
        System.out.println("ELITE");
        evaluator.testEvaluation(narrative_graph, target_graph);

        System.out.println("");

        double step = narrative_graph.distanceBetweenGraphs(target_graph);

        System.out.println("STEP: " + step);
    }

    public void analyzeZTempleElite()
    {
        //ZELDA temple
        target_graph = new GrammarGraph();

        GrammarNode link = target_graph.addNode(TVTropeType.HERO);
        GrammarNode conf = target_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode generic_en = target_graph.addNode(TVTropeType.ENEMY);
//		GrammarNode drake = graph.addNode(TVTropeType.DRA);
        GrammarNode bow = target_graph.addNode(TVTropeType.MHQ);
        GrammarNode boss = target_graph.addNode(TVTropeType.BAD);
        GrammarNode quest_item = target_graph.addNode(TVTropeType.MCG);
        GrammarNode elder = target_graph.addNode(TVTropeType.HERO);
        GrammarNode extra_item = target_graph.addNode(TVTropeType.MHQ);

        link.addConnection(conf, 1);
        link.addConnection(quest_item, 1);
        conf.addConnection(generic_en, 1);

        generic_en.addConnection(bow, 0);
//		drake.addConnection(bow, 0);
        bow.addConnection(boss, 0);
        boss.addConnection(quest_item, 0);
        quest_item.addConnection(elder, 1);
        elder.addConnection(extra_item, 0);

        extra_item.addConnection(link, 1);

        target_graph.pattern_finder.findNarrativePatterns(null);

        //NOW WE BUILD THE ELITE!

        GrammarGraph narrative_graph = new GrammarGraph();

        GrammarNode a = narrative_graph.addNode(TVTropeType.HERO);
        GrammarNode b = narrative_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c = narrative_graph.addNode(TVTropeType.ENEMY);
        GrammarNode d = narrative_graph.addNode(TVTropeType.MHQ);
        GrammarNode e = narrative_graph.addNode(TVTropeType.CHK);
        GrammarNode f = narrative_graph.addNode(TVTropeType.BAD);
        GrammarNode g = narrative_graph.addNode(TVTropeType.MCG);

        a.addConnection(b, 1);
        b.addConnection(c, 1);
        c.addConnection(d, 0);
        d.addConnection(e, 0);
        e.addConnection(f, 0);
        f.addConnection(d, 1);
        a.addConnection(g, 1);
        g.addConnection(f, 0); //this was 1 for some reason.

        target_graph.pattern_finder.all_narrative_patterns.clear();
        narrative_graph.pattern_finder.all_narrative_patterns.clear();

        NSEvolutionarySystemEvaluator evaluator = new NSEvolutionarySystemEvaluator();
        System.out.println("TEMPLE ZELDA!");
        System.out.println("ORIGINAL");
        evaluator.testEvaluation(target_graph, null);
        target_graph.pattern_finder.all_narrative_patterns.clear();
        narrative_graph.pattern_finder.all_narrative_patterns.clear();
        System.out.println();
        System.out.println("ELITE NO TARGET");
        evaluator.testEvaluation(narrative_graph, null);
        target_graph.pattern_finder.all_narrative_patterns.clear();
        narrative_graph.pattern_finder.all_narrative_patterns.clear();
        System.out.println();
        System.out.println("ELITE");
        evaluator.testEvaluation(narrative_graph, target_graph);

        System.out.println("");

        double step = narrative_graph.distanceBetweenGraphs(target_graph);

        System.out.println("STEP: " + step);
    }

    public void analyzeExp4Info()
    {
        target_graph = new GrammarGraph();

        GrammarNode hero = new GrammarNode(0, TVTropeType.HERO);
        GrammarNode conflict = new GrammarNode(1, TVTropeType.CONFLICT);
        GrammarNode enemy = new GrammarNode(2, TVTropeType.BAD);
        GrammarNode mcg = new GrammarNode(3, TVTropeType.MCG);
        GrammarNode drake = new GrammarNode(4, TVTropeType.DRA);

        hero.addConnection(conflict, 1);
        hero.addConnection(mcg, 1);
        conflict.addConnection(enemy, 1);
        conflict.addConnection(drake, 1);
        drake.addConnection(mcg, 0);

        target_graph.nodes.add(hero);
        target_graph.nodes.add(conflict);
        target_graph.nodes.add(enemy);
        target_graph.nodes.add(mcg);
        target_graph.nodes.add(drake);

        target_graph.pattern_finder.findNarrativePatterns(null);


        target_graph.pattern_finder.all_narrative_patterns.clear();

        NSEvolutionarySystemEvaluator evaluator = new NSEvolutionarySystemEvaluator();
        System.out.println("EXPERIMNT 4 TEST");
        System.out.println("ORIGINAL");
        evaluator.testEvaluation(target_graph, null);
        target_graph.pattern_finder.all_narrative_patterns.clear();

    }

    public void analyzeMarioElite()
    {
        target_graph = new GrammarGraph();

        GrammarNode mario = target_graph.addNode(TVTropeType.HERO);
        GrammarNode conf = target_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode empire = target_graph.addNode(TVTropeType.EMP);
        GrammarNode fake_bowser = target_graph.addNode(TVTropeType.DRA);
        GrammarNode bowser = target_graph.addNode(TVTropeType.BAD);
        GrammarNode quest_item = target_graph.addNode(TVTropeType.MCG);
        GrammarNode peach = target_graph.addNode(TVTropeType.HERO);

        mario.addConnection(conf, 1);
        mario.addConnection(quest_item, 1);
        conf.addConnection(empire, 1);

        empire.addConnection(fake_bowser, 0);
        fake_bowser.addConnection(bowser, 0);
        bowser.addConnection(quest_item, 0);

        quest_item.addConnection(peach, 1);

        target_graph.pattern_finder.findNarrativePatterns(null);

        //NOW WE BUILD THE ELITE!

        GrammarGraph narrative_graph = new GrammarGraph();

        GrammarNode a = narrative_graph.addNode(TVTropeType.HERO);
        GrammarNode b = narrative_graph.addNode(TVTropeType.CONFLICT);
        GrammarNode c = narrative_graph.addNode(TVTropeType.EMP);
        GrammarNode d = narrative_graph.addNode(TVTropeType.DRA);
        GrammarNode e = narrative_graph.addNode(TVTropeType.MCG);
        GrammarNode f = narrative_graph.addNode(TVTropeType.BAD);

        a.addConnection(b, 1);
        b.addConnection(c, 1);
        c.addConnection(d, 0);

        a.addConnection(e, 1);
        e.addConnection(f, 0);

        target_graph.pattern_finder.all_narrative_patterns.clear();
        narrative_graph.pattern_finder.all_narrative_patterns.clear();

        NSEvolutionarySystemEvaluator evaluator = new NSEvolutionarySystemEvaluator();
        System.out.println("It's me MARIO!");
        System.out.println("ORIGINAL");
        evaluator.testEvaluation(target_graph, null);
        target_graph.pattern_finder.all_narrative_patterns.clear();
        narrative_graph.pattern_finder.all_narrative_patterns.clear();
        System.out.println();
        System.out.println("ELITE NO TARGET");
        evaluator.testEvaluation(narrative_graph, null);
        target_graph.pattern_finder.all_narrative_patterns.clear();
        narrative_graph.pattern_finder.all_narrative_patterns.clear();
        System.out.println();
        System.out.println("ELITE");
        evaluator.testEvaluation(narrative_graph, target_graph);

        System.out.println("");

        double step = narrative_graph.distanceBetweenGraphs(target_graph);

        System.out.println("STEP: " + step);
    }

    int elite_counter = 0;

    public NarrativeStructure()
    {
//        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5)});
//        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5)});
//        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5)});
//        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});
//        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5)});
//        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5)});
//        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5)});
//		possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5),
//				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5)});

//        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5)});
//        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5)});
//        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5)});
//        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5)});
//        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
//                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5)});

        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5),
				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
				new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});

        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});

        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});

        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});

        possibleCombinations.add(new MAPEDimensionGrammarFXML[]{
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.INTERESTING, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.STEP, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.DIVERSITY, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.CONFLICT, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_DEVICES, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_POINTS, 5),
                new MAPEDimensionGrammarFXML(GADimensionGrammar.GrammarDimensionTypes.PLOT_TWISTS, 5)});

        EventRouter.getInstance().registerListener(this, new AlgorithmDone(null, null, ""));

//        AlgorithmSetup.getInstance().setNarrativeConstraints(currentDungeon);
        //AlgorithmSetup.getInstance().setFakeNarrativeConstraints();
//        AlgorithmSetup.getInstance().setMarioConstraints();
		AlgorithmSetup.getInstance().setZTempleConstraints();
//		AlgorithmSetup.getInstance().setOcarinaConstraints();

        AlgorithmSetup.getInstance().setConstraintNarrativeStruct(false);
        AlgorithmSetup.getInstance().setSaveData(true);

//        runExperiment();
 //       runZOOTExperiment();
//        runMarioExperiment();
//        runZTempleExperiment();
//        runExperiment4();

        analyzeZOOTElite();
        analyzeZTempleElite();
        analyzeMarioElite();
        analyzeExp4Info();
//        sampleTest();

//        runPatternFinderExperiment();
//        runDerivativePatternExperiment();
//        runQualityExperiment();
//        runFitnessTest();
//        runCoherenceTest();
//        runSamples();
//        runSameTestSameAxiom();
//        runSpecialDerivativeIssue();
        if(true)
            return;

//        runTestRemoveNode_Connections();
//        runTestGhostConns_RepeatedID();
//        runExperiment();

        //CORE RULES
        productionRules.put("hero", new String[]{"5ma","neo","sh"});
        productionRules.put("conflict", new String[]{"covs","cose","cona","coso"});
        productionRules.put("enemy", new String[]{"emp","emp_enemy_hero","bad","dra"});
        productionRules.put("modifier", new String[]{"chk","mcg","mhq"});
        productionRules.put("basic", new String[]{"hero_conflict_enemy"});

        //The actual graph
        grammarGraph = new GrammarGraph();
        GrammarNode hero = new GrammarNode(0, TVTropeType.HERO);
        GrammarNode conflict = new GrammarNode(1, TVTropeType.CONFLICT);
        GrammarNode enemy = new GrammarNode(2, TVTropeType.ENEMY);

        hero.addConnection(conflict, 1);
        conflict.addConnection(enemy, 1);

        grammarGraph.nodes.add(hero);
        grammarGraph.nodes.add(conflict);
        grammarGraph.nodes.add(enemy);

        grammarGraph.computeAdjacencyMatrix(0);

        //PATTERN 1

        GrammarPattern pattern_1 = new GrammarPattern();
        GrammarGraph ipatt_1 = new GrammarGraph();
        GrammarGraph opatt_11 = new GrammarGraph();

        GrammarNode a = new GrammarNode(0, TVTropeType.ANY);
        GrammarNode b = new GrammarNode(1, TVTropeType.ANY);
        GrammarNode c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(b, 1);
        b.addConnection(c, 1);
        ipatt_1.nodes.add(a); ipatt_1.nodes.add(b); ipatt_1.nodes.add(c);

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(b, 1);
        a.addConnection(c, 1);
        opatt_11.nodes.add(a); opatt_11.nodes.add(b); opatt_11.nodes.add(c);

        pattern_1.setPattern(ipatt_1);
        pattern_1.addProductionRule(opatt_11);

        //PATTERN 2

        GrammarPattern pattern_2 = new GrammarPattern();
        GrammarGraph ipatt_2 = new GrammarGraph();
        GrammarGraph opatt_21 = new GrammarGraph();

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        a.addConnection(b, 1);
        ipatt_2.nodes.add(a); ipatt_2.nodes.add(b);

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
        c = new GrammarNode(2, TVTropeType.ANY);
        a.addConnection(c, 1);
        c.addConnection(b, 1);
        opatt_21.nodes.add(a); opatt_21.nodes.add(b); opatt_21.nodes.add(c);

        pattern_2.setPattern(ipatt_2);
        pattern_2.addProductionRule(opatt_21);

        //PATTERN 2

        GrammarPattern pattern_3 = new GrammarPattern();
        GrammarGraph ipatt_3 = new GrammarGraph();
        GrammarGraph opatt_31 = new GrammarGraph();

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
//        a.addConnection(b, 1);
        ipatt_3.nodes.add(a); ipatt_3.nodes.add(b);

        a = new GrammarNode(0, TVTropeType.ANY);
        b = new GrammarNode(1, TVTropeType.ANY);
//        c = new GrammarNode(2, TVTropeType.ANY);
//        a.addConnection(c, 1);
//        c.addConnection(b, 1);
        opatt_31.nodes.add(a);opatt_31.nodes.add(b);
//        opatt_31.nodes.add(b); opatt_31.nodes.add(c);

        pattern_3.setPattern(ipatt_3);
        pattern_3.addProductionRule(opatt_31);

        System.out.println("DISTANCE BETWEEN CORE AND PATTERN 3");
        grammarGraph.distanceBetweenGraphs(ipatt_3);

        //PATTERN RND URLE

        GrammarPattern rnd_pattern = createRule();

//        pattern_1.match(grammarGraph, 4);
        System.out.println("RULES::::\n");

        System.out.println("PATTERN 1:");
        System.out.println("INPUT:");
        System.out.println(pattern_1.pattern.toString());
        System.out.println("OUTPUT:");
        System.out.println(pattern_1.productionRules.get(0).toString());

        System.out.println("PATTERN 2:");
        System.out.println("INPUT:");
        System.out.println(pattern_2.pattern.toString());
        System.out.println("OUTPUT:");
        System.out.println(pattern_2.productionRules.get(0).toString());

        System.out.println("RND PATTERN!!!:::");

        System.out.println("INPUT:");
        System.out.println(rnd_pattern.pattern.toString());
        System.out.println("OUTPUT:");
        System.out.println(rnd_pattern.productionRules.get(0).toString());

        System.out.println("CURRENT GRAMMAR:");

        System.out.println(grammarGraph.toString());
        pattern_3.match(grammarGraph, 4);
        System.out.println(grammarGraph.toString());
        rnd_pattern.match(grammarGraph, 4);
        System.out.println(grammarGraph.toString());
        pattern_1.match(grammarGraph, 4);
        System.out.println(grammarGraph.toString());
        pattern_2.match(grammarGraph, 4);
        System.out.println(grammarGraph.toString());
//        ArrayList<GrammarGraph> perms = grammarGraph.getPermutations(2);
//        System.out.println(perms);

    }

    public GrammarPattern createRule()
    {
        GrammarPattern rndRule = new GrammarPattern();
        GrammarGraph pattern = new GrammarGraph();

        int node_amount = Util.getNextInt(1, 4);

        //Create the nodes
        for(int i = 0; i < node_amount; i++)
        {
            //I add any but maybe i shouldn't; lets try!
            pattern.addNode(TVTropeType.ANY);
        }

        //Add random connections
        for(int i = 0; i < node_amount; i++)
            addConnection(pattern);

        rndRule.setPattern(pattern);

        //Now create the production
        GrammarGraph production1 = new GrammarGraph();
        node_amount = Util.getNextInt(1, 4);

        for(int i = 0; i < node_amount; i++)
        {
            //I add any but maybe i shouldn't; lets try!
            production1.addNode(TVTropeType.ANY);
        }

        //Add random connections
        for(int i = 0; i < node_amount; i++)
            addConnection(production1);

        rndRule.addProductionRule(production1);
//        this.chromosome.add(rndRule);,

        return rndRule;

    }

    private GrammarGraph addConnection(GrammarGraph pat)
    {
        if(pat.nodes.size() >= 2)
        {
            int first_index = Util.getNextInt(0, pat.nodes.size());
            int second_index = Util.getNextInt(0, pat.nodes.size());
            GrammarNode first = pat.nodes.get(first_index);

            while( second_index == first_index)
                second_index = Util.getNextInt(0, pat.nodes.size());

            GrammarNode second = pat.nodes.get(second_index);

            //Fixme: This still needs more testing!
            if(!first.checkConnectionExists(second))
            {
                int connection_type = Util.getNextInt(0, 3);
                connection_type = 1;
                first.addConnection(second, connection_type);

                if(connection_type != 1)
                    second.addConnection(first, connection_type);
            }
        }

        return pat;
    }

    public void generateGraph()
    {

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
//        ns.expand(0, ns.axiom);


    }

    @Override
    public void ping(PCGEvent e) {
        if(e instanceof AlgorithmDone)
        {
            nextElite();
        }
    }
}
