package content.bot.combat

/**
 * Registry of [CombatBotContext] implementations. The script in `BotCommands` populates this
 * once at init time; lifecycle dispatchers (playerDeath, entered, ...) look up the owning
 * context by [find]. New minigames register here without touching `BotCommands`.
 */
object CombatBotContexts {

    private val all = mutableListOf<CombatBotContext>()

    fun register(context: CombatBotContext) {
        require(all.none { it.id == context.id }) { "Combat bot context '${context.id}' already registered." }
        all.add(context)
    }

    fun unregister(context: CombatBotContext): Boolean = all.remove(context)

    /** Test-only: clear every registration. */
    internal fun clear() = all.clear()

    fun all(): List<CombatBotContext> = all.toList()

    /** First context whose [CombatBotContext.handles] matches the tier, or null. */
    fun find(tier: CombatTier): CombatBotContext? = all.firstOrNull { it.handles(tier) }

    /** Context that exposes the given arena key, or null if no context owns it. */
    fun forArenaKey(arenaKey: String): CombatBotContext? = all.firstOrNull { arenaKey in it.arenaKeys() }

    /** Union of every context's subscribed areas — used to wire `entered(...)` listeners once. */
    fun subscribedAreas(): Set<String> = all.flatMapTo(mutableSetOf()) { it.subscribedAreas }

    fun loadAll() {
        for (context in all) context.load()
    }
}
