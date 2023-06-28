package world.gregs.voidps.world.interact.world.spawn

import world.gregs.voidps.engine.entity.Direction
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.get
import world.gregs.voidps.engine.getProperty
import world.gregs.voidps.engine.map.Tile
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.YamlParser
import world.gregs.yaml.config.FastUtilConfiguration

@Suppress("UNCHECKED_CAST")
fun loadNpcSpawns(
    npcs: NPCs,
    parser: YamlParser = get(),
    path: String = getProperty("npcSpawnsPath")
) {
    timedLoad("npc spawn") {
        npcs.clear()
        val membersWorld = World.members
        var count = 0
        val config = object : FastUtilConfiguration() {
            override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
                value as Map<String, Any>
                val members = value["members"] as? Boolean ?: false
                if (!membersWorld && members) {
                    return
                }
                val id = value["id"] as String
                val tile = Tile.fromMap(value)
                val direction = value["direction"] as? Direction ?: Direction.NONE
                val delay = value["delay"] as? Int
                npcs.add(id, tile, direction, delay)
                count++
            }

            override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
                if (key == "direction") {
                    super.set(map, key, Direction.valueOf(value as String), indent, parentMap)
                } else {
                    super.set(map, key, value, indent, parentMap)
                }
            }
        }
        parser.load<Any>(path, config)
        count
    }
}