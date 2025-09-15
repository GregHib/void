package world.gregs.voidps.tools

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import net.pearx.kasechange.toSnakeCase
import net.pearx.kasechange.toTitleCase
import world.gregs.config.Config
import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.config.decoder.RenderAnimationDecoder
import world.gregs.voidps.cache.definition.decoder.NPCDecoder
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.configFiles
import world.gregs.voidps.engine.data.definition.AmmoDefinitions
import world.gregs.voidps.engine.data.definition.CategoryDefinitions
import world.gregs.voidps.engine.data.definition.NPCDefinitions
import world.gregs.voidps.engine.data.definition.ParameterDefinitions
import world.gregs.voidps.engine.data.find
import world.gregs.voidps.engine.data.list
import world.gregs.voidps.tools.convert.SpawnSorter
import world.gregs.voidps.type.Direction
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.area.Rectangle
import java.io.File

object NPCDefinitions {
    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val files = configFiles()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val categories = CategoryDefinitions().load(files.find(Settings["definitions.categories"]))
        val ammo = AmmoDefinitions().load(files.find(Settings["definitions.ammoGroups"]))
        val parameters = ParameterDefinitions(categories, ammo).load(files.find(Settings["definitions.parameters"]))
        val definitions = NPCDecoder(true, parameters).load(cache)
        val decoder = NPCDefinitions(definitions).load(files.getValue(Settings["definitions.npcs"]))
        val renderAnimations = RenderAnimationDecoder().load(cache)
        val file = File("C:\\Users\\Greg\\Downloads\\rs3-spawns\\map_npcs.json")

        val objectMapper = ObjectMapper()
        val map: List<Map<String, Int>> = objectMapper.readValue(file)

        val baseMaps = mutableListOf<Pair<Rectangle, String>>()

        val base: List<Map<String, Any>> = objectMapper.readValue(File("C:\\Users\\Greg\\Downloads\\rs3-spawns\\basemaps.json"))
        for (map in base) {
            val bounds = map["bounds"] as List<List<Int>>
            val shape = if (bounds.size == 2) {
                Rectangle(bounds[0][0], bounds[0][1], bounds[1][0], bounds[1][1])
            } else {
                continue
            }
            if (map["name"] == "default" || map["name"] == "asgarnia_ice_cave" || map["name"] == "RuneScape Surface") {
                continue
            }
            baseMaps.add(shape to (map["name"] as String).toSnakeCase().replace("'", ""))
        }

        val existing = spawns(configFiles().list(Settings["spawns.npcs"]))
        val current = existing.associateBy { it.tile }
        println(existing.size)

