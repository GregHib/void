package world.gregs.voidps.tools.map.obj

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.data.ObjectDefinitionFull
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull
import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.tools.cache.Xteas
import world.gregs.voidps.tools.map.MapDecoder
import world.gregs.voidps.type.Delta
import world.gregs.voidps.type.Region
import world.gregs.voidps.type.Tile
import world.gregs.voidps.type.Zone
import world.gregs.voidps.type.area.Rectangle
import world.gregs.voidps.type.equals

object DungeoneeringDumper {

    data class RoomData(
        var zone: Zone = Zone.EMPTY,
        var complexity: Int = 0,
        var type: String? = null,
        var doors: BooleanArray = BooleanArray(4),
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as RoomData

            if (complexity != other.complexity) return false
            if (type != other.type) return false
            if (!doors.contentEquals(other.doors)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = complexity
            result = 31 * result + (type?.hashCode() ?: 0)
            result = 31 * result + doors.contentHashCode()
            return result
        }
    }

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val xteas = Xteas()
        val decoder = ObjectDecoderFull(members = false, lowDetail = false).load(cache)
        val mapDecoder = MapDecoder(xteas)
        val maps = mapDecoder.load(cache)

        val dungeons = Rectangle(64, 1926, 655, 5583)
        val zones = mutableMapOf<Int, RoomData>()
        for (map in maps) {
            val region = Region(map.id)
            if (region.tile !in dungeons) {
                continue
            }
            if (map.objects.isEmpty()) {
                continue
            }
            for (obj in map.objects) {
                val def = decoder.getOrNull(obj.id) ?: continue
                val tile = region.tile.add(obj.x, obj.y, obj.level)
                val zone = tile.zone
                if (zone.level != 0) {
                    continue
                }
                val roomX = tile.x / 16
                val roomY = tile.y / 16
                val room = Tile(roomX * 16, roomY * 16).zone
                val data = zones.getOrPut(room.id) { RoomData() }
                val puzzle = puzzle(tile)
                if (puzzle != null) {
                    data.complexity = 5
                    data.type = puzzle
                }
                if (def.interactive == 1 || def.options != null) {
                    val comp = complexity(def)
                    if (comp > data.complexity) {
                        data.complexity = comp
                    }
                    val t = type(def, tile)
                    if (t != null) {
                        data.type = t
                    }
                    doors(def, tile, roomX, roomY, data.doors)
                }
            }
        }
        for ((zone, data) in zones) {
            println("${zone} complexity=${data.complexity} type=${data.type} ${Zone(zone).tile} ${if(data.doors[WEST]) "w" else ""}${if(data.doors[NORTH]) "n" else ""}${if(data.doors[EAST]) "e" else ""}${if(data.doors[SOUTH]) "s" else ""}")
        }
        println("Zones ${zones.size}")
    }

    private fun puzzle(tile: Tile): String? {
        if (tile.y !in 4225..<4847) {
            return null
        }
        val x = (tile.zone.x - 8) / 2
        val y = (tile.y - 4225) / 73
        return when (x + 1) {
            1 -> "monolith"
            2 -> "collapsing_room"
            3 -> "crystal_puzzle"
            4 -> "ghosts"
            5 -> "three_statue_weapon"
            6 -> "follow_the_leader"
            7 -> "toxin_maze"
            8 -> when (y) {
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
            37 -> when (y) {
                3 -> "coloured_bookcases"
                else -> null
            }
            else -> null
        }
    }

    private const val WEST = 0
    private const val NORTH = 1
    private const val EAST = 2
    private const val SOUTH = 3

    private fun doors(def: ObjectDefinitionFull, tile: Tile, roomX: Int, roomY: Int, array: BooleanArray) {
        if (def.name != "Door") {
            return
        }
        val delta = tile.delta(roomX * 16, roomY * 16)
        when (delta) {
            Delta(0, 7) -> array[WEST] = true
            Delta(7, 15) -> array[NORTH] = true
            Delta(15, 7) -> array[EAST] = true
            Delta(7, 0) -> array[SOUTH] = true
            else -> return
        }
    }

    private fun type(def: ObjectDefinitionFull, tile: Tile): String? {
        return when (def.name) {
            "Dungeon exit" -> "start"
            "Table" -> "start"
            "Group gatestone portal" -> "start"
            "Boss door" -> "boss"
            else -> puzzle(tile)
        }
    }

    private fun complexity(def: ObjectDefinitionFull): Int {
        return when (def.name) {
            "Wall" -> 2
            "Cooking range" -> 2
            "Runecrafting altar" -> 3
            "Furnace" -> 3
            "Anvil" -> 3
            "Spinning wheel" -> 4
            "Summoning obelisk" -> 5
            "Water trough" -> 5
            "Group gatestone portal" -> 5
            else -> -1
        }
    }

}
