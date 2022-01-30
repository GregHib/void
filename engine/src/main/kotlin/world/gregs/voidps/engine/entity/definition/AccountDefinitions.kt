package world.gregs.voidps.engine.entity.definition

import world.gregs.voidps.engine.client.variable.getVar
import world.gregs.voidps.engine.data.FileStorage
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.chat.Clan
import world.gregs.voidps.engine.entity.character.player.chat.Rank
import world.gregs.voidps.engine.entity.character.update.visual.player.name
import world.gregs.voidps.engine.entity.character.update.visual.player.previousName
import world.gregs.voidps.engine.entity.definition.config.AccountDefinition
import world.gregs.voidps.engine.entity.get
import world.gregs.voidps.engine.timedLoad
import world.gregs.voidps.engine.utility.get
import world.gregs.voidps.engine.utility.getProperty
import java.io.File

/**
 * Stores data about player accounts whether they're online or offline
 */
class AccountDefinitions(
    private val variableDefinitions: VariableDefinitions
) {

    private val definitions = mutableMapOf<String, AccountDefinition>()
    private val displayNames = mutableMapOf<String, String>()
    private val clans = mutableMapOf<String, Clan>()

    fun add(player: Player) {
        displayNames[player.accountName] = player.name
        definitions[player.name] = AccountDefinition(player.accountName, player.name, player.previousName)
        clans[player.name] = Clan(
            owner = player.accountName,
            ownerDisplayName = player.name,
            name = player["clan_name", ""],
            friends = player.friends,
            ignores = player.ignores,
            joinRank = Rank.valueOf(player["clan_join_rank", "Anyone"]),
            talkRank = Rank.valueOf(player["clan_talk_rank", "Anyone"]),
            kickRank = Rank.valueOf(player["clan_kick_rank", "Corporeal"]),
            lootRank = Rank.valueOf(player["clan_loot_rank", "None"]),
            coinShare = player.getVar("coin_share_setting", false)
        )
    }

    fun update(accountName: String, newName: String, previousDisplayName: String) {
        val definition = definitions.remove(previousDisplayName) ?: return
        definitions[newName] = definition
        definition.displayName = newName
        definition.previousName = previousDisplayName
        displayNames[accountName] = newName
    }

    fun account(display: String) = getValue(display).accountName

    fun display(account: String) = displayNames[account]

    fun clan(displayName: String) = clans[displayName]

    fun getByAccount(account: String): AccountDefinition? {
        return get(displayNames[account] ?: return null)
    }

    fun get(key: String) = definitions[key]

    fun getValue(key: String) = definitions.getValue(key)

    fun load(storage: FileStorage = get(), path: String = getProperty("savePath")): AccountDefinitions {
        timedLoad("account") {
            for (save in File(path).listFiles() ?: return@timedLoad 0) {
                val player = storage.load<Player>(save.path)
                player.variables.link(player, variableDefinitions)
                add(player)
            }
            definitions.size
        }
        return this
    }

}