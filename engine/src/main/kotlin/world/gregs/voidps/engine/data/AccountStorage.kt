package world.gregs.voidps.engine.data

import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.entity.character.player.chat.clan.Clan

interface AccountStorage {
    fun names(): Map<String, AccountDefinition>
    fun clans(): Map<String, Clan>
    fun save(accounts: List<PlayerSave>)
    fun exists(accountName: String): Boolean
    fun load(accountName: String) : PlayerSave?
}