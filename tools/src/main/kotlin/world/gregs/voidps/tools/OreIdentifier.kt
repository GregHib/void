package world.gregs.voidps.tools

import world.gregs.voidps.cache.Cache
import world.gregs.voidps.cache.CacheDelegate
import world.gregs.voidps.cache.definition.decoder.ObjectDecoderFull
import world.gregs.voidps.engine.data.Settings

object OreIdentifier {

    @JvmStatic
    fun main(args: Array<String>) {
        Settings.load()
        val cache: Cache = CacheDelegate(Settings["storage.cache.path"])
        val decoder = ObjectDecoderFull(members = false, lowDetail = false).load(cache)
        val map = mapOf(
            3184 to 1,
            3183 to 2,
            3195 to 3,
            1390 to 1,
            1391 to 2,
            37432 to 1,
            37387 to 1,
            45689 to 1,
            45683 to 2,
            45692 to 3,
            45690 to 1,
            45685 to 2,
            45682 to 3,
            48536 to 3,
        )
        val search = map.keys
        for (id in decoder.indices) {
            val def = decoder.getOrNull(id) ?: continue
            val models = def.modelIds ?: continue
            if (models.any { it.any { id -> search.contains(id) } } && def.contains("Mine")) {
                val single = id <= 2111
                val rockId = def.modifiedColours?.getOrNull(0)?.toUShort()?.toInt()
                val oreId = if (single) rockId else def.modifiedColours?.getOrNull(1)?.toUShort()?.toInt()
                val ore = if (oreId != null && !single && oreId == rockId) {
                    "depleted"
                } else {
                    when (oreId) {
                        55201 -> "gem"
                        0 -> "coal"
                        53, 80 -> "tin"
                        10508 -> "coal"
                        74, 57, 7366 -> "silver"
                        3776, 4645, 4037 -> "copper"
                        6589 -> "clay"
                        38086, 10583, 39869 -> "blurite"
                        2576 -> "iron"
                        6949 -> "sandstone"
                        8128, 8885 -> "gold"
                        43297 -> "mithril"
                        5679 -> "granite"
                        21662 -> "adamantite"
                        34099 -> "runite"
                        43038, 10295, 5008, 6812 -> "rock"
                        15503, 6705, 7475 -> "clay"
                        else -> "depleted"
                    }
                }
                val rock = if (single) {
                    "old"
                } else {
                    when (rockId) {
                        7054 -> "tutorial_island"
                        7704 -> "falador_mine"
                        7580 -> "rock"
                        7952 -> "mud"
                        16 -> "black"
                        9521 -> "sand"
                        10392 -> "dungeon"
                        7475 -> "light"
                        34927 -> "ice"
                        10266 -> "crandor"
                        10258 -> "lumbridge_cellar"
                        7710 -> "lava_maze_dungeon"
                        8373 -> "arandar"
                        6040 -> "rimmington"
                        8101, 5559, 6829 -> "quarry"
                        5790 -> "feldip_hills"
                        41 -> "piscatoris"
                        10398 -> "misc_expansion"
                        2581 -> "tzhaar"
                        57 -> "ingneous"
                        6812 -> "tourist_trap"
                        32916 -> "ancient_cavern"
                        6930 -> "dirt"
                        6554 -> "rat_pit"
                        10396 -> "grey"
                        37 -> "tourist_trap_dark"
                        else -> "unknown"
                    }
                }
                val type = when {
                    else -> {
                        var result = 0
                        for (array in models) {
                            for (model in array) {
                                if (map.containsKey(model)) {
                                    result = map[model]!!
                                    break
                                }
                            }
                        }
                        result
                    }
                }
                println("${ore}_rocks_${rock}_$type:\n  id: $id")
            }
        }
    }
}
