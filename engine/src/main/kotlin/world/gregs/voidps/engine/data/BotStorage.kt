package world.gregs.voidps.engine.data

/** Keeps bot saves separate from accounts that can log in as players. */
class BotStorage(private val players: Storage, private val bots: Storage) : Storage by players {
    override fun save(accounts: List<PlayerSave>) {
        val (botAccounts, playerAccounts) = accounts.partition { it.bot }
        if (playerAccounts.isNotEmpty()) players.save(playerAccounts)
        if (botAccounts.isNotEmpty()) bots.save(botAccounts)
    }

    override fun loadBot(accountName: String): PlayerSave? = bots.load(accountName)?.takeIf { it.bot }

    override fun botNames() = bots.names()
}
