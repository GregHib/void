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

    private fun toJson(save: PlayerSave) = """
        {
          "accountName": "${save.name}",
          "passwordHash": "${save.password}",
          "tile": {
            "x": ${save.tile.x},
            "y": ${save.tile.y},
            "level": ${save.tile.level},
          },
          "experience": {
            "experience": [ ${save.experience.joinToString(", ")} ],
            "blocked": [ ${save.blocked.map { it.ordinal }.joinToString(", ")} ]
          },
          "levels": [ ${save.levels.joinToString(", ")} ],
          "male": ${save.male},
          "looks": [ ${save.looks.joinToString(", ")} ],
          "colours": [ ${save.colours.joinToString(", ")} ],
          "variables": {
            ${save.variables.toList().joinToString(", ") { "\"${it.first}\": ${it.second}" }},
          },
          "inventories": {
            ${save.inventories.toList().joinToString(", ") { (id, items) -> "{ \"${id}\": ${items.joinToString(", ") { item -> "\"id\": \"${item.id}\", \"amount\": ${item.amount} }" }}" }}
          },
          "friends": {
            ${save.friends.toList().joinToString(", ") { "\"${it.first}\": ${it.second}" }},
          },
          "ignores": [ ${save.ignores.joinToString(", ")} ],
        }
    """.trimIndent()

    override fun exists(accountName: String): Boolean = false

    override fun load(accountName: String): PlayerSave? = null
}