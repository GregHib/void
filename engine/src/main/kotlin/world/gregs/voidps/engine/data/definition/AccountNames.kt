package world.gregs.voidps.engine.data.definition

/** One availability check shared by account creation, renaming and bot spawning. */
class AccountNames(private val players: AccountDefinitions, private val bots: BotAccountDefinitions) {
    private val activeBots = mutableMapOf<String, Boolean>()
    fun used(name: String, owner: AccountNameRegistry? = null, accountName: String? = null): Boolean =
        players.used(name, if (owner === players) accountName else null) ||
            bots.used(name, if (owner === bots) accountName else null)

    @Synchronized
    fun reserveBot(name: String): Boolean {
        val key = name.lowercase().replace('_', ' ')
        if (used(name, bots, name) || key in activeBots) return false
        activeBots[key] = bots.getByAccount(name) == null
        bots.reserve(name)
        return true
    }

    @Synchronized
    fun releaseBot(name: String, discardNew: Boolean = false) {
        val wasNew = activeBots.remove(name.lowercase().replace('_', ' '))
        if (discardNew && wasNew == true) bots.discard(name)
    }

    fun isBot(name: String): Boolean = bots.used(name)
}
