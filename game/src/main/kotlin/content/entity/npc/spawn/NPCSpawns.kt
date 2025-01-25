package content.entity.npc.spawn

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.type.Direction
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.character.npc.NPCs
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.timedLoad
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

@Suppress("UNCHECKED_CAST")
fun loadNpcSpawns(
    npcs: NPCs,
    yaml: Yaml = get(),
    path: String = Settings["spawns.npcs"]
) {
    timedLoad("npc spawn") {
        npcs.clear()
        val membersWorld = World.members
        var count = 0
        val config = object : YamlReaderConfiguration() {
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
        yaml.load<Any>(path, config)
        count
    }
}