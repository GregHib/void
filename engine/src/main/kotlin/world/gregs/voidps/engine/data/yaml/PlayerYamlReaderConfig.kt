package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.chat.clan.ClanRank
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.exp.Experience
import world.gregs.voidps.engine.entity.character.player.skill.level.Levels
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.voidps.type.Tile
import world.gregs.yaml.read.YamlReaderConfiguration

internal class PlayerYamlReaderConfig(
    private val itemDefinitions: ItemDefinitions
) : YamlReaderConfiguration() {
    override fun add(list: MutableList<Any>, value: Any, parentMap: String?) {
        if (value is Map<*, *> && value.containsKey("id")) {
            val id = value["id"] as String
            val item = Item(id, value["amount"] as? Int ?: 0, itemDefinitions.get(id))
            super.add(list, item, parentMap)
        } else if (value is Map<*, *> && value.isEmpty()) {
            super.add(list, Item.EMPTY, parentMap)
        } else {
            super.add(list, when (parentMap) {
                "blocked" -> Skill.valueOf(value as String)
                "friends" -> ClanRank.valueOf(value as String)
                else -> value
            }, parentMap)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun set(map: MutableMap<String, Any>, key: String, value: Any, indent: Int, parentMap: String?) {
        if (parentMap == "tile") {
            super.set(map, key, Tile.fromMap(value as Map<String, Any>), indent, parentMap)
        } else if (key == "experience" && value is Map<*, *>) {
            value as Map<String, Any>
            val exp = Experience(
                experience = (value["experience"] as List<Double>).toDoubleArray(),
                blocked = (value["blocked"] as List<Skill>).toMutableSet()
            )
            super.set(map, key, exp, indent, parentMap)
        } else if (key == "levels") {
            value as List<Int>
            super.set(map, key, Levels(value.toIntArray()), indent, parentMap)
        } else if (key == "looks" || key == "colours") {
            value as List<Int>
            super.set(map, key, value.toIntArray(), indent, parentMap)
        } else {
            super.set(map, key, value, indent, parentMap)
        }
    }
}