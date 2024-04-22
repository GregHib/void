package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * A backup storage method to use when the primary one fails
 */
class SafeStorage(
    private val directory: File
) : AccountStorage {

    override fun names(): Map<String, AccountDefinition> = emptyMap()

    override fun clans(): Map<String, Clan> = emptyMap()

    override fun save(accounts: List<PlayerSave>) {
        directory.mkdirs()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
        val current = LocalDateTime.now().format(formatter)
        for (account in accounts) {
            val file = directory.resolve("$current-${account.name}.json")
            file.writeText(toJson(account))
        }
    }

    private fun toJson(save: PlayerSave): String {
        val variables = save.variables.toList().joinToString(",\n    ") { (key, value) ->
            "\"${key}\": ${
                when (value) {
                    is String -> "\"${value}\""
                    is Collection<*> -> if (value.all { it is String }) value.map { "\"${it}\"" } else value
                    else -> value
                }
            }"
        }
        val inventories = save.inventories.toList()
            .joinToString(",\n    ") { (id, items) -> "\"${id}\": [ ${items.joinToString(", ") { item -> if (item.isEmpty()) "{}" else "{ \"id\": \"${item.id}\", \"amount\": ${item.value} }" }} ]" }
        return buildString {
            appendLine("{")
            appendLine("  \"accountName\": \"${save.name}\",")
            appendLine("  \"passwordHash\": \"${save.password}\",")
            appendLine("  \"tile\": {")
            appendLine("    \"x\": ${save.tile.x},")
            appendLine("    \"y\": ${save.tile.y},")
            appendLine("    \"level\": ${save.tile.level}")
            appendLine("  },")
            appendLine("  \"experience\": {")
            appendLine("    \"experience\": [ ${save.experience.joinToString(", ")} ],")
            appendLine("    \"blocked\": [ ${save.blocked.joinToString(", ") { "\"${it.name}\"" }} ]")
            appendLine("  },")
            appendLine("  \"levels\": [ ${save.levels.joinToString(", ")} ],")
            appendLine("  \"male\": ${save.male},")
            appendLine("  \"looks\": [ ${save.looks.joinToString(", ")} ],")
            appendLine("  \"colours\": [ ${save.colours.joinToString(", ")} ],")
            appendLine("  \"variables\": {")
            append("    ")
            appendLine(variables)
            appendLine("  },")
            appendLine("  \"inventories\": {")
            append("    ")
            appendLine(inventories)
            appendLine("  },")
            appendLine("  \"friends\": {")
            append("    ")
            appendLine(save.friends.toList().joinToString(",") { "\"${it.first}\": \"${it.second}\"" })
            appendLine("  },")
            appendLine("  \"ignores\": [ ${save.ignores.joinToString(", ") { "\"${it}\"" }} ]")
            append("}")
        }
    }

    override fun exists(accountName: String): Boolean = false

    override fun load(accountName: String): PlayerSave? = null
}