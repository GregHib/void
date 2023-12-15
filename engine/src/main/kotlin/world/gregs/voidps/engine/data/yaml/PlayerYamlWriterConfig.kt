package world.gregs.voidps.engine.data.yaml

import world.gregs.voidps.engine.data.PlayerSave
import world.gregs.voidps.engine.entity.item.Item
import world.gregs.yaml.write.YamlWriterConfiguration

internal class PlayerYamlWriterConfig : YamlWriterConfiguration(forceQuoteStrings = true, forceExplicit = true, forceQuoteKeys = true, formatExplicitMap = true) {
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
        } else if (value is PlayerSave) {
            mapOf(
                "accountName" to value.name,
                "passwordHash" to value.password,
                "tile" to mapOf(
                    "x" to value.tile.x,
                    "y" to value.tile.y,
                    "level" to value.tile.level,
                ),
                "experience" to mapOf(
                    "experience" to value.experience,
                    "blocked" to value.blocked
                ),
                "levels" to value.levels,
                "male" to value.male,
                "looks" to value.looks,
                "colours" to value.colours,
                "variables" to value.variables,
                "inventories" to value.inventories,
                "friends" to value.friends,
                "ignores" to value.ignores
            )
        } else {
            super.write(value, indent, parentMap)
        }
    }
}