package world.gregs.voidps.engine.data.definition

import world.gregs.voidps.engine.data.Storage
import world.gregs.voidps.engine.data.config.AccountDefinition
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.name
import world.gregs.voidps.engine.entity.character.player.previousName

/** Bot identities reserve names without participating in player authentication. */
class BotAccountDefinitions : AccountNameRegistry() {
    fun load(storage: Storage): BotAccountDefinitions {
        merge(storage.botNames()) { false }
        return this
    }

    fun merge(names: Map<String, AccountDefinition>, skip: (String) -> Boolean) {
        for (definition in names.values) {
            if (skip(definition.accountName)) continue
            displayNames[definition.accountName.lowercase()]?.let { definitions.remove(key(it)) }
            definitions[key(definition.displayName)] = definition.copy(passwordHash = "")
            displayNames[definition.accountName.lowercase()] = definition.displayName
        }
        indexNames()
    }

    fun reserve(name: String) {
        if (getByAccount(name) == null) register(AccountDefinition(name, name, "", ""))
    }

    fun discard(accountName: String) = unregister(accountName)

    fun add(player: Player) {
        register(AccountDefinition(player.accountName, player.name, player.previousName, ""))
    }
}
