package world.gregs.voidps.tools.convert

import java.io.File

/**
 * Moves item/npc/object spawns into region based files
 */
object SpawnSorter {
    @JvmStatic
    fun main(args: Array<String>) {
        val type = ".npc-spawns.toml"
        val file = File("../void/data/npcs/spawns/all${type}")

        val lines = file.readLines()
        val directory = file.parentFile
        var region = 0
        val files = mutableSetOf<File>()
        for (line in lines) {
            if (line.trim(' ').startsWith("#")) {
                region = line.trim('#').trim().toInt()
                val file = directory.resolve("${regionName(region)}$type")
                if (file.exists()) {
                    file.appendText("# ${region}\n")
                } else {
                    file.createNewFile()
                    file.appendText("spawns = [\n# $region\n")
                    files.add(file)
                }
                continue
            }
            if (!line.startsWith(" ")) {
                continue
            }
            val line = line.trim(' ')
            if (line.startsWith("{")) {
                val parts = line.trimStart('{').trimEnd('}').split(",", "=").map { it.trim() }
                val name = regionName(region)
                val file = directory.resolve("${name}$type")
                if (file.exists()) {
                    file.appendText("  ${line}\n")
                } else {
                    println("Not found!")
                }
                println("${regionName(region)} $parts")
            }
        }
        for (file in files) {
            file.appendText("]")
        }
    }

