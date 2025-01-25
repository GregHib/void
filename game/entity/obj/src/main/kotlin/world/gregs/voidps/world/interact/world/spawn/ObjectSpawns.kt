package world.gregs.voidps.world.interact.world.spawn

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.ObjectDefinitions
import world.gregs.voidps.engine.entity.World
import world.gregs.voidps.engine.entity.obj.GameObject
import world.gregs.voidps.engine.entity.obj.GameObjects
import world.gregs.voidps.engine.get
import world.gregs.voidps.type.Tile
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.world.interact.entity.obj.Teleports
import world.gregs.voidps.world.interact.entity.player.music.MusicTracks
import world.gregs.yaml.Yaml
import world.gregs.yaml.read.YamlReaderConfiguration

@Suppress("UNCHECKED_CAST")
fun loadObjectSpawns(
    objects: GameObjects,
    yaml: Yaml = get(),
    path: String = Settings["spawns.objects"],
    definitions: ObjectDefinitions = get(),
) = timedLoad("object spawn") {
    objects.reset()
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
            val type = value["type"] as Int
            val rotation = value["rotation"] as? Int ?: 0
            count++
            objects.add(GameObject(definitions.get(id).id, tile.x, tile.y, tile.level, type, rotation))
        }
    }
    yaml.load<Any>(path, config)
    count
}