        val unique = mutableMapOf<Int, String>()
        val names = mutableSetOf<String>()
        val grouped = mutableMapOf<String, MutableList<String>>()
        val npcs = File("C:\\Users\\Greg\\Downloads\\rs3-spawns\\npcids\\").listFiles()!!
        val rs3Defs: List<Map<String, Any>> = objectMapper.readValue(File("C:\\Users\\Greg\\Documents\\Void\\data\\rs3-bestiary.json"))
        val rs3Definitions = rs3Defs.associateBy { it["id"] as Int }
        val npcDefs = mutableMapOf<String, MutableList<String>>()
        var count = 0
        val regions = mutableMapOf<Int, String>()
        for (file in npcs) {
            val list: List<Map<String, Any>> = objectMapper.readValue(file)
            for (spawn in list) {
                val name = spawn["name"] as String
                val id = spawn["id"] as Int
                val x = spawn["x"] as Int
                val y = spawn["y"] as Int
                val p = spawn["p"] as Int
                val def = decoder.getOrNull(id) ?: continue
                if (def.name != name) {
                    val transforms = def.transforms?.map { decoder.getOrNull(it)?.name?.lowercase() }?.filterNotNull() ?: emptyList()
                    if (!transforms.contains(name.lowercase())) {
//                        println("Different ${def.id} ${def.name} $transforms $name")
                        continue
                    }
                }
                val tile = Tile(x, y, p)
                if (current[tile] == null) {
                    var place = SpawnSorter.regionName(tile.region.id)// ?: "default"// ?: area.firstOrNull()
                    if (place != null) {
                        continue
                    }
                    place = when (tile.region.id) {
                        9528 -> "piscatoris_falconry_area"
                        10129, 13717 -> "eagles_eyrie"
                        12944, 13200 -> "sophanem_dungeon"
                        13981, 13465, 13464 -> "varrock_dig_site_caves"
                        10057 -> "mage_arena_bank"
                        13463, 13462 -> "mage_training_arena"
                        12633 -> "senntisten_temple"
                        13145 -> "senntisten_dig"
                        6738 -> "dragon_forge"
                        6995 -> "ancient_cavern"
                        17753 -> "dagannoth_caves"
                        17755 -> "silass_dream"
                        17238 -> "korasis_dream"
                        18012 -> "rellekka_instance"
                        13366 -> "silvarea"
                        8524 -> "draynor_instance"
                        7250 -> "ancient_cavern_cave"
                        12848 -> "kalphite_hive"
                        12847, 12846, 13359, 13357, 13101, 12845, 12844, 13100  -> "kharidian_desert"
                        13616 -> "uzer_hunter_area"
                        10322 -> "barbarian_assault_lobby"
                        7244 -> "crash_site"
                        13368, 13624, 13880, 13881, 13882, 13625, 13626, 13627, 13883 -> "deamonheim"
                        9621 -> "underground_path"
                        11675, 11419 -> "ice_queens_lair"
                        11166, 11421, 11676 -> "troll_stronghold_tunnels"
                        11421 -> "keldagrim_entrance_tunnel"
                        7499, 7755, 8011, 8012 -> "fishing_trawler"
                        1858, 1859, 1860, 1861, 1862, 1863, 1864, 1865, 1866, 1867, 9047, 2377, 2370, 2371, 2372, 2373, 2374, 2375, 2376, 2378, 2379, 850, 9559, 1602, 1603, 1604, 1605, 1606, 1607, 1608, 1609, 1610, 1611, 334, 335, 336, 337, 338, 339, 340, 341, 342, 343,848,1346, 1347, 1348, 1349, 1350, 1351, 1352, 1353, 1354, 1355, 835, 837, 839, 841, 843 -> "dungeoneering"
                        8519, 8521, 8009, 8520 -> "ibans_lair"
                        12891, 12892, 13147, 13148, 13403, 13404 -> "nomads_temple"
                        13656 -> "ice_cave"
                        12186 -> "ramarnos_forge"
                        13121 -> "spirit_realm"
                        11356 -> "clan_wars"
                        14167 -> "circus"
                        13393, 13138, 13650 -> "meeting_history"
                        6741 -> "runecrafting_guild"
                        13212, 12956 -> "chaos_temple_dungeon"
                        7218, 7474, 7730, 7473 -> "soul_wars"
                        13361 -> "desert_strykewyrm"
                        14678, 14934, 14679, 14935 -> "spirit_plane"
                        10584, 10327 -> "xmas_2009"
                        11681, 11682 -> "ghorrock_fortress_dungeon"
                        10907, 10908, 11164 -> "fremennik_dungeon"
                        17500 -> "fremennik_spiritual_realm"
                        9545 -> "kharazi_caves"
                        8025 -> "land_of_snow"
                        10329, 10074, 10330, 10073 -> "ancient_guthix_temple"
                        10075, 10331  -> "guthixian_temple"
                        10544 -> "hazelmeres_island"
                        7490 -> "black_knights_fortress_basement"
                        12188, 12187, 12189 -> "black_knights_catacombs"
                        12948 -> "tears_of_guthix_cavern"
                        13647 -> "while_guthix_sleeps"
                        8004 -> "movarios_base"
                        11579 -> "luciens_camp"
                        13645 -> "dragonkin_castle"
                        9263, 9519 -> "castle_wars"
                        10650, 10649, 10905 -> "temple_of_ikov"
                        7511 -> "defender_of_varrock"
                        17756 -> "fremennik_fleet"
                        10821, 13718 -> "haunted_mine"
                        10906 -> "elemental_workshop"
                        12946, 13202 -> "smoke_dungeon"
                        13722 -> "paterdomus"
                        14133 -> "mort_myre_swamp"
                        8010 -> "mime_event"
                        8008, 7751 -> "temple_of_light"
                        5961 -> "chaos_dwarf_battlefield"
                        11593, 14426 -> "yanille_instance"
                        12117 -> "snake_pit"
                        15259 -> "dragontooth_shipwreck"
                        11924 -> "underwater_cave"
                        7236, 7492, 7748 -> "waterbirth_dungeon"
                        11304, 14425 -> "desert_island"
                        14420 -> "soul_wars_tutorial"
                        12102 -> "enchanted_valley"
                        12621 -> "lumbridge_swamp_mine"
                        10062 -> "lumbridge_cellar"
                        11153 -> "kharazi_caves"
                        12619 -> "camo_event"
                        11930 -> "recipe_for_disaster"
                        6478 -> "battle_of_the_archipelago"
                        13458, 13459 -> "pollninveach_dungeon"
                        14170 -> "red_raktuber"
                        8280, 9553, 9809, 10065 -> "tzhaar_caves"
                        11672, 11416, 11928, 11673, 11417 -> "taverley_dungeon"
                        9016 -> "piscatoris"
                        9271, 9272, 9272 -> "piscatoris_hunter_area"
                        10903, 9295 -> "witchaven_dungeon"
                        10658 -> "penguin_outpost"
                        9527 -> "tree_gnome_stronhold"
                        10558, 10814, 10559 -> "iceberg"
                        14672, 14671, 14415, 14416 -> "living_rock_caverns"
                        10900 -> "brimhaven_dungeon"
                        4934 -> "chaos_tunnels_resource_dungeon"
                        8014, 10318 -> "temple_trekking"
                        6219, 5965 -> "uzer_mastaba"
                        13871 -> "uzer"
                        8022 -> "yewnocks_cave"
                        9264 -> "poison_waste"
                        6212 -> "evil_chicken_lair"
                        11602, 11347, 11603, 11346, 11345, 11601, 11347 -> "god_wars_dungeon"
                        10910 -> "brine_rat_caverns"
                        8276 -> "easter_2007"
                        13209 -> "tolnas_rift"
                        7234 -> "courtroom"
                        6722 -> "keep_le_faye"
                        10307 -> "puro_puro"
                        9046 -> "mouse_hole"
                        7745, 8001, 8002 -> "poison_waste_slayer_dungeon"
                        11074 -> "goblin_temple"
                        6985 -> "goblin_cave"
                        9276, 9532 -> "fremennik_isle"
                        12616, 12615 -> "lair_of_tarn_razorlor"
                        9632 -> "ice_troll_caves"
                        11605 -> "sorceress_garden"
                        8278 -> "the_lady_lumbridge"
                        11425 -> "jhallans_resting_place"
                        9810 -> "easter_2009"
                        6473 -> "minesweeper"
                        10135 -> "southern_ardougne_sewer"
                        4679 -> "karamja_volcano_resource_dungeon"
                        10583 -> "xmas_2009"
                        10305 -> "zaniks_lab"
                        13611, 13612, 13355 -> "ullek"
                        13456, 13712, 13968 -> "scabaras_dungeon"
                        7248, 7247, 6991 -> "dream_world"
                        12625 -> "tunnel_of_chaos"
                        9814 -> "dorgesh_kaan_station"
                        10070 -> "dorgesh_kaan_station_construction_site"
                        11059 -> "fishing_platform"
                        10642 -> "rantzs_house"
                        4935 -> "baxtorian_falls"
                        15248 -> "windmill_cellar"
                        12100 -> "tower_of_life"
                        7509, 7508 -> "barbarian_assault"
                        11666, 11665 -> "kharazi_caves"
                        12627 -> "gublinch_cave"
                        7500 -> "captain_barnabys_ship"
                        9032 -> "meiyerditch_daeyalt_mine"
                        8525 -> "eyes_of_glouphrie_battlefield"
                        9029, 6210 -> "fairy_hq"
                        9796 -> "zanaris"
                        8267 -> "cosmic_entity_plane"
                        9377 -> "lunar_isle_mine"
                        15251, 14995, 14994 -> "mos_leharmless_caves"
                        10321 -> "ham_store_rooms"
                        7758 -> "pinball"
                        18522, 18524, 19034, 19036 -> "thieves_guild"
                        13972, 14228 -> "kalphite_hive"
                        10136 -> "western_ardougne_sewer"
                        10577 -> "killerwatt_plane"
                        12433 -> "enakhras_temple"
                        13461 -> "river_elid_cave"
                        9874, 9875 -> "jiggig_dungeon"
                        13098, 19714, 14990, 13129, 12190, 18355, 18612, 13716, 9301, 19023, 18767, 18768, 18511, 18512, 19024, 18255, 9829, 8284, 8799, 8543, 7750, 12357, 8542, 8798, 16484, 5955, 12446, 17587, 7764, 21284, 22566, 21028, 22050, 22304, 21285, 12842, 18611, 10595, 4444, 11107, 9066, 9303, 8803, 8547, 11160, 8291, 8035, 3413, 5192, 22054, 21799, 14950, 17743, 8036, 8288, 10585, 12445, 11364, 5496, 11927, 8037, 5450, 8293, 8549, 8805, 19858, 9312, 8804, 8801, 8548, 8292 -> "null"
                        else -> "default"
                    }
                    if (place == "default") {
                        regions.putIfAbsent(tile.region.id, "$id $name $tile ${baseMaps.filter { it.first.contains(tile) }.map { it.second }}")
                    }
//                    println("$id $name $tile ${tile.region.id} ${baseMaps.filter { it.first.contains(tile) }.map { it.second }}")
                    place = "default"
                    count++
                    val rs3 = rs3Definitions[id]
                    var stringId = if (def.stringId != id.toString()) def.stringId else "${name.toSnakeCase().replace("'", "")}_${place.toSnakeCase()}"
                    if (!unique.containsKey(id)) {
                        for (i in 2..50) {
                            if (names.contains(stringId)) {
                                stringId = "${name.toSnakeCase().replace("'", "")}_${place.toSnakeCase()}_$i"
                            } else {
                                break
                            }
                        }
                        names.add(stringId)
                        unique[id] = stringId
                        println("$stringId = $id")
                        val list = npcDefs.getOrPut(place) { mutableListOf() }
                        if (def.stringId != stringId) {
                            list.add("[$stringId]")
                            list.add("id = $id")
                            if (rs3 != null) {
                                if (rs3["poisonous"] == "true") {
                                    list.add("poisonous = true")
                                }
                                if (rs3.contains("slayercat")) {
                                    list.add("categories = [\"${(rs3["slayercat"] as String).toSnakeCase()}\"]")
                                }
                                if (rs3.containsKey("description")) {
                                    list.add("examine = \"${rs3["description"]}\"")
                                }
                            }
                            list.add("")
                        }
                    }
                    grouped.getOrPut(place) { mutableListOf() }.add("  { id = \"${unique[id]}\", x = ${tile.x}, y = ${tile.y}${if (tile.level != 0) ", level = ${tile.level}" else ""}${if (rs3?.get("members") == "false") "" else ", members = ${if (rs3 != null) rs3["members"] as? Boolean ?: true else true}"} },")
                }
            }
        }
        for ((region, string) in regions) {
            println("$region - $string")
        }
        println("Spawns: $count")
        val output = File("./temp/spawns/")
        for ((area, spawns) in grouped) {
            val folder = output.resolve(area)
            folder.mkdirs()
            folder.resolve("${area}.npc-spawns.toml").writeText(buildString {
                appendLine("spawns = [")
                for (spawn in spawns) {
                    appendLine(spawn)
                }
                appendLine("]")
            })
        }
        for ((area, def) in npcDefs) {
            val folder = output.resolve(area)
            folder.mkdirs()
            folder.resolve("${area}.npcs.toml").writeText(buildString {
                for (spawn in def) {
                    appendLine(spawn)
                }
            })
        }
    }

    data class NPCSpawn(val id: String, val tile: Tile, val direction: Direction, val members: Boolean)

    fun spawns(paths: List<String>): List<NPCSpawn> {
        val list = mutableListOf<NPCSpawn>()
        val membersWorld = true
        for (path in paths) {
            Config.fileReader(path) {
                while (nextPair()) {
                    require(key() == "spawns")
                    while (nextElement()) {
                        var id = ""
                        var direction = Direction.NONE
                        var x = 0
                        var y = 0
                        var level = 0
                        var members = false
                        while (nextEntry()) {
                            when (val key = key()) {
                                "id" -> id = string()
                                "x" -> x = int()
                                "y" -> y = int()
                                "level" -> level = int()
                                "direction" -> direction = Direction.valueOf(string())
                                "members" -> members = boolean()
                                else -> throw IllegalArgumentException("Unexpected key: '$key' ${exception()}")
                            }
                        }
                        if (!membersWorld && members) {
                            continue
                        }
                        val tile = Tile(x, y, level)
                        list.add(NPCSpawn(id, tile, direction, members))
                    }
                }
            }
        }
        return list
    }
}
