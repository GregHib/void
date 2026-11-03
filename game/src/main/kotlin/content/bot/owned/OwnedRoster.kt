package content.bot.owned

import world.gregs.voidps.engine.data.Settings
import world.gregs.voidps.engine.data.definition.AccountDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.network.login.AccountNames

/**
 * The owner's roster, kept in persistent owner variables (`owned.vars.toml`), one block per slot.
 * A slot is "alive" from purchase until the bot dies; dismissing or logging out keeps it.
 */
object OwnedRoster {
    const val MAX_SLOTS = 4

    val PROFILES = listOf("passive", "melee", "ranged", "mage")
    val AGGRESSION = listOf("passive", "aggressive")

    fun slots(): Int = Settings["bots.owned.perPlayer", MAX_SLOTS].coerceIn(1, MAX_SLOTS)

    fun price(slot: Int): Int = Settings["bots.owned.price.${slot + 1}", DEFAULT_PRICES.getOrElse(slot) { DEFAULT_PRICES.last() }]

    fun renamePrice(): Int = Settings["bots.owned.renamePrice", 10_000]

    fun name(owner: Player, slot: Int): String? = owner.get<String>("owned_bot_${slot}_name")?.takeIf { it.isNotBlank() }

    fun alive(owner: Player, slot: Int): Boolean = owner["owned_bot_${slot}_alive", false]

    fun profile(owner: Player, slot: Int): String = owner["owned_bot_${slot}_profile", "passive"]

    fun aggression(owner: Player, slot: Int): String = owner["owned_bot_${slot}_aggression", "passive"]

    fun knobs(owner: Player, slot: Int): Map<String, String> = owner["owned_bot_${slot}_knobs", ""]
        .split(',')
        .filter { it.contains('=') }
        .associate { it.substringBefore('=') to it.substringAfter('=') }

    fun setKnobs(owner: Player, slot: Int, knobs: Map<String, String>) {
        val value = knobs.entries.joinToString(",") { "${it.key}=${it.value}" }
        if (value.isEmpty()) {
            owner.clear("owned_bot_${slot}_knobs")
        } else {
            owner["owned_bot_${slot}_knobs"] = value
        }
    }

    fun snapshot(owner: Player, slot: Int): List<String> = strings(owner, "owned_bot_${slot}_items")

    /**
     * Lowest slot without a living bot, or null when every slot is taken. Slots are bought in order.
     */
    fun nextSlot(owner: Player): Int? = (0 until slots()).firstOrNull { !alive(owner, it) }

    fun aliveCount(owner: Player): Int = (0 until slots()).count { alive(owner, it) }

    fun purchase(owner: Player, slot: Int, name: String) {
        owner["owned_bot_${slot}_name"] = name
        owner["owned_bot_${slot}_alive"] = true
        owner.clear("owned_bot_${slot}_items")
        // a new player's kit, given at the first summon
        owner["owned_bot_${slot}_kit"] = true
    }

    fun kill(owner: Player, slot: Int) {
        owner["owned_bot_${slot}_alive"] = false
        owner.clear("owned_bot_${slot}_items")
        owner.clear("owned_bot_${slot}_worn")
        owner.clear("owned_bot_${slot}_preset")
    }

    /** The preset last loaded onto the bot, put back on after a flight; none until one is loaded. */
    fun preset(owner: Player, slot: Int): String? = owner.get<String>("owned_bot_${slot}_preset")?.takeIf { it.isNotBlank() }

    fun setPreset(owner: Player, slot: Int, name: String?) {
        if (name == null) owner.clear("owned_bot_${slot}_preset") else owner["owned_bot_${slot}_preset"] = name
    }

    /** What the bot wore when it last left the world, to put back on at the next summon. */
    fun worn(owner: Player, slot: Int): List<String> = strings(owner, "owned_bot_${slot}_worn")

    /** The string entries of a list variable; anything else stored under [key] (an older save's string) counts as none. */
    private fun strings(owner: Player, key: String): List<String> = (owner.get<Any>(key) as? List<*>)?.filterIsInstance<String>() ?: emptyList()

    /**
     * Why [name] can't be a bot's name, or null when it can: the rules real display names follow, and no clash
     * with an existing account's display name (the bot's own account name is never registrable).
     */
    fun validateName(name: String, definitions: AccountDefinitions): String? {
        if (name.length !in 1..AccountNames.MAX_USERNAME_LENGTH) {
            return "A name is 1 to ${AccountNames.MAX_USERNAME_LENGTH} characters long."
        }
        if (!NAME_CHARS.matches(name)) {
            return "A name is letters, numbers and spaces only."
        }
        if (definitions.get(name) != null) {
            return "That name is already taken."
        }
        return null
    }

    private val NAME_CHARS = Regex("^[A-Za-z0-9][A-Za-z0-9 ]*$")

    /**
     * Parses a user-facing slot number (1-4) into an index, or null.
     */
    fun parseSlot(text: String?): Int? {
        val number = text?.toIntOrNull() ?: return null
        val slot = number - 1
        return if (slot in 0 until slots()) slot else null
    }

    private val DEFAULT_PRICES = listOf(500_000, 500_000, 500_000, 500_000)
}