    private fun regionName(region: Int): String = when (region) {
        9882 -> "grand_tree_tunnels"
        9516 -> "mobilising_armies"
        10390 -> "clocktower_dungeon"
        10134, 10391 -> "ardougne_sewers"
        8527, 8528 -> "braindeath_island"
        10392 -> "chaos_druid_tower"
        13636, 10811 -> "rellekka_hunter_area"
        11065 -> "mountain_camp"
        10569 -> "fisher_realm"
        10803 -> "witchaven"
        10804 -> "legends_guild"
        10805 -> "sorcerers_tower"
        10835, 10834 -> "dorgesh_kaan"
        10833 -> "dorgesh_kaan_agility_course"
        10807 -> "sinclair_mansion"
        10575, 10831 -> "shadow_dungeon"
        10810, 10809, 10552, 10296, 10297, 10553, 10554, 10551, 11064, 10808 -> "rellekka"
        10647, 10646 -> "ardougne_sewers_mine"
        10028, 10284 -> "oo_glog"
        13365 -> "digsite"
        13364 -> "exam_centre"
        10326 -> "as_a_first_resort"
        9772, 9773, 10029, 10285, 10030, 10286, 9774, 10542 -> "feldip_hills"
        9779, 9623, 9879, 9878, 9369, 9370 -> "underground_pass"
        10035 -> "west_ardougne"
        9778 -> "ourania"
        10137 -> "glarials_tomb"
        10393 -> "goblin_cave"
        10394, 10138 -> "waterfall_dungeon"
        9811, 10067 -> "easter"
        9780 -> "outpost"
        9802 -> "kiss_the_frog"
        10034 -> "battlefield"
        10290 -> "monastery"
        8792 -> "lava_flow_mine"
        10291, 10547, 10292, 10548 -> "east_ardougne"
        10546 -> "tower_of_life"
        10545 -> "port_khazard"
        11060, 11316 -> "entrana"
        11062 -> "camelot_castle"
        10289 -> "khazard_fight_arena"
        10033 -> "tree_gnome_village"
        10032, 10288 -> "yanille"
        10036 -> "combat_training_camp"
        9782 -> "grand_tree"
        9285, 9541, 9540, 9797 -> "zanaris"
        9622 -> "underground_pass_dungeon"
        10040 -> "lighthouse"
        10039 -> "barbarian_outpost"
        10038, 10037 -> "baxtorian_falls"
        10293 -> "fishing_guild"
        10549 -> "ranging_guild"
        10550 -> "mcgrubors_wood"
        10294 -> "dwarven_outpost"
        9781, 9525, 9526 -> "tree_gnome_stronghold"
        9267 -> "arandar"
        9524 -> "arandar_pass"
        7757 -> "blast_furnace"
        9270, 10911, 8013 -> "eagels_peak"
        11320, 11576 -> "death_plateau"
        11577 -> "trollheim"
        11151, 10894, 11150, 10794, 11050, 10795, 11051 -> "ape_atoll"
        11068 -> "trollweiss"
        11314, 11315 -> "crandor"
        11322 -> "troll_country"
        11321 -> "troll_stronghold"
        9362 -> "observatory_dungeon"
        9033 -> "king_black_dragons_lair"
        12338, 12339, 12340 -> "draynor"
        12439, 12438 -> "draynor_sewers"
        12337, 12437 -> "wizards_tower"
        12950 -> "lumbridge_basement"
        12595 -> "freds_farm"
        10648 -> "temple_of_ikov_dungeon"
        12850, 12594, 12851 -> "lumbridge"
        12849, 12593 -> "lumbridge_swamp"
        13105, 13106, 13107 -> "al_kharid"
        12193 -> "deep_wilderness_dungeon"
        13108, 12852, 12596, 13109, 12853, 12597, 13110, 12854, 12697 -> "varrock"
        12598 -> "grand_exchange"
        15148 -> "harmony"
        13720 -> "nature_spirit"
        11418 -> "mountain_dwarf_coloney"
        11925, 12181 -> "asgarnian_ice_dungeon"
        14132, 14388 -> "darkmeyer"
        14387, 14385, 14386 -> "meiyerditch"
        12341 -> "barbarian_village"
        12342, 12086 -> "edgeville"
        10133 -> "gnome_village_dungeon"
        12089, 12088 -> "wilderness_bandit_camp"
        11831, 12087, 12343, 12599, 12855, 13111, 13367, 11832, 12344, 12600, 12856, 13112,
        13368, 11833, 12857, 13113, 13369, 11834, 12090, 12858, 13114, 13370, 11835, 12091,
        12347, 12603, 12859, 13115, 13371, 11836, 12092, 12348, 12604, 12860, 13116, 13372, 12093, 12349, 12605, 12861, 13117 -> "wilderness"
        12345, 12601, 12346, 12602 -> "bounty_hunter"
        11837 -> "wilderness_agility_course"
        12082, 12081 -> "port_sarim"
        11825, 11824 -> "mudskipper_point"
        11826, 11570 -> "rimmington"
        10389, 10388 -> "yanille_agility_dungeon"
        10131, 10387, 10287 -> "gu_tanoth"
        11571, 11827, 12083, 11572, 11828, 12084, 11829 -> "falador"
        11573 -> "taverley"
        11575, 11574, 11319 -> "burthorpe"
        11318 -> "white_wolf_mountain"
        11601, 11317, 11061 -> "catherby"
        10806 -> "seers_village"
        11830 -> "goblin_village"
        11926 -> "rat_pits"
        12436 -> "tutorial_island"
        11423, 11422, 11679, 11678 -> "keldagrim"
        13104 -> "shantay_pass"
        14135 -> "fenkenstrains_castle"
        14486 -> "in_search_of_the_myreque"
        14232, 14488, 14487, 14233, 13977, 13978, 13721 -> "meiyerditch_tunnels"
        12440 -> "gnome_glider_hangar"
        11937 -> "wilderness_agility_course"
        11588, 11844 -> "corporeal_beasts_lair"
        11842, 11586, 11587, 11843, 11330, 11075, 10819, 11076, 11332 -> "spirit_realm"
        7505, 8017, 8530, 9297 -> "stronghold_of_security"
        9551, 9552, 9808, 10064 -> "tzhaar_city"
        6993, 6992 -> "giant_mole_lair"
        6482 -> "kuradals_dungeon"
        12107 -> "abyss"
        7502 -> "exam_centre"
        7754, 8261 -> "random_event"
        8763 -> "pirates_cove"
        8252, 8253, 8509, 8508 -> "lunar_isle"
        9275 -> "neitiznot"
        9531 -> "jatizso"
        9777 -> "observatory"
        10044, 10300, 10144, 10400 -> "miscellania"
        10042 -> "waterbirth_island"
        9273 -> "piscatoris"
        8754 -> "iorwerth_camp"
        8753 -> "tyras_camp"
        9265 -> "lletya"
        8496 -> "port_tyras"
        9009, 9010, 8498, 9497 -> "isafdar"
        9520, 9776 -> "castle_wars"
        9775 -> "jiggig"
        10031 -> "gu_tanoth"
        11569 -> "musa_point"
        10802, 10801 -> "karamja"
        11313 -> "karamja_volcano"
        11057, 11058 -> "brimhaven"
        11056, 11055 -> "tai_bwo_wannai"
        11823 -> "karamja_ship_yard"
        11312, 11311, 11568, 11567, 11054, 11566, 11822 -> "karamja"
        11310 -> "shilo_village"
        11053, 11309, 11565, 11821 -> "kharazi_jungle"
        12080, 12079, 12335, 12336, 12592 -> "tutorial_island"
        12591 -> "bedabin_camp"
        13103, 13203 -> "desert_mining_camp"
        12590 -> "bandit_camp"
        13358 -> "pollnivneach"
        13613 -> "nardah"
        13872, 10828 -> "ruins_of_uzer"
        13099 -> "sophanem"
        7749 -> "pyramid_plunder"
        13356 -> "agility_pyramid"
        12589 -> "quarry"
        13874, 13873, 14129, 14130 -> "burgh_de_rott"
        13875 -> "mort_ton"
        13618, 11078, 11079 -> "abandoned_mine"
        13362 -> "duel_arena"
        11088 -> "sophanem_dungeon"
        13363 -> "mage_training"
        14131, 14231 -> "barrows"
        13619, 13620, 13876, 13621, 13877 -> "mort_myre_swamp"
        13878 -> "canifis"
        10841, 11097 -> "christmas"
        11417, 11673, 11416, 11928 -> "taverley_dungeon"
        11343, 7753 -> "rat_catchers"
        7507 -> "recipe_for_disaster"
        7510 -> "demon_slayer"
        7243 -> "cabin_fever"
        6743 -> "fist_of_guthix"
        7496 -> "mournings_end_part_2"
        11414 -> "crandor_dungeon"
        11668 -> "rashiliyias_tomb"
        11670 -> "melzars_maze"
        11671 -> "black_knights_base"
        11413 -> "karamja_dungeon"
        13622 -> "paterdomus_temple"
        13623 -> "slayer_tower"
        14134, 14390 -> "haunted_woods"
        14646, 14647, 14902 -> "port_phasmatys"
        14638, 14894, 14895, 14639 -> "mos_le_harmless"
        15151, 15150 -> "trouble_brewing"
        13131 -> "ourania_cave"
        12629, 12885, 13141, 12630, 12886, 13142 -> "chaos_tunnels"
        12954, 13210, 12698 -> "varrock_sewers"
        12444, 12443, 12442, 12441 -> "edgeville_dungeon"
        11929, 12185, 12184 -> "dwarven_mine"
        12870 -> "swept_away"
        9625 -> "brimstails_home"
        else -> region.toString()
    }
}