package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.MapObject
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.tools.map.MapDecoder
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.area.Rectangle

object DungeoneeringDumper {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val xteas = Xteas()
        val decoder = ObjectDecoderFull(members = false, lowDetail = false).load(cache)
        val mapDecoder = MapDecoder(xteas)
        val maps = mapDecoder.load(cache)

        var set = mutableSetOf<String>()
        val dungeons = Rectangle(64, 1926, 655, 5583)
        for (map in maps) {
            val region = Region(map.id)
            if (region.tile !in dungeons) {
                continue
            }
            val zone = region.tile.zone
//            for (zone in region.tile.toCuboid(64, 64).toZones(0)) {
            var complexity = -1
            var type: String? = null
            for (obj in map.objects) {
                val def = decoder.getOrNull(obj.id) ?: continue
                if (def.interactive == 1) {
                    val tile = zone.tile.add(obj.x, obj.y, obj.level)
                    val t = type(def)
                    if (t != null) {
                        type = t
                    }
                    val comp = complexity(def)
                    if (comp > complexity) {
                        complexity = comp
                    }
                    if (set.add(def.name)) {
                        println("${def.id} ${tile} ${def.name} ${def.varbit} ${def.transforms?.toList()}")
                    }
                }
            }
            println("${zone} complexity=${complexity} type=${type}")
//            }
        }
//        for (zoneX in 1..10) {
//            for (zoneY in 30..87) {
    }

    private fun puzzle(tile: Tile): String? {
        if (tile.y !in 4225..<4847) {
            return null
        }
        val x = (tile.zone.x - 8) / 2
        val y = (tile.y - 4425) / 73
        return when (x) {
            1 -> "monolith"
            2 -> "collapsing_room"
            3 -> "crystal_puzzle"
            4 -> "ghosts"
            5 -> "three_statue_weapon"
            6 -> "follow_the_leader"
            7 -> "toxin_maze"
            8 -> when(y) {
                1 -> "seeker_sentinel"
                2 -> "sleeping_guards"
                3 -> "coloured_bookcases"
                4 -> "portal_maze"
                else -> "icy_pressure_pad"
            }
            9 -> "flip_tiles"
            10 -> "fremennik_camp"
            11 -> "ten_statue_weapon"
            12 -> "lodestone_power"
            13 -> "fishing_ferret"
            14 -> "suspicious_grooves"
            15 -> "agility_maze"
            16 -> "levers"
            17 -> "barrel_puzzle"
            18 -> "sliding_statues"
            19 -> "hunter_ferret"
            20 -> "magic_construct"
            21 -> "enigmatic_hoardstalker"
            22 -> "flower_roots"
            23 -> "poltergeist"
            24 -> "keystone_bridge"
            25 -> "grapple_tightrope"
            26 -> "pondskaters"
            27 -> "unhappy_ghost"
            28 -> "broken_plank_bridge"
            29 -> "statue_bridge"
            30 -> "return_the_flow"
            31 -> "coloured_recess"
            32 -> "winch_bridge"
            33 -> "mercenary_leader"
            34 -> "ramokee_familiars"
            35 -> "sliding_block"
            36 -> "coloured_ferrets"
            else -> null
        }
    }
    private fun type(def: ObjectDefinitionFull): String? {
        return when (def.name) {
            "Dungeon exit" -> "start"
            "Table" -> "start"
            "Group gatestone portal" -> "start"
            else -> null
        }
    }

    private fun complexity(def: ObjectDefinitionFull): Int {
        return when (def.name) {
            "Wall" -> 2
            "Cooking range" -> 2
            "Runecrafting Altar" -> 3
            "Furnace" -> 3
            "Anvil" -> 3
            "Spinning Wheel" -> 4
            "Summoning obelisk" -> 5
            "Water trough" -> 5
            "Group gatestone portal" -> 5
            else -> -1
        }
    }

}
