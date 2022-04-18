package game.narrative;

import game.Dungeon;
import game.narrative.NarrativeFinder.HeroNodePattern;
import game.narrative.NarrativeFinder.NarrativePattern;
import game.narrative.NarrativeFinder.PlotDevicePattern;
import game.narrative.NarrativeFinder.VillainNodePattern;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class NarrativeStructureConstraints
{
    public int characters;
    public int villains;
    public int bosses;
    public int heroes;
    public int quest_items;
    public int treasures;

    public NarrativeStructureConstraints()
    {
    }

    public void testConstraints()
    {
        resetConstraints();
        this.heroes = 2;
        this.villains = 2;
        this.quest_items = 1;
        this.treasures = 1;
    }

    public void fakeConstraintsMario()
    {
        resetConstraints();
        this.heroes = 2;
        this.villains = 2;
        this.quest_items = 1;
        this.treasures = 1;
    }

    public void fakeConstraintsZeldaTemple()
    {
        resetConstraints();
        this.heroes = 2;
        this.villains = 2;
        this.quest_items = 2;
        this.treasures = 1;
    }

    public void fakeConstraintsOcarina()
    {
        resetConstraints();
        this.heroes = 4;
        this.villains = 1;
        this.quest_items = 1;
        this.treasures = 0;
    }

    public void setConstraintsFromDungeon(Dungeon current_dungeon)
    {
//        current_dungeon.getAllRooms()
        resetConstraints();

        this.characters += 1; // Hero!
        this.heroes += 1;

        //rest of heroes (maybe it can be more specific, no need to have heroes as civilians for instance)
        this.characters += current_dungeon.getAllNpcs();
        this.heroes += current_dungeon.getBountyHunters().size() +
                current_dungeon.getMages().size() +
                current_dungeon.getSoldiers().size();

        //Now lets add the enemies. Do we want enemies in general to add up to villains? or one type of enemy = one type of villain
        this.villains += current_dungeon.getEnemies().isEmpty() ? 0 : 1;
        this.characters += current_dungeon.getEnemies().isEmpty() ? 0 : 1;

        //nowo big bosses! (which count as well as villains!)
        this.villains += current_dungeon.getBosses().size();
        this.bosses += current_dungeon.getBosses().size();

        //Add info about items & treasure, which can be used as PDs!!
        this.quest_items += current_dungeon.getItems().size();
        this.treasures += current_dungeon.getTreasures().isEmpty() ? 0 : 1;
    }

    private void resetConstraints()
    {
        this.characters = 0;
        this.villains = 0;
        this.bosses = 0;
        this.heroes = 0;
        this.quest_items = 0;
        this.treasures = 0;
    }

    public int isViolatingConstraints(GrammarGraph generated_structure)
    {
        int violation_count = 0;

        /**
         * Missing the code that assess if any violation is ongoing!
         */
        ArrayList<HeroNodePattern> contained_heroes = generated_structure.pattern_finder.getAllPatternsByType(HeroNodePattern.class);
        ArrayList<VillainNodePattern> contained_villains = generated_structure.pattern_finder.getAllPatternsByType(VillainNodePattern.class);
        ArrayList<PlotDevicePattern> contained_plot_devices = generated_structure.pattern_finder.getAllPatternsByType(PlotDevicePattern.class);

        int simple_heroes = contained_heroes.size();
        int simple_villains = contained_villains.size();
        int simple_characters = simple_heroes + simple_villains;
        int simple_plot_devices = contained_plot_devices.size();

        simple_heroes = 0;
        simple_villains = 0;
        simple_characters = 0;
        simple_plot_devices = contained_plot_devices.size(); //Actually, I will need to get back here!  characters could also be plot devices!

        ArrayList<NarrativePattern> micro_pats = generated_structure.pattern_finder.getAllMicros();
        ArrayList<NarrativePattern> counted_character = new ArrayList<NarrativePattern>();

        //FIXME: I think reveal pattern is working now!
        for(NarrativePattern micro_pat : micro_pats)
        {
            //not counted as character!!
            if(!micro_pat.faction && !micro_pat.ambiguous && !counted_character.contains(micro_pat))
            {
                if(micro_pat instanceof HeroNodePattern)
                {
                    simple_characters++;
                    simple_heroes++;

                    if(micro_pat.revealed != null)
                        counted_character.add(micro_pat.revealed);

                    counted_character.add(micro_pat);
                }
                else if(micro_pat instanceof VillainNodePattern)
                {
                    simple_characters++;
                    simple_villains++;

                    if(micro_pat.revealed != null)
                        counted_character.add(micro_pat.revealed);

                    counted_character.add(micro_pat);
                }
            }
        }

//        System.out.println("THIS GRAPH have: " + simple_heroes + " heroes");
//        System.out.println("THIS GRAPH have: " + simple_villains + " villains");
//        System.out.println("THIS GRAPH have: " + simple_plot_devices + " plot devices");

        violation_count += simple_heroes <= this.heroes ? 0 : simple_heroes - this.heroes;
        violation_count += simple_villains <= this.villains ? 0 : simple_villains - this.villains;
        violation_count += simple_plot_devices <= this.treasures + this.quest_items ? 0 : simple_plot_devices - (this.treasures + this.quest_items);

//        System.out.println("THIS GRAPH ONLY HAS: " + violation_count + " violations!");


        return violation_count;
    }

    public int[] testingViolatingConstraints(GrammarGraph generated_structure)
    {
        int[] info_to_send = new int[4];
        int violation_count = 0;

        /**
         * Missing the code that assess if any violation is ongoing!
         */
        ArrayList<HeroNodePattern> contained_heroes = generated_structure.pattern_finder.getAllPatternsByType(HeroNodePattern.class);
        ArrayList<VillainNodePattern> contained_villains = generated_structure.pattern_finder.getAllPatternsByType(VillainNodePattern.class);
        ArrayList<PlotDevicePattern> contained_plot_devices = generated_structure.pattern_finder.getAllPatternsByType(PlotDevicePattern.class);

        int simple_heroes = contained_heroes.size();
        int simple_villains = contained_villains.size();
        int simple_characters = simple_heroes + simple_villains;
        int simple_plot_devices = contained_plot_devices.size();

        simple_heroes = 0;
        simple_villains = 0;
        simple_characters = 0;
        simple_plot_devices = contained_plot_devices.size(); //Actually, I will need to get back here!  characters could also be plot devices!

        ArrayList<NarrativePattern> micro_pats = generated_structure.pattern_finder.getAllMicros();
        ArrayList<NarrativePattern> counted_character = new ArrayList<NarrativePattern>();

        //FIXME: I think reveal pattern is working now!
        for(NarrativePattern micro_pat : micro_pats)
        {
            //not counted as character!!
            if(!micro_pat.faction && !micro_pat.ambiguous && !counted_character.contains(micro_pat))
            {
                if(micro_pat instanceof HeroNodePattern)
                {
                    simple_characters++;
                    simple_heroes++;

                    if(micro_pat.revealed != null)
                        counted_character.add(micro_pat.revealed);

                    counted_character.add(micro_pat);
                }
                else if(micro_pat instanceof VillainNodePattern)
                {
                    simple_characters++;
                    simple_villains++;

                    if(micro_pat.revealed != null)
                        counted_character.add(micro_pat.revealed);

                    counted_character.add(micro_pat);
                }
            }
        }

        System.out.println("THIS GRAPH have: " + simple_heroes + " heroes");
        System.out.println("THIS GRAPH have: " + simple_villains + " villains");
        System.out.println("THIS GRAPH have: " + simple_plot_devices + " plot devices");

        violation_count += simple_heroes <= this.heroes ? 0 : simple_heroes - this.heroes;
        violation_count += simple_villains <= this.villains ? 0 : simple_villains - this.villains;
        violation_count += simple_plot_devices <= this.treasures + this.quest_items ? 0 : simple_plot_devices - (this.treasures + this.quest_items);

        System.out.println("THIS GRAPH ONLY HAS: " + violation_count + " violations!");

        info_to_send[0] = violation_count;
        info_to_send[1] = simple_heroes;
        info_to_send[2] = simple_villains;
        info_to_send[3] = simple_plot_devices;

        return info_to_send;
    }
}
