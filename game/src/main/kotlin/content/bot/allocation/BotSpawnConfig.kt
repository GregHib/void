package content.bot.allocation

/**
 * Shared spawn settings for a [BotSpawnStrategy]
 * @param count target number of bots, overridden by the [countSetting] setting when set
 * @param spawnSeconds seconds between spawns, overridden by the [spawnSecondsSetting] setting when set
 * @param refill keep topping up to [count] as bots leave, otherwise stop once [count] have been spawned
 * @param saved whether previously saved bot accounts may be reused (see `bots.spawnSavedChance`)
 * @param starterItems items given to brand-new accounts (and to role bots each time they're provisioned)
 * @param restockAreas areas which re-provision a role bot on entry (e.g. restocking after teleporting out)
 */
data class BotSpawnConfig(
    val id: String,
    val count: Int = 0,
    val countSetting: String? = null,
    val spawnSeconds: Int = 60,
    val spawnSecondsSetting: String? = null,
    val spawnAreas: List<String> = emptyList(),
    val refill: Boolean = false,
    val saved: Boolean = false,
    val starterItems: List<Pair<String, Int>> = emptyList(),
    val restockAreas: Set<String> = emptySet(),
)
