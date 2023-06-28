package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.male
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.yaml.write.YamlWriterConfiguration

internal class PlayerYamlWriterConfig : YamlWriterConfiguration(quoteStrings = true, forceExplicit = true, quoteKeys = true, formatExplicitMap = true) {
    override fun write(value: Any?, indent: Int, parentMap: String?): Any? {
        return if (value is Item) {
            if (value.isEmpty()) {
                emptyMap()
            } else {
                val map = mutableMapOf<String, Any>("id" to value.id)
                if (value.amount != 0) {
                    map["amount"] = value.amount
                }
                map
            }
        } else if (value is Player) {
            mapOf(
                "accountName" to value.accountName,
                "passwordHash" to value.passwordHash,
                "tile" to mapOf(
                    "x" to value.tile.x,
                    "y" to value.tile.y,
                    "plane" to value.tile.plane,
                ),
                "experience" to mapOf(
                    "experience" to value.experience.experience,
                    "blocked" to value.experience.blocked
                ),
                "levels" to value.levels.levels,
                "male" to value.male,
                "looks" to value.body.looks,
                "colours" to value.body.colours,
                "variables" to value.variables.data,
                "containers" to value.containers.containers,
                "friends" to value.friends,
                "ignores" to value.ignores
            )
        } else {
            super.write(value, indent, parentMap)
        }
    }
}