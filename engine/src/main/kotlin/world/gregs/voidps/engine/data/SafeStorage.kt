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
    private val directory: File,
) : AccountStorage {

    override fun names(): Map<String, AccountDefinition> = emptyMap()

    override fun clans(): Map<String, Clan> = emptyMap()

    override fun save(accounts: List<PlayerSave>) {
        directory.mkdirs()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
        val current = LocalDateTime.now().format(formatter)
        for (account in accounts) {
            val file = directory.resolve("$current-${account.name}.toml")
            file.writeText(toJson(account))
        }
    }

    private fun toJson(save: PlayerSave): String = buildString {
        appendLine("accountName = \"${save.name}\"")
        appendLine("passwordHash = \"${save.password}\"")
        appendLine("experience = [ ${save.experience.joinToString(", ")} ]")
        appendLine("blocked_skills = [ ${save.blocked.joinToString(", ") { "\"${it.name}\"" }} ]")
        appendLine("levels = [ ${save.levels.joinToString(", ")} ]")
        appendLine("male = ${save.male}")
        appendLine("looks = [ ${save.looks.joinToString(", ")} ]")
        appendLine("colours = [ ${save.colours.joinToString(", ")} ]")
        appendLine()
        appendLine("[tile]")
        appendLine("x = ${save.tile.x}")
        appendLine("y = ${save.tile.y}")
        appendLine("level = ${save.tile.level}")
        appendLine()
        appendLine("[variables]")
        for ((key, value) in save.variables) {
            appendLine(
                "$key = ${
                    when (value) {
                        is String -> "\"${value}\""
                        is Collection<*> -> "[${value.map { if (it is String)"\"${it}\"" else it }.joinToString(", ")}]"
                        else -> value
                    }
                }",
            )
        }
        appendLine()
        appendLine("[inventories]")
        for ((inv, items) in save.inventories) {
            appendLine("$inv = [${items.joinToString(", ") { if (it.isEmpty()) "{}" else "{id = \"${it.id}\", amount = ${it.amount}}" }}]")
        }
        appendLine()
        appendLine("[social]")
        appendLine("friends = {${save.friends.toList().joinToString(", ") { "\"${it.first}\" = \"${it.second}\""}}}")
        appendLine("ignores = [${save.ignores.joinToString(", ") { "\"${it}\"" }}]")
        appendLine()
        appendLine("[exchange]")
        appendLine("offers = [${save.offers.joinToString(", ") { if (it.isEmpty()) "{}" else "{id = ${it.id}}, item = \"${it.item}\", amount = ${it.amount}, price = ${it.price}, state = \"${it.state.name}\", completed = ${it.completed}, coins = ${it.coins}}" }}]")
        appendLine("history = [${save.history.joinToString(", ") { "{item = \"${it.item}\", amount = ${it.amount}, price = ${it.price}}" }}]")
    }

    override fun exists(accountName: String): Boolean = false

    override fun load(accountName: String): PlayerSave? = null
}
