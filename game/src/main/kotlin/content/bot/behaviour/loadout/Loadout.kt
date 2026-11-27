package content.bot.behaviour.loadout

import content.bot.behaviour.condition.BotEquipmentSetup
import content.bot.behaviour.condition.BotInventorySetup
import content.bot.behaviour.condition.BotItem
import content.bot.behaviour.condition.Condition
import content.entity.player.bank.bank
import content.entity.player.bank.ownsItem
import world.gregs.voidps.engine.data.definition.ItemDefinitions
import world.gregs.voidps.engine.entity.character.player.Player
import world.gregs.voidps.engine.entity.character.player.skill.Skill
import world.gregs.voidps.engine.entity.character.player.skill.level.Level
import world.gregs.voidps.engine.inv.add
import world.gregs.voidps.network.login.protocol.visual.update.player.EquipSlot

/**
 * A set of equipment (and the inventory items it depends on) a bot uses for an activity.
 * Slots either list explicit item [LoadoutSlot.ids] or reference a [Gear] list to pick the best usable item from.
 * Defined in `*.loadouts.toml` or inline in a template's `loadouts` (hybrid bots).
 */
data class Loadout(
    val id: String,
    val slots: Map<EquipSlot, LoadoutSlot>,
    val inventory: List<BotItem> = emptyList(),
    val autocast: String? = null,
    val weight: Int = 1,
) {

    /**
     * Every candidate item for each slot, used by hybrid swaps and kit grants which don't select per bot.
     */
    val equipment: BotEquipmentSetup by lazy {
        BotEquipmentSetup(slots.mapValues { (_, slot) -> slot.item(slot.candidates.toSet()) })
    }

    val extraInventory: BotInventorySetup?
        get() = if (inventory.isEmpty()) null else BotInventorySetup(inventory)

    /**
     * For each required gear slot, the distinct requirements of its items (any one of which needs meeting).
     * Slots containing an item without requirements can always be met so are left out.
     */
    private val requirements: List<List<List<Pair<Skill, Int>>>> by lazy {
        slots.values.mapNotNull { slot ->
            val gear = slot.gear
            if (slot.optional || gear == null) {
                return@mapNotNull null
            }
            val options = gear.items.map { it.requirements }.distinct()
            if (options.any { it.isEmpty() }) null else options
        }
    }

    /**
     * Skills which affect whether a bot [qualifies] for this loadout
     */
    val skills: Set<Skill>
        get() = requirements.flatMapTo(mutableSetOf()) { options ->
            options.flatMap { reqs -> reqs.map { it.first } }
        }

    /**
     * Whether [player] has the levels to equip at least one item in every required gear slot
     */
    fun qualifies(player: Player): Boolean {
        for (options in requirements) {
            if (options.none { reqs -> meets(player, reqs) }) {
                return false
            }
        }
        return true
    }

    /**
     * Raise [player]'s levels to the easiest requirements of each required gear slot
     */
    fun grantLevels(player: Player) {
        for (options in requirements) {
            if (options.any { meets(player, it) }) {
                continue
            }
            val easiest = options.minBy { reqs -> reqs.sumOf { (skill, level) -> (level - player.levels.getMax(skill)).coerceAtLeast(0) } }
            for ((skill, level) in easiest) {
                if (player.levels.getMax(skill) >= level) {
                    continue
                }
                player.experience.set(skill, Level.experience(skill, level))
                player.levels.set(skill, level)
            }
        }
    }

    /**
     * Pick the concrete items [player] should use for each slot.
     * Gear slots use the best item the player can equip and owns, otherwise required slots use the best they can [obtain].
     * Optional slots only use owned items (so a failed purchase can't block an activity) and are dropped otherwise.
     * Required slots with nothing owned or obtainable fall back to the best equippable item.
     */
    fun bind(player: Player, obtain: (id: String, amount: Int) -> Boolean): Map<EquipSlot, BotItem> = select(
        player,
        owned = { id, amount -> player.ownsItem(id, amount) },
        obtain = obtain,
    )

    /**
     * Give [player] the best item they can equip for every slot (including optional ones) plus the loadout's inventory, all in the bank.
     */
    fun grant(player: Player) {
        for ((_, item) in select(player, fillOptional = true, owned = { _, _ -> true }, obtain = { _, _ -> false })) {
            val id = item.ids.firstOrNull { it != "empty" } ?: continue
            player.bank.add(id, item.min ?: 1)
        }
        for ((ids, min) in inventory) {
            val id = ids.firstOrNull { it != "empty" } ?: continue
            player.bank.add(id, min ?: 1)
        }
    }

    private inline fun select(
        player: Player,
        fillOptional: Boolean = false,
        owned: (String, Int) -> Boolean,
        obtain: (String, Int) -> Boolean,
    ): Map<EquipSlot, BotItem> {
        val selected = LinkedHashMap<EquipSlot, BotItem>(slots.size)
        val shield = slots[EquipSlot.Shield]
        val excludeTwoHanded = shield != null && !shield.optional
        var twoHanded = false
        // Weapon first as a two-handed weapon removes the shield
        for ((slot, spec) in slots.entries.sortedBy { if (it.key == EquipSlot.Weapon) 0 else 1 }) {
            val gear = spec.gear
            if (gear == null) {
                selected[slot] = spec.item(spec.ids)
                continue
            }
            if (slot == EquipSlot.Shield && twoHanded) {
                continue
            }
            val amount = spec.min ?: 1
            var best: GearItem? = null
            var obtainable: GearItem? = null
            var chosen: GearItem? = null
            for (item in gear.items) {
                if (excludeTwoHanded && item.twoHanded) {
                    continue
                }
                if (!item.canEquip(player)) {
                    continue
                }
                if (best == null) {
                    best = item
                }
                if (owned(item.id, amount)) {
                    chosen = item
                    break
                }
                if (obtainable == null && !spec.optional && obtain(item.id, amount)) {
                    obtainable = item
                }
            }
            chosen = chosen ?: obtainable ?: if (spec.optional && !fillOptional) null else best
            if (chosen == null) {
                if (!spec.optional) {
                    // Nothing equippable; leave all candidates for the resolvers to fail on
                    selected[slot] = spec.item(gear.ids.toSet())
                }
                continue
            }
            if (slot == EquipSlot.Weapon && chosen.twoHanded) {
                twoHanded = true
            }
            selected[slot] = spec.item(setOf(chosen.id))
        }
        return selected
    }

    private fun meets(player: Player, requirements: List<Pair<Skill, Int>>): Boolean {
        for ((skill, level) in requirements) {
            if (player.levels.getMax(skill) < level) {
                return false
            }
        }
        return true
    }

    /**
     * Check gear slots reference gear for the same equipment slot
     */
    fun validate() {
        if (!ItemDefinitions.loaded) {
            return
        }
        for ((slot, spec) in slots) {
            val gear = spec.gear ?: continue
            for ((id1, slot1) in gear.items) {
                require(slot1 == slot) { "Loadout '$id' slot ${slot.name.lowercase()} references gear '${gear.id}' with ${slot1.name.lowercase()} item '$id1'." }
            }
        }
    }

    override fun toString() = "Loadout($id)"

    companion object {
        /**
         * Parse a loadout from a map of equipment slot names to items, optionally nested inside an `equipment` map.
         * Slot values accept `id` or `gear` along with `min`, `max`, `optional`, `usable` and `equippable`.
         */
        @Suppress("UNCHECKED_CAST")
        fun parse(id: String, map: Map<String, Any>, gear: (String) -> Gear?): Loadout {
            val slots = LinkedHashMap<EquipSlot, LoadoutSlot>()
            var inventory: List<BotItem> = emptyList()
            var autocast: String? = null
            var weight = 1
            fun slot(key: String, value: Any?) {
                val slot = EquipSlot.by(key)
                require(slot != EquipSlot.None) { "Invalid equipment slot '$key' in loadout '$id'." }
                val spec = value as? Map<String, Any> ?: error("Loadout '$id' slot '$key' expecting map, found: $value")
                slots[slot] = LoadoutSlot.parse(spec, gear) { "loadout '$id' slot '$key'" }
            }
            for ((key, value) in map) {
                when (key) {
                    "inventory" -> {
                        val list = when (value) {
                            is Map<*, *> -> listOf(value as Map<String, Any>)
                            is List<*> -> value as List<Map<String, Any>>
                            else -> error("Loadout '$id' inventory expecting list, found: $value")
                        }
                        if (list.isNotEmpty()) {
                            inventory = (Condition.parse(listOf("inventory" to list), "loadout $id").single() as BotInventorySetup).items
                        }
                    }
                    "autocast" -> autocast = value as String
                    "weight" -> weight = value as Int
                    "equipment" -> for ((slotKey, slotValue) in value as Map<String, Any>) {
                        slot(slotKey, slotValue)
                    }
                    else -> slot(key, value)
                }
            }
            return Loadout(id, slots, inventory, autocast, weight)
        }
    }
}
