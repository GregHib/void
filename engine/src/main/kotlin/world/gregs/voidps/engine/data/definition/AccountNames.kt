package world.gregs.voidps.engine.data.definition

/** One availability check shared by account creation, renaming and bot spawning. */
class AccountNames(private val players: AccountDefinitions, private val bots: BotAccountDefinitions) {
    private val activeBots = mutableSetOf<String>()
    fun used(name: String, owner: AccountNameRegistry? = null, accountName: String? = null): Boolean =
        players.used(name, if (owner === players) accountName else null) ||
            bots.used(name, if (owner === bots) accountName else null)

    @Synchronized
    fun reserveBot(name: String): Boolean {
        val key = name.lowercase().replace('_', ' ')
        if (used(name, bots, name) || !activeBots.add(key)) return false
        bots.reserve(name)
        return true
    }

    @Synchronized
    fun releaseBot(name: String) {
        activeBots.remove(name.lowercase().replace('_', ' '))
    }

    fun isBot(name: String): Boolean = bots.used(name)
}
